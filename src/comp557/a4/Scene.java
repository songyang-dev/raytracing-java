package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.prism.paint.Color;

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
    
    /** Scene node for the shadow ray */
    public SceneNode snode;
    
    /** 
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    	
    	snode = new SceneNode();
    	snode.children = this.surfaceList;
    	snode.M.setIdentity();
    	snode.Minv.setIdentity();
    }
    
    /**
     * renders the scene
     */
    public void render(boolean showPanel) {
 
        Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        
        render.init(w, h, showPanel);
        
        // Get camera space transformations and more ready
        cam.prepareCamera();
        
        // temporary variables
        Ray ray = new Ray();
        double[] offset = {0.5,0.5};
        IntersectResult result = new IntersectResult();
        IntersectResult closestIntersection;
        double tBest;
        
        for ( int j = 0; j < h && !render.isDone(); j++ ) {
            for ( int i = 0; i < w && !render.isDone(); i++ ) {
            	
                // TODO: Objective 1: generate a ray (use the generateRay method)
            	generateRay(i, j, offset, cam, ray);
            	
                // TODO: Objective 2: test for intersection with scene surfaces
            	tBest = Double.POSITIVE_INFINITY;
            	closestIntersection = null;
            	
            	for (Intersectable surface : surfaceList) {
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
            		render.setPixel(i, h-1-j, render.bgcolor.get().getRGB());
                	continue;
            	}
            	
            	// Shading
            	render.setPixel(i, h-1-j, computeShading(closestIntersection));
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
		
		// turn i,j pixel coordinate into world coordinates
		double u,v;
		u = cam.l + (cam.r - cam.l)*(i + offset[0])/cam.imageSize.width;
		v = cam.b + (cam.t - cam.b)*(j + offset[1])/cam.imageSize.height;
		
		// calculate 's' the eye point of the ray on the viewing rectangle at the given screen coordinates
		// s = eye + screen_u * u + screen_v * v - near * w
		dummy1.scale(u, cam.u);
		dummy2.scale(v, cam.v);
		
		dummy1.add(dummy2);
		
		//dummy2.scale(distance to near plane, cam.w);
		dummy1.sub(cam.w); // dummy1 = s - eye, near = 1
		
		// calculate 'd' the direction vector
		// d = s - eye = (eye + screen_u * u + screen_v * v - focal * w) - eye
		// d = screen_u * u + screen_v * v - focal * w
		dummy1.normalize();
		
		// set the ray eye point to eye
		// set the ray direction to d
		ray.set(cam.from, dummy1);
		
	}

	
	private static Ray shadowRay = new Ray();
	private static IntersectResult shadowResult = new IntersectResult();
	
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
		
		// Set the origin of the shadow ray to be the intersection result
		// Set the direction of the shadow ray to be going to the light
		dummy1.sub(light.from, result.p);
		dummy1.normalize();
		shadowRay.set(result.p, dummy1);
		
		// Intersect the shadow with the scene
		// Find any intersection between the shadow ray origin and the light
		
		shadowResult.t = Double.POSITIVE_INFINITY;
		for (Intersectable surface : root.children) {
			surface.intersect(shadowRay, shadowResult);
			
			// check if there is an intersection
			if (shadowResult.t != Double.POSITIVE_INFINITY) {
				return true;
			}
		}
		
		// If no intersection, return false
		return false;
	}
	
	private static Color3f dummyColor = new Color3f();
	private static Color3f shaded = new Color3f();
	
	/**
	 * Computes the shading color to be called by the render.setPixel
	 * 
	 * @param result The intersection generated by a ray
	 * @return The integer compact representation of a RGB color
	 */
	public int computeShading(IntersectResult result) {
		
		shaded.set(0,0,0);
		double dot = 0;
		
		// for every light source, point lights only
		for (Light light : this.lights.values()) {
			
			// Check for shadows
			if (inShadow(result, light, this.snode, shadowResult, shadowRay))
				continue;
			
			// Lambertian diffuse
	    	// L = k * I * max(0, n dot l)
			// assume k = 1
			dummy1.sub(light.from, result.p); // l vector
			dummy1.normalize();
			dot = result.n.dot(dummy1); // n dot l
			dot = Math.max(0, dot);
			
			// if no lambertian, then no blinn-phong
			if (dot == 0) {
				continue;
			}
			
			
			// add the lambertian diffuse
			dummyColor.set(light.color.get());
			dummyColor.scale((float) dot);
			dummyColor.scale((float) light.power);
			shaded.x += dummyColor.x * result.material.diffuse.x;
			shaded.y += dummyColor.y * result.material.diffuse.y;
			shaded.z += dummyColor.z * result.material.diffuse.z;
			
			
			// Blinn-Phong specular
			// h = norm(v + l)
			// L = k * I * max(0, n dot h)^p
			dummy2.sub(render.camera.from, result.p); // v vector
			dummy1.sub(light.from, result.p); // l vector
			dummy2.add(dummy1); // v + l
			dummy2.normalize();
			dot = result.n.dot(dummy2);
			dot = Math.max(0, dot);
			dot = Math.pow(dot, result.material.shinyness);
			
			// add the specular
			dummyColor.set(light.color.get());
			dummyColor.scale((float) dot);
			dummyColor.scale((float) light.power);
			shaded.x += dummyColor.x * result.material.specular.x;
			shaded.y += dummyColor.y * result.material.specular.y;
			shaded.z += dummyColor.z * result.material.specular.z;
		}
		
		// ambient lighting
		shaded.x += this.ambient.x* result.material.diffuse.x;
		shaded.y += this.ambient.y* result.material.diffuse.y;
		shaded.z += this.ambient.z* result.material.diffuse.z;
		
		
		// max color
		shaded.clampMax(1);
		
		
		return shaded.get().getRGB();
	}
}
