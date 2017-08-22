package huck.pasta2.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import huck.pasta2.ViewController;
import huck.pasta2.view.DoNothingViewController;

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface ViewTypeParam {
	String name();
	boolean actionRequired();

	Class<? extends ViewController> viewController() default DoNothingViewController.class;
	boolean defaultView() default false;
}
