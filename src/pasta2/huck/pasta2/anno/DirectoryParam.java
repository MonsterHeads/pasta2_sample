package huck.pasta2.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import huck.pasta2.ActionFilter;
import huck.pasta2.ExceptionHandler;



@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface DirectoryParam {
	String path();
	Class<? extends ActionFilter>[] filters() default {};
	Class<? extends ExceptionHandler<? extends Exception>>[] exceptionHandlers();
	ViewMappingParam[] viewMappings();
}
