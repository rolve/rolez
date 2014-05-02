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

import ch.trick17.peppl.lib.immutable.ImmutableObject;

public abstract class Primitive extends ImmutableObject {
    
    public final Material mat;
    
    public Primitive(final Material mat) {
        this.mat = mat;
    }
    
    public abstract Vec normal(Vec pnt);
    
    public abstract Intersection intersect(Ray ry, Vec temp);
    
    @Override
    public abstract String toString();
    
    public abstract ImmutableVec getCenter();
}
