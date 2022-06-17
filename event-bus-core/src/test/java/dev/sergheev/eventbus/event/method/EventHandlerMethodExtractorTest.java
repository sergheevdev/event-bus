package dev.sergheev.eventbus.event.method;

import dev.sergheev.eventbus.Event;
import dev.sergheev.eventbus.EventHandler;
import dev.sergheev.eventbus.EventListener;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

public class EventHandlerMethodExtractorTest {

    /**
     * When extracting a group of {@link EventHandlerMethod} instances from a concrete
     * {@link EventListener} class that has only one method defined, then that single
     * method (which meets all preconditions to be converted) is successfully extracted.
     */
    @Test
    public void testWhenExtractingOneMethodWithoutConditionsFromListenerClassReturnsExpectedEventHandlerMethod() {
        EventHandlerMethodExtractor eventHandlerMethodExtractor = new EventHandlerMethodExtractor();
        List<EventHandlerMethod> extractedMethods = eventHandlerMethodExtractor.extractMethodsFrom(FirstConcreteEventListener.class);
        Assert.assertFalse(extractedMethods.isEmpty());
        EventHandlerMethod eventHandlerMethod = extractedMethods.remove(0);
        Assert.assertEquals(FirstConcreteEventListener.class.getMethods()[0], eventHandlerMethod.getMethod());
        Assert.assertEquals(FirstConcreteEvent.class, eventHandlerMethod.getEventType());
        Assert.assertEquals(5, eventHandlerMethod.getOrder());
    }

    private static class FirstConcreteEventListener implements EventListener {
        @EventHandler(order = 5)
        public void onConcrete(FirstConcreteEvent event){
        }
    }

    /**
     * When extracting a group of {@link EventHandlerMethod} instances from a concrete
     * {@link EventListener} class which has multiple methods (which meet all preconditions)
     * then all those methods are successfully extracted.
     */
    @Test
    public void testWhenExtractingMultipleMethodsWithoutConditionsFromListenerClassReturnsExpectedEventHandlerMethods() {
        EventHandlerMethodExtractor eventHandlerMethodExtractor = new EventHandlerMethodExtractor();
        List<EventHandlerMethod> methodsGroup = eventHandlerMethodExtractor.extractMethodsFrom(SecondConcreteEventListener.class);
        Assert.assertEquals(3, methodsGroup.size());
        // Ensure the methods which meet all preconditions are extracted, but those that don't they are not
        Assert.assertTrue(methodsGroup.stream().anyMatch(method -> method.getMethod().getName().equals("onFirstConcrete")));
        Assert.assertTrue(methodsGroup.stream().anyMatch(method -> method.getMethod().getName().equals("onSecondConcrete")));
        Assert.assertTrue(methodsGroup.stream().anyMatch(method -> method.getMethod().getName().equals("onThirdConcrete")));
        Assert.assertFalse(methodsGroup.stream().anyMatch(method -> method.getMethod().getName().equals("onFourthConcrete")));
    }

    private static class SecondConcreteEventListener implements EventListener {
        @EventHandler(order = 1)
        public void onFirstConcrete(SecondConcreteEvent event) {
        }
        @EventHandler(order = 2)
        public void onSecondConcrete(SecondConcreteEvent event) {
        }
        @EventHandler(order = 3)
        public void onThirdConcrete(SecondConcreteEvent event) {
        }
        /** Non-annotated method which the extractor should ignore **/
        public void onFourthConcrete(SecondConcreteEvent event) {
        }
    }

    /**
     * When extracting a group of {@link EventHandlerMethod} instances from a concrete
     * {@link EventListener} class which has multiple methods (which meet all preconditions)
     * then the methods which meet certain predicate conditions are successfully extracted.
     */
    @Test
    public void testWhenExtractingMultipleMethodsWithConditionsFromListenerClassReturnsExpectedEventHandlerMethods() {
        EventHandlerMethodExtractor eventHandlerMethodExtractor = new EventHandlerMethodExtractor();
        // Extract all methods whose event type parameter is equal to a given concrete event type
        Predicate<Method> firstCondition = method -> EventHandlerMethod.createFrom(method).getEventType() == FirstConcreteEvent.class;
        List<EventHandlerMethod> firstMeeting = eventHandlerMethodExtractor.extractMethodsFromMeeting(ThirdConcreteEventListener.class, firstCondition);
        Assert.assertEquals(2, firstMeeting.size());
        Assert.assertTrue(firstMeeting.stream().anyMatch(method -> method.getMethod().getName().equals("onFirstConcreteOne")));
        Assert.assertTrue(firstMeeting.stream().anyMatch(method -> method.getMethod().getName().equals("onFirstConcreteTwo")));
        // Extract all methods whose annotation order is equal to a given concrete order
        Predicate<Method> secondCondition = method -> EventHandlerMethod.createFrom(method).getOrder() == 1;
        List<EventHandlerMethod> secondMeeting = eventHandlerMethodExtractor.extractMethodsFromMeeting(ThirdConcreteEventListener.class, secondCondition);
        Assert.assertEquals(2, secondMeeting.size());
        Assert.assertTrue(secondMeeting.stream().anyMatch(method -> method.getMethod().getName().equals("onFirstConcreteOne")));
        Assert.assertTrue(secondMeeting.stream().anyMatch(method -> method.getMethod().getName().equals("onThirdConcrete")));
        // Extract all methods whose annotation order is equal to zero
        Predicate<Method> thirdCondition = method -> EventHandlerMethod.createFrom(method).getOrder() == 0;
        List<EventHandlerMethod> thirdMeeting = eventHandlerMethodExtractor.extractMethodsFromMeeting(ThirdConcreteEventListener.class, thirdCondition);
        Assert.assertTrue(thirdMeeting.isEmpty());
        // Extract all methods whose method name ends with "One" or "Two"
        Predicate<Method> fourthCondition = method -> method.getName().endsWith("One") || method.getName().endsWith("Two");
        List<EventHandlerMethod> fourthMeeting = eventHandlerMethodExtractor.extractMethodsFromMeeting(ThirdConcreteEventListener.class, fourthCondition);
        Assert.assertTrue(fourthMeeting.stream().anyMatch(method -> method.getMethod().getName().equals("onFirstConcreteOne")));
        Assert.assertTrue(fourthMeeting.stream().anyMatch(method -> method.getMethod().getName().equals("onFirstConcreteTwo")));
    }

    private static class ThirdConcreteEventListener implements EventListener {
        @EventHandler(order = 1)
        public void onFirstConcreteOne(FirstConcreteEvent event) {
        }
        @EventHandler(order = 2)
        public void onFirstConcreteTwo(FirstConcreteEvent event) {
        }
        @EventHandler(order = 3)
        public void onSecondConcrete(SecondConcreteEvent event) {
        }
        @EventHandler(order = 1)
        public void onThirdConcrete(ThirdConcreteEvent event) {
        }
        /** Non-annotated method which the extractor should ignore **/
        public void onFourthConcrete(FourthConcreteEvent event) {
        }
    }

    private static class FirstConcreteEvent implements Event {
    }

    private static class SecondConcreteEvent implements Event {
    }

    private static class ThirdConcreteEvent implements Event {
    }

    private static class FourthConcreteEvent implements Event {
    }

}
