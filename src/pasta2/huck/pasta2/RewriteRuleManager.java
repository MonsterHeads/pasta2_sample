package huck.pasta2;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import huck.pasta2.anno.RewriteParam;

public class RewriteRuleManager {
	public class RewriteRule {
		private Pattern pattern;
		private MessageFormat replace;
		private boolean redirect;
		private boolean stop;
		public RewriteRule(RewriteParam param) {
			pattern = Pattern.compile(param.pattern());
			replace = new MessageFormat(param.replace());
			redirect = param.redirect();
			stop = param.stop();
		}
		public boolean isStop() {
			return stop;
		}
		public boolean isRedirect() {
			return redirect;
		}
		public StringBuffer process(StringBuffer path) {
			Matcher m = pattern.matcher(path);
			if( !m.matches() ) {
				return null;
			}
			int count = m.groupCount();
			Object[] args = new Object[count];
			for( int i=0; i<count; i++ ) {
				args[i] = m.group(i+1);
			}
			path.setLength(0);
			StringBuffer replaced = replace.format(args, path, null);			
			return replaced;
		}
	}
	
	private ArrayList<RewriteRule> ruleList;
	public RewriteRuleManager(RewriteParam[] params) {
		ArrayList<RewriteRule> ruleList = new ArrayList<RewriteRule>();
		for( RewriteParam param : params ) {
			ruleList.add(new RewriteRule(param));
		}
		this.ruleList = ruleList;
	}
	
	public boolean process(String path, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		boolean isMatched = false;
		boolean isRedirect = false;
		StringBuffer buf = new StringBuffer(path);
		for( RewriteRule rule : ruleList ) {
			StringBuffer next = rule.process(buf);
			if( null != next ) {
				buf = next;
				isMatched = true;
				if( rule.isRedirect() ) {
					isRedirect = true;
					break;
				}
				if( rule.isStop() ) {
					break;
				}
			}
		}
		if( isMatched ) {
			String rewritePath = buf.toString();
			if( isRedirect ) {
				res.sendRedirect(rewritePath);
			} else {
				RequestDispatcher rd = req.getRequestDispatcher(rewritePath);
				rd.forward(req, res);
			}
			return true;
		} else {
			return false;
		}
	}
}
