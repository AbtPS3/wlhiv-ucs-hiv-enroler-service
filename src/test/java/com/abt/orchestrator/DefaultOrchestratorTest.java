package com.abt.orchestrator;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.RegistrationConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.testing.TestingUtils;
import com.abt.orchestrator.mock.MockDestination;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultOrchestratorTest {
    /**
     * Represents the system actor.
     */
    protected static ActorSystem system;


    /**
     * Represents the configuration.
     */
    protected MediatorConfig testConfig;

    @BeforeEach
    void setUp() throws IOException {
        system = ActorSystem.create();

        testConfig = new MediatorConfig();
        testConfig.setName("UCS-DATA-IMPORT");
        testConfig.setProperties("mediator-unit-test.properties");

        InputStream regInfo = DefaultOrchestratorTest.class.getClassLoader().getResourceAsStream("mediator-registration-info.json");
        RegistrationConfig regConfig = null;
        if (regInfo != null) {
            regConfig = new RegistrationConfig(regInfo);
        }

        testConfig.setRegistrationConfig(regConfig);

    }

    @AfterEach
    void tearDown() {
        TestingUtils.clearRootContext(system, testConfig.getName());
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests the mediator.
     *
     * @throws Exception if an exception occurs
     */
    @Test
    public void testSendingCtcClientsImportHTTPRequest() throws Exception {
        assertNotNull(testConfig);
        new JavaTestKit(system) {{
            List<TestMockLauncher.ActorToLaunch> toLaunch = new LinkedList<>();

            toLaunch.add(new TestMockLauncher.ActorToLaunch("http-connector", MockDestination.class, null));
            com.abt.orchestrator.TestingUtils.launchActors(system, testConfig.getName(), toLaunch);

            InputStream stream = DefaultOrchestratorTest.class.getClassLoader().getResourceAsStream("request.json");

            assertNotNull(stream);


            createActorAndSendRequest(system, testConfig, getRef(), IOUtils.toString(stream), DefaultOrchestrator.class, "/results");

            final Object[] out =
                    new ReceiveWhile<Object>(Object.class, duration("3 second")) {
                        @Override
                        protected Object match(Object msg) throws Exception {
                            return msg;
                        }
                    }.get();

            boolean foundResponse = false;

            for (Object o : out) {
                if (o instanceof FinishRequest) {
                    foundResponse = true;
                    break;
                }
            }
            assertNotNull(foundResponse);
        }};
    }

    /**
     * Method for initiating actors, creating requests and sending request to the actor.
     *
     * @param system     the actor system used to initialize the destination actor
     * @param testConfig the configuration used
     * @param sender     the sending actor
     * @param payload    the payload
     * @param type       class type of the destination orchestrator
     * @param path       to send the request
     */
    public void createActorAndSendRequest(ActorSystem system, MediatorConfig testConfig, ActorRef sender, String payload, Class<?> type, String path) {
        final ActorRef orchestratorActor = system.actorOf(Props.create(type, testConfig));
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/json");
        MediatorHTTPRequest POST_Request = new MediatorHTTPRequest(
                sender,
                sender,
                "unit-test",
                "POST",
                "http",
                null,
                null,
                path,
                payload,
                headers,
                Collections.<Pair<String, String>>emptyList()
        );

        orchestratorActor.tell(POST_Request, sender);
    }

}