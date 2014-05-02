package ch.trick17.peppl.lib.immutable;

import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

public abstract class Immutable {
    
    private static final Set<Class<?>> knownImmutable = unmodifiableSet(new HashSet<Class<?>>() {
        {
            add(String.class);
        }
    });
    
    /**
     * Indicates whether the given type is <em>immutable</em>, i.e., is either
     * primitive, or an enum, or an known immutable JDK class, or a subclass of
     * {@link Immutable}.
     * <p>
     * Note that primitive types are considered immutable because they behave
     * like immutable reference types, e.g. {@link Integer} or {@link String}.
     * In particular, a final field with a primitive type or an immutable
     * reference type can be considered an immutable part of the class defining
     * the field.
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
    
    Immutable() {}
}
