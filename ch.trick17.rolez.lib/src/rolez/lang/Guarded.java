package rolez.lang;

import static java.util.Collections.emptyList;
import static java.util.concurrent.locks.LockSupport.park;
import static rolez.lang.Task.currentTask;
import static rolez.lang.Task.idForBits;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Superclass of all guarded objects
 * 
 * @author Michael Faes
 */
public abstract class Guarded {
    
    private boolean guardingDisabled = false;
    
    private volatile long ownerBits = 0;
    private AtomicLong readerBits;
    
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
            ensureGuardingInitialized(currentTask().idBits());
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
    
    /* Initialization and enabling/disabling guarding */
    
    private boolean guardingInitialized() {
        return ownerBits != 0;
    }
    
    /**
     * Initializes the guarding "infrastructure", if guarding is not
     * {@linkplain #disableGuarding(Guarded) disabled}.
     */
    protected final void ensureGuardingInitialized(long taskBits) {
        assert !guardingDisabled;
        if(!guardingInitialized()) {
            ownerBits = taskBits;
            readerBits = new AtomicLong();
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
            ownerBits = 0;
        }
        
        for(Object g : guardedRefs())
            if(g instanceof Guarded)
                ((Guarded) g).disableGuarding();
    }
    
    /* Role transitions */
    
    final void pass(long newTaskBits) {
        if(!guardingDisabled) {
            ensureGuardingInitialized(newTaskBits);
            ownerBits = newTaskBits;
            invalidateGuardingCaches();
        }
    }
    
    final void share(long newTaskBits, long parentBits) {
        if(!guardingDisabled) {
            ensureGuardingInitialized(parentBits);
            assert (readerBits.get() & newTaskBits) == 0;
            readerBits.addAndGet(newTaskBits); // addition is the same as bitwise OR if bits don't overlap!
            invalidateGuardingCaches();
        }
    }
    
    final boolean ownedBy(long taskBits) {
        return ownerBits == taskBits; // implies guardingInitialized(), assuming taskBits != 0
    }
    
    final boolean ownedByOrSharedWith(long taskBits) {
        return ownerBits == taskBits || guardingInitialized() && (readerBits.get() & taskBits) != 0;
    }
    
    final void releaseShared(long taskBits) {
        if(!guardingDisabled) {
            assert (readerBits.get() & taskBits) != 0;
            readerBits.addAndGet(-taskBits); // see above
        }
        // TODO: notify tasks that wait for other views?
    }
    
    final void releasePassed(long newOwnerBits) {
        if(!guardingDisabled) {
            ensureGuardingInitialized(newOwnerBits);
            ownerBits = newOwnerBits;
        }
        // TODO: notify tasks that wait for other views?
    }
    
    /* Eager interference checking */
    
    final void checkInterferesRo(long newTaskBits, long otherTaskBits) {
        checkInterferesRoView(newTaskBits, otherTaskBits, this);
        if(viewLock() != null)
            synchronized(viewLock()) {
                for(Guarded v : views())
                    v.checkInterferesRoView(newTaskBits, otherTaskBits, this);
            }
    }
    
    private void checkInterferesRoView(long newTaskBits, long otherTaskBits, Guarded original) {
        if((ownerBits & otherTaskBits) != 0)
            throw new InterferenceError(original, this, "readonly", "readwrite", newTaskBits, ownerBits);
    }
    
    final void checkInterferesRw(long newTaskBits, long otherTaskBits) {
        checkInterferesRwView(newTaskBits, otherTaskBits, this);
        
        if(viewLock() != null)
            synchronized(viewLock()) {
                for(Guarded v : views())
                    v.checkInterferesRwView(newTaskBits, otherTaskBits, this);
            }
    }
    
    private void checkInterferesRwView(long newTaskBits, long otherTaskBits, Guarded original) {
        if((ownerBits & otherTaskBits) != 0)
            throw new InterferenceError(original, this, "readwrite", "readwrite", newTaskBits, ownerBits);
        if (guardingInitialized()) {
        	long otherReaders = readerBits.get() & otherTaskBits;
            if(otherReaders != 0)
                throw new InterferenceError(original, this, "readwrite", "readonly", newTaskBits, otherReaders);
        }
    }
    
    
    /* Guarding methods. The static versions can return the guarded object with the precise type
     * (which is not possible with instance methods, due to the lack of self types). This simplifies
     * code generation a lot, since guarding can be done within an expression. */

    public static <G extends Guarded> G guardReadOnly(G guarded, long taskBits) {
        ((Guarded) guarded).guardReadOnly(taskBits);
        return guarded;
    }
    
    public static <G extends Guarded> G guardReadWrite(G guarded, long taskBits) {
        ((Guarded) guarded).guardReadWrite(taskBits);
        return guarded;
    }
    
    // TODO: Remove this version at some point
    public static <G extends Guarded> G guardReadOnly(G guarded) {
        ((Guarded) guarded).guardReadOnly(currentTask().idBits());
        return guarded;
    }
    
    // TODO: Remove this version at some point
    public static <G extends Guarded> G guardReadWrite(G guarded) {
        ((Guarded) guarded).guardReadWrite(currentTask().idBits());
        return guarded;
    }
    
    final void guardReadOnly(long taskBits) {
        if(!guardingInitialized() || alreadyReadOnlyGuardedIn(taskBits))
            return;
        
        while(!mayRead(taskBits))
            park();
    }
    
    final void guardReadWrite(long taskBits) {
        if(!guardingInitialized() || alreadyReadWriteGuardedIn(taskBits))
            return;
        
        while(!mayWrite(taskBits))
            park();
    }
    
    final void guardShare(long taskBits, long newTaskBits) {
        if(!guardingInitialized() || alreadyReadOnlyGuardedIn(taskBits))
            return;
        
        while(!mayShare(taskBits, newTaskBits))
            park();
    }
    
    final void guardPass(long taskBits, long newTaskBits) {
        if(!guardingInitialized() || alreadyReadWriteGuardedIn(taskBits))
            return;

        while(!mayPass(taskBits, newTaskBits))
            park();
    }
    
    /* The following two are required for expressions of sliced types, which are mapped to interface
     * types in Java (which don't extend Guarded...) */
    
    public static <S> S guardReadOnlySlice(S slice, long taskBits) {
        guardReadOnly((Guarded) slice, taskBits);
        return slice;
    }
    
    public static <S> S guardReadWriteSlice(S slice, long taskBits) {
        guardReadWrite((Guarded) slice, taskBits);
        return slice;
    }
    
    /* The following two are required for expressions of type java.lang.Object, for which it is only
     * known at runtime whether guarding is needed */
    
    public static <G> G guardReadOnlyIfNeeded(G guarded, long taskBits) {
        if(guarded instanceof Guarded)
            guardReadOnly((Guarded) guarded, taskBits);
        return guarded;
    }
    
    public static <G> G guardReadWriteIfNeeded(G guarded, long taskBits) {
        if(guarded instanceof Guarded)
            guardReadWrite((Guarded) guarded, taskBits);
        return guarded;
    }
    
    private boolean mayRead(long taskBits) {
        return readerBits.get() != 0 ||
                ownerBits == taskBits &&
                (viewLock() == null || !descWithReadWriteViewExists());
    }
    
    private boolean mayWrite(long taskBits) {
        return ownerBits == taskBits && readerBits.get() == 0 &&
                (viewLock() == null || !descWithReadViewExists()) &&
                (viewLock() == null || !descWithReadWriteViewExists());
        // IMPROVE: Combine above two methods for efficiency
    }
    
    private boolean mayShare(long taskBits, long newTaskBits) {
        return readerBits.get() != 0 ||
                ownerBits == taskBits &&
                (viewLock() == null || !descWithReadWriteViewExists(newTaskBits));
    }
    
    private boolean mayPass(long taskBits, long newTaskBits) {
        return ownerBits == taskBits && readerBits.get() == 0 &&
                (viewLock() == null || !descWithReadViewExists()) && // no need to ignore new task here, passing happens before sharing
                (viewLock() == null || !descWithReadWriteViewExists(newTaskBits));
        // IMPROVE: Combine above two methods for efficiency
    }
    
    private boolean descWithReadWriteViewExists() {
        Task<?> currentTask = currentTask();
        synchronized(viewLock()) {
            for(Guarded v : views())
                if(v.readerBits.get() == 0) {
                    Task<?> owner = Task.withId(idForBits(v.ownerBits));
                    // TODO: could registeredTask[id] have been overridden?..
                    if(owner.isActive() && owner.isDescendantOf(currentTask))
                        return true;
                }
        }
        return false;
    }
    
    private boolean descWithReadWriteViewExists(long ignoreTaskBits) {
        Task<?> currentTask = currentTask();
        synchronized(viewLock()) {
            for(Guarded v : views())
                if(v.readerBits.get() == 0 && v.ownerBits != ignoreTaskBits) {
                    Task<?> owner = Task.withId(idForBits(v.ownerBits));
                    // TODO: could registeredTask[id] have been overridden?..
                    if(owner.isActive() && owner.isDescendantOf(currentTask))
                        return true;
                }
        }
        return false;
    }
    
    private boolean descWithReadViewExists() {
        Task<?> currentTask = currentTask();
        synchronized(viewLock()) {
            for(Guarded view : views()) {
                long viewReaderBits = view.readerBits.get();
                if(viewReaderBits != 0) {
                    for(int id = 0; id < 64; id++)
                        if((viewReaderBits & (1L << id)) != 0 && Task.withId(id).isDescendantOf(currentTask))
                            return true;
                }
            }
            return false;
        }
    }
    
    // IMPROVE: Deduplicate guarding cache code using Java 9 VarHandles?
    
    private boolean alreadyReadOnlyGuardedIn(long taskBits) {
        boolean alreadyGuarded = isInReadGuardingCache(taskBits);
        if(!alreadyGuarded)
            addToReadGuardingCache(taskBits);
        return alreadyGuarded;
    }
    
    private boolean alreadyReadWriteGuardedIn(long taskBits) {
        boolean alreadyGuarded = isInWriteGuardingCache(taskBits);
        if(!alreadyGuarded) {
            addToWriteGuardingCache(taskBits);
            addToReadGuardingCache(taskBits);
        }
        return alreadyGuarded;
    }
    
    private boolean isInReadGuardingCache(long taskBits) {
        assert taskBits != 0;
        return (readGuardingCache & taskBits) != 0;
    }
    
    private boolean isInWriteGuardingCache(long taskBits) {
        assert taskBits != 0;
        return (writeGuardingCache & taskBits) != 0;
    }
    
    private void addToReadGuardingCache(long taskBits) {
        assert taskBits != 0;
        synchronized(guardingCachesLock) {
            readGuardingCache |= taskBits;
        }
    }
    
    private void addToWriteGuardingCache(long taskBits) {
        assert taskBits != 0;
        synchronized(guardingCachesLock) {
            writeGuardingCache |= taskBits;
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
