package com.marksmile.icp.tools.entry;

public class SaveInfo {
	private String  content;
	private String domainName;
	private String type;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public SaveInfo(String content, String domainName, String type) {
		super();
		this.content = content;
		this.domainName = domainName;
		this.type = type;
	}


}
