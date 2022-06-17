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

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An object that represents a centralized entrypoint for listeners management and event transmission.
 */
public interface EventManager {

    /**
     * Transmits the given {@link Event} instance to the listeners (which may consume it, if they can).
     * @param event the event that is to be transmitted
     */
    void transmitToListeners(Event event);

    /**
     * Registers all the handler methods contained inside the given {@link EventListener} instance.
     * @param eventListener the listener all of whose handler methods are to be registered
     * @return the amount of registered handler methods
     */
    int register(EventListener eventListener);

    /**
     * Registers the handler methods contained inside the given {@link EventListener} instance, which meet the given predicate.
     * @param eventListener the listener some of whose handler methods (the ones that meet the predicate) are to be registered
     * @param predicate the predicate which handler methods of a listener must meet in order to be registered
     * @return the amount of registered handler methods
     */
    int registerMeeting(EventListener eventListener, Predicate<Method> predicate);

    /**
     * Unregisters all the handler methods contained in a given {@link EventListener} instance.
     * @param eventListener the listener all of whose handler methods are to be unregistered
     * @return the amount of unregistered handler methods
     */
    int unregister(EventListener eventListener);

    /**
     * Unregisters all the handler methods contained in a given {@link EventListener} instance, which meet the given predicate.
     * @param eventListener the listener some of whose handler methods (the ones that meet the predicate) are to be unregistered
     * @param predicate the predicate which a handler method of the given listener instance must meet in order to be unregistered
     * @return the amount of unregistered handler methods
     */
    int unregisterMeeting(EventListener eventListener, Predicate<Method> predicate);

    /**
     * Unregisters all the handler methods contained in a given {@link EventListener} type, which meet the given predicate.
     * @param listenerType the listener type some of whose handler methods (the ones that meet the predicate) are to be unregistered
     * @param predicate the predicate which a handler method of the given listener type must meet in order to be unregistered
     * @return the amount of unregistered handler methods
     */
    int unregisterWithMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate);

    /**
     * Returns all the registered listeners in this event manager.
     * @return all the registered listeners in this event manager
     */
    Set<EventListener> getAll();

    /**
     * Returns all the registered {@link EventListener} instances all of whose handler methods meet the given predicate.
     * @param predicate the predicate that all handler methods of a listener must meet in order the listener to be returned
     * @return a set of listeners which all of their handler methods meet the given predicate
     */
    Set<EventListener> getMeeting(Predicate<Method> predicate);

    /**
     * Returns all the registered listeners whose class type is the given.
     * @param listenerType the concrete class type which listeners must be of in order to be returned
     * @return all the registered listeners whose class type is the given
     */
    Set<EventListener> getWith(Class<? extends EventListener> listenerType);

    /**
     * Returns all the registered listeners whose class type matches the given and all of whose handler methods meet the given predicate.
     * @param listenerType the concrete class type which listeners must be of in order to be returned
     * @param predicate the predicate that all handler methods must meet in order the listener to be returned
     * @return a set of listeners whose class type is the given and all of whose handler methods meet the given predicate
     */
    Set<EventListener> getWithMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate);

    /**
     * Checks the presence of the given {@link EventListener} instance in this manager.
     * @param eventListener the listener whose presence is to be tested
     * @return {@code true} if the given listener instance is present in this manager, {@code false} otherwise
     */
    boolean contains(EventListener eventListener);

    /**
     * Checks if there is any listener all of whose handler methods meet the given predicate.
     * @param predicate the predicate that all handler methods must meet in order the listener to be returned
     * @return {@code true} if at least one listener all of whose handler methods meet the predicate was found, {@code false} otherwise
     */
    boolean containsAnyMeeting(Predicate<Method> predicate);

    /**
     * Checks if there is any listener whose class type is the given.
     * @param listenerType the listener type whose the instance presence is to be tested
     * @return {@code true} if any listener instance of the given type was found, {@code false} otherwise
     */
    boolean containsAnyWith(Class<? extends EventListener> listenerType);

    /**
     * Checks if there is any listener whose class type is the given and all of whose handler methods meet the given predicate.
     * @param listenerType the listener type whose the instance presence is to be tested
     * @param predicate the predicate that all handler methods must meet in order the listener to be returned
     * @return {@code true} if any listener instance under those conditions was found, {@code false} otherwise
     */
    boolean containsAnyWithMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate);

    /**
     * Clears all this event manager (by unregistering all listeners' handler methods).
     */
    void clearAll();

    /**
     * Returns the amount of handler methods whose listener is of the given type.
     * @param listenerType the listener type whose registered handler methods are to be counted
     * @return the amount of handler methods whose listener is of the given type
     */
    int handlersAmountWith(Class<? extends EventListener> listenerType);

    /**
     * Returns the amount of handler methods that meet the given predicate.
     * @param predicate the predicate that a handler method must meet in order to be counted
     * @return the amount of handler methods that meet the given predicate
     */
    int handlersAmountMeeting(Predicate<Method> predicate);

    /**
     * Returns the amount of handler methods whose listener is of the given type and meet the given predicate.
     * @param listenerType the listener type whose registered handler methods are to be counted
     * @param predicate the predicate that a handler method must meet in order to be counted
     * @return the amount of handler methods whose listener is of the given type and meet the given predicate
     */
    int handlersAmountWithMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate);

    /**
     * Returns the total amount of handler methods present in this manager.
     * @return the total amount of handler methods present in this manager
     */
    int handlersAmount();

    /**
     * Returns the amount of registered listeners of the given type.
     * @param listenerType the listener type which a listener instance must match in order to be counted
     * @return the amount of registered listeners of the given type
     */
    int sizeWith(Class<? extends EventListener> listenerType);

    /**
     * Returns the amount of registered listeners all of whose handler methods meet the given predicate.
     * @param predicate the predicate that all handler methods must meet in order for the listener to be counted
     * @return the amount of registered listeners all of whose handler methods meet the given predicate
     */
    int sizeMeeting(Predicate<Method> predicate);

    /**
     * Returns the amount of registered listeners of the given type and all of whose handler methods meet the given predicate.
     * @param listenerType the listener type which a listener instance must match in order to be counted
     * @param predicate the predicate that all handler methods must meet in order for the listener to be counted
     * @return the amount of registered listeners of the given type and all of whose handler methods meet the given predicate
     */
    int sizeWithMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate);

    /**
     * Returns the total amount of registered listeners.
     * @return the total amount of registered listeners
     */
    int size();

}
