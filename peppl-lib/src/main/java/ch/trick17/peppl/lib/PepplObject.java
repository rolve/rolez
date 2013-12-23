package ch.trick17.peppl.lib;

public class PepplObject {
    
    // IMPROVE: volatile unnecessary? (other volatile fields are written before
    // object is visible to other tasks)
    private volatile Guard guard;
    
    public final void share() {
        getGuard().share(this);
    }
    
    public final void pass() {
        getGuard().pass(this);
    }
    
    Guard getGuard() {
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
}
