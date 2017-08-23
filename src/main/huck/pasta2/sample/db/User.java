package huck.pasta2.sample.db;

import huck.pasta2.viewvars.auto.ViewVarBean;
import huck.pasta2.viewvars.auto.ViewVarConfig;
import huck.pasta2.viewvars.auto.ViewVarType;

public class User implements ViewVarBean {
	private String userId;
	private String userInfo;
	
	@ViewVarConfig(name="userId", type=ViewVarType.ATTRIBUTE, printType="api")
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@ViewVarConfig(name="userInfo", type=ViewVarType.ATTRIBUTE, printType="api")
	public String getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}
}
