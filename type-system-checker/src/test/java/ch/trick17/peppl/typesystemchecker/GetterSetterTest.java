package ch.trick17.peppl.typesystemchecker;

public class GetterSetterTest extends PepplCheckerTest {
    
    @SuppressWarnings("cast")
    public void foo() {
        final Int i = new Int();
        i.set(3);
        
        Int roi = (@ReadOnly Int) i;
        System.out.println(roi.get());
    }
    
    public static class Int {
        
        private int i;
        
        private int get(@ReadOnly Int this) {
            final Int i2 = this;
            return i2.i;
        }
        
        private void set(@ReadWrite Int this, int value) {
            i = value;
        }
    }
}
