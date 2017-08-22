package huck.servlet.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {
	public static final int BROWSER_SESSION_COOKIE_AGE = -1;

	public static String getCookie(Cookie[] cookies, String cookieName) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookieName.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	public static void setCookie(HttpServletResponse response, String domain, String key, String value, boolean isHttpOnly) {
		Cookie cookie = new Cookie(key, value);
		cookie.setDomain(domain);
		cookie.setPath("/");
		cookie.setHttpOnly(isHttpOnly);
		response.addCookie(cookie);
	}

	public static void setCookie(HttpServletResponse response, String domain, String key, String value, int maxAge, boolean isHttpOnly) {
		Cookie cookie = new Cookie(key, value);
		cookie.setDomain(domain);
		cookie.setPath("/");
		cookie.setHttpOnly(isHttpOnly);
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}

	public static void deleteCookie(HttpServletResponse response, String domain, String key) {
		Cookie cookie = new Cookie(key, "");
		cookie.setDomain(domain);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

}
