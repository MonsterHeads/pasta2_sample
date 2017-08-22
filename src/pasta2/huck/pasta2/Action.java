package huck.pasta2;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.viewvars.ViewVarMap;

public abstract class Action {
	protected ServletContext servletContext;
	void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	abstract public boolean doAction(HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception;
}
