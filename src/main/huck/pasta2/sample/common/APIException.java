package huck.pasta2.sample.common;

import huck.pasta2.viewvars.ViewVarMap;

public class APIException extends Exception {
	private static final long serialVersionUID = 378401073720100197L;
	private APICode apiCode;
	private ViewVarMap additionalInfoMap;
	
	public APIException(APICode apiCode) {
		this(apiCode, null, null);
	}
	
	public APIException(APICode apiCode, ViewVarMap additionalInfoMap) {
		this(apiCode, additionalInfoMap, null);
	}
	
	public APIException(APICode apiCode, Throwable causeEx) {
		this(apiCode, null, causeEx);
	}
	
	public APIException(APICode apiCode, ViewVarMap additionalInfoMap, Throwable causeEx) {
		super(apiCode.getCodeMsg(), causeEx);
		this.apiCode = apiCode;
		this.additionalInfoMap = additionalInfoMap;
	}
	
	public APICode getApiCode() {
		return apiCode;
	}
	public ViewVarMap getAdditionalInfoMap() {
		return additionalInfoMap;
	}
}