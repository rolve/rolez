package ch.trick17.peppl.typesystemchecker;

import ch.trick17.peppl.typesystemchecker.qual.Inaccessible;
import ch.trick17.peppl.typesystemchecker.qual.ReadOnly;

@TypeErrors(lines = {12, 15, 22, 26})
public class FieldAccessFail extends PepplCheckerTest {
    
    @SuppressWarnings("cast")
    public void foo() {
        Int roi = (@ReadOnly Int) new Int();
        roi.i = 3;
        
        Int ini = (@Inaccessible Int) roi;
        System.out.println(ini.i);
    }
    
    public static class Int {
        private int i;
        
        public void bar(@ReadOnly Int this) {
            this.i = 10;
        }
        
        public void baz(@Inaccessible Int this) {
            System.out.println(i);
        }
        
        public void foobar() {
            int v;
            v = 0;
            System.out.println(v);
        }
    }
}
