package com.marksmile.icp.tools.entry;

import java.util.Date;

public class IcpInfoMiitBeian {
	private String commanyName; // 主办单位名称
	private String commanyType; // 主办单位类型
	private String beiAnNo; // 网站备案/许可证号
	private String siteName; // 网站名称
	private String siteUrl; // 网站首页网址
	private String domain; // 网站域名
	private String shenHeTime; // 审核时间
	private boolean isBeian;//是否备案
	 private Date cacheTime; //缓存时间-采集时间
	
	
	public boolean isBeian() {
		return isBeian;
	}
	public void setBeian(boolean isBeian) {
		this.isBeian = isBeian;
	}
	public Date getCacheTime() {
		return cacheTime;
	}
	public void setCacheTime(Date cacheTime) {
		this.cacheTime = cacheTime;
	}
	public String getCommanyName() {
		return commanyName;
	}
	public void setCommanyName(String commanyName) {
		this.commanyName = commanyName;
	}
	public String getCommanyType() {
		return commanyType;
	}
	public void setCommanyType(String commanyType) {
		this.commanyType = commanyType;
	}
	public String getBeiAnNo() {
		return beiAnNo;
	}
	public void setBeiAnNo(String beiAnNo) {
		this.beiAnNo = beiAnNo;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public String getSiteUrl() {
		return siteUrl;
	}
	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getShenHeTime() {
		return shenHeTime;
	}
	public void setShenHeTime(String shenHeTime) {
		this.shenHeTime = shenHeTime;
	}
	
	
	

}
