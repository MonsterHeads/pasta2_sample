package huck.pasta2.sample.servlet;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.ExceptionHandler;
import huck.pasta2.sample.common.ErrorLogger;
import huck.pasta2.viewvars.ViewVarMap;
import huck.servlet.utils.ParameterGetter;

public class AllExceptionHandler extends ExceptionHandler<Exception> {
	@Override
	public void init(ServletConfig servletConfig) throws Exception {
	}
	
	public static final String NO_LOG_ATTR = AllExceptionHandler.class.getName() + ".no_log";
	public static void setNoLog(HttpServletRequest req) {
		req.setAttribute(NO_LOG_ATTR, new Object());
	}

	public static Exception log(HttpServletRequest req, Exception ex) {
		String lineSeperator = System.getProperty("line.separator");
		StringBuffer buf = new StringBuffer();
		buf.append("[AllExceptionHandler]").append(req.getRequestURI()).append("?").append(req.getQueryString()).append(lineSeperator);
		String referer = req.getHeader("Referer");
		buf.append("REFERER=").append(null==referer?"":referer).append(lineSeperator);

		String userAgent = req.getHeader("User-Agent");
		buf.append("USER_AGENT=").append(null==userAgent?"":userAgent).append(lineSeperator);

		String proxyedIP = req.getHeader("X-Forwarded-For");
		if (null == proxyedIP || 0 >= proxyedIP.length()) {
			buf.append("IP=").append(ParameterGetter.getClientIp(req)).append(lineSeperator);
		} else {
			buf.append("PROXY_IP=").append(req.getRemoteAddr()).append(lineSeperator);
			buf.append("IP=").append(proxyedIP).append(lineSeperator);
		}

		buf.append("PARAMETERS").append(lineSeperator);
		Enumeration<?> keyEnum = req.getParameterNames();
		while( keyEnum.hasMoreElements())
		{
			String key = (String)keyEnum.nextElement();
			String[] paramList = req.getParameterValues(key);
			if( null != paramList ) {
				for( String value : paramList ) {
					buf.append("\t").append(key).append("=").append(value).append(lineSeperator);
				}
			}
		}
		ErrorLogger.error(buf.toString(), ex);
		return ex;
	}

	@Override
	public Exception handleException(Exception ex, String path,	HttpServletRequest req, HttpServletResponse res, ViewVarMap viewVars) {
		if( null == req.getAttribute(NO_LOG_ATTR) ) return log(req, ex);	
		else return ex;
	}

	@Override
	public void destroy() {
	}

}
