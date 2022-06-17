package dev.sergheev.eventbus.event.structure;

import dev.sergheev.eventbus.EventListener;
import dev.sergheev.eventbus.event.method.NotifiableEventHandlerMethod;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Set;

public class EventListenerToMethodsCacheTest {

    @Test
    public void testAdd() {
        EventListenerToMethodsCache cache = new EventListenerToMethodsCache();
        // Adding the first method should return `true`
        NotifiableEventHandlerMethod firstMethod = createMethodMock();
        Assert.assertTrue(cache.map(firstMethod.getEventListener(), firstMethod));
        Assert.assertFalse(cache.isEmpty());
        Assert.assertEquals(1, cache.size());
        Assert.assertTrue(cache.contains(firstMethod.getEventListener(), firstMethod));
        // Adding the same method should return `false` (because the method was already registered)
        Assert.assertFalse(cache.map(firstMethod.getEventListener(), firstMethod));
        Assert.assertFalse(cache.isEmpty());
        Assert.assertEquals(1, cache.size());
        Assert.assertTrue(cache.contains(firstMethod.getEventListener(), firstMethod));
        // Adding another non-registered method should return `true`
        NotifiableEventHandlerMethod secondMethod = createMethodMock();
        Assert.assertTrue(cache.map(secondMethod.getEventListener(), secondMethod));
        Assert.assertFalse(cache.isEmpty());
        Assert.assertEquals(2, cache.size());
        Assert.assertTrue(cache.contains(secondMethod.getEventListener(), secondMethod));
    }

    @Test
    public void testRemove() {
        EventListenerToMethodsCache cache = new EventListenerToMethodsCache();
        // Populating the cache with all the instantiated methods
        NotifiableEventHandlerMethod firstMethod = createMethodMock();
        cache.map(firstMethod.getEventListener(), firstMethod);
        NotifiableEventHandlerMethod secondMethod = createMethodMock();
        cache.map(secondMethod.getEventListener(), secondMethod);
        // Removing twice the same method returns first `true`, but the second time `false`
        Assert.assertTrue(cache.unmap(firstMethod.getEventListener(), firstMethod));
        Assert.assertFalse(cache.unmap(firstMethod.getEventListener(), firstMethod));
        // Ensure the cache is left with only one method stored
        Assert.assertFalse(cache.isEmpty());
        Assert.assertEquals(1, cache.size());
        Assert.assertEquals(1, cache.getFor(secondMethod.getEventListener()).size());
        Assert.assertTrue(cache.contains(secondMethod.getEventListener(), secondMethod));
    }

    @Test
    public void testContains() {
        EventListenerToMethodsCache cache = new EventListenerToMethodsCache();
        // Populating the cache with all the instantiated methods
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        cache.map(firstNotifiableMethod.getEventListener(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        cache.map(secondNotifiableMethod.getEventListener(), secondNotifiableMethod);
        // Don't add the third method to the cache
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        // Ensure the first two (except the third) methods are present in the cache
        Assert.assertTrue(cache.contains(firstNotifiableMethod.getEventListener(), firstNotifiableMethod));
        Assert.assertTrue(cache.contains(secondNotifiableMethod.getEventListener(), secondNotifiableMethod));
        Assert.assertFalse(cache.contains(thirdNotifiableMethod.getEventListener(), thirdNotifiableMethod));
    }

    @Test
    public void testGetFor() {
        EventListenerToMethodsCache cache = new EventListenerToMethodsCache();
        // Populating the cache with all the instantiated methods (the first two with same listener instance)
        EventListener eventListener = Mockito.mock(EventListener.class);
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMockWith(eventListener);
        cache.map(firstNotifiableMethod.getEventListener(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMockWith(eventListener);
        cache.map(secondNotifiableMethod.getEventListener(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        cache.map(thirdNotifiableMethod.getEventListener(), thirdNotifiableMethod);
        // Ensure the two first added methods to the cache are present in the obtained list
        Set<NotifiableEventHandlerMethod> notifiableEventHandlerMethods = cache.getFor(eventListener);
        Assert.assertEquals(2, notifiableEventHandlerMethods.size());
        Assert.assertTrue(notifiableEventHandlerMethods.contains(firstNotifiableMethod));
        Assert.assertTrue(notifiableEventHandlerMethods.contains(secondNotifiableMethod));
        Assert.assertFalse(notifiableEventHandlerMethods.contains(thirdNotifiableMethod));
    }

    @Test
    public void testGetAll() {
        EventListenerToMethodsCache cache = new EventListenerToMethodsCache();
        // Populating the cache with all the instantiated methods (each with different listener instance)
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        cache.map(firstNotifiableMethod.getEventListener(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        cache.map(secondNotifiableMethod.getEventListener(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        cache.map(thirdNotifiableMethod.getEventListener(), thirdNotifiableMethod);
        // Ensure all the added methods to the cache are present in the obtained map
        Map<EventListener, Set<NotifiableEventHandlerMethod>> notifiableEventHandlerMethods = cache.getAll();
        Assert.assertTrue(notifiableEventHandlerMethods.get(firstNotifiableMethod.getEventListener()).contains(firstNotifiableMethod));
        Assert.assertTrue(notifiableEventHandlerMethods.get(secondNotifiableMethod.getEventListener()).contains(secondNotifiableMethod));
        Assert.assertTrue(notifiableEventHandlerMethods.get(thirdNotifiableMethod.getEventListener()).contains(thirdNotifiableMethod));
    }

    @Test
    public void testSizeFor() {
        EventListenerToMethodsCache cache = new EventListenerToMethodsCache();
        // Populating the cache with all the instantiated methods (the first two with same listener instance)
        EventListener eventListener = Mockito.mock(EventListener.class);
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMockWith(eventListener);
        cache.map(firstNotifiableMethod.getEventListener(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMockWith(eventListener);
        cache.map(secondNotifiableMethod.getEventListener(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        cache.map(thirdNotifiableMethod.getEventListener(), thirdNotifiableMethod);
        // Ensure the size for each of the two different listeners is correspondent to the amount of registered methods
        Assert.assertEquals(2, cache.sizeFor(eventListener));
        Assert.assertEquals(1, cache.sizeFor(thirdNotifiableMethod.getEventListener()));
    }

    @Test
    public void testSize() {
        EventListenerToMethodsCache cache = new EventListenerToMethodsCache();
        // Populating the cache with all the instantiated methods (each with different listener instance)
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        cache.map(firstNotifiableMethod.getEventListener(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        cache.map(secondNotifiableMethod.getEventListener(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        cache.map(thirdNotifiableMethod.getEventListener(), thirdNotifiableMethod);
        // Ensure the size is correspondent to the amount of registered methods
        Assert.assertEquals(3, cache.size());
    }

    @Test
    public void testIsEmpty() {
        EventListenerToMethodsCache cache = new EventListenerToMethodsCache();
        // Ensuring at first that the cache is empty
        Assert.assertTrue(cache.isEmpty());
        // Populating the cache with all the instantiated methods (each with different listener instance)
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        cache.map(firstNotifiableMethod.getEventListener(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        cache.map(secondNotifiableMethod.getEventListener(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        cache.map(thirdNotifiableMethod.getEventListener(), thirdNotifiableMethod);
        // Remove all the registered methods
        cache.unmap(firstNotifiableMethod.getEventListener(), firstNotifiableMethod);
        cache.unmap(secondNotifiableMethod.getEventListener(), secondNotifiableMethod);
        cache.unmap(thirdNotifiableMethod.getEventListener(), thirdNotifiableMethod);
        // Ensure that the cache is empty now
        Assert.assertTrue(cache.isEmpty());
    }

    @Test
    public void testClear() {
        EventListenerToMethodsCache cache = new EventListenerToMethodsCache();
        // Populating the cache with all the instantiated methods (each with different listener instance)
        NotifiableEventHandlerMethod firstNotifiableMethod = createMethodMock();
        cache.map(firstNotifiableMethod.getEventListener(), firstNotifiableMethod);
        NotifiableEventHandlerMethod secondNotifiableMethod = createMethodMock();
        cache.map(secondNotifiableMethod.getEventListener(), secondNotifiableMethod);
        NotifiableEventHandlerMethod thirdNotifiableMethod = createMethodMock();
        cache.map(thirdNotifiableMethod.getEventListener(), thirdNotifiableMethod);
        // Clear the cache and after that ensure its size is equal to zero (the cache is empty)
        cache.clear();
        Assert.assertEquals(0, cache.size());
        Assert.assertEquals(0, cache.sizeFor(firstNotifiableMethod.getEventListener()));
        Assert.assertEquals(0, cache.sizeFor(secondNotifiableMethod.getEventListener()));
        Assert.assertEquals(0, cache.sizeFor(thirdNotifiableMethod.getEventListener()));
        Assert.assertTrue(cache.isEmpty());
    }

    private static NotifiableEventHandlerMethod createMethodMock() {
        NotifiableEventHandlerMethod notifiableEventHandlerMethod = Mockito.mock(NotifiableEventHandlerMethod.class);
        Mockito.when(notifiableEventHandlerMethod.getEventListener()).thenReturn(Mockito.mock(EventListener.class));
        return notifiableEventHandlerMethod;
    }

    private static NotifiableEventHandlerMethod createMethodMockWith(EventListener eventListener) {
        NotifiableEventHandlerMethod notifiableEventHandlerMethod = Mockito.mock(NotifiableEventHandlerMethod.class);
        Mockito.when(notifiableEventHandlerMethod.getEventListener()).thenReturn(eventListener);
        return notifiableEventHandlerMethod;
    }

}
