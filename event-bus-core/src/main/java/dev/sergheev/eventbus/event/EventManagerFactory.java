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

import dev.sergheev.eventbus.EventManager;

/**
 * <p>
 * A simple factory class with static creation methods for the construction of an {@link EventManager}.
 */
public class EventManagerFactory {

    private EventManagerFactory() {
        throw new AssertionError();
    }

    /**
     * Creates a <b>non thread-safe</b> {@link EventManager} instance.
     * @return creates a <b>non thread-safe</b> {@link EventManager} instance
     */
    public static SimpleEventManager newSimpleEventManager() {
        return new SimpleEventManager();
    }

    /**
     * Creates a <b>thread-safe (concurrent)</b> {@link EventManager} instance.
     * @return creates a <b>thread-safe (concurrent)</b> {@link EventManager} instance
     */
    public static ConcurrentEventManager newConcurrentEventManager() {
        return new ConcurrentEventManager();
    }

}
