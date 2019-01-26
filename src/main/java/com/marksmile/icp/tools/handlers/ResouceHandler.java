package com.marksmile.icp.tools.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.handler.Handler;

public class ResouceHandler extends Handler {
	private static Logger logger = LoggerFactory.getLogger(ResouceHandler.class);

	@Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		logger.debug("url:{}",target);
		if ("/favicon.ico".equals(target)) {
			next.handle("/images/favicon.ico", request, response, isHandled);
			return ;
		}if ("/".equals(target)) {
			next.handle("/", request, response, isHandled);
			return ;
		}
		if (target.endsWith("/")) {
			target = target.substring(0, target.length() - 1);
			try {
				response.sendRedirect(target);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ;
		}

		int index = target.indexOf("/", 1);
		if (index == -1) {
			request.setAttribute("domain", target.substring(1));
			target = "/q/queryWitchCache";
		}
		next.handle(target, request, response, isHandled);
	}

}
