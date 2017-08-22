package huck.pasta2.sample.view;

import huck.pasta2.ViewController;
import huck.pasta2.anno.ConfigEntry;
import huck.pasta2.anno.ViewControllerParam;
import huck.pasta2.viewvars.ViewVarMap;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ViewControllerParam(
	requireConfig = {
		@ConfigEntry(name=ApiViewController.OUTPUT_ENCODING, value="UTF-8", required=false)
	}
)
public class ApiViewController extends ViewController {
	public final static String OUTPUT_ENCODING = "OUTPUT_ENCODING";
	private String encoding;
	
	@Override
	public void init(ServletConfig servletConfig) throws Exception {
		this.encoding = getConfig(OUTPUT_ENCODING);
	}

	@Override
	public Boolean isResourceExist(String path) {
		return null;
	}
	
	@Override
	public void showView(String path, HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception {
		String result = APIViewUtils.renderView(path, extension, viewVars, extension);
		byte[] resultBytes = result.getBytes(encoding);
		String contentType;
		if( "json".equalsIgnoreCase(extension) ) {
			contentType="text/json";
		} else {
			contentType = "text/xml";
		}
		res.setContentLength(resultBytes.length);
		res.setContentType(contentType);
		res.getOutputStream().write(resultBytes);
		res.getOutputStream().flush();
	}

	@Override
	public void showException(String path, Exception ex, HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception {
		String result = APIViewUtils.renderException(path, ex, extension, viewVars, encoding);
		byte[] resultBytes = result.getBytes(encoding);
		String contentType;
		if( "json".equalsIgnoreCase(extension) ) {
			contentType="text/json";
		} else {
			contentType = "text/xml";
		}
		res.setContentLength(resultBytes.length);
		res.setContentType(contentType);
		res.getOutputStream().write(resultBytes);
		res.getOutputStream().flush();
	}

	@Override
	public void destroy() {
	}
}
