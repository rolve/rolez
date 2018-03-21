package rolez.lang;

import static java.util.Collections.emptyList;
import static java.util.Collections.newSetFromMap;
import static java.util.concurrent.locks.LockSupport.park;
import static rolez.lang.Task.currentTask;
import static rolez.lang.Task.idBitsFor;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Superclass of all guarded objects
 * 
 * @author Michael Faes
 */
public abstract class Guarded {
    
    // IMPROVE: Create separate, optimized class for objects without views?
    
    private boolean guardingDisabled = false;
    
    private volatile int ownerId = -1;
    
    private AtomicInteger sharedCount; // atomic because tasks can share concurrently
    private Set<Task<?>> readers;
    
    private Object guardingCachesLock; // IMPROVE: replace with CAS using Java 9's VarHandles?
    private volatile long readGuardingCache;
    private volatile long writeGuardingCache;
    
    /**
     * Default constructor that does not initialize guarding. Initialization happens lazily, when
     * the object first changes its role. This can improve performance significantly, because any
     * guarding op returns immediately if guarding is not initialized.
     */
    protected Guarded() {}
    
    /**
     * Constructor that <strong>does</strong> initialize guarding, if the given argument is
     * <code>true</code>. Required for object with views, because they can change their effective
     * role without being passed or shared themselves, so lazy initialization doesn't work for them.
     */
    protected Guarded(boolean initializeGuarding) {
        if(initializeGuarding)
            ensureGuardingInitialized(currentTask().id);
    }
    
    private boolean guardingInitialized() {
        return ownerId >= 0;
    }
    
    /**
     * Initializes the guarding "infrastructure", if guarding is not
     * {@linkplain #disableGuarding(Guarded) disabled}.
     */
    protected final void ensureGuardingInitialized(int ownerTaskId) {
        assert !guardingDisabled;
        if(!guardingInitialized()) {
            ownerId = ownerTaskId;
            sharedCount = new AtomicInteger(0);
            // IMPROVE: Initialize only when first used?
            readers = newSetFromMap(new ConcurrentHashMap<Task<?>, java.lang.Boolean>());
            guardingCachesLock = new Object();
        }
    }
    
    // TODO: Test disabling guarding
    
    /**
     * Disables guarded object and all objects it (transitively) references. This is used for
     * objects that are reachable from global object (singleton classes).
     */
    public static <G extends Guarded> G disableGuarding(G g) {
        ((Guarded) g).disableGuarding();
        return g;
    }
    
    private void disableGuarding() {
        guardingDisabled = true;
        if(guardingInitialized()) {
            /* In case guarding has been initialized eagerly (for objects with views), this will
             * "uninitialize" it again */
            ownerId = -1;
            sharedCount = null;
            readers = null;
        }
        
        for(Object g : guardedRefs())
            if(g instanceof Guarded)
                ((Guarded) g).disableGuarding();
    }
    
    /* Role transitions */
    
    final void pass(Task<?> task) {
        if(!guardingDisabled) {
            ensureGuardingInitialized(task.id);
            ownerId = task.id;
            invalidateGuardingCaches();
        }
    }
    
    final void share(Task<?> task) {
        if(!guardingDisabled) {
            ensureGuardingInitialized(task.parent.id);
            sharedCount.incrementAndGet();
            if(viewLock() != null)
                readers.add(task);
            invalidateGuardingCaches();
        }
    }
    
    final void releaseShared() {
        if(!guardingDisabled) {
            sharedCount.decrementAndGet();
            if(viewLock() != null) {
                boolean removed = readers.remove(currentTask());
                assert removed;
            }
        }
        // TODO: notify tasks that wait for other views?
    }
    
    final void releasePassed(Task<?> newOwner) {
        if(!guardingDisabled) {
            ensureGuardingInitialized(newOwner.id);
            ownerId = newOwner.id;
        }
        // TODO: notify tasks that wait for other views?
    }
    
    /* Guarding methods. The static versions can return the guarded object with the precise type
     * (which is not possible with instance methods, due to the lack of self types). This simplifies
     * code generation a lot, since guarding can be done within an expression. */
    
    // TODO: Remove this version at some point
    public static <G extends Guarded> G guardReadOnly(G guarded) {
        ((Guarded) guarded).guardReadOnly(currentTask().idBits());
        return guarded;
    }
    
    public static <G extends Guarded> G guardReadOnly(G guarded, long currentTaskIdBits) {
        ((Guarded) guarded).guardReadOnly(currentTaskIdBits);
        return guarded;
    }
    
    public final void guardReadOnlyReachable(Set<Guarded> processed, long currentTaskIdBits) {
        if(processed.add(this)) {
            guardReadOnly(currentTaskIdBits);
            for(Object g : guardedRefs())
                if(g instanceof Guarded)
                    ((Guarded) g).guardReadOnlyReachable(processed, currentTaskIdBits);
        }
    }
    
    private final void guardReadOnly(long currentTaskIdBits) {
        if(!guardingInitialized() || alreadyReadOnlyGuardedIn(currentTaskIdBits))
            return;
        
        while(!mayRead(currentTaskIdBits))
            park();
    }
    
    // TODO: Remove this version at some point
    public static <G extends Guarded> G guardReadWrite(G guarded) {
        ((Guarded) guarded).guardReadWrite(currentTask().idBits());
        return guarded;
    }
    
    public static <G extends Guarded> G guardReadWrite(G guarded, long currentTaskIdBits) {
        ((Guarded) guarded).guardReadWrite(currentTaskIdBits);
        return guarded;
    }
    
    public final void guardReadWriteReachable(Set<Guarded> processed, long currentTaskIdBits) {
        if(processed.add(this)) {
            guardReadWrite(currentTaskIdBits);
            for(Object g : guardedRefs())
                if(g instanceof Guarded)
                    ((Guarded) g).guardReadWriteReachable(processed, currentTaskIdBits);
        }
    }
    
    private void guardReadWrite(long currentTaskIdBits) {
        if(!guardingInitialized() || alreadyReadWriteGuardedIn(currentTaskIdBits))
            return;
        
        while(!mayWrite(currentTaskIdBits))
            park();
    }
    
    // IMPROVE: Deduplicate guarding cache code using Java 9 VarHandles?
    
    private boolean alreadyReadOnlyGuardedIn(long taskIdBits) {
        boolean alreadyGuarded = isInReadGuardingCache(taskIdBits);
        if(!alreadyGuarded)
            addToReadGuardingCache(taskIdBits);
        return alreadyGuarded;
    }
    
    private boolean alreadyReadWriteGuardedIn(long taskIdBits) {
        boolean alreadyGuarded = isInWriteGuardingCache(taskIdBits);
        if(!alreadyGuarded) {
            addToWriteGuardingCache(taskIdBits);
            addToReadGuardingCache(taskIdBits);
        }
        return alreadyGuarded;
    }

    /* The following two are required for expressions of sliced types, which are mapped to interface
     * types in Java (which don't extend Guarded...) */
    
    public static <S> S guardReadOnlySlice(S slice, long $task) {
        guardReadOnly((Guarded) slice, $task);
        return slice;
    }
    
    public static <S> S guardReadWriteSlice(S slice, long $task) {
        guardReadWrite((Guarded) slice, $task);
        return slice;
    }
    
    /* Methods that can be overridden by concrete Guarded classes */
    
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
    
    /* Implementation methods */
    
    private boolean mayRead(long currentTaskIdBits) {
        return sharedCount.get() > 0
                || (idBitsFor(ownerId) == currentTaskIdBits && !descWithReadWriteViewExists());
    }
    
    /**
     * Determines if there is a descendant task of the current task that has a (overlapping)
     * readwrite view of the given object.
     */
    private boolean descWithReadWriteViewExists() {
        if(viewLock() == null)
            return false;
        else
            return descWithReadWriteViewExistsSlow();
    }
    
    private boolean descWithReadWriteViewExistsSlow() {
        Task<?> currentTask = currentTask();
        synchronized(viewLock()) {
            for(Guarded v : views())
                if(v.sharedCount.get() == 0) {
                    Task<?> currentOwner = Task.withId(v.ownerId); // is this corrent?...
                    if(currentOwner.isActive() && currentOwner.isDescendantOf(currentTask))
                        return true;
                }
        }
        return false;
    }
    
    private boolean mayWrite(long currenTaskIdBits) {
        return idBitsFor(ownerId) == currenTaskIdBits && sharedCount.get() == 0
                && !descWithReadViewExists() && !descWithReadWriteViewExists();
        // IMPROVE: Combine above two methods for efficiency
    }
    
    /**
     * Determines if there is a descendant task of the current task that has a (overlapping) read
     * view of the given object.
     */
    private boolean descWithReadViewExists() {
        if(viewLock() == null)
            return false;
        else
            return descWithReadViewExistsSlow();
    }
    
    private boolean descWithReadViewExistsSlow() {
        Task<?> currentTask = currentTask();
        synchronized(viewLock()) {
            for(Guarded view : views())
                for(Task<?> reader : view.readers)
                    if(reader.isDescendantOf(currentTask))
                        return true;
        }
        return false;
    }
    
    private boolean isInReadGuardingCache(long taskIdBits) {
        assert taskIdBits != 0;
        return (readGuardingCache & taskIdBits) != 0;
    }
    
    private boolean isInWriteGuardingCache(long taskIdBits) {
        assert taskIdBits != 0;
        return (writeGuardingCache & taskIdBits) != 0;
    }
    
    private void addToReadGuardingCache(long taskIdBits) {
        assert taskIdBits != 0;
        synchronized(guardingCachesLock) {
            readGuardingCache |= taskIdBits;
        }
    }
    
    private void addToWriteGuardingCache(long taskIdBits) {
        assert taskIdBits != 0;
        synchronized(guardingCachesLock) {
            writeGuardingCache |= taskIdBits;
        }
    }
    
    private final void invalidateGuardingCaches() {
        synchronized(guardingCachesLock) {
            readGuardingCache = 0L;
            writeGuardingCache = 0L;
        }
        if(viewLock() != null)
            synchronized(viewLock()) {
                for(Guarded v : views())
                    if(v != null)
                        synchronized(v.guardingCachesLock) {
                            v.readGuardingCache = 0L;
                            v.writeGuardingCache = 0L;
                        }
            }
    }
}
