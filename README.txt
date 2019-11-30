Song Yang 260744153

Attempted bonus:

- Quadrics:
	I implemented a quadric intersection method. Only works if the input
	quadric homogeneous matrix Q is valid. The quadric surface is textured
	with a checker board-ish pattern like the plane. See Quadric.xml.
	
- Mirror material:
	The mirror material is a reflection lighting. The number of bounces is
	set by a global variable in Scene.java. Currently, it is 1.