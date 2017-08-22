package huck.pasta2.sample.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.ViewController;
import huck.pasta2.anno.ConfigEntry;
import huck.pasta2.anno.ViewControllerParam;
import huck.pasta2.viewvars.ViewVarMap;

@ViewControllerParam(
	requireConfig = {
		@ConfigEntry(name=StaticViewController.RESOURCE_ROOT_PATH, value="")
	}
)
public class StaticViewController extends ViewController {
	public static final String RESOURCE_ROOT_PATH = "resourceRootPath";

	private ServletContext servletContext;
	private File rootDir;


	@Override
	public void init(ServletConfig servletConfig) throws Exception {
		this.servletContext = servletConfig.getServletContext();

		String rootPath = getConfig(RESOURCE_ROOT_PATH);
		File rootDir = new File(servletContext.getRealPath(rootPath));

		if (!rootDir.exists()) {
			new ServletException("invalid root directory - " + rootPath);
		}
		this.rootDir = rootDir;
	}
	
	private File findFile(String path) {
		File file = new File(rootDir, path);
		if( file.isFile() ) {
			return file;
		} else {
			return null;
		}
	}
	@Override
	public Boolean isResourceExist(String path) {
		File file = findFile(path);
		return null != file;
	}
	
	@Override
	public void showView(String path, HttpServletRequest req, HttpServletResponse res, String extension, ViewVarMap viewVars) throws Exception {
		File file = findFile(path);
		if( null != file ) {
			String mimeType = servletContext.getMimeType(file.getName());
			if (null == mimeType) {
				mimeType = "application/octet-stream";
			}
			long lastModified = file.lastModified();
			if( lastModified > req.getDateHeader("If-Modified-Since") ) {
				res.setContentType(mimeType);
				res.setContentLength((int) file.length());
				res.setDateHeader("Last-Modified", file.lastModified());
				res.setHeader("Cache-Control", "must-revalidate");
				FileInputStream fin = new FileInputStream(file);
				try {
					OutputStream out = res.getOutputStream();

					byte[] buf = new byte[10240];
					int readLen;
					while( 0 < (readLen=fin.read(buf)) ) {
						out.write(buf, 0, readLen);
					}
				} finally {
					fin.close();
				}
			} else {
				res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			}
		} else {
			System.out.println("not found");
			res.sendError(404, "File Not Found - " + path);
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
