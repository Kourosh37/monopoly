package com.monopoly.datastructures;

/**
 * Node class for the General Tree structure.
 * Used in Player Asset Hierarchy: Player -> ColorGroup -> Property -> Buildings
 * 
 * @param <T> The type of data stored in the node
 */
public class TreeNode<T> {

    private T data;
    private TreeNode<T> parent;
    private LinkedList<TreeNode<T>> children;

    /**
     * Creates a tree node with the specified data.
     * @param data the data to store
     */
    public TreeNode(T data) {
        this.data = data;
        this.parent = null;
        this.children = new LinkedList<>();
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
     * Returns the parent of this node.
     * @return the parent node, or null if this is the root
     */
    public TreeNode<T> getParent() {
        return parent;
    }

    /**
     * Sets the parent of this node.
     * @param parent the parent node
     */
    public void setParent(TreeNode<T> parent) {
        this.parent = parent;
    }

    /**
     * Returns the children of this node.
     * @return list of child nodes
     */
    public LinkedList<TreeNode<T>> getChildren() {
        return children;
    }

    /**
     * Adds a child node to this node.
     * @param child the child node to add
     */
    public void addChild(TreeNode<T> child) {
        child.setParent(this);
        children.add(child);
    }

    /**
     * Creates and adds a new child node with the specified data.
     * @param data the data for the new child
     * @return the newly created child node
     */
    public TreeNode<T> addChild(T data) {
        TreeNode<T> child = new TreeNode<>(data);
        child.setParent(this);
        children.add(child);
        return child;
    }

    /**
     * Removes a child node from this node.
     * @param child the child node to remove
     * @return true if the child was found and removed
     */
    public boolean removeChild(TreeNode<T> child) {
        int index = -1;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == child) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            children.remove(index);
            child.setParent(null);
            return true;
        }
        return false;
    }

    /**
     * Removes all children from this node.
     */
    public void clearChildren() {
        for (TreeNode<T> child : children) {
            child.setParent(null);
        }
        children.clear();
    }

    /**
     * Returns the number of children.
     * @return the child count
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * Returns the child at the specified index.
     * @param index the index
     * @return the child node
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public TreeNode<T> getChild(int index) {
        return children.get(index);
    }

    /**
     * Checks if this node is a leaf (has no children).
     * @return true if this node has no children
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Checks if this node is the root (has no parent).
     * @return true if this node has no parent
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Returns the depth of this node (distance from root).
     * @return the depth (0 for root)
     */
    public int getDepth() {
        int depth = 0;
        TreeNode<T> current = this.parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }

    /**
     * Returns the height of the subtree rooted at this node.
     * @return the height (0 for leaf)
     */
    public int getHeight() {
        if (isLeaf()) {
            return 0;
        }
        int maxChildHeight = 0;
        for (TreeNode<T> child : children) {
            int childHeight = child.getHeight();
            if (childHeight > maxChildHeight) {
                maxChildHeight = childHeight;
            }
        }
        return 1 + maxChildHeight;
    }

    /**
     * Returns the root of the tree containing this node.
     * @return the root node
     */
    public TreeNode<T> getRoot() {
        TreeNode<T> current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    /**
     * Checks if this node is an ancestor of the given node.
     * @param node the potential descendant
     * @return true if this node is an ancestor
     */
    public boolean isAncestorOf(TreeNode<T> node) {
        TreeNode<T> current = node.parent;
        while (current != null) {
            if (current == this) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }

    /**
     * Checks if this node is a descendant of the given node.
     * @param node the potential ancestor
     * @return true if this node is a descendant
     */
    public boolean isDescendantOf(TreeNode<T> node) {
        return node.isAncestorOf(this);
    }

    /**
     * Finds a node with the specified data in this subtree.
     * @param data the data to search for
     * @return the node containing the data, or null if not found
     */
    public TreeNode<T> find(T data) {
        if (this.data == null ? data == null : this.data.equals(data)) {
            return this;
        }
        for (TreeNode<T> child : children) {
            TreeNode<T> result = child.find(data);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns all descendants of this node (not including this node).
     * @return list of all descendant nodes
     */
    public LinkedList<TreeNode<T>> getAllDescendants() {
        LinkedList<TreeNode<T>> descendants = new LinkedList<>();
        collectDescendants(descendants);
        return descendants;
    }

    /**
     * Helper method to collect all descendants.
     */
    private void collectDescendants(LinkedList<TreeNode<T>> list) {
        for (TreeNode<T> child : children) {
            list.add(child);
            child.collectDescendants(list);
        }
    }

    @Override
    public String toString() {
        return "TreeNode{" + data + "}";
    }
}
