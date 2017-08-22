package huck.pasta2.sample.view;

import huck.servlet.utils.RequestUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;

public class VelocityTools {
	private HttpServletRequest req;

	public VelocityTools(HttpServletRequest req) {
		this.req = req;
	}

	public String contextUrl() {
		return RequestUtil.contextUrl(req); 
	}

	public String urlEncoding(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public String escapeXml(String str) {
		if( null == str ) {
			return str;
		}
		try {
			return StringEscapeUtils.escapeXml(str);
		} catch( Exception ignore ) {
			return null;
		}
	}

	public String getRequestUri() {
		return RequestUtil.getRequestUri(req);
	}
	public String currentUrlWithParam() {
		String url = contextUrl();
		String path = req.getServletPath() + (null == req.getPathInfo() ? "" : req.getPathInfo());

		StringBuffer params = new StringBuffer();
		Enumeration<String> paramNames = (Enumeration<String>)req.getParameterNames();
		while( paramNames.hasMoreElements() ) {
			String name = paramNames.nextElement();
			String[] values = req.getParameterValues(name);
			for( String value : values ) {
				params.append("&").append(urlEncoding(name)).append("=").append(urlEncoding(value));
			}
		}
		params.replace(0, 1, "?");
		return url + path + params;
	}
	
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}
	public String currentTime() {
		SimpleDateFormat fmt = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.S");
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		return fmt.format(new Date(System.currentTimeMillis()));
	}
	public String javascriptNewDateString() {
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		String str = String.format("new Date(%d,%d,%d,%d,%d,%d, %d);", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
		return str;
	}
	
	public static void main( String... args) throws Exception {
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		String str = String.format("new Date(%d,%d,%d,%d,%d,%d, %d);", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
		System.out.println(str);
		
	}
}
