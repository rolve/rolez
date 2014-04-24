package ch.trick17.peppl.lib.guard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GuardedObject extends Guarded {
    
    private static final Set<Class<?>> knownImmutable = new HashSet<Class<?>>() {
        {
            add(String.class);
        }
    };
    
    @Override
    final Iterable<? extends Guarded> allRefs() {
        final ArrayList<Guarded> refs = new ArrayList<>();
        Class<?> currentClass = getClass();
        while(currentClass != GuardedObject.class) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for(final Field field : declaredFields) {
                if(guardNecessary(field.getType())) {
                    field.setAccessible(true);
                    final Object ref;
                    try {
                        ref = field.get(this);
                    } catch(final IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                    assert ref == null || ref instanceof Guarded;
                    refs.add((Guarded) ref);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return Collections.unmodifiableList(refs);
    }
    
    private static boolean guardNecessary(final Class<?> type) {
        return !type.isPrimitive() && !knownImmutable.contains(type);
    }
    
}
