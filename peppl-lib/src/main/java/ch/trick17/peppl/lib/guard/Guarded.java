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
            guard.guardRead(this);
    }
    
    public void guardReadWrite() {
        if(guard != null)
            guard.guardReadWrite(this);
    }
    
    final void processGuardedRefs(final GuardOp op, final Set<Guarded> processed) {
        if(processed.add(this)) {
            /* FIRST process references */
            for(final Guarded ref : guardedRefs())
                if(ref != null)
                    ref.processGuardedRefs(op, processed);
            
            /* Process "this" */
            op.process(this);
        }
    }
    
    final void processViews(final GuardOp op, final Set<Guarded> processed) {
        // Same as processViewsRecursive, except "this" is not processed
        for(final Guarded view : views())
            if(view != null)
                view.processViewsRecursive(op, processed);
    }
    
    private void processViewsRecursive(final GuardOp op,
            final Set<Guarded> processed) {
        if(processed.add(this)) {
            for(final Guarded view : views())
                if(view != null)
                    view.processViewsRecursive(op, processed);
            
            op.process(this);
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
    
    /**
     * Returns all views of this object. Views are (guarded) objects that
     * provide access to a subset of the data of the object they belong to.
     * 
     * @return All views of this object. To simplify the implementation of this
     *         method, the {@link Iterable} may return <code>null</code>
     *         references.
     */
    abstract Iterable<? extends Guarded> views();
}
