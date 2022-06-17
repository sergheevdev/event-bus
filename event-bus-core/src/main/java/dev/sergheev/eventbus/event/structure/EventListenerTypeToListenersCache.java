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

import java.util.*;

import static java.util.Objects.requireNonNull;


/**
 * <p>
 * Represents a cache which associates a key to multiple values. A key is said to be an
 * {@link EventListener} class type, and the values are {@link EventListener} instances.
 */
public class EventListenerTypeToListenersCache {

    /**
     * Associates an {@link EventListener} type (key) to its {@link EventListener} instances (values) of the same type as the key.
     */
    private final Map<Class<? extends EventListener>, Set<EventListener>> typeToListeners;

    /**
     * Creates a new {@link EventListenerTypeToListenersCache} instance.
     */
    public EventListenerTypeToListenersCache() {
        this.typeToListeners = new HashMap<>();
    }

    /**
     * Associates the given {@link EventListener} instance to its class type in this cache.
     * @param eventListener the listener instance that is to be added
     * @return {@code true} if the given listener was successfully added, {@code false} otherwise
     */
    public boolean register(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        Set<EventListener> listeners = typeToListeners.getOrDefault(eventListener.getClass(), new HashSet<>());
        boolean success = listeners.add(eventListener);
        typeToListeners.put(eventListener.getClass(), listeners);
        return success;
    }

    /**
     * Unmaps the given {@link EventListener} instance from its class type from this cache.
     * @param eventListener the listener instance that is to be removed
     * @return {@code true} if the given listener was found and removed, {@code false} otherwise
     */
    public boolean unregister(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        Set<EventListener> listeners = typeToListeners.getOrDefault(eventListener.getClass(), new HashSet<>());
        boolean success = listeners.remove(eventListener);
        if (!listeners.isEmpty()) {
            typeToListeners.put(eventListener.getClass(), listeners);
        } else {
            typeToListeners.remove(eventListener.getClass());
        }
        return success;
    }

    /**
     * Unmaps all the {@link EventListener} instances of the given class type from this cache.
     * @param listenerType the listener class type whose instances are to be removed
     * @return {@code true} if listeners associated to the given class type were found and removed, {@code false} otherwise
     */
    public boolean unregisterAllFor(Class<? extends EventListener> listenerType) {
        requireNonNull(listenerType, "listenerType must not be null");
        if (!typeToListeners.containsKey(listenerType)) return false;
        Set<EventListener> listeners = typeToListeners.get(listenerType);
        if (listeners.isEmpty()) return false;
        listeners.clear();
        typeToListeners.remove(listenerType);
        return true;
    }

    /**
     * Checks if the given {@link EventListener} instance is contained in this cache.
     * @param eventListener the listener whose presence is to be tested
     * @return {@code true} if the given instance is present in this cache, {@code false} otherwise
     */
    public boolean contains(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        if (!typeToListeners.containsKey(eventListener.getClass())) return false;
        Set<EventListener> listeners = typeToListeners.get(eventListener.getClass());
        return listeners.contains(eventListener);
    }

    /**
     * Checks if any {@link EventListener} instance of the given class type is contained in this cache.
     * @param listenerType the listener class type whose {@link EventListener} instances presence is to be tested
     * @return {@code true} if this cache contains any listener of the given class type, {@code false} otherwise
     */
    public boolean containsAny(Class<? extends EventListener> listenerType) {
        requireNonNull(listenerType, "listenerType must not be null");
        if (!typeToListeners.containsKey(listenerType)) return false;
        Set<EventListener> listeners = typeToListeners.get(listenerType);
        return !listeners.isEmpty();
    }

    /**
     * Collects the {@link EventListener} instances of the given class type from this cache.
     * @param listenerType the listener class type whose matching {@link EventListener} instances are to be collected
     * @return a set of {@link EventListener} instances matching the given listener class type
     */
    public Set<EventListener> getFor(Class<? extends EventListener> listenerType) {
        requireNonNull(listenerType, "listenerType must not be null");
        if (typeToListeners.containsKey(listenerType)) {
            return new HashSet<>(typeToListeners.get(listenerType));
        } else {
            return new HashSet<>();
        }
    }

    /**
     * Returns an unmodifiable map containing all relationships between an {@link EventListener} class type (key)
     * related to their {@link EventListener} instances (values) present in this cache.
     * @return an unmodifiable map containing all relationships present in this cache
     */
    public Map<Class<? extends EventListener>, Set<EventListener>> getAll() {
        return Collections.unmodifiableMap(typeToListeners);
    }

    /**
     * Returns the amount of {@link EventListener} instances of the given class type present in this cache.
     * @param listenerType the listener class type whose matching {@link EventListener} instances are to be counted
     * @return the amount of {@link EventListener} instances of the given class type present in this cache
     */
    public int sizeFor(Class<? extends EventListener> listenerType) {
        requireNonNull(listenerType, "listenerType must not be null");
        if (!typeToListeners.containsKey(listenerType)) return 0;
        Set<EventListener> listeners = typeToListeners.get(listenerType);
        return listeners.size();
    }

    /**
     * Returns the total amount of {@link EventListener} instances present in this cache.
     * @return the total amount of {@link EventListener} instances present in this cache
     */
    public int size() {
        return typeToListeners.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * Checks if there are no {@link EventListener} instances of the given class type present in this cache.
     * @param listenerType the listener class type whose {@link EventListener} instances presence is to be tested
     * @return {@code true} if there are no instances of the given type present in this cache, {@code false} otherwise
     */
    public boolean isEmptyFor(Class<? extends EventListener> listenerType) {
        requireNonNull(listenerType, "listenerType must not be null");
        return !typeToListeners.containsKey(listenerType);
    }

    /**
     * Checks if this cache is empty and has no associations.
     * @return {@code true} if this cache is empty and has no associations, {@code false} otherwise
     */
    public boolean isEmpty() {
        return typeToListeners.isEmpty();
    }

    /**
     * Clears all the {@link EventListener} instances of the given class type from this cache.
     * @param listenerType the listener class type of which matching {@link EventListener} instances are to be removed
     */
    public void clearFor(Class<? extends EventListener> listenerType) {
        requireNonNull(listenerType, "listenerType must not be null");
        typeToListeners.remove(listenerType);
    }

    /**
     * Clears this cache by removing all currently stored associations.
     */
    public void clear() {
        typeToListeners.clear();
    }

}
