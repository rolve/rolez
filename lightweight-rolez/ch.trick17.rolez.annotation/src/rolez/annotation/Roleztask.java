package rolez.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;


/**
 * This annotation is used to declare methods as Rolez tasks. Classes including
 * Rolez tasks need to be annotated with the <code>@Guarded</code> annotation and
 * therefore extending the Guarded class from the Rolez runtime library.
 * 
 * @author Michael Giger
 *
 */
@Target({ElementType.METHOD}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface Roleztask {
	
}
