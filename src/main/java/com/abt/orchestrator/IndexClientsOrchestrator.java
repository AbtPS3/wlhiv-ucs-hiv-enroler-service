package com.abt.orchestrator;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.abt.domain.CTCPatient;
import com.abt.service.OpenSrpService;
import com.abt.util.DateTimeTypeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;

import java.util.List;

import static com.abt.util.Util.fetchOpenMRSIds;
import static com.abt.util.Util.sendRequestToDestination;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Orchestrator for handling requests and responses while sending Index
 * Clients to UCS.
 */
public class IndexClientsOrchestrator extends UntypedActor {

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

    private List<CTCPatient> ctcPatients;

    /**
     * Initializes a new instance of the {@link IndexClientsOrchestrator} class.
     *
     * @param config The mediator configuration.
     */
    public IndexClientsOrchestrator(MediatorConfig config) {
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
            handleMediatorHTTPRequest((MediatorHTTPRequest) msg);
        } else if (msg instanceof MediatorHTTPResponse) {
            handleMediatorHTTPResponse((MediatorHTTPResponse) msg);
        } else {
            unhandled(msg);
        }
    }

    private void handleMediatorHTTPRequest(MediatorHTTPRequest request) {
        originalRequest = request;
        log.info("Received request: {} {} {} {}", request.getHost(),
            request.getMethod(), request.getPath(), request.getBody());

        try {
            Gson gson
                = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .registerTypeAdapter(DateTime.class,
                    new DateTimeTypeConverter())
                .create();

            ctcPatients = gson.fromJson(request.getBody(),
                new TypeToken<List<CTCPatient>>() {
                }.getType());
            validateAndProcessRequest(ctcPatients);
        } catch (Exception e) {
            handleBadRequest();
        }
    }

    private void validateAndProcessRequest(List<CTCPatient> ctcPatients) {
        try {
            JSONArray identifiers = fetchOpenMRSIds(host, port, scheme,
                username, password,
                ctcPatients.size());
            log.info("Received identifiers : " + identifiers.toString());
            for (int i = 0; i < ctcPatients.size(); i++) {
                ctcPatients.get(i).setUniqueId(identifiers.getString(i)
                    .replace("-", ""));
            }
        } catch (Exception e) {
            log.info("Received an error message while getting Identifiers");
            log.error(e.getMessage());
            handleBadRequest();
        }

        String clientsEvents = OpenSrpService.generateClientEvent(ctcPatients);
        sendRequestToDestination(host, port, scheme, username,
            password, clientsEvents, log, originalRequest, getSelf(),
            getContext(), config);
    }


    private void handleBadRequest() {
        FinishRequest finishRequest = new FinishRequest("Bad Request",
            "application/json", SC_BAD_REQUEST);
        originalRequest.getRequestHandler().tell(finishRequest, getSelf());
    }

    private void handleMediatorHTTPResponse(MediatorHTTPResponse response) {
        log.info("Received response with status code :: " + response.getStatusCode());
        FinishRequest finishRequest =
            new FinishRequest(new Gson().toJson(ctcPatients),
                "application/json", SC_OK);
        originalRequest.getRequestHandler().tell(finishRequest, getSelf());
    }


}
