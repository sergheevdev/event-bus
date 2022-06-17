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

package dev.sergheev.eventbus.event.method;

import dev.sergheev.eventbus.Event;
import dev.sergheev.eventbus.EventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * Represents a decorated {@link EventHandlerMethod}, it is composed of the method with
 * its correspondent {@link EventListener} instance, which is to whom the {@link Event}
 * instance is to be provided to.
 *
 * <p>
 * This object also implements the {@link Comparable} interface. The comparison is
 * delegated to the {@link EventHandlerMethod} that is being decorated (which only
 * compares the execution priority or order).
 */
public class NotifiableEventHandlerMethod implements Comparable<NotifiableEventHandlerMethod> {

    /**
     * The listener whose class contains this method (which is to whom an {@link Event} instance is to be provided).
     */
    private final EventListener eventListener;

    /**
     * A decorated method, whose raw (or native) method, belongs to this {@link #eventListener} class.
     */
    private final EventHandlerMethod eventHandlerMethod;

    private int cachedHashCode = 0;

    /**
     * Constructs a new {@link NotifiableEventHandlerMethod} instance.
     */
    public NotifiableEventHandlerMethod(EventListener eventListener, EventHandlerMethod eventHandlerMethod) {
        this.validate(eventListener, eventHandlerMethod);
        this.eventListener = eventListener;
        this.eventHandlerMethod = eventHandlerMethod;
    }

    /**
     * Ensures the domain validity of this object.
     * @param eventListener the listener that is to be checked
     * @param eventHandlerMethod the method that is to be checked
     * @throws RuntimeException if a validation error occurs
     */
    private void validate(EventListener eventListener, EventHandlerMethod eventHandlerMethod) {
        if (eventListener == null) {
            final String message = "eventListener must not be null";
            throw new NullPointerException(message);
        }
        if (eventHandlerMethod == null) {
            final String message = "eventHandlerMethod must not be null";
            throw new NullPointerException(message);
        }
        if (!classContainsMethod(eventListener.getClass(), eventHandlerMethod.getMethod())) {
            final String message = "eventListener::getClass()::getMethods() must contain eventHandlerMethod::getMethod()";
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks if a given class contains a concrete method.
     * @param givenClass the class where the method should be searched
     * @param searchedMethod the method that is to be found
     * @return {@code true} if the searched method was found in the class's declared methods, {@code false} otherwise
     */
    private boolean classContainsMethod(Class<?> givenClass, Method searchedMethod) {
        boolean foundMatch = false;
        int currentIndex = 0;
        Method[] allMethods = givenClass.getDeclaredMethods();
        while (currentIndex < allMethods.length && !foundMatch) {
            Method currentMethod = allMethods[currentIndex];
            foundMatch = currentMethod.equals(searchedMethod);
            currentIndex++;
        }
        return foundMatch;
    }

    /**
     * Notifies the {@link #eventListener} with the given {@link Event} instance by calling its raw method from {@link #eventHandlerMethod}.
     * @param event the event that is to be transmitted
     * @throws RuntimeException if an event execution, reflection, or security manager error occurs
     */
    public void notifyWith(Event event) {
        Method method = eventHandlerMethod.getMethod();
        try {
            method.setAccessible(true);
            method.invoke(eventListener, requireNonNull(event, "event must not be null"));
        } catch (IllegalAccessException | InvocationTargetException exception) {
            final String template = "An error occurred while invoking the listener's method to provide it with the event: %s";
            final String message = String.format(template, exception.getMessage());
            throw new RuntimeException(message);
        }
    }

    /**
     * The name (or unique identifier) of the wrapped {@link EventHandlerMethod} instance.
     * @return the name (or unique identifier) of the wrapped {@link EventHandlerMethod} instance
     */
    public String getId() {
        return eventHandlerMethod.getId();
    }

    /**
     * The execution priority (or order) of the wrapped {@link EventHandlerMethod} instance.
     * @return the execution priority (or order) of the wrapped {@link EventHandlerMethod} instance
     */
    public int getOrder() {
        return eventHandlerMethod.getOrder();
    }

    /**
     * The {@link Event} class type which the raw (or native) method is able to consume
     * @return the {@link Event} class type which the raw (or native) method is able to consume
     */
    public Class<? extends Event> getEventType() {
        return eventHandlerMethod.getEventType();
    }

    /**
     * Returns the original raw (or native) method.
     * @return the original raw (or native) method
     */
    public Method getMethod() {
        return eventHandlerMethod.getMethod();
    }

    /**
     * Returns the current listener to whom events of the {@link #getEventType()} type will be notified
     * @return the current listener to whom events of the {@link #getEventType()} type will be notified
     */
    public EventListener getEventListener() {
        return eventListener;
    }

    /**
     * Returns the current {@link EventHandlerMethod} instance that is being wrapped.
     * @return the current {@link EventHandlerMethod} instance that is being wrapped
     */
    public EventHandlerMethod getEventHandlerMethod() {
        return eventHandlerMethod;
    }

    @Override
    public int compareTo(NotifiableEventHandlerMethod other) {
        requireNonNull(other, "other must not be null");
        return eventHandlerMethod.compareTo(other.eventHandlerMethod);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NotifiableEventHandlerMethod that = (NotifiableEventHandlerMethod) object;
        return eventListener.equals(that.eventListener) && eventHandlerMethod.equals(that.eventHandlerMethod);
    }

    @Override
    public int hashCode() {
        int result = cachedHashCode;
        if (result == 0) {
            final int prime = 31;
            result = (eventListener == null ? 0 : eventListener.hashCode());
            result = prime * result + (eventHandlerMethod == null ? 0 : eventHandlerMethod.hashCode());
            cachedHashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", getClass().getSimpleName() + " = { " , " }");
        joiner.add(String.format("eventListener = '%s'", eventListener));
        joiner.add(String.format("eventHandlerMethod = '%s'", eventHandlerMethod));
        return joiner.toString();
    }

}