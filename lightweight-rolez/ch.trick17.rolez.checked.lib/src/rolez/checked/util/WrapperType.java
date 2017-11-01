package rolez.checked.util;

public interface WrapperType<T> {
	
	/**
	 * Method which returns the wrapped instance and checks
	 * for <b>legal read</b> before returning it.
	 * @return
	 */
	T getUncheckedReadInstance();
	
	/**
	 * Method which returns the wrapped instance and checks
	 * for <b>legal write</b> before returning it.
	 * @return
	 */
	T getUncheckedWriteInstance();
}
