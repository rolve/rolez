package ch.trick17.peppl.lib.guard;

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
    
    protected final Guard getGuard() {
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
    
    final void processRecursively(final GuardOp op, final Set<Guarded> processed) {
        if(processed.add(this)) {
            /* Process current object */
            op.process(getGuard());
            
            /* Process references */
            for(final Guarded ref : allRefs())
                if(ref != null)
                    ref.processRecursively(op, processed);
        }
    }
    
    abstract Iterable<? extends Guarded> allRefs();
}
