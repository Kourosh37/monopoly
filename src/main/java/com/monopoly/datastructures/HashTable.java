package com.monopoly.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom Hash Table implementation for O(1) lookup of Players and Properties.
 * Key: ID, Value: Object
 * Uses separate chaining for collision handling.
 * 
 * @param <K> The type of keys
 * @param <V> The type of values
 */
public class HashTable<K, V> implements Iterable<HashTable.Entry<K, V>> {

    /**
     * Entry class for key-value pairs.
     */
    public static class Entry<K, V> {
        private K key;
        private V value;
        Entry<K, V> next; // For chaining

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Entry<K, V>[] buckets;
    private int size;
    private int capacity;
    private float loadFactor;

    /**
     * Creates a hash table with default capacity and load factor.
     */
    @SuppressWarnings("unchecked")
    public HashTable() {
        this.capacity = DEFAULT_CAPACITY;
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    /**
     * Creates a hash table with specified initial capacity.
     * @param initialCapacity the initial capacity
     */
    @SuppressWarnings("unchecked")
    public HashTable(int initialCapacity) {
        this.capacity = initialCapacity > 0 ? initialCapacity : DEFAULT_CAPACITY;
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    /**
     * Creates a hash table with specified capacity and load factor.
     * @param initialCapacity the initial capacity
     * @param loadFactor the load factor threshold for resizing
     */
    @SuppressWarnings("unchecked")
    public HashTable(int initialCapacity, float loadFactor) {
        this.capacity = initialCapacity > 0 ? initialCapacity : DEFAULT_CAPACITY;
        this.loadFactor = loadFactor > 0 ? loadFactor : DEFAULT_LOAD_FACTOR;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    /**
     * Computes hash value for a key.
     * @param key the key
     * @return the bucket index
     */
    private int hash(K key) {
        if (key == null) {
            return 0;
        }
        int hashCode = key.hashCode();
        return Math.abs(hashCode) % capacity;
    }

    /**
     * Associates the specified value with the specified key.
     * If the key already exists, the old value is replaced.
     * @param key the key
     * @param value the value to associate
     * @return the previous value, or null if there was none
     */
    public V put(K key, V value) {
        // Check if resize is needed
        if ((float) (size + 1) / capacity > loadFactor) {
            resize();
        }

        int index = hash(key);
        Entry<K, V> current = buckets[index];

        // Check if key already exists
        while (current != null) {
            if (keysEqual(current.key, key)) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            current = current.next;
        }

        // Key doesn't exist, add new entry at the beginning of the chain
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = buckets[index];
        buckets[index] = newEntry;
        size++;
        return null;
    }

    /**
     * Returns the value associated with the specified key.
     * @param key the key
     * @return the value, or null if key not found
     */
    public V get(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];

        while (current != null) {
            if (keysEqual(current.key, key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    /**
     * Returns the value associated with the specified key, or default if not found.
     * @param key the key
     * @param defaultValue the default value
     * @return the value or default
     */
    public V getOrDefault(K key, V defaultValue) {
        V value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Removes the entry for the specified key.
     * @param key the key to remove
     * @return the removed value, or null if key not found
     */
    public V remove(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];
        Entry<K, V> previous = null;

        while (current != null) {
            if (keysEqual(current.key, key)) {
                if (previous == null) {
                    buckets[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                size--;
                return current.value;
            }
            previous = current;
            current = current.next;
        }
        return null;
    }

    /**
     * Checks if the hash table contains the specified key.
     * @param key the key to check
     * @return true if the key exists
     */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /**
     * Checks if the hash table contains the specified value.
     * @param value the value to check
     * @return true if the value exists
     */
    public boolean containsValue(V value) {
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                if (valuesEqual(current.value, value)) {
                    return true;
                }
                current = current.next;
            }
        }
        return false;
    }

    /**
     * Returns the number of key-value pairs in the hash table.
     * @return the size
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the hash table is empty.
     * @return true if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all entries from the hash table.
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        buckets = new Entry[capacity];
        size = 0;
    }

    /**
     * Resizes the hash table when load factor is exceeded.
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = capacity * 2;
        Entry<K, V>[] oldBuckets = buckets;
        buckets = new Entry[newCapacity];
        capacity = newCapacity;
        size = 0;

        // Rehash all entries
        for (int i = 0; i < oldBuckets.length; i++) {
            Entry<K, V> current = oldBuckets[i];
            while (current != null) {
                put(current.key, current.value);
                current = current.next;
            }
        }
    }

    /**
     * Helper method to compare keys (handles null).
     */
    private boolean keysEqual(K k1, K k2) {
        if (k1 == null) {
            return k2 == null;
        }
        return k1.equals(k2);
    }

    /**
     * Helper method to compare values (handles null).
     */
    private boolean valuesEqual(V v1, V v2) {
        if (v1 == null) {
            return v2 == null;
        }
        return v1.equals(v2);
    }

    /**
     * Returns all keys in the hash table.
     * @return ArrayList of all keys
     */
    public ArrayList<K> keys() {
        ArrayList<K> keyList = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                keyList.add(current.key);
                current = current.next;
            }
        }
        return keyList;
    }

    /**
     * Returns all keys as an ArrayList (alias for keys() for compatibility).
     * @return ArrayList of all keys
     */
    public ArrayList<K> keySet() {
        return keys();
    }

    /**
     * Returns all values in the hash table.
     * @return ArrayList of all values
     */
    public ArrayList<V> values() {
        ArrayList<V> valueList = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                valueList.add(current.value);
                current = current.next;
            }
        }
        return valueList;
    }

    /**
     * Returns all entries in the hash table.
     * @return ArrayList of all entries
     */
    public ArrayList<Entry<K, V>> entries() {
        ArrayList<Entry<K, V>> entryList = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                entryList.add(current);
                current = current.next;
            }
        }
        return entryList;
    }

    /**
     * Executes the given action for each entry.
     * @param action the action to perform
     */
    public void forEach(java.util.function.BiConsumer<K, V> action) {
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                action.accept(current.key, current.value);
                current = current.next;
            }
        }
    }

    /**
     * Returns an iterator over the entries in this hash table.
     * @return an Iterator
     */
    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new HashTableIterator();
    }

    /**
     * Iterator implementation for HashTable.
     */
    private class HashTableIterator implements Iterator<Entry<K, V>> {
        private int bucketIndex = 0;
        private Entry<K, V> currentEntry = null;
        private Entry<K, V> nextEntry = null;

        HashTableIterator() {
            findNextEntry();
        }

        private void findNextEntry() {
            // If there's a next in the current chain
            if (nextEntry != null && nextEntry.next != null) {
                nextEntry = nextEntry.next;
                return;
            }

            // Move to next non-empty bucket
            int startIndex = (nextEntry == null) ? bucketIndex : bucketIndex + 1;
            for (int i = startIndex; i < capacity; i++) {
                if (buckets[i] != null) {
                    nextEntry = buckets[i];
                    bucketIndex = i;
                    return;
                }
            }
            nextEntry = null;
        }

        @Override
        public boolean hasNext() {
            return nextEntry != null;
        }

        @Override
        public Entry<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            currentEntry = nextEntry;
            findNextEntry();
            return currentEntry;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("HashTable{");
        boolean first = true;
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(current.key).append("=").append(current.value);
                first = false;
                current = current.next;
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
