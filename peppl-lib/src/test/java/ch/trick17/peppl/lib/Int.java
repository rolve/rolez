package ch.trick17.peppl.lib;

import ch.trick17.peppl.lib.guard.GuardedObject;

class Int extends GuardedObject {
    int value;
    
    public Int() {}
    
    public Int(final int value) {
        this.value = value;
    }
}
