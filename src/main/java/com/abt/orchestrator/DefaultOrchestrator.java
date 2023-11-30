package com.abt.orchestrator;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.abt.domain.CTCPatient;
import com.abt.service.OpenSrpService;
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

/**
 * Default orchestrator for handling requests and responses.
 */
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

    private static String sendGetRequest(String url, String username, String password) throws IOException {
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setConnectTimeout(60000);

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

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) {
            handleMediatorHTTPRequest((MediatorHTTPRequest) msg);
        } else if (msg instanceof MediatorHTTPResponse) {
            handleMediatorHTTPResponse((MediatorHTTPResponse) msg);
        } else {
            unhandled(msg);
        }
    }

    private void handleMediatorHTTPRequest(MediatorHTTPRequest request) {
        originalRequest = request;
        log.info("Received request: {} {} {} {}", request.getHost(), request.getMethod(), request.getPath(), request.getBody());

        try {
            List<CTCPatient> ctcPatients = new Gson().fromJson(request.getBody(), new TypeToken<List<CTCPatient>>() {
            }.getType());
            validateAndProcessRequest(ctcPatients);
        } catch (Exception e) {
            handleBadRequest();
        }
    }

    private void validateAndProcessRequest(List<CTCPatient> ctcPatients) {
        try {
            JSONArray identifiers = fetchOpenMRSIds(ctcPatients.size());
            log.info("Successfully Received identifiers : " + identifiers.toString());
            for (int i = 0; i < ctcPatients.size(); i++) {
                ctcPatients.get(i).setUniqueId(identifiers.getString(i).replace("-", ""));
            }
        } catch (Exception e) {
            log.info("Received an error message while getting Identifiers");
            log.error(e.getMessage());
            handleBadRequest();
        }

        String clientsEvents = OpenSrpService.generateClientEvent(ctcPatients);
        sendRequestToDestination(clientsEvents);
    }

    private void sendRequestToDestination(String clientsEvents) {
        String url = scheme + "://" + host + ":" + port + "/opensrp/rest/event/add";

        log.info("Sending Requests to URL::" + url);
        log.info("Sending Payload ::" + clientsEvents);

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        List<Pair<String, String>> parameters = new ArrayList<>();

        if (config.getDynamicConfig().isEmpty()) {
            configureBasicAuthHeader(headers);
        }

        MediatorHTTPRequest newRequest = new MediatorHTTPRequest(originalRequest.getRequestHandler(), getSelf(), host, "POST",
                url, clientsEvents, headers, parameters);

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(newRequest, getSelf());
    }

    private void configureBasicAuthHeader(Map<String, String> headers) {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.put(HttpHeaders.AUTHORIZATION, authHeader);
        }
    }

    private void handleBadRequest() {
        FinishRequest finishRequest = new FinishRequest("Bad Request", "application/json", SC_BAD_REQUEST);
        originalRequest.getRequestHandler().tell(finishRequest, getSelf());
    }

    private void handleMediatorHTTPResponse(MediatorHTTPResponse response) {
        log.info("Received response from target system :: " + response.getBody());
        FinishRequest finishRequest = response.toFinishRequest();
        originalRequest.getRequestHandler().tell(finishRequest, getSelf());
    }

    private JSONArray fetchOpenMRSIds(int numberToGenerate) throws Exception {
        String path = "/opensrp/uniqueids/get?source=2&numberToGenerate=" + numberToGenerate;
        String url = scheme + "://" + host + ":" + port + path;
        System.out.println("URL: " + url);
        return new JSONObject(sendGetRequest(url, username, password)).getJSONArray("identifiers");
    }

}
