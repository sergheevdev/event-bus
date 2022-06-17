package dev.sergheev.eventbus.event.method;

import dev.sergheev.eventbus.Event;
import dev.sergheev.eventbus.EventHandler;
import dev.sergheev.eventbus.EventListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class NotifiableEventHandlerMethodTest {

    /**
     * <p>
     * When using a {@link NotifiableEventHandlerMethod} instance, if we notify the
     * method with an {@link Event} instance, then the method in {@link EventListener}
     * receives that event and mutates its state successfully.
     *
     * <p>
     * In more complex software we might provide the event with domain entities or
     * any other instance which should be mutated or logged by the {@link EventListener}.
     */
    @Test
    public void testWhenSendingNotificationToNotifiableEventHandlerMethodListenerInstanceReceivesEvent() {
        final String prefix = "[Admin]";
        final String username = "Jack Sparrow";
        final String expected = prefix + ' ' + username;
        EventListener eventListener = new UserJoinListener(prefix);
        EventHandlerMethodExtractor eventHandlerMethodExtractor = new EventHandlerMethodExtractor();
        List<EventHandlerMethod> methodsGroup = eventHandlerMethodExtractor.extractMethodsFrom(eventListener.getClass());
        Assert.assertEquals(1, methodsGroup.size());
        EventHandlerMethod eventHandlerMethod = methodsGroup.remove(0);
        NotifiableEventHandlerMethod notifiableEventHandlerMethod = new NotifiableEventHandlerMethod(eventListener, eventHandlerMethod);
        UserJoinEvent concreteEvent = new UserJoinEvent(username);
        notifiableEventHandlerMethod.notifyWith(concreteEvent);
        Assert.assertEquals(expected, concreteEvent.getUsername());
    }

    public static class UserJoinEvent implements Event {

        private String username;

        public UserJoinEvent(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

    }

    public static class UserJoinListener implements EventListener {

        private final String namePrefix;

        public UserJoinListener(String prefix) {
            this.namePrefix = prefix;
        }

        @EventHandler
        public void onUserLogin(UserJoinEvent event) {
            event.setUsername(namePrefix + ' ' + event.getUsername());
        }

    }

}
