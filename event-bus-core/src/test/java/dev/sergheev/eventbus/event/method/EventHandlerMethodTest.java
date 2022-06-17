package dev.sergheev.eventbus.event.method;

import dev.sergheev.eventbus.Event;
import dev.sergheev.eventbus.EventHandler;
import dev.sergheev.eventbus.EventListener;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class EventHandlerMethodTest {

    /**
     * When creating an {@link EventHandlerMethod}, it successfully assigns the default values.
     */
    @Test
    public void testWhenCreatingAnEventHandlerMethodDefaultValuesAreSuccessfullyAssigned() {
        EventListener eventListener = new CorrectEventListener();
        Method method = eventListener.getClass().getMethods()[0];
        EventHandlerMethod eventHandlerMethod = EventHandlerMethod.createFrom(method);
        Assert.assertEquals(method, eventHandlerMethod.getMethod());
        Assert.assertEquals(0, eventHandlerMethod.getOrder());
        Assert.assertEquals(CorrectEvent.class, eventHandlerMethod.getEventType());
    }

    private static class CorrectEvent implements Event {
    }

    private static class CorrectEventListener implements EventListener {
        @EventHandler
        public void onCorrect(CorrectEvent event) {
        }
    }

    /**
     * When creating an {@link EventHandlerMethod}, if the provided method is {@code null}, then
     * a {@link NullPointerException} is thrown.
     */
    @Test(expected = NullPointerException.class)
    public void testWhenCreatingAnEventHandlerMethodWithNullProvidedRawMethodThrowsException() {
        EventHandlerMethod.createFrom(null);
    }

    /**
     * When creating an {@link EventHandlerMethod}, if the method's declaring class does not
     * implement {@link EventListener}, then an {@link IllegalArgumentException} is thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWhenCreatingAnEventHandlerMethodWithDeclaringClassThatDoesNotImplementEventListenerThrowsException() {
        WrongDeclaringClassEventListener eventListener = new WrongDeclaringClassEventListener();
        Method method = eventListener.getClass().getMethods()[0];
        EventHandlerMethod.createFrom(method);
    }

    private static class WrongDeclaringClassEventListener {
        @EventHandler
        public void onCorrect(CorrectEvent event) {
        }
    }

    /**
     * When creating an {@link EventHandlerMethod}, if the method in the {@link EventListener} is
     * not annotated with {@link EventHandler}, then an {@link IllegalArgumentException} is thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWhenCreatingAnEventHandlerMethodFromAnUnannotatedMethodThrowsException() {
        EventListener eventListener = new UnAnnotatedHandlerMethodEventListener();
        Method method = eventListener.getClass().getMethods()[0];
        EventHandlerMethod.createFrom(method);
    }

    private static class UnAnnotatedHandlerMethodEventListener implements EventListener {
        public void onWrongHandlerMethod() {
        }
    }

    /**
     * When creating an {@link EventHandlerMethod}, if the method return type does
     * not return {@link Void}, then an {@link IllegalArgumentException} is thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWhenCreatingAnEventHandlerMethodIfReturnTypeIsNotVoidThrowsException() {
        WrongReturnTypeEventListener eventListener = new WrongReturnTypeEventListener();
        Method method = eventListener.getClass().getMethods()[0];
        EventHandlerMethod.createFrom(method);
    }

    private static class WrongReturnTypeEventListener {
        @EventHandler
        public String onCorrect(CorrectEvent event) {
            return "";
        }
    }

    /**
     * When creating an {@link EventHandlerMethod}, if the parameters amount in the method
     * from the {@link EventListener} is not exactly 1 (because an {@link EventHandlerMethod}
     * is restricted to accept a single {@link Event} instance as parameter), then an
     * {@link IllegalArgumentException} is thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWhenCreatingAnEventHandlerMethodWithWrongParametersAmountInTheListenerMethodThrowsException() {
        EventListener eventListener = new WrongHandlerMethodParametersAmountEventListener();
        Method method = eventListener.getClass().getMethods()[0];
        EventHandlerMethod.createFrom(method);
    }

    private static class WrongHandlerMethodParametersAmountEventListener implements EventListener {
        @EventHandler
        public void onWrongHandlerMethod() {
        }
    }

    /**
     * When creating an {@link EventHandlerMethod}, if the method from the {@link EventListener}
     * is of a type that doesn't implement {@link Event} (which is the only accepted type), then
     * an {@link IllegalArgumentException} is thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWhenCreatingAnEventHandlerMethodWithWrongListenerMethodParameterTypeThrowsException() {
        EventListener eventListener = new WrongHandlerMethodParametersTypeEventListener();
        Method method = eventListener.getClass().getMethods()[0];
        EventHandlerMethod.createFrom(method);
    }

    private static class WrongHandlerMethodParametersTypeEventListener implements EventListener {
        @EventHandler
        public void onWrongHandlerMethod(String invalidParameterType) {
        }
    }

    /**
     * When having two {@link EventHandlerMethod} that are two different instances of the
     * same type, but contain the same data, then they must be equal and return the same
     * hashcode.
     */
    @Test
    public void testThatTwoEventHandlerMethodDifferentInstancesButWithSameDataAreEqualAndReturnSameHashCode() {
        EventListener eventListener = new CorrectEventListener();
        Method method = eventListener.getClass().getMethods()[0];
        EventHandlerMethod firstEventHandlerMethod = EventHandlerMethod.createFrom(method);
        EventHandlerMethod secondEventHandlerMethod = EventHandlerMethod.createFrom(method);
        Assert.assertEquals(firstEventHandlerMethod, secondEventHandlerMethod);
        Assert.assertEquals(firstEventHandlerMethod.hashCode(), secondEventHandlerMethod.hashCode());
    }

    /**
     * When comparing two {@link EventHandlerMethod} that are two different instances of the
     * same type, and contain the same data, then the comparison method returns zero (this
     * means the objects are equal).
     */
    @Test
    public void testWhenComparingTwoEqualEventHandlerMethodsComparisonReturnsZero() {
        EventListener eventListener = new LowerOrderEventListener();
        Method method = eventListener.getClass().getMethods()[0];
        EventHandlerMethod firstEventHandlerMethod = EventHandlerMethod.createFrom(method);
        EventHandlerMethod secondEventHandlerMethod = EventHandlerMethod.createFrom(method);
        Assert.assertEquals(0, firstEventHandlerMethod.compareTo(secondEventHandlerMethod));
    }

    /**
     * When comparing two {@link EventHandlerMethod} that are two different instances of the
     * same type, the only field that is involved in the comparison process is the
     * {@link EventHandlerMethod#getOrder()} field, so having the first method with higher
     * order value, and the second one, with lower order value, the comparison returns the
     * value "one" (meaning that the first has higher order value than the second).
     */
    @Test
    public void testWhenComparingHigherWithLowerEventHandlerMethodComparisonReturnsOne() {
        EventListener lowerOrderEventListener = new LowerOrderEventListener();
        EventListener higherOrderEventListener = new HigherOrderEventListener();
        Method lowerOrderMethod = lowerOrderEventListener.getClass().getMethods()[0];
        Method higherOrderMethod = higherOrderEventListener.getClass().getMethods()[0];
        EventHandlerMethod lowerEventHandlerMethod = EventHandlerMethod.createFrom(lowerOrderMethod);
        EventHandlerMethod higherEventHandlerMethod = EventHandlerMethod.createFrom(higherOrderMethod);
        Assert.assertEquals(1, higherEventHandlerMethod.compareTo(lowerEventHandlerMethod));
    }

    /**
     * When comparing two {@link EventHandlerMethod} that are two different instances of the
     * same type, the only field that is involved in the comparison process is the
     * {@link EventHandlerMethod#getOrder()} field, so having the first method with lower
     * order value, and the second one, with higher order value, the comparison returns the
     * value "minus one" (meaning that the first has lower order value than the second).
     */
    @Test
    public void testWhenComparingLowerWithHigherEventHandlerMethodComparisonReturnsMinusOne() {
        EventListener lowerOrderEventListener = new LowerOrderEventListener();
        EventListener higherOrderEventListener = new HigherOrderEventListener();
        Method lowerOrderMethod = lowerOrderEventListener.getClass().getMethods()[0];
        Method higherOrderMethod = higherOrderEventListener.getClass().getMethods()[0];
        EventHandlerMethod lowerEventHandlerMethod = EventHandlerMethod.createFrom(lowerOrderMethod);
        EventHandlerMethod higherEventHandlerMethod = EventHandlerMethod.createFrom(higherOrderMethod);
        Assert.assertEquals(-1, lowerEventHandlerMethod.compareTo(higherEventHandlerMethod));
    }

    private static class LowerOrderEventListener implements EventListener {
        @EventHandler(order = 1)
        public void onLowerOrder(OrderEvent event) {
        }
    }

    private static class HigherOrderEventListener implements EventListener {
        @EventHandler(order = 2)
        public void onHigherOrder(OrderEvent event) {
        }
    }

    private static class OrderEvent implements Event {
    }

}
