package com.abt.orchestrator.mock;


import org.json.JSONObject;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.testing.MockHTTPConnector;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Represents a mock destination.
 */
public class IndexClientMockDestination extends MockHTTPConnector {

    /**
     * Gets the response.
     *
     * @return Returns the response.
     */
    @Override
    public String getResponse() {
        return "";
    }

    /**
     * Gets the status code.
     *
     * @return Returns the status code.
     */
    @Override
    public Integer getStatus() {
        return 200;
    }

    /**
     * Gets the HTTP headers.
     *
     * @return Returns the HTTP headers.
     */
    @Override
    public Map<String, String> getHeaders() {
        return Collections.emptyMap();
    }

    /**
     * Handles the message.
     *
     * @param msg The message.
     */
    @Override
    public void executeOnReceive(MediatorHTTPRequest msg) {
        System.out.println("Received body : " + msg.getBody());

        JSONObject body = new JSONObject(msg.getBody());
        assertEquals(3, body.getInt("no_of_events"));
        assertEquals(2, body.getJSONArray("clients").length());
        assertEquals(3, body.getJSONArray("events").length());

        assertEquals("Family Registration",
            body.getJSONArray("events").getJSONObject(0).getString("eventType"
            ));
        assertEquals("Family Member Registration",
            body.getJSONArray("events").getJSONObject(1).getString("eventType"
            ));
        assertEquals("HIV Registration",
            body.getJSONArray("events").getJSONObject(2).getString("eventType"
            ));


    }
}
