package huck.pasta2.sample.servlet.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.Action;
import huck.pasta2.anno.ActionParam;
import huck.pasta2.sample.db.User;
import huck.pasta2.sample.modules.SampleModule;
import huck.pasta2.sample.servlet.Modules;
import huck.pasta2.viewvars.ViewVarMap;
import huck.servlet.utils.ParameterGetter;

@ActionParam(
	extensions = { "json", "xml" },
	filters = {}
)
public class GetUser extends Action {
	@Override
	public boolean doAction(HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception {
		String userId = ParameterGetter.getString(req, "userId", true);
		SampleModule sampleModule = Modules.inst(getServletContext(), SampleModule.class);
		User user = sampleModule.getUser(userId);
		
		viewVars.setChildMap("user", user, "api");
		return true;
	}
}