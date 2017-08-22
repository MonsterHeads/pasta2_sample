package huck.pasta2.sample.view;

import huck.pasta2.sample.common.APICode;
import huck.pasta2.sample.common.APIException;
import huck.pasta2.sample.view.XMLViewUtils.XMLElement;
import huck.pasta2.viewvars.ViewVarMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONObject;

public class APIViewUtils {
	private static String getRootName(String path, String extension) {
		int lastSlashIdx = path.lastIndexOf('/');
		String rootName = null;
		if( 0 < extension.length() ) {
			rootName = path.substring(lastSlashIdx+1,path.length()-extension.length()-1);
		} else {
			rootName = path.substring(lastSlashIdx+1,path.length());
		}
		return rootName;
	}
	private static ViewVarMap wrapViewVarsForAPI(APICode resultCode, ViewVarMap additionalInfoMap, ViewVarMap viewVars) {
		ViewVarMap map = new ViewVarMap();
		ViewVarMap api = map.createChildMap("api");
		api.setChildValue("code", resultCode.getCode());
		api.setChildValue("codeMsg", resultCode.getCodeMsg());
		if( null != additionalInfoMap ) {
			api.setChildMap("additionalInfo", additionalInfoMap);
		}
		map.setChildMap("result", viewVars);
		return map;		
	}
	private static String render(String path, String extension, ViewVarMap output, String encoding) throws IOException {
		if( "json".equalsIgnoreCase(extension) ) {
			JSONObject jsonOut = JSONViewUtils.convertViewVarMapToJSON(output);
			return jsonOut.toString();
		} else {
			XMLElement rootE = XMLViewUtils.convertViewVarMapToXMLElement(getRootName(path, extension), output);
			ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
			XMLViewUtils.exportXml(xmlOut, rootE, encoding);
			byte[] resultBytes = xmlOut.toByteArray();
			return new String(resultBytes, encoding);
		}
	}
	public static String renderView(String path, String extension, ViewVarMap viewVars, String encoding) throws IOException {
		ViewVarMap wrapped = wrapViewVarsForAPI(APICode.SUCCESS, null, viewVars);
		return render(path, extension, wrapped, encoding);
	}

	public static String renderException(String path, Exception ex, String extension, ViewVarMap viewVars, String encoding) throws IOException {
		ViewVarMap wrapped;
		if( ex instanceof APIException ) {
			APIException apiEx = (APIException)ex;
			wrapped = wrapViewVarsForAPI(apiEx.getApiCode(), apiEx.getAdditionalInfoMap(), viewVars);			
		} else {
			wrapped = wrapViewVarsForAPI(APICode.COMMON__INTERNAL_ERROR, null, viewVars);
		}
		
		return render(path, extension, wrapped, encoding);
	}
}
