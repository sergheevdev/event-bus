package dev.sergheev.eventbus;

import dev.sergheev.eventbus.event.EventManagerFactory;

public class SimpleEventManagerTest extends AbstractEventManagerTest {

    @Override
    public EventManager createEventManager() {
        return EventManagerFactory.newSimpleEventManager();
    }

}
