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

import dev.sergheev.eventbus.collections.DoublyLinkedPriorityQueue;
import dev.sergheev.eventbus.Event;
import dev.sergheev.eventbus.EventListener;
import dev.sergheev.eventbus.event.method.NotifiableEventHandlerMethod;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * This object abstracts away the management of a {@link Queue} associated to a concrete {@link Event}
 * type. There is a different queue for each {@link Event} type, because we want to collect methods
 * with the same {@link Event} type, ordered by their execution priority, but regardless of their
 * concrete {@link EventListener} instances.
 *
 * <p>
 * All the {@link NotifiableEventHandlerMethod} instances stored in a concrete {@link Queue} need to
 * be kept ordered by their execution priority at all times because we might transmit an {@link Event}
 * at any time, and iterate through all the {@link NotifiableEventHandlerMethod} instances by their
 * order of execution (lower executes first), that's why we need an additional custom {@link Queue}
 * implementation, because the default {@link PriorityQueue} is based on a heap and does not keep
 * elements in order, so we'll be using our concrete {@link DoublyLinkedPriorityQueue} implementation.
 *
 * <p>
 * Basically what we're trying to archive is a chain of responsibility, because we want to iterate
 * through all the {@link NotifiableEventHandlerMethod} instances and then call the {@link Method}
 * in a concrete {@link EventListener} passing to it as argument the given {@link Event} instance
 * which the method will consume, that way the {@link Event} instance passes through several
 * different {@link EventListener} instances and each listener mutates the state of the original
 * event data.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern">Chain of Responsibility Pattern</a>
 */
public class EventTypeToMethodsQueues {

    /**
     * Associates an {@link Event} type to a queue of always-ordered {@link NotifiableEventHandlerMethod} instances.
     */
    private final Map<Class<? extends Event>, Queue<NotifiableEventHandlerMethod>> eventTypeToMethods;

    /**
     * Creates a new {@link EventTypeToMethodsQueues} instance.
     */
    public EventTypeToMethodsQueues() {
        this.eventTypeToMethods = new HashMap<>();
    }

    /**
     * Adds the given {@link NotifiableEventHandlerMethod} instance (value) to its {@link Event} class type (key) correspondent queue.
     * @param eventType the {@link Event} class type (key)
     * @param notifiableMethod the {@link NotifiableEventHandlerMethod} instance (value) that is to be registered in its correspondent queue
     * @return {@code true} if the method was successfully added to its queue, {@code false} if the method was already registered
     */
    public boolean offer(Class<? extends Event> eventType, NotifiableEventHandlerMethod notifiableMethod) {
        requireNonNull(eventType, "eventType must not be null");
        requireNonNull(notifiableMethod, "method must not be null");
        Queue<NotifiableEventHandlerMethod> methodsQueue = eventTypeToMethods.getOrDefault(eventType, new DoublyLinkedPriorityQueue<>());
        boolean success = !methodsQueue.contains(notifiableMethod) && methodsQueue.add(notifiableMethod);
        eventTypeToMethods.put(eventType, methodsQueue);
        return success;
    }

    /**
     * Removes the given {@link NotifiableEventHandlerMethod} instance (value) from the queue correspondent to the {@link Event} class type (key).
     * @param eventType the {@link Event} class type (key)
     * @param notifiableMethod the {@link NotifiableEventHandlerMethod} instance (value) that is to be unregistered from its correspondent queue
     * @return {@code true} if the provided method was found and successfully unregistered, {@code false} otherwise (the method was not found)
     */
    public boolean remove(Class<? extends Event> eventType, NotifiableEventHandlerMethod notifiableMethod) {
        requireNonNull(eventType, "eventType must not be null");
        requireNonNull(notifiableMethod, "method must not be null");
        if (!eventTypeToMethods.containsKey(eventType)) return false;
        Queue<NotifiableEventHandlerMethod> methodsQueue = eventTypeToMethods.get(eventType);
        boolean success = methodsQueue.remove(notifiableMethod);
        if (!methodsQueue.isEmpty()) {
            eventTypeToMethods.put(eventType, methodsQueue);
        } else {
            eventTypeToMethods.remove(eventType);
        }
        return success;
    }

    /**
     * Checks if the given {@link NotifiableEventHandlerMethod} instance (value) is associated to the given {@link Event} class type (key).
     * @param eventType the {@link Event} class type (key)
     * @param notifiableMethod the {@link NotifiableEventHandlerMethod} whose presence in its correspondent queue is to be tested
     * @return {@code true} if the provided method instance is present in its correspondent queue, {@code false} otherwise
     */
    public boolean contains(Class<? extends Event> eventType, NotifiableEventHandlerMethod notifiableMethod) {
        requireNonNull(eventType, "eventType must not be null");
        requireNonNull(notifiableMethod, "method must not be null");
        if (!eventTypeToMethods.containsKey(eventType)) return false;
        Queue<NotifiableEventHandlerMethod> methodsQueue = eventTypeToMethods.get(eventType);
        return methodsQueue.contains(notifiableMethod);
    }

    /**
     * Collects all the {@link NotifiableEventHandlerMethod} instances (values) that associated to the given {@link Event} class type (key).
     * @param eventType the {@link Event} class type (key)
     * @return a new queue with all the {@link NotifiableEventHandlerMethod} instances associated to the given {@link Event} class type
     */
    public Queue<NotifiableEventHandlerMethod> getFor(Class<? extends Event> eventType) {
        requireNonNull(eventType, "eventType must not be null");
        if (eventTypeToMethods.containsKey(eventType)) {
            return new DoublyLinkedPriorityQueue<>(eventTypeToMethods.get(eventType));
        } else {
            return new DoublyLinkedPriorityQueue<>();
        }
    }

    /**
     * Returns an unmodifiable map containing all relationships between an {@link Event} class type (key)
     * and its queue of {@link NotifiableEventHandlerMethod} instances (values) present in this manager.
     * @return an unmodifiable map containing all relationships present in this manager
     */
    public Map<Class<? extends Event>, Queue<NotifiableEventHandlerMethod>> getAll() {
        return Collections.unmodifiableMap(eventTypeToMethods);
    }

    /**
     * Returns the amount of {@link NotifiableEventHandlerMethod} instances (values) in the queue associated to the given {@link Event} class type (key) in this manager.
     * @param eventType the {@link Event} class type (key)
     * @return the amount of mapped values to the given {@link Event} class type (key)
     */
    public int sizeFor(Class<? extends Event> eventType) {
        requireNonNull(eventType, "eventType must not be null");
        if (!eventTypeToMethods.containsKey(eventType)) return 0;
        Queue<NotifiableEventHandlerMethod> methodsQueue = eventTypeToMethods.get(eventType);
        return methodsQueue.size();
    }

    /**
     * The sum amount of {@link NotifiableEventHandlerMethod} instances (values) associated to all the registered keys in this manager.
     * @return the sum amount of {@link NotifiableEventHandlerMethod} instances (values) associated to all the registered keys in this manager
     */
    public int size() {
        return eventTypeToMethods.values().stream().mapToInt(Queue::size).sum();
    }

    /**
     * Checks if this manager is empty (has no stored associations).
     * @return {@code true} if there are no associations stored in this manager, {@code false} otherwise
     */
    public boolean isEmpty() {
        return eventTypeToMethods.isEmpty();
    }

    /**
     * Clears this cache by removing all currently stored associations.
     */
    public void clear() {
        eventTypeToMethods.values().forEach(Queue::clear);
        eventTypeToMethods.clear();
    }

}
