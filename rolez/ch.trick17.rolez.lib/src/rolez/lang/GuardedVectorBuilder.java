package rolez.lang;

public class GuardedVectorBuilder<A> extends Guarded {
    
    public final A data;
    private boolean built = false;
    
    public GuardedVectorBuilder(A array) {
        this.data = array;
    }
    
    public void set(int index, Object component) {
        checkNotBuilt();
        if(component instanceof Guarded)
            throw new IllegalArgumentException(
                    "vector components must be immutable (i.e. not guarded)");
        ((Object[]) data)[index] = component;
    }
    
    public void setInt(int index, int component) {
        checkNotBuilt();
        ((int[]) data)[index] = component;
    }
    
    public void setDouble(int index, double component) {
        checkNotBuilt();
        ((double[]) data)[index] = component;
    }
    
    public void setBoolean(int index, boolean component) {
        checkNotBuilt();
        ((boolean[]) data)[index] = component;
    }
    
    public void setChar(int index, char component) {
        checkNotBuilt();
        ((char[]) data)[index] = component;
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
