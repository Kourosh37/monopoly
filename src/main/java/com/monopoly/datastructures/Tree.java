package com.monopoly.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Custom General Tree implementation for Player Asset Hierarchy.
 * Structure: Player -> ColorGroup -> Property -> Buildings
 * This is NOT a Binary Search Tree - it's a general tree where nodes can have multiple children.
 * 
 * @param <T> The type of elements stored in the tree
 */
public class Tree<T> implements Iterable<T> {

    private TreeNode<T> root;
    private int size;

    /**
     * Creates an empty tree.
     */
    public Tree() {
        this.root = null;
        this.size = 0;
    }

    /**
     * Creates a tree with the specified root data.
     * @param rootData the data for the root node
     */
    public Tree(T rootData) {
        this.root = new TreeNode<>(rootData);
        this.size = 1;
    }

    /**
     * Returns the root node of the tree.
     * @return the root node, or null if tree is empty
     */
    public TreeNode<T> getRoot() {
        return root;
    }

    /**
     * Sets the root node of the tree.
     * @param root the new root node
     */
    public void setRoot(TreeNode<T> root) {
        this.root = root;
        recalculateSize();
    }

    /**
     * Sets the root data, creating a new root if necessary.
     * @param data the data for the root
     * @return the root node
     */
    public TreeNode<T> setRoot(T data) {
        if (root == null) {
            root = new TreeNode<>(data);
            size = 1;
        } else {
            root.setData(data);
        }
        return root;
    }

    /**
     * Adds a child to the specified parent node.
     * @param parent the parent node
     * @param data the data for the new child
     * @return the newly created child node
     * @throws IllegalArgumentException if parent is null
     */
    public TreeNode<T> addChild(TreeNode<T> parent, T data) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent cannot be null");
        }
        TreeNode<T> child = parent.addChild(data);
        size++;
        return child;
    }

    /**
     * Adds an existing node as a child to the specified parent.
     * @param parent the parent node
     * @param child the child node to add
     * @throws IllegalArgumentException if parent or child is null
     */
    public void addChild(TreeNode<T> parent, TreeNode<T> child) {
        if (parent == null || child == null) {
            throw new IllegalArgumentException("Parent and child cannot be null");
        }
        // Remove from previous parent if exists
        if (child.getParent() != null) {
            child.getParent().removeChild(child);
        }
        parent.addChild(child);
        size += countNodes(child);
    }

    /**
     * Removes a node and all its descendants from the tree.
     * @param node the node to remove
     * @return true if the node was found and removed
     */
    public boolean remove(TreeNode<T> node) {
        if (node == null) {
            return false;
        }
        if (node == root) {
            int oldSize = size;
            root = null;
            size = 0;
            return oldSize > 0;
        }
        TreeNode<T> parent = node.getParent();
        if (parent != null) {
            int removedCount = countNodes(node);
            boolean removed = parent.removeChild(node);
            if (removed) {
                size -= removedCount;
            }
            return removed;
        }
        return false;
    }

    /**
     * Finds a node with the specified data.
     * @param data the data to search for
     * @return the node containing the data, or null if not found
     */
    public TreeNode<T> find(T data) {
        if (root == null) {
            return null;
        }
        return root.find(data);
    }

    /**
     * Checks if the tree contains a node with the specified data.
     * @param data the data to search for
     * @return true if data exists in the tree
     */
    public boolean contains(T data) {
        return find(data) != null;
    }

    /**
     * Returns the depth of a node.
     * @param node the node
     * @return the depth (0 for root)
     */
    public int getDepth(TreeNode<T> node) {
        if (node == null) {
            return -1;
        }
        return node.getDepth();
    }

    /**
     * Returns the height of the tree.
     * @return the height (0 for single node, -1 for empty tree)
     */
    public int getHeight() {
        if (root == null) {
            return -1;
        }
        return root.getHeight();
    }

    /**
     * Returns the number of nodes in the tree.
     * @return the size
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the tree is empty.
     * @return true if the tree has no nodes
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Clears all nodes from the tree.
     */
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * Traverses the tree in pre-order (root, then children).
     * @param action the action to perform on each element
     */
    public void traversePreOrder(Consumer<T> action) {
        traversePreOrder(root, action);
    }

    /**
     * Helper method for pre-order traversal.
     */
    private void traversePreOrder(TreeNode<T> node, Consumer<T> action) {
        if (node == null) return;
        action.accept(node.getData());
        for (TreeNode<T> child : node.getChildren()) {
            traversePreOrder(child, action);
        }
    }

    /**
     * Traverses the tree in post-order (children, then root).
     * @param action the action to perform on each element
     */
    public void traversePostOrder(Consumer<T> action) {
        traversePostOrder(root, action);
    }

    /**
     * Helper method for post-order traversal.
     */
    private void traversePostOrder(TreeNode<T> node, Consumer<T> action) {
        if (node == null) return;
        for (TreeNode<T> child : node.getChildren()) {
            traversePostOrder(child, action);
        }
        action.accept(node.getData());
    }

    /**
     * Traverses the tree in level-order (breadth-first).
     * @param action the action to perform on each element
     */
    public void traverseLevelOrder(Consumer<T> action) {
        if (root == null) return;
        
        Queue<TreeNode<T>> queue = new Queue<>();
        queue.enqueue(root);
        
        while (!queue.isEmpty()) {
            TreeNode<T> node = queue.dequeue();
            action.accept(node.getData());
            for (TreeNode<T> child : node.getChildren()) {
                queue.enqueue(child);
            }
        }
    }

    /**
     * Returns a list of all elements in pre-order.
     * @return ArrayList of elements
     */
    public ArrayList<T> toListPreOrder() {
        ArrayList<T> list = new ArrayList<>();
        traversePreOrder(list::add);
        return list;
    }

    /**
     * Returns a list of all elements in post-order.
     * @return ArrayList of elements
     */
    public ArrayList<T> toListPostOrder() {
        ArrayList<T> list = new ArrayList<>();
        traversePostOrder(list::add);
        return list;
    }

    /**
     * Returns a list of all elements in level-order.
     * @return ArrayList of elements
     */
    public ArrayList<T> toListLevelOrder() {
        ArrayList<T> list = new ArrayList<>();
        traverseLevelOrder(list::add);
        return list;
    }

    /**
     * Returns all leaf nodes (nodes with no children).
     * @return list of leaf nodes
     */
    public ArrayList<TreeNode<T>> getLeaves() {
        ArrayList<TreeNode<T>> leaves = new ArrayList<>();
        collectLeaves(root, leaves);
        return leaves;
    }

    /**
     * Helper method to collect leaves.
     */
    private void collectLeaves(TreeNode<T> node, ArrayList<TreeNode<T>> leaves) {
        if (node == null) return;
        if (node.isLeaf()) {
            leaves.add(node);
        } else {
            for (TreeNode<T> child : node.getChildren()) {
                collectLeaves(child, leaves);
            }
        }
    }

    /**
     * Returns all nodes at a specific level.
     * @param level the level (0 for root)
     * @return list of nodes at that level
     */
    public ArrayList<TreeNode<T>> getNodesAtLevel(int level) {
        ArrayList<TreeNode<T>> nodes = new ArrayList<>();
        collectNodesAtLevel(root, 0, level, nodes);
        return nodes;
    }

    /**
     * Helper method to collect nodes at a specific level.
     */
    private void collectNodesAtLevel(TreeNode<T> node, int currentLevel, int targetLevel, 
                                      ArrayList<TreeNode<T>> nodes) {
        if (node == null) return;
        if (currentLevel == targetLevel) {
            nodes.add(node);
        } else {
            for (TreeNode<T> child : node.getChildren()) {
                collectNodesAtLevel(child, currentLevel + 1, targetLevel, nodes);
            }
        }
    }

    /**
     * Counts the total number of nodes starting from a given node.
     */
    private int countNodes(TreeNode<T> node) {
        if (node == null) return 0;
        int count = 1;
        for (TreeNode<T> child : node.getChildren()) {
            count += countNodes(child);
        }
        return count;
    }

    /**
     * Recalculates the size of the tree.
     */
    private void recalculateSize() {
        this.size = countNodes(root);
    }

    /**
     * Returns an iterator over the elements in pre-order.
     * @return an Iterator
     */
    @Override
    public Iterator<T> iterator() {
        return toListPreOrder().iterator();
    }

    @Override
    public String toString() {
        if (root == null) {
            return "Tree{empty}";
        }
        StringBuilder sb = new StringBuilder();
        buildTreeString(root, "", true, sb);
        return sb.toString();
    }

    /**
     * Helper method to build a visual tree representation.
     */
    private void buildTreeString(TreeNode<T> node, String prefix, boolean isTail, StringBuilder sb) {
        sb.append(prefix).append(isTail ? "└── " : "├── ").append(node.getData()).append("\n");
        LinkedList<TreeNode<T>> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            buildTreeString(children.get(i), prefix + (isTail ? "    " : "│   "), 
                          i == children.size() - 1, sb);
        }
    }
}
