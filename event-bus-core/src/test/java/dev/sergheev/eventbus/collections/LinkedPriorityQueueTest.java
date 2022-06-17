package dev.sergheev.eventbus.collections;

import org.junit.Assert;
import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class LinkedPriorityQueueTest {

    @Test
    public void testOffer() {
        DoublyLinkedPriorityQueue<Integer> queue = new DoublyLinkedPriorityQueue<>();
        // [] -> [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        for(int i = 10; i >= 1; i--) { queue.offer(i); }
        Assert.assertEquals(10, queue.size());
        int counter = 1;
        // Remove all integers by lower priority and ensure they follow the sequence
        Integer next;
        while((next = queue.poll()) != null) {
            Assert.assertEquals(counter++, next.intValue());
        }
        // At this point, ensure the queue is empty
        Assert.assertEquals(0, queue.size());
        Assert.assertTrue(queue.isEmpty());
        // [] -> [0, 1, 2, 3, 4]
        for(int i = 4; i >= 0; i--) { queue.offer(i); }
        Iterator<Integer> iterator = queue.iterator();
        while(iterator.hasNext()) {
            // Get the next element from the queue
            int current = iterator.next();
            // Ignore even numbers
            if(current % 2 == 0) continue;
            // Remove odd numbers
            iterator.remove();
        }
        // [] -> [0, 2, 4]
        Assert.assertEquals(3, queue.size());
        Assert.assertFalse(queue.isEmpty());
        // Populate with more random values the queue
        queue.offer(25);
        queue.offer(-5);
        // At this point the value of the queue: [-5, 0, 2, 4, 25]
        // Ensure the values when extracted follow the comparison order
        Assert.assertEquals(-5, Objects.requireNonNull(queue.poll()).intValue());
        Assert.assertEquals(0, Objects.requireNonNull(queue.poll()).intValue());
        Assert.assertEquals(2, Objects.requireNonNull(queue.poll()).intValue());
        Assert.assertEquals(4, Objects.requireNonNull(queue.poll()).intValue());
        Assert.assertEquals(25, Objects.requireNonNull(queue.poll()).intValue());
        Assert.assertNull(queue.poll());
        // Ensure the queue is empty at the end
        Assert.assertEquals(0, queue.size());
        Assert.assertTrue(queue.isEmpty());
        try {
            queue.iterator().next();
            throw new AssertionError();
        } catch(Exception e) {
            Assert.assertTrue(e instanceof NoSuchElementException);
        }
    }

    @Test
    public void testRemove() {
        DoublyLinkedPriorityQueue<Integer> queue = new DoublyLinkedPriorityQueue<>();
        // [] -> [0, 1, 2, 3, 4, 5, 6]
        for(int i = 6; i >= 0; i--) { queue.offer(i); }
        Iterator<Integer> iterator = queue.iterator();
        while(iterator.hasNext()) {
            // Get the next element from the queue
            int current = iterator.next();
            // Ignore even numbers
            if(current % 2 != 0) continue;
            // Remove odd numbers
            iterator.remove();
        }
        // Provoke a concurrent modification exception (for queue remove method)
        try {
            for(int value : queue) {
                queue.remove(value);
            }
            throw new AssertionError();
        } catch(Exception e) {
            Assert.assertTrue(e instanceof ConcurrentModificationException);
        }
        // Provoke a concurrent modification exception (for iterator remove method)
        try {
            iterator = queue.iterator();
            while(iterator.hasNext()) {
                int current = iterator.next();
                queue.add(current);
                iterator.remove();
            }
            throw new AssertionError();
        } catch(Exception e) {
            Assert.assertTrue(e instanceof ConcurrentModificationException);
        }
        // Provoke a no such element exception (for iterator remove)
        queue.clear();
        try {
            queue.iterator().remove();
            throw new AssertionError();
        } catch(Exception e) {
            Assert.assertTrue(e instanceof NoSuchElementException);
        }
        // Try removing the first element in the iterator (edge case)
        queue.clear();
        for(int i = 6; i >= 0; i--) { queue.offer(i); }
        iterator = queue.iterator();
        iterator.next();
        iterator.remove();
        Assert.assertEquals(1, Objects.requireNonNull(queue.peek()).intValue());
    }

    @Test
    public void testPeek() {
        DoublyLinkedPriorityQueue<Integer> queue = new DoublyLinkedPriorityQueue<>();
        Assert.assertNull(queue.peek());
        // [] -> [0, 1, 2, 3, 4, 5, 6]
        for(int i = 6; i >= 0; i--) { queue.offer(i); }
        Assert.assertFalse(queue.isEmpty());
        int peek = queue.peek();
        Assert.assertEquals(0, peek);
        // Clear the queue
        queue.clear();
        Assert.assertTrue(queue.isEmpty());
        // Attempting to remove an element from empty queue
        Assert.assertFalse(queue.remove(0));
    }

}
