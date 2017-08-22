package huck.pasta2;


import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.viewvars.ViewVarMap;

public abstract class ViewController {
	private boolean actionRequired;
	void setActionRequired(boolean actionRequired) {
		this.actionRequired = actionRequired;
	}
	boolean isActionRequired() {
		return actionRequired;
	}

	private static HashMap<String,HashMap<String,String>> controllerConfigMap = new HashMap<String,HashMap<String,String>>();
	public static void addConfig(String name, String configName, String configValue) {
		HashMap<String,String> configMap = controllerConfigMap.get(name);
		if( null == configMap ) {
			configMap = new HashMap<String,String>();
			controllerConfigMap.put(name, configMap);
		}
		configMap.put(configName, configValue);
	}
	static HashMap<String,String> getConfigMap(String name) {
		return controllerConfigMap.get(name);
	}

	private HashMap<String,String> configMap;
	void setConfigMap(HashMap<String,String> configMap) {
		this.configMap = configMap;
	}

	protected String getConfig(String name) {
		return configMap.get(name);
	}

	abstract public void init(ServletConfig servletConfig) throws Exception;
	abstract public void destroy();

	abstract public Boolean isResourceExist(String path);
	abstract public void showView(String path, HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception;

	abstract public void showException(String path, Exception ex, HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception;
}
