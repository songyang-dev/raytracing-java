package comp557.a4;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point4d;
import javax.vecmath.Tuple4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;


public class Quadric extends Intersectable {
    
	/**
	 * Radius of the sphere.
	 */
	public Matrix4d Q = new Matrix4d();
	public Matrix3d A = new Matrix3d();
	public Vector3d B = new Vector3d();
	public double C;
	
	/**
	 * The second material, e.g., for front and back?
	 */
	Material material2 = null;
	
	public Quadric() {
	
	}
	
	private static Vector3d normalTemp = new Vector3d();
	
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// follows quadratic formula
		double denom, discriminant, b;
		
		// put ray into homogeneous coordinates
		Vector4d rayPoint = new Vector4d(ray.eyePoint);
		rayPoint.w = 1;
		Vector4d rayDirection = new Vector4d(ray.viewDirection);
		rayDirection.w = 1;
		
		// check if the denominator is 0
		denom = 2*mulVecMatVec(rayDirection, Q, rayDirection);
		if (denom < EPSILON && denom > -EPSILON) {
			result.t = Double.POSITIVE_INFINITY;
			return;
		}
		
		// calculate b
		b = mulVecMatVec(rayPoint, Q, rayDirection) + 
				mulVecMatVec(rayDirection, Q, rayPoint);
		
		// calculate the discriminant
		discriminant = Math.pow(b, 2) - 2 * denom * mulVecMatVec(rayPoint, Q, rayPoint);
		
		// if discriminant is negative, no intersection
		if (discriminant < 0) {
			result.t = Double.POSITIVE_INFINITY;
			return;
		}
		else if (discriminant == 0) {
			result.t = - b / denom;
		}
		else {
			result.t = (- b - Math.sqrt(discriminant))/denom;
		}
		
		// t < 0
		if (result.t < EPSILON) {
			result.t = Double.POSITIVE_INFINITY;
			return;
		}
		
		// track the position
    	ray.getPoint(result.t, result.p);
    	
    	// calculate the normal
    	A.transform(result.p, result.n);
    	result.n.sub(B);
    	result.n.normalize();
    	
    	// get the material
    	result.material = this.material;
	}
	
	private static Vector4d leftProduct = new Vector4d();
	private static Vector4d temp = new Vector4d();
	
	/**
	 * Helper method to compute v^T A v, uses a static variable
	 * @param leftVec
	 * @param matrix
	 * @param rightVec
	 * @return
	 */
	private static double mulVecMatVec(Vector4d leftVec, Matrix4d matrix, Vector4d rightVec) {
		
		// leftVec times matrix, result is in leftProduct
		matrix.getColumn(0, temp);
		leftProduct.x = leftVec.dot(temp);
		matrix.getColumn(1, temp);
		leftProduct.y = leftVec.dot(temp);
		matrix.getColumn(2, temp);
		leftProduct.z = leftVec.dot(temp);
		matrix.getColumn(3, temp);
		leftProduct.w = leftVec.dot(temp);
		
		// leftProduct is a vector
		// dot that with the rightVector
		
		return leftProduct.dot(rightVec);
	}
	
}
