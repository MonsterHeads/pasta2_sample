package huck.pasta2;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.anno.ActionFilterParam;
import huck.pasta2.anno.ActionParam;
import huck.pasta2.anno.ConfigEntry;
import huck.pasta2.anno.DirectoryParam;
import huck.pasta2.anno.ServletParam;
import huck.pasta2.anno.ViewControllerParam;
import huck.pasta2.anno.ViewMappingParam;
import huck.pasta2.anno.ViewTypeParam;

public abstract class Pasta2Servlet extends HttpServlet {
	abstract protected void initPasta(ServletConfig servletConfig) throws Exception;

	abstract protected void destroyPasta();

	private void send404(String path, HttpServletResponse res) throws IOException {
		res.sendError(HttpServletResponse.SC_NOT_FOUND, path);
	}

	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String requestCharEnc = req.getCharacterEncoding();
		if (null == requestCharEnc) {
			req.setCharacterEncoding(defaultRequestEncoding);
		}
		String path = req.getServletPath() + (null == req.getPathInfo() ? "" : req.getPathInfo());

		String contextPath = req.getContextPath();
		contextPath = (null == contextPath || contextPath.equals("/")) ? "" : contextPath;
		String port = req.getServerPort() > 0 ? ":" + req.getServerPort() : "";
		String requestPath = (req.isSecure() ? "https" : "http") + "://" + req.getServerName() + port + contextPath
				+ path;

		String queryString = req.getQueryString();
		if (null == queryString)
			queryString = "";

		boolean updated = rewriteRuleManager.process(path, req, res);
		if (updated) {
			return;
		}

		// parsing name and extension
		String name;
		String extension;
		int lastSlashIndex = path.lastIndexOf('/');
		int lastDotIndex = path.lastIndexOf('.');
		if (-1 == lastDotIndex || (0 < lastSlashIndex && lastSlashIndex > lastDotIndex)) {
			lastDotIndex = path.length();
			extension = "";
		} else {
			extension = path.substring(lastDotIndex + 1);
		}
		name = path.substring(0, lastDotIndex);
		name = name.toLowerCase();

		DirectoryProcessor dirProcessor = actionDirProcessorMap.get(name);
		if (null == dirProcessor) {
			String[] dirs = parsePathToDirs(name);
			for (String dir : dirs) {
				dirProcessor = dirProcessorMap.get(dir);
				if (null != dirProcessor) {
					break;
				}
			}
		}
		if (null == dirProcessor) {
			send404("No Directory", res);
			return;
		}
		dirProcessor.process(req, res, path, name, extension, requestPath);
	}

	@Override
	public final void destroy() {
		for (ActionFilter filter : filterInstMap.values()) {
			filter.destroy();
		}
		for (ExceptionHandler<?> exHandler : exHandlerInstMap.values()) {
			exHandler.destroy();
		}
		for (ViewController viewController : viewControllerMap.values()) {
			viewController.destroy();
		}
		destroyPasta();
	}

	@Override
	public final void init() throws ServletException {
		try {
			ServletConfig servletConfig = this.getServletConfig();
			initPasta(servletConfig);

			Class<? extends Pasta2Servlet> servletClass = this.getClass();
			ServletParam param = servletClass.getAnnotation(ServletParam.class);
			if (null == param) {
				throw new Exception("need ServletParam Annotation for " + servletClass.getSimpleName());
			}

			rewriteRuleManager = new RewriteRuleManager(param.rewriteRules());

			defaultRequestEncoding = param.defaultRequestEncoding();

			exHandlerInstMap = new HashMap<Class<? extends ExceptionHandler<? extends Exception>>, ExceptionHandler<? extends Exception>>();
			filterInstMap = new HashMap<Class<? extends ActionFilter>, ActionFilter>();

			viewControllerMap = initViewControllers(param.viewTypes());
			dirProcessorMap = initDirProcessor(param.directorys(), viewControllerMap, exHandlerInstMap);

			LinkedList<ActionClassWrap> actionClassList = getActionClassList(param.actionPackage());
			LinkedList<ActionProcessor> actionProcessorList = initActionProcessorList(actionClassList,
					viewControllerMap, exHandlerInstMap, filterInstMap);

			actionDirProcessorMap = new HashMap<String, DirectoryProcessor>();
			for (ActionProcessor actionProcessor : actionProcessorList) {
				for (String dir : actionProcessor.dirs()) {
					DirectoryProcessor dirProcessor = dirProcessorMap.get(dir);
					if (null != dirProcessor) {
						actionDirProcessorMap.put(actionProcessor.path(), dirProcessor);
						dirProcessor.addActionProcessor(actionProcessor);
						break;
					}
				}
			}

		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

	static class ActionClassWrap {
		Class<? extends Action> actionClass;
		String path;
		String[] dirs;
	}

	private HashMap<String, ViewController> initViewControllers(ViewTypeParam[] viewTypes) throws Exception {
		HashMap<String, ViewController> viewControllerMap = new HashMap<String, ViewController>();
		for (ViewTypeParam viewTypeParam : viewTypes) {
			String viewControllerName = viewTypeParam.name();
			if (viewControllerMap.containsKey(viewControllerName)) {
				throw new Exception("duplicated ViewController: " + viewControllerName);
			}
			ViewControllerParam viewControllerParam = viewTypeParam.viewController()
					.getAnnotation(ViewControllerParam.class);
			if (null == viewControllerParam) {
				throw new Exception("need ViewControllerParam Annotation: " + viewTypeParam.viewController().getName());
			}

			HashMap<String, String> configMap = ViewController.getConfigMap(viewControllerName);
			if (null == configMap)
				configMap = new HashMap<String, String>();
			ConfigEntry[] requiredConfigList = viewControllerParam.requireConfig();
			for (ConfigEntry requiredConfig : requiredConfigList) {
				if (!configMap.containsKey(requiredConfig.name())) {
					if (requiredConfig.required()) {
						throw new Exception(
								viewTypeParam.viewController().getName() + " : need \"" + requiredConfig + "\" config");
					} else {
						configMap.put(requiredConfig.name(), requiredConfig.value());
					}
				}
			}

			ViewController viewController = viewTypeParam.viewController().newInstance();
			viewController.setActionRequired(viewTypeParam.actionRequired());
			viewController.setConfigMap(configMap);
			viewController.init(getServletConfig());

			viewControllerMap.put(viewControllerName, viewController);
		}
		return viewControllerMap;
	}

	private Class<? extends Exception> getHandledException(
			Class<? extends ExceptionHandler<? extends Throwable>> exHandlerClass) throws Exception {
		for (Method m : exHandlerClass.getMethods()) {
			if ("handleException".equals(m.getName()) && !m.isBridge()) {
				Class<?>[] paramTypes = m.getParameterTypes();
				if (paramTypes.length == 5) {
					if (Throwable.class.isAssignableFrom(paramTypes[0])) {
						@SuppressWarnings("unchecked")
						Class<? extends Exception> handledException = (Class<? extends Exception>) paramTypes[0];
						return handledException;
					}
				}
			}
		}
		throw new Exception("invalid ExceptionHandler");
	}

	private HashMap<Class<? extends Exception>, ExceptionHandler<? extends Exception>> createExHandlerMap(
			Class<? extends ExceptionHandler<? extends Exception>>[] exHandlerClassList,
			HashMap<Class<? extends ExceptionHandler<? extends Exception>>, ExceptionHandler<? extends Exception>> exHandlerInstMap)
					throws Exception {
		HashMap<Class<? extends Exception>, ExceptionHandler<? extends Exception>> exHandlerMap = new HashMap<Class<? extends Exception>, ExceptionHandler<? extends Exception>>();
		for (Class<? extends ExceptionHandler<? extends Exception>> exHandlerClass : exHandlerClassList) {
			Class<? extends Exception> ex = getHandledException(exHandlerClass);
			Class<? extends Exception> exTmp = ex;
			boolean isExist = false;
			while (true) {
				if (exHandlerMap.containsKey(exTmp)) {
					isExist = true;
					break;
				}
				if (Throwable.class.equals(exTmp.getSuperclass())) {
					break;
				}
				@SuppressWarnings("unchecked")
				Class<? extends Exception> exTmp2 = (Class<? extends Exception>) exTmp.getSuperclass();
				exTmp = exTmp2;
			}
			if (!isExist) {
				ExceptionHandler<? extends Exception> exHandler = exHandlerInstMap.get(exHandlerClass);
				if (null == exHandler) {
					exHandler = exHandlerClass.newInstance();
					exHandlerInstMap.put(exHandlerClass, exHandler);
					exHandler.init(this.getServletConfig());
				}
				exHandlerMap.put(ex, exHandler);
			}
		}
		return exHandlerMap;
	}

	private HashMap<String, ViewController> createViewMapping(ViewMappingParam[] viewMappings,
			HashMap<String, ViewController> viewControllerMap) throws Exception {
		HashMap<String, ViewController> viewControllerExtMap = new HashMap<String, ViewController>();
		for (ViewMappingParam viewMapping : viewMappings) {
			String viewControllerName = viewMapping.name();
			ViewController viewController = viewControllerMap.get(viewControllerName);
			if (null == viewController) {
				throw new Exception("invalid view mapping: " + viewControllerName);
			}
			String[] extList = viewMapping.extensions();
			if (0 == extList.length) {
				if (viewControllerExtMap.containsKey(null)) {
					throw new Exception("duplicated default viewController");
				}
				viewControllerExtMap.put(null, viewController);
			} else {
				for (String ext : extList) {
					if (viewControllerExtMap.containsKey(ext)) {
						throw new Exception("duplicated extension : " + ext);
					}
					viewControllerExtMap.put(ext, viewController);
				}
			}
		}
		return viewControllerExtMap;
	}

	private HashMap<String, DirectoryProcessor> initDirProcessor(DirectoryParam[] dirs,
			HashMap<String, ViewController> viewControllerMap,
			HashMap<Class<? extends ExceptionHandler<? extends Exception>>, ExceptionHandler<? extends Exception>> exHandlerInstMap)
					throws Exception {
		HashMap<String, DirectoryProcessor> dirProcessorMap = new HashMap<String, DirectoryProcessor>();
		for (DirectoryParam dir : dirs) {
			String dirPath = dir.path().toLowerCase();
			if (dirProcessorMap.containsKey(dirPath)) {
				throw new Exception("duplicated dir: " + dirPath);
			}

			LinkedList<ActionFilter> actionFilterList = null;
			if (0 < dir.filters().length) {
				actionFilterList = createFilterList(dir.filters(), filterInstMap);
			}

			// initialize Exception Handler
			HashMap<Class<? extends Exception>, ExceptionHandler<? extends Exception>> exHandlerMap = createExHandlerMap(
					dir.exceptionHandlers(), exHandlerInstMap);

			// map extension with viewController
			HashMap<String, ViewController> viewControllerExtMap = createViewMapping(dir.viewMappings(),
					viewControllerMap);
			ViewController defaultViewController = viewControllerExtMap.get(null);

			DirectoryProcessor dirProcessor = new DirectoryProcessor();
			dirProcessor.setExHandlerMap(exHandlerMap);
			dirProcessor.setDefaultViewController(defaultViewController);
			dirProcessor.setViewControllerExtMap(viewControllerExtMap);
			dirProcessor.setActionFilterList(actionFilterList);
			dirProcessorMap.put(dirPath, dirProcessor);
		}
		return dirProcessorMap;
	}

	private void findAllClassFile(String prefix, File rootDir, LinkedList<String> classFiles) {
		File[] filelist = rootDir.listFiles();
		for (File file : filelist) {
			String name = file.getName();
			if (file.isDirectory()) {
				findAllClassFile(prefix + "/" + name, file, classFiles);
			} else {

				if (file.getName().endsWith(".class")) {
					classFiles.add(prefix + "/" + name.substring(0, name.length() - 6));
				}
			}
		}
	}

	private String[] parsePathToDirs(String path) {
		path = path.substring(1); // delete first slash
		String[] split = path.split("/");
		String[] dirs = new String[split.length];
		dirs[dirs.length - 1] = "";
		for (int i = dirs.length - 2; i >= 0; i--) {
			dirs[i] = dirs[i + 1] + "/" + split[dirs.length - 2 - i];
		}
		return dirs;
	}

	private LinkedList<ActionClassWrap> getActionClassList(String actionPackage) throws Exception {
		Class<? extends Pasta2Servlet> servletClass = this.getClass();
		ClassLoader servletClassLoader = servletClass.getClassLoader();
		String servletClassName = servletClass.getName();
		servletClassName = servletClassName.replace('.', '/') + ".class";
		URL url = servletClassLoader.getResource(servletClassName);

		if (null == url) {
			throw new Exception("error");
		}
		String servletClasspath = URLDecoder.decode(url.getPath(), "UTF-8");
		if (!servletClasspath.endsWith(servletClassName)) {
			throw new Exception("error");
		}
		String classRoot = servletClasspath.substring(0, servletClasspath.length() - servletClassName.length());
		File actionRootDir = new File(classRoot, actionPackage.replace('.', File.separatorChar));

		LinkedList<String> classFiles = new LinkedList<String>();
		findAllClassFile("", actionRootDir, classFiles);

		LinkedList<ActionClassWrap> actionClassList = new LinkedList<ActionClassWrap>();
		for (String classFile : classFiles) {
			String className = actionPackage + classFile.replace('/', '.');
			if (className.startsWith("."))
				className = className.substring(1);

			Class<?> c;
			try {
				c = servletClassLoader.loadClass(className);
			} catch (ClassNotFoundException ignore) {
				continue;
			}

			if (Action.class.isAssignableFrom(c)) {
				ActionClassWrap a = new ActionClassWrap();
				@SuppressWarnings("unchecked")
				Class<? extends Action> actionC = (Class<? extends Action>) c;

				a.actionClass = actionC;
				a.path = classFile.toLowerCase();
				a.dirs = parsePathToDirs(classFile.toLowerCase());
				actionClassList.add(a);
			}
		}
		return actionClassList;
	}

	private LinkedList<ActionFilter> createFilterList(Class<? extends ActionFilter>[] filters,
			HashMap<Class<? extends ActionFilter>, ActionFilter> filterInstMap) throws Exception {
		LinkedList<ActionFilter> actionFilterList = new LinkedList<ActionFilter>();
		for (Class<? extends ActionFilter> filterClass : filters) {
			ActionFilterParam filterParam = filterClass.getAnnotation(ActionFilterParam.class);
			if (null == filterParam) {
				throw new Exception("need ActionFilterParam Annotation : " + filterClass.getName());
			}

			ActionFilter filter = filterInstMap.get(filterClass);
			if (null == filter) {
				filter = filterClass.newInstance();
				filter.init(this.getServletConfig());
				filterInstMap.put(filterClass, filter);
			}
			actionFilterList.add(filter);
		}

		return actionFilterList;
	}

	private LinkedList<ActionProcessor> initActionProcessorList(LinkedList<ActionClassWrap> actionClassList,
			HashMap<String, ViewController> viewControllerMap,
			HashMap<Class<? extends ExceptionHandler<? extends Exception>>, ExceptionHandler<? extends Exception>> exHandlerInstMap,
			HashMap<Class<? extends ActionFilter>, ActionFilter> filterInstMap) throws Exception {
		LinkedList<ActionProcessor> processorList = new LinkedList<ActionProcessor>();
		for (ActionClassWrap actionClassWrap : actionClassList) {
			Class<? extends Action> actionClass = actionClassWrap.actionClass;
			ActionParam actionParam = actionClass.getAnnotation(ActionParam.class);
			if (null == actionParam) {
				throw new Exception("need ActionParam Annotation : " + actionClass.getName());
			}

			Action action = actionClass.newInstance();
			action.setServletContext(getServletContext());

			LinkedList<ActionFilter> actionFilterList = null;
			if (0 < actionParam.filters().length) {
				actionFilterList = createFilterList(actionParam.filters(), filterInstMap);
			}

			// initialize Exception Handler
			HashMap<Class<? extends Exception>, ExceptionHandler<? extends Exception>> exHandlerMap = null;
			if (0 < actionParam.exceptionHandlers().length) {
				exHandlerMap = createExHandlerMap(actionParam.exceptionHandlers(), exHandlerInstMap);
			}

			HashMap<String, ViewController> viewControllerExtMap = null;
			ViewController defaultViewController = null;

			if (0 < actionParam.viewMappings().length) {
				viewControllerExtMap = createViewMapping(actionParam.viewMappings(), viewControllerMap);
				defaultViewController = viewControllerExtMap.get(null);
			}

			HashSet<String> supportExtensionSet = new HashSet<String>();
			String[] extensionList = actionParam.extensions();
			for (String extension : extensionList) {
				supportExtensionSet.add(extension);
			}

			ActionProcessor actionProcessor = new ActionProcessor(actionClassWrap, action, supportExtensionSet,
					exHandlerMap, defaultViewController, viewControllerExtMap, actionFilterList);
			processorList.add(actionProcessor);
		}

		return processorList;
	}

	private String defaultRequestEncoding;
	private HashMap<String, ViewController> viewControllerMap;
	private RewriteRuleManager rewriteRuleManager;
	private HashMap<Class<? extends ExceptionHandler<? extends Exception>>, ExceptionHandler<? extends Exception>> exHandlerInstMap;
	private HashMap<Class<? extends ActionFilter>, ActionFilter> filterInstMap;
	private HashMap<String, DirectoryProcessor> dirProcessorMap;
	private HashMap<String, DirectoryProcessor> actionDirProcessorMap;

	private static final long serialVersionUID = 6918779965560132389L;
}
