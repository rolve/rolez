package rolez.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes annotated with @Checked will inherit from the 
 * Checked class of the Rolez runtime library. Instances of @Checked
 * annotated classes are legal to use in tasks. And tasks can only be
 * declared in classes annotated with @Checked
 * 
 * @author Michael Giger
 *
 */
@Target({ElementType.TYPE}) 
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Checked {

}
