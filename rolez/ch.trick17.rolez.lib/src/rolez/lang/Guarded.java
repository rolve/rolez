package rolez.lang;

import static java.util.Collections.emptyList;

import java.util.Set;

/**
 * Superclass of all guarded objects
 * 
 * @author Michael Faes
 */
public abstract class Guarded {
    
    private Guard guard;
    final Object viewLock = new Object();
    
    final Guard getGuard() {
        if(guard == null)
            guard = new Guard();
        return guard;
    }
    
    final void processAll(final Guard.Op op, final Set<Guarded> processed,
            final boolean lockViews) {
        if(processed.add(this)) {
            /* First, process references, otherwise "parent" task may replace
             * them through a view that has already been released. */
            for(final Object ref : guardedRefs())
                if(ref instanceof Guarded)
                    ((Guarded) ref).processAll(op, processed, lockViews);
                    
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
    
    final void processViews(final Guard.Op op, final Set<Guarded> processed) {
        // Same as processViewsRecursive, except "this" is not processed
        for(final Guarded view : views())
            if(view != null)
                view.processViewsRecursive(op, processed);
    }
    
    private void processViewsRecursive(final Guard.Op op,
            final Set<Guarded> processed) {
        if(processed.add(this)) {
            for(final Guarded view : views())
                if(view != null)
                    view.processViewsRecursive(op, processed);
                    
            op.process(this);
        }
    }
    
    /**
     * Returns all references to mutable and therefore guarded objects that are
     * reachable from this. This may exclude internal objects of the Rolez
     * library.
     * <p>
     * This implementation returns an empty iterable.
     * 
     * @return All references to mutable objects reachable from this. To
     *         simplify the implementation of this method, the {@link Iterable}
     *         may return references to non-guarded objects or even
     *         <code>null</code>s.
     */
    protected Iterable<?> guardedRefs() {
        return emptyList();
    }
    
    /**
     * Returns all views of this object. Views are (guarded) objects that
     * provide access to a subset of the data of the object they belong to.
     * <p>
     * This implementation returns an empty iterable.
     * 
     * @return All views of this object. To simplify the implementation of this
     *         method, the {@link Iterable} may return <code>null</code>
     *         references.
     */
    protected Iterable<? extends Guarded> views() {
        return emptyList();
    }
    
    /* Role transition methods */
    
    public final void share() {
        getGuard().share(this);
    }
    
    public final void pass() {
        getGuard().pass(this);
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
    
    /* Guarding methods. These are static so that they can return the guarded
     * object with the precise type (which is not possible with instance
     * methods, due to the lack of self types). This simplifies code generation
     * a lot, since guarding can be done within an expression. */
    
    public static <G extends Guarded> G guardReadOnly(G guarded) {
        if(((Guarded) guarded).guard != null)
            ((Guarded) guarded).guard.guardReadOnly(guarded);
        return guarded;
    }
    
    public static <G extends Guarded> G guardReadWrite(G guarded) {
        if(((Guarded) guarded).guard != null)
            ((Guarded) guarded).guard.guardReadWrite(guarded);
        return guarded;
    }
}
