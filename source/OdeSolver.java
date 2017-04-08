/*************************************************************************
 *  Compilation:  javac OdeSolver.java
 *  Execution:    java OdeSolver
 *  Dependencies: toxiclibscore.jar  MassSpringSystem.java
 *
 *  Ordinary Differential Equation solver via simple Euler forward method.
 *  
 *  @author Alexandros Charidis, MIT
 *************************************************************************/

import toxi.geom.Vec3D;

public class OdeSolver {

	/**
	 * Default constructor
	 */
	public OdeSolver(){ }
	
	public Vec3D[] takeStep(MassSpringSystem mss_, final float h){
		Vec3D[] state = mss_.getState();
        Vec3D[] derX = mss_.evalF(state);
        
        int num = state.length;
        Vec3D[] newState = new Vec3D[num];
 
        for (int i = 0; i < num; i++) {
            newState[i] = state[i].add(derX[i].scale(h));
        }

        return newState;
	}
	
	public static void main(String[] args) { }

}
