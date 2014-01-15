package ch.trick17.peppl.lib.guard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GuardedObject {
    
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
    
    void processRecursively(final Op op, final Set<GuardedObject> processed) {
        if(processed.add(this)) {
            /* Process current object */
            op.process(getGuard());
            
            /* Process children */
            final List<Field> fields = allRefFields();
            for(final Field field : fields) {
                field.setAccessible(true);
                final Object ref;
                try {
                    ref = field.get(this);
                } catch(final IllegalAccessException e) {
                    throw new AssertionError(e);
                }
                if(ref != null) {
                    assert ref instanceof GuardedObject;
                    final GuardedObject other = (GuardedObject) ref;
                    other.processRecursively(op, processed);
                }
            }
        }
    }
    
    private List<Field> allRefFields() {
        final ArrayList<Field> fields = new ArrayList<>();
        Class<?> currentClass = getClass();
        while(currentClass != GuardedObject.class) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for(final Field declaredField : declaredFields)
                if(!declaredField.getType().isPrimitive())
                    fields.add(declaredField);
            currentClass = currentClass.getSuperclass();
        }
        return Collections.unmodifiableList(fields);
    }
}
