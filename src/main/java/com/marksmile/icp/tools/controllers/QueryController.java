package com.marksmile.icp.tools.controllers;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.marksmile.icp.tools.db.model.BeianDomainInfo;
import com.marksmile.icp.tools.entry.AiZhanSession;
import com.marksmile.icp.tools.entry.IcpInfo;
import com.marksmile.icp.tools.interceptors.IPInterceptor;
import com.marksmile.icp.tools.services.AiZhanICPService;
import com.marksmile.icp.tools.views.AuthCodeRender;
import com.marksmile.utils.DateUtil;
import com.marksmile.utils.HttpClientKit;

public class QueryController extends Controller {
	private static Logger logger = LoggerFactory.getLogger(QueryController.class);

	public void queryWitchCache() throws Exception {
		// String domain = getPara("domain");
		String domain = getPara("domain");
		if (domain == null) {
			domain = getAttrForStr("domain");
		}
		if (domain == null) {
			setAttr("info", new IcpInfo());
			setAttr("isBeiAn", true);
			renderTemplate("/index.html");
			return;
		}
		setAttr("domain", domain);
		IcpInfo info = querySolr(domain);
		if (info == null) {
			HttpClientKit clientKit = new HttpClientKit();
			info = AiZhanICPService.one().getIpcInfoCache(clientKit, domain);
			AiZhanSession zhanSession = new AiZhanSession();
			zhanSession.setToken(info.getToken());
			zhanSession.setCookie(clientKit.getCookies());
			getSession().setAttribute("aiZhanSession", zhanSession);
			logger.debug("01-queryWitchCache-domain:{}", domain);
		} else {
			logger.debug("02-queryWitchCache-domain:{}", domain);
		}
		if(info.isBeiAn()){
			setAttr("isBeiAn", true);
		}else{
			setAttr("isBeiAn", false);
		}
		setAttr("info", info);
		renderTemplate("/index.html");
	}

	public void monitor() throws Exception {
		renderText(AiZhanICPService.one().getMonitorInfo());
	}

	private IcpInfo querySolr(String domain) {
		BeianDomainInfo domainInfo = BeianDomainInfo.dao.findFirst(
				"select * from cha_icp_beian_domain_info where domain_name= ? and source_type<>'01'", domain);
		if (domainInfo != null) {
			IcpInfo info = new IcpInfo();
			info.setBeiAnNo(domainInfo.getBeianNo());
			info.setCommanyName(domainInfo.getBeianCompany());
			info.setCommanyType(domainInfo.getBeianCompanyType());
			info.setDomain(domain);
			info.setShenHeTime(domainInfo.getBeianTime());
			info.setSiteName(domainInfo.getBeianSiteName());
			info.setSiteUrl(domainInfo.getBeianSiteUrl());
			info.setBeiAn("01".equals(domainInfo.getBeianType()));
			Date date = domainInfo.getSpriderDate();
			if (date != null) {
				info.setCacheTime(DateUtil.format(date, DateUtil.YYYY_MM_DD_HH_MM_SS));
			}
			return info;
		}
		return null;
	}

	@Before(IPInterceptor.class)
	public void queryNoCache() {
		String domain = getPara("domain");
		if (AiZhanICPService.one().hasResource()) {
			try {
				String json = AiZhanICPService.one().getHtml(domain);
				logger.info("queryNoCache-json:{}", json);
				renderJson(json);
			} catch (Exception e) {
				if ("no_resource".equals(e.getMessage())) {
					renderJson("state", "no_resource");
				} else {
					logger.error(e.getMessage(), e);
				}
			}
		} else {
			renderJson("state", "no_resource");
		}

	}

	public void genAuthCode() throws Exception {
		AiZhanSession zhanSession = (AiZhanSession) getSession().getAttribute("aiZhanSession");
		if(zhanSession==null){//这个资源可以先准备后，后期再优化
			HttpClientKit clientKit = new HttpClientKit();
			BeianDomainInfo bInfo = AiZhanICPService.getAUnSriderDomain();
			String domainName = "58.com";
			if (bInfo != null) {
				domainName = bInfo.getDomainName();
			}
			IcpInfo info = AiZhanICPService.one().getIpcInfoCache(clientKit, domainName);
			 zhanSession = new AiZhanSession();
			zhanSession.setToken(info.getToken());
			zhanSession.setCookie(clientKit.getCookies());
			getSession().setAttribute("aiZhanSession", zhanSession);
		}
		HttpClientKit clientKit = new HttpClientKit();
		byte[] bytes = AiZhanICPService.one().getAuthCodeImage(clientKit, zhanSession.getCookie());
		if (zhanSession.getCookie() != null) {
			zhanSession.setCookie(zhanSession.getCookie() + ";" + clientKit.getCookies());
		}
		clientKit.close();
		render(new AuthCodeRender(bytes));
	}

	public void checkAuthCode() throws Exception {
		String code = getPara("code");
		AiZhanSession zhanSession = (AiZhanSession) getSession().getAttribute("aiZhanSession");
		HttpClientKit clientKit = new HttpClientKit();
		boolean ret = AiZhanICPService.one().checkAuthCode(clientKit, code, zhanSession.getToken(),
				zhanSession.getCookie());
		logger.debug("checkAuthCode-ret:{}", ret);
		if (ret) {
			renderJson("error", "true");
		} else {
			renderJson("error", "false");
		}
	}

	public void queryWhithAuthCode() throws Exception {
		String domain = getPara("domain");
		String code = getPara("code");
		logger.info("queryWhithAuthCode-domain:{}", domain);
		AiZhanSession zhanSession = (AiZhanSession) getSession().getAttribute("aiZhanSession");
		String json = AiZhanICPService.one().getHtml(zhanSession.getCookie(), domain, code, zhanSession.getToken());
		renderJson(json);
	}

}
