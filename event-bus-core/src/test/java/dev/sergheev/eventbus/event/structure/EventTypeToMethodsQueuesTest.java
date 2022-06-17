package dev.sergheev.eventbus.event.structure;

import dev.sergheev.eventbus.Event;
import dev.sergheev.eventbus.EventListener;
import dev.sergheev.eventbus.event.method.NotifiableEventHandlerMethod;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Queue;

public class EventTypeToMethodsQueuesTest {

    @Test
    public void testRegister() {
        EventTypeToMethodsQueues queue = new EventTypeToMethodsQueues();
        // Adding the first method should return `true`
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        Assert.assertTrue(queue.offer(firstNotifiableMethod.getEventType(), firstNotifiableMethod));
        Assert.assertFalse(queue.isEmpty());
        Assert.assertEquals(1, queue.size());
        // Adding the first method again should return `false` (because it's already registered)
        Assert.assertFalse(queue.offer(firstNotifiableMethod.getEventType(), firstNotifiableMethod));
        Assert.assertFalse(queue.isEmpty());
        Assert.assertEquals(1, queue.size());
        // Adding the second method should return `true`
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        Assert.assertTrue(queue.offer(secondNotifiableMethod.getEventType(), secondNotifiableMethod));
        Assert.assertFalse(queue.isEmpty());
        Assert.assertEquals(2, queue.size());
    }

    @Test
    public void testUnregister() {
        EventTypeToMethodsQueues queue = new EventTypeToMethodsQueues();
        // Populating the queue with all the instantiated methods
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        queue.offer(firstNotifiableMethod.getEventType(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        queue.offer(secondNotifiableMethod.getEventType(), secondNotifiableMethod);
        // Removing twice the same method returns first `true`, but the second time `false`
        Assert.assertTrue(queue.remove(firstNotifiableMethod.getEventType(), firstNotifiableMethod));
        Assert.assertFalse(queue.remove(firstNotifiableMethod.getEventType(), firstNotifiableMethod));
        // Ensure the queue is left with only one method stored
        Assert.assertFalse(queue.isEmpty());
        Assert.assertEquals(1, queue.size());
        Assert.assertEquals(1, queue.getFor(secondNotifiableMethod.getEventType()).size());
        Assert.assertTrue(queue.contains(secondNotifiableMethod.getEventType(), secondNotifiableMethod));
    }

    @Test
    public void testContains() {
        EventTypeToMethodsQueues queue = new EventTypeToMethodsQueues();
        // Populating the queue with all the instantiated methods
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        queue.offer(firstNotifiableMethod.getEventType(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        queue.offer(secondNotifiableMethod.getEventType(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        // Ensure the queue contains all the previously added methods
        Assert.assertTrue(queue.contains(firstNotifiableMethod.getEventType(), firstNotifiableMethod));
        Assert.assertTrue(queue.contains(secondNotifiableMethod.getEventType(), secondNotifiableMethod));
        Assert.assertFalse(queue.contains(thirdNotifiableMethod.getEventType(), thirdNotifiableMethod));
    }

    @Test
    public void testGetFor() {
        EventTypeToMethodsQueues queue = new EventTypeToMethodsQueues();
        // Populating the queue with all the instantiated methods
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock(ConcreteEvent.class);
        queue.offer(firstNotifiableMethod.getEventType(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock(ConcreteEvent.class);
        queue.offer(secondNotifiableMethod.getEventType(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock(AnotherConcreteEvent.class);
        queue.offer(thirdNotifiableMethod.getEventType(), thirdNotifiableMethod);
        // Ensure previously added methods are contained in the list returned by the queue
        Assert.assertTrue(queue.getFor(ConcreteEvent.class).contains(firstNotifiableMethod));
        Assert.assertTrue(queue.getFor(ConcreteEvent.class).contains(secondNotifiableMethod));
        Assert.assertTrue(queue.getFor(AnotherConcreteEvent.class).contains(thirdNotifiableMethod));
    }

    @Test
    public void testGetAll() {
        EventTypeToMethodsQueues queue = new EventTypeToMethodsQueues();
        // Populating the queue with all the instantiated methods
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        queue.offer(firstNotifiableMethod.getEventType(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        queue.offer(secondNotifiableMethod.getEventType(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        queue.offer(thirdNotifiableMethod.getEventType(), thirdNotifiableMethod);
        // Ensure all the added methods to the queue are present in the obtained map
        Map<Class<? extends Event>, Queue<NotifiableEventHandlerMethod>> notifiableEventHandlerMethods = queue.getAll();
        Assert.assertTrue(notifiableEventHandlerMethods.get(firstNotifiableMethod.getEventType()).contains(firstNotifiableMethod));
        Assert.assertTrue(notifiableEventHandlerMethods.get(secondNotifiableMethod.getEventType()).contains(secondNotifiableMethod));
        Assert.assertTrue(notifiableEventHandlerMethods.get(thirdNotifiableMethod.getEventType()).contains(thirdNotifiableMethod));
    }

    @Test
    public void testSizeFor() {
        EventTypeToMethodsQueues queue = new EventTypeToMethodsQueues();
        // Populating the queue with all the instantiated methods (the first two with same event type)
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock(ConcreteEvent.class);
        queue.offer(firstNotifiableMethod.getEventType(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock(ConcreteEvent.class);
        queue.offer(secondNotifiableMethod.getEventType(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock(AnotherConcreteEvent.class);
        queue.offer(thirdNotifiableMethod.getEventType(), thirdNotifiableMethod);
        // Ensure the size for each of the two different events is correspondent to the amount of registered methods
        Assert.assertEquals(2, queue.sizeFor(ConcreteEvent.class));
        Assert.assertEquals(1, queue.sizeFor(AnotherConcreteEvent.class));
    }

    @Test
    public void testSize() {
        EventTypeToMethodsQueues queue = new EventTypeToMethodsQueues();
        // Populating the cache with all the instantiated methods (all with the same event type)
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        queue.offer(firstNotifiableMethod.getEventType(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        queue.offer(secondNotifiableMethod.getEventType(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        queue.offer(thirdNotifiableMethod.getEventType(), thirdNotifiableMethod);
        // Ensure the size is correspondent to the amount of registered methods
        Assert.assertEquals(3, queue.size());
    }

    @Test
    public void testIsEmpty() {
        EventTypeToMethodsQueues queue = new EventTypeToMethodsQueues();
        // Ensuring at first that the queue is empty
        Assert.assertTrue(queue.isEmpty());
        // Populating the queue with all the instantiated methods (all with the same event type)
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        queue.offer(firstNotifiableMethod.getEventType(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        queue.offer(secondNotifiableMethod.getEventType(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        queue.offer(thirdNotifiableMethod.getEventType(), thirdNotifiableMethod);
        // Remove all the registered methods
        queue.remove(firstNotifiableMethod.getEventType(), firstNotifiableMethod);
        queue.remove(secondNotifiableMethod.getEventType(), secondNotifiableMethod);
        queue.remove(thirdNotifiableMethod.getEventType(), thirdNotifiableMethod);
        // Ensure that the queue is empty now
        Assert.assertTrue(queue.isEmpty());
    }

    @Test
    public void testClear() {
        EventTypeToMethodsQueues queue = new EventTypeToMethodsQueues();
        // Populating the queue with all the instantiated methods (all with the same event type)
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        queue.offer(firstNotifiableMethod.getEventType(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        queue.offer(secondNotifiableMethod.getEventType(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        queue.offer(thirdNotifiableMethod.getEventType(), thirdNotifiableMethod);
        // Clear the queue and after that ensure its size is equal to zero (the queue is empty)
        queue.clear();
        Assert.assertEquals(0, queue.size());
        Assert.assertEquals(0, queue.sizeFor(firstNotifiableMethod.getEventType()));
        Assert.assertEquals(0, queue.sizeFor(secondNotifiableMethod.getEventType()));
        Assert.assertEquals(0, queue.sizeFor(thirdNotifiableMethod.getEventType()));
        Assert.assertTrue(queue.isEmpty());
    }

    private static class ConcreteEvent implements Event {
    }

    private static class AnotherConcreteEvent implements Event {
    }

    private static NotifiableEventHandlerMethod createMethodMock() {
        NotifiableEventHandlerMethod notifiableEventHandlerMethod = Mockito.mock(NotifiableEventHandlerMethod.class);
        Mockito.when(notifiableEventHandlerMethod.getEventListener()).thenReturn(Mockito.mock(EventListener.class));
        Mockito.doReturn(ConcreteEvent.class).when(notifiableEventHandlerMethod).getEventType();
        return notifiableEventHandlerMethod;
    }

    private static NotifiableEventHandlerMethod createMethodMock(Class<? extends Event> eventType) {
        NotifiableEventHandlerMethod notifiableEventHandlerMethod = Mockito.mock(NotifiableEventHandlerMethod.class);
        Mockito.when(notifiableEventHandlerMethod.getEventListener()).thenReturn(Mockito.mock(EventListener.class));
        Mockito.doReturn(eventType).when(notifiableEventHandlerMethod).getEventType();
        return notifiableEventHandlerMethod;
    }

}
