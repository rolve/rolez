package rolez.lang;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.Set;

import rolez.lang.Guard.Op;

/**
 * Superclass of all guarded objects
 * 
 * @author Michael Faes
 */
public abstract class Guarded {
    
    Guard guard;
    
    final Guard getGuard() {
        if(guard == null)
            guard = new Guard();
        return guard;
    }
    
    final void processAll(Op op, Set<Guarded> processed) {
        if(processed.add(this)) {
            /* First, process references, otherwise "parent" task may replace them */
            for(final Object ref : guardedRefs())
                if(ref instanceof Guarded)
                    ((Guarded) ref).processAll(op, processed);
            op.process(this);
        }
    }
    
    /**
     * Returns all references to mutable and therefore guarded objects that are reachable from this.
     * This may exclude internal objects of the Rolez library.
     * <p>
     * This implementation returns an empty iterable.
     * 
     * @return All references to mutable objects reachable from this. To simplify the implementation
     *         of this method, the {@link Iterable} may return references to non-guarded objects or
     *         even <code>null</code>s.
     */
    protected Iterable<?> guardedRefs() {
        return emptyList();
    }
    
    /**
     * Returns all other views of this object, i.e., objects that provide access to (some of) the
     * data that this object provides access to. Note that the view relation is symmetric: iff A is
     * a view of B, B is a view of A.
     * <p>
     * This implementation returns an empty collection, meaning that by default, guarded objects
     * don't have any other views.
     * 
     * @return All other views of this object. To simplify the implementation of this method, the
     *         {@link Collection} may contain <code>null</code> references.
     */
    protected Collection<? extends Guarded> views() {
        return emptyList();
    }
    
    /**
     * Returns the lock that should be used to synchronize access to the {@linkplain #views() views}
     * . This can be any object, as long as it is the same for all views of some data. It goes
     * without saying that this object must not be locked for any other purpose.
     * <p>
     * If this object cannot have any other views, <code>null</code> should be returned.
     * <p>
     * The default implementation returns <code>null</code>.
     */
    protected Object viewLock() {
        return null;
    }
    
    /* Role transition methods */
    
    public final void share(Task<?> task) {
        getGuard().share(this, task);
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
    
    /* Guarding methods. These are static so that they can return the guarded object with the
     * precise type (which is not possible with instance methods, due to the lack of self types).
     * This simplifies code generation a lot, since guarding can be done within an expression. */
    
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
    
    /* The following two are required for expressions of type java.lang.Object, for which it is only
     * known at runtime whether guarding is needed */
    
    public static <G> G guardReadOnlyIfNeeded(G guarded) {
        if(guarded instanceof Guarded)
            guardReadOnly((Guarded) guarded);
        return guarded;
    }
    
    public static <G> G guardReadWriteIfNeeded(G guarded) {
        if(guarded instanceof Guarded)
            guardReadWrite((Guarded) guarded);
        return guarded;
    }
}
