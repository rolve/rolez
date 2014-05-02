package ch.trick17.peppl.lib.guard;

import java.util.Set;

import ch.trick17.peppl.lib.immutable.Immutable;

public abstract class Guarded {
    
    private volatile Guard guard; // IMPROVE: volatile necessary? Task system
                                  // should guarantee happens-before.
    
    Guarded() {}
    
    public void share() {
        getGuard().share(this);
    }
    
    public void pass() {
        getGuard().pass(this);
    }
    
    final Guard getGuard() {
        if(guard == null)
            guard = GuardFactory.getDefault().newGuard();
        return guard;
    }
    
    public void registerNewOwner() {
        assert guard != null;
        guard.registerNewOwner(this);
    }
    
    public void releaseShared() {
        assert guard != null;
        guard.releaseShared(this);
    }
    
    public void releasePassed() {
        assert guard != null;
        guard.releasePassed(this);
    }
    
    public void guardRead() {
        if(guard != null)
            guard.guardRead();
    }
    
    public void guardReadWrite() {
        if(guard != null)
            guard.guardReadWrite();
    }
    
    final void processRecursively(final GuardOp op, final Set<Guarded> processed) {
        if(processed.add(this)) {
            /* Process current object */
            op.process(getGuard());
            
            /* Process references */
            for(final Guarded ref : guardedRefs())
                if(ref != null)
                    ref.processRecursively(op, processed);
        }
    }
    
    /**
     * Returns all references to {@linkplain Immutable#isImmutable(Class)
     * mutable} and therefore guarded objects that are reachable from this. This
     * may exclude internal objects of the PEPPL library.
     * 
     * @return All references to mutable objects reachable from this. To
     *         simplify the implementation of this method, the {@link Iterable}
     *         may return <code>null</code> references.
     */
    abstract Iterable<? extends Guarded> guardedRefs();
}
