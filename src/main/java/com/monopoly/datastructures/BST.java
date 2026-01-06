package com.monopoly.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Custom Binary Search Tree implementation for maintaining sorted dynamic data.
 * Used for: Player rankings by wealth, rent collected, etc.
 * Supports In-Order Traversal for generating ranked lists.
 * 
 * @param <T> The type of elements stored (must be Comparable)
 */
public class BST<T extends Comparable<T>> implements Iterable<T> {

    private BSTNode<T> root;
    private int size;

    /**
     * Creates an empty BST.
     */
    public BST() {
        this.root = null;
        this.size = 0;
    }

    /**
     * Returns the root node.
     * @return the root
     */
    public BSTNode<T> getRoot() {
        return root;
    }

    /**
     * Inserts an element into the BST.
     * @param element the element to insert
     * @throws IllegalArgumentException if element is null
     */
    public void insert(T element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot insert null element");
        }
        root = insertRecursive(root, element, null);
        size++;
    }

    /**
     * Recursive helper for insert.
     */
    private BSTNode<T> insertRecursive(BSTNode<T> node, T element, BSTNode<T> parent) {
        if (node == null) {
            BSTNode<T> newNode = new BSTNode<>(element);
            newNode.setParent(parent);
            return newNode;
        }

        int cmp = element.compareTo(node.getData());
        if (cmp < 0) {
            node.setLeft(insertRecursive(node.getLeft(), element, node));
        } else if (cmp > 0) {
            node.setRight(insertRecursive(node.getRight(), element, node));
        }
        // If cmp == 0, element already exists, do nothing (no duplicates)

        return node;
    }

    /**
     * Deletes an element from the BST.
     * @param element the element to delete
     * @return true if element was found and deleted
     */
    public boolean delete(T element) {
        if (element == null || root == null) {
            return false;
        }
        int originalSize = size;
        root = deleteRecursive(root, element);
        return size < originalSize;
    }

    /**
     * Recursive helper for delete.
     */
    private BSTNode<T> deleteRecursive(BSTNode<T> node, T element) {
        if (node == null) {
            return null;
        }

        int cmp = element.compareTo(node.getData());
        if (cmp < 0) {
            node.setLeft(deleteRecursive(node.getLeft(), element));
        } else if (cmp > 0) {
            node.setRight(deleteRecursive(node.getRight(), element));
        } else {
            // Node to delete found
            size--;

            // Case 1: Leaf node
            if (node.isLeaf()) {
                return null;
            }

            // Case 2: Node with one child
            if (node.getLeft() == null) {
                BSTNode<T> right = node.getRight();
                if (right != null) {
                    right.setParent(node.getParent());
                }
                return right;
            }
            if (node.getRight() == null) {
                BSTNode<T> left = node.getLeft();
                if (left != null) {
                    left.setParent(node.getParent());
                }
                return left;
            }

            // Case 3: Node with two children
            // Find in-order successor (minimum in right subtree)
            BSTNode<T> successor = findMinNode(node.getRight());
            node.setData(successor.getData());
            size++; // Compensate because deleteRecursive will decrement
            node.setRight(deleteRecursive(node.getRight(), successor.getData()));
        }
        return node;
    }

    /**
     * Searches for an element in the BST.
     * @param element the element to search for
     * @return true if found
     */
    public boolean search(T element) {
        return searchNode(element) != null;
    }

    /**
     * Returns the node containing the element.
     * @param element the element to find
     * @return the node, or null if not found
     */
    public BSTNode<T> searchNode(T element) {
        if (element == null) {
            return null;
        }
        return searchRecursive(root, element);
    }

    /**
     * Recursive helper for search.
     */
    private BSTNode<T> searchRecursive(BSTNode<T> node, T element) {
        if (node == null) {
            return null;
        }
        int cmp = element.compareTo(node.getData());
        if (cmp == 0) {
            return node;
        } else if (cmp < 0) {
            return searchRecursive(node.getLeft(), element);
        } else {
            return searchRecursive(node.getRight(), element);
        }
    }

    /**
     * Finds the minimum element in the BST.
     * @return the minimum element
     * @throws NoSuchElementException if tree is empty
     */
    public T findMin() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return findMinNode(root).getData();
    }

    /**
     * Finds the node with minimum value starting from a given node.
     */
    private BSTNode<T> findMinNode(BSTNode<T> node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }

    /**
     * Finds the maximum element in the BST.
     * @return the maximum element
     * @throws NoSuchElementException if tree is empty
     */
    public T findMax() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        return findMaxNode(root).getData();
    }

    /**
     * Finds the node with maximum value starting from a given node.
     */
    private BSTNode<T> findMaxNode(BSTNode<T> node) {
        while (node.getRight() != null) {
            node = node.getRight();
        }
        return node;
    }

    /**
     * Returns the in-order successor of an element.
     * @param element the element
     * @return the successor, or null if none
     */
    public T successor(T element) {
        BSTNode<T> node = searchNode(element);
        if (node == null) {
            return null;
        }
        BSTNode<T> succ = successorNode(node);
        return succ != null ? succ.getData() : null;
    }

    /**
     * Finds the in-order successor of a node.
     */
    private BSTNode<T> successorNode(BSTNode<T> node) {
        // If right subtree exists, successor is minimum in right subtree
        if (node.getRight() != null) {
            return findMinNode(node.getRight());
        }
        // Otherwise, go up until we find a node that is a left child
        BSTNode<T> parent = node.getParent();
        while (parent != null && node == parent.getRight()) {
            node = parent;
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * Returns the in-order predecessor of an element.
     * @param element the element
     * @return the predecessor, or null if none
     */
    public T predecessor(T element) {
        BSTNode<T> node = searchNode(element);
        if (node == null) {
            return null;
        }
        BSTNode<T> pred = predecessorNode(node);
        return pred != null ? pred.getData() : null;
    }

    /**
     * Finds the in-order predecessor of a node.
     */
    private BSTNode<T> predecessorNode(BSTNode<T> node) {
        // If left subtree exists, predecessor is maximum in left subtree
        if (node.getLeft() != null) {
            return findMaxNode(node.getLeft());
        }
        // Otherwise, go up until we find a node that is a right child
        BSTNode<T> parent = node.getParent();
        while (parent != null && node == parent.getLeft()) {
            node = parent;
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * Performs in-order traversal (sorted order).
     * @param action the action to perform on each element
     */
    public void inOrderTraversal(Consumer<T> action) {
        inOrderRecursive(root, action);
    }

    private void inOrderRecursive(BSTNode<T> node, Consumer<T> action) {
        if (node == null) return;
        inOrderRecursive(node.getLeft(), action);
        action.accept(node.getData());
        inOrderRecursive(node.getRight(), action);
    }

    /**
     * Performs reverse in-order traversal (descending order).
     * Useful for getting rankings from highest to lowest.
     * @param action the action to perform on each element
     */
    public void reverseInOrderTraversal(Consumer<T> action) {
        reverseInOrderRecursive(root, action);
    }

    private void reverseInOrderRecursive(BSTNode<T> node, Consumer<T> action) {
        if (node == null) return;
        reverseInOrderRecursive(node.getRight(), action);
        action.accept(node.getData());
        reverseInOrderRecursive(node.getLeft(), action);
    }

    /**
     * Performs pre-order traversal.
     * @param action the action to perform on each element
     */
    public void preOrderTraversal(Consumer<T> action) {
        preOrderRecursive(root, action);
    }

    private void preOrderRecursive(BSTNode<T> node, Consumer<T> action) {
        if (node == null) return;
        action.accept(node.getData());
        preOrderRecursive(node.getLeft(), action);
        preOrderRecursive(node.getRight(), action);
    }

    /**
     * Performs post-order traversal.
     * @param action the action to perform on each element
     */
    public void postOrderTraversal(Consumer<T> action) {
        postOrderRecursive(root, action);
    }

    private void postOrderRecursive(BSTNode<T> node, Consumer<T> action) {
        if (node == null) return;
        postOrderRecursive(node.getLeft(), action);
        postOrderRecursive(node.getRight(), action);
        action.accept(node.getData());
    }

    /**
     * Performs level-order traversal (BFS).
     * @param action the action to perform on each element
     */
    public void levelOrderTraversal(Consumer<T> action) {
        if (root == null) return;
        Queue<BSTNode<T>> queue = new Queue<>();
        queue.enqueue(root);
        while (!queue.isEmpty()) {
            BSTNode<T> node = queue.dequeue();
            action.accept(node.getData());
            if (node.getLeft() != null) queue.enqueue(node.getLeft());
            if (node.getRight() != null) queue.enqueue(node.getRight());
        }
    }

    /**
     * Returns a sorted list (ascending order).
     * @return ArrayList of elements in sorted order
     */
    public ArrayList<T> toSortedList() {
        ArrayList<T> list = new ArrayList<>();
        inOrderTraversal(list::add);
        return list;
    }

    /**
     * Returns a sorted list in descending order.
     * @return ArrayList of elements in descending order
     */
    public ArrayList<T> toDescendingList() {
        ArrayList<T> list = new ArrayList<>();
        reverseInOrderTraversal(list::add);
        return list;
    }

    /**
     * Returns the top K elements (highest values).
     * Useful for leaderboards.
     * @param k the number of elements to return
     * @return ArrayList of top k elements
     */
    public ArrayList<T> getTopK(int k) {
        ArrayList<T> result = new ArrayList<>();
        getTopKRecursive(root, k, result);
        return result;
    }

    private void getTopKRecursive(BSTNode<T> node, int k, ArrayList<T> result) {
        if (node == null || result.size() >= k) return;
        getTopKRecursive(node.getRight(), k, result);
        if (result.size() < k) {
            result.add(node.getData());
        }
        getTopKRecursive(node.getLeft(), k, result);
    }

    /**
     * Returns the number of elements in the BST.
     * @return the size
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the BST is empty.
     * @return true if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the height of the BST.
     * @return the height (-1 for empty tree)
     */
    public int getHeight() {
        return getHeightRecursive(root);
    }

    private int getHeightRecursive(BSTNode<T> node) {
        if (node == null) return -1;
        return 1 + Math.max(getHeightRecursive(node.getLeft()), 
                          getHeightRecursive(node.getRight()));
    }

    /**
     * Clears all elements from the BST.
     */
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * Checks if the BST property is maintained (for testing/debugging).
     * @return true if valid BST
     */
    public boolean isValidBST() {
        return isValidBSTRecursive(root, null, null);
    }

    private boolean isValidBSTRecursive(BSTNode<T> node, T min, T max) {
        if (node == null) return true;
        if (min != null && node.getData().compareTo(min) <= 0) return false;
        if (max != null && node.getData().compareTo(max) >= 0) return false;
        return isValidBSTRecursive(node.getLeft(), min, node.getData()) &&
               isValidBSTRecursive(node.getRight(), node.getData(), max);
    }

    /**
     * Returns an iterator (in-order traversal).
     * @return an Iterator
     */
    @Override
    public Iterator<T> iterator() {
        return toSortedList().iterator();
    }

    @Override
    public String toString() {
        ArrayList<T> list = toSortedList();
        StringBuilder sb = new StringBuilder("BST[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
