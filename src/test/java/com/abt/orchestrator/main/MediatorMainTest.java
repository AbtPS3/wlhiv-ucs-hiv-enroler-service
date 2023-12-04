package com.abt.orchestrator.main;

import com.abt.MediatorMain;
import org.junit.jupiter.api.Test;
import org.openhim.mediator.engine.MediatorConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediatorMainTest {

    /**
     * Test the mediator main class loading the configuration.
     *
     * @throws Exception
     */
    @Test
    void main() throws NoSuchMethodException, InvocationTargetException,
        IllegalAccessException {
        Method loadConfigMethod = MediatorMain.class.getDeclaredMethod(
            "loadConfig", String.class);

        loadConfigMethod.setAccessible(true);
        MediatorConfig mediatorConfig =
            (MediatorConfig) loadConfigMethod.invoke(null, "src/test" +
                "/resources/mediator.properties");

        assertEquals("localhost", mediatorConfig.getServerHost());
        assertEquals(Integer.valueOf(3106), mediatorConfig.getServerPort());
        assertEquals(Integer.valueOf(600000), mediatorConfig.getRootTimeout());
        assertTrue(mediatorConfig.getHeartsbeatEnabled());
    }
}
