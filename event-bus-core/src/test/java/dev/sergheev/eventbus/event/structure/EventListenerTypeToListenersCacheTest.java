package dev.sergheev.eventbus.event.structure;

import dev.sergheev.eventbus.EventListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class EventListenerTypeToListenersCacheTest {

    @Test
    public void testAdd() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // When adding the first listener to the cache then method returns `true` and we ensure it was added
        EventListener firstListener = new ConcreteEventListener();
        Assert.assertTrue(cache.register(firstListener));
        Assert.assertTrue(cache.contains(firstListener));
        Assert.assertFalse(cache.isEmpty());
        Assert.assertEquals(1, cache.sizeFor(firstListener.getClass()));
        // When attempting to add an already-present listener then the method returns `false` and we ensure it's still present
        Assert.assertFalse(cache.register(firstListener));
        Assert.assertTrue(cache.contains(firstListener));
        Assert.assertFalse(cache.isEmpty());
        Assert.assertEquals(1, cache.sizeFor(firstListener.getClass()));
        // When adding the second listener to the cache then method returns `true` and we ensure it was added
        EventListener secondListener = new AnotherConcreteEventListener();
        Assert.assertTrue(cache.register(secondListener));
        Assert.assertTrue(cache.contains(secondListener));
        Assert.assertFalse(cache.isEmpty());
        Assert.assertEquals(2, cache.size());
    }

    @Test
    public void testRemove() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        // Removing twice the same listener returns first `true`, but the second time `false`
        Assert.assertTrue(cache.unregister(firstListener));
        Assert.assertFalse(cache.unregister(firstListener));
        // Ensure the cache is left with only one listener stored
        Assert.assertFalse(cache.isEmpty());
        Assert.assertEquals(1, cache.size());
        Assert.assertEquals(1, cache.sizeFor(secondListener.getClass()));
        Assert.assertTrue(cache.contains(secondListener));
    }

    @Test
    public void testRemoveAllFor() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        EventListener thirdListener = new AnotherConcreteEventListener();
        cache.register(thirdListener);
        // Remove all listeners that have a concrete listener type
        cache.unregisterAllFor(ConcreteEventListener.class);
        // Ensure the cache does no longer contain any of the first two listeners
        Assert.assertFalse(cache.contains(firstListener));
        Assert.assertFalse(cache.containsAny(ConcreteEventListener.class));
        Assert.assertEquals(0, cache.sizeFor(ConcreteEventListener.class));
        // Ensure the cache still contains the third listener
        Assert.assertTrue(cache.contains(thirdListener));
        Assert.assertTrue(cache.containsAny(AnotherConcreteEventListener.class));
        Assert.assertEquals(1, cache.sizeFor(AnotherConcreteEventListener.class));
        Assert.assertEquals(1, cache.size());
    }

    @Test
    public void testContains() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        // Don't add the third listener to the cache
        EventListener thirdListener = new AnotherConcreteEventListener();
        // Ensure the first two (except the third) listeners are present in the cache
        Assert.assertTrue(cache.contains(firstListener));
        Assert.assertTrue(cache.contains(secondListener));
        Assert.assertFalse(cache.contains(thirdListener));
    }

    @Test
    public void testContainsAny() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        // Ensure the first two (except the third) listeners are present in the cache
        Assert.assertTrue(cache.containsAny(ConcreteEventListener.class));
        Assert.assertFalse(cache.containsAny(AnotherConcreteEventListener.class));
        cache.unregisterAllFor(ConcreteEventListener.class);
        Assert.assertFalse(cache.containsAny(ConcreteEventListener.class));
    }

    @Test
    public void testGetFor() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        EventListener thirdListener = new AnotherConcreteEventListener();
        cache.register(thirdListener);
        // Ensure the two first added listeners to the cache are present in the obtained set
        Set<EventListener> listeners = cache.getFor(ConcreteEventListener.class);
        Assert.assertEquals(2, listeners.size());
        Assert.assertTrue(listeners.contains(firstListener));
        Assert.assertTrue(listeners.contains(secondListener));
        Assert.assertFalse(listeners.contains(thirdListener));
    }

    @Test
    public void testGetAll() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        EventListener thirdListener = new AnotherConcreteEventListener();
        cache.register(thirdListener);
        // Ensure all the added listeners to the cache are present in the obtained map
        Map<Class<? extends EventListener>, Set<EventListener>> typeToListeners = cache.getAll();
        Assert.assertTrue(typeToListeners.get(ConcreteEventListener.class).contains(firstListener));
        Assert.assertTrue(typeToListeners.get(ConcreteEventListener.class).contains(secondListener));
        Assert.assertTrue(typeToListeners.get(AnotherConcreteEventListener.class).contains(thirdListener));
    }

    @Test
    public void testClearFor() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        EventListener thirdListener = new AnotherConcreteEventListener();
        cache.register(thirdListener);
        // Clear cache for a concrete listener type and after that ensure its size for that type is equal to zero
        cache.clearFor(ConcreteEventListener.class);
        Assert.assertEquals(0, cache.sizeFor(ConcreteEventListener.class));
        Assert.assertFalse(cache.contains(firstListener));
        Assert.assertFalse(cache.contains(secondListener));
        Assert.assertTrue(cache.contains(thirdListener));
    }

    @Test
    public void testClear() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        EventListener thirdListener = new AnotherConcreteEventListener();
        cache.register(thirdListener);
        // Clear the cache and after that ensure its size is equal to zero (the cache is empty)
        cache.clear();
        Assert.assertEquals(0, cache.size());
        Assert.assertEquals(0, cache.sizeFor(ConcreteEventListener.class));
        Assert.assertEquals(0, cache.sizeFor(AnotherConcreteEventListener.class));
        Assert.assertFalse(cache.contains(firstListener));
        Assert.assertFalse(cache.contains(secondListener));
        Assert.assertFalse(cache.contains(thirdListener));
    }

    @Test
    public void testSizeFor() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        EventListener thirdListener = new AnotherConcreteEventListener();
        cache.register(thirdListener);
        // Ensure the size for each of the two different listeners is correspondent to the amount of registered methods
        Assert.assertEquals(2, cache.sizeFor(ConcreteEventListener.class));
        Assert.assertEquals(1, cache.sizeFor(AnotherConcreteEventListener.class));
    }

    @Test
    public void testSize() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        EventListener thirdListener = new AnotherConcreteEventListener();
        cache.register(thirdListener);
        // Ensure the size is correspondent to the amount of registered listeners
        Assert.assertEquals(3, cache.size());
    }

    @Test
    public void testIsEmptyFor() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        EventListener thirdListener = new AnotherConcreteEventListener();
        cache.register(thirdListener);
        // Remove all the registered listeners for a certain type
        cache.unregisterAllFor(ConcreteEventListener.class);
        // Ensure that the cache is empty now for that type
        Assert.assertTrue(cache.isEmptyFor(ConcreteEventListener.class));
    }

    @Test
    public void testIsEmpty() {
        EventListenerTypeToListenersCache cache = new EventListenerTypeToListenersCache();
        // Populating the cache with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        cache.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        cache.register(secondListener);
        EventListener thirdListener = new AnotherConcreteEventListener();
        cache.register(thirdListener);
        // Remove all the registered listeners
        cache.unregister(firstListener);
        cache.unregister(secondListener);
        cache.unregister(thirdListener);
        // Ensure that the cache is empty now
        Assert.assertTrue(cache.isEmpty());
    }

    private static class ConcreteEventListener implements EventListener {
    }

    private static class AnotherConcreteEventListener implements EventListener {
    }

}
