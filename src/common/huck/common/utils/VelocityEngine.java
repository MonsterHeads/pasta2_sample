package huck.common.utils;

import java.io.File;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;

public class VelocityEngine {
	private org.apache.velocity.app.VelocityEngine ve;
	private String templateEncoding;
	
	public VelocityEngine(String name, Properties config, String contextPath, String templateEncoding) throws Exception {
		String logDir = null;
		String velocityLogDir = System.getProperty("velocity_log_dir");
		if( null != velocityLogDir ) {
			File logDirFile = new File(velocityLogDir);
			if( logDirFile.isDirectory() ) {
				logDir = logDirFile.getAbsolutePath();
			}
		}
		if( null == logDir ) {
			File logDirFile = new File(contextPath);
			logDir = logDirFile.getAbsolutePath() + "/WEB-INF";
		}

		config.setProperty("runtime.log", logDir + "/velocity_"+name+".log");
		config.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, contextPath + "/WEB-INF/"+name+"_templates");
		ve = new org.apache.velocity.app.VelocityEngine(config);

		this.templateEncoding = templateEncoding;
	}

	public Template findTemplate(String path) {
		try {
			return ve.getTemplate(path,templateEncoding);
		} catch( ResourceNotFoundException ex ) {
			return null;
		}
	}
	
	public Boolean isResourceExist(String path) {
		Template template = findTemplate(path);
		return null != template;
	}
	
	public boolean showView(String path, VelocityContext context, Writer wr) throws Exception {
		Template page = findTemplate(path);
		if( null != page ) {
			page = ve.getTemplate(path, templateEncoding);
			page.merge(context, wr);
			return true;
		} else {
			return false;
		}
	}
}
