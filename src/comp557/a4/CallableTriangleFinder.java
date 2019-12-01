package comp557.a4;

import java.util.List;
import java.util.concurrent.Callable;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


public class CallableTriangleFinder implements Callable<Integer> {

	private int soupMin;
	private int soupMax;
	
	private PolygonSoup soup;
	private Vector3d[][] triangles;
	
	public CallableTriangleFinder(int soupMin, int soupMax, PolygonSoup soup, Vector3d[][] triangleEdges) {
		// TODO Auto-generated constructor stub
		this.soupMin = soupMin;
		this.soupMax = soupMax;
		this.soup = soup;
		this.triangles = triangleEdges;
	}

	@Override
	public Integer call() {
		// TODO Auto-generated method stub
		
		// Cache the triangle edges
		for (int i = this.soupMin; i < this.soupMax; i++) {

			int[] face = this.soup.faceList.get(i);

			//3 points of triangle
			Point3d p0 = soup.vertexList.get(face[0]).p;
			Point3d p1 = soup.vertexList.get(face[1]).p;
			Point3d p2 = soup.vertexList.get(face[2]).p;

			Vector3d v0v1 = new Vector3d();
			Vector3d v0v2 = new Vector3d();
			// Cache these in advance
			v0v1.sub(p1, p0); // one side of the triangle
			v0v2.sub(p2, p0); // second side of the triangle

			// Array of the two edges
			Vector3d[] triangle = new Vector3d[2];
			triangle[0] = v0v1;
			triangle[1] = v0v2;

			this.triangles[i] = triangle;
		}
		
		return this.soupMin;
	}

}
