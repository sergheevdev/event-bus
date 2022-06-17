package dev.sergheev.eventbus.event.structure;

import dev.sergheev.eventbus.EventListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class EventListenerCacheTest {

    @Test
    public void testRegister() {
        EventListenerCache registry = new EventListenerCache();
        // When adding the first listener to the registry then method returns `true` and we ensure it was added
        EventListener firstListener = new ConcreteEventListener();
        Assert.assertTrue(registry.register(firstListener));
        Assert.assertTrue(registry.contains(firstListener));
        Assert.assertFalse(registry.isEmpty());
        Assert.assertEquals(1, registry.size());
        // When attempting to add an already-present listener then the method returns `false` and we ensure it's still present
        Assert.assertFalse(registry.register(firstListener));
        Assert.assertTrue(registry.contains(firstListener));
        Assert.assertFalse(registry.isEmpty());
        Assert.assertEquals(1, registry.size());
        // When adding the second listener to the registry then method returns `true` and we ensure it was added
        EventListener secondListener = new ConcreteEventListener();
        Assert.assertTrue(registry.register(secondListener));
        Assert.assertTrue(registry.contains(secondListener));
        Assert.assertFalse(registry.isEmpty());
        Assert.assertEquals(2, registry.size());
    }

    @Test
    public void testUnregister() {
        EventListenerCache registry = new EventListenerCache();
        // Populating the registry with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        registry.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        registry.register(secondListener);
        // Removing twice the same listener returns first `true`, but the second time `false`
        Assert.assertTrue(registry.unregister(firstListener));
        Assert.assertFalse(registry.unregister(firstListener));
        // Ensure the registry is left with only one listener stored
        Assert.assertFalse(registry.isEmpty());
        Assert.assertEquals(1, registry.size());
        Assert.assertTrue(registry.contains(secondListener));
    }

    @Test
    public void testContains() {
        EventListenerCache registry = new EventListenerCache();
        // Populating the registry with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        registry.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        registry.register(secondListener);
        // Don't add the third listener to the registry
        EventListener thirdListener = new ConcreteEventListener();
        // Ensure the first two (except the third) listeners are present in the registry
        Assert.assertTrue(registry.contains(firstListener));
        Assert.assertTrue(registry.contains(secondListener));
        Assert.assertFalse(registry.contains(thirdListener));
    }

    @Test
    public void testGetAll() {
        EventListenerCache registry = new EventListenerCache();
        // Populating the registry with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        registry.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        registry.register(secondListener);
        EventListener thirdListener = new ConcreteEventListener();
        registry.register(thirdListener);
        // Ensure all the added listeners to the registry are present in the obtained set
        Set<EventListener> listeners = registry.getAll();
        Assert.assertTrue(listeners.contains(firstListener));
        Assert.assertTrue(listeners.contains(secondListener));
        Assert.assertTrue(listeners.contains(thirdListener));
    }

    @Test
    public void testSize() {
        EventListenerCache registry = new EventListenerCache();
        // Populating the registry with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        registry.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        registry.register(secondListener);
        EventListener thirdListener = new ConcreteEventListener();
        registry.register(thirdListener);
        // Ensure the size is correspondent to the amount of registered listeners
        Assert.assertEquals(3, registry.size());
    }

    @Test
    public void testIsEmpty() {
        EventListenerCache registry = new EventListenerCache();
        // Populating the registry with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        registry.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        registry.register(secondListener);
        EventListener thirdListener = new ConcreteEventListener();
        registry.register(thirdListener);
        // Remove all the registered listeners
        registry.unregister(firstListener);
        registry.unregister(secondListener);
        registry.unregister(thirdListener);
        // Ensure that the registry is empty now
        Assert.assertTrue(registry.isEmpty());
    }

    @Test
    public void testClear() {
        EventListenerCache registry = new EventListenerCache();
        // Populating the registry with all the instantiated listeners
        EventListener firstListener = new ConcreteEventListener();
        registry.register(firstListener);
        EventListener secondListener = new ConcreteEventListener();
        registry.register(secondListener);
        EventListener thirdListener = new ConcreteEventListener();
        registry.register(thirdListener);
        // Clear the registry and after that ensure its size is equal to zero (the registry is empty)
        registry.clear();
        Assert.assertEquals(0, registry.size());
        Assert.assertFalse(registry.contains(firstListener));
        Assert.assertFalse(registry.contains(secondListener));
        Assert.assertFalse(registry.contains(thirdListener));
    }

    private static class ConcreteEventListener implements EventListener {
    }

}
