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

public class View extends ImmutableObject {
    
    public final ImmutableVec from;
    public final ImmutableVec at;
    public final ImmutableVec up;
    public final double dist;
    public final double angle;
    public final double aspect;
    
    public View(final ImmutableVec from, final ImmutableVec at,
            final ImmutableVec up, final double dist, final double angle,
            final double aspect) {
        this.from = from;
        this.at = at;
        this.up = up;
        this.dist = dist;
        this.angle = angle;
        this.aspect = aspect;
    }
}
