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

	public Mesh() {
		super();
		this.soup = null;
	}			
	
	/** Cached normals of every face, computed once at the beginning of intersections */
	public List<Vector3d> normals = new ArrayList<Vector3d>();
	
	/** Cached edges of every faces, computed once at the beginning of intersections
	 * edges[i]= {edgeAB, edgeBC, edgeCA} */
	public List<Vector3d[]> edges = new ArrayList<Vector3d[]>();
	
	private boolean prepared = false;
	
	public void prepare() {
		// assume triangles!
		List<PolygonSoup.Vertex> list = this.soup.vertexList;

		for (int[] face : this.soup.faceList) {
			Vector3d normal = new Vector3d();
			Vector3d edgeAB = new Vector3d();
			Vector3d edgeBC = new Vector3d();
			Vector3d edgeCA = new Vector3d();
			
			edgeAB.sub(list.get(face[1]).p, list.get(face[0]).p);
			edgeBC.sub(list.get(face[2]).p, list.get(face[1]).p);
			edgeCA.sub(list.get(face[0]).p, list.get(face[2]).p);
			
			Vector3d[] triangle = new Vector3d[3];
			triangle[0] = edgeAB;
			triangle[1] = edgeBC;
			triangle[2] = edgeCA;

			this.edges.add(triangle);

			normal.cross(edgeAB, edgeBC);
			normal.normalize();

			this.normals.add(normal);
		}
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// TODO: Objective 9: ray triangle intersection for meshes
		
		// To avoid calculating the same thing over and over, cache the face normals
		if (!prepared) {
			prepared = true;
			
			this.prepare();
		}
		
		// For every face, look for the intersection and perform inside-edge test
		Iterator<Vector3d[]> edgesIterator = this.edges.iterator();
		Iterator<Vector3d> normalsIterator = this.normals.iterator();
		Iterator<int[]> faceIterator = this.soup.faceList.iterator();
	
		
		while(edgesIterator.hasNext() && normalsIterator.hasNext() && faceIterator.hasNext()) {
			Vector3d[] triangle = (Vector3d[]) edgesIterator.next();
			Vector3d normal = (Vector3d) normalsIterator.next();
			int[] face = (int[]) faceIterator.next();
			
			// there is an intersection
			if (intersectRayTriangle(ray, face, triangle, normal, result)) {
				// intersection's t value is in result now
				result.material = this.material;
				result.n.set(normal);
				ray.getPoint(result.t, result.p);
				
				return;
			}
			
		}
		
		// If no face is intersected, no intersection
		result.t = Double.POSITIVE_INFINITY;
	}

	// temporary variables
	private static Vector3d temp = new Vector3d();
	private static Vector3d aToX = new Vector3d();
	private static Vector3d bToX = new Vector3d();
	private static Vector3d cToX = new Vector3d();
	
	/** 
	 * According to the slide 21-23 in ray tracing
	 * @param ray A ray
	 * @param face An array of vertex indices belonging to the face
	 * @param triangle An array of 2 edges of the triangle
	 * @param result Where the intersection is put, if any
	 * @return True if an intersection is found
	 */
	private boolean intersectRayTriangle(Ray ray, int[] face, Vector3d[] triangle,
			Vector3d normal, IntersectResult result) {
		
		// get the t that intersects the plane
		double nom, denom;
		List<PolygonSoup.Vertex> vertices = this.soup.vertexList;
		
		denom = ray.viewDirection.dot(normal); // d dot n
		if (denom < EPSILON && denom > -EPSILON) return false; // ray parallel to the plane
		
		temp.sub(vertices.get(face[0]).p, ray.eyePoint); // a - p
		nom = temp.dot(normal); // (a - p) dot n
		
		result.t = nom / denom;
		
		if (result.t < EPSILON) return false; // wrong ray direction
		
		ray.getPoint(result.t, result.p); // compute intersection with plane
		
		aToX.sub(result.p, vertices.get(face[0]).p); // x - a
		temp.cross(triangle[0], aToX); // (b - a) x (x - a)
		if (temp.dot(normal) < -EPSILON) return false;
		
		bToX.sub(result.p, vertices.get(face[1]).p); // x - b
		temp.cross(triangle[1], bToX); // (c - b) x (x - b)
		if (temp.dot(normal) < -EPSILON) return false;
		
		cToX.sub(result.p, vertices.get(face[2]).p); // x - c
		temp.cross(triangle[2], cToX); // (a - c) x (x - c)
		if (temp.dot(normal) < -EPSILON) return false;
		
		// intersection is in the triangle
		
		return true;
	}

}
