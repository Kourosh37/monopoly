package com.monopoly.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Custom Queue (FIFO) implementation for Chance and Community Chest card decks.
 * Drawn cards are executed and returned to the back of the queue.
 * 
 * @param <T> The type of elements stored in the queue
 */
public class Queue<T> implements Iterable<T> {

    /**
     * Node class for the queue.
     */
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> front;
    private Node<T> rear;
    private int size;

    /**
     * Creates an empty queue.
     */
    public Queue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    /**
     * Adds an element to the back of the queue.
     * @param element the element to add
     */
    public void enqueue(T element) {
        Node<T> newNode = new Node<>(element);
        if (isEmpty()) {
            front = rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    /**
     * Removes and returns the element at the front of the queue.
     * @return the removed element
     * @throws NoSuchElementException if queue is empty
     */
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        T data = front.data;
        front = front.next;
        size--;
        if (isEmpty()) {
            rear = null;
        }
        return data;
    }

    /**
     * Returns the element at the front without removing it.
     * @return the front element
     * @throws NoSuchElementException if queue is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return front.data;
    }

    /**
     * Returns the element at the back without removing it.
     * @return the rear element
     * @throws NoSuchElementException if queue is empty
     */
    public T peekRear() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return rear.data;
    }

    /**
     * Draws a card: removes from front, executes, and returns to back.
     * This is the primary operation for card decks in Monopoly.
     * @return the drawn card
     * @throws NoSuchElementException if queue is empty
     */
    public T draw() {
        T card = dequeue();
        enqueue(card);
        return card;
    }

    /**
     * Draws a card but doesn't return it to the deck.
     * Used for "Get Out of Jail Free" cards that players keep.
     * @return the drawn card
     * @throws NoSuchElementException if queue is empty
     */
    public T drawAndKeep() {
        return dequeue();
    }

    /**
     * Returns a kept card back to the deck (bottom).
     * Used when a player uses their "Get Out of Jail Free" card.
     * @param card the card to return
     */
    public void returnCard(T card) {
        enqueue(card);
    }

    /**
     * Shuffles the queue using Fisher-Yates algorithm.
     * Used for initial deck setup.
     */
    @SuppressWarnings("unchecked")
    public void shuffle() {
        if (size <= 1) return;

        // Convert to array
        Object[] array = new Object[size];
        Node<T> current = front;
        for (int i = 0; i < size; i++) {
            array[i] = current.data;
            current = current.next;
        }

        // Fisher-Yates shuffle
        Random random = new Random();
        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Object temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

        // Rebuild queue
        clear();
        for (Object element : array) {
            enqueue((T) element);
        }
    }

    /**
     * Shuffles the queue with a specific seed (for reproducibility).
     * @param seed the random seed
     */
    @SuppressWarnings("unchecked")
    public void shuffle(long seed) {
        if (size <= 1) return;

        Object[] array = new Object[size];
        Node<T> current = front;
        for (int i = 0; i < size; i++) {
            array[i] = current.data;
            current = current.next;
        }

        Random random = new Random(seed);
        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Object temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

        clear();
        for (Object element : array) {
            enqueue((T) element);
        }
    }

    /**
     * Returns the number of elements in the queue.
     * @return the size of the queue
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the queue is empty.
     * @return true if queue has no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all elements from the queue.
     */
    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    /**
     * Checks if the queue contains the specified element.
     * @param element the element to search for
     * @return true if element exists in the queue
     */
    public boolean contains(T element) {
        Node<T> current = front;
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
     * Converts the queue to an array.
     * @return an array containing all elements (front to rear)
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] array = new Object[size];
        Node<T> current = front;
        for (int i = 0; i < size; i++) {
            array[i] = current.data;
            current = current.next;
        }
        return (T[]) array;
    }

    /**
     * Returns an iterator over the elements in this queue.
     * @return an Iterator
     */
    @Override
    public Iterator<T> iterator() {
        return new QueueIterator();
    }

    /**
     * Iterator implementation for Queue.
     */
    private class QueueIterator implements Iterator<T> {
        private Node<T> current = front;

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
        StringBuilder sb = new StringBuilder("Queue[front -> ");
        Node<T> current = front;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append(" <- rear]");
        return sb.toString();
    }
}
