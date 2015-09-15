package ch.trick17.rolez.lib.immutable;

import static java.lang.reflect.Modifier.isFinal;
import static java.util.Collections.newSetFromMap;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImmutableObject extends Immutable {
    
    private static final Set<Class<?>> checkedClasses = newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());
    
    private static void checkClass(final Class<?> clazz) {
        assert clazz != null;
        if(clazz != ImmutableObject.class && checkedClasses.add(clazz)) {
            /* Recursively check superclasses */
            checkClass(clazz.getSuperclass());
            
            /* Check that all fields are final and immutable */
            for(final Field field : clazz.getDeclaredFields()) {
                if(!isFinal(field.getModifiers())) {
                    throw new AssertionError(
                            clazz.getName()
                                    + " is a subclass of ImmutableObject but has a non-final field: "
                                    + field.getName());
                }
                if(!isImmutable(field.getType()))
                    throw new AssertionError(
                            clazz.getName()
                                    + " is a subclass of ImmutableObject but has a field of a non-immutable type: "
                                    + field.getName() + " of type "
                                    + field.getType().getName());
            }
        }
    }
    
    public ImmutableObject() {
        checkClass(getClass());
    }
}
