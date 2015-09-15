package ch.trick17.rolez.lib.guard;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;

import java.lang.reflect.Field;
import java.util.ArrayList;

import ch.trick17.rolez.lib.immutable.Immutable;

public class GuardedObject extends Guarded {
    
    @Override
    Iterable<? extends Guarded> guardedRefs() {
        /* Compiler may generate more efficient (i.e. reflection-less)
         * implementations of this method for each class. */
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
        return unmodifiableList(refs);
    }
    
    @Override
    Iterable<? extends Guarded> views() {
        return emptySet();
    }
}
