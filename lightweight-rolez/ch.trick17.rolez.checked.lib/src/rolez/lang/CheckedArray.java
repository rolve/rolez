package rolez.lang;

import java.lang.reflect.Array;

public final class CheckedArray<A> extends CheckedSlice<A> {
    
    public CheckedArray(A array) {
        super(array, new SliceRange(0, Array.getLength(array), 1));
    }
    
    @SuppressWarnings("unchecked")
    public static <A> CheckedArray<A> wrap(Object array) {
        if(array == null)
            return null;
            
        Class<?> componentType = array.getClass().getComponentType();
        if(componentType.isArray()) {
            Object[] origArray = (Object[]) array;
            CheckedArray<?>[] wrappedArray = new CheckedArray[origArray.length];
            for(int i = 0; i < origArray.length; i++)
                wrappedArray[i] = wrap(origArray[i]);
            return new CheckedArray<A>((A) wrappedArray);
        }
        else
            return new CheckedArray<A>((A) array);
    }
    
    public static <O> O unwrap(CheckedArray<?> wrapped, Class<O> arrayType) {
        if(wrapped == null)
            return null;
            
        Class<?> componentType = arrayType.getComponentType();
        if(componentType.isArray()) {
            int length = Array.getLength(wrapped.data);
            Object[] array = (Object[]) Array.newInstance(componentType, length);
            for(int i = 0; i < length; i++)
                array[i] = unwrap((CheckedArray<?>) Array.get(wrapped.data, i), componentType);
            return arrayType.cast(array);
        }
        else
            return arrayType.cast(wrapped.data);
    }
    
    // TODO: Test that the stuff above actually works...
    
    @Override
    @SuppressWarnings("unchecked")
    public CheckedArray<A> clone() {
        if(data instanceof Object[])
            return new CheckedArray<A>((A) ((Object[]) data).clone());
        if(data instanceof int[])
            return new CheckedArray<A>((A) ((int[]) data).clone());
        if(data instanceof double[])
            return new CheckedArray<A>((A) ((double[]) data).clone());
        if(data instanceof boolean[])
            return new CheckedArray<A>((A) ((boolean[]) data).clone());
        if(data instanceof char[])
            return new CheckedArray<A>((A) ((char[]) data).clone());
        else
            throw new AssertionError("unexpected array type");
    }
}
