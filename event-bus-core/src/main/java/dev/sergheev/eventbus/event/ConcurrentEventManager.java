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

package dev.sergheev.eventbus.event;

import dev.sergheev.eventbus.Event;
import dev.sergheev.eventbus.EventListener;
import dev.sergheev.eventbus.EventManager;
import dev.sergheev.eventbus.event.structure.EventListenerTypeToListenersCache;
import dev.sergheev.eventbus.event.structure.EventListenerCache;
import dev.sergheev.eventbus.event.method.EventHandlerMethod;
import dev.sergheev.eventbus.event.method.EventHandlerMethodExtractor;
import dev.sergheev.eventbus.event.method.NotifiableEventHandlerMethod;
import dev.sergheev.eventbus.event.structure.EventListenerToMethodsCache;
import dev.sergheev.eventbus.event.structure.EventTypeToMethodsQueues;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * This implementation is <b>thread-safe</b> (or concurrent).
 *
 * <p>
 * This object is responsible for managing all the {@link EventListener} instances and the
 * {@link EventHandlerMethod} associated to them. It represents an unified entrypoint for
 * all event handlers management.
 *
 * <p>
 * This façade represents a front-facing interface, and acts as an entrypoint for clients to
 * manage event handlers. The main problem at first was the <b>performance</b> issues when
 * trying to retrieve different {@link EventListener} instances based on a concrete criteria,
 * this lead us to the <b>redundancy</b> concepts which helped us to improve performance and
 * fulfill our objectives by the usage or several coordinated data structures which will
 * hold the necessary information, so we use a façade to hide the complex underlying or
 * structural code, so we provide a simpler interface, and by doing so we hide most of the
 * conceptual weight.
 *
 * @see <a href="https://domaincentric.net/blog/understanding-gof-facade-design-pattern">Façade Design Pattern</a>
 */
public class ConcurrentEventManager implements EventManager {

    /**
     * A predicate that when tested returns always {@code true}.
     */
    private static final Predicate<Method> TRUE_PREDICATE = (method) -> true;

    /**
     * Stores a set of the currently registered {@link EventListener} instances.
     */
    private final EventListenerCache listenerCache;

    /**
     * Associates an {@link EventListener} concrete class type to a set of {@link EventListener} instances of that type.
     */
    private final EventListenerTypeToListenersCache listenerTypeToListenersCache;

    /**
     * Associates an {@link Event} type a list of ordered {@link NotifiableEventHandlerMethod} instances (which can consume the event type they're related to).
     */
    private final EventTypeToMethodsQueues eventTypeToMethodsQueues;

    /**
     * Associates an {@link EventListener} instance to a set of {@link NotifiableEventHandlerMethod} (that belong to that listener).
     */
    private final EventListenerToMethodsCache listenerToMethodsCache;

    /**
     * Extracts all the methods that can be successfully converted into {@link EventHandlerMethod} instances.
     */
    private final EventHandlerMethodExtractor methodsExtractor;

    /**
     * Ensures concurrency atomicity: when a thread modifies the state of our set of objects, another thread can't.
     */
    private final Lock lock;

    /**
     * Creates a new {@link ConcurrentEventManager} instance.
     */
    ConcurrentEventManager() {
        this.listenerCache = new EventListenerCache();
        this.listenerTypeToListenersCache = new EventListenerTypeToListenersCache();
        this.listenerToMethodsCache = new EventListenerToMethodsCache();
        this.eventTypeToMethodsQueues = new EventTypeToMethodsQueues();
        this.methodsExtractor = new EventHandlerMethodExtractor();
        this.lock = new ReentrantLock();
    }

    @Override
    public void transmitToListeners(Event event) {
        requireNonNull(event, "event must not be null");
        // Fetch all the handlers listening for the given event type and transmit them (by execution priority) the event instance
        lock.lock();
        try {
            if (eventTypeToMethodsQueues.isEmpty()) return;
            eventTypeToMethodsQueues.getFor(event.getClass()).forEach(method -> method.notifyWith(event));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int register(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        lock.lock();
        try {
            return register(eventListener, methodsExtractor.extractMethodsFrom(eventListener.getClass()));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int registerMeeting(EventListener eventListener, Predicate<Method> predicate) {
        requireNonNull(eventListener, "eventListener must not be null");
        requireNonNull(predicate, "predicate must not be null");
        lock.lock();
        try {
            return register(eventListener, methodsExtractor.extractMethodsFromMeeting(eventListener.getClass(), predicate));
        } finally {
            lock.unlock();
        }
    }

    private int register(EventListener eventListener, List<EventHandlerMethod> methodsToRegister) {
        listenerCache.register(eventListener);
        listenerTypeToListenersCache.register(eventListener);
        int newRegistrationsAmount = 0;
        for (EventHandlerMethod method : methodsToRegister) {
            NotifiableEventHandlerMethod notifiableMethod = new NotifiableEventHandlerMethod(eventListener, method);
            boolean isNew;
            isNew = eventTypeToMethodsQueues.offer(notifiableMethod.getEventType(), notifiableMethod);
            isNew &= listenerToMethodsCache.map(eventListener, notifiableMethod);
            if (isNew) newRegistrationsAmount++;
        }
        return newRegistrationsAmount;
    }

    @Override
    public int unregister(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        lock.lock();
        try {
            return unregister(eventListener, listenerToMethodsCache.getFor(eventListener), TRUE_PREDICATE);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int unregisterMeeting(EventListener eventListener, Predicate<Method> predicate) {
        requireNonNull(eventListener, "eventListener must not be null");
        requireNonNull(predicate, "predicate must not be null");
        lock.lock();
        try {
            return unregister(eventListener, listenerToMethodsCache.getFor(eventListener), predicate);
        } finally {
            lock.unlock();
        }
    }

    private int unregister(EventListener eventListener, Set<NotifiableEventHandlerMethod> methodsToUnregister, Predicate<Method> predicate) {
        if(!listenerCache.contains(eventListener)) return 0;
        int unregisteredMethods = 0;
        for (NotifiableEventHandlerMethod method : methodsToUnregister) {
            boolean meetsPredicate = predicate.test(method.getMethod());
            if (!meetsPredicate) continue;
            boolean wasRemoved;
            wasRemoved = eventTypeToMethodsQueues.remove(method.getEventType(), method);
            wasRemoved &= listenerToMethodsCache.unmap(eventListener, method);
            if (wasRemoved) {
                unregisteredMethods++;
            } else {
                throw new IllegalStateException("Inconsistency between different data structures detected");
            }
        }
        // If the cache still has handlers associated to that listener instance don't delete the remaining handlers
        if (listenerToMethodsCache.sizeFor(eventListener) == 0) {
            listenerCache.unregister(eventListener);
            listenerTypeToListenersCache.unregister(eventListener);
        }
        return unregisteredMethods;
    }

    @Override
    public int unregisterWithMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate) {
        requireNonNull(listenerType, "listenerType must not be null");
        requireNonNull(predicate, "predicate must not be null");
        lock.lock();
        try {
            int totalUnregisteredMethods = 0;
            for (EventListener eventListener : listenerTypeToListenersCache.getFor(listenerType)) {
                totalUnregisteredMethods += unregister(eventListener, listenerToMethodsCache.getFor(eventListener), predicate);
            }
            return totalUnregisteredMethods;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<EventListener> getMeeting(Predicate<Method> predicate) {
        requireNonNull(predicate, "predicate must not be null");
        lock.lock();
        try {
            return collect(listenerCache.getAll(), predicate);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<EventListener> getWith(Class<? extends EventListener> listenerType) {
        requireNonNull(listenerType, "listenerType must not be null");
        lock.lock();
        try {
            return collect(listenerTypeToListenersCache.getFor(listenerType), TRUE_PREDICATE);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<EventListener> getWithMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate) {
        requireNonNull(listenerType, "listenerType must not be null");
        requireNonNull(predicate, "predicate must not be null");
        lock.lock();
        try {
            return collect(listenerTypeToListenersCache.getFor(listenerType), predicate);
        } finally {
            lock.unlock();
        }
    }

    private Set<EventListener> collect(Set<EventListener> listeners, Predicate<Method> predicate) {
        Set<EventListener> collectedListeners = new HashSet<>();
        for (EventListener eventListener : listeners) {
            Set<NotifiableEventHandlerMethod> notifiableMethods = listenerToMethodsCache.getFor(eventListener);
            boolean allMeetPredicate = notifiableMethods.stream().allMatch(current -> predicate.test(current.getMethod()));
            if (allMeetPredicate) collectedListeners.add(eventListener);
        }
        return collectedListeners;
    }

    @Override
    public Set<EventListener> getAll() {
        lock.lock();
        try {
            return listenerCache.getAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsAnyWith(Class<? extends EventListener> listenerType) {
        requireNonNull(listenerType, "listenerType must not be null");
        lock.lock();
        try {
            return listenerTypeToListenersCache.containsAny(listenerType);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsAnyWithMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate) {
        requireNonNull(listenerType, "listenerType must not be null");
        requireNonNull(predicate, "predicate must not be null");
        lock.lock();
        try {
            return contains(listenerTypeToListenersCache.getFor(listenerType), predicate);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsAnyMeeting(Predicate<Method> predicate) {
        requireNonNull(predicate, "conditions must not be null");
        lock.lock();
        try {
            return contains(listenerCache.getAll(), predicate);
        } finally {
            lock.unlock();
        }
    }
    
    private boolean contains(Set<EventListener> listeners, Predicate<Method> predicate) {
        for (EventListener eventListener : listeners) {
            Set<NotifiableEventHandlerMethod> notifiableMethods = listenerToMethodsCache.getFor(eventListener);
            boolean allMeetPredicate = notifiableMethods.stream().anyMatch(current -> predicate.test(current.getMethod()));
            if (allMeetPredicate) return true;
        }
        return false;
    }

    @Override
    public boolean contains(EventListener eventListener) {
        requireNonNull(eventListener, "eventListener must not be null");
        lock.lock();
        try {
            return listenerCache.contains(eventListener);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clearAll() {
        lock.lock();
        try {
            getAll().forEach(this::unregister);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int handlersAmountWith(Class<? extends EventListener> listenerType) {
        requireNonNull(listenerType, "listenerType must not be null");
        lock.lock();
        try {
            return handlersAmount(listenerTypeToListenersCache.getFor(listenerType), TRUE_PREDICATE);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int handlersAmountMeeting(Predicate<Method> predicate) {
        requireNonNull(predicate, "predicate must not be null");
        lock.lock();
        try {
            return handlersAmount(listenerCache.getAll(), predicate);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int handlersAmountWithMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate) {
        requireNonNull(listenerType, "listenerType must not be null");
        requireNonNull(predicate, "predicate must not be null");
        lock.lock();
        try {
            return handlersAmount(listenerTypeToListenersCache.getFor(listenerType), predicate);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int handlersAmount() {
        lock.lock();
        try {
            return handlersAmount(listenerCache.getAll(), TRUE_PREDICATE);
        } finally {
            lock.unlock();
        }
    }

    private int handlersAmount(Set<EventListener> listeners, Predicate<Method> predicate) {
        int totalMethodsAmount = 0;
        for (EventListener eventListener : listeners) {
            Set<NotifiableEventHandlerMethod> notifiableMethods = listenerToMethodsCache.getFor(eventListener);
            int handlersForListener = (int) notifiableMethods.stream().filter(current -> predicate.test(current.getMethod())).count();
            totalMethodsAmount += handlersForListener;
        }
        return totalMethodsAmount;
    }

    @Override
    public int sizeWith(Class<? extends EventListener> listenerType) {
        requireNonNull(listenerType, "listenerType must not be null");
        lock.lock();
        try {
            return listenerTypeToListenersCache.sizeFor(listenerType);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int sizeMeeting(Predicate<Method> predicate) {
        requireNonNull(predicate, "predicate must not be null");
        lock.lock();
        try {
            return size(listenerCache.getAll(), predicate);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int sizeWithMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate) {
        lock.lock();
        try {
            return size(listenerTypeToListenersCache.getFor(listenerType), predicate);
        } finally {
            lock.unlock();
        }
    }

    private int size(Set<EventListener> listeners, Predicate<Method> predicate) {
        int listenersAmount = 0;
        for (EventListener eventListener : listeners) {
            Set<NotifiableEventHandlerMethod> notifiableMethods = listenerToMethodsCache.getFor(eventListener);
            boolean allMeetConditions = notifiableMethods.stream().allMatch(current -> predicate.test(current.getMethod()));
            if (allMeetConditions) listenersAmount++;
        }
        return listenersAmount;
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return listenerCache.size();
        } finally {
            lock.unlock();
        }
    }

}