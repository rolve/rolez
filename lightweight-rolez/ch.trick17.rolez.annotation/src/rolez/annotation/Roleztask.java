package rolez.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;


/**
 * Annotation for Rolez Tasks. It's used to annotate threads.
 * @author Michael Giger
 *
 */
@Target({ElementType.METHOD}) 
@Retention(RetentionPolicy.SOURCE)
public @interface Roleztask {
	
}
