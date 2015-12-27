package rolez.lang;

import java.util.Set;

/**
 * Superclass of all guarded objects
 * 
 * @author Michael Faes
 * @param <S>
 *            The self type
 */
public abstract class Guarded<S extends Guarded<S>> {
    
    private Guard guard;
    
    final Object viewLock = new Object();
    
    @SuppressWarnings("unchecked")
    public final S share() {
        getGuard().share(this);
        return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public final S pass() {
        getGuard().pass(this);
        return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public final S registerNewOwner() {
        assert guard != null;
        guard.registerNewOwner(this);
        return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public final S releaseShared() {
        assert guard != null;
        guard.releaseShared(this);
        return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public final S releasePassed() {
        assert guard != null;
        guard.releasePassed(this);
        return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public final S guardReadOnly() {
        if(guard != null)
            guard.guardReadOnly(this);
        return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public final S guardReadWrite() {
        if(guard != null)
            guard.guardReadWrite(this);
        return (S) this;
    }
    
    final Guard getGuard() {
        if(guard == null)
            guard = new Guard();
        return guard;
    }
    
    final void processAll(final Guard.Op op, final Set<Guarded<?>> processed,
            final boolean lockViews) {
        if(processed.add(this)) {
            /* First, process references, otherwise "parent" task may replace
             * them through a view that has already been released. */
            for(final Guarded<?> ref : guardedRefs())
                if(ref != null)
                    ref.processAll(op, processed, lockViews);
                    
            /* Then, process views and finally "this". If necessary, make sure
             * that no new views are added during this phase using the viewLock. */
            if(lockViews) {
                synchronized(viewLock) {
                    processViews(op, processed);
                    op.process(this);
                }
            }
            else {
                processViews(op, processed);
                op.process(this);
            }
        }
    }
    
    final void processViews(final Guard.Op op,
            final Set<Guarded<?>> processed) {
        // Same as processViewsRecursive, except "this" is not processed
        for(final Guarded<?> view : views())
            if(view != null)
                view.processViewsRecursive(op, processed);
    }
    
    private void processViewsRecursive(final Guard.Op op,
            final Set<Guarded<?>> processed) {
        if(processed.add(this)) {
            for(final Guarded<?> view : views())
                if(view != null)
                    view.processViewsRecursive(op, processed);
                    
            op.process(this);
        }
    }
    
    /**
     * Returns all references to mutable and therefore guarded objects that are
     * reachable from this. This may exclude internal objects of the Rolez
     * library.
     * 
     * @return All references to mutable objects reachable from this. To
     *         simplify the implementation of this method, the {@link Iterable}
     *         may return <code>null</code> references.
     */
    protected abstract Iterable<? extends Guarded<?>> guardedRefs();
    
    /**
     * Returns all views of this object. Views are (guarded) objects that
     * provide access to a subset of the data of the object they belong to.
     * 
     * @return All views of this object. To simplify the implementation of this
     *         method, the {@link Iterable} may return <code>null</code>
     *         references.
     */
    protected abstract Iterable<? extends Guarded<?>> views();
}
