package huck.servlet.utils;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import huck.pasta2.sample.common.APICode;
import huck.pasta2.sample.common.APIException;
import huck.pasta2.sample.servlet.Environment;
import huck.pasta2.sample.servlet.Environment.ServerMode;
import huck.pasta2.viewvars.ViewVarMap;

public class ParameterGetter {
	public static String getClientIp(HttpServletRequest req) {
		String remoteIP;
		remoteIP = req.getHeader("X-Real-IP");
		if( null != remoteIP ) {
			return remoteIP;
		}
		remoteIP = req.getRemoteAddr();
		return remoteIP;
	}
	
	public static String getString(HttpServletRequest req, String name, boolean required) throws APIException {
		String value = req.getParameter(name);
		if( null != value ) {
			value = value.trim();
			if( 0 < value.length() ) {
				return value;
			}
		}
		if( required ) {
			ViewVarMap map = new ViewVarMap();
			map.setAttribute("name", name);
			throw new APIException(APICode.COMMON__PARAMETER_REQUIRED, map);
		} else {
			return null;
		}
	}
	
	public static Integer getInt(HttpServletRequest req, String name, boolean required) throws APIException {
		String value = getString(req, name, required);
		if( null != value ) {
			try {
				return Integer.parseInt(value);
			} catch( NumberFormatException ex ) {
			}			
		}
		if( required ) {
			ViewVarMap map = new ViewVarMap();
			map.setAttribute("name", name);
			throw new APIException(APICode.COMMON__PARAMETER_REQUIRED, map);
		} else {
			return null;
		}
	}
	
	public static Long getLong(HttpServletRequest req, String name, boolean required) throws APIException {
		String value = getString(req, name, required);
		if( null != value ) {
			try {
				return Long.parseLong(value);
			} catch( NumberFormatException ex ) {
			}			
		}
		if( required ) {
			ViewVarMap map = new ViewVarMap();
			map.setAttribute("name", name);
			throw new APIException(APICode.COMMON__PARAMETER_REQUIRED, map);
		} else {
			return null;
		}
	}
	
	public static String getContextUrl(HttpServletRequest req) {
		String contextPath;
		String proxyContextPath = req.getHeader("X-Original-Context");
		if( null != proxyContextPath ) {
			contextPath = proxyContextPath;
		} else {
			contextPath = req.getContextPath();
		}
		contextPath = (null==contextPath || contextPath.equals("/")) ? "" : contextPath;
		String port = (req.getServerPort()>0&&req.getServerPort()!=80)?":"+req.getServerPort():"";
		String protocol;
		if(ServerMode.LOCAL == Environment.getServerMode(req.getServletContext())) {
			protocol = "http";
		} else {
			protocol = "https";
		}
		String url = protocol+"://"+req.getServerName() + port + contextPath;
		
		return url;
	}
	
	public static String getRequestUri(HttpServletRequest req) {
		String proxyURI = req.getHeader("X-Original-URI");
		if( null != proxyURI ) {
			return proxyURI;
		}
		Object obj = req.getAttribute("javax.servlet.forward.request_uri");
		if( null != obj && (obj instanceof String) ) {
			return (String)obj;
		} else {
			return req.getRequestURI();
		}
	}

	public static String getClientLocale(HttpServletRequest req) {
		if( null == req.getHeader("Accept-Language") ) {
			return null;
		} else {
			Locale locale = req.getLocale();
			return locale.toLanguageTag();
		}
	}
}
