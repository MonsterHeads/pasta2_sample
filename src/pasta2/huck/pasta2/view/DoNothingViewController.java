package huck.pasta2.view;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.ViewController;
import huck.pasta2.anno.ViewControllerParam;
import huck.pasta2.viewvars.ViewVarMap;

@ViewControllerParam
public final class DoNothingViewController extends ViewController {
	@Override
	public void init(ServletConfig servletConfig) throws Exception {
	}
	
	@Override
	public Boolean isResourceExist(String path) {
		return null;
	}

	@Override
	public void showView(String path, HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) {
		// Do Nothing
	}

	@Override
	public void showException(String path, Exception ex, HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception {
		throw ex;
	}

	@Override
	public void destroy() {
	}
}
