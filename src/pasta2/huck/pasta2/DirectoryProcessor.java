package huck.pasta2;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.viewvars.ViewVarMap;


public class DirectoryProcessor {
	private HashMap<Class<? extends Exception>, ExceptionHandler<? extends Exception>> exHandlerMap;
	private ViewController defaultViewController;
	private LinkedList<ActionFilter> actionFilterList;
	private HashMap<String, ViewController> viewControllerExtMap;
	private HashMap<String, ActionProcessor> actionProcessorMap = new HashMap<String, ActionProcessor>();

	void setActionFilterList(LinkedList<ActionFilter> actionFilterList) {
		this.actionFilterList = actionFilterList;
	}
	void setExHandlerMap(HashMap<Class<? extends Exception>, ExceptionHandler<? extends Exception>> exHandlerMap) {
		this.exHandlerMap = exHandlerMap;
	}
	void setDefaultViewController(ViewController defaultViewController) {
		this.defaultViewController = defaultViewController;
	}
	void setViewControllerExtMap(HashMap<String, ViewController> viewControllerExtMap) {
		this.viewControllerExtMap = viewControllerExtMap;
	}

	void addActionProcessor(ActionProcessor actionProcessor) throws Exception {
		actionProcessorMap.put(actionProcessor.path(), actionProcessor);
		actionProcessor.setViewControllerIfNotSet(defaultViewController, viewControllerExtMap);
		actionProcessor.setExHandlerIfNotSet(exHandlerMap);
		actionProcessor.addFilterToFirst(actionFilterList);
	}

	private void send404(String path, HttpServletResponse res) throws IOException {
		res.sendError(HttpServletResponse.SC_NOT_FOUND, path);
	}

	void process(HttpServletRequest req, HttpServletResponse res, String path, String name, String extension, String requestPath) throws IOException, ServletException {
		// process action
		boolean showView = true;
		Exception processEx = null;
		ViewController viewController = null;

		ViewVarMap viewVars = new ViewVarMap();
		ActionProcessor ap = actionProcessorMap.get(name);
		if( null == ap ) {
			viewController = viewControllerExtMap.get(extension); 
			if( null == viewController ) {
				if( null != defaultViewController ) {
					viewController = defaultViewController;
				} else {
					send404("No View", res);
					return;
				}
			} else if( viewController.isActionRequired() ) {
				send404("No Action", res);
				return;
			}
		} else {
			viewController = ap.getViewController(extension);
			if( null == viewController ) {
				send404("No ViewControl for Action", res);
				return;
			}
			Boolean isExist = viewController.isResourceExist(path);
			if( null != isExist && !isExist ) {
				send404("No View for Action", res);
				return;				
			}

			try {
				showView = ap.process(extension, req, res, viewVars);
			} catch (Exception ex) {
				processEx = ex;
			}
		}

		if( null != processEx ) {
			Class<? extends Exception> eCls = processEx.getClass();
			@SuppressWarnings("rawtypes")
			ExceptionHandler exHandler = ap.getExceptionHandler(eCls);

			while( null == exHandler ) {
				if( Throwable.class.equals(eCls) ) {
					break;
				}
				@SuppressWarnings("unchecked")
				Class<? extends Exception> superCls = (Class<? extends Exception>)eCls.getSuperclass();
				eCls = superCls;
				exHandler = exHandlerMap.get(eCls);
			}
			if( null != exHandler ) {
				@SuppressWarnings("unchecked")
				Exception handledEx = exHandler.handleException(processEx, path, req, res, viewVars); 
				processEx = handledEx;
			}
		}
		if( res.isCommitted() ) {
			return;
		}

		// process view
		try {
			if( null == processEx ) {
				if( showView ) {
					try {
						viewController.showView(path, req, res, extension, viewVars);
					} catch( Exception ex ) {
						processEx = ex;
					}
				}
			}
			if( null != processEx ) {
				processEx.printStackTrace();
				viewController.showException(path, processEx, req, res, extension, viewVars);
			}
		} catch( IOException ioE ) {
			throw ioE;
		} catch( ServletException servletE ) {
			throw servletE;
		} catch( Exception exception ) {
			throw new ServletException(exception);
		}

	}
}
