package ch.trick17.peppl.typesystemchecker;

@TypeErrors(lines = {9, 12})
public class GetterSetterTestFail extends PepplCheckerTest {
    
    @SuppressWarnings("cast")
    public void foo() {
        Int roi = (@ReadOnly Int) new Int();
        roi.set(3);
        
        Int ini = (@Inaccessible Int) roi;
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
