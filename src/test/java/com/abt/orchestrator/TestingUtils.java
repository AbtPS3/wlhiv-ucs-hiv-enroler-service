package com.abt.orchestrator;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import org.openhim.mediator.engine.testing.MockHTTPConnector;

import java.util.Collections;
import java.util.List;

public class TestingUtils {
    public TestingUtils() {
    }

    public static void launchMockHTTPConnector(ActorSystem system,
                                               String rootContext, Class<?
        extends MockHTTPConnector> mockConnector) {
        launchActors(system, rootContext,
            Collections.singletonList(new TestMockLauncher.ActorToLaunch(
                "http-connector", mockConnector, null)));
    }

    public static void launchActors(ActorSystem system, String rootContext,
                                    List<TestMockLauncher.ActorToLaunch> actorsToLaunch) {
        system.actorOf(Props.create(TestMockLauncher.class,
                actorsToLaunch), rootContext);
    }

    public static void clearRootContext(ActorSystem system,
                                        String rootContext) {
        system.actorSelection("/user/" + rootContext).tell(PoisonPill.getInstance(), ActorRef.noSender());
    }
}
