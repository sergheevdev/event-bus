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

package dev.sergheev.eventbus.collections;

import java.util.*;

/**
 * <p>
 * This object is similar to an {@link PriorityQueue}, but instead of being backed by
 * a priority heap, it is based on a <b>doubly linked list</b>.
 *
 * <p>
 * By using this overlying structure, we can ensure that the order of elements will
 * remain constant over time (i.e., when iterating, the elements are kept ordered by
 * their priority).
 *
 * <p>
 * Even though we decrease insertion performance from O(log(n)) to O(n), when using
 * this implementation (compared to its heap equivalent), <b>it's not that important</b>,
 * because the frequency of iterating over all the elements is higher than inserting.
 *
 * <p>
 * The other alternative would have been to use the default {@link PriorityQueue} which
 * is based on a <b>heap structure</b>, so each time we need to iterate, we clone the
 * queue (which is an time & space complexity disadvantage) and then pop the elements in
 * that queue until it is empty.
 *
 * @param <E> the type of elements (which this queue can store)
 *
 * @see <a href="https://en.wikipedia.org/wiki/Doubly_linked_list">Base Structure</a>
 */
public class DoublyLinkedPriorityQueue<E extends Comparable<E>> extends AbstractQueue<E> {

    /**
     * A pointer to the first element in the queue.
     */
    protected Node head;

    /**
     * The current amount of elements in the queue.
     */
    protected int size;

    /**
     * Used to detect concurrent modifications.
     */
    protected transient int mutations;

    public DoublyLinkedPriorityQueue() {
        this.head = null;
        this.size = 0;
        this.mutations = 0;
    }

    public DoublyLinkedPriorityQueue(Collection<? extends E> items) {
        this();
        items.forEach(this::offer);
    }

    @Override
    public boolean offer(E element) {
        if (head == null) {
            head = new Node(element);
            size++;
            mutations++;
            return true;
        }
        Node node = new Node(element);
        Node current = head;
        Node parent = null;
        while (current != null && element.compareTo(current.value) > 0) {
            parent = current;
            current = current.next;
        }
        if (parent == null) {
            node.next = head;
            head.previous = node;
            head = node;
        } else if (current == null) {
            parent.next = node;
            node.previous = parent;
        } else {
            parent.next = node;
            node.previous = parent;
            node.next = current;
            current.previous = node;
        }
        size++;
        mutations++;
        return true;
    }

    @Override
    public E poll() {
        if (head != null) {
            E value = head.value;
            head = head.next;
            if (head != null) {
                head.previous = null;
            }
            size--;
            mutations++;
            return value;
        }
        return null;
    }

    @Override
    public E peek() {
        if (head == null) {
            return null;
        }
        return head.value;
    }

    @Override
    public boolean remove(Object object) {
        if (head == null) {
            return false;
        }
        if (object.equals(head.value)) {
            if (head.next != null) {
                head.next.previous = null;
            }
            head = head.next;
            size--;
            mutations++;
            return true;
        }
        Node current = head;
        while (current != null && !object.equals(current.value)) {
            current = current.next;
        }
        if (current != null) {
            current.previous.next = current.next;
            if (current.next != null) {
                current.next.previous = current.previous;
            }
            size--;
            mutations++;
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(Object object) {
        boolean found = false;
        Node current = head;
        while (current != null && !found) {
            found = object.equals(current.value);
            current = current.next;
        }
        return found;
    }

    @Override
    public void clear() {
        Node current = head;
        while (current != null) {
            Node toDelete = current;
            current = current.next;
            toDelete.next = null;
            toDelete.previous = null;
            toDelete.value = null;
            mutations++;
        }
        head = null;
        size = 0;
    }


    @Override
    public Iterator<E> iterator() {
        return new ConcreteIterator();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", getClass().getSimpleName() + "[ " , " ]");
        Node current = head;
        while (current != null) {
            joiner.add(current.toString());
            current = current.next;
        }
        return joiner.toString();
    }

    private class Node {

        private E value;
        private Node previous;
        private Node next;

        public Node(E value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

    }

    private final class ConcreteIterator implements Iterator<E> {

        private Node current;
        private Node parent;

        transient int expectedMutations;

        public ConcreteIterator() {
            this.expectedMutations = mutations;
            this.current = head;
        }

        public boolean hasNext() {
            return current != null;
        }

        public E next() {
            if (expectedMutations != mutations) {
                throw new ConcurrentModificationException();
            }
            if (current != null) {
                E value = current.value;
                parent = current;
                current = current.next;
                return value;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (expectedMutations != mutations) {
                throw new ConcurrentModificationException();
            }
            if (parent == null) {
                throw new NoSuchElementException();
            } else {
                if (parent.previous != null) {
                    parent.previous.next = parent.next;
                } else {
                    head = current;
                    head.previous = null;
                }
                if(parent.next != null) {
                    parent.next.previous = parent.previous;
                }
            }
            size--;
            mutations++;
            expectedMutations = mutations;
        }

    }

}
