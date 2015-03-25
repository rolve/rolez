package ch.trick17.peppl.typesystemchecker;

@TypeErrors(lines = {9, 12, 19, 23})
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
