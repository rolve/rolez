package ch.trick17.peppl.lib.guard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

public class GuardedObject extends Guarded {
    
    @Override
    final Iterable<? extends Guarded> allRefs() {
        final ArrayList<Guarded> refs = new ArrayList<>();
        Class<?> currentClass = getClass();
        while(currentClass != GuardedObject.class
                && currentClass != GuardedArray.class) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for(final Field field : declaredFields)
                if(!field.getType().isPrimitive()) {
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
            currentClass = currentClass.getSuperclass();
        }
        return Collections.unmodifiableList(refs);
    }
}
