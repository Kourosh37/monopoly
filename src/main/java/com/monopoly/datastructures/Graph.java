package com.monopoly.datastructures;

import java.util.Iterator;

/**
 * Custom Graph implementation for modeling Financial Interactions.
 * Nodes: Players
 * Edges: Money transfers (Rent payments, Trades)
 * Weight: Amount of money transferred
 * 
 * Used to calculate: "Most financial interaction" reports
 * Uses adjacency list representation with directed weighted edges.
 * 
 * @param <T> The type of vertex data
 */
public class Graph<T> {

    /**
     * Edge class representing a directed weighted edge.
     */
    public static class Edge<T> {
        private T source;
        private T destination;
        private int weight;

        public Edge(T source, T destination, int weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }

        public T getSource() {
            return source;
        }

        public T getDestination() {
            return destination;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public void addWeight(int additionalWeight) {
            this.weight += additionalWeight;
        }

        @Override
        public String toString() {
            return source + " --(" + weight + ")--> " + destination;
        }
    }

    // Adjacency list: maps each vertex to its outgoing edges
    private HashTable<T, LinkedList<Edge<T>>> adjacencyList;
    private int vertexCount;
    private int edgeCount;

    /**
     * Creates an empty graph.
     */
    public Graph() {
        this.adjacencyList = new HashTable<>();
        this.vertexCount = 0;
        this.edgeCount = 0;
    }

    /**
     * Adds a vertex to the graph.
     * @param vertex the vertex to add
     * @return true if vertex was added (didn't exist before)
     */
    public boolean addVertex(T vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex cannot be null");
        }
        if (adjacencyList.containsKey(vertex)) {
            return false;
        }
        adjacencyList.put(vertex, new LinkedList<>());
        vertexCount++;
        return true;
    }

    /**
     * Removes a vertex and all its incident edges.
     * @param vertex the vertex to remove
     * @return true if vertex was found and removed
     */
    public boolean removeVertex(T vertex) {
        if (!adjacencyList.containsKey(vertex)) {
            return false;
        }
        
        // Count outgoing edges to be removed
        LinkedList<Edge<T>> outgoingEdges = adjacencyList.get(vertex);
        edgeCount -= outgoingEdges.size();
        
        // Remove all incoming edges to this vertex
        for (T v : adjacencyList.keys()) {
            if (!v.equals(vertex)) {
                LinkedList<Edge<T>> edges = adjacencyList.get(v);
                LinkedList<Edge<T>> toRemove = new LinkedList<>();
                for (Edge<T> edge : edges) {
                    if (edge.destination.equals(vertex)) {
                        toRemove.add(edge);
                    }
                }
                for (Edge<T> edge : toRemove) {
                    removeEdgeFromList(edges, edge);
                    edgeCount--;
                }
            }
        }
        
        adjacencyList.remove(vertex);
        vertexCount--;
        return true;
    }

    /**
     * Helper method to remove an edge from a linked list.
     */
    private void removeEdgeFromList(LinkedList<Edge<T>> edges, Edge<T> edgeToRemove) {
        for (int i = 0; i < edges.size(); i++) {
            Edge<T> edge = edges.get(i);
            if (edge.source.equals(edgeToRemove.source) && 
                edge.destination.equals(edgeToRemove.destination)) {
                edges.remove(i);
                return;
            }
        }
    }

    /**
     * Adds a directed weighted edge. If edge already exists, updates the weight.
     * @param source the source vertex
     * @param destination the destination vertex
     * @param weight the edge weight
     */
    public void addEdge(T source, T destination, int weight) {
        // Ensure both vertices exist
        addVertex(source);
        addVertex(destination);
        
        // Check if edge already exists
        LinkedList<Edge<T>> edges = adjacencyList.get(source);
        for (Edge<T> edge : edges) {
            if (edge.destination.equals(destination)) {
                edge.setWeight(weight);
                return;
            }
        }
        
        // Add new edge
        edges.add(new Edge<>(source, destination, weight));
        edgeCount++;
    }

    /**
     * Adds weight to an existing edge, or creates edge if it doesn't exist.
     * Used for accumulating financial transactions.
     * @param source the source vertex
     * @param destination the destination vertex
     * @param additionalWeight the weight to add
     */
    public void addOrUpdateEdge(T source, T destination, int additionalWeight) {
        // Ensure both vertices exist
        addVertex(source);
        addVertex(destination);
        
        // Check if edge already exists
        LinkedList<Edge<T>> edges = adjacencyList.get(source);
        for (Edge<T> edge : edges) {
            if (edge.destination.equals(destination)) {
                edge.addWeight(additionalWeight);
                return;
            }
        }
        
        // Create new edge
        edges.add(new Edge<>(source, destination, additionalWeight));
        edgeCount++;
    }

    /**
     * Removes an edge.
     * @param source the source vertex
     * @param destination the destination vertex
     * @return true if edge was found and removed
     */
    public boolean removeEdge(T source, T destination) {
        if (!adjacencyList.containsKey(source)) {
            return false;
        }
        
        LinkedList<Edge<T>> edges = adjacencyList.get(source);
        for (int i = 0; i < edges.size(); i++) {
            Edge<T> edge = edges.get(i);
            if (edge.destination.equals(destination)) {
                edges.remove(i);
                edgeCount--;
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the weight of an edge.
     * @param source the source vertex
     * @param destination the destination vertex
     * @return the edge weight, or 0 if edge doesn't exist
     */
    public int getEdgeWeight(T source, T destination) {
        if (!adjacencyList.containsKey(source)) {
            return 0;
        }
        
        LinkedList<Edge<T>> edges = adjacencyList.get(source);
        for (Edge<T> edge : edges) {
            if (edge.destination.equals(destination)) {
                return edge.weight;
            }
        }
        return 0;
    }

    /**
     * Checks if an edge exists.
     * @param source the source vertex
     * @param destination the destination vertex
     * @return true if edge exists
     */
    public boolean hasEdge(T source, T destination) {
        if (!adjacencyList.containsKey(source)) {
            return false;
        }
        
        LinkedList<Edge<T>> edges = adjacencyList.get(source);
        for (Edge<T> edge : edges) {
            if (edge.destination.equals(destination)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a vertex exists.
     * @param vertex the vertex
     * @return true if vertex exists
     */
    public boolean hasVertex(T vertex) {
        return adjacencyList.containsKey(vertex);
    }

    /**
     * Returns all neighbors (vertices with outgoing edges from source).
     * @param vertex the source vertex
     * @return list of neighboring vertices
     */
    public ArrayList<T> getNeighbors(T vertex) {
        ArrayList<T> neighbors = new ArrayList<>();
        if (!adjacencyList.containsKey(vertex)) {
            return neighbors;
        }
        
        LinkedList<Edge<T>> edges = adjacencyList.get(vertex);
        for (Edge<T> edge : edges) {
            neighbors.add(edge.destination);
        }
        return neighbors;
    }

    /**
     * Returns all outgoing edges from a vertex.
     * @param vertex the source vertex
     * @return list of outgoing edges
     */
    public ArrayList<Edge<T>> getOutgoingEdges(T vertex) {
        ArrayList<Edge<T>> result = new ArrayList<>();
        if (!adjacencyList.containsKey(vertex)) {
            return result;
        }
        
        LinkedList<Edge<T>> edges = adjacencyList.get(vertex);
        for (Edge<T> edge : edges) {
            result.add(edge);
        }
        return result;
    }

    /**
     * Returns all incoming edges to a vertex.
     * @param vertex the destination vertex
     * @return list of incoming edges
     */
    public ArrayList<Edge<T>> getIncomingEdges(T vertex) {
        ArrayList<Edge<T>> result = new ArrayList<>();
        
        for (T v : adjacencyList.keys()) {
            LinkedList<Edge<T>> edges = adjacencyList.get(v);
            for (Edge<T> edge : edges) {
                if (edge.destination.equals(vertex)) {
                    result.add(edge);
                }
            }
        }
        return result;
    }

    /**
     * Returns all vertices in the graph.
     * @return list of vertices
     */
    public ArrayList<T> getAllVertices() {
        return adjacencyList.keys();
    }

    /**
     * Returns all edges in the graph.
     * @return list of all edges
     */
    public ArrayList<Edge<T>> getAllEdges() {
        ArrayList<Edge<T>> allEdges = new ArrayList<>();
        for (T vertex : adjacencyList.keys()) {
            LinkedList<Edge<T>> edges = adjacencyList.get(vertex);
            for (Edge<T> edge : edges) {
                allEdges.add(edge);
            }
        }
        return allEdges;
    }

    /**
     * Returns total weight of all outgoing edges from a vertex.
     * Represents total money paid by a player.
     * @param vertex the vertex
     * @return total outgoing weight
     */
    public int getTotalOutgoingWeight(T vertex) {
        if (!adjacencyList.containsKey(vertex)) {
            return 0;
        }
        
        int total = 0;
        LinkedList<Edge<T>> edges = adjacencyList.get(vertex);
        for (Edge<T> edge : edges) {
            total += edge.weight;
        }
        return total;
    }

    /**
     * Returns total weight of all incoming edges to a vertex.
     * Represents total money received by a player.
     * @param vertex the vertex
     * @return total incoming weight
     */
    public int getTotalIncomingWeight(T vertex) {
        int total = 0;
        for (T v : adjacencyList.keys()) {
            LinkedList<Edge<T>> edges = adjacencyList.get(v);
            for (Edge<T> edge : edges) {
                if (edge.destination.equals(vertex)) {
                    total += edge.weight;
                }
            }
        }
        return total;
    }

    /**
     * Returns total interactions (sum of incoming and outgoing weights).
     * @param vertex the vertex
     * @return total interaction weight
     */
    public int getTotalInteractions(T vertex) {
        return getTotalOutgoingWeight(vertex) + getTotalIncomingWeight(vertex);
    }

    /**
     * Returns the edge with the highest weight (most money transferred).
     * @return the highest weighted edge, or null if no edges
     */
    public Edge<T> getMostInteractedPair() {
        Edge<T> maxEdge = null;
        int maxWeight = Integer.MIN_VALUE;
        
        for (T vertex : adjacencyList.keys()) {
            LinkedList<Edge<T>> edges = adjacencyList.get(vertex);
            for (Edge<T> edge : edges) {
                if (edge.weight > maxWeight) {
                    maxWeight = edge.weight;
                    maxEdge = edge;
                }
            }
        }
        return maxEdge;
    }

    /**
     * Returns the vertex with the most total interactions.
     * @return the most active vertex, or null if empty
     */
    public T getMostActiveVertex() {
        T mostActive = null;
        int maxInteractions = Integer.MIN_VALUE;
        
        for (T vertex : adjacencyList.keys()) {
            int interactions = getTotalInteractions(vertex);
            if (interactions > maxInteractions) {
                maxInteractions = interactions;
                mostActive = vertex;
            }
        }
        return mostActive;
    }

    /**
     * Returns the total bidirectional weight between two vertices.
     * Represents total money exchanged in both directions.
     * @param v1 first vertex
     * @param v2 second vertex
     * @return total bidirectional weight
     */
    public int getBidirectionalWeight(T v1, T v2) {
        return getEdgeWeight(v1, v2) + getEdgeWeight(v2, v1);
    }

    /**
     * Returns the vertex count.
     * @return number of vertices
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * Returns the edge count.
     * @return number of edges
     */
    public int getEdgeCount() {
        return edgeCount;
    }

    /**
     * Checks if the graph is empty.
     * @return true if no vertices
     */
    public boolean isEmpty() {
        return vertexCount == 0;
    }

    /**
     * Clears all vertices and edges.
     */
    public void clear() {
        adjacencyList.clear();
        vertexCount = 0;
        edgeCount = 0;
    }

    /**
     * Performs depth-first traversal starting from a vertex.
     * @param start the starting vertex
     * @return list of vertices in DFS order
     */
    public ArrayList<T> depthFirstTraversal(T start) {
        ArrayList<T> result = new ArrayList<>();
        if (!hasVertex(start)) {
            return result;
        }
        
        HashTable<T, Boolean> visited = new HashTable<>();
        Stack<T> stack = new Stack<>();
        stack.push(start);
        
        while (!stack.isEmpty()) {
            T vertex = stack.pop();
            if (!visited.containsKey(vertex)) {
                visited.put(vertex, true);
                result.add(vertex);
                
                // Push neighbors in reverse order for consistent ordering
                ArrayList<T> neighbors = getNeighbors(vertex);
                for (int i = neighbors.size() - 1; i >= 0; i--) {
                    if (!visited.containsKey(neighbors.get(i))) {
                        stack.push(neighbors.get(i));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Performs breadth-first traversal starting from a vertex.
     * @param start the starting vertex
     * @return list of vertices in BFS order
     */
    public ArrayList<T> breadthFirstTraversal(T start) {
        ArrayList<T> result = new ArrayList<>();
        if (!hasVertex(start)) {
            return result;
        }
        
        HashTable<T, Boolean> visited = new HashTable<>();
        Queue<T> queue = new Queue<>();
        queue.enqueue(start);
        visited.put(start, true);
        
        while (!queue.isEmpty()) {
            T vertex = queue.dequeue();
            result.add(vertex);
            
            for (T neighbor : getNeighbors(vertex)) {
                if (!visited.containsKey(neighbor)) {
                    visited.put(neighbor, true);
                    queue.enqueue(neighbor);
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Graph {\n");
        for (T vertex : adjacencyList.keys()) {
            sb.append("  ").append(vertex).append(" -> [");
            LinkedList<Edge<T>> edges = adjacencyList.get(vertex);
            boolean first = true;
            for (Edge<T> edge : edges) {
                if (!first) sb.append(", ");
                sb.append(edge.destination).append("(").append(edge.weight).append(")");
                first = false;
            }
            sb.append("]\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
