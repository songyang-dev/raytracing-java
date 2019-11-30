package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;
	
	/** The max depth of a bounding volume tree, 2 by default
	 * Depth = 1 means just a box around the mesh */
	private int maxHierarchy = 2;

	public Mesh() {
		
		super();
		this.soup = null;
	}			

	/**
	 * Uses barycentric coordinates, as opposed to the slides
	 */
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// TODO: Objective 7: ray triangle intersection for meshes
		// only triangles!!
		// using barycentric coordinates

		Vector3d v0v1 = new Vector3d();
		Vector3d v0v2 = new Vector3d();
		Vector3d pvec = new Vector3d();
		
		double det, invdet, u, v, t;
		
		Vector3d tvec = new Vector3d();
		Vector3d qvec = new Vector3d();
		
		result.p = new Point3d();
		result.n = new Vector3d();
		
		// Set the current result to nothing, so this would be
		// the "return" value of t if nothing is found
		result.t = Double.POSITIVE_INFINITY;
		
		for (int[] face : soup.faceList) {

			//3 points of triangle
			Point3d p0 = soup.vertexList.get(face[0]).p;
			Point3d p1 = soup.vertexList.get(face[1]).p;
			Point3d p2 = soup.vertexList.get(face[2]).p;

			
			v0v1.sub(p1, p0); // one side of the triangle
			
			v0v2.sub(p2, p0); // second side of the triangle

			pvec.cross(ray.viewDirection,v0v2);
			det = v0v1.dot(pvec);

			if(Math.abs(det)< EPSILON) continue; // parallel to the triangle

			invdet = 1/det;
			
			tvec.sub(ray.eyePoint,p0);
			u = tvec.dot(pvec)*invdet;
			if(u<0||u>1) continue;
			
			
			qvec.cross(tvec, v0v1);
			v = ray.viewDirection.dot(qvec)*invdet;
			if(v<0||u+v>1) continue;
			
			t = v0v2.dot(qvec)*invdet;
			
			if (t>EPSILON && t<result.t) {
				result.t = t;
				
				ray.getPoint(result.t, result.p);
				result.material = material;
				
				result.n.cross(v0v1, v0v2);
				result.n.normalize();
			}
		}
		
	}
	
	/**
	 * Prepares the mesh for computation. Sets the max_hierarchy
	 * and many vectors.
	 */
	public void prepare() {
		// This method calculates how many divisions of a 
		// bounding volume are needed
		// Rule: the smallest volume contains <1000 vertices
		
		// Let n be the number of vertices
		// Then, n / #volumes = 1000
		// # volumes = 1000 / n
		// # volumes = 8^depth - 1 
	}
	
	/**
	 * Builds a hierarchical bounding volume of the mesh
	 * Recursive function
	 */
	public void buildBoundingVolumes(int depth) {
		
	}
}
