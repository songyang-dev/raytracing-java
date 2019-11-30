package comp557.a4;

import java.util.Set;

public class BoundVolume extends Box implements Comparable<BoundVolume>{
	
	public BoundVolume[] volumes; // eight volumes per volume 
	
	public Set<Integer> faces;
	
	public BoundVolume() {
		// TODO Auto-generated constructor stub
		this.material = null;
	}

	@Override
	public int compareTo(BoundVolume arg0) {
		// TODO Auto-generated method stub
		
		if (this.min.epsilonEquals(arg0.min, EPSILON)
				&& this.max.epsilonEquals(arg0.max, EPSILON))
			return 0;
		
		if ((this.min.x < arg0.min.x) && (this.min.y < arg0.min.y) && (this.min.z < arg0.min.z))
			return -1;
		else return 1;
	}

}
