package com.abt.util;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActorContext;
import akka.event.LoggingAdapter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;

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

public class Util {
    public static String sendGetRequest(String url, String username,
                                        String password) throws IOException {
        URL urlObject = new URL(url);
        HttpURLConnection connection =
            (HttpURLConnection) urlObject.openConnection();
        connection.setConnectTimeout(60000);

        // Set the request method to GET
        connection.setRequestMethod("GET");

        // Set up basic authentication
        String credentials = username + ":" + password;

        String encodedCredentials =
            new String(Base64.encodeBase64(credentials
                .getBytes(StandardCharsets.ISO_8859_1)));
        connection.setRequestProperty("Authorization",
            "Basic " + encodedCredentials);

        // Get the response code
        int responseCode = connection.getResponseCode();

        // Read the response from the server
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader =
                 new BufferedReader(
                     new InputStreamReader(connection.getInputStream()))) {
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
            throw new IOException("Failed to get response. Response Code: "
                + responseCode);
        }
    }


    public static JSONArray fetchOpenMRSIds(String host, int port,
                                            String scheme,
                                            String username, String password,
                                            int numberToGenerate) throws Exception {
        String path =
            "/opensrp/uniqueids/get?source=2&numberToGenerate="
                + numberToGenerate;
        String url = scheme + "://" + host + ":" + port + path;
        System.out.println("URL: " + url);
        return new JSONObject(sendGetRequest(url, username, password))
            .getJSONArray("identifiers");
    }

    public static void configureBasicAuthHeader(String username,
                                                String password,
                                                Map<String, String> headers) {
        if (
            username != null &&
                !username.isEmpty() &&
                password != null &&
                !password.isEmpty()
        ) {
            String auth = username + ":" + password;
            byte[] encodedAuth =
                Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.put(HttpHeaders.AUTHORIZATION, authHeader);
        }
    }

    public static void sendRequestToDestination(String host, int port,
                                                String scheme,
                                                String username,
                                                String password,
                                                String clientsEvents,
                                                LoggingAdapter log,
                                                MediatorHTTPRequest originalRequest,
                                                ActorRef selfActor,
                                                UntypedActorContext context,
                                                MediatorConfig config) {
        String url = scheme + "://" + host + ":" + port + "/opensrp/rest" +
            "/event/add";

        log.info("Sending Requests to URL::" + url);
        log.info("Sending Payload ::" + clientsEvents);

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        List<Pair<String, String>> parameters = new ArrayList<>();

        configureBasicAuthHeader(username, password, headers);

        MediatorHTTPRequest newRequest =
            new MediatorHTTPRequest(originalRequest.getRequestHandler(),
                selfActor, host, "POST",
                url, clientsEvents, headers, parameters);

        ActorSelection httpConnector =
            context.actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(newRequest, selfActor);
    }
}
