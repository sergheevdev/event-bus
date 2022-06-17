package dev.sergheev.eventbus;

import dev.sergheev.eventbus.event.EventManagerFactory;
import org.junit.Test;

import java.lang.reflect.Constructor;

public class EventManagerFactoryTest {

    @Test(expected = Exception.class)
    @SuppressWarnings("unchecked")
    public void testInstantiatingWithConstructor() throws Exception {
        Constructor<EventManagerFactory> constructor = (Constructor<EventManagerFactory>) EventManagerFactory.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        constructor.newInstance();
    }

}

