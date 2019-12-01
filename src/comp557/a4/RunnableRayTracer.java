package comp557.a4;

import javax.vecmath.Color3f;

public class RunnableRayTracer implements Runnable {
	
	private int i_min;
	private int i_max;
	private int j_min;
	private int j_max;
	
	private Scene scene;
	
	private Thread thread;
	
	/**
	 * Creates a thread casting rays on a rectangle of pixels
	 * @param i_min
	 * @param i_max exclusive
	 * @param j_min
	 * @param j_max exclusive
	 * @param scene
	 */
	public RunnableRayTracer(int i_min, int i_max, int j_min, int j_max, Scene scene) {
		// TODO Auto-generated constructor stub
		this.i_min = i_min;
		this.i_max = i_max;
		this.j_min = j_min;
		this.j_max = j_max;
		this.scene = scene;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
        // References to global objects
		Render render = this.scene.render;
        Camera cam = render.camera;
        
        // temporary variables
        Ray ray = new Ray();
        double[] offset = {0.5,0.5}; // for supersampling
        IntersectResult result = new IntersectResult();
        IntersectResult closestIntersection;
        double tBest;
        Color3f dummyColor = new Color3f(0,0,0);
        
        // supersampling
        Color3f[] supersample = new Color3f[render.samples];
        
        for ( int j = this.j_min; j < this.j_max && !render.isDone(); j++ ) {
            for ( int i = this.i_min; i < this.i_max && !render.isDone(); i++ ) {
            	
            	
            	for (int sample = 0; sample < supersample.length; sample++) {
            		
            		// uniform grid offset
            		offset[0] = sample * 1.0 / supersample.length;
            		offset[1] = sample * 1.0 / supersample.length;
	            	
	                // TODO: Objective 1: generate a ray (use the generateRay method)
	            	Scene.generateRay(i, j, offset, cam, ray);
	            	
	                // TODO: Objective 2: test for intersection with scene surfaces
	            	tBest = Double.POSITIVE_INFINITY;
	            	closestIntersection = null;
	            	
	            	for (Intersectable surface : this.scene.surfaceList) {
	            		surface.intersect(ray, result);
	            		
	            		// no intersection, background color
	            		if (result.t == Double.POSITIVE_INFINITY) {
	            			continue;
	            		}
	            		
	            		// find the closest object
	            		if (result.t < tBest) {
	            			tBest = result.t;
	            			closestIntersection = new IntersectResult(result);
	            		}
	            		
	            	}
	            	
	                // TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
	
	            	// set background color, skip to next ray
	            	if (tBest == Double.POSITIVE_INFINITY) {
	            		supersample[sample] = render.bgcolor;
	                	continue;
	            	}
	            	
	            	// Shading
	            	supersample[sample] = this.scene.computeShading(closestIntersection, 0);
	            	
            	}
            	
            	// TODO: Objective 8: Supersampling
            	// take the average of all colors
            	dummyColor.set(0,0,0);
            	for (Color3f color : supersample) dummyColor.add(color);
            	dummyColor.scale((float) (1.0 / supersample.length));
            	render.setPixel(i, cam.imageSize.height-1-j, dummyColor.get().getRGB());
            }
        }
        
	}
	
	
	public void start() {
		System.out.println("Thread of " + j_min + " to " + j_max);
		
		if (thread == null) {
			thread = new Thread(this, "Thread of " + j_min + " to " + j_max);
			thread.start();
		}
	}

}
