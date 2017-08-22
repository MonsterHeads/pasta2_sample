package huck.pasta2.sample.servlet.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.Action;
import huck.pasta2.anno.ActionParam;
import huck.pasta2.viewvars.ViewVarMap;

@ActionParam(
	extensions = { "html" },
	filters = {}
)
public class Index extends Action {
	@Override
	public boolean doAction(HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception {
		viewVars.setAttribute("msg", "Hello World!");
		return true;
	}
}