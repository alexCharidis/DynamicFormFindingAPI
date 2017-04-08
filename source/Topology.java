/*************************************************************************
 *  Dependencies: Edge.java
 *
 *  Topology data structure implemented with an interna; list of Edge structs.
 *  Supported operations are adding a directed edge between two
 *  indices, and getters for the list of edges, number of edges and vertices
 *  
 *  @author Alexandros Charidis, MIT
 *************************************************************************/

import java.util.ArrayList;

public class Topology {

    private ArrayList<Edge<Integer>> adj_;  // internal edges container
    
    private int vertices_;                  // number of vertices
    private int edges_;                     // number of edges
    
    // validate that v is a valid index
    private void validate(int v) {
        if (v < 0 || v >= vertices_) {
            throw new IndexOutOfBoundsException("index " + v + " is not between 0 and " + vertices_);
        }
    }
    
    // construct topology from input txt file
    public Topology(String str) { /* TO DO */ }
    
    // empty Topology with V vertices
    public Topology(final int V) {
        vertices_ = V;
        edges_ = 0;
        adj_ = new ArrayList<Edge<Integer>>();
    }
    
    // add directed edge v -> w
    // @throws java.lang.IndexOutOfBoundsException unless both 0 <= vertices < N and 0 <= w < vertices
    public void addEdge(int v, int w) {
        validate(v);
        validate(w);
        
        Edge<Integer> e = new Edge<Integer>(v, w);
        
        adj_.add(e);
        edges_++;
    }
    
    public ArrayList<Edge<Integer>> getEdges() {
        return adj_;
    }
    
    // number of vertices and edges
    public int E() {  return edges_;  }
    public int V() {  return vertices_;  }
	
	public static void main(String[] args) {}

}
