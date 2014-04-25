/**************************************************************************
 *                                                                         *
 *         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
 *                                                                         *
 *                            produced by                                  *
 *                                                                         *
 *                  Java Grande Benchmarking Project                       *
 *                                                                         *
 *                                at                                       *
 *                                                                         *
 *                Edinburgh Parallel Computing Centre                      *
 *                                                                         *
 *                email: epcc-javagrande@epcc.ed.ac.uk                     *
 *                                                                         *
 *                 Original version of this code by                        *
 *            Florian Doyon (Florian.Doyon@sophia.inria.fr)                *
 *              and  Wilfried Klauser (wklauser@acm.org)                   *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 2001.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

package section3.raytracer;

public class RayTracer {
    
    protected final Scene scene;
    
    /**
     * Alpha channel
     */
    static final int alpha = 255 << 24;
    
    /**
     * Null vector (for speedup, instead of <code>new Vec(0,0,0)</code>
     */
    static final Vec voidVec = new Vec();
    
    /**
     * Temporary vect
     */
    private final Vec L = new Vec();
    
    /**
     * Temporary ray
     */
    private final Ray tRay = new Ray();
    
    public RayTracer(final Scene scene) {
        this.scene = scene;
    }
    
    public long render(final Interval interval, final int nthreads) {
        
        // Screen variables
        final int row[] = new int[interval.width
                * (interval.yto - interval.yfrom)];
        
        // Rendering variables
        int x, y, red, green, blue;
        double xlen, ylen;
        Vec viewVec;
        
        viewVec = Vec.sub(scene.view.at, scene.view.from);
        
        viewVec.normalize();
        
        final Vec tmpVec = new Vec(viewVec);
        tmpVec.scale(Vec.dot(scene.view.up, viewVec));
        
        final Vec upVec = Vec.sub(scene.view.up, tmpVec);
        upVec.normalize();
        
        final Vec leftVec = Vec.cross(scene.view.up, viewVec);
        leftVec.normalize();
        
        final double frustrumwidth = scene.view.dist
                * Math.tan(scene.view.angle);
        
        upVec.scale(-frustrumwidth);
        leftVec.scale(scene.view.aspect * frustrumwidth);
        
        final Ray r = new Ray(scene.view.from, voidVec);
        Vec col = new Vec();
        
        // Header for .ppm file
        // System.out.println("P3");
        // System.out.println(width + " " + height);
        // System.out.println("255");
        
        // All loops are reversed for 'speedup' (cf. thinking in java p331)
        
        // For each line
        int pixCounter = 0;
        long checksum = 0;
        for(y = interval.yfrom + interval.threadid; y < interval.yto; y += nthreads) {
            
            ylen = 2.0 * y / interval.width - 1.0;
            // System.out.println("Doing line " + y);
            // For each pixel of the line
            for(x = 0; x < interval.width; x++) {
                xlen = 2.0 * x / interval.width - 1.0;
                r.dir = Vec.comb(xlen, leftVec, ylen, upVec);
                r.dir.add(viewVec);
                r.dir.normalize();
                col = trace(0, 1.0, r);
                
                // computes the color of the ray
                red = (int) (col.x * 255.0);
                if(red > 255)
                    red = 255;
                green = (int) (col.y * 255.0);
                if(green > 255)
                    green = 255;
                blue = (int) (col.z * 255.0);
                if(blue > 255)
                    blue = 255;
                
                checksum += red;
                checksum += green;
                checksum += blue;
                
                // RGB values for .ppm file
                // System.out.println(red + " " + green + " " + blue);
                // Sets the pixels
                row[pixCounter++] = alpha | (red << 16) | (green << 8) | (blue);
            } // end for (x)
        } // end for (y)
        return checksum;
    }
    
    private Intersection intersect(final Ray r) {
        Intersection closest = null;
        for(final Primitive object : scene.objects) {
            final Intersection isect = object.intersect(r);
            if(isect != null && (closest == null || isect.t < closest.t))
                closest = isect;
        }
        return closest;
    }
    
    /**
     * Checks if there is a shadow
     * 
     * @param r
     *            The ray
     * @return Returns 1 if there is a shadow, 0 if there isn't
     */
    private boolean shadow(final Ray r) {
        return intersect(r) == null;
    }
    
    /**
     * Return the Vector's reflection direction
     * 
     * @return The specular direction
     */
    private static Vec specularDirection(final Vec I, final Vec N) {
        Vec r;
        r = Vec.comb(1.0 / Math.abs(Vec.dot(I, N)), I, 2.0, N);
        r.normalize();
        return r;
    }
    
    /**
     * Return the Vector's transmission direction
     */
    private static Vec transDir(final Material m1, final Material m2,
            final Vec I, final Vec N) {
        double n1, n2, eta, c1, cs2;
        Vec r;
        n1 = m1 == null ? 1.0 : m1.ior;
        n2 = m2 == null ? 1.0 : m2.ior;
        eta = n1 / n2;
        c1 = -Vec.dot(I, N);
        cs2 = 1.0 - eta * eta * (1.0 - c1 * c1);
        if(cs2 < 0.0)
            return null;
        r = Vec.comb(eta, I, eta * c1 - Math.sqrt(cs2), N);
        r.normalize();
        return r;
    }
    
    /**
     * Returns the shaded color
     * 
     * @return The color in Vec form (rgb)
     */
    private Vec shade(final int level, final double weight, final Vec P,
            final Vec N, final Vec I, final Intersection hit) {
        Vec tcol;
        Vec r;
        double diff, spec;
        Material mat;
        Vec col;
        int l;
        
        col = new Vec();
        mat = hit.mat;
        r = new Vec();
        if(mat.shine > 1e-6) {
            r = specularDirection(I, N);
        }
        
        // Computes the effectof each light
        for(l = 0; l < scene.lights.length; l++) {
            L.sub2(scene.lights[l].pos, P);
            if(Vec.dot(N, L) >= 0.0) {
                L.normalize();
                
                tRay.origin = P;
                tRay.dir = L;
                
                // Checks if there is a shadow
                if(shadow(tRay)) {
                    diff = Vec.dot(N, L) * mat.kd * scene.lights[l].brightness;
                    
                    col.adds(diff, mat.color);
                    if(mat.shine > 1e-6) {
                        spec = Vec.dot(r, L);
                        if(spec > 1e-6) {
                            spec = Math.pow(spec, mat.shine);
                            col.x += spec;
                            col.y += spec;
                            col.z += spec;
                        }
                    }
                }
            } // if
        } // for
        
        tRay.origin = P;
        if(mat.ks * weight > 1e-3) {
            tRay.dir = specularDirection(I, N);
            tcol = trace(level + 1, mat.ks * weight, tRay);
            col.adds(mat.ks, tcol);
        }
        if(mat.kt * weight > 1e-3) {
            if(hit.enter > 0)
                tRay.dir = transDir(null, mat, I, N);
            else
                tRay.dir = transDir(mat, null, I, N);
            tcol = trace(level + 1, mat.kt * weight, tRay);
            col.adds(mat.kt, tcol);
        }
        return col;
    }
    
    /**
     * Launches a ray
     */
    private Vec trace(final int level, final double weight, final Ray r) {
        Vec P, N;
        
        // Checks the recursion level
        if(level > 6) {
            return new Vec();
        }
        
        final Intersection isect = intersect(r);
        if(isect != null) {
            P = r.point(isect.t);
            N = isect.prim.normal(P);
            if(Vec.dot(r.dir, N) >= 0.0) {
                N.negate();
            }
            return shade(level, weight, P, N, r.dir, isect);
        }
        // no intersection --> col = 0,0,0
        return voidVec;
    }
    
    public static void main(final String argv[]) {
        final RayTracer rt = new RayTracer(Scene.createScene());
        
        // Set interval to be rendered to the whole picture
        // (overkill, but will be useful to retain this for parallel versions)
        final Interval interval = new Interval(100, 100, 0, 100, 0);
        
        // Do the business!
        rt.render(interval, 1);
    }
}
