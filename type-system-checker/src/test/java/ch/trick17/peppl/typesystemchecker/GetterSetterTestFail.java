package ch.trick17.peppl.typesystemchecker;

import ch.trick17.peppl.typesystemchecker.qual.ReadOnly;

public class GetterSetterTestFail extends PepplCheckerTest {
    
    @SuppressWarnings("cast")
    public void foo() {
        Int roi = (@ReadOnly Int) new Int();
        
        //:: error: (method.invocation.invalid)
        roi.set(3);
    }
    
    public static class Int {
        
        private int i;
        
        public int get(@ReadOnly Int this) {
            return i;
        }
        
        public void set(Int this, int value) {
            i = value;
        }
    }
}
