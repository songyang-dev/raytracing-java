package comp557.a4;

import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {
    
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );
    
	/** A point on the plane */
	public static final Vector3d p0 = new Vector3d(0,0,0);
	
    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }

    private static Vector3d dummy = new Vector3d();
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: Objective 4: intersection of ray with plane
    	
    	// plane equation (x - p0) dot normal = 0
    	// line equation r = p + td
    	// set r = x ---> t = (p0 - p) dot n / (d dot n)
    	
    	// calculate the denominator
    	double denom = ray.viewDirection.dot(n);
    	
    	// denom is 0, there is no intersection
    	if (denom == 0) {
    		result.t = Double.POSITIVE_INFINITY;
    		return;
    	}
    	
    	// get t
    	dummy.sub(p0, ray.eyePoint);
    	dummy.normalize();
    	double dot = dummy.dot(n);
    	result.t = dot / denom;
    	
    	// if t < 0, unidirectional ray does not hit the plane
    	if (result.t < 0) {
    		result.t = Double.POSITIVE_INFINITY;
    		return;
    	}
    	
    	// set normal and intersection
    	result.n.set(n);
    	result.n.normalize();
    	ray.getPoint(result.t, result.p);
    	
    	// checker board pattern material
    	int x = result.p.x >= 0 ? (int) result.p.x : (int) result.p.x - 1;
    	int z = result.p.z >= 0 ? (int) result.p.z : (int) result.p.z - 1;
    	if (Math.abs(x) % 2 == Math.abs(z) % 2) {
    		// first material
    		result.material = this.material;
    	}
    	else {
    		// second material, if any
    		if (this.material2 != null)
    			result.material = this.material2;
    		else
    			result.material = this.material;
    	}
    		
    }
    
}
