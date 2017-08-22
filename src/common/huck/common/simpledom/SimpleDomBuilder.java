package huck.common.simpledom;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SimpleDomBuilder {
	public static SimpleDocument createSimpleDocument(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		return createSimpleDocument(in,true);
	}
	public static SimpleDocument createSimpleDocument(InputStream in, boolean casesensitive) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setValidating(false);
        factory.setNamespaceAware(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(in);

        Element root = doc.getDocumentElement();
        
        SimpleElement documentElement = w3cElemenetToSimpleElement(root, casesensitive);
        SimpleDocument simpleDoc = new SimpleDocument();
        simpleDoc.setVersion(doc.getXmlVersion());
        simpleDoc.setEncoding(doc.getXmlEncoding());
        simpleDoc.setDocumentElement(documentElement);
        return simpleDoc;
	}
	
	private static SimpleElement w3cElemenetToSimpleElement(Element element, boolean casesensitive) {
		Node tmpNode=null;
		SimpleElement result = new SimpleElement(element.getNodeName(), casesensitive);
		result.setValue(element.getTextContent());
		
		NamedNodeMap attList = element.getAttributes();
		for( int i=0; null!=(tmpNode=attList.item(i)); i++) {
			String nodeName = tmpNode.getNodeName();
			String nodeValue = tmpNode.getNodeValue();
			result.addAttribute(new SimpleAttribute(nodeName, nodeValue));			
		}
		
		NodeList nodeList = element.getChildNodes();
        for( int i=0; null != (tmpNode=nodeList.item(i)); i++ ) {
        	if( tmpNode instanceof Element ) {
        		result.addChildElement(w3cElemenetToSimpleElement((Element)tmpNode, casesensitive));
        	}
        }
		
		return result;
	}
	public static SimpleElement createExceptionElement(Throwable ex) {
		SimpleElement exception = new SimpleElement("exception");
		SimpleElement errorName = new SimpleElement("errorName");
		errorName.setValue(ex.getClass().getName());
		SimpleElement errorMsg = new SimpleElement("errorMsg");
		errorMsg.setValue(ex.getMessage());
		SimpleElement stackTrace = new SimpleElement("stackTrace");
	    for (StackTraceElement trace : ex.getStackTrace() ) {
	    	SimpleElement stack = new SimpleElement("stack");
	    	stack.addAttribute(new SimpleAttribute("location",trace.getClassName() + "." + trace.getMethodName()));
	    	stack.addAttribute(new SimpleAttribute("file",trace.getFileName()));
	    	stack.addAttribute(new SimpleAttribute("line",Integer.toString(trace.getLineNumber())));
	    	stackTrace.addChildElement(stack);
	    }
		
		exception.addChildElement(errorName);
		exception.addChildElement(errorMsg);
		exception.addChildElement(stackTrace);
		
		if( null != ex.getCause() ) {
			exception.addChildElement(createExceptionElement(ex.getCause()));
		}
		return exception;
	}
}
