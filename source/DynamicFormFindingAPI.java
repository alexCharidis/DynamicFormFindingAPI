/*****************************************************************************
 *  Dependencies: Jama.jar controlP5.jar peasy.jar toxiclibscore.jar
 *                utils.jar BranchNodeGraph.java ForceDensityMethod.java
 *                Scene.java Misc.java
 *
 *  Dynamic Form Finding of Surface Structures using a Particle Spring System 
 *  
 *  Please refer to the accompanying pdf "Form Finding and Surface Structures" 
 *  which explains this implementation.
 *  
 *  This is written in Java 8 and Processing 2.2.1 using Eclipse Luna 4.*. 
 *  Processing can be used within the Eclipse IDE following this tutorial:
 *  https://processing.org/tutorials/eclipse/
 *  
 *  @author Alexandros Charidis, MIT
 ****************************************************************************/

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PFont;
import processing.data.IntList;
import toxi.processing.*;
import toxi.geom.Sphere;
import toxi.geom.Vec3D;
import toxi.geom.mesh.TriangleMesh;
import controlP5.*;
import peasy.*;

public class DynamicFormFindingAPI extends PApplet {
	
	/**    System variables & controls    */

	ControlP5 cp5;
	ToxiclibsSupport gfx;
	PeasyCam cam;

	int numW = 23;
	int numH = 23;
	int V = numW * numH;

	// Window
	int W = 1200, H = 600;

	// Scene
	int WIDTH = 700, LENGTH = 700;
	int backgroundColor = color(255);

	// Fonts
	PFont signatureFont;

	// geometry
	int lngth = 10 * 10;
	IntList fixedN;

	// GUI
	Textlabel lb_load;

	//
	boolean drawAsMesh = true;

	//
	Topology topology;
	MassSpringSystem mss;
	OdeSolver ode;

	TriangleMesh mesh;

	Vec3D[] geometry;
	boolean initSimulation = false;
	boolean drawSigmaFL = false;
	int topolType = 0;
	
	public void setup() {
		
		size(W, H, OPENGL); 
	    smooth();
	    
	    // create font for signature text
	    signatureFont = createFont("Courier-48", 14, true);

	    gfx = new ToxiclibsSupport(this);
	    cp5 = new ControlP5(this);
	    cam = new PeasyCam(this, 10, 100, 400, 400);
	    //cam.setMinimumDistance(1);
	    cam.setMaximumDistance(1000);
	    
	     // initialize ode
	    ode = new OdeSolver();
	    
	    // 
	    resetMassSpringSystem();
	    
	    // GUI controls 
	    
	    cp5.setAutoDraw(false);
	    cp5.addButton("btn_Sim")
	       .setLabel("SIMULATE")
	       .setBroadcast(false)
	       .setPosition(160, H - 140)
	       .setSize(100, 40)
	       .setValue(1)
	       .setColorCaptionLabel(0)
	       .setColorForeground(color(255, 255, 0))
	       .setColorBackground(color(240))
	       .setBroadcast(true)
	       .getCaptionLabel().align(CENTER, CENTER);
	       
	    cp5.addButton("btn_Rst")
	       .setLabel("RESET")
	       .setBroadcast(false)
	       .setPosition(160, H - 200)
	       .setSize(100, 40)
	       .setValue(1)
	       .setColorCaptionLabel(0)
	       .setColorForeground(color(255, 255, 0))
	       .setColorBackground(color(100, 200, 50))
	       .setBroadcast(true)
	       .getCaptionLabel().align(CENTER, CENTER);
	       
	    cp5.addButton("btn_ExpMesh")
	       .setLabel("Export Mesh")
	       .setBroadcast(false)
	       .setPosition(W - 140, H - 140)
	       .setSize(100, 40)
	       .setValue(1)
	       .setColorCaptionLabel(0)
	       .setColorForeground(color(255, 255, 0))
	       .setColorBackground(color(180))
	       .setBroadcast(true)
	       .getCaptionLabel().align(CENTER, CENTER);
	    
	    cp5.addSlider2D("sl2D_loadXY")
	       .setPosition(40, H - 200)
	       .setSize(100,100)
	       .setColorBackground(color(255, 0, 0))
	       .setColorForeground(color(0, 0, 255))
	       .setColorValue(0)
	       .setColorActive(color(0, 255, 0))
	       .setMinX(0)
	       .setMinY(0)
	       .setMaxX(50)
	       .setMaxY(50)
	       .disableCrosshair();
	         
	    // add slider for kSpring
	    cp5.addSlider("sl_kSpring")
	       .setLabel("kSpring")
	       .setPosition(40, H - 340)
	       .setRange(0.0001f, 1.0f)
	       .setValue(0.07f)
	       .setColorActive(color(0, 255, 0))
	       .setColorForeground(color(140))
	       .setColorBackground(color(255,255,255))
	       .setColorValueLabel(0)
	       .setSize(100, 20);
	       
	    // add slider for kSpring
	    cp5.addSlider("sl_RestLngth")
	       .setLabel("Rest L")
	       .setPosition(40, H - 315)
	       .setRange(1, 15)
	       .setValue(7)
	       .setColorActive(color(0, 255, 0))
	       .setColorForeground(color(140))
	       .setColorBackground(color(255,255,255))
	       .setColorValueLabel(0)
	       .setSize(100, 20);
	       
	    // add slider for gravity
	    cp5.addSlider("sl_gravity")
	       .setLabel("kGrav")
	       .setPosition(40, H - 290)
	       .setRange(0.1f, 3)
	       .setValue(0.2f)
	       .setColorActive(color(0, 255, 0))
	       .setColorForeground(color(140))
	       .setColorBackground(color(255,255,255))
	       .setColorValueLabel(0)
	       .setSize(100, 20);
	    
	    // add slider for number of particles
	    cp5.addSlider("sl_numParticles")
	       .setLabel("Num Particles")
	       .setPosition(40, H - 265)
	       .setRange(4, 40)
	       .setValue(8)
	       .setColorActive(color(0, 255, 0))
	       .setColorForeground(color(140))
	       .setColorBackground(color(255,255,255))
	       .setColorValueLabel(0)
	       .setSize(100, 20)
	       .setDecimalPrecision(0);
	       
	    cp5.addSlider("sl_Topology")
	       .setLabel("Topology")
	       .setPosition(40, H - 360)
	       .setSize(100, 15)
	       .setRange(3,0) // values can range from big to small as well
	       .setValue(0)
	       .setColorCaptionLabel(0)
	       .setColorActive(color(0, 255, 0))
	       .setColorBackground(color(100))
	       .setColorForeground(color(140))
	       .setNumberOfTickMarks(4)
	       .setSliderMode(Slider.FLEXIBLE)
	       .setDecimalPrecision(0);
		
	}

	public void draw() {
		
		if (cp5.isMouseOver()) {
	        cam.setActive(false);
	    } else {
	        cam.setActive(true);
	    }
	    
	    background(backgroundColor);
	    
	    // draw origini axis
	    gfx.origin(WIDTH);
	    Misc.drawSceneGrid(this, numW, numH, lngth);
	    drawMeshGrid();
	    
	    if (initSimulation) {
	        initMassSpringSystemSimulation();
	    }
	    
	    // graphic user interface setup
	    
	    gui();
		
	}
	
	public void gui() {
		  
	    if (cp5.isMouseOver()) cursor(CROSS);
	    else cursor(ARROW);
	    
	    hint(DISABLE_DEPTH_TEST);
	    cam.beginHUD();
	    cp5.draw();
	    Misc.displayMark(this, signatureFont, 30, 30);
	    if (drawSigmaFL) Misc.displaySigmaFl(this, drawSigmaFL, mss.getSumFL(), H);
	    Misc.drawGUIText(this, H);
	    cam.endHUD();
	    hint(ENABLE_DEPTH_TEST);
	    
	}
	
	public void stepSystem() {
		  
	    Vec3D[] st = ode.takeStep(mss, 0.0001f);
	    
	    for (int i = 0; i < fixedN.size(); i++) {
	        int id = fixedN.get(i);
	        st[id*2] = geometry[id];
	    }
	    
	    mss.setState(st);
	    
	}
	
	public void massSpringSystemCallback(int stepsPerFrame) {
		  
	    for (int i = 0; i < stepsPerFrame; i++) {
	        stepSystem();
	    }
	    
	}

	public void initMassSpringSystemSimulation() {
	    massSpringSystemCallback(10); // 5 iterations a frame
	}
	
	public void controlEvent(ControlEvent theEvent) {
		  
	    if (theEvent.isFrom(cp5.getController("btn_Sim"))) {
	        initSimulation = !initSimulation;
	        drawSigmaFL = true;
	    }
	  
	    if (theEvent.isFrom(cp5.getController("btn_Rst"))) {
	        resetMassSpringSystem();
	        initSimulation = false;
	    }
	    
	    if (theEvent.isFrom(cp5.getController("btn_ExpMesh"))) {
	        try {
	            String fileID="massSpringModel-"+(System.currentTimeMillis()/1000);
	            FileOutputStream fs;
	            fs = new FileOutputStream(sketchPath(fileID+".obj"));
	            
	            mesh.saveAsOBJ(fs);
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
	    }
	    
	    if (theEvent.isFrom(cp5.getController("sl_Topology"))) {
	        topolType = (int) theEvent.getController().getValue();
	        resetMassSpringSystem();
	    }
	    
	    if (theEvent.isFrom(cp5.getController("sl_kSpring"))) {
	        mss.setkSpring(theEvent.getController().getValue());
	    }
	  
	    if (theEvent.isFrom(cp5.getController("sl_gravity"))) {
	        mss.setGravity(theEvent.getController().getValue());
	    }
	    
	    if (theEvent.isFrom(cp5.getController("sl_RestLngth"))) {
	        mss.setRestLenght(theEvent.getController().getValue());
	    }
	    
	    if (theEvent.isFrom(cp5.getController("sl_numParticles"))) {
	        numW = (int) theEvent.getController().getValue();
	        numH = numW;
	        V = numW * numH;
	        resetMassSpringSystem();
	    }
	    
	    if (theEvent.isFrom(cp5.getController("sl2D_loadXY"))) {
	      
	        float val0 = theEvent.getController().getArrayValue(0);
	        float val1 = theEvent.getController().getArrayValue(1);
	        
	        // map the x, y values from the GUI slider to 0-100 scale
	        // and calculate the percentage along x and along y
	        float prct0 = map(val0, 0, 50, 0, 100) / 100f;
	        float prct1 = map(val1, 0, 50, 0, 100) / 100f;
	        
	        // use the percentage to get x,y indices from the mesh
	        int ind1 = (int)((numW-1)*prct0);
	        int ind2 = (int)((numH-1)*prct1);
	        
	        // convert the x,y indices to 1D index
	        int index = ind2 * (numW) + ind1; 
	        
	        // put the new index inside the current list of fixed indices
	        fixedN.append(index);
	        
	        // update the MSS 
	        
	        // anchor the particle on ground
	        Vec3D token = mss.getStatePositionAt(index);
	        mss.setStatePositionAt(index, new Vec3D(token.x(), token.y(), 0));
	        
	        // update fixed array
	        mss.setFixed(fixedN.array());
	       
	    }

	}

	///

	public void resetMassSpringSystem() {
	    
	    topologyBuilder(topolType);
	  
	    // Construct geometry and fixed points
	    
	    geometryBuilder(topolType);
	    
	    // @Note in this application, the structural system
	    //       has its four corners pinned by default.
	    
	    fixedN = new IntList(); 
	    
	    // invariant pins
	    fixedN.append(0);
	    fixedN.append(numW - 1);
	    fixedN.append(numW * (numH - 1));
	    fixedN.append((numW * numH) - 1);
	    
	    // Initialize Mass Spring System with geometry and fixed points
	    
	    mss.initialize(geometry, fixedN.array());
	    mss.setupSprings(0.07f, -0.0004f);
	    
	}
	
	public void topologyBuilder(int type) {
        
	    if (type == 0) { // regular grid mesh
	        // Construct topology
	        topology = new Topology(V);
	    
	        // horizontal
	        for (int y = 0; y < numH; y++) {
	            for (int x = 0; x < numW - 1; x++) {
	                topology.addEdge(y * numH + x, y * numH + x + 1);
	            }
	        }
	    
	        // vertical
	        for (int x = 0; x < numW; x++) {
	            for (int y = 0; y < numH - 1; y++) {
	                topology.addEdge(y * numW + x, (y + 1) * numW + x);
	            }
	        }
	        
	    } else if (type == 1) { // only for odd number of joints
	        
	        if (V % 2 == 0) {
	            numW--;
	            numH = numW;
	            V = numW * numH;
	        }
	        
	        // Construct topology
	        topology = new Topology(V);
	        
	        // diagonal
	        for (int y = 0; y < numH - 1; y++) {
	            for (int x = 0; x < numW - 1; x++) {
	              
	                // first row
	                if ( (y == 0 && (x > 1 && x < numW/2)) ) {
	                   topology.addEdge(y * numH + x, y * numH + x + numH - 1);
	                }
	                if ( (y == 0 && (x > numW/2 && x < numW-2)) ) {
	                   topology.addEdge(y * numH + x, y * numH + x + 1 + numH );
	                }
	                // last row
	                if ( (y == numH - 2 && (x > 0 && x < numW/2-1)) ) {
	                   topology.addEdge(y * numH + x, y * numH + x + 1 + numH );
	                }
	                if ( (y == numH - 2 && (x > numW/2+1 && x < numW)) ) {
	                   topology.addEdge(y * numH + x, y * numH + x - 1 + numH );
	                }
	                
	                // first column
	                if ( (x == 0 && (y < numH/2 && y > 1)) ) {
	                   topology.addEdge(y * numH + x, y * numH + x + 1 - numH );
	                }
	                if ( (x == 0 && (y > numH/2 && y < numH - 2) ) ) {
	                   topology.addEdge(y * numH + x, y * numH + x + 1 + numH );
	                }
	                // last column
	                if ( (x == numW - 2 && (y < numH/2 - 1 && y > 0)) ) {
	                   topology.addEdge(y * numH + x, y * numH + x + 1 + numH );
	                }
	                if ( (x == numW - 2 && (y > numH/2 + 1 && y < numH) ) ) {
	                   topology.addEdge(y * numH + x, y * numH + x + 1 - numH );
	                }
	                
	                // diagonal elements horizontal stride
	                if ( (x >= numW/2 && x < numW-1) && (y == numH/2) ) {
	                    topology.addEdge(y * numH + x, y * numH + x + 1 + numH);
	                    topology.addEdge(y * numH + x, y * numH + x + 1 - numH); 
	                }
	                if ( (x <= numW/2 && x > 0) && (y == numH/2) ) {
	                    topology.addEdge(y * numH + x, y * numH + x - 1 + numH);
	                    topology.addEdge(y * numH + x, y * numH + x - 1 - numH);  
	                }
	                // diagonal elements vertical stride
	                if ( (y >= numH/2 && y < numH-1) && (x == numW/2) ) {
	                    topology.addEdge(y * numH + x, y * numH + x + 1 + numH);
	                    topology.addEdge(y * numH + x, y * numH + x - 1 + numH); 
	                }
	                if ( (y <= numH/2 && y > 0) && (x == numW/2) ) {
	                    topology.addEdge(y * numH + x, y * numH + x - 1 - numH);
	                    topology.addEdge(y * numH + x, y * numH + x + 1 - numH);  
	                }
	                
	            }
	        }
	        
	        // horizontal
	        for (int y = 0; y < numH; y++) {
	            for (int x = 0; x < numW - 1; x++) {
	                topology.addEdge(y * numH + x, y * numH + x + 1);
	            }
	        }
	        
	        // vertical
	        for (int x = 0; x < numW; x++) {
	            for (int y = 0; y < numH - 1; y++) {
	                topology.addEdge(y * numW + x, (y + 1) * numW + x);
	            }
	        }
	        
	    } else if (type == 2) {
	        
	        // Construct topology
	        topology = new Topology(V);
	        
	        // random diagonals
	        for (int y = 0; y < numH - 1; y++) {
	            for (int x = 0; x < numW - 1; x++) {
	                int i = (int)random(0, 2);
	                if ( i == 1) {
	                    topology.addEdge(y * numH + x, y * numH + x + 1 + numH);
	                } else {
	                    topology.addEdge(y * numH + x + 1, y * numH + x + numH);
	                }
	            }
	        }
	      
	        // horizontal
	        for (int y = 0; y < numH; y++) {
	            for (int x = 0; x < numW - 1; x++) {
	                topology.addEdge(y * numH + x, y * numH + x + 1);
	            }
	        }
	        
	        // vertical
	        for (int x = 0; x < numW; x++) {
	            for (int y = 0; y < numH - 1; y++) {
	                topology.addEdge(y * numW + x, (y + 1) * numW + x);
	            }
	        }
	      
	    } else if (type == 3) {
	        
	        int F = (numW - 1) * (numH - 1);
	        int Vn = V + F;
	        
	        // Construct topology
	        topology = new Topology(Vn);
	      
	        // horizontal
	        for (int y = 0; y < 2; y++) {
	            for (int x = 0; x < numW - 1; x++) {
	                topology.addEdge(x + y*(numW*numH - numW), x + y*(numW*numH - numW) + 1);
	            }
	        }
	        
	        // vertical
	        for (int x = 0; x < 2; x++) {
	            for (int y = 0; y < numH - 1; y++) {
	                topology.addEdge( x*(numW-1) + y*numW, x*(numW-1) + (y+1)*numW );
	            }
	        }
	      
	        // diagonal
	        for (int y = 0; y < numH - 1; y++) {
	            for (int x = 0; x < numW - 1; x++) {
	                int id1 = y*(numH-1) + x;
	                int id2 = y*numH + x;
	                
	                topology.addEdge(id2, id1 + numW*numH);
	                topology.addEdge(id2 + 1, id1 + numW*numH);
	                topology.addEdge(id2 + numH, id1 + numW*numH);
	                topology.addEdge(id2 + numH + 1, id1 + numW*numH);
	            }
	        }
	    }
	    
	    // Construct Mass Spring System with topology
	    
	    mss = new MassSpringSystem(topology);
	}

	public void geometryBuilder(int type) {
	    if (type == 0 || type == 1 || type == 2) {
	        float stepW = (float) WIDTH / (numW - 1);
	        float stepL = (float) LENGTH / (numH - 1);
	        
	        geometry = new Vec3D[V];
	        for (int j = 0; j < numW; j++) {
	            for (int i = 0; i < numH; i++) {
	                geometry[j*numW + i] = new Vec3D(i * stepW, j * stepL, 0f);
	            }
	        }
	    } 
	    else if (type == 3) {
	      
	        float stepW = (float) WIDTH / (numW - 1);
	        float stepL = (float) LENGTH / (numH - 1);
	        
	        int F = (numW - 1) * (numH - 1);
	        int Vn = V + F;
	        
	        geometry = new Vec3D[Vn];
	        for (int j = 0; j < numW; j++) {
	            for (int i = 0; i < numH; i++) {
	                geometry[j*numW + i] = new Vec3D(i * stepW, j * stepL, 0f);
	            }
	        }
	        
	        // internal points
	        for (int i = 0; i < numW - 1; i++) {
	            for (int j = 0; j < numH - 1; j++) {
	              
	                int id = i * (numW) + j;
	                
	                Vec3D p0 = geometry[id];
	                Vec3D p1 = geometry[id + 1];
	                Vec3D p2 = geometry[id + numW];
	                Vec3D p3 = geometry[id + 1 + numW];
	                
	                int id2 = i * (numW-1) + j;
	                
	                geometry[numW*numH + id2] = p0.add(p1).add(p2).add(p3).scale(1f/4);
	                
	            }
	        }
	        
	    }
	}

	public void keyPressed() {
	    if (key == 'p' || key == 'P')
	        saveFrame("mss-######.png");
	}
	
	/**
	 *  Draw the surface using the state returned from FDM
	 */
	public void drawMeshGrid() {

		fill(50, 255, 50);
	      int num = mss.getNumMasses();
	      for (int i = 0; i < num; i++) {
	          //if (sysstate.size() > 0) {
	          Sphere s = new Sphere(mss.getState()[i*2], 4);
	          gfx.sphere(s, 3, true);
	      }
	     
	     strokeWeight(1); 
	     stroke(0);
	     
	     // By building a generic Topology graph, drawing the resultant polygon mesh
	     // in wireframe becomes as simple as traversing the edges of the topology
	     // and drawing each edge as one line
	     
	     for (Edge<Integer> e : topology.getEdges()) {
	         int id0 = e.from;
	         int id1 = e.to;
	         
	         Vec3D p0 = mss.getState()[id0 * 2];
	         Vec3D p1 = mss.getState()[id1 * 2];
	         
	         line(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z());
	     }
	     
	     // draw as mesh
	     mesh = new TriangleMesh();
	     
	     for(int y = 0; y < numW - 1; y++) {
	         for(int x = 0; x < numH - 1; x++) {
	             
	             if (topolType != 3) {
	                 int i = y * numW + x;
	          
	                 Vec3D p0 = mss.getState()[i*2];
	                 Vec3D p1 = mss.getState()[i*2 + 2];
	                 Vec3D p2 = mss.getState()[i*2 + 2 + numW*2];
	                 Vec3D p3 = mss.getState()[i*2 + numW*2];
	              
	                 // two triangles for each face
	                 mesh.addFace(p0,p3,p2);
	                 mesh.addFace(p0,p2,p1);
	             } else {
	                 int i = y * numW + x;
	              
	                 Vec3D p0 = mss.getState()[i*2];
	                 Vec3D p1 = mss.getState()[i*2 + 2];
	                 Vec3D p2 = mss.getState()[i*2 + 2 + numW*2];
	                 Vec3D p3 = mss.getState()[i*2 + numW*2];
	                 
	                 int i2 = y * (numW-1) + x;
	                 Vec3D p4 = mss.getState()[numW*numH*2 + i2*2];
	                 
	                 // four triangles for each face
	                 mesh.addFace(p0,p4,p3);
	                 mesh.addFace(p0,p4,p1);
	                 mesh.addFace(p1,p4,p2);
	                 mesh.addFace(p2,p4,p3);
	             }
	         }
	     }
	     
	     fill(160, 50);
	     noStroke();
	     gfx.mesh(mesh, false);
		
	}
	
}
