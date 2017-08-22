package huck.pasta2.sample.db;

import huck.pasta2.viewvars.auto.ViewVarBean;

public class User implements ViewVarBean {
	private String userId;
	private String userInfo;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}
}
