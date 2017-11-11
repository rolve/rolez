package rolez.checked.lang;

import java.lang.reflect.Array;

/**
 * This class represents the interface of the <code>Vector</code> class in the Rolez compiler, but
 * is not actually used in Rolez programs. Instead, Java arrays are used directly.
 * 
 * @author Michael Faes
 */
public class Vector<T> {
    
	private T data;
	
    public final int length;
    
    public Vector(T data) {
    	this.length = Array.getLength(data);
		this.data = data;
	}

    public T getData() {
    	return this.data;
    }
    
	public int getInt(int i) {
		return ((int[])data)[i];
	}
	
	public short getShort(int i) {
		return ((short[])data)[i];
	}
}
