package rolez.checked.lang;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Methods of mapped Rolez classes or parameters thereof that are annotated with this are not
 * guarded by the Rolez runtime.
 */
@Retention(CLASS)
@Target({METHOD, PARAMETER})
public @interface Safe {}
