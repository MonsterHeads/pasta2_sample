package huck.pasta2.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import huck.pasta2.ActionFilter;
import huck.pasta2.ExceptionHandler;


@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface ActionParam {
	String[] extensions();
	Class<? extends ActionFilter>[] filters() default {};
	Class<? extends ExceptionHandler<? extends Exception>>[] exceptionHandlers() default {};
	ViewMappingParam[] viewMappings() default {};
}