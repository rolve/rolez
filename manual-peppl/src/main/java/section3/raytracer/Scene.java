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

import java.util.ArrayList;
import java.util.List;

public class Scene {
    
    public final Light[] lights;
    public final Primitive[] objects;
    public final View view;
    
    public Scene(final Light[] lights, final Primitive[] objects,
            final View view) {
        this.lights = lights;
        this.objects = objects;
        this.view = view;
    }
    
    public View getView() {
        return this.view;
    }
    
    public static Scene createScene() {
        final int x = 0;
        final int y = 0;
        
        /* create spheres */
        final List<Primitive> objects = new ArrayList<>();
        final int nx = 4;
        final int ny = 4;
        final int nz = 4;
        for(int i = 0; i < nx; i++) {
            for(int j = 0; j < ny; j++) {
                for(int k = 0; k < nz; k++) {
                    final double xx = 20.0 / (nx - 1) * i - 10.0;
                    final double yy = 20.0 / (ny - 1) * j - 10.0;
                    final double zz = 20.0 / (nz - 1) * k - 10.0;
                    
                    final ImmutableVec color = new ImmutableVec(0, 0, (i + j)
                            / (double) (nx + ny - 2));
                    final Material mat = new Material(color, 15.0, 1.5 - 1.0,
                            1.5 - 1.0);
                    final Primitive p = new Sphere(mat, new ImmutableVec(xx,
                            yy, zz), 3);
                    objects.add(p);
                }
            }
        }
        
        /* Creates five lights for the scene */
        final List<Light> lights = new ArrayList<Light>();
        lights.add(new Light(100, 100, -50, 1.0));
        lights.add(new Light(-100, 100, -50, 1.0));
        lights.add(new Light(100, -100, -50, 1.0));
        lights.add(new Light(-100, -100, -50, 1.0));
        lights.add(new Light(200, 200, 0, 1.0));
        
        /* Creates a View (viewing point) for the rendering scene */
        final View v = new View(new ImmutableVec(x, 20, -30), new ImmutableVec(
                x, y, 0), new ImmutableVec(0, 1, 0), 1.0,
                35.0 * 3.14159265 / 180.0, 1.0);
        /* v.from = new Vec(x, y, -30); v.at = new Vec(x, y, -15); v.up = new
         * Vec(0, 1, 0); v.angle = 35.0 * 3.14159265 / 180.0; v.aspect = 1.0;
         * v.dist = 1.0; */
        
        return new Scene(lights.toArray(new Light[lights.size()]), objects
                .toArray(new Primitive[objects.size()]), v);
    }
}
