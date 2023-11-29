package tz.go.moh.ucs.orchestrator;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import tz.go.moh.ucs.domain.CTCPatient;
import tz.go.moh.ucs.service.OpenSrpService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

public class DefaultOrchestrator extends UntypedActor {

    /**
     * The mediator configuration.
     */
    private final MediatorConfig config;

    /**
     * Represents a mediator request.
     */
    protected MediatorHTTPRequest originalRequest;

    /**
     * The logger instance.
     */
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    String host;

    int port;

    String scheme;

    String username;

    String password;

    /**
     * Initializes a new instance of the {@link DefaultOrchestrator} class.
     *
     * @param config The mediator configuration.
     */
    public DefaultOrchestrator(MediatorConfig config) {
        this.config = config;
        host = config.getProperty("destination.host");
        port = Integer.parseInt(config.getProperty("destination.port"));
        username = config.getProperty("destination.username");
        password = config.getProperty("destination.password");
        scheme = "http";
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) {
            originalRequest = (MediatorHTTPRequest) msg;
            log.info("Received request: " + originalRequest.getHost() + " " + originalRequest.getMethod() + " " + originalRequest.getPath() + " " + originalRequest.getBody());

            List<CTCPatient> ctcPatients;
            try {
                ctcPatients = new Gson().fromJson(((MediatorHTTPRequest) msg).getBody(), new TypeToken<List<CTCPatient>>() {
                }.getType());
            } catch (Exception e) {
                log.error(e.getMessage());
                FinishRequest finishRequest = new FinishRequest("Bad Request", "application/json", SC_BAD_REQUEST);
                (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
                return;
            }

            //Obtain unique ids for registering clients into UCS
            JSONArray identifiers = fetchOpenMRSIds(ctcPatients.size());
            for (int i = 0; i < ctcPatients.size(); i++) {
                ctcPatients.get(i).setUniqueId(identifiers.getString(i).replace("-", ""));
            }

            String clientsEvents = OpenSrpService.generateClientEvent(ctcPatients);


            String url = null;
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
            List<Pair<String, String>> parameters = new ArrayList<>();

            if (config.getDynamicConfig().isEmpty()) {
                log.debug("Dynamic config is empty, using config from mediator.properties");

                // if we have a username and a password
                // we want to add the username and password as the Basic Auth header in the HTTP request
                if (username != null && !"".equals(username) && password != null && !"".equals(password)) {
                    String auth = username + ":" + password;
                    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
                    String authHeader = "Basic " + new String(encodedAuth);
                    headers.put(HttpHeaders.AUTHORIZATION, authHeader);
                }
            }

            url = scheme + "://" + host + ":" + port + "/opensrp/rest/event/add";

            MediatorHTTPRequest request = new MediatorHTTPRequest(originalRequest.getRequestHandler(), getSelf(), host, "POST",
                    url, clientsEvents, headers, parameters);

            ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
            httpConnector.tell(request, getSelf());
        } else if (msg instanceof MediatorHTTPResponse) { //respond
            log.info("Received response from target system");

            FinishRequest finishRequest = ((MediatorHTTPResponse) msg).toFinishRequest();
            (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
        } else {
            unhandled(msg);
        }
    }


    private JSONArray fetchOpenMRSIds(int numberToGenerate) throws Exception {
        String path = "/opensrp/uniqueids/get?source=2&numberToGenerate=" + numberToGenerate;

        String url = scheme + "://" + host + ":" + port + path;
        System.out.println("URL: " + url);

        return new JSONObject(sendGetRequest(url, username, password)).getJSONArray("identifiers");
    }


    private static String sendGetRequest(String url, String username, String password) throws IOException {
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();

        // Set the request method to GET
        connection.setRequestMethod("GET");

        // Set up basic authentication
        String credentials = username + ":" + password;

        String encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes(StandardCharsets.ISO_8859_1)));
        connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);

        // Get the response code
        int responseCode = connection.getResponseCode();

        // Read the response from the server
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        // Close the connection
        connection.disconnect();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            return response.toString();
        } else {
            throw new IOException("Failed to get response. Response Code: " + responseCode);
        }
    }

}
