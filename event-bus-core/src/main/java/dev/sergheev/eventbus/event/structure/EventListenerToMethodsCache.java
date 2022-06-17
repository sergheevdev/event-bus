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
import dev.sergheev.eventbus.event.method.NotifiableEventHandlerMethod;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * A cache which associates an {@link EventListener} instance (which is the key) to a
 * {@link Set} of {@link NotifiableEventHandlerMethod} instances (which are the values).
 */
public class EventListenerToMethodsCache {

    /**
     * Associates a {@link EventListener} to a set of {@link NotifiableEventHandlerMethod} instances.
     */
    private final Map<EventListener, Set<NotifiableEventHandlerMethod>> listenerToMethods;

    /**
     * Creates a new {@link NotifiableEventHandlerMethod} instance.
     */
    public EventListenerToMethodsCache() {
        this.listenerToMethods = new HashMap<>();
    }

    /**
     * Adds the {@link NotifiableEventHandlerMethod} instance, to the {@link Set} (value) associated to
     * the {@link EventListener} instance (key) in this cache.
     * @param eventListener the provided listener (which represents the key) to whom the given method is to be mapped
     * @param notifiableMethod the method (which is considered the value)
     * @return {@code true} if the given method instance was successfully stored, or {@code false} otherwise
     */
    public boolean map(EventListener eventListener, NotifiableEventHandlerMethod notifiableMethod) {
        requireNonNull(eventListener, "eventListener must not be null");
        requireNonNull(notifiableMethod, "notifiableMethod must not be null");
        Set<NotifiableEventHandlerMethod> cachedMethods = listenerToMethods.getOrDefault(eventListener, new HashSet<>());
        boolean success = cachedMethods.add(notifiableMethod);
        listenerToMethods.put(eventListener, cachedMethods);
        return success;
    }

    /**
     * Removes {@link NotifiableEventHandlerMethod} instance, from the {@link Set} (value) associated to
     * the {@link EventListener} instance (key) in this cache.
     * @param eventListener the provided listener (which represents the key) from whom the given method is to be unmapped
     * @param notifiableMethod the method (considered the value)
     * @return {@code true} if the given method instance was found and successfully removed, {@code false} otherwise
     */
    public boolean unmap(EventListener eventListener, NotifiableEventHandlerMethod notifiableMethod) {
        requireNonNull(eventListener, "eventListener must not be null");
        requireNonNull(notifiableMethod, "notifiableMethod must not be null");
        if (!listenerToMethods.containsKey(eventListener)) return false;
        Set<NotifiableEventHandlerMethod> cachedMethods = listenerToMethods.get(eventListener);
        boolean success = cachedMethods.remove(notifiableMethod);
        if (!cachedMethods.isEmpty()) {
            listenerToMethods.put(eventListener, cachedMethods);
        } else {
            listenerToMethods.remove(eventListener);
        }
        return success;
    }

    /**
     * Checks if the {@link NotifiableEventHandlerMethod} instance, is present in the {@link Set} (value)
     * associated to the {@link EventListener} instance (key) in this cache.
     * @param eventListener the provided listener (which represents the key) whose methods are checked
     * @param notifiableMethod the method (considered the value) whose presence in the associated set is to be tested
     * @return {@code true} if the method is present in the set associated to the listener, {@code false} otherwise
     */
    public boolean contains(EventListener eventListener, NotifiableEventHandlerMethod notifiableMethod) {
        requireNonNull(eventListener, "eventListener must not be null");
        requireNonNull(notifiableMethod, "notifiableMethod must not be null");
        if (!listenerToMethods.containsKey(eventListener)) return false;
        Set<NotifiableEventHandlerMethod> cachedMethods = listenerToMethods.get(eventListener);
        return cachedMethods.contains(notifiableMethod);
    }

    /**
     * A copy of all the {@link NotifiableEventHandlerMethod} instances from the {@link Set} (value) associated
     * to the {@link EventListener} instance (key) in this cache.
     * @param eventListener the listener (the key) whose mapped methods (values) are to be returned
     * @return a copy of all the method instances from the set (value) associated to the listener instance (key)
     */
    public Set<NotifiableEventHandlerMethod> getFor(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        if (listenerToMethods.containsKey(eventListener)) {
            return new HashSet<>(listenerToMethods.get(eventListener));
        } else {
            return new HashSet<>();
        }
    }

    /**
     * Returns an unmodifiable view map with all the {@link EventListener} to {@link NotifiableEventHandlerMethod} associations.
     * @return an unmodifiable view map with all the listener to methods associations
     */
    public Map<EventListener, Set<NotifiableEventHandlerMethod>> getAll() {
        return Collections.unmodifiableMap(listenerToMethods);
    }

    /**
     * Returns the amount of {@link NotifiableEventHandlerMethod} instances in the {@link Set} (value)
     * associated to the given {@link EventListener} instance (key) in this cache.
     * @param eventListener the listener whose amount of mapped methods is to be returned
     * @return the amount of mapped values to the given listener instance (key)
     */
    public int sizeFor(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        if (!listenerToMethods.containsKey(eventListener)) return 0;
        Set<NotifiableEventHandlerMethod> cachedMethods = listenerToMethods.get(eventListener);
        return cachedMethods.size();
    }

    /**
     * Returns the sum amount of {@link NotifiableEventHandlerMethod} instances associated to any
     * {@link EventListener} (key) in this cache.
     * @return the sum amount of method instances associated to any listener (key) in this cache
     */
    public int size() {
        return listenerToMethods.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * Checks if this cache is empty (has no stored associations).
     * @return {@code true} if there are no associations present in this cache, {@code false} otherwise
     */
    public boolean isEmpty() {
        return listenerToMethods.isEmpty();
    }

    /**
     * Clears this cache by removing all currently stored associations.
     */
    public void clear() {
        listenerToMethods.values().forEach(Set::clear);
        listenerToMethods.clear();
    }

}
