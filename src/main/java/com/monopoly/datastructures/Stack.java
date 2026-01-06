package com.monopoly.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom Stack (LIFO) implementation for Undo/Redo functionality.
 * Two separate stacks are needed: UndoStack and RedoStack.
 * 
 * @param <T> The type of elements stored in the stack
 */
public class Stack<T> implements Iterable<T> {

    /**
     * Node class for the stack.
     */
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> top;
    private int size;
    private int maxSize; // Optional capacity limit for undo history

    /**
     * Creates an empty stack with unlimited capacity.
     */
    public Stack() {
        this.top = null;
        this.size = 0;
        this.maxSize = Integer.MAX_VALUE;
    }

    /**
     * Creates an empty stack with a maximum capacity.
     * Useful for limiting undo history size.
     * @param maxSize the maximum number of elements
     */
    public Stack(int maxSize) {
        this.top = null;
        this.size = 0;
        this.maxSize = maxSize > 0 ? maxSize : Integer.MAX_VALUE;
    }

    /**
     * Pushes an element onto the top of the stack.
     * If stack is at max capacity, removes bottom element.
     * @param element the element to push
     */
    public void push(T element) {
        Node<T> newNode = new Node<>(element);
        newNode.next = top;
        top = newNode;
        size++;

        // If we exceed max size, remove the oldest (bottom) element
        if (size > maxSize) {
            removeBottom();
        }
    }

    /**
     * Removes and returns the element at the top of the stack.
     * @return the top element
     * @throws NoSuchElementException if stack is empty
     */
    public T pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    /**
     * Returns the element at the top without removing it.
     * @return the top element
     * @throws NoSuchElementException if stack is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return top.data;
    }

    /**
     * Returns the element at a specific depth from top.
     * Index 0 is the top element.
     * @param index the depth from top
     * @return the element at the specified depth
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public T peek(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> current = top;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    /**
     * Helper method to remove the bottom element when stack exceeds max size.
     */
    private void removeBottom() {
        if (size <= 1) {
            clear();
            return;
        }
        
        Node<T> current = top;
        while (current.next != null && current.next.next != null) {
            current = current.next;
        }
        current.next = null;
        size--;
    }

    /**
     * Returns the number of elements in the stack.
     * @return the size of the stack
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the stack is empty.
     * @return true if stack has no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all elements from the stack.
     */
    public void clear() {
        top = null;
        size = 0;
    }

    /**
     * Checks if the stack contains the specified element.
     * @param element the element to search for
     * @return true if element exists in the stack
     */
    public boolean contains(T element) {
        Node<T> current = top;
        while (current != null) {
            if ((element == null && current.data == null) ||
                (element != null && element.equals(current.data))) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Searches for an element and returns its position from top.
     * @param element the element to search for
     * @return the 1-based position from top, or -1 if not found
     */
    public int search(T element) {
        Node<T> current = top;
        int position = 1;
        while (current != null) {
            if ((element == null && current.data == null) ||
                (element != null && element.equals(current.data))) {
                return position;
            }
            current = current.next;
            position++;
        }
        return -1;
    }

    /**
     * Converts the stack to an array (top to bottom order).
     * @return an array containing all elements
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] array = new Object[size];
        Node<T> current = top;
        for (int i = 0; i < size; i++) {
            array[i] = current.data;
            current = current.next;
        }
        return (T[]) array;
    }

    /**
     * Creates a copy of this stack.
     * @return a new Stack with the same elements
     */
    public Stack<T> copy() {
        Stack<T> temp = new Stack<>();
        Stack<T> result = new Stack<>(this.maxSize);
        
        // Push to temp (reverses order)
        Node<T> current = top;
        while (current != null) {
            temp.push(current.data);
            current = current.next;
        }
        
        // Push from temp to result (restores original order)
        while (!temp.isEmpty()) {
            result.push(temp.pop());
        }
        
        return result;
    }

    /**
     * Returns an iterator over the elements in this stack (top to bottom).
     * @return an Iterator
     */
    @Override
    public Iterator<T> iterator() {
        return new StackIterator();
    }

    /**
     * Iterator implementation for Stack.
     */
    private class StackIterator implements Iterator<T> {
        private Node<T> current = top;

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
        StringBuilder sb = new StringBuilder("Stack[top -> ");
        Node<T> current = top;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append(" <- bottom]");
        return sb.toString();
    }
}
