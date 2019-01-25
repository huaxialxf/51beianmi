package com.marksmile.icp.tools.entry;

public class SpriderResource {
	private String cookies;
	private String proxyAddress;
	private String authCode;
	private String token;
	private long date;

	
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}


	public String getCookies() {
		return cookies;
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}

	public String getProxyAddress() {
		return proxyAddress;
	}

	public void setProxyAddress(String proxyAddress) {
		this.proxyAddress = proxyAddress;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}


}
