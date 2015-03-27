package ch.trick17.peppl.typesystemchecker;

import ch.trick17.peppl.typesystemchecker.qual.ReadOnly;

public class FieldAccessFail extends PepplCheckerTest {
    
    @SuppressWarnings("cast")
    public void foo() {
        Int roi = (@ReadOnly Int) new Int();
        //:: error: (illegal.write)
        roi.i = 3;
    }
    
    public static class Int {
        private int i;
        
        public void bar(@ReadOnly Int this) {
            System.out.println(i);
            
            //:: error: (illegal.write)
            this.i = 10;
        }
        
        public void foobar() {
            int v;
            v = 0;
            System.out.println(v);
        }
    }
}
