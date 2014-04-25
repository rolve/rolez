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

public class Material {
    public final ImmutableVec color;
    public final double kd;
    public final double ks;
    public final double shine;
    public final double kt;
    public final double ior;
    
    public Material(final ImmutableVec color, final double shine, final double ks,
            final double kt) {
        this.color = color;
        this.shine = shine;
        this.ks = ks;
        this.kt = kt;
        
        kd = 1.0;
        ior = 1.0;
    }
    
    @Override
    public String toString() {
        return "Material { color=" + color + "}";
    }
}
