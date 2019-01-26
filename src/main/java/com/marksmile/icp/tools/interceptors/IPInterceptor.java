package com.marksmile.icp.tools.interceptors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.plugin.ehcache.CacheKit;

public class IPInterceptor implements Interceptor {
	private static Logger logger = LoggerFactory.getLogger(IPInterceptor.class);

	@Override
	public void intercept(Invocation inv) {
		String ip = getRemoteIP(inv.getController().getRequest());
		logger.debug(ip);
		Integer counter = CacheKit.get("ip", ip);
		if (counter == null) {
			counter = 1;
			CacheKit.put("ip", ip, counter);
			inv.invoke();
			return;
		}

		counter++;
		CacheKit.put("ip", ip, counter);
		logger.info("ip:{}  counter:{}",ip,counter);
		if (counter < 5) {
			inv.invoke();
		} else {
			inv.getController().renderJson("msg", "访问太频繁");
			logger.info("访问太频繁:ip-{}",ip);
		}
	}

	public static String getRemoteIP(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;

	}
}
