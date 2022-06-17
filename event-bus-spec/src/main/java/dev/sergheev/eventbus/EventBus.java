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

/**
 * <p>
 * An object that publishes certain events to inform all {@link EventListener} listening to
 * that concrete {@link Event} type about something that happened.
 *
 * <p>
 * This object is responsible for taking in an {@link Event}, finding out which are all the
 * methods in the {@link EventListener} instances that are listening and can consume that
 * concrete {@link Event} class type, finally calling by order (execution priority) all the
 * corresponding methods in the {@link EventListener} instances.
 */
@FunctionalInterface
public interface EventBus {

    /**
     * Posts the event that will be delivered to the handlers subscribed for that {@link Event} type.
     * @param event the event that is to be published
     */
    void post(Event event);

}
