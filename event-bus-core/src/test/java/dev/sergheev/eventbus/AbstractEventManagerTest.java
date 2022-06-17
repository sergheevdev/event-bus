package dev.sergheev.eventbus;

import dev.sergheev.eventbus.event.method.EventHandlerMethod;
import dev.sergheev.eventbus.event.structure.EventTypeToMethodsQueues;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @see <a href="https://www.informit.com/articles/article.aspx?p=1398606&seqNum=4">Introduce Polymorphic Creation with Factory Method</a>
 */
public abstract class AbstractEventManagerTest {

    /**
     * <p>
     * Test that when transmitting the same event multiple times, that event's state will
     * get mutated by the correspondent handlers in the correct order (ensuring that each
     * mutation will leave a consistent object, with the expected result).
     *
     * <p>
     * Analogy: consider a number <b>n = 10</b>, by applying those two formulas in different
     * order, it will lead us to a completely different result. The example formulas being:
     * <ol>
     *     <li><b>f(n) = n * 2
     *     <li><b>g(n) = n + 10
     * </ol>
     * Doing <b>f(g(10)) = f(20) = 40</b> is not the same as doing <b>g(f(10)) = g(20) = 30</b>.
     *
     * <p>
     * The main point of this test, is to understand and clarify "visually" that the order
     * of execution of events matters (the order in which the event is passed through different
     * handler methods (which are contained in listeners) will give us one or another result).
     */
    @Test
    public void testTransmitToListeners() {
        EventManager eventManager = createEventManager();
        FirstConcreteEvent firstEvent = new FirstConcreteEvent(10);
        // Try when transmitting the event without any registered listeners (the event state is not mutated)
        eventManager.transmitToListeners(firstEvent);
        Assert.assertEquals(10, firstEvent.getValue());
        // Populate the event manager with the first concrete listener (which will mutate an integer value inside the event)
        eventManager.register(new FirstConcreteEventListener());
        // Transmit the event to the listeners (which are listening to it)
        eventManager.transmitToListeners(firstEvent);
        // Ensure that the event is mutated in the correct order (it was passed to the different event handlers in the correct order)
        // If the event had been passed to handler methods in an incorrect order, the computation result sequence won't be equal
        Assert.assertEquals(40, firstEvent.getValue());
        eventManager.transmitToListeners(firstEvent);
        Assert.assertEquals(100, firstEvent.getValue());
        eventManager.transmitToListeners(firstEvent);
        Assert.assertEquals(220, firstEvent.getValue());
        // Populate the event manager with the second concrete listener (which will mutate a string value inside the event)
        eventManager.register(new SecondConcreteEventListener());
        // Generate a random string to represent (any string)
        String randomString = Long.toHexString(Double.doubleToLongBits(Math.random()));
        SecondConcreteEvent secondEvent = new SecondConcreteEvent(randomString);
        // Ensure that the event is mutated in the correct order (it was passed to the different event handlers in the correct order)
        // If the event had been passed to handler methods in an incorrect order, the computation result sequence won't be equal
        eventManager.transmitToListeners(secondEvent);
        Assert.assertEquals("abc" + randomString, secondEvent.getValue());
        eventManager.transmitToListeners(secondEvent);
        Assert.assertEquals("abcabc" + randomString, secondEvent.getValue());
        eventManager.transmitToListeners(secondEvent);
        Assert.assertEquals("abcabcabc" + randomString, secondEvent.getValue());
    }

    /**
     * <p>
     * Test that when registering a listener instance' handler methods, the first time the
     * returned amount will be the amount of newly registered methods, after that, if trying
     * to register the same listener instance again, the amount of newly registered methods will
     * be zero (because the handler methods are already present in the current event manager).
     */
    @Test
    public void testRegister() {
        EventManager eventManager = createEventManager();
        EventListener firstListener = new FirstConcreteEventListener();
        // The first time the returned value is the amount of registered handler methods, the following times, zero
        Assert.assertEquals(2, eventManager.register(firstListener));
        Assert.assertEquals(0, eventManager.register(firstListener));
        EventListener secondListener = new FirstConcreteEventListener();
        // The first time the returned value is the amount of registered handler methods, the following times, zero
        Assert.assertEquals(2, eventManager.register(secondListener));
        Assert.assertEquals(0, eventManager.register(secondListener));
        EventListener thirdListener = new SecondConcreteEventListener();
        // The first time the returned value is the amount of registered handler methods, the following times, zero
        Assert.assertEquals(3, eventManager.register(thirdListener));
        Assert.assertEquals(0, eventManager.register(thirdListener));
        EventListener fourthListener = new SecondConcreteEventListener();
        // The first time the returned value is the amount of registered handler methods, the following times, zero
        Assert.assertEquals(3, eventManager.register(fourthListener));
        Assert.assertEquals(0, eventManager.register(fourthListener));
    }

    /**
     * <p>
     * Test that when registering some concrete handlers of a given listener instance, which
     * meet a given predicate, then the registered handler methods are only the ones that
     * successfully passed that given predicate conditions (or prerequisites).
     *
     * <p>
     * We can find more rigorous ways to workaround this problem and ensure that the registered
     * methods are only the ones which meet the predicate. But the most intuitive way of doing
     * this is creating a sequence which will be a combination of applying different formulas.
     *
     * <p>
     * To ensure that only the desired handler methods had been registered, we create an event,
     * and after each event transmission to listeners, check if only the desired formulas are
     * applied, if more formulas are applied and the value doesn't follow the sequence, then the
     * registration (only meeting that predicate) has been unsuccessful or successful otherwise.
     */
    @Test
    public void testRegisterMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with the first concrete listener (which will mutate an integer value inside the event)
        EventListener firstListener = new FirstConcreteEventListener();
        // Register only the first handler method called "onFirstConcreteOne" (which only increments the integer value by 10 units)
        eventManager.registerMeeting(firstListener, (method) -> EventHandlerMethod.createFrom(method).getOrder() == 1);
        FirstConcreteEvent firstEvent = new FirstConcreteEvent(10);
        // Transmit the event to the listeners (which are listening to it)
        eventManager.transmitToListeners(firstEvent);
        // Ensure that the event is mutated only by the registered handlers (which should be only the ones whose order is equal to one)
        // If the event will be passed through different handler methods whose mutation operation is different the sequence won't be equal
        Assert.assertEquals(20, firstEvent.getValue());
        eventManager.transmitToListeners(firstEvent);
        Assert.assertEquals(30, firstEvent.getValue());
        eventManager.transmitToListeners(firstEvent);
        Assert.assertEquals(40, firstEvent.getValue());
        // Populate the event manager with the second concrete listener (which will mutate a string value inside the event)
        EventListener secondListener = new SecondConcreteEventListener();
        // Register only the handlers called "onSecondConcreteTwo" and "onSecondConcreteThree" (which only appends to the string the "ab" sequence)
        eventManager.registerMeeting(secondListener, (method) -> EventHandlerMethod.createFrom(method).getOrder() >= 2);
        // Generate a random string to represent (any string)
        String randomString = Long.toHexString(Double.doubleToLongBits(Math.random()));
        SecondConcreteEvent secondEvent = new SecondConcreteEvent(randomString);
        // Ensure that the event is mutated only by the registered handlers (which should be only the ones whose order is greater or equal to two)
        // If the event will be passed through different handler methods whose mutation operation is different the sequence won't be equal
        eventManager.transmitToListeners(secondEvent);
        Assert.assertEquals("ab" + randomString, secondEvent.getValue());
        eventManager.transmitToListeners(secondEvent);
        Assert.assertEquals("abab" + randomString, secondEvent.getValue());
        eventManager.transmitToListeners(secondEvent);
        Assert.assertEquals("ababab" + randomString, secondEvent.getValue());
    }

    /**
     * <p>
     * Test that when unregistering some listener instance' handler methods, the amount
     * of unregistered methods per each listener equals to the amount of registered at
     * the beginning for that concrete listener.
     */
    @Test
    public void testUnregister() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        EventListener secondListener = new SecondConcreteEventListener();
        EventListener thirdListener = new FirstConcreteEventListener();
        // Attempt unregistering a non-registered listener (the returned value must be zero)
        int unregisterAmount = eventManager.unregister(firstListener);
        Assert.assertEquals(0, unregisterAmount);
        // Now register all the previously instantiated listener instances (and keep track of the amount of registered handler methods)
        int firstAmount = eventManager.register(firstListener);;
        int secondAmount = eventManager.register(secondListener);
        int thirdAmount = eventManager.register(thirdListener);
        // Ensure the amount of removed methods amount is equal to the amount of registered methods per listener (at the beginning)
        Assert.assertEquals(firstAmount, eventManager.unregister(firstListener));
        Assert.assertEquals(secondAmount, eventManager.unregister(secondListener));
        Assert.assertEquals(thirdAmount, eventManager.unregister(thirdListener));
        // At the end, the event manager registry must be empty (because we unregistered all listener instances)
        Assert.assertEquals(0, eventManager.size());
        // Test registering the first listener back and trying to force an inconsistent state
        eventManager.register(firstListener);
        try {
            // Access one of the data structures in the event manager
            Field field = eventManager.getClass().getDeclaredField("eventTypeToMethodsQueues");
            field.setAccessible(true);
            EventTypeToMethodsQueues manager = (EventTypeToMethodsQueues) field.get(eventManager);
            // Clear its contents to force an inconsistent state between the data structures
            manager.clear();
            eventManager.unregister(firstListener);
            throw new AssertionError("Expected an IllegalStateException to be thrown");
        } catch(Exception e) {
            // Expect an "IllegalStateException" to be thrown
            Assert.assertTrue(e instanceof IllegalStateException);
        }
    }

    /**
     * <p>
     * Test that when unregistering some listener instance' handler methods that meet a
     * concrete {@link Predicate}, only the methods which meet that certain predicate
     * are unregistered, and the other ones are not (they remain registered).
     */
    @Test
    public void testUnregisterMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        // We register two event handler methods (with order 1 and 2 respectively, for each handler method)
        int registeredAmount = eventManager.register(firstListener);
        // At first, we must have 2 handler methods registered
        Assert.assertEquals(2, registeredAmount);
        // Unregister from the manager the method with order equal to one (only the first handler method)
        Assert.assertEquals(1, eventManager.unregisterMeeting(firstListener, (method) -> EventHandlerMethod.createFrom(method).getOrder() == 1));
        // Ensure there is only one handler method left registered (whose order is equal to two)
        Assert.assertEquals(1, eventManager.sizeMeeting((method) -> EventHandlerMethod.createFrom(method).getOrder() == 2));
        // Unregister from the manager the method with order equal to two (unregister the last handler method left)
        Assert.assertEquals(1, eventManager.unregisterMeeting(firstListener, (method) -> EventHandlerMethod.createFrom(method).getOrder() == 2));
        // Ensure there are not handlers left, the event manager registry must be empty (because we removed them all)
        Assert.assertEquals(0, eventManager.handlersAmountWith(firstListener.getClass()));
    }

    /**
     * <p>
     * Test that when unregistering handler methods related to a concrete class, then
     * the only methods which are unregistered are the ones that are contained in that
     * class.
     */
    @Test
    public void testUnregisterWithMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        // Keep track of the amount of registered handler methods for the first listener
        int firstAmount = eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        // Keep track of the amount of registered handler methods for the second listener
        int secondAmount = eventManager.register(secondListener);
        // First we just ensure the amount of total handler methods is the sum of the two previous amounts
        Assert.assertEquals(firstAmount + secondAmount, eventManager.handlersAmount());
        // The predicate which handler methods must meet in order to be unregistered
        Predicate<Method> predicate = (method) -> EventHandlerMethod.createFrom(method).getOrder() == 1;
        // Only two handler methods are removed from the event manager (because we have only one handler method of order one per each listener)
        Assert.assertEquals(2, eventManager.unregisterWithMeeting(FirstConcreteEventListener.class, predicate));
        // The expected amount of still-registered handler methods is = (total registered amount) - (just unregistered amount)
        int expectedLeft = (firstAmount + secondAmount) - 2;
        Assert.assertEquals(expectedLeft, eventManager.handlersAmountWith(FirstConcreteEventListener.class));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager, it contains those
     * listener instances (ensuring that the listeners were successfully registered and are
     * currently present in the event manager's registry).
     */
    @Test
    public void testGetAll() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        // Ensure that all the registered listeners are present in the event manager
        Set<EventListener> listeners = eventManager.getAll();
        Assert.assertTrue(listeners.contains(firstListener));
        Assert.assertTrue(listeners.contains(secondListener));
        Assert.assertTrue(listeners.contains(thirdListener));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager, and then trying
     * fetch some of the listeners whose methods meet a concrete predicate, the returned
     * listeners are the expected.
     */
    @Test
    public void testGetMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        // The predicate that must be met for a handler method for its listener to be collected
        Predicate<Method> predicate = (method) -> EventHandlerMethod.createFrom(method).getOrder() <= 2;
        // Returns the collected listener instances all of whose handler methods met the given predicate
        Set<EventListener> listeners = eventManager.getMeeting(predicate);
        // Ensure there are only 2 listeners which meet the given predicate
        Assert.assertEquals(2, listeners.size());
        // These return true, because all the handler methods of the current listener meet the predicate
        Assert.assertTrue(listeners.contains(firstListener));
        Assert.assertTrue(listeners.contains(secondListener));
        // This one returns false, because there is a method whose order is equal to 3, therefore it does not meet the predicate
        Assert.assertFalse(listeners.contains(thirdListener));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager, and then trying
     * fetch some of the listeners of a concrete class, the returned listeners are the
     * expected.
     */
    @Test
    public void testGetWith() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        // Fetch all the listeners whose class is the provided one
        Set<EventListener> listeners = eventManager.getWith(FirstConcreteEventListener.class);
        Assert.assertTrue(listeners.contains(firstListener));
        Assert.assertTrue(listeners.contains(secondListener));
        // The "third listener" is not contained in the returned list because its class is not equal to the one we asked for
        Assert.assertFalse(listeners.contains(thirdListener));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager, and then trying
     * fetch some of the listeners of a concrete class type whose methods meet a concrete
     * predicate, the returned listeners are the expected.
     */
    @Test
    public void testGetWithMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        // The predicate that must be met for a handler method for its listener to be collected
        Predicate<Method> predicate = (method) -> EventHandlerMethod.createFrom(method).getOrder() <= 2;
        // Returns the collected listener instances all of whose handler methods met the given predicate
        Set<EventListener> listeners = eventManager.getWithMeeting(SecondConcreteEventListener.class, predicate);
        // There are no listeners found because the "SecondConcreteEventListener" has a method with order value of 3
        Assert.assertEquals(0, listeners.size());
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager, and then trying
     * check if those listeners are present in the event manager's registry, then the
     * result is the expected.
     */
    @Test
    public void testContains() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        // Don't register the fourth listener in the event manager
        EventListener fourthListener = new SecondConcreteEventListener();
        // There must be only three registered listener instances
        Assert.assertEquals(3, eventManager.size());
        // All the registered listeners must be present in the event manager's registry except for the fourth one
        Assert.assertTrue(eventManager.contains(firstListener));
        Assert.assertTrue(eventManager.contains(secondListener));
        Assert.assertTrue(eventManager.contains(thirdListener));
        Assert.assertFalse(eventManager.contains(fourthListener));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager, and then trying
     * check if the event manager contains any listener whose methods meet a concrete given
     * predicate, then the result is the expected.
     */
    @Test
    public void testContainsAnyMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        // Ensure there exists a listener whose method event type is "FirstConcreteEvent"
        Predicate<Method> firstPredicate = (method) -> EventHandlerMethod.createFrom(method).getEventType() == FirstConcreteEvent.class;
        Assert.assertTrue(eventManager.containsAnyMeeting(firstPredicate));
        // Ensure it doesn't exist a listener whose method event type is "SecondConcreteEvent" (because we haven't registered one yet)
        Predicate<Method> secondPredicate = (method) -> EventHandlerMethod.createFrom(method).getEventType() == SecondConcreteEvent.class;
        Assert.assertFalse(eventManager.containsAnyMeeting(secondPredicate));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager, and then trying
     * check if the event manager contains any listener whose class is of a concrete type,
     * then the result is the expected.
     */
    @Test
    public void testContainsAnyWith() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        // Ensure there exists a listener whose class type is the given
        Assert.assertTrue(eventManager.containsAnyWith(FirstConcreteEventListener.class));
        // It shouldn't exist a listener whose class type is one of which any listener has been registered yet
        Assert.assertFalse(eventManager.containsAnyWith(SecondConcreteEventListener.class));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager, and then trying
     * check if the event manager contains any listener whose class is of a concrete type,
     * and whose methods meet a concrete predicate, then the result is the expected.
     */
    @Test
    public void testContainsAnyWithMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        // Ensure there exists a listener whose class type is the given and whose methods meet the given predicate
        Predicate<Method> firstPredicate = (method) -> EventHandlerMethod.createFrom(method).getEventType() == FirstConcreteEvent.class;
        Assert.assertTrue(eventManager.containsAnyWithMeeting(FirstConcreteEventListener.class, firstPredicate));
        // Even though there exists listeners whose class type is the given, there's no method whose type (which they're able to consume) is the given
        Predicate<Method> secondPredicate = (method) -> EventHandlerMethod.createFrom(method).getEventType() == SecondConcreteEvent.class;
        Assert.assertFalse(eventManager.containsAnyWithMeeting(FirstConcreteEventListener.class, secondPredicate));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager, and clearing
     * the registry, then the event manager registry must be empty (all sizes must be
     * zeroes).
     */
    @Test
    public void testClearAll() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        EventListener fourthListener = new SecondConcreteEventListener();
        eventManager.register(fourthListener);
        // Clear the event manager registry (by doing so, we remove all currently registered listeners in that manager)
        eventManager.clearAll();
        // Ensure the size of the event manager metrics is all zeroes
        Assert.assertEquals(0, eventManager.size());
        Assert.assertEquals(0, eventManager.sizeWith(FirstConcreteEventListener.class));
        Assert.assertEquals(0, eventManager.sizeWith(SecondConcreteEventListener.class));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager and then checking,
     * the amount of handler methods whose containing listener class is the given, then the
     * result is the expected.
     */
    @Test
    public void testHandlersAmountWith() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        EventListener fourthListener = new SecondConcreteEventListener();
        eventManager.register(fourthListener);
        // Per "FirstConcreteEventListener" we have 2 event handler methods (as we have registered 2 listeners we must have 4 handler methods)
        Assert.assertEquals(4, eventManager.handlersAmountWith(FirstConcreteEventListener.class));
        // Per "SecondConcreteEventListener" we have 3 event handler methods (as we have registered 2 listeners we must have 6 handler methods)
        Assert.assertEquals(6, eventManager.handlersAmountWith(SecondConcreteEventListener.class));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager and then checking,
     * the amount of registered handler methods that meet a concrete given predicate, then
     * the result is the expected.
     */
    @Test
    public void testHandlersAmountMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        // Each of the registered listeners contains one method with order equal to 1, so 3 listeners * 1 methods/listener = 3 handler methods
        Assert.assertEquals(3, eventManager.handlersAmountMeeting((method) -> EventHandlerMethod.createFrom(method).getOrder() == 1));
        // Each of the registered listeners contains one method with order equal to 2, so 3 listeners * 1 methods/listener = 3 handler methods
        Assert.assertEquals(3, eventManager.handlersAmountMeeting((method) -> EventHandlerMethod.createFrom(method).getOrder() == 2));
        // Only the third listener contains one method with order equal to 3, so 1 listeners * 1 methods/listener = 1 handler method
        Assert.assertEquals(1, eventManager.handlersAmountMeeting((method) -> EventHandlerMethod.createFrom(method).getOrder() == 3));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager and then checking,
     * the amount of registered handler methods whose containing listener class is the given
     * and meet a concrete given predicate, then the result is the expected.
     */
    @Test
    public void testHandlersAmountWithMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        EventListener fourthListener = new SecondConcreteEventListener();
        eventManager.register(fourthListener);
        // There is one handler method with order 1 per each listener with "FirstConcreteEventListener" type (2 listeners * 1 methods/listener = 2 handler methods)
        Predicate<Method> firstPredicate = (method) -> EventHandlerMethod.createFrom(method).getOrder() == 1;
        Assert.assertEquals(2, eventManager.handlersAmountWithMeeting(FirstConcreteEventListener.class, firstPredicate));
        // There is one handler method with order 2 per each listener with "FirstConcreteEventListener" type (2 listeners * 1 methods/listener = 2 handler methods)
        Predicate<Method> secondPredicate = (method) -> EventHandlerMethod.createFrom(method).getOrder() == 2;
        Assert.assertEquals(2, eventManager.handlersAmountWithMeeting(FirstConcreteEventListener.class, secondPredicate));
        // There are zero handler methods with order 3 per each listener with "FirstConcreteEventListener" type (2 listeners * 0 methods/listener = 0 handler methods)
        Predicate<Method> thirdPredicate = (method) -> EventHandlerMethod.createFrom(method).getOrder() == 3;
        Assert.assertEquals(0, eventManager.handlersAmountWithMeeting(FirstConcreteEventListener.class, thirdPredicate));
        // There is one handler method with order 3 per each listener with "SecondConcreteEventListener" type (2 listeners * 1 methods/listener = 2 handler methods)
        Predicate<Method> fourthPredicate = (method) -> EventHandlerMethod.createFrom(method).getOrder() == 3;
        Assert.assertEquals(2, eventManager.handlersAmountWithMeeting(SecondConcreteEventListener.class, fourthPredicate));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager and when checking
     * the amount of registered handler methods, then the result is the expected.
     */
    @Test
    public void testHandlersAmount() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        EventListener fourthListener = new SecondConcreteEventListener();
        eventManager.register(fourthListener);
        // Each "FirstConcreteEventListener" instance has 2 handler methods, 2 listeners * 2 methods/listener = 4 handler methods
        // Each "SecondConcreteEventListener" instance has 3 handler methods, 2 listeners * 3 methods/listener = 6 handler methods
        // The total amount of registered handler methods must be 4 + 6 = 10 handler methods
        Assert.assertEquals(10, eventManager.handlersAmount());
    }


    /**
     * <p>
     * Test that after registering some listeners into the event manager and when checking
     * the amount of registered listener instances of a concrete type, then the result is
     * the expected.
     */
    @Test
    public void testSizeWith() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        // We registered two listeners of the type "FirstConcreteEventListener", so the size must be equal to 2
        Assert.assertEquals(2, eventManager.sizeWith(FirstConcreteEventListener.class));
        // We registered one listener of the type "SecondConcreteEventListener", so the size must be equal to 1
        Assert.assertEquals(1, eventManager.sizeWith(SecondConcreteEventListener.class));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager and when checking
     * the amount of registered listener instances all of whose handler methods meet the
     * given predicate, then the result is the expected.
     */
    @Test
    public void testSizeMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        // The amount of listeners all of whose handler methods' consumable parameter is "FirstConcreteEvent" (we registered only two listeners of that type)
        Predicate<Method> firstPredicate = (method) -> EventHandlerMethod.createFrom(method).getEventType() == FirstConcreteEvent.class;
        Assert.assertEquals(2, eventManager.sizeMeeting(firstPredicate));
        // The amount of listeners all of whose handler methods' consumable parameter is "SecondConcreteEvent" (we registered only one listener of that type)
        Predicate<Method> secondPredicate = (method) -> EventHandlerMethod.createFrom(method).getEventType() == SecondConcreteEvent.class;
        Assert.assertEquals(1, eventManager.sizeMeeting(secondPredicate));
        // The amount of listeners all of whose handler methods' order must be equal to one
        // (in all the listener classes, there are handler methods with order != 1, therefore there are no listeners, because all the handler methods in the listener must be of order == 1)
        Predicate<Method> thirdPredicate = (method) -> EventHandlerMethod.createFrom(method).getOrder() == 1;
        Assert.assertEquals(0, eventManager.sizeMeeting(thirdPredicate));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager and when checking
     * the amount of registered listener instances of a concrete type and all of whose
     * handler methods meet the given predicate, then the result is the expected.
     */
    @Test
    public void testSizeWithMeeting() {
        EventManager eventManager = createEventManager();
        // Populate the event manager with all the instantiated listeners
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        // The predicates that are to be met
        Predicate<Method> firstEventPredicate = (method) -> EventHandlerMethod.createFrom(method).getEventType() == FirstConcreteEvent.class;
        Predicate<Method> secondEventPredicate = (method) -> EventHandlerMethod.createFrom(method).getEventType() == SecondConcreteEvent.class;
        // All the two listeners of the "FirstConcreteEventListener" type all of whose handler methods match the "firstEventPredicate" (there must be 2 listeners that match the predicate)
        Assert.assertEquals(2, eventManager.sizeWithMeeting(FirstConcreteEventListener.class, firstEventPredicate));
        // The only listener of the "SecondConcreteEventListener" type all of whose handler methods match the "secondEventPredicate" (there must be 1 listener that matches the predicate)
        Assert.assertEquals(1, eventManager.sizeWithMeeting(SecondConcreteEventListener.class, secondEventPredicate));
        // No listeners of the "FirstConcreteEventListener" type all of whose handler methods match the "secondEventPredicate"
        // (because there are no handler methods whose consumable parameter is of the "SecondConcreteEvent" type in the "FirstConcreteEventListener")
        Assert.assertEquals(0, eventManager.sizeWithMeeting(FirstConcreteEventListener.class, secondEventPredicate));
        // No listeners of the "SecondConcreteEventListener" type all of whose handler methods match the "firstEventPredicate"
        // (because there are no handler methods whose consumable parameter is of the "FirstConcreteEvent" type in the "SecondConcreteEventListener")
        Assert.assertEquals(0, eventManager.sizeWithMeeting(SecondConcreteEventListener.class, firstEventPredicate));
    }

    /**
     * <p>
     * Test that after registering some listeners into the event manager and when checking
     * the amount of registered listener instances, then the result is the expected.
     */
    @Test
    public void testSize() {
        EventManager eventManager = createEventManager();
        // Populating the event manager and purposely removing and re-adding again the instances (to ensure the procedures are bug-free)
        EventListener firstListener = new FirstConcreteEventListener();
        eventManager.register(firstListener);
        eventManager.unregister(firstListener);
        eventManager.register(firstListener);
        EventListener secondListener = new FirstConcreteEventListener();
        eventManager.register(secondListener);
        eventManager.unregister(secondListener);
        eventManager.register(secondListener);
        EventListener thirdListener = new SecondConcreteEventListener();
        eventManager.register(thirdListener);
        eventManager.unregister(thirdListener);
        eventManager.register(thirdListener);
        // The final amount of registered listeners must be equal to 3 (because we registered only three listener instances)
        Assert.assertEquals(3, eventManager.size());
    }

    private static class FirstConcreteEvent implements Event {

        private int value;

        public FirstConcreteEvent(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

    }

    private static class SecondConcreteEvent implements Event {

        private String value;

        public SecondConcreteEvent(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    private static class FirstConcreteEventListener implements EventListener {

        @EventHandler(order = 1)
        public void onFirstConcreteOne(FirstConcreteEvent event) {
            event.setValue(event.getValue() + 10);
        }

        @EventHandler(order = 2)
        public void onFirstConcreteTwo(FirstConcreteEvent event) {
            event.setValue(event.getValue() * 2);
        }

    }

    private static class SecondConcreteEventListener implements EventListener {

        @EventHandler(order = 1)
        public void onSecondConcreteOne(SecondConcreteEvent event) {
            event.setValue('c' + event.getValue());
        }

        @EventHandler(order = 2)
        public void onSecondConcreteTwo(SecondConcreteEvent event) {
            event.setValue('b' + event.getValue());
        }

        @EventHandler(order = 3)
        public void onSecondConcreteThree(SecondConcreteEvent event) {
            event.setValue('a' + event.getValue());
        }

    }

    public abstract EventManager createEventManager();

}
