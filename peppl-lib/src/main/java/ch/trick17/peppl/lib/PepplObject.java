package ch.trick17.peppl.lib;

public class PepplObject {
    
    // IMPROVE: volatile unnecessary? (other volatile fields are written before
    // object is visible to other tasks)
    private volatile Guard guard;
    
    public final void share() {
        if(guard == null)
            guard = GuardFactory.getDefault().newGuard();
        guard.share();
    }
    
    public final void pass() {
        if(guard == null)
            guard = GuardFactory.getDefault().newGuard();
        guard.pass();
    }
    
    public final void registerNewOwner() {
        assert guard != null;
        guard.registerNewOwner();
    }
    
    public final void releaseShared() {
        assert guard != null;
        guard.releaseShared();
    }
    
    public final void releasePassed() {
        assert guard != null;
        guard.releasePassed();
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
