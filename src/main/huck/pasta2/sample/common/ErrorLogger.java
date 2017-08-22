package huck.pasta2.sample.common;

import org.apache.log4j.Logger;

public class ErrorLogger {
	private final static String LOG_NAME ="error";

	public static void debug(String msg) {
		Logger.getLogger(LOG_NAME).debug(msg);
	}	
	public static void debug(String msg, Throwable t) {
		Logger.getLogger(LOG_NAME).debug(msg, t);
	}
	
	public static void info(String msg) {
		Logger.getLogger(LOG_NAME).info(msg);
	}	
	public static void info(String msg, Throwable t) {
		Logger.getLogger(LOG_NAME).info(msg, t);
	}
	
	public static void warn(String msg) {
		Logger.getLogger(LOG_NAME).warn(msg);
	}
	public static void warn(String msg, Throwable t) {
		Logger.getLogger(LOG_NAME).warn(msg, t);
	}

	public static void error(String msg) {
		Logger.getLogger(LOG_NAME).error(msg);
	}
	public static void error(String msg, Throwable t) {
		Logger.getLogger(LOG_NAME).error(msg, t);
	}
	
	public static void fatal(String msg) {
		Logger.getLogger(LOG_NAME).fatal(msg);
	}
	public static void fatal(String msg, Throwable t) {
		Logger.getLogger(LOG_NAME).fatal(msg, t);
	}
}
