Song Yang 260744153

Attempted bonus:

- Quadrics:
	I implemented a quadric intersection method. Only works if the input
	quadric homogeneous matrix Q is valid. The quadric surface is textured
	with a checker board-ish pattern like the plane. See Quadric.xml.
	
	Note: The Q matrix is input as a flat 16 entry vector.
	
- Mirror material:
	The mirror material is a reflection lighting. The number of bounces is
	set by a global variable in Scene.java. By default, it is 1. The xml field
	for the mirror is called "mirror" and takes a rgb vector as the tint of 
	reflection.
	
- Multithreading:
	The Scene.java now instantiates as many threads as there are processors.
	The screen is split into many sections that work independently. 
	
- Mesh preprocessing:
	The mesh's triangle vectors and all computed and stored in a list before
	intersections are computed. This saves the time from recomputing the
	triangle edges from the vertices in the polygon soup. Done by the
	prepare() method.