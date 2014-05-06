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

package ch.trick17.peppl.manual.raytracer;

import ch.trick17.peppl.lib.Mutable;
import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.guard.Slice;

public class RayTracer {
    
    private final Scene scene;
    
    /**
     * Temporary ray
     */
    private final Ray tRay = new Ray();
    
    public RayTracer(final Scene scene) {
        this.scene = scene;
    }
    
    public void render(@Mutable final Slice<IntArray> image) {
        final long startTime = System.nanoTime();
        
        final int width = image.data[0].size();
        
        final Vec viewVec = Vec.sub(scene.view.at, scene.view.from);
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
        
        final Ray r = new Ray(new Vec(scene.view.from), new Vec());
        
        // Header for .ppm file
        // System.out.println("P3");
        // System.out.println(width + " " + height);
        // System.out.println("255");
        
        // All loops are reversed for 'speedup' (cf. thinking in java p331)
        // For each line
        image.guardReadWrite();
        for(int y = image.begin; y < image.end; y += image.step) {
            final double ylen = 2.0 * y / width - 1.0;
            
            image.data[y].guardReadWrite();
            // For each pixel of the line
            for(int x = 0; x < width; x++) {
                final double xlen = 2.0 * x / width - 1.0;
                r.dir = Vec.comb(xlen, leftVec, ylen, upVec);
                r.dir.add(viewVec);
                r.dir.normalize();
                
                // Compute the color of the ray
                final ImmutableVec col = trace(0, 1.0, r);
                int red = (int) (col.x * 255.0);
                if(red > 255)
                    red = 255;
                int green = (int) (col.y * 255.0);
                if(green > 255)
                    green = 255;
                int blue = (int) (col.z * 255.0);
                if(blue > 255)
                    blue = 255;
                
                // RGB values for .ppm file
                // System.out.println(red + " " + green + " " + blue);
                // Sets the pixels
                image.data[y].data[x] = 255 << 24 | red << 16 | green << 8
                        | blue;
            } // end for (x)
        } // end for (y)
        
        System.out.println("Render time: " + (System.nanoTime() - startTime)
                / 1000000);
    }
    
    private Intersection intersect(final Ray r) {
        Intersection closest = null;
        final Vec t = new Vec();
        for(final Primitive object : scene.objects) {
            final Intersection isect = object.intersect(r, t);
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
    private static Vec specularDirection(final Vec dir, final Vec normal) {
        Vec r;
        r = Vec.comb(1.0 / Math.abs(Vec.dot(dir, normal)), dir, 2.0, normal);
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
    private ImmutableVec shade(final int level, final double weight,
            final Vec p, final Vec normal, final Vec dir, final Intersection hit) {
        final Material mat = hit.mat;
        Vec r = new Vec();
        if(mat.shine > 1e-6)
            r = specularDirection(dir, normal);
        
        // Computes the effectof each light
        final Vec color = new Vec();
        final Vec temp = new Vec();
        for(int l = 0; l < scene.lights.length(); l++) {
            temp.sub2(scene.lights.data[l].pos, p);
            if(Vec.dot(normal, temp) >= 0.0) {
                temp.normalize();
                
                tRay.origin = p;
                tRay.dir = temp;
                
                // Checks if there is a shadow
                if(shadow(tRay)) {
                    final double diff = Vec.dot(normal, temp) * mat.kd
                            * scene.lights.data[l].brightness;
                    color.adds(diff, mat.color);
                    if(mat.shine > 1e-6) {
                        double spec = Vec.dot(r, temp);
                        if(spec > 1e-6) {
                            spec = Math.pow(spec, mat.shine);
                            color.x += spec;
                            color.y += spec;
                            color.z += spec;
                        }
                    }
                }
            } // if
        } // for
        
        ImmutableVec tcol;
        tRay.origin = p;
        if(mat.ks * weight > 1e-3) {
            tRay.dir = specularDirection(dir, normal);
            tcol = trace(level + 1, mat.ks * weight, tRay);
            color.adds(mat.ks, tcol);
        }
        if(mat.kt * weight > 1e-3) {
            if(hit.enter > 0)
                tRay.dir = transDir(null, mat, dir, normal);
            else
                tRay.dir = transDir(mat, null, dir, normal);
            tcol = trace(level + 1, mat.kt * weight, tRay);
            color.adds(mat.kt, tcol);
        }
        return color.immutable();
    }
    
    /**
     * Launches a ray
     */
    private ImmutableVec trace(final int level, final double weight, final Ray r) {
        // Checks the recursion level
        if(level > 6)
            return ImmutableVec.O;
        
        Vec p, n;
        final Intersection isect = intersect(r);
        if(isect != null) {
            p = r.point(isect.t);
            n = isect.prim.normal(p);
            if(Vec.dot(r.dir, n) >= 0.0) {
                n.negate();
            }
            return shade(level, weight, p, n, r.dir, isect);
        }
        // no intersection --> col = 0,0,0
        return ImmutableVec.O;
    }
}
