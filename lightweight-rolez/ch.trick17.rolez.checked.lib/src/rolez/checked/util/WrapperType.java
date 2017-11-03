package rolez.checked.util;

import rolez.checked.lang.Checked;
import rolez.checked.lang.annotation.Read;
import rolez.checked.lang.annotation.Write;

public abstract class WrapperType<T> extends Checked {
	
	/**
	 * The wrapped instance
	 */
	T instance;
	
	/**
	 * Method to unpack the wrapped instance which checks
	 * for <b>legal read</b> before returning it.
	 * @return
	 */
	@Read
	public T getUncheckedReadInstance() {
		return checkLegalRead(this).instance;
	}
	
	/**
	 * Method to unpack the wrapped instance which checks
	 * for <b>legal write</b> before returning it.
	 * @return
	 */
	@Write
	public T getUncheckedWriteInstance() {
		return checkLegalWrite(this).instance;
	}
}
