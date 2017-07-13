package rolez.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes annotated with <code>@Guarded</code> will inherit from the 
 * Guarded class of the Rolez runtime library.
 * 
 * @author Michael Giger
 *
 */
@Target({ElementType.TYPE}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface Checked {

}
