package com.marksmile.icp.tools.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.core.Controller;
import com.marksmile.icp.tools.entry.IcpInfo;

public class IndexController extends Controller {
	private static Logger logger = LoggerFactory.getLogger(IndexController.class);

	public void index() throws Exception {
		setAttr("info", new IcpInfo());
		setAttr("isBeiAn", "true");
		renderTemplate("/index.html");
		return;
	}

}
