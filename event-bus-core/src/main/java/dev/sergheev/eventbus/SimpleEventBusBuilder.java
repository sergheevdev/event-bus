/*
 * Copyright 2022 Serghei Sergheev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sergheev.eventbus;

import dev.sergheev.eventbus.event.EventManagerFactory;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * A simple fluent API builder for the construction of a {@link SimpleEventBus}.
 *
 * <p>
 * This object is responsible for providing the clients a simple API for the
 * construction of a {@link SimpleEventBus}, by using this builder we prevent
 * manual assembly confusion, because we provide a simple way to construct a
 * complex object.
 *
 * <p>
 * Apart from allowing us to easily construct the bus (in a simple way), it also
 * allows us to specify custom properties that will define the bus behavior at
 * runtime (i.e. thread-safety).
 */
public class SimpleEventBusBuilder {

    /**
     * A static factory method for the creation of a new {@code SimpleEventBusBuilder} instance.
     * @return a new {@link SimpleEventBusBuilder} instance
     */
    public static SimpleEventBusBuilder create() {
        return new SimpleEventBusBuilder();
    }

    /**
     * Indicates if the bus being built must be thread-safe (concurrent) or not.
     */
    private boolean isConcurrent;

    /**
     * Stores a custom or client-managed {@link EventManager} instance that is to be provided by the client.
     */
    private EventManager customManager;

    /**
     * Contains all the listener instances that are to be registered in the event manager.
     */
    private final Set<EventListener> eventListeners;

    /**
     * Creates a new {@link SimpleEventBus} instance.
     */
    public SimpleEventBusBuilder() {
        this.isConcurrent = false;
        this.customManager = null;
        this.eventListeners = new HashSet<>();
    }

    /**
     * Enables the thread-safety of the bus being built and its functioning in multi-threaded environments.
     */
    public SimpleEventBusBuilder concurrent() {
        isConcurrent = true;
        return this;
    }

    /**
     * Registers multiple listeners in the current {@link EventManager} instance of the bus.
     * @param eventListeners the listeners that are to be registered
     */
    public SimpleEventBusBuilder registerListeners(Iterable<EventListener> eventListeners) {
        requireNonNull(eventListeners, "eventListeners must not be null");
        eventListeners.forEach(this::registerListener);
        return this;
    }

    /**
     * Registers a listener in the current {@link EventManager} instance of the bus.
     * @param eventListener the listener that is to be registered
     */
    public SimpleEventBusBuilder registerListener(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        eventListeners.add(eventListener);
        return this;
    }

    /**
     * Use a client-managed or custom provided {@link EventManager} instance for the bus being
     * built. It is useful to provide a client self-manager manager when event handlers will
     * be modified at runtime, to prevent a {@link ConcurrentModificationException}.
     * @param customManager the custom {@link EventManager} instance that is to be used
     */
    public SimpleEventBusBuilder withManager(EventManager customManager) {
        requireNonNull(customManager, "customManager must not be null");
        this.customManager = customManager;
        return this;
    }

    /**
     * Constructs a new {@link SimpleEventBus} instance configured accordingly. If a custom or
     * client-managed {@link EventManager} is not specified, then a default <b>non thread-safe</b>
     * implementation of the manager will be used instead.
     * @return a new {@link SimpleEventBus} instance configured accordingly.
     */
    public SimpleEventBus build() {
        final EventManager eventManager;
        final boolean hasCustomManager = !Objects.isNull(customManager);
        if (hasCustomManager) {
            eventManager = customManager;
        } else if (isConcurrent) {
            eventManager = EventManagerFactory.newConcurrentEventManager();
        } else {
            eventManager = EventManagerFactory.newSimpleEventManager();
        }
        eventListeners.forEach(eventManager::register);
        return new SimpleEventBus(eventManager);
    }

}
