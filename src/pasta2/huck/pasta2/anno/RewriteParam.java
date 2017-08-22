package huck.pasta2.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface RewriteParam {
	String pattern();
	String replace();
	boolean redirect();
	boolean stop();
}
