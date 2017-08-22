package huck.pasta2.sample.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;

public class Modules {
	public static void setModuleInstance(ServletContext ctx, Object module) throws IOException {
		ctx.setAttribute(module.getClass().getName()+"._module_instance", module);
	}
	
	public static <T> T inst(ServletContext ctx, Class<T> moduleCls) {
		Object obj = ctx.getAttribute(moduleCls.getName()+"._module_instance");
		if( null != obj && moduleCls.isInstance(obj) ) {
			@SuppressWarnings("unchecked")
			T module = (T)obj;
			return module;
		}
		return null;
	}
}
