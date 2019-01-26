package com.marksmile.icp.tools.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import org.eclipse.jetty.util.UrlEncoded;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.marksmile.icp.tools.authcode.AuthCodeUtil;
import com.marksmile.icp.tools.db.model.BeianDomainInfo;
import com.marksmile.icp.tools.entry.IcpInfoMiitBeian;
import com.marksmile.icp.tools.htmlunit.MyWebConnection;
import com.marksmile.utils.DateUtil;
import com.marksmile.utils.FileUtil;
import com.marksmile.utils.HttpClientKit;
import com.marksmile.utils.JSONUtil;

public class MiitbeianService {
	private static Logger logger = LoggerFactory.getLogger(MiitbeianService.class);

	private static MiitbeianService one = new MiitbeianService();

	public static MiitbeianService one() {
		if (one == null) {
			one = new MiitbeianService();
		}
		return one;
	}

	private MiitbeianService() {
	}

	public String getCookie() throws Exception {
		WebClient client = new WebClient(BrowserVersion.CHROME);
		client.getOptions().setTimeout(30000);
		client.getOptions().setJavaScriptEnabled(true);
		client.getOptions().setCssEnabled(false);
		client.setAjaxController(new NicelyResynchronizingAjaxController());
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.waitForBackgroundJavaScript(10000);
		client.setWebConnection(new MyWebConnection(client));
		try {
			client.getPage("http://www.miitbeian.gov.cn/icp/publish/query/icpMemoInfo_showPage.action");
		} catch (Throwable e) {
		}
		try {
			client.getPage("http://www.miitbeian.gov.cn/icp/publish/query/icpMemoInfo_showPage.action");
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new Exception("cookeieError");
		}

		Set<Cookie> cookies = client.getCookieManager().getCookies();
		StringBuilder builder = new StringBuilder();
		for (Cookie cookie : cookies) {
			builder.append(cookie.getName() + "=" + cookie.getValue() + "; ");
		}

		logger.debug("cookie:" + builder.toString());
		client.close();
		return builder.toString();
	}

	public void getIndexPage(String cookie) throws Exception {
		String userAgent = BrowserVersion.CHROME.getUserAgent();
		HttpClientKit clientKit = new HttpClientKit();
		String url = "http://www.miitbeian.gov.cn/icp/publish/query/icpMemoInfo_showPage.action";
		Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("User-Agent", userAgent);
		headers.put("Cookie", cookie);
		try {
			clientKit.exeGetMethodForString(headers, url);
		} finally {
			clientKit.close();
		}
	}

	public byte[] getAuthCodeImage(String cookie) throws Exception {
		String userAgent = BrowserVersion.CHROME.getUserAgent();

		Random random = new Random();
		String srcUrl = "http://www.miitbeian.gov.cn/getVerifyCode?" + random.nextInt(100);

		Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("User-Agent", userAgent);
		headers.put("Cookie", cookie);
		HttpClientKit clientKit = getHttpClientKit();
		byte[] bytes = clientKit.downHttpFile(srcUrl, headers);
		return bytes;
	}

	public String getAuthCode(byte[] image) throws Exception {
		String code = null;
		code = AuthCodeUtil.getCode(ImageIO.read(new ByteArrayInputStream(image)));
		logger.info("code == {}", code);
		return code;
	}

	private HttpClientKit getHttpClientKit() {
		return new HttpClientKit();
	}

	public boolean checkAuthCode(String cookie, String code) throws Exception {
		String userAgent = BrowserVersion.CHROME.getUserAgent();
		Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("User-Agent", userAgent);
		headers.put("Cookie", cookie);
		HttpClientKit clientKit = getHttpClientKit();
		String url = "http://www.miitbeian.gov.cn/common/validate/validCode.action";

		Map<String, String> param = new LinkedHashMap<String, String>();
		// code = SystemIn.getInput("请输入验证码["+code+"]:" );
		param.put("validateValue", code);
		String ret = clientKit.exePostMethodForString(url, headers, param);
		if (ret != null && ret.toLowerCase().indexOf("true") > -1) {
			return true;
		} else {
			return false;
		}
	}

	public String getHtml(String domain, String cookie, String code) throws Exception {
		sleep(1000);
		String userAgent = BrowserVersion.CHROME.getUserAgent();
		Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("User-Agent", userAgent);
		headers.put("Cookie", cookie);
		HttpClientKit clientKit = getHttpClientKit();
		headers = new LinkedHashMap<String, String>();
		headers.put("User-Agent", userAgent);
		headers.put("Cookie", cookie);

		Map<String, String> param = new LinkedHashMap<String, String>();
		param.put("siteName", "");
		param.put("condition", "1");
		param.put("siteDomain", domain);
		param.put("siteUrl", "");
		param.put("mainLicense", "");
		param.put("siteIp", "");
		param.put("unitName", "");
		param.put("mainUnitNature", "-1");
		param.put("certType", "-1");
		param.put("mainUnitCertNo", "");
		param.put("verifyCode", code.toLowerCase());
		String ret = clientKit.exePostMethodForString(
				"http://www.miitbeian.gov.cn/icp/publish/query/icpMemoInfo_searchExecute.action", headers, param);
		return ret;
	}

	public String getHtmlByBeiNo(String beiAnNo, String cookie, String code) throws Exception {
		sleep(1000);
		String userAgent = BrowserVersion.CHROME.getUserAgent();
		Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("User-Agent", userAgent);
		headers.put("Cookie", cookie);
		HttpClientKit clientKit = new HttpClientKit("127.0.0.1;8888", null);
		headers = new LinkedHashMap<String, String>();
		headers.put("User-Agent", userAgent);
		headers.put("Cookie", cookie);
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		Map<String, String> param = new LinkedHashMap<String, String>();
		param.put("siteName", "");
		param.put("condition", "3");
		param.put("siteDomain", "");
		param.put("siteUrl", "");
		param.put("mainLicense", beiAnNo);
		param.put("siteIp", "");
		param.put("unitName", "");
		param.put("mainUnitNature", "-1");
		param.put("certType", "-1");
		param.put("mainUnitCertNo", "");
		param.put("verifyCode", code.toLowerCase());
		String ret = clientKit.exePostMethodForString(
				"http://www.miitbeian.gov.cn/icp/publish/query/icpMemoInfo_searchExecute.action", headers, param,
				"GBK");
		return ret;
	}

	private static int errNum = 0;
	private static int successNum = 0;

	public static void parseHtml(String html) {
		Document doc = Jsoup.parse(html);
		Elements es = doc.select("table").select("tr");
		for (Element element : es) {
			if ("1".equals(element.attr("id"))) {
				successNum++;
				logger.info("seccess - 正确:" + (successNum) + "    错误:" + (errNum));
				return;
			}
		}
		errNum++;
		logger.info("err-正确:" + (successNum) + "    错误:" + (errNum));
	}

	public static IcpInfoMiitBeian parseHtml(String domain, String html) {
		Document doc = Jsoup.parse(html);
		Element tr = doc.select("table[width=100%]").select("tr").get(1);
		if (tr == null) {
			throw new RuntimeException("tr == null 内容不对");
		}
		IcpInfoMiitBeian info = new IcpInfoMiitBeian();
		info.setDomain(domain);
		info.setCacheTime(new Date());
		if (tr.select("td").size() == 1 && tr.text().indexOf("没有符合条件的记录") > -1) {
			info.setBeian(false);
		} else if ("1".equals(tr.attr("id"))) {
			Elements tds = tr.select("td");
			info.setBeian(true);
			info.setBeiAnNo(tds.get(3).text());
			info.setCommanyName(tds.get(1).text());
			info.setCommanyType(tds.get(2).text());
			info.setShenHeTime(dateStringFormat(tds.get(6).text()));
			info.setSiteName(tds.get(4).text());
			info.setSiteUrl(tds.get(5).text());
		} else {
			errNum++;
			throw new RuntimeException("无法解析的内容");
		}

		return info;
	}

	long time = System.currentTimeMillis();

	public synchronized void sleep(long ttime) {
		long t = System.currentTimeMillis() - time;
		Random random = new Random();
		t = 1000 + random.nextInt(1000) - t;
		try {
			Thread.sleep(Math.max(1, t));
		} catch (InterruptedException e) {
		}
		time = System.currentTimeMillis();

	}

	public static String dateStringFormat(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
		try {
			return DateUtil.format(sdf.parse(date), DateUtil.YYYY_MM_DD);
		} catch (ParseException e) {
			logger.info("error date:{}", date);
			return null;
		}
	}

	public void runTask(String domain, String cookie) throws Exception {
		if (cookie == null) {
			cookie = getCookie();
		}
		sleep(1000);
		// try {
		// getIndexPage(cookie);
		// } catch (Exception e) {
		// logger.info("获取页面失败");
		// throw e;
		// }
		String code = null;
		int n = 0;
		while (true) {
			byte[] image = null;
			try {
				image = getAuthCodeImage(cookie);
				sleep(1000);
			} catch (Exception e) {
				logger.info("getAuthCodeImage 失败");
				throw e;
			}
			try {
				code = getAuthCode(image);
				if (code == null) {
					continue;
				}
			} catch (Exception e) {
				logger.info("getAuthCode 失败");
				throw e;
			}

			boolean isCheck = false;

			try {
				isCheck = checkAuthCode(cookie, code);
				sleep(1000);
				logger.debug("checkAuthCode : " + isCheck);
				n++;
				if (isCheck) {
					break;
				}
			} catch (Exception e) {
				logger.info("checkAuthCode 失败");
				throw e;
			}
			if (n > 8) {
				throw new Exception("noimage");
			}
		}

		try {
			String html = getHtml(domain, cookie, code);
			// String html = getHtmlByBeiNo("京ICP备04000001号", cookie, code);
			List<BeianDomainInfo> list = AiZhanPageParser.parseMiit(domain, html);
			AiZhanPageParser.saveToDb(list);
			FileOutputStream fos = new FileOutputStream(new File("miitbeian.html"));
			fos.write(html.getBytes());
			fos.close();
		} catch (Exception e) {
			logger.info("checkAuthCode 失败");
			throw e;
		}

	}

	public static String getDomain() {
		BeianDomainInfo info = AiZhanICPService.getAUnSriderDomain();
		if (info != null) {
			String domain = info.getDomainName();
			return domain;
		}
		return null;
	}

	public void start(String name) {
		Thread t = new Thread() {
			public void run() {
				while (true) {
					try {
						long start = System.currentTimeMillis();
						String domain = getDomain();
						if (domain == null) {
							logger.info("没有域名...");
							Thread.sleep(1000 * 5);
							continue;
						}
						logger.info("domainName:{}", domain);
						runTask(getDomain(), null);
						Thread.sleep(Math.max(1, start + 1000 - System.currentTimeMillis()));
					} catch (Exception e) {
						if ("noimage".equals(e.getMessage())) {
							logger.info("noimage");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
						} else if ("cookeieError".equals(e.getMessage())) {
							logger.info("cookeieError-2");
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e1) {
							}
						} else if ("isSprider".equals(e.getMessage())) {
							logger.info("isSprider");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
						} else {
							logger.error(e.getMessage(), e);
						}
					}
				}
			};
		};
		t.setName("t_task_" + name);
		t.start();

	}

	public static void main3(String[] args) throws IOException {
		// String html = FileUtil.getFileInfo(new File("miitbeian_正常.html"));
		String html = FileUtil.getFileInfo(new File("miitbeian.html"));
		IcpInfoMiitBeian beian = MiitbeianService.parseHtml("baidu.com", html);
		System.out.println(JSONUtil.toJsonString(beian));
	}

	public static void main33(String[] args) throws UnsupportedEncodingException {
		String s = "%E4%BA%ACICP%E5%A4%8704000001%E5%8F%B7";// utf-8
		String ss = "京ICP备04000001号";
		// String s= "%BE%A9ICP%B1%B804000001%BA%C5";
		System.out.println(UrlEncoded.encodeString(new String(ss.getBytes("UTF-8"), "GBK")));
		System.out.println(UrlEncoded.encodeString(ss));
		System.out.println(UrlEncoded.encodeString(ss, Charset.forName("GBK")));
	}

	public static void main(String[] args) {
		new MiitbeianService().start("1");
	}

}
