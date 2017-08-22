package huck.pasta2.sample.view;

import huck.common.utils.VelocityEngine;
import huck.pasta2.ViewController;
import huck.pasta2.anno.ConfigEntry;
import huck.pasta2.anno.ViewControllerParam;
import huck.pasta2.viewvars.ViewVarMap;

import java.io.Writer;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

@ViewControllerParam(
	requireConfig = {
		@ConfigEntry(name=VelocityViewController.NAME, value="default"),
		@ConfigEntry(name=VelocityViewController.DEFAULT_CONTENT_TYPE, value="text/html", required=false),
		@ConfigEntry(name=VelocityViewController.TEMPLATE_ENCODING, value="UTF-8", required=false),
		@ConfigEntry(name=VelocityViewController.OUTPUT_ENCODING, value="UTF-8", required=false),
	}
)
public class VelocityViewController extends ViewController {
	public final static String NAME = "NAME";
	public final static String DEFAULT_CONTENT_TYPE = "DEFAULT_CONTENT_TYPE";
	public final static String TEMPLATE_ENCODING = "TEMPLATE_ENCODING";
	public final static String OUTPUT_ENCODING = "OUTPUT_ENCODING";

	private ServletConfig servletConfig;
	private VelocityEngine velocityEngine;
	private String defaultContentType;
	private String outputEncoding;
	private String cdnRoot;
	
	public static class ViewVariablesContext extends VelocityContext {
		private ViewVarMap viewVars;

		public ViewVariablesContext(ViewVarMap viewVars)
		{
			this.viewVars = viewVars;
		}

		public Object put(String key, Object value) {
			return null;
		}

		public Object get(String key) {
			if( null == viewVars ) return null;
			try {
				return viewVars.get(key);
			} catch( Exception e ) {
				return null;
			}
		}

		public boolean containsKey(Object key) {
			if( null != key && (key instanceof String) ) {
				return viewVars.containsKey((String)key);
			} else {
				return false;
			}
		}

		public Object[] getKeys() {
			return null;
		}

		public Object remove(Object key) {
			return null;
		}
	}
	
	@Override
	public void init(ServletConfig servletConfig) throws Exception {
		String name = getConfig(NAME);
		Properties props = new Properties();
		props.load(servletConfig.getServletContext().getResourceAsStream("/WEB-INF/"+name+"_velocity.properties"));
		String contextPath = servletConfig.getServletContext().getRealPath("/");
		this.defaultContentType = getConfig(DEFAULT_CONTENT_TYPE);
		this.outputEncoding = getConfig(OUTPUT_ENCODING);
		String templateEncoding = getConfig(TEMPLATE_ENCODING);
		
		this.servletConfig = servletConfig;
		this.velocityEngine = new VelocityEngine(name, props, contextPath, templateEncoding);
	}

	@Override
	public Boolean isResourceExist(String path) {
		return velocityEngine.isResourceExist(path);
	}
	
	@Override
	public void showView(String path, HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception {
		ViewVariablesContext context = new ViewVariablesContext(viewVars);
		VelocityContext wrapContext = new VelocityContext(context);
		wrapContext.put("__req", req);
		wrapContext.put("__tools", new VelocityTools(req));
		wrapContext.put("__cdn", cdnRoot);
		res.setCharacterEncoding(outputEncoding);
		String mimeType = servletConfig.getServletContext().getMimeType(path);
		if (null == mimeType) {
		    mimeType = defaultContentType;
		}
		res.setContentType(mimeType);
		Writer wr = res.getWriter();
		if( velocityEngine.showView(path, wrapContext, wr) ) {
			return;
		} else {
			res.sendError(HttpServletResponse.SC_NOT_FOUND, "No Template:" + path);
		}
	}

	@Override
	public void showException(String path, Exception ex, HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception {
		throw ex;
	}

	@Override
	public void destroy() {
	}
}
