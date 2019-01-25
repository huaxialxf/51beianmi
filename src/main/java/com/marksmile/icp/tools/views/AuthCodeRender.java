package com.marksmile.icp.tools.views;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import com.jfinal.kit.LogKit;
import com.jfinal.render.Render;
import com.jfinal.render.RenderException;

public class AuthCodeRender extends Render {
	
	private byte[] imageData ; 
	
	
	public AuthCodeRender(byte[] imageData) {
		super();
		this.imageData = imageData;
	}


	@Override
	public void render() {

		response.setHeader("Pragma","no-cache");
		response.setHeader("Cache-Control","no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");
		
		ServletOutputStream sos = null;
		try {
			sos = response.getOutputStream();
			sos.write(imageData);
		} catch (IOException e) {
			if (getDevMode()) {
				throw new RenderException(e);
			}
		} catch (Exception e) {
			throw new RenderException(e);
		} finally {
			if (sos != null) {
				try {sos.close();} catch (IOException e) {LogKit.logNothing(e);}
			}
		}
	
	}

}
