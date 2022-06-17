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
import dev.sergheev.eventbus.EventHandler;
import dev.sergheev.eventbus.EventListener;

import java.lang.reflect.Method;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * An {@link EventHandlerMethod} represents a decorated method with extra meta-data
 * attributes that are: a method identifier, an order of execution (because we may
 * want to execute handlers in a certain order), and finally an {@link Event} type
 * (represents the parameter type which this wrapped method is able to consume).
 *
 * <p>
 * This composed object's purpose is to decorate some {@link EventListener} class
 * method, making it comparable, to be able to distinguish the execution priority
 * between several instances, and to know which concrete {@link Event} implementor
 * class type this wrapped method (the method which is currently being decorated)
 * is able to consume as a parameter.
 *
 * <p>
 * As previously introduced, this object also implements the {@link Comparable}
 * interface. The single attribute that takes part in the comparison is the
 * {@link #order} of execution (the performed comparison uses the default integer
 * comparator, lower has higher priority, that means it should be executed first).
 *
 * @see <a href="https://www.informit.com/articles/article.aspx?p=1398607&seqNum=3">Move Embellishment to Decorator</a>
 */
public class EventHandlerMethod implements Comparable<EventHandlerMethod> {

    /**
     * A static factory method for the creation of a new {@link EventHandlerMethod} instance.
     * @param method the method that is to be decorated
     * @return a new {@link EventHandlerMethod} instance
     */
    public static EventHandlerMethod createFrom(Method method) {
        return new EventHandlerMethod(method);
    }

    /**
     * The method's name (or unique identifier).
     */
    private final String id;

    /**
     * The execution order priority (lower executes first).
     */
    private final int order;

    /**
     * The event class type which the raw method is able to consume as parameter.
     */
    private final Class<? extends Event> eventType;

    /**
     * The raw method that is being decorated (when saying raw, we mean the native Java's method class).
     */
    private final Method method;

    private int cachedHashCode = 0;

    /**
     * Constructs a new {@link EventHandlerMethod} instance.
     * @param method the method that is to be decorated
     */
    public EventHandlerMethod(Method method) {
        this.validate(method);
        this.id = extractIdFrom(method);
        this.order = extractOrderFrom(method);
        this.eventType = extractEventTypeFrom(method);
        this.method = method;
    }

    /**
     * Validates that the given method can be successfully converted into an {@link EventHandlerMethod}.
     * @param method the method that is to be validated
     * @throws RuntimeException if a validation error occurs
     */
    private void validate(Method method) {
        if (method == null) {
            final String message = "method must not be null";
            throw new NullPointerException(message);
        }
        if (!method.isAnnotationPresent(EventHandler.class)) {
            final String template = "%s::%s must be annotated with %s";
            final String message = String.format(template, method.getDeclaringClass(), method.getName(), EventHandler.class);
            throw new IllegalArgumentException(message);
        }
        if (!EventListener.class.isAssignableFrom(method.getDeclaringClass())) {
            final String template = "%s::%s class must implement %s";
            final String message = String.format(template, method.getDeclaringClass(), method.getName(), EventListener.class);
            throw new IllegalArgumentException(message);
        }
        if (method.getReturnType() != void.class) {
            final String template = "%s::%s(%s) return type must be void";
            final String message = String.format(template, method.getDeclaringClass(), method.getName(), method.getParameterTypes()[0]);
            throw new IllegalArgumentException(message);
        }
        if (method.getParameterCount() != 1) {
            final String template = "%s::%s must have only one parameter";
            final String message = String.format(template, method.getDeclaringClass(), method.getName());
            throw new IllegalArgumentException(message);
        }
        if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
            final String template = "%s::%s(%s) parameter must implement %s";
            final String message = String.format(template, method.getDeclaringClass(), method.getName(), method.getParameterTypes()[0], Event.class);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Extracts the name (or unique identifier) from the provided raw method's {@link EventHandler} annotation.
     * @param method the method from which the name (or unique identifier) is to be extracted
     * @return the name (or unique identifier) for this handler method
     */
    private String extractIdFrom(Method method) {
        return method.getAnnotation(EventHandler.class).id();
    }

    /**
     * Extracts the execution priority or order from the provided raw method's {@link EventHandler} annotation.
     * @param method the method from which the execution priority or order is to be extracted
     * @return the execution or priority order for this handler method
     */
    private int extractOrderFrom(Method method) {
        return method.getAnnotation(EventHandler.class).order();
    }

    /**
     * Extracts the event class type that the provided method is able to consume passed as argument
     * @param method the method from which the event class type is to be extracted
     * @return the event class type that the provided method is able to consume passed as argument
     * @implNote a type-safe casting is performed (so we don't need to worry about the warning)
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Event> extractEventTypeFrom(Method method) {
        return (Class<? extends Event>) method.getParameterTypes()[0];
    }

    /**
     * Returns the current raw (or native) method that is being decorated.
     * @return the current raw (or native) method that is being decorated
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns this method's name (or unique identifier).
     * @return this method's name (or unique identifier)
     */
    public String getId() {
        return id;
    }

    /**
     * Returns this method's execution priority (or order).
     * @return this method's execution priority (or order)
     */
    public int getOrder() {
        return order;
    }

    /**
     * Returns the {@link Event} class type that can be consumed as a parameter by this method.
     * @return the {@link Event} class type that can be consumed as a parameter by this method
     */
    public Class<? extends Event> getEventType() {
        return eventType;
    }

    @Override
    public int compareTo(EventHandlerMethod other) {
        requireNonNull(other, "other must not be null");
        return Integer.compare(this.order, other.order);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        EventHandlerMethod that = (EventHandlerMethod) object;
        return method.equals(that.method) && id.equals(that.id) && order == that.order && eventType == that.eventType;
    }

    @Override
    public int hashCode() {
        int result = cachedHashCode;
        if (result == 0) {
            final int prime = 31;
            result = (id == null ? 0 : id.hashCode());
            result = prime * result + Integer.hashCode(order);
            result = prime * result + (eventType == null ? 0 : eventType.hashCode());
            result = prime * result + (method == null ? 0 : method.hashCode());
            cachedHashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", getClass().getSimpleName() + " = { " , " }");
        joiner.add(String.format("id = '%s'", id));
        joiner.add(String.format("order = '%s'", order));
        joiner.add(String.format("eventType = '%s'", eventType.getName()));
        joiner.add(String.format("method = '%s::%s'", method.getDeclaringClass().getName(), method.getName()));
        return joiner.toString();
    }

}