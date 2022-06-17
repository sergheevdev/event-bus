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

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * A simple {@link EventBus} implementation (wraps around an {@link EventManager} instance).
 */
public class SimpleEventBus implements EventBus {

    /**
     * The current {@link EventManager} that is being decorated.
     */
    private final EventManager eventManager;

    /**
     * Creates a new {@link SimpleEventBus} instance.
     */
    public SimpleEventBus(EventManager eventManager) {
        requireNonNull(eventManager, "eventManager must not be null");
        this.eventManager = eventManager;
    }

    @Override
    public void post(Event event) {
        requireNonNull(event, "event must not be null");
        eventManager.transmitToListeners(event);
    }

}
