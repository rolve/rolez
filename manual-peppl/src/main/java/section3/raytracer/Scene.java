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

import java.util.Vector;

public class Scene {
    public final Vector<Light> lights;
    public final Vector<Primitive> objects;
    private View view;
    
    public Scene() {
        this.lights = new Vector<Light>();
        this.objects = new Vector<Primitive>();
    }
    
    public void addLight(final Light l) {
        this.lights.addElement(l);
    }
    
    public void addObject(final Primitive object) {
        this.objects.addElement(object);
    }
    
    public void setView(final View view) {
        this.view = view;
    }
    
    public View getView() {
        return this.view;
    }
    
    public Light getLight(final int number) {
        return this.lights.elementAt(number);
    }
    
    public Primitive getObject(final int number) {
        return objects.elementAt(number);
    }
    
    public int getLights() {
        return this.lights.size();
    }
    
    public int getObjects() {
        return this.objects.size();
    }
    
    public void setObject(final Primitive object, final int pos) {
        this.objects.setElementAt(object, pos);
    }
    
    public static Scene createScene() {
        final int x = 0;
        final int y = 0;
        
        final Scene scene = new Scene();
        
        /* create spheres */
        
        Primitive p;
        final int nx = 4;
        final int ny = 4;
        final int nz = 4;
        for(int i = 0; i < nx; i++) {
            for(int j = 0; j < ny; j++) {
                for(int k = 0; k < nz; k++) {
                    final double xx = 20.0 / (nx - 1) * i - 10.0;
                    final double yy = 20.0 / (ny - 1) * j - 10.0;
                    final double zz = 20.0 / (nz - 1) * k - 10.0;
                    
                    p = new Sphere(new Vec(xx, yy, zz), 3);
                    // p.setColor(i/(double) (nx-1), j/(double)(ny-1),
                    // k/(double) (nz-1));
                    p.setColor(0, 0, (i + j) / (double) (nx + ny - 2));
                    p.mat.shine = 15.0;
                    p.mat.ks = 1.5 - 1.0;
                    p.mat.kt = 1.5 - 1.0;
                    scene.addObject(p);
                }
            }
        }
        
        /* Creates five lights for the scene */
        scene.addLight(new Light(100, 100, -50, 1.0));
        scene.addLight(new Light(-100, 100, -50, 1.0));
        scene.addLight(new Light(100, -100, -50, 1.0));
        scene.addLight(new Light(-100, -100, -50, 1.0));
        scene.addLight(new Light(200, 200, 0, 1.0));
        
        /* Creates a View (viewing point) for the rendering scene */
        final View v = new View(new Vec(x, 20, -30), new Vec(x, y, 0), new Vec(
                0, 1, 0), 1.0, 35.0 * 3.14159265 / 180.0, 1.0);
        /* v.from = new Vec(x, y, -30); v.at = new Vec(x, y, -15); v.up = new
         * Vec(0, 1, 0); v.angle = 35.0 * 3.14159265 / 180.0; v.aspect = 1.0;
         * v.dist = 1.0; */
        scene.setView(v);
        
        return scene;
    }
}
