package com.monopoly.datastructures;

/**
 * Node class for the Binary Search Tree.
 * 
 * @param <T> The type of data stored in the node (must be Comparable)
 */
public class BSTNode<T extends Comparable<T>> {

    private T data;
    private BSTNode<T> left;
    private BSTNode<T> right;
    private BSTNode<T> parent; // Optional parent reference for easier operations

    /**
     * Creates a BST node with the specified data.
     * @param data the data to store
     */
    public BSTNode(T data) {
        this.data = data;
        this.left = null;
        this.right = null;
        this.parent = null;
    }

    /**
     * Returns the data stored in this node.
     * @return the data
     */
    public T getData() {
        return data;
    }

    /**
     * Sets the data for this node.
     * @param data the new data
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Returns the left child of this node.
     * @return the left child, or null if none
     */
    public BSTNode<T> getLeft() {
        return left;
    }

    /**
     * Sets the left child of this node.
     * @param left the new left child
     */
    public void setLeft(BSTNode<T> left) {
        this.left = left;
        if (left != null) {
            left.parent = this;
        }
    }

    /**
     * Returns the right child of this node.
     * @return the right child, or null if none
     */
    public BSTNode<T> getRight() {
        return right;
    }

    /**
     * Sets the right child of this node.
     * @param right the new right child
     */
    public void setRight(BSTNode<T> right) {
        this.right = right;
        if (right != null) {
            right.parent = this;
        }
    }

    /**
     * Returns the parent of this node.
     * @return the parent, or null if root
     */
    public BSTNode<T> getParent() {
        return parent;
    }

    /**
     * Sets the parent of this node.
     * @param parent the new parent
     */
    public void setParent(BSTNode<T> parent) {
        this.parent = parent;
    }

    /**
     * Checks if this node is a leaf (has no children).
     * @return true if no children
     */
    public boolean isLeaf() {
        return left == null && right == null;
    }

    /**
     * Checks if this node has exactly one child.
     * @return true if exactly one child
     */
    public boolean hasOneChild() {
        return (left == null) != (right == null);
    }

    /**
     * Checks if this node has two children.
     * @return true if two children
     */
    public boolean hasTwoChildren() {
        return left != null && right != null;
    }

    /**
     * Checks if this node is the left child of its parent.
     * @return true if left child
     */
    public boolean isLeftChild() {
        return parent != null && parent.left == this;
    }

    /**
     * Checks if this node is the right child of its parent.
     * @return true if right child
     */
    public boolean isRightChild() {
        return parent != null && parent.right == this;
    }

    /**
     * Returns the sibling of this node.
     * @return the sibling, or null if none
     */
    public BSTNode<T> getSibling() {
        if (parent == null) {
            return null;
        }
        return isLeftChild() ? parent.right : parent.left;
    }

    /**
     * Returns the uncle of this node (parent's sibling).
     * @return the uncle, or null if none
     */
    public BSTNode<T> getUncle() {
        if (parent == null) {
            return null;
        }
        return parent.getSibling();
    }

    /**
     * Returns the grandparent of this node.
     * @return the grandparent, or null if none
     */
    public BSTNode<T> getGrandparent() {
        if (parent == null) {
            return null;
        }
        return parent.parent;
    }

    @Override
    public String toString() {
        return "BSTNode{" + data + "}";
    }
}
