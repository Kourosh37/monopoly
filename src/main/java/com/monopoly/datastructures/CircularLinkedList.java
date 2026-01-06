package com.monopoly.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom Circular Linked List implementation for the Monopoly Board.
 * Each node represents a Tile on the board.
 * Supports traversal in a loop (moving N steps from any position).
 * 
 * @param <T> The type of elements stored in the list
 */
public class CircularLinkedList<T> implements Iterable<T> {

    /**
     * Node class for the circular linked list.
     */
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /**
     * Creates an empty circular linked list.
     */
    public CircularLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Adds an element to the end of the circular list.
     * @param element the element to add
     */
    public void add(T element) {
        Node<T> newNode = new Node<>(element);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
            tail.next = head; // Make it circular
        } else {
            tail.next = newNode;
            tail = newNode;
            tail.next = head; // Maintain circular property
        }
        size++;
    }

    /**
     * Adds an element at the specified index.
     * @param index the index at which to insert
     * @param element the element to add
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public void add(int index, T element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        if (index == size) {
            add(element);
            return;
        }
        
        Node<T> newNode = new Node<>(element);
        
        if (index == 0) {
            if (isEmpty()) {
                head = newNode;
                tail = newNode;
                tail.next = head;
            } else {
                newNode.next = head;
                head = newNode;
                tail.next = head; // Update circular link
            }
            size++;
        } else {
            Node<T> prev = getNode(index - 1);
            newNode.next = prev.next;
            prev.next = newNode;
            size++;
        }
    }

    /**
     * Returns the element at the specified index.
     * @param index the index of the element (can be >= size for circular access)
     * @return the element at the normalized index
     * @throws IllegalStateException if list is empty
     */
    public T get(int index) {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        // Normalize index for circular access
        int normalizedIndex = ((index % size) + size) % size;
        return getNode(normalizedIndex).data;
    }

    /**
     * Sets the element at the specified index.
     * @param index the index of the element
     * @param element the new element
     * @return the previous element
     */
    public T set(int index, T element) {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        int normalizedIndex = ((index % size) + size) % size;
        Node<T> node = getNode(normalizedIndex);
        T oldData = node.data;
        node.data = element;
        return oldData;
    }

    /**
     * Removes the element at the specified index.
     * @param index the index of element to remove
     * @return the removed element
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public T remove(int index) {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        T removedData;
        
        if (size == 1) {
            removedData = head.data;
            head = null;
            tail = null;
        } else if (index == 0) {
            removedData = head.data;
            head = head.next;
            tail.next = head;
        } else {
            Node<T> prev = getNode(index - 1);
            removedData = prev.next.data;
            if (prev.next == tail) {
                tail = prev;
            }
            prev.next = prev.next.next;
        }
        
        size--;
        return removedData;
    }

    /**
     * Calculates the new position after moving N steps from current position.
     * This is the key method for board traversal in Monopoly.
     * 
     * @param currentIndex current position on the board
     * @param steps number of steps to move (can be negative)
     * @return the new position index
     */
    public int moveSteps(int currentIndex, int steps) {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        return ((currentIndex + steps) % size + size) % size;
    }

    /**
     * Gets the element after moving N steps from current index.
     * 
     * @param currentIndex starting position
     * @param steps number of steps to move
     * @return the element at the new position
     */
    public T getAfterSteps(int currentIndex, int steps) {
        return get(moveSteps(currentIndex, steps));
    }

    /**
     * Returns the node at the specified index.
     * @param index the index of the node
     * @return the node at the index
     */
    private Node<T> getNode(int index) {
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    /**
     * Returns the distance from one index to another (going forward).
     * 
     * @param from starting position
     * @param to destination position
     * @return number of steps from 'from' to 'to'
     */
    public int getDistance(int from, int to) {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        from = ((from % size) + size) % size;
        to = ((to % size) + size) % size;
        
        if (to >= from) {
            return to - from;
        } else {
            return size - from + to;
        }
    }

    /**
     * Checks if the specified index would pass through a target index
     * when moving from start with given steps.
     * 
     * @param startIndex starting position
     * @param steps number of steps
     * @param targetIndex position to check for passing
     * @return true if targetIndex is passed during the move
     */
    public boolean passesThroughIndex(int startIndex, int steps, int targetIndex) {
        if (steps <= 0) return false;
        
        startIndex = ((startIndex % size) + size) % size;
        targetIndex = ((targetIndex % size) + size) % size;
        
        int endIndex = moveSteps(startIndex, steps);
        
        if (startIndex < endIndex) {
            return targetIndex > startIndex && targetIndex <= endIndex;
        } else {
            // Wrapped around
            return targetIndex > startIndex || targetIndex <= endIndex;
        }
    }

    /**
     * Returns the number of elements in the list.
     * @return the size of the list
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the list is empty.
     * @return true if list has no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all elements from the list.
     */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Checks if the list contains the specified element.
     * @param element the element to search for
     * @return true if element exists in the list
     */
    public boolean contains(T element) {
        return indexOf(element) != -1;
    }

    /**
     * Returns the index of the first occurrence of the element.
     * @param element the element to search for
     * @return the index of the element, or -1 if not found
     */
    public int indexOf(T element) {
        if (isEmpty()) return -1;
        
        Node<T> current = head;
        for (int i = 0; i < size; i++) {
            if ((element == null && current.data == null) ||
                (element != null && element.equals(current.data))) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    /**
     * Converts the list to an array.
     * @return an array containing all elements
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] array = new Object[size];
        Node<T> current = head;
        for (int i = 0; i < size; i++) {
            array[i] = current.data;
            current = current.next;
        }
        return (T[]) array;
    }

    /**
     * Returns an iterator over the elements in this list.
     * Iterates exactly 'size' elements (one full loop).
     * @return an Iterator
     */
    @Override
    public Iterator<T> iterator() {
        return new CircularLinkedListIterator();
    }

    /**
     * Iterator implementation for CircularLinkedList.
     * Iterates through all elements exactly once.
     */
    private class CircularLinkedListIterator implements Iterator<T> {
        private Node<T> current = head;
        private int count = 0;

        @Override
        public boolean hasNext() {
            return count < size;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T data = current.data;
            current = current.next;
            count++;
            return data;
        }
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        Node<T> current = head;
        for (int i = 0; i < size; i++) {
            sb.append(current.data);
            if (i < size - 1) {
                sb.append(" -> ");
            }
            current = current.next;
        }
        sb.append(" -> (back to start)]");
        return sb.toString();
    }
}
