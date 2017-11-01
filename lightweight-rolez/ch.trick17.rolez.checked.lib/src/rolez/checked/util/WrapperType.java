package rolez.checked.util;

import rolez.checked.lang.Checked;

public abstract class WrapperType<T> extends Checked {
	
	/**
	 * The wrapped instance
	 */
	T instance;
	
	/**
	 * Method which returns the wrapped instance and checks
	 * for <b>legal read</b> before returning it.
	 * @return
	 */
	public T getUncheckedReadInstance() {
		return checkLegalRead(this).instance;
	}
	
	/**
	 * Method which returns the wrapped instance and checks
	 * for <b>legal write</b> before returning it.
	 * @return
	 */
	public T getUncheckedWriteInstance() {
		return checkLegalWrite(this).instance;
	}
}
