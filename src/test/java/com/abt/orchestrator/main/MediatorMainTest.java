package com.abt.orchestrator.main;

import org.junit.jupiter.api.Test;
import org.openhim.mediator.engine.MediatorConfig;
import com.abt.MediatorMain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MediatorMainTest {

    /**
     * Test the mediator main class loading the configuration.
     *
     * @throws Exception
     */
    @Test
    void main() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method loadConfigMethod = MediatorMain.class.getDeclaredMethod("loadConfig", String.class);

        loadConfigMethod.setAccessible(true);
        MediatorConfig mediatorConfig = (MediatorConfig) loadConfigMethod.invoke(null, "src/test/resources/mediator.properties");

        assertEquals("localhost", mediatorConfig.getServerHost());
        assertEquals(new Integer(3106), mediatorConfig.getServerPort());
        assertEquals(new Integer(600000), mediatorConfig.getRootTimeout());
        assertTrue(mediatorConfig.getHeartsbeatEnabled());
    }
}