package ch.trick17.peppl.typesystemchecker;

import ch.trick17.peppl.typesystemchecker.qual.Inaccessible;
import ch.trick17.peppl.typesystemchecker.qual.ReadOnly;

public class GetterSetterTestFail extends PepplCheckerTest {
    
    @SuppressWarnings("cast")
    public void foo() {
        Int roi = (@ReadOnly Int) new Int();
        //:: error: (method.invocation.invalid)
        roi.set(3);
        
        Int ini = (@Inaccessible Int) roi;
        //:: error: (method.invocation.invalid)
        System.out.println(ini.get());
    }
    
    public static class Int {
        
        private int i;
        
        private int get(@ReadOnly Int this) {
            return i;
        }
        
        private void set(Int this, int value) {
            i = value;
        }
    }
}
