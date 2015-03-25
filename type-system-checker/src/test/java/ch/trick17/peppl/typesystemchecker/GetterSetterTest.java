package ch.trick17.peppl.typesystemchecker;

@SuppressWarnings("cast")
public class GetterSetterTest extends PepplCheckerTest {
    
    public void foo() {
        Int i = new Int();
        i.set(3);
        
        Int roi = (@ReadOnly Int) i;
        System.out.println(roi.get());
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
