package huck.pasta2.sample.servlet;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.log4j.PropertyConfigurator;

import huck.common.db.DBConnectionManager;
import huck.pasta2.sample.common.ErrorLogger;
import huck.pasta2.sample.common.SystemLogger;
import huck.pasta2.sample.modules.SampleModule;

public class Environment implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent evt) {
		ServletContext ctx = evt.getServletContext();
		
		String serverModeStr = System.getProperty("pasta2sample.server.mode");
		ServerMode serverMode = ServerMode.LOCAL;
		if( null != serverMode ) {
			try {
				serverMode = ServerMode.valueOf(serverModeStr.toUpperCase());
			} catch( Exception ignore ) {
			}
		}
		evt.getServletContext().setAttribute(ATTR_NAME_ServerMode, serverMode);

		String logConfPath;
		String mainDBURL;
		try {
			Class.forName(org.mariadb.jdbc.Driver.class.getName());
			Class.forName(org.apache.commons.dbcp2.PoolingDriver.class.getName());
			switch( serverMode ) {
			case SERVICE:
				logConfPath = "/WEB-INF/classes/service.log4j.properties";
				mainDBURL = "jdbc:mariadb://127.0.0.1:3306/pasta2_sample?characterEncoding=UTF-8";
				break;
			case TEST:
				logConfPath = "/WEB-INF/classes/test.log4j.properties";
				mainDBURL = "jdbc:mariadb://127.0.0.1:3306/pasta2_sample?characterEncoding=UTF-8";
				break;
			case LOCAL:
			default:
				logConfPath = "/WEB-INF/classes/local.log4j.properties";
				mainDBURL = "jdbc:mariadb://192.168.137.102:3306/pasta2_sample?characterEncoding=UTF-8";
				break;
			}
			
			PropertyConfigurator.configure(ctx.getRealPath(logConfPath));
			SystemLogger.info("[Pasta2Sample] SERVER MODE: "+serverMode + "; TimeZone="+TimeZone.getDefault().getDisplayName());

			registerDBConnPool("sample_maindb", mainDBURL, "root", "root");
			DBConnectionManager dbConnMgr = new DBConnectionManager("jdbc:apache:commons:dbcp:sample_maindb");
			Modules.setModuleInstance(ctx, new SampleModule(dbConnMgr));
		} catch(Exception ex) {
			ErrorLogger.fatal("[Pasta2Sample]", ex);
			evt.getServletContext().setAttribute(ATTR_NAME_InitException, ex);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent evt) {
		try {
			if( null != dbConnPoolNameSet ) {
				PoolingDriver dbcpDriver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
				for( String name : dbConnPoolNameSet ) {
					try{dbcpDriver.closePool(name);} catch(Exception ignore){}
				}
				dbConnPoolNameSet.clear();
			}
		} catch( Exception ex ) {
		}
	}
	private static TreeSet<String> dbConnPoolNameSet = new TreeSet<>();
	
	private static void registerDBConnPool(String name, String url, String user, String password) throws SQLException {
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, password);
		PoolableConnectionFactory poolfactory = new PoolableConnectionFactory(connectionFactory, null);
		poolfactory.setDefaultAutoCommit(true);
		poolfactory.setValidationQuery("SELECT 1");
		poolfactory.setMaxConnLifetimeMillis(600000); // 10 minutes
		
		GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>(poolfactory);
		poolfactory.setPool(connectionPool);
		connectionPool.setMaxIdle(50);
		connectionPool.setMinIdle(10);
		connectionPool.setLifo(true);
		connectionPool.setMaxTotal(200);
		connectionPool.setTestOnBorrow(true);
		connectionPool.setTestOnCreate(true);
		connectionPool.setTestWhileIdle(true);
		connectionPool.setTimeBetweenEvictionRunsMillis(60000); // 1 minute

		PoolingDriver dbcpDriver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
		dbcpDriver.registerPool(name, connectionPool);
		dbConnPoolNameSet.add(name);
	}
	
	public static String ATTR_NAME_ServerMode = Environment.class.getName()+".server_mode";
	public static String ATTR_NAME_InitException = Environment.class.getName()+".init_exception";

	public static void checkInitException(ServletContext servletContext) throws ServletException {
		Object obj = servletContext.getAttribute(ATTR_NAME_InitException);
		if( null != obj ) {
			if( obj instanceof ServletException ) {
				throw (ServletException)obj;
			} else if( obj instanceof Exception ) {
				throw new ServletException((Exception)obj);
			}
		}
	}
	public static ServerMode getServerMode(ServletContext servletContext) {
		Object obj = servletContext.getAttribute(ATTR_NAME_ServerMode);
		if( null != obj && (obj instanceof ServerMode) ) {
			return (ServerMode)obj;
		} else {
			return null;
		}
	}
	public static enum ServerMode {
		SERVICE, TEST, LOCAL 
	}
}
