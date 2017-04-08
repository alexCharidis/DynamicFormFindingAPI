/*************************************************************************
 *  Dependencies: toxiclibscore.jar Spring.java Topology.java  Edge.java
 *
 *  Mass Spring System data structure for particle-spring simulation 
 *  applications. 
 *  
 *  @author Alexandros Charidis, MIT
 *************************************************************************/

import java.util.ArrayList;
import java.util.Arrays;

import toxi.geom.Vec3D;

public class MassSpringSystem {
	
	private int M_;
    private int S_;
    
    private Vec3D origin_;
    private Topology topology_;
    
    private Vec3D[] state_;  // fixed capacity state
    private ArrayList<Spring> springs_;
    private float[] masses_;
    private Vec3D[] forces_;
    private int[] fixed_;
    
    private Vec3D GRAVITY;
    private float restLngh;
    
    /**
     *  Constructs a mass spring system with M masses 
     */
    public MassSpringSystem(Topology topology) {
        M_ = topology.V();
        S_ = 0;
        origin_ = new Vec3D();
        topology_ = topology;
        springs_ = new ArrayList<Spring>();
    }
	
    /**
     *  Initializes the system's state vector (size 2 * M), forces vector (size M), 
     *  masses vector (size M) from input array of Vec3D points and an array
     *  with indices for anchor points
     */
     
    public void initialize(Vec3D[] geometry, int[] fixed) {
      
        state_ = new Vec3D[2 * M_];
        masses_ = new float[M_]; 
        forces_ = new Vec3D[M_];
        fixed_ = (int[]) fixed;
        
        GRAVITY = new Vec3D(0f, 0f, 0.2f);
        restLngh = 7f;
        
        for (int i = 0; i < M_; i++) {
            state_[i*2] = origin_.add(geometry[i]);
            state_[i*2 + 1] = new Vec3D();
            masses_[i] = 1.4f; // homogeneous mass distribution
            forces_[i] = new Vec3D();
        }
 
    }
    
    /**
     *  Initializes the system's state vector (size 2 * M), forces vector (size M), 
     *  masses vector (size M) from an input txt of Vec3D points and an array
     *  with indices for anchor points
     */
     
    public void initialize(String str, int[] fixed) { }
     
    // traverse the topology and add springs for each edge
    public void setupSprings(float ks, float kd) {
    	
        springs_ = new ArrayList<Spring>();
        
        for (Edge<Integer> e : topology_.getEdges()) {
            Spring spr = new Spring(e.from, e.to);
            spr.setSpringConstant(ks);
            spr.setDampingConstant(kd);
            spr.setRestLength(restLngh);
            springs_.add(spr);
            S_++;
        }
        
    }
    
    /**
     *  The method for computing all the forces acting upon a particle.
     *  Simulated forces are a constant Gravity, a Viscous Drag force
     *  with independent damping coefficient, a Wind vector, and
     *  the all the Springs forces based on a particles' links.
     *
     *  @param the system's state vector
     */
    private void computeForces() {
      
        float VISCOUS = -0.0032f;
        
        // accumulate forces from gravity and viscous drag
        for (int i = 0; i < M_; i++) {  
            forces_[i] = new Vec3D();
                      
            // add gravity force
            forces_[i].addSelf(GRAVITY);
            
            // add viscous drag as f = -x' * k
            forces_[i].addSelf(state_[i*2 + 1].scale(VISCOUS));
        }
        
        // accumulate forces from springs
        for (Spring s : springs_) {
          
            int from = s.from();
            int to   = s.to();
             
            Vec3D dx = state_[to*2].sub(state_[from*2]);
            
            s.setLength(dx.magnitude() + 1e-6f ); // for accumulating Sigma FL
            
            float dist = dx.magnitude() + 1e-6f;
            
            float distStrength = (dist - s.restLength()) / (dist * (1f/masses_[from] + 1f/masses_[to])) * s.kSpring();
            
            s.setForce(distStrength);
            
            // force acting on mass at index <>from<> is springF
            // and by Newtons law, force acting on mass at index <>to<> is -springF
            
            if (Arrays.binarySearch(fixed_, from) < 0) {
                forces_[from].addSelf(dx.scale(distStrength * 1.0f / masses_[from]));
            }
            
            if (Arrays.binarySearch(fixed_, to) < 0) {
                forces_[to].addSelf(dx.scale(-distStrength * 1.0f / masses_[to]));
            }
            
        }
        
        // reverse the force acting on anchor points (again based on Newton..)
        for (int i = 0; i < fixed_.length; i++) {
            Vec3D ftoken = forces_[fixed_[i]];
            forces_[fixed_[i]].subSelf(ftoken);
        }
       
    }
    
    /**
     *  This computes the derivative of a state  at any time.
     *  x -> x' = u (velocity), x' -> x" = a (acceleration)
     *  Since we know the forces and masses, x" = a = F / m
     *  The new state stores the velocity at even indices
     *  and the acceleration at odd indices. Thus a second
     *  order ODE becomes first order.
     *
     *  @return the new state
     */
     
    public Vec3D[] evalF(Vec3D[] state) {
      
        computeForces();
        
        for (int i = 0; i < M_; i++) {
            // take velocity
            Vec3D token = state_[i * 2 + 1];
            
            // x -> x' = velocity
            state_[i*2].addSelf(token);
            
            // v -> v' = a = Force / mass
            state_[i*2 + 1] = forces_[i];
        }
        
        return state_;
        
    }
    
    public float getSumFL() {
        float sum = 0;
        for (Spring s : springs_) {
            sum += s.F() * s.L();
        }
        return sum;
    }
    
    // Setters
    
    public void setOrigin(Vec3D origin) {  origin_ = origin;  }
    public void setState(Vec3D[] state) {  state_ = state;  }
    public void setStatePositionAt(int idx, Vec3D v) {  state_[idx*2] = v;  }
    public void setFixed(int[] fixed) {  fixed_ = fixed;  }
    
    public void setGravity(float z) {  GRAVITY = new Vec3D(0, 0, z);  }
    public void setRestLenght(float l) {
        for (Spring s : springs_) {
            s.setRestLength(l);
        }
    }
    public void setkSpring(float ks) {
        for (Spring s : springs_) {
            s.setSpringConstant(ks);
        }
    }
    
    // Getters
    
    public Vec3D getStatePositionAt(int idx) {  return state_[idx*2];  }
    public Vec3D[] getState() {  return state_;  }
    
    public int getNumMasses()  {  return M_;  }
    public int getNumSprings() {  return S_;  }
    
    public static void main(String[] args) { }
    
}
