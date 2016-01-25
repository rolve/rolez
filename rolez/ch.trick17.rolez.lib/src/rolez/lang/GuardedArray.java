package rolez.lang;

import java.lang.reflect.Array;

public final class GuardedArray<A> extends GuardedSlice<A> {
    
    public GuardedArray(A array) {
        super(array, new SliceRange(0, Array.getLength(array), 1));
    }
    
    public static <A> GuardedArray<A> wrap(Object array) {
        if(array == null)
            return null;
            
        Class<?> componentType = array.getClass().getComponentType();
        if(componentType.isArray()) {
            Object[] origArray = (Object[]) array;
            GuardedArray<?>[] wrappedArray = new GuardedArray[origArray.length];
            for(int i = 0; i < origArray.length; i++)
                wrappedArray[i] = wrap(origArray[i]);
            return new GuardedArray<A>((A) wrappedArray);
        }
        else
            return new GuardedArray<A>((A) array);
    }
    
    public static <O> O unwrap(GuardedArray<?> wrapped, Class<O> arrayType) {
        if(wrapped == null)
            return null;
            
        Class<?> componentType = arrayType.getComponentType();
        if(componentType.isArray()) {
            int length = Array.getLength(wrapped.data);
            Object[] array = (Object[]) Array.newInstance(componentType, length);
            for(int i = 0; i < length; i++)
                array[i] = unwrap((GuardedArray<?>) Array.get(wrapped.data, i), componentType);
            return arrayType.cast(array);
        }
        else
            return arrayType.cast(wrapped.data);
    }
    
    // TODO: Test that the stuff above actually works...
}
