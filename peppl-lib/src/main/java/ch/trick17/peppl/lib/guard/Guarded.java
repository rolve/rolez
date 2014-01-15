package ch.trick17.peppl.lib.guard;

import java.util.List;
import java.util.Set;

abstract class Guarded {
    
    private volatile Guard guard;
    
    public Guarded() {
        super();
    }
    
    public final void share() {
        getGuard().share(this);
    }
    
    public final void pass() {
        getGuard().pass(this);
    }
    
    protected Guard getGuard() {
        if(guard == null)
            guard = GuardFactory.getDefault().newGuard();
        return guard;
    }
    
    public final void registerNewOwner() {
        assert guard != null;
        guard.registerNewOwner(this);
    }
    
    public final void releaseShared() {
        assert guard != null;
        guard.releaseShared(this);
    }
    
    public final void releasePassed() {
        assert guard != null;
        guard.releasePassed(this);
    }
    
    public final void guardRead() {
        if(guard != null)
            guard.guardRead();
    }
    
    public final void guardReadWrite() {
        if(guard != null)
            guard.guardReadWrite();
    }
    
    protected void processRecursively(final Op op, final Set<Guarded> processed) {
        if(processed.add(this)) {
            /* Process current object */
            op.process(getGuard());
            
            /* Process references */
            final List<?> refs = allRefs();
            for(final Object ref : refs)
                if(ref != null) {
                    assert ref instanceof GuardedObject;
                    final GuardedObject other = (GuardedObject) ref;
                    other.processRecursively(op, processed);
                }
        }
    }
    
    abstract List<?> allRefs();
}
