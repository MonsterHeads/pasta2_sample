package huck.common.simpledom;

public class SimpleDocument {
	private SimpleElement documentElement;
	private String version;
	private String encoding;
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public SimpleElement getDocumentElement() {
		return documentElement;
	}

	public void setDocumentElement(SimpleElement documentElement) {
		this.documentElement = documentElement;
	}
}
