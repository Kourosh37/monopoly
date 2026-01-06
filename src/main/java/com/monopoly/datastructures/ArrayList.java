package com.monopoly.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom Dynamic Array (ArrayList) implementation.
 * Used as a helper structure where dynamic array is needed.
 * 
 * @param <T> The type of elements stored in the array
 */
public class ArrayList<T> implements Iterable<T> {

    private static final int DEFAULT_CAPACITY = 10;
    private Object[] elements;
    private int size;

    /**
     * Creates an ArrayList with default initial capacity.
     */
    public ArrayList() {
        this.elements = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    /**
     * Creates an ArrayList with specified initial capacity.
     * @param initialCapacity the initial capacity
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.elements = new Object[initialCapacity];
        this.size = 0;
    }

    /**
     * Adds an element at the end of the list.
     * @param element the element to add
     */
    public void add(T element) {
        ensureCapacity(size + 1);
        elements[size++] = element;
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
        ensureCapacity(size + 1);
        // Shift elements to the right
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size++;
    }

    /**
     * Removes the element at the specified index.
     * @param index the index of element to remove
     * @return the removed element
     * @throws IndexOutOfBoundsException if index is invalid
     */
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        T oldValue = (T) elements[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        elements[--size] = null; // Help GC
        return oldValue;
    }

    /**
     * Removes the first occurrence of the specified element.
     * @param element the element to remove
     * @return true if element was found and removed
     */
    public boolean remove(T element) {
        int index = indexOf(element);
        if (index == -1) {
            return false;
        }
        remove(index);
        return true;
    }

    /**
     * Returns the element at the specified index.
     * @param index the index of the element
     * @return the element at the index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return (T) elements[index];
    }

    /**
     * Sets the element at the specified index.
     * @param index the index of the element to set
     * @param element the new element
     * @return the previous element at that index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    @SuppressWarnings("unchecked")
    public T set(int index, T element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        T oldValue = (T) elements[index];
        elements[index] = element;
        return oldValue;
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
        for (int i = 0; i < size; i++) {
            if ((element == null && elements[i] == null) ||
                (element != null && element.equals(elements[i]))) {
                return i;
            }
        }
        return -1;
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
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    /**
     * Ensures the capacity is at least the specified minimum.
     * @param minCapacity the minimum capacity needed
     */
    public void ensureCapacity(int minCapacity) {
        if (minCapacity > elements.length) {
            resize(Math.max(elements.length * 2, minCapacity));
        }
    }

    /**
     * Resizes the internal array to the new capacity.
     */
    private void resize(int newCapacity) {
        Object[] newElements = new Object[newCapacity];
        System.arraycopy(elements, 0, newElements, 0, size);
        elements = newElements;
    }

    /**
     * Trims the capacity to the current size.
     */
    public void trimToSize() {
        if (size < elements.length) {
            Object[] newElements = new Object[size];
            System.arraycopy(elements, 0, newElements, 0, size);
            elements = newElements;
        }
    }

    /**
     * Returns the current capacity of the internal array.
     * @return the capacity
     */
    public int capacity() {
        return elements.length;
    }

    /**
     * Converts the list to an array.
     * @return an array containing all elements
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] result = new Object[size];
        System.arraycopy(elements, 0, result, 0, size);
        return (T[]) result;
    }

    /**
     * Returns an iterator over the elements in this list.
     * @return an Iterator
     */
    @Override
    public Iterator<T> iterator() {
        return new ArrayListIterator();
    }

    /**
     * Iterator implementation for ArrayList.
     */
    private class ArrayListIterator implements Iterator<T> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return currentIndex < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return (T) elements[currentIndex++];
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(elements[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
