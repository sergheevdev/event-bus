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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * <p>
 * This object is responsible for extracting all the methods in a class that can be
 * successfully converted into {@link EventHandlerMethod} instances.
 *
 * <p>
 * A {@link Method} is said that can be converted into an {@link EventHandlerMethod}
 * if it meets all the following conditions:
 *
 * <ul>
 *     <li>The method must be annotated with {@link EventHandler} decorator.
 *     <li>The method's declaring class implements {@link EventListener}.
 *     <li>The method's return type must be {@link Void} (but in its primitive form).
 *     <li>The method contains only one parameter.
 *     <li>The method's only parameter must be assignable to {@link Event}.
 * </ul>
 */
public class EventHandlerMethodExtractor {

    /**
     * An always {@code true} predicate.
     */
    private static final Predicate<Method> TRUE_PREDICATE = (method) -> true;

    /**
     * Extracts from the given class all the methods that can be converted into {@link EventHandlerMethod} instances.
     * @param listenerType the listener class from which a group of methods will be extracted
     * @return all the extracted methods already converted into {@link EventHandlerMethod} instances
     */
    public List<EventHandlerMethod> extractMethodsFrom(Class<? extends EventListener> listenerType) {
        return extractMethodsFromMeeting(listenerType, TRUE_PREDICATE);
    }

    /**
     * Extracts from the given class all the methods that can be converted into {@link EventHandlerMethod} instances
     * and satisfy the given predicate.
     * @param listenerType the listener class from which a group of methods will be extracted
     * @param predicate the predicate which the methods must satisfy in order to be extracted
     * @return all the extracted methods already converted into {@link EventHandlerMethod} instances
     */
    public List<EventHandlerMethod> extractMethodsFromMeeting(Class<? extends EventListener> listenerType, Predicate<Method> predicate) {
        validateExtractionParameters(listenerType, predicate);
        List<EventHandlerMethod> collectedMethods = new ArrayList<>();
        for (Method method : listenerType.getDeclaredMethods()) {
            boolean canBeConverted = canBeConvertedToEventHandlerMethod(method);
            if (!canBeConverted) continue;
            boolean meetsPredicate = predicate.test(method);
            if (!meetsPredicate) continue;
            collectedMethods.add(EventHandlerMethod.createFrom(method));
        }
        return collectedMethods;
    }

    /**
     * Ensures the validity of the given extraction parameters.
     * @param listenerType the listener class that is to be checked
     * @param predicate the predicate that is to be checked
     * @throws RuntimeException if a validation error occurs
     */
    private void validateExtractionParameters(Class<? extends EventListener> listenerType, Predicate<Method> predicate) {
        if (listenerType == null) {
            final String message = "listenerType must not be null";
            throw new NullPointerException(message);
        }
        if (listenerType == EventListener.class) {
            final String template = "listenerType must be an implementor, not the raw interface: %s";
            final String message = String.format(template, listenerType);
            throw new IllegalArgumentException(message);
        }
        if (predicate == null) {
            final String message = "predicate must not be null";
            throw new NullPointerException(message);
        }
    }

    /**
     * Checks if a method can be successfully converted into an {@link EventHandlerMethod} instance.
     * @param method the method that is to be checked
     * @return {@code true} if the method can be converted, {@code false} otherwise
     */
    private boolean canBeConvertedToEventHandlerMethod(Method method) {
        if (!method.isAnnotationPresent(EventHandler.class)) return false;
        if (!EventListener.class.isAssignableFrom(method.getDeclaringClass())) return false;
        if (method.getReturnType() != void.class) return false;
        if (method.getParameterCount() != 1) return false;
        if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) return false;
        return true;
    }

}