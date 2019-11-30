package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {
    
	/** Radius of the sphere. */
	public double radius = 1;
    
	/** Location of the sphere center. */
	public Point3d center = new Point3d( 0, 0, 0 );
    
    /**
     * Default constructor
     */
    public Sphere() {
    	super();
    }
    
    /**
     * Creates a sphere with the request radius and center. 
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere( double radius, Point3d center, Material material ) {
    	super();
    	this.radius = radius;
    	this.center = center;
    	this.material = material;
    }

    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
        /** Used for calculation */
        Vector3d dummy = new Vector3d();
        
        /** Used for calculation */
        double dot = 0;
        
        /** Used for calculation */
        double discriminant = 0;
        
        // TODO: Objective 2: intersection of ray with sphere
    	// t = -( d dot (e-center)) +- sqrt( (d dot (e-center))^2 - (norm(e-center)^2 - radius^2))
    	
    	// dummy = e - center
    	dummy.sub(ray.eyePoint, this.center);
    	
    	// dot = d dot (e-center)
    	dot = ray.viewDirection.dot(dummy);
    	
    	// discriminant = (d dot (e-center))^2 - (norm(e-center)^2 - radius^2)
    	discriminant = dot*dot - (dummy.lengthSquared() - this.radius*this.radius);
    	
    	// cases
    	if (discriminant < 0) {
    		result.t = Double.POSITIVE_INFINITY;
    		return;
    	}
    	else if (discriminant == 0) {
    		result.t = -dot;
    	}
    	// intersects two places, but only takes the smallest one
    	else {
    		result.t = - dot - Math.sqrt(discriminant);
    	}
    	
    	// if t < 0, object is behind the camera
    	// if t < epsilon, self-shadowing happens
    	if (result.t < EPSILON) {
    		result.t = Double.POSITIVE_INFINITY;
    		return;
    	}
    	
    	// track the material, normal and position
    	result.material = this.material;
    	ray.getPoint(result.t, result.p);
    	result.n.sub(result.p, this.center);
    	result.n.normalize();
    }
    
}
