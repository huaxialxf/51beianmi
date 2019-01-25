package com.marksmile.icp.tools.services;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marksmile.icp.tools.authcode.AuthCodeUtil;
import com.marksmile.icp.tools.db.model.BeianDomainInfo;
import com.marksmile.icp.tools.entry.AiZhanPageInfo;
import com.marksmile.icp.tools.entry.IcpInfo;
import com.marksmile.icp.tools.entry.SaveInfo;
import com.marksmile.icp.tools.entry.SpriderResource;
import com.marksmile.utils.HttpClientKit;
import com.marksmile.utils.htmlpath.JSoupPathUtil;

public class AiZhanICPService {
	private static Logger logger = LoggerFactory.getLogger(AiZhanICPService.class);

	static String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.6788.400 QQBrowser/10.3.2864.400";
	static String baseUrl = "https://icp.aizhan.com";
	// static String baseUrl = "http://www.lxf.com";
	private LinkedBlockingQueue<SpriderResource> rquestQueue = new LinkedBlockingQueue<SpriderResource>();
	private LinkedBlockingQueue<SaveInfo> taskQueue = new LinkedBlockingQueue<SaveInfo>();
	private static AiZhanICPService one = new AiZhanICPService();

	public static AiZhanICPService one() {
		if (one == null) {
			one = new AiZhanICPService();
		}
		return one;
	}

	public boolean hasResource() {
		if (rquestQueue.size() > 0) {
			return true;
		}
		return false;
	}

	public String getCsrToken(HttpClientKit clientKit, String domain) throws Exception {
		Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("Connection", "keep-alive");
		headers.put("User-Agent", userAgent);
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		// headers.put("Referer", "http://www.lxf.com/");
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Accept-Language", "zh-CN,zh;q=0.9");

		String url = baseUrl + "/" + domain + "/";
		String html = clientKit.exeGetMethodForString(headers, url);
		Document doc = Jsoup.parse(html);
		putHtmlQueue(html, domain);
		String token = JSoupPathUtil.getSelect(doc, "meta[name=csrf-token]/[content]").get(0);
		return token;
	}

	public void putHtmlQueue(String html, String domainName) {
		try {
			taskQueue.put(new SaveInfo(html, domainName, "01"));
		} catch (InterruptedException e) {
		}
	}

	public void putJsonQueue(String json, String domainName) {
		try {
			taskQueue.put(new SaveInfo(json, domainName, "02"));
		} catch (InterruptedException e) {
		}
	}

	public IcpInfo getIpcInfoCache(HttpClientKit clientKit, String domain) throws Exception {
		Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("Connection", "keep-alive");
		headers.put("User-Agent", userAgent);
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Accept-Language", "zh-CN,zh;q=0.9");

		String url = baseUrl + "/" + domain + "/";
		String html = clientKit.exeGetMethodForString(headers, url);

		putHtmlQueue(html, domain);
		AiZhanPageInfo aiZhanPageInfo = new AiZhanPageInfo();
		aiZhanPageInfo.parse(html);
		return aiZhanPageInfo.getInfo();
	}

	public String getAuthCode(byte[] image) throws Exception {
		String code = null;
		code = AuthCodeUtil.getCode(ImageIO.read(new ByteArrayInputStream(image)));
		if (code != null) {
			logger.debug("code == {}", code);
		} else {
			logger.debug("code == null");
		}
		return code;
	}

	public boolean checkAuthCode(HttpClientKit clientKit, String code, String token) throws Exception {
		Map<String, String> headers = new LinkedHashMap<String, String>();
		String url = baseUrl + "/api/validate-captcha";

		Map<String, String> param = new LinkedHashMap<String, String>();
		// code = SystemIn.getInput("请输入验证码["+code+"]:" );
		param.put("code", code);
		param.put("switch", "1");
		param.put("_csrf", token);
		String ret = clientKit.exePostMethodForString(url, headers, param);
		if (ret != null && ret.toLowerCase().indexOf("true") > -1) {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkAuthCode(HttpClientKit clientKit, String code, String token, String cookie) throws Exception {
		Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("Connection", "keep-alive");
		headers.put("User-Agent", userAgent);
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headers.put("X-Requested-With", "XMLHttpRequest");
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Accept-Language", "zh-CN,zh;q=0.9");

		if (cookie != null) {
			headers.put("Cookie", cookie);
		}
		String url = baseUrl + "/api/validate-captcha";

		Map<String, String> param = new LinkedHashMap<String, String>();
		// code = SystemIn.getInput("请输入验证码["+code+"]:" );
		param.put("code", code);
		param.put("switch", "1");
		param.put("_csrf", token);

		logger.info("checkAuthCode===============code:{} param:{} cookie:{}", code, param, cookie);
		String ret = clientKit.exePostMethodForString(url, headers, param);
		logger.info("checkAuthCode===============" + ret);
		if (ret != null && ret.toLowerCase().indexOf("true") > -1) {
			return true;
		} else {
			return false;
		}
	}

	public String getHtml(String domain) throws Exception {
		SpriderResource resource = rquestQueue.poll();
		if (resource == null) {
			throw new Exception("no_resource");
		}
		return getHtml(resource.getCookies(), domain, resource.getAuthCode(), resource.getToken());
	}

	public String getHtml(String cookie, String domain, String code, String token) throws Exception {
		Map<String, String> param = new LinkedHashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headers.put("Connection", "keep-alive");
		headers.put("Pragma", "no-cache");
		headers.put("Cache-Control", "no-cache");
		headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
		// headers.put("Origin", "http://www.lxf.com");
		headers.put("X-Requested-With", "XMLHttpRequest");
		headers.put("User-Agent", userAgent);
		// headers.put("Referer", "http://www.lxf.com/");
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Accept-Language", "zh-CN,zh;q=0.9");
		headers.put("Cookie", cookie);
		param.put("domain", domain);
		param.put("switch", "1");
		param.put("code", code.toLowerCase());
		param.put("_csrf", token);
		HttpClientKit clientKit = new HttpClientKit(/* "127.0.0.1;8888",null */);

		logger.info("checkAuthCode===============code:{} param:{} cookie:{}", code, param, cookie);
		String ret = clientKit.exePostMethodForString(baseUrl + "/api/refresh-icp", headers, param);
		putJsonQueue(ret, domain);
		return ret;
	}

	public byte[] getAuthCodeImage(HttpClientKit clientKit) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		if (clientKit == null) {
			clientKit = new HttpClientKit();
		}
		String srcUrl = baseUrl + "/api/captcha?switch=1&t=" + System.currentTimeMillis();
		byte[] bytes = clientKit.downHttpFile(srcUrl, headers);
		return bytes;
	}

	public byte[] getAuthCodeImage(HttpClientKit clientKit, String cookies) throws Exception {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Cookie", cookies);
		String srcUrl = baseUrl + "/api/captcha?switch=1&t=" + System.currentTimeMillis();
		byte[] bytes = clientKit.downHttpFile(srcUrl, headers);
		return bytes;
	}

	private BeianDomainInfo getAUnSriderDomain() {
		List<BeianDomainInfo> list = BeianDomainInfo.dao
				.find("select * from cha_icp_beian_domain_info where state_type = '01' limit 10");
		if (list.size() > 0) {
			int n = new Random().nextInt(list.size());
			return list.get(n);
		}
		return null;
	}

	public void runTask() throws Exception {
		HttpClientKit clientKit = new HttpClientKit(/* "127.0.0.1;8888",null */);
		BeianDomainInfo info = getAUnSriderDomain();
		String domainName = "58.com";
		if (info != null) {
			domainName = info.getDomainName();
		}
		logger.info("runTask -domainName:{}", domainName);
		String token = getCsrToken(clientKit, domainName);
		byte[] image = null;
		String code = null;
		while (true) {
			try {
				image = getAuthCodeImage(clientKit);
			} catch (Exception e) {
				logger.info("getAuthCodeImage error");
				throw e;
			}
			try {
				code = getAuthCode(image);
				if (code == null) {
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("getAuthCode error");
				continue;
			}

			boolean isCheck = false;

			try {
				isCheck = checkAuthCode(clientKit, code, token);
				logger.debug("checkAuthCode : " + isCheck);
				if (isCheck) {
					break;
				}
			} catch (Exception e) {
				logger.info("checkAuthCode 失败");
				throw e;
			}
		}
		SpriderResource resource = new SpriderResource();
		resource.setAuthCode(code);

		resource.setCookies(clientKit.getCookies());
		resource.setToken(token);
		resource.setDate(System.currentTimeMillis());
		rquestQueue.put(resource);
		logger.info("rquestQueue_size:{}", rquestQueue.size());

	}

	private int threadNum = 1;

	public void start() {
		Thread t = new Thread() {
			public void run() {
				while (true) {
					SpriderResource spriderResource = rquestQueue.peek();
					if (spriderResource != null) {
						if (System.currentTimeMillis() - spriderResource.getDate() > 1000 * 10 * 60) {
							SpriderResource resource = rquestQueue.poll();
							BeianDomainInfo info = getAUnSriderDomain();
							if (info != null) {
								String domain = info.getDomainName();
								try {
									getHtml(resource.getCookies(), domain, resource.getAuthCode(), resource.getToken());
								} catch (Exception e) {
									logger.error("dispose html " + e.getMessage());
								}

							}
							logger.info("dispose sprideResource rquestQueue_size:{}", rquestQueue.size());
							continue;
						}
					}
					try {
						Thread.sleep(1000 * 5);
					} catch (InterruptedException e) {
					}
				}
			};
		};
		t.setName("worker_dispose");
		t.start();

		Thread t_save = new Thread() {
			public void run() {
				while (true) {
					SaveInfo saveInfo = taskQueue.poll();
					if (saveInfo == null) {
						try {
							Thread.sleep(1000 * 5);
						} catch (InterruptedException e) {
						}
						continue;
					}
					if ("01".equals(saveInfo.getType())) {
						List<BeianDomainInfo> list = AiZhanPageParser.parseDoc(saveInfo.getDomainName(),
								saveInfo.getContent());
						AiZhanPageParser.saveToDb(list);
					} else if ("02".equals(saveInfo.getType())) {
						List<BeianDomainInfo> list = AiZhanPageParser.parseJson(saveInfo.getDomainName(),
								saveInfo.getContent());
						AiZhanPageParser.saveToDb(list);
					} else {
						logger.error("error type:{}", saveInfo.getType());
					}

				}
			};
		};
		t_save.setName("t_save");
		t_save.start();

		for (int i = 0; i < threadNum; i++) {
			Thread t2 = new Thread() {
				public void run() {
					while (true) {
						try {
							runTask();
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
				};
			};
			t2.setName("worker_" + i);
			t2.start();
		}
	}

	public String getIcpInfo(String domain) throws Exception {
		SpriderResource resource = rquestQueue.poll();
		if (resource == null) {
			throw new RuntimeException("NO_RESOURCE");
		}
		return getHtml(resource.getCookies(), domain, resource.getAuthCode(), resource.getToken());
	}

	public String getMonitorInfo() throws Exception {
		int size = rquestQueue.size();
		int size2 = taskQueue.size();
		return "rquestQueue:" + size + "   ----  \n" + "taskQueue:" + size2;
	}
}
