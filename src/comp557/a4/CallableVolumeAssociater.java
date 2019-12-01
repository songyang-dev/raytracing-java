package comp557.a4;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import javax.vecmath.Point3d;

public class CallableVolumeAssociater implements Callable<Integer> {

	private BoundVolume bd;

	private PolygonSoup soup;
	
	private int id;

	public CallableVolumeAssociater(BoundVolume bd, PolygonSoup soup, int id) {
		// TODO Auto-generated constructor stub
		this.bd = bd;
		this.soup = soup;
		this.id = id;
	}

	@Override
	public Integer call() {
		// TODO Auto-generated method stub
		BoundVolume volume = this.bd;
		
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
		return this.id;
	}
	
}
