package huck.pasta2;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.Pasta2Servlet.ActionClassWrap;
import huck.pasta2.anno.ActionFilterParam;
import huck.pasta2.viewvars.ViewVarMap;

class ActionProcessor {
	private static class ActionFilterProcessor {
		private ActionFilter filter;
		private HashMap<Class<? extends Annotation>, Annotation> requireAnnotationMap;

		public ActionFilterProcessor(ActionFilter filter, HashMap<Class<? extends Annotation>, Annotation> requireAnnotationMap) {
			this.filter = filter;
			this.requireAnnotationMap = requireAnnotationMap;
		}

		public void preDoAction(HttpServletRequest req, HttpServletResponse res, String path, String extension, ViewVarMap viewVars) throws Exception {
			filter.setThreadAnnoMap(requireAnnotationMap);
			filter.preDoAction(req, res, path, extension, viewVars);
		}
		public void afterDoAction(HttpServletRequest req, HttpServletResponse res, String path, String extension, ViewVarMap viewVars) throws Exception {
			filter.afterDoAction(req, res,path, extension, viewVars);
			filter.setThreadAnnoMap(null);
		}
	}

	private ActionClassWrap actionClassWrap;
	private Action action;
	private LinkedList<ActionFilterProcessor> actionFilterProcessorList = new LinkedList<ActionFilterProcessor>();
	private LinkedList<ActionFilterProcessor> actionFilterProcessorList_oppositeOrder = new LinkedList<ActionFilterProcessor>();
	private HashSet<String> supportExtensionSet;
	private HashMap<Class<? extends Exception>, ExceptionHandler<? extends Exception>> exHandlerMap;
	private ViewController defaultViewController;
	private HashMap<String, ViewController> viewControllerExtMap;

	ActionProcessor(
			ActionClassWrap actionClassWrap,
			Action action,
			HashSet<String> supportExtensionSet,
			HashMap<Class<? extends Exception>, ExceptionHandler<? extends Exception>> exHandlerMap,
			ViewController defaultViewController,
			HashMap<String, ViewController> viewControllerExtMap,
			LinkedList<ActionFilter> actionFilterList
	) throws Exception
	{
		this.actionClassWrap = actionClassWrap;
		this.action = action;
		this.supportExtensionSet = supportExtensionSet;
		this.exHandlerMap = exHandlerMap;
		this.defaultViewController = defaultViewController;
		this.viewControllerExtMap = viewControllerExtMap;
		addFilterToFirst(actionFilterList);
	}

	String path() {
		return actionClassWrap.path;
	}
	String[] dirs() {
		return actionClassWrap.dirs;
	}

	void setViewControllerIfNotSet( ViewController defaultViewController, HashMap<String, ViewController> viewControllerExtMap) {
		if( null != viewControllerExtMap && null == this.viewControllerExtMap ) {
			this.defaultViewController = defaultViewController;
			this.viewControllerExtMap = viewControllerExtMap;
		}
	}

	void setExHandlerIfNotSet( HashMap<Class<? extends Exception>, ExceptionHandler<? extends Exception>> exHandlerMap ) {
		if( null != exHandlerMap && null == this.exHandlerMap ) {
			this.exHandlerMap = exHandlerMap;
		}
	}

	void addFilterToFirst( LinkedList<ActionFilter> actionFilterList) throws Exception {
		LinkedList<ActionFilterProcessor> reverseList = new LinkedList<ActionFilterProcessor>();
		if( null != actionFilterList )
		for( ActionFilter filter : actionFilterList ) {
			ActionFilterParam filterParam = filter.getClass().getAnnotation(ActionFilterParam.class);
			HashMap<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
			Class<? extends Annotation>[] requireAnnotationList = filterParam.requireAnnotation();
			for( Class<? extends Annotation> requireAnnotationClass : requireAnnotationList ) {
				Annotation annotation = actionClassWrap.actionClass.getAnnotation(requireAnnotationClass);
				if( null == annotation ) {
					throw new Exception(actionClassWrap.actionClass.getName() + " : need " + requireAnnotationClass.getName() + " annotation for " + filter.getClass().getName() );
				}
				annotationMap.put(requireAnnotationClass, annotation);
			}
			ActionFilterProcessor actionFilterProcessor = new ActionFilterProcessor(filter, annotationMap);
			reverseList.addFirst(actionFilterProcessor);
		}

		for( ActionFilterProcessor filter : reverseList ) {
			actionFilterProcessorList.addFirst(filter);
			actionFilterProcessorList_oppositeOrder.addLast(filter);
		}
	}

	public boolean process(String extension, HttpServletRequest req, HttpServletResponse res, ViewVarMap viewVars) throws Exception {
		if( null != actionFilterProcessorList )
		for( ActionFilterProcessor filterProcessor : actionFilterProcessorList ) {
			filterProcessor.preDoAction(req, res, path(), extension, viewVars);
		}

		boolean result = action.doAction(req, res, extension, viewVars);

		if( null != actionFilterProcessorList_oppositeOrder )
		for( ActionFilterProcessor filterProcessor : actionFilterProcessorList_oppositeOrder ) {
			filterProcessor.afterDoAction(req, res, path(), extension, viewVars);
		}
		return result;
	}


	public ViewController getViewController(String extension) {
		if( supportExtensionSet.contains(extension) ) {
			ViewController viewController = viewControllerExtMap.get(extension);
			if( null != viewController ) {
				return viewController;
			} else if( null != defaultViewController ) {
				return defaultViewController;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	public ExceptionHandler<? extends Exception> getExceptionHandler(Class<? extends Exception> exCls) {
		return exHandlerMap.get(exCls);
	}
}
