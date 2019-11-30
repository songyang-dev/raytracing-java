package comp557.a4;

import javax.vecmath.Point3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	
    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
     */
    public Box() {
    	super();
    	this.max = new Point3d( 1, 1, 1 );
    	this.min = new Point3d( -1, -1, -1 );
    }	

	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// TODO: Objective 6: intersection of Ray with axis aligned box
		
		double tx_min, ty_min, tz_min, tx_max, ty_max, tz_max;
		
		// check for zero components
		if (ray.viewDirection.x == 0) {
			// check directly if the ray has origin within the x range
			if (ray.eyePoint.x < this.min.x || ray.eyePoint.x > this.max.x) {
				result.t = Double.POSITIVE_INFINITY;
				return;
			}
			// otherwise proceed with the ray's origin x component
			tx_min = ray.eyePoint.x;
			tx_max = ray.eyePoint.x;
		}
		else {
			tx_min = (this.min.x - ray.eyePoint.x) / ray.viewDirection.x;
			tx_max = (this.max.x - ray.eyePoint.x) / ray.viewDirection.x;
		}
		
		if (ray.viewDirection.y == 0) {
			// similar situation for y
			if (ray.eyePoint.y < this.min.y || ray.eyePoint.y > this.max.y) {
				result.t = Double.POSITIVE_INFINITY;
				return;
			}
			ty_min = ray.eyePoint.y;
			ty_max = ray.eyePoint.y;
		}
		else {
			ty_min = (this.min.y - ray.eyePoint.y) / ray.viewDirection.y;
			ty_max = (this.max.y - ray.eyePoint.y) / ray.viewDirection.y;
		}
		
		if (ray.viewDirection.z == 0) {
			// similar situation for z
			if (ray.eyePoint.z < this.min.z || ray.eyePoint.z > this.max.z) {
				result.t = Double.POSITIVE_INFINITY;
				return;
			}
			tz_min = ray.eyePoint.z;
			tz_max = ray.eyePoint.z;
		}
		else {
			tz_min = (this.min.z - ray.eyePoint.z) / ray.viewDirection.z;
			tz_max = (this.max.z - ray.eyePoint.z) / ray.viewDirection.z;
		}
		
		// getting lows and highs
		double tx_low = Math.min(tx_min, tx_max);
		double tx_high = Math.max(tx_min, tx_max);
		
		double ty_low = Math.min(ty_min, ty_max);
		double ty_high = Math.max(ty_min, ty_max);
		
		double tz_low = Math.min(tz_min, tz_max);
		double tz_high = Math.max(tz_min, tz_max);
		
		// intersect x and y
		double t_min = Math.max(tx_low, ty_low);
		double t_max = Math.min(tx_high, ty_high);
		
		// no intersection
		if (t_max < t_min) {
			result.t = Double.POSITIVE_INFINITY;
			return;
		}
		
		// if there is an intersection, compare with z
		t_min = Math.max(t_min, tz_low);
		t_max = Math.min(t_max, tz_high);
		
		// no intersection
		if (t_max < t_min) {
			result.t = Double.POSITIVE_INFINITY;
			return;
		}
		
		// intersection found
		
		// prevent self-shadowing
		if (t_min < EPSILON) {
			result.t = Double.POSITIVE_INFINITY;
			return;
		}
		
		result.t = t_min;
		result.material = this.material;
		ray.getPoint(result.t, result.p);
		
		// point on the -x face
		if (Math.abs(result.p.x - this.min.x) < EPSILON) {
			result.n.set(-1, 0, 0);
		}
		// point on the +x face
		else if (Math.abs(result.p.x - this.max.x) < EPSILON) {
			result.n.set(1,0,0);
		}
		// point on the -y face
		else if (Math.abs(result.p.y - this.min.y) < EPSILON) {
			result.n.set(0,-1,0);
		}
		// point on the +y face
		else if (Math.abs(result.p.y - this.max.y) < EPSILON) {
			result.n.set(0,1,0);
		}
		// point on the -z face
		else if (Math.abs(result.p.z - this.min.z) < EPSILON) {
			result.n.set(0,0,-1);
		}
		// point on the +z face
		else if (Math.abs(result.p.z - this.max.z) < EPSILON) {
			result.n.set(0,0,1);
		}
		
		else System.out.println("Unexpected box intersection");
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		
	}	

}
