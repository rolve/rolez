package ch.trick17.peppl.lib.guard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import ch.trick17.peppl.lib.immutable.Immutable;

public class GuardedObject extends Guarded {
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        final ArrayList<Guarded> refs = new ArrayList<>();
        Class<?> currentClass = getClass();
        while(currentClass != GuardedObject.class) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for(final Field field : declaredFields)
                if(!Immutable.isImmutable(field.getType())) {
                    field.setAccessible(true);
                    try {
                        refs.add((Guarded) field.get(this));
                    } catch(final IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                }
            currentClass = currentClass.getSuperclass();
        }
        return Collections.unmodifiableList(refs);
    }
}
