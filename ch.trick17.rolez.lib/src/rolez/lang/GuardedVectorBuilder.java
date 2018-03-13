package rolez.lang;

public class GuardedVectorBuilder<A> extends Guarded {
    
    public final A data;
    private boolean built = false;
    
    public GuardedVectorBuilder(A array) {
        this.data = array;
    }
    
    public void set(int index, Object component) {
        checkNotBuilt();
        // IMPROVE: Could check vector type arg at compile time, but would lose some flexibility
        if(component instanceof Guarded)
            throw new IllegalArgumentException(
                    "vector components must be pure (primitive or instances of pure classes, including singleton classes)");
        ((Object[]) data)[index] = component;
    }
    
    public void setDouble(int index, double component) {
        checkNotBuilt();
        ((double[]) data)[index] = component;
    }
    
    public void setLong(int index, long component) {
        checkNotBuilt();
        ((long[]) data)[index] = component;
    }
    
    public void setInt(int index, int component) {
        checkNotBuilt();
        ((int[]) data)[index] = component;
    }
    
    public void setShort(int index, short component) {
        checkNotBuilt();
        ((short[]) data)[index] = component;
    }
    
    public void setByte(int index, byte component) {
        checkNotBuilt();
        ((byte[]) data)[index] = component;
    }
    
    public void setChar(int index, char component) {
        checkNotBuilt();
        ((char[]) data)[index] = component;
    }
    
    public void setBoolean(int index, boolean component) {
        checkNotBuilt();
        ((boolean[]) data)[index] = component;
    }
    
    private final void checkNotBuilt() {
        if(built)
            throw new IllegalStateException("vector has already been built");
    }
    
    public A build() {
        built = true;
        return data;
    }
}
