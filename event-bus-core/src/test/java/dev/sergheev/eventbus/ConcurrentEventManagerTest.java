package dev.sergheev.eventbus;

import dev.sergheev.eventbus.event.EventManagerFactory;

public class ConcurrentEventManagerTest extends AbstractEventManagerTest {

    @Override
    public EventManager createEventManager() {
        return EventManagerFactory.newConcurrentEventManager();
    }

}
