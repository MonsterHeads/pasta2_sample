package huck.pasta2.sample.view;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import huck.pasta2.viewvars.ViewVarMap;
import huck.pasta2.viewvars.ViewVarMap.TraversalCallback;


public class XMLViewUtils {
	public static class XMLElement {
		public String name;
		public String value;
		public ArrayList<XMLElement> childList = new ArrayList<XMLElement>();
		public LinkedHashMap<String, String> attrMap = new LinkedHashMap<String, String>();

		public XMLElement(String name) {
			this.name = name;
		}
	}

	private static class ViewVarsCallback implements TraversalCallback {
		private XMLElement rootElement;
		private ArrayList<XMLElement> elementStack = new ArrayList<XMLElement>();

		@Override
		public void startMap(String name) {
			XMLElement newE = new XMLElement(name);
			if( 0 < elementStack.size() ) {
				elementStack.get(elementStack.size()-1).childList.add(newE);
			} else {
				rootElement = newE;
			}
			elementStack.add(newE);
		}

		@Override
		public void endMap() {
			elementStack.remove(elementStack.size()-1);
		}

		@Override
		public void meetAttribute(String name, Object value) {
			String attrVal = null;
			if( null != value ) {
				attrVal = value.toString();
			}
			elementStack.get(elementStack.size()-1).attrMap.put(name, attrVal);
		}

		@Override
		public void meetChildValue(String name, Object value) {
			if( null != value ) {
				startMap(name);
				elementStack.get(elementStack.size()-1).value = value.toString();
				endMap();
			}
		}

		@Override
		public void startList(String name, int count) {
			startMap(name);
			meetAttribute("count", count);
		}

		@Override
		public void endList() {
			endMap();
		}
	}


	private static String escapeString(String src) {
		if (null == src) {
			return null;
		}

		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < src.length(); i++) {
			char ch = src.charAt(i);
			switch (ch) {
			case '&':
				buf.append("&amp;");
				break;
			case '<':
				buf.append("&lt;");
				break;
			case '>':
				buf.append("&gt;");
				break;
			case '\'':
				buf.append("&#39;");
				break;
			case '"':
				buf.append("&#34;");
				break;
			default:
				buf.append(ch);
			}
		}
		return buf.toString();
	}
	private static void printlnWithIndent(Writer wr, int indent, String str) throws IOException {
		for (int i = 0; i < indent; i++) {
			wr.write('\t');
		}
		wr.write(str);
		wr.write('\n');
		wr.flush();
	}

	private static void exportElement(Writer wr, int indent, XMLElement element) throws IOException {
		StringBuffer line = new StringBuffer();
		line.append("<");
		line.append(element.name);
		for (Map.Entry<String,String> att : element.attrMap.entrySet()) {
			line.append(" ");
			line.append(att.getKey());
			line.append("=");
			line.append("\"");
			line.append((null == att.getValue()) ? "" : escapeString(att.getValue()));
			line.append("\"");
		}

		boolean isEnd = false;
		if (0 < element.childList.size()) {
			line.append(">");
			printlnWithIndent(wr, indent, line.toString());
			line.setLength(0);
			for (XMLElement childElement : element.childList) {
				exportElement(wr, indent + 1, childElement);
			}
		} else {
			if (null != element.value && 0 != element.value.length()) {
				line.append(">");
				//line.append("<![CDATA[");
				line.append(element.value);
				//line.append("]]>");
			} else {
				isEnd = true;
				line.append("/>");
			}
		}
		if (!isEnd) {
			line.append("<");
			line.append("/");
			line.append(element.name);
			line.append(">");
		}
		printlnWithIndent(wr, indent, line.toString());
	}

	public static void exportXml(OutputStream out, XMLElement rootE, String outputEncoding) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(out, outputEncoding);
		StringBuffer line = new StringBuffer();
		line.append("<?xml version=\"1.0\" encoding=\"").append(outputEncoding).append("\"?>");
		printlnWithIndent(writer, 0, line.toString());

		exportElement(writer, 0, rootE);
	}

	public static XMLElement convertViewVarMapToXMLElement(String rootName, ViewVarMap viewVars) {
		ViewVarsCallback callback = new ViewVarsCallback();
		viewVars.traversal(callback, rootName, true);
		return callback.rootElement;
	}
}
