package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3d;

import com.sun.prism.paint.Color;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {
    
	/** If many mirror surfaces are visible, this determines how many bounces there 
	 * can be from a mirror surface. 0 means no mirror at all. */
    private static final int MAX_BOUNCES = 1;

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
        
        // Prepare geometry
        for (Intersectable surface: this.surfaceList) surface.prepare();
        
        // Prepare threads
        // Initialize thread array
        int cores = Runtime.getRuntime().availableProcessors(); // as many threads as cores
        //int cores = 4;
        RunnableRayTracer[] threads = new RunnableRayTracer[cores];
        
        // Partition the work to the threads
        // simply partition according to the vertical pixel coordinate
        for(int i = 0; i < cores; i++) {
        	threads[i] = new RunnableRayTracer(0, w, i*h/cores, (i+1)*h/cores, this);
        }
        
        // Start threads
        for (RunnableRayTracer r: threads) r.start();
        
       
        
        // wait for render viewer to close
        render.waitDone();
        
        // save the final render image
        render.save();
    }
    
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
		Vector3d dummy1 = new Vector3d();
	    Vector3d dummy2 = new Vector3d();
	    
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
		Vector3d direction = new Vector3d();
		
			
		// Set the origin of the shadow ray to be the intersection result
		// Set the direction of the shadow ray to be going to the light
		direction.sub(light.from, result.p);
		direction.normalize();
		shadowRay.set(result.p, direction);
		
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
	
	/**
	 * Computes the shading color to be called by the render.setPixel
	 * 
	 * @param result The intersection generated by a ray
	 * @param bounceNumber How many reflections have occurred before from the current ray
	 * @return A copy of the shaded color
	 */
	public Color3f computeShading(IntersectResult result, int bounceNumber) {
		
		Color3f dummyColor = new Color3f();
		Color3f shaded = new Color3f();
		double dot = 0;
		Ray shadowRay = new Ray();
		IntersectResult shadowResult = new IntersectResult();
		
		Vector3d dummy1 = new Vector3d();
		Vector3d dummy2 = new Vector3d();
		
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
			
			// mirror reflection
			if (bounceNumber < MAX_BOUNCES && result.material.mirror.x != 0
					&& result.material.mirror.y != 0 && result.material.mirror.z != 0) {	
				// get reflection color
				dummyColor.set(mirrorReflection(result, bounceNumber));
				dummyColor.scale((float) 0.0000001);
				shaded.x += dummyColor.x * result.material.mirror.x;
				shaded.y += dummyColor.y * result.material.mirror.y;
				shaded.z += dummyColor.z * result.material.mirror.z;
			}
		}
		
		// ambient lighting
		shaded.x += this.ambient.x* result.material.diffuse.x;
		shaded.y += this.ambient.y* result.material.diffuse.y;
		shaded.z += this.ambient.z* result.material.diffuse.z;
		
		
		// max color
		shaded.clampMax(1);
		
		
		return (Color3f) shaded.clone();
	}
	
	
	/**
	 * Mirror reflection calculation
	 * @param result
	 * @return Additional mirror color
	 */
	private Color3f mirrorReflection(IntersectResult result, int bounceNumber) {
		
		Matrix4d reflection = new Matrix4d();
		
		if (bounceNumber >= MAX_BOUNCES) {
			return new Color3f(0,0,0);
		}
		
		// reflection = 2 n n^T - I 
		reflection.m00 = 2*result.n.x*result.n.x - 1;
		reflection.m01 = 2*result.n.x*result.n.y;
		reflection.m02 = 2*result.n.x*result.n.z;
		reflection.m10 = reflection.m01;
		reflection.m11 = 2*result.n.y*result.n.y - 1;
		reflection.m12 = 2*result.n.y*result.n.z;
		reflection.m20 = reflection.m02;
		reflection.m21 = reflection.m12;
		reflection.m22 = 2*result.n.z*result.n.z - 1;
		
		Ray reflected = new Ray();
		reflected.eyePoint.set(result.p);
		reflected.viewDirection = new Vector3d();
		reflected.viewDirection.sub(this.render.camera.from, result.p);
		reflected.viewDirection.normalize();
		// apply reflection transformation
		reflection.transform(reflected.viewDirection);
		
		// get the closest intersection
		double tBest = Double.POSITIVE_INFINITY;
		IntersectResult closestIntersection = null;
		for (Intersectable surface : this.surfaceList) {
			surface.intersect(reflected, result);
			
			// a closer intersection
			if (result.t < tBest) {
				tBest = result.t;
				closestIntersection = new IntersectResult(result);
			}
		}
		
		// an intersection is found
		if (tBest < Double.POSITIVE_INFINITY) {
			Color3f reflectedColor = computeShading(closestIntersection, bounceNumber + 1);
			
			// do phong shading
			// L = k color max(0, r dot v)
			
			return reflectedColor;
		}
		
		return new Color3f(0,0,0);
	}
}
