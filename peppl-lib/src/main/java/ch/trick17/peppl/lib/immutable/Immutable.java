package ch.trick17.peppl.lib.immutable;

import static java.lang.reflect.Modifier.isFinal;
import static java.util.Collections.newSetFromMap;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Immutable {
    
    private static final Set<Class<?>> knownImmutable = new HashSet<Class<?>>() {
        {
            add(String.class);
        }
    };
    
    private static final Set<Class<?>> checkedClasses = newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());
    
    private static void checkClass(final Class<?> clazz) {
        assert clazz != null;
        if(clazz != Immutable.class && checkedClasses.add(clazz)) {
            /* Recursively check superclasses */
            checkClass(clazz.getSuperclass());
            
            /* Check that all fields are final and immutable */
            for(final Field field : clazz.getDeclaredFields()) {
                if(!isFinal(field.getModifiers())) {
                    throw new AssertionError(
                            clazz.getName()
                                    + " is a subclass of Immutable but has a non-final field: "
                                    + field.getName());
                }
                if(!isImmutable(field.getType()))
                    throw new AssertionError(
                            clazz.getName()
                                    + " is a subclass of Immutable but has a field of a non-immutable type: "
                                    + field.getName() + " of type "
                                    + field.getType().getName());
            }
        }
    }
    
    /**
     * Indicates whether the given type is <em>immutable</em>, i.e., is either
     * primitive, or an enum, or an known immutable JDK class, or a subclass of
     * {@link Immutable}.
     * 
     * @param type
     *            the type to check
     * @return <code>true</code> if the given type is immutable, as define
     *         above.
     */
    public static boolean isImmutable(final Class<?> type) {
        return type.isPrimitive() || type.isEnum()
                || knownImmutable.contains(type)
                || Immutable.class.isAssignableFrom(type);
    }
    
    public Immutable() {
        checkClass(getClass());
    }
}
