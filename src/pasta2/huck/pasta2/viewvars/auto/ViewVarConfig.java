package huck.pasta2.viewvars.auto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface ViewVarConfig {
	String printType();
	String subPrintType() default "";
	ViewVarType type();
	String name();
	String listItemName() default "";
}
