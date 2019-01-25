package com.marksmile.icp.tools.entry;

import com.marksmile.utils.HttpClientKit;

public class AiZhanSession {
	private String token;
	private String cookie;
	private byte[] authCodeImage;
	private String authCode;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getCookie() {
		return cookie;
	}
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	public byte[] getAuthCodeImage() {
		return authCodeImage;
	}
	public void setAuthCodeImage(byte[] authCodeImage) {
		this.authCodeImage = authCodeImage;
	}
	public String getAuthCode() {
		return authCode;
	}
	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}
	
	
}
