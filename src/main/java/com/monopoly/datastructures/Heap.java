package com.monopoly.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom Heap (Priority Queue) implementation for Top-K Reports.
 * Examples: Top 3 richest players, Top 3 by rent collected.
 * Can be configured as Min-Heap or Max-Heap.
 * 
 * @param <T> The type of elements stored (must be Comparable)
 */
public class Heap<T extends Comparable<T>> implements Iterable<T> {

    private static final int DEFAULT_CAPACITY = 16;

    private Object[] array;
    private int size;
    private int capacity;
    private boolean isMaxHeap;

    /**
     * Creates a max-heap with default capacity.
     */
    public Heap() {
        this(true, DEFAULT_CAPACITY);
    }

    /**
     * Creates a heap with specified type.
     * @param isMaxHeap true for max-heap, false for min-heap
     */
    public Heap(boolean isMaxHeap) {
        this(isMaxHeap, DEFAULT_CAPACITY);
    }

    /**
     * Creates a heap with specified type and initial capacity.
     * @param isMaxHeap true for max-heap, false for min-heap
     * @param initialCapacity the initial array capacity
     */
    public Heap(boolean isMaxHeap, int initialCapacity) {
        this.isMaxHeap = isMaxHeap;
        this.capacity = initialCapacity > 0 ? initialCapacity : DEFAULT_CAPACITY;
        this.array = new Object[capacity];
        this.size = 0;
    }

    /**
     * Creates a heap from an array.
     * @param elements the elements to add
     * @param isMaxHeap true for max-heap, false for min-heap
     */
    public Heap(T[] elements, boolean isMaxHeap) {
        this.isMaxHeap = isMaxHeap;
        this.capacity = elements.length > 0 ? elements.length * 2 : DEFAULT_CAPACITY;
        this.array = new Object[capacity];
        this.size = 0;
        for (T element : elements) {
            insert(element);
        }
    }

    /**
     * Inserts an element into the heap.
     * @param element the element to insert
     * @throws IllegalArgumentException if element is null
     */
    public void insert(T element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot insert null element");
        }
        ensureCapacity();
        array[size] = element;
        heapifyUp(size);
        size++;
    }

    /**
     * Removes and returns the top element (max for max-heap, min for min-heap).
     * @return the top element
     * @throws NoSuchElementException if heap is empty
     */
    public T extractTop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        @SuppressWarnings("unchecked")
        T top = (T) array[0];
        array[0] = array[size - 1];
        array[size - 1] = null;
        size--;
        if (size > 0) {
            heapifyDown(0);
        }
        return top;
    }

    /**
     * Returns the top element without removing it.
     * @return the top element
     * @throws NoSuchElementException if heap is empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return (T) array[0];
    }

    /**
     * Restores heap property by moving an element up.
     * @param index the index to start from
     */
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = getParentIndex(index);
            if (shouldSwap(parentIndex, index)) {
                swap(parentIndex, index);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    /**
     * Restores heap property by moving an element down.
     * @param index the index to start from
     */
    private void heapifyDown(int index) {
        while (true) {
            int leftIndex = getLeftChildIndex(index);
            int rightIndex = getRightChildIndex(index);
            int targetIndex = index;

            if (leftIndex < size && shouldSwap(targetIndex, leftIndex)) {
                targetIndex = leftIndex;
            }
            if (rightIndex < size && shouldSwap(targetIndex, rightIndex)) {
                targetIndex = rightIndex;
            }

            if (targetIndex != index) {
                swap(index, targetIndex);
                index = targetIndex;
            } else {
                break;
            }
        }
    }

    /**
     * Determines if elements should be swapped based on heap type.
     * @param parentIndex the parent index
     * @param childIndex the child index
     * @return true if swap is needed
     */
    @SuppressWarnings("unchecked")
    private boolean shouldSwap(int parentIndex, int childIndex) {
        T parent = (T) array[parentIndex];
        T child = (T) array[childIndex];
        if (isMaxHeap) {
            return child.compareTo(parent) > 0; // Child should be smaller in max-heap
        } else {
            return child.compareTo(parent) < 0; // Child should be larger in min-heap
        }
    }

    /**
     * Swaps two elements in the array.
     */
    private void swap(int i, int j) {
        Object temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    /**
     * Returns the parent index.
     */
    private int getParentIndex(int index) {
        return (index - 1) / 2;
    }

    /**
     * Returns the left child index.
     */
    private int getLeftChildIndex(int index) {
        return 2 * index + 1;
    }

    /**
     * Returns the right child index.
     */
    private int getRightChildIndex(int index) {
        return 2 * index + 2;
    }

    /**
     * Ensures there's capacity for a new element.
     */
    private void ensureCapacity() {
        if (size >= capacity) {
            capacity *= 2;
            Object[] newArray = new Object[capacity];
            System.arraycopy(array, 0, newArray, 0, size);
            array = newArray;
        }
    }

    /**
     * Returns the number of elements in the heap.
     * @return the size
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the heap is empty.
     * @return true if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks if this is a max-heap.
     * @return true if max-heap
     */
    public boolean isMaxHeap() {
        return isMaxHeap;
    }

    /**
     * Clears all elements from the heap.
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            array[i] = null;
        }
        size = 0;
    }

    /**
     * Returns the top K elements without modifying the heap.
     * For max-heap: returns K largest elements (descending order)
     * For min-heap: returns K smallest elements (ascending order)
     * @param k the number of elements to return
     * @return ArrayList of top k elements
     */
    public ArrayList<T> getTopK(int k) {
        if (k <= 0) {
            return new ArrayList<>();
        }
        k = Math.min(k, size);
        
        // Create a copy of the heap
        Heap<T> copy = new Heap<>(isMaxHeap, capacity);
        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked")
            T element = (T) array[i];
            copy.insert(element);
        }
        
        ArrayList<T> result = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            result.add(copy.extractTop());
        }
        return result;
    }

    /**
     * Checks if the heap property is maintained (for testing/debugging).
     * @return true if valid heap
     */
    @SuppressWarnings("unchecked")
    public boolean isValidHeap() {
        for (int i = 0; i < size; i++) {
            int leftIndex = getLeftChildIndex(i);
            int rightIndex = getRightChildIndex(i);
            
            if (leftIndex < size) {
                T parent = (T) array[i];
                T left = (T) array[leftIndex];
                if (isMaxHeap) {
                    if (left.compareTo(parent) > 0) return false;
                } else {
                    if (left.compareTo(parent) < 0) return false;
                }
            }
            
            if (rightIndex < size) {
                T parent = (T) array[i];
                T right = (T) array[rightIndex];
                if (isMaxHeap) {
                    if (right.compareTo(parent) > 0) return false;
                } else {
                    if (right.compareTo(parent) < 0) return false;
                }
            }
        }
        return true;
    }

    /**
     * Converts the heap to an array (in heap order, not sorted).
     * @return array of elements
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] result = new Object[size];
        System.arraycopy(array, 0, result, 0, size);
        return (T[]) result;
    }

    /**
     * Converts the heap to a sorted list.
     * @return ArrayList of elements in sorted order
     */
    public ArrayList<T> toSortedList() {
        return getTopK(size);
    }

    /**
     * Updates an element's value and restores heap property.
     * @param oldValue the current value
     * @param newValue the new value
     * @return true if element was found and updated
     */
    @SuppressWarnings("unchecked")
    public boolean update(T oldValue, T newValue) {
        // Find the element
        int index = -1;
        for (int i = 0; i < size; i++) {
            if (oldValue.equals(array[i])) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            return false;
        }
        
        T current = (T) array[index];
        array[index] = newValue;
        
        // Determine if we need to heapify up or down
        int cmp = newValue.compareTo(current);
        if (isMaxHeap) {
            if (cmp > 0) {
                heapifyUp(index);
            } else {
                heapifyDown(index);
            }
        } else {
            if (cmp < 0) {
                heapifyUp(index);
            } else {
                heapifyDown(index);
            }
        }
        return true;
    }

    /**
     * Removes a specific element from the heap.
     * @param element the element to remove
     * @return true if element was found and removed
     */
    @SuppressWarnings("unchecked")
    public boolean remove(T element) {
        // Find the element
        int index = -1;
        for (int i = 0; i < size; i++) {
            if (element.equals(array[i])) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            return false;
        }
        
        // Replace with last element
        array[index] = array[size - 1];
        array[size - 1] = null;
        size--;
        
        if (index < size) {
            // Try to restore heap property
            heapifyUp(index);
            heapifyDown(index);
        }
        return true;
    }

    /**
     * Checks if the heap contains the specified element.
     * @param element the element to search for
     * @return true if found
     */
    public boolean contains(T element) {
        for (int i = 0; i < size; i++) {
            if (element.equals(array[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an iterator over the elements (in heap order, not sorted).
     * @return an Iterator
     */
    @Override
    public Iterator<T> iterator() {
        return new HeapIterator();
    }

    /**
     * Iterator implementation for Heap.
     */
    private class HeapIterator implements Iterator<T> {
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
            return (T) array[currentIndex++];
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(isMaxHeap ? "MaxHeap[" : "MinHeap[");
        for (int i = 0; i < size; i++) {
            sb.append(array[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
