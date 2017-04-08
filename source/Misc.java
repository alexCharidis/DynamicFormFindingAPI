/*************************************************************************
 *  Compilation:  javac Misc.java
 *  Execution:    java Misc
 *  Dependencies: processing.core.PApplet  processing.core.PFont
 *                Jama.Matrix
 *
 *  Helper functions primarily for displaying geometry, text, operating with
 *  matrices in Processing API and Java.
 *  
 *  @author Alexandros Charidis, MIT
 *************************************************************************/

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;

public class Misc {

	/**
	 *  Draws the bounding box of the scene 
	 */
	public static void drawBox(final PApplet p5, float x, float y, float z) {
		p5.noFill();
		p5.stroke(180, 90);
		p5.strokeWeight(1);
		p5.pushMatrix();
		p5.translate(x/2, y/2, z/4);
		p5.box(x, y, z/2);
		p5.popMatrix();
	}
	
	/**
	 *  
	 */
	public static void drawSceneGrid(final PApplet p5, int nnX, int nnY, int lngth) {
		p5.stroke(200);
		p5.strokeWeight(0.5f);
	    
	    // horizontal 
	    for (int x = 0; x < nnX - 1; x++) {
	        for (int y = 0; y < nnY; y++) {
	            p5.line(x*lngth, y*lngth, (x + 1) * lngth, y*lngth);
	        }
	    }
	    for (int x = 0; x < nnX; x++) {
	        for (int y = 0; y < nnY - 1; y++) {
	            p5.line(x*lngth, y*lngth, x * lngth, (y + 1)*lngth);
	        }
	    }
	}
	
	// H E L P E R  F U N C T I O N S

	public static void drawGUIText(final PApplet p5, int H) {
	  
	    displaytext(p5, "K spring", p5.createFont("Courier", 11), 11, 0, 'L', 100 + 20 + 40, H - 327);
	    displaytext(p5, "Rest L", p5.createFont("Courier", 11), 11, 0, 'L', 100 + 20 + 40, H - 300);
	    displaytext(p5, "Gravity", p5.createFont("Courier", 11), 11, 0, 'L', 100 + 20 + 40, H - 275);
	    displaytext(p5, "Particles", p5.createFont("Courier", 11), 11, 0, 'L', 100 + 20 + 40, H - 250);
	    
	}


	/**
	 *  draws information about the current sketch
	 */
	public static void displayMark(final PApplet p5, PFont font, int x, int y) {
	    String string0 = "M a s s  S p r i n g  S y s t e m  M o d e l";
	    String string1 = "Alexandros Charidis, MIT";
	    String string2 = "charidis@mit.edu";
	    String string3 = "4.s48 Computational Structural Design & Optimization";
	    String string4 = "Spring 2015";
	    
	    String string5 = "Built with Processing";
	    String string6 = "Using: Toxiclibs, PeasyCam and ControlP5";
	    
	    int off = 20;
	    
	    displaytext(p5, string0, font, 12, 0, 'L', x, y);
	    displaytext(p5, string1, font, 12, 0, 'L', x, y + off);
	    displaytext(p5, string2, font, 12, 0, 'L', x, y + 2*off);
	    displaytext(p5, string3, font, 10, 0, 'L', x, y + 3*off + 10);
	    displaytext(p5, string4, font, 10, 0, 'L', x, y + 4*off + 10);
	    displaytext(p5, string5, font, 10, 0, 'L', x, y + 6*off + 10);
	    displaytext(p5, string6, font, 10, 0, 'L', x, y + 7*off + 10);
	}

	/**
	 *  Helper function for displaying texts on screen
	 */
	public static void displaytext(final PApplet p5, String _text, PFont _font, int _size, int _color, char _align, int _x, int _y){
	    switch (_align){
	      case 'C':
	    	p5.textAlign(p5.CENTER);
	        break;
	      case 'L':
	    	p5.textAlign(p5.LEFT);
	        break;
	      case 'R':
	    	p5.textAlign(p5.RIGHT);
	        break;
	    }
	    p5.textFont(_font, _size);
	    p5.fill(_color); // blue letters with oppacity
	    p5.text(_text, _x, _y);
	}

	/**
	 *  draws the sum of F * L 
	 */
	public static void displaySigmaFl(final PApplet p5, boolean drawSigmaFL, float FL, int H) {
	    if (drawSigmaFL)
	       displaytext(p5, "Sigma FL: " + FL, p5.createFont("Courier", 14), 11, 0, 'L', 290, H - 102);
	   else
	       displaytext(p5, "Sigma FL: ", p5.createFont("Courier", 11), 11, 0, 'C', 180, H - 100);
	}

	ArrayList<PVector> pVectorsFromExternalTXT(final PApplet p5, String dir) {
	    ArrayList<PVector> temp = new ArrayList<PVector>();
	    String[] strLines = p5.loadStrings(dir);
	    for(int i = 1; i < strLines.length; i++){
	        // if "N" is read, a path was finished. We add the path to our list,
	        // we instantiate a new Path object, and skip the current loop.
	        if (strLines[i].equals("N")) { break; } 
	        // splits array with coma character to isolate each component
	        String[] arrTokens = p5.split(strLines[i], ','); 
	        temp.add(new PVector(Integer.parseInt(arrTokens[0]), Integer.parseInt(arrTokens[1]), Integer.parseInt(arrTokens[2])));    
	    }
	    return temp;
	}
	
	public static void main(String[] args) { }

}
