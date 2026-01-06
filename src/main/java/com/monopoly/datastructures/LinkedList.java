package com.monopoly.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom Singly Linked List implementation.
 * Used as a helper structure for other data structures.
 * 
 * @param <T> The type of elements stored in the list
 */
public class LinkedList<T> implements Iterable<T> {

    /**
     * Node class for the linked list.
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
     * Creates an empty linked list.
     */
    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Adds an element at the beginning of the list.
     * @param element the element to add
     */
    public void addFirst(T element) {
        Node<T> newNode = new Node<>(element);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head = newNode;
        }
        size++;
    }

    /**
     * Adds an element at the end of the list.
     * @param element the element to add
     */
    public void addLast(T element) {
        Node<T> newNode = new Node<>(element);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }
    
    /**
     * Adds an element at the end of the list.
     * Convenience method equivalent to addLast.
     * @param element the element to add
     * @return true (as per Collection contract)
     */
    public boolean add(T element) {
        addLast(element);
        return true;
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
        if (index == 0) {
            addFirst(element);
        } else if (index == size) {
            addLast(element);
        } else {
            Node<T> newNode = new Node<>(element);
            Node<T> prev = getNode(index - 1);
            newNode.next = prev.next;
            prev.next = newNode;
            size++;
        }
    }

    /**
     * Removes and returns the first element.
     * @return the removed element
     * @throws NoSuchElementException if list is empty
     */
    public T removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        T data = head.data;
        head = head.next;
        size--;
        if (isEmpty()) {
            tail = null;
        }
        return data;
    }

    /**
     * Removes and returns the last element.
     * @return the removed element
     * @throws NoSuchElementException if list is empty
     */
    public T removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        T data = tail.data;
        if (size == 1) {
            head = tail = null;
        } else {
            Node<T> prev = getNode(size - 2);
            prev.next = null;
            tail = prev;
        }
        size--;
        return data;
    }

    /**
     * Removes the element at the specified index.
     * @param index the index of element to remove
     * @return the removed element
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == 0) {
            return removeFirst();
        } else if (index == size - 1) {
            return removeLast();
        } else {
            Node<T> prev = getNode(index - 1);
            T data = prev.next.data;
            prev.next = prev.next.next;
            size--;
            return data;
        }
    }

    /**
     * Removes the first occurrence of the specified element.
     * @param element the element to remove
     * @return true if element was found and removed
     */
    public boolean removeElement(T element) {
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
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return getNode(index).data;
    }

    /**
     * Returns the first element without removing it.
     * @return the first element
     * @throws NoSuchElementException if list is empty
     */
    public T getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        return head.data;
    }

    /**
     * Returns the last element without removing it.
     * @return the last element
     * @throws NoSuchElementException if list is empty
     */
    public T getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        return tail.data;
    }

    /**
     * Sets the element at the specified index.
     * @param index the index of the element to set
     * @param element the new element
     * @return the previous element at that index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public T set(int index, T element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> node = getNode(index);
        T oldData = node.data;
        node.data = element;
        return oldData;
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
        Node<T> current = head;
        int index = 0;
        while (current != null) {
            if ((element == null && current.data == null) ||
                (element != null && element.equals(current.data))) {
                return index;
            }
            current = current.next;
            index++;
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
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Converts the list to an array.
     * @return an array containing all elements
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] array = new Object[size];
        Node<T> current = head;
        int index = 0;
        while (current != null) {
            array[index++] = current.data;
            current = current.next;
        }
        return (T[]) array;
    }

    /**
     * Returns an iterator over the elements in this list.
     * @return an Iterator
     */
    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    /**
     * Helper method to get the node at specified index.
     */
    private Node<T> getNode(int index) {
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    /**
     * Iterator implementation for LinkedList.
     */
    private class LinkedListIterator implements Iterator<T> {
        private Node<T> current = head;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T data = current.data;
            current = current.next;
            return data;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> current = head;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
