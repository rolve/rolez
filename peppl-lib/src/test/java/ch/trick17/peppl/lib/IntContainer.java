package ch.trick17.peppl.lib;

import ch.trick17.peppl.lib.guard.GuardedObject;

class IntContainer extends GuardedObject {
    Int i;
    
    public IntContainer() {
        i = new Int();
    }
    
    public IntContainer(final Int i) {
        this.i = i;
    }
}
