package ch.trick17.peppl.typesystemchecker;

import ch.trick17.peppl.typesystemchecker.qual.Poly;
import ch.trick17.peppl.typesystemchecker.qual.ReadOnly;

@SuppressWarnings("cast")
public class TransitivityTest extends PepplCheckerTest {
    
    public void foo() {
        Int i = new Int();
        i.setNext(new Int());
        i.getNext().set(4);
        
        Int roi = (@ReadOnly Int) i;
        Int roiNext = roi.getNext();
        System.out.println(roiNext.get());
        
        //:: error: (method.invocation.invalid)
        roiNext.set(10);
    }
    
    public static class Int {
        
        private Int next;
        private int i;
        
        public int get(@ReadOnly Int this) {
            return i;
        }
        
        public void set(Int this, int value) {
            i = value;
        }
        
        public @Poly Int getNext(@Poly Int this) {
            return next;
        }
        
        public void setNext(Int next) {
            this.next = next;
        }
        
        public void foo(@ReadOnly Int this) {
            //:: error: (illegal.write)
            this.next.i = 3;
            
            //:: error: (illegal.write)
            next.i = 3;
        }
    }
}
