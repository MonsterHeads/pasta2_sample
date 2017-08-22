package huck.pasta2;


import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.anno.ActionFilterParam;
import huck.pasta2.viewvars.ViewVarMap;


@ActionFilterParam
public abstract class ActionFilter {
	abstract public void init(ServletConfig servletConfig) throws Exception;
	abstract public void destroy();
	abstract public void preDoAction(HttpServletRequest req, HttpServletResponse res, String path, String extension, ViewVarMap viewVars) throws Exception;
	abstract public void afterDoAction(HttpServletRequest req, HttpServletResponse res, String path, String extension, ViewVarMap viewVars) throws Exception;

	private ThreadLocal<HashMap<Class<? extends Annotation>, Annotation>> threadAnnoMap = new ThreadLocal<HashMap<Class<? extends Annotation>, Annotation>>();

	void setThreadAnnoMap(HashMap<Class<? extends Annotation>, Annotation> requireAnnotationMap) {
		threadAnnoMap.set(requireAnnotationMap);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Annotation> T getRequireAnnotation(Class<T> annotationClass) {
		HashMap<Class<? extends Annotation>, Annotation> requireAnnotationMap = threadAnnoMap.get();
		return (T)requireAnnotationMap.get(annotationClass);
	}
}
