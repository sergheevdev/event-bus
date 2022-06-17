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

package dev.sergheev.eventbus.event.structure;

import dev.sergheev.eventbus.EventListener;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * This object represents a simple cache of {@link EventListener} instances.
 */
public class EventListenerCache {

    /**
     * The set of stored {@link EventListener} instances.
     */
    private final Set<EventListener> listeners;

    /**
     * Creates a new {@link EventListenerCache} instance.
     */
    public EventListenerCache() {
        this.listeners = new HashSet<>();
    }

    /**
     * Registers a new {@link EventListener} instance in this cache.
     * @param eventListener the {@link EventListener} instance that is to be registered
     * @return {@code true} if the registration was successful, {@code false} otherwise
     */
    public boolean register(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        return listeners.add(eventListener);
    }

    /**
     * Unregisters a {@link EventListener} instance from this cache.
     * @param eventListener the {@link EventListener} instance that is to be unregistered
     * @return {@code true} was successfully unregistered from this cache, {@code false} otherwise
     */
    public boolean unregister(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        return listeners.remove(eventListener);
    }

    /**
     * Checks if the given {@link EventListener} instance is present in this cache.
     * @param eventListener the {@link EventListener} instance whose presence is to be tested
     * @return {@code true} if the given instance is present in this cache, {@code false} otherwise
     */
    public boolean contains(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        return listeners.contains(eventListener);
    }

    /**
     * Returns a copy of all the {@link EventListener} instances present in this cache.
     * @return a copy of all the {@link EventListener} instances present in this cache
     */
    public Set<EventListener> getAll() {
        return new HashSet<>(listeners);
    }

    /**
     * Checks if this cache is empty.
     * @return {@code true} if this cache is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    /**
     * Clears this cache (removes all the stored instances).
     */
    public void clear() {
        listeners.clear();
    }

    /**
     * Returns the amount of {@link EventListener} instances present in this cache.
     * @return the amount of {@link EventListener} instances present in this cache
     */
    public int size() {
        return listeners.size();
    }

}
