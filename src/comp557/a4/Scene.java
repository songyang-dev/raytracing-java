package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {
    
    /** List of surfaces in the scene */
    public List<Intersectable> surfaceList = new ArrayList<Intersectable>();
	
	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();

    /** Contains information about how to render the scene */
    public Render render;
    
    /** The ambient light color */
    public Color3f ambient = new Color3f();

    /** 
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    }
    
    /**
     * renders the scene
     */
    public void render(boolean showPanel) {
 
        Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        
        render.init(w, h, showPanel);
        
        // TODO: Objective 1: prepare the camera
        cam.setCameraSpaceVectors();
        
        Ray ray = new Ray();
        double[] offset = {0,0};
        
        for ( int j = 0; j < h && !render.isDone(); j++ ) {
            for ( int i = 0; i < w && !render.isDone(); i++ ) {
            	
                // TODO: Objective 1: generate a ray (use the generateRay method)
            	generateRay(i, j, offset, cam, ray);
            	
                // TODO: Objective 2: test for intersection with scene surfaces
            	
                // TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
                
            	// Here is an example of how to calculate the pixel value.
            	Color3f c = new Color3f(render.bgcolor);
            	int r = (int)(255*c.x);
                int g = (int)(255*c.y);
                int b = (int)(255*c.z);
                int a = 255;
                int argb = (a<<24 | r<<16 | g<<8 | b);    
                
                // update the render image
                render.setPixel(i, j, argb);
            }
        }
        
        // save the final render image
        render.save();
        
        // wait for render viewer to close
        render.waitDone();
        
    }
    
    private static Vector3d dummy1 = new Vector3d();
    private static Vector3d dummy2 = new Vector3d();
    
    /**
     * Generate a ray through pixel (i,j).
     * 
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
     * @param cam The camera.
     * @param ray Contains the generated ray.
     */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {
		
		// TODO: Objective 1: generate rays given the provided parameters
		
		// calculate 's' the eye point of the ray on the viewing rectangle at the given screen coordinates
		// s = eye + screen_u * u + screen_v * v - focal * w
		dummy1.scale(i + offset[0], cam.u);
		dummy2.scale(j + offset[1], cam.v);
		
		dummy1.add(dummy2);
		
		dummy2.scale(cam.focalLength, cam.w);
		dummy1.sub(dummy2); // dummy1 = s - eye
		
		// calculate 'd' the direction vector
		// d = s - eye = (eye + screen_u * u + screen_v * v - focal * w) - eye
		// d = screen_u * u + screen_v * v - focal * w
		
		
		// set the ray eye point to eye
		// set the ray direction to d
		ray.set(cam.from, dummy1);
		
	}

	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param root The scene node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public static boolean inShadow(final IntersectResult result, final Light light, final SceneNode root, IntersectResult shadowResult, Ray shadowRay) {
		
		// TODO: Objective 5: check for shadows and use it in your lighting computation
		
		return false;
	}    
}
