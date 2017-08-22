package huck.common.simpledom;

import java.util.ArrayList;
import java.util.HashMap;

public class SimpleElement {
	private boolean casesensitive;
	private String name;
	private String value;
	private ArrayList<SimpleAttribute> attributeList = new ArrayList<SimpleAttribute>();
	private HashMap<String,ArrayList<String>> attMap = new HashMap<String,ArrayList<String>>();
	private ArrayList<SimpleElement> elementList = new ArrayList<SimpleElement>();
	private HashMap<String,ArrayList<SimpleElement>> elementMap = new HashMap<String,ArrayList<SimpleElement>>();
	
	public SimpleElement(String name) {
		this(name,null,true);
	}
	
	public SimpleElement(String name, String value) {
		this(name,value,true);
	}
	
	public SimpleElement(String name, boolean casesensitive) {
		this(name,null,casesensitive);
	}
	
	public SimpleElement(String name, String value, boolean casesensitive) {
		this.name = name;
		this.casesensitive = casesensitive;
		this.value = value;
	}
	
	public ArrayList<SimpleAttribute> getAttributeList() {
		return attributeList;
	}
	public ArrayList<String> getAttributeList(String name) {
		if( casesensitive )
			return attMap.get(name);
		else
			return attMap.get(name.toLowerCase());
	}
	public String getAttribute(String name) {
		ArrayList<String> attList = getAttributeList(name);
		if( null != attList && 0 < attList.size() ) {
			return attList.get(0);
		} else {
			return null;
		}
	}
	public void addAttribute(SimpleAttribute attribute) {
		ArrayList<String> attList = getAttributeList(attribute.getName());
		if( null == attList ) {
			attList = new ArrayList<String>();
			if( casesensitive )
				attMap.put(attribute.getName(), attList);
			else
				attMap.put(attribute.getName().toLowerCase() , attList);
		}
		attList.add(attribute.getValue());
		attributeList.add(attribute);
	}
	
	public ArrayList<SimpleElement> getChildElementList() {
		return elementList;
	}
	public ArrayList<SimpleElement> getChildElementList(String name) {
		if( casesensitive )
			return elementMap.get(name);
		else
			return elementMap.get(name.toLowerCase());
	}
	public SimpleElement getChildElement(String name) {
		ArrayList<SimpleElement> elList = getChildElementList(name);
		if( null != elList && 0 < elList.size() ) {
			return elList.get(0);
		} else {
			return null;
		}
	}
	public void addChildElement(SimpleElement element) {
		ArrayList<SimpleElement> elList = getChildElementList(element.getName());
		if( null == elList ) {
			elList = new ArrayList<SimpleElement>();
			if( casesensitive )
				elementMap.put(element.getName(), elList);
			else
				elementMap.put(element.getName().toLowerCase(), elList);
		}
		elList.add(element);
		elementList.add(element);
	}
	
	public String getName() {
		return name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	private void toStringInternal(StringBuffer buf, int depth) {
		for( int i=0; i<depth; i++ ) {
			buf.append("\t");
		}
		buf.append("<").append(this.name);
		for( SimpleAttribute attr: attributeList ) {
			buf.append(" ").append(attr.getName()).append("=\"").append(attr.getValue()).append("\"");
		}
		
		if( 0 < elementList.size() ) {
			buf.append(">").append(System.lineSeparator());
			for( SimpleElement child : elementList ) {
				child.toStringInternal(buf, depth+1);
			}
			for( int i=0; i<depth; i++ ) {
				buf.append("\t");
			}
			buf.append("</").append(this.name).append(">");
		} else if( null != value ) {
			buf.append(">").append(value).append("</").append(this.name).append(">");
		} else {
			buf.append("/>");
		}

		buf.append(System.lineSeparator());
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		toStringInternal(buf, 0);
		return buf.toString();
	}
}
