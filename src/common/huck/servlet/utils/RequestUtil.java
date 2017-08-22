package huck.servlet.utils;

import huck.pasta2.sample.common.APICode;
import huck.pasta2.sample.common.APIException;
import huck.pasta2.viewvars.ViewVarMap;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestUtil {
	public static String contextUrl(HttpServletRequest req) {
		String contextPath;
		String proxyContextPath = req.getHeader("X-Original-Context");
		if( null != proxyContextPath ) {
			contextPath = proxyContextPath;
		} else {
			contextPath = req.getContextPath();
		}
		contextPath = (null==contextPath || contextPath.equals("/")) ? "" : contextPath;
		String port = (req.getServerPort()>0&&req.getServerPort()!=80)?":"+req.getServerPort():"";
		String protocol = req.getScheme();
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
	
	public static String getTrimParam(HttpServletRequest req, String name, boolean required) throws APIException {
		String value = req.getParameter(name);
		if( null != value ) {
			value = value.trim();
			if( 0 != value.length() ) {
				return value;
			}
		}
		if( !required ) {
			return null;
		} else {
			ViewVarMap map = new ViewVarMap();
			map.setAttribute("required_parameter", name);
			throw new APIException(APICode.COMMON__PARAMETER_REQUIRED, map);
		}
	}
	
	public static void redirectToTop(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.sendRedirect(contextUrl(req) + "/");
	}
	
	public static void send404Error(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.sendError(HttpServletResponse.SC_NOT_FOUND, getRequestUri(req));
	}
}
