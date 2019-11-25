package comp557.a4;

import java.awt.Dimension;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple camera object, which could be extended to handle a variety of 
 * different camera settings (e.g., aperture size, lens, shutter)
 */
public class Camera {
	
	/** Camera name */
    public String name = "camera";

    /** The eye position */
    public Point3d from = new Point3d(0,0,10);
    
    /** The "look at" position */
    public Point3d to = new Point3d(0,0,0);
    
    /** Up direction, default is y up */
    public Vector3d up = new Vector3d(0,1,0);
    
    /** Vertical field of view (in degrees), default is 45 degrees */
    public double fovy = 45.0;
    
    /** The rendered image size */
    public Dimension imageSize = new Dimension(640,480);
    
    /** Camera w vector, must be set by setCameraSpaceVectors() */
    public Vector3d w = new Vector3d(0,0,0);
    
    /** Camera v vector, must be set by setCameraSpaceVectors() */
    public Vector3d v = new Vector3d(0,0,0);
    
    /** Camera u vector, must be set by setCameraSpaceVectors() */
    public Vector3d u = new Vector3d(0,0,0);
    
    /** Computes the w,v,u vectors */
    public void setCameraSpaceVectors() {
    	
    	// w = eye - look at, then normalize
    	this.w.sub(this.from, this.to);
    	this.w.normalize();
    	
    	// u = up X w, then normalize
    	this.u.cross(this.up, this.w);
    	this.u.normalize();
    	
    	// v = w x u
    	this.v.cross(this.w, this.u);
    }
    
    /**
     * Default constructor
     */
    public Camera() {
    	// do nothing
    }
}

