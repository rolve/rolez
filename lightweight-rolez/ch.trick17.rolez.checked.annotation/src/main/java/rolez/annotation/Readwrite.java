package rolez.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to declare the role for task parameters. Also used on methods to declare
 * the role of <code>this</code> in the task.
 * 
 * @author Michael Giger
 *
 */
@Target({ElementType.PARAMETER, ElementType.METHOD}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface Readwrite {

}
