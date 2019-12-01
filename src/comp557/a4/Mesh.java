package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import comp557.a4.PolygonSoup.Vertex;

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

	/** Number of vertices in the smallest volume */
	private static final int SMALLEST_VOLUME_VERTICES = 1000;
	
	/** Preparatory computations, array of edge pairs that make up a triangle */
	private Vector3d[][] triangleEdges;

	/** Bounding volume of the mesh */
	private BoundVolume boundVolume;
	
	public Mesh() {
		
		super();
		this.soup = null;
	}			

	/**
	 * Uses barycentric coordinates, as opposed to the slides
	 */
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// Base check on the outer volume
		this.boundVolume.intersect(ray, result);

		if (result.t == Double.POSITIVE_INFINITY) {
			return;
		}		
		
		// Find the deepest bounding volume, if any
		Set<BoundVolume> foundVolumes = traverseVolume(ray, result, this.boundVolume);
		if (foundVolumes == null) {
			// no intersection
			result.t = Double.POSITIVE_INFINITY;
			return;
		}
		
		// TODO: Objective 7: ray triangle intersection for meshes
		// only triangles!!
		// using barycentric coordinates

		Vector3d v0v1;
		Vector3d v0v2;
		Vector3d pvec = new Vector3d();
		
		double det, invdet, u, v, t;
		
		Vector3d tvec = new Vector3d();
		Vector3d qvec = new Vector3d();
		
		result.p = new Point3d();
		result.n = new Vector3d();
		
		// Set the current result to nothing, so this would be
		// the "return" value of t if nothing is found
		result.t = Double.POSITIVE_INFINITY;
		
		for (int i = 0; i < this.soup.faceList.size(); i++) {
			int[] face = soup.faceList.get(i);
			
			//3 points of triangle
			Point3d p0 = soup.vertexList.get(face[0]).p;
			
			// Recover from the cached edges
			v0v1 = this.triangleEdges[i][0]; // one side of the triangle
			
			v0v2 = this.triangleEdges[i][1]; // second side of the triangle

			// compare the face orientation and the ray direction
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
	 * Traverses the bounding volume hierarchy and finds the
	 * deepest volumes that the ray goes through
	 * @param ray
	 * @param result
	 * @param volume
	 * @return An array of bound volumes, null if nothing is found
	 */
	private Set<BoundVolume> traverseVolume(Ray ray, IntersectResult result, BoundVolume volume) {
		// TODO Auto-generated method stub
		TreeSet<BoundVolume> found = new TreeSet<BoundVolume>();
		
		// No more children
		if (volume.volumes == null) {
			found.add(volume);
			return found;
		}
		
		// Find which child volume to explore
		for (BoundVolume child : volume.volumes) {
			
			// intersect the child volume
			child.intersect(ray, result);
			if (result.t != Double.POSITIVE_INFINITY) {
				// the child volume is a potential
				found.addAll(traverseVolume(ray, result, child));
			}
		}
		
		return found;
	}

	/**
	 * Prepares the mesh for computation. Sets the max_hierarchy
	 * and many vectors.
	 */
	public void prepare() {
		
		// initialize the array list to be the size of the face list
		this.triangleEdges = new Vector3d[this.soup.faceList.size()][2];
		
		// Cache the triangle edges
		// multithreading
		int cores = Runtime.getRuntime().availableProcessors();
		
		ExecutorService service = Executors.newFixedThreadPool(cores);
		
        List<CallableTriangleFinder> futureList = new ArrayList<CallableTriangleFinder>();
        for ( int i=0; i<cores; i++){
            CallableTriangleFinder runnable = new CallableTriangleFinder(i*this.triangleEdges.length/cores,
					(i+1)*this.soup.faceList.size()/cores,
					this.soup,
					this.triangleEdges);
            futureList.add(runnable);
        }
        System.out.println("Start mesh triangle pre-processing");
        try{
            List<Future<Integer>> futures = service.invokeAll(futureList);  
        }catch(Exception err){
            err.printStackTrace();
        }
        System.out.println("Completed mesh triangle pre-processing");
        service.shutdown();
		
        // Bounding volumes
        
		// This method calculates how many divisions of a 
		// bounding volume are needed
		// Rule: the smallest volume contains <1000 vertices
		
		// Let n be the number of vertices
		// Then, n / #volumes = 1000
		// # volumes = 1000 / n
		// # volumes = (8^depth - 1)/7
		// depth = ceiling( log(7 #volumes + 1) / log(8) )
		this.maxHierarchy = (int) Math.ceil(Math.log1p(7 * SMALLEST_VOLUME_VERTICES) / Math.log(8));
	
		// build bounding volumes on the mesh
		int initialDepth = 1;
		// Initialize the volume
		this.boundVolume = new BoundVolume();
		buildBoundingVolumes(initialDepth, this.boundVolume);
	}
	
	/**
	 * Builds a hierarchical bounding volume of the mesh
	 * Recursive function
	 */
	private void buildBoundingVolumes(int depth, BoundVolume parent) {
		
		// no volume
		if (depth == 0) return;
		
		// Base case:
		if (depth == 1) {
			
			// Find the min and max of the outer volume box
			Point3d min = new Point3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			Point3d max = new Point3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
			for (Vertex point : this.soup.vertexList) {
				if (point.p.x < min.x) min.x = point.p.x;
				if (point.p.x > max.x) max.x = point.p.x;
				if (point.p.y < min.y) min.y = point.p.y;
				if (point.p.y > max.y) max.y = point.p.y;
				if (point.p.z < min.z) min.z = point.p.z;
				if (point.p.z > max.z) max.z = point.p.z;
			}
			parent.min = min;
			parent.max = max;
			
			if (this.maxHierarchy == 1) return;
		}
		
		// Recursion
		// Use the parent volume to divide into 8 subvolumes
		parent.volumes = new BoundVolume[8];
		
		// Temporary variables
		Point3d middle = new Point3d();
		middle.interpolate(parent.min, parent.max, 0.5);
		
		// Region 1
		parent.volumes[0] = new BoundVolume();
		parent.volumes[0].min = new Point3d(
				middle.x,
				parent.min.y,
				parent.min.z);
		parent.volumes[0].max = new Point3d(
				parent.max.x,
				middle.y,
				middle.z);
		
		// Region 2
		parent.volumes[1] = new BoundVolume();
		parent.volumes[1].min = new Point3d(
				middle.x,
				parent.min.y,
				middle.z);
		parent.volumes[1].max = new Point3d(
				parent.max.x,
				middle.y,
				parent.max.z);
		
		// Region 3
		parent.volumes[2] = new BoundVolume();
		parent.volumes[2].min = new Point3d(
				parent.min.x,
				parent.min.y,
				middle.z);
		parent.volumes[2].max = new Point3d(
				middle.x,
				middle.y,
				parent.max.z);
		
		// Region 4
		parent.volumes[3] = new BoundVolume();
		parent.volumes[3].min = new Point3d(parent.min);
		parent.volumes[3].max = new Point3d(middle);
		
		// Region 5
		parent.volumes[4] = new BoundVolume();
		parent.volumes[4].min = new Point3d(
				middle.x,
				middle.y,
				parent.min.z);
		parent.volumes[4].max = new Point3d(
				parent.max.x,
				parent.max.y,
				middle.z);
		
		// Region 6
		parent.volumes[5] = new BoundVolume();
		parent.volumes[5].min = new Point3d(middle);
		parent.volumes[5].max = new Point3d(parent.max);
		
		// Region 7
		parent.volumes[6] = new BoundVolume();
		parent.volumes[6].min = new Point3d(
				parent.min.x,
				middle.y,
				middle.z);
		parent.volumes[6].max = new Point3d(
				middle.x,
				parent.max.y,
				parent.max.z);
		
		// Region 8
		parent.volumes[7] = new BoundVolume();
		parent.volumes[7].min = new Point3d(
				parent.min.x,
				middle.y,
				parent.min.z);
		parent.volumes[7].max = new Point3d(
				middle.x,
				parent.max.y,
				middle.z);
		
		// Final case
		if (depth + 1 == this.maxHierarchy) {
			// Associate the corresponding 
			for (BoundVolume child : parent.volumes) associate(child);
			return;
		}
		
		for (BoundVolume child : parent.volumes) buildBoundingVolumes(depth + 1, child);
	}

	/**
	 * Associates the bounding volume with the faces of the mesh that has a vertex inside 
	 * Places indices of the face list
	 * @param child
	 */
	private void associate(BoundVolume volume) {
		// TODO Auto-generated method stub
		
		// Assign a set of face indices, no duplicates allowed
		Set<Integer> faces = new TreeSet<Integer>();
		volume.faces = faces;
		
		// temporary var
		Point3d[] points = new Point3d[3];
		
		// Find every face with a vertex in the volume
		for (int index = 0; index < this.soup.faceList.size(); index++) {
			
			int[] face = this.soup.faceList.get(index);
			
			// 3 points of the triangle
			points[0] = this.soup.vertexList.get(face[0]).p;
			points[1] = this.soup.vertexList.get(face[1]).p;
			points[2] = this.soup.vertexList.get(face[2]).p;
			
			// check for bounds
			for (Point3d point: points) {
				if ((point.x > volume.min.x && point.x < volume.max.x)
						&& (point.y > volume.min.y && point.y < volume.max.y)
						&& (point.z > volume.min.z && point.z < volume.max.z)
					) {
					// the point is inside the volume
					faces.add(index);
					break;
				}
			}
		}
	}
}
