package huck.pasta2.sample.servlet;

import javax.servlet.ServletConfig;

import huck.pasta2.Pasta2Servlet;
import huck.pasta2.ViewController;
import huck.pasta2.anno.DirectoryParam;
import huck.pasta2.anno.RewriteParam;
import huck.pasta2.anno.ServletParam;
import huck.pasta2.anno.ViewMappingParam;
import huck.pasta2.anno.ViewTypeParam;
import huck.pasta2.sample.view.ApiViewController;
import huck.pasta2.sample.view.StaticViewController;
import huck.pasta2.sample.view.VelocityViewController;
import huck.pasta2.view.DoNothingViewController;

@ServletParam(
	defaultRequestEncoding = "UTF-8",
	actionPackage = "huck.pasta2.sample.servlet.actions",
	viewTypes = {
		@ViewTypeParam(
			name = "API",
			viewController = ApiViewController.class,
			actionRequired = true
		),
		@ViewTypeParam(
			name = "Velocity",
			viewController = VelocityViewController.class,
			actionRequired = false
		),
		@ViewTypeParam(
			name = "Static",
			viewController = StaticViewController.class,
			actionRequired = false
		),
		@ViewTypeParam(
			name = "DoNothing",
			viewController = DoNothingViewController.class,
			actionRequired = true
		),
	},
	rewriteRules = {
		//@RewriteParam(pattern="/home/([^\\/\\.]*)\\/?", replace="/home/Home.html?userid={0}", stop=true, redirect=false),
		@RewriteParam(pattern="(.*)\\/", replace="{0}/index.html", stop=true, redirect=false),
		@RewriteParam(pattern="([^.]*)", replace="{0}/", stop=true, redirect=true),
	},
	directorys = {
		@DirectoryParam(
			path = "",
			exceptionHandlers = { AllExceptionHandler.class },
			filters = {},
			viewMappings = {
				@ViewMappingParam(name="API", extensions = { "json" }),
				@ViewMappingParam(name="DoNothing", extensions = { "action" }),
				@ViewMappingParam(name="Velocity", extensions = { "", "htm", "html" }),
				@ViewMappingParam(name="Static", extensions = { }),
			}
		),
		@DirectoryParam(
			path = "/static",
			exceptionHandlers = { AllExceptionHandler.class },
			viewMappings = {
				@ViewMappingParam(name="Static", extensions = { }),
			}
		)
	}
)
public class MainServlet extends Pasta2Servlet {
	private static final long serialVersionUID = 4712215643320337655L;

	@Override
	protected void initPasta(ServletConfig servletConfig) throws Exception {
		Environment.checkInitException(getServletContext());
		ViewController.addConfig("Static", StaticViewController.RESOURCE_ROOT_PATH, "/WEB-INF/static_resources");
		ViewController.addConfig("API", ApiViewController.OUTPUT_ENCODING, "UTF-8");
		ViewController.addConfig("Velocity", VelocityViewController.NAME, "default");
		ViewController.addConfig("Velocity", VelocityViewController.TEMPLATE_ENCODING, "UTF-8");
	}

	@Override
	protected void destroyPasta() {
	}
}
