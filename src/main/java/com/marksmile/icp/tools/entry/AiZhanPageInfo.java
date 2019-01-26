package com.marksmile.icp.tools.entry;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.kit.StrKit;
import com.marksmile.utils.htmlpath.JSoupPathUtil;

public class AiZhanPageInfo {
	private static Logger logger = LoggerFactory.getLogger(AiZhanPageInfo.class);
	private IcpInfo info = new IcpInfo();
	private List<String> listHistory = new ArrayList<String>();
	private List<IcpInfo> listCommpayDomains = new ArrayList<>();

	public IcpInfo getInfo() {
		return info;
	}

	public List<String> getListHistory() {
		return listHistory;
	}

	public List<IcpInfo> getListCommpayDomains() {
		return listCommpayDomains;
	}

	public void parse(Document doc) {
		String token = JSoupPathUtil.getSelect(doc, "meta[name=csrf-token]/[content]").get(0);
		info.setToken(token);
		Elements es = doc.select("div.last-search").select("ul").select("li").select("a");
		for (Element element : es) {
			String domain = element.text();
			listHistory.add(domain);
		}

		List<String> list = JSoupPathUtil.getSelect(doc, "div.search-cache/(缓存于,<)");
		if (list != null && list.size() > 0) {
			info.setCacheTime(list.get(0));
		}

		Elements tables = doc.select("div#icp-table").select("table.table");
		if (tables.size() == 1) {
			info.setBeiAn(true);
			Elements trs = tables.select("tr");
			for (Element tr : trs) {
				String key = tr.select("td").get(0).text();
				String value = tr.select("td").get(1).text();
				if ("网站备案/许可证号".equals(key)) {
					info.setBeiAnNo(value);
				} else if ("主办单位名称".equals(key)) {
					info.setCommanyName(value);
				} else if ("主办单位性质".equals(key)) {
					info.setCommanyType(value);
				} else if ("网站名称".equals(key)) {
					info.setSiteName(value);
				} else if ("网站首页网址".equals(key)) {
					info.setSiteUrl(value);
				} else if ("网站域名".equals(key)) {
					info.setDomain(value);
				} else if ("审核时间".equals(key)) {
					info.setShenHeTime(value);
				} else {
					logger.warn("不认识的字段:{}={}", key, value);
				}
			}
			trs = doc.select("div#icp-company").select("table.table").select("tbody").select("tr");
			if (tables != null) {
				for (Element element : trs) {
					String siteUrl = element.select("td").get(2).text();
					String arr[] = siteUrl.split("\\ ");
					siteUrl = StrKit.join(arr, "|");
					for (String _url : arr) {
						IcpInfo lInfo = new IcpInfo();
						lInfo.setBeiAn(true);
						lInfo.setBeiAnNo(element.select("td").get(0).text());
						String domainName = _url;
						if (domainName.startsWith("www.")) {
							domainName = domainName.substring("www.".length());
						}
						lInfo.setDomain(domainName);
						lInfo.setShenHeTime(element.select("td").get(3).text());
						lInfo.setSiteName(element.select("td").get(1).text());
						lInfo.setSiteUrl(siteUrl);

						lInfo.setCommanyName(info.getCommanyName());
						lInfo.setCommanyType(info.getCommanyType());
						listCommpayDomains.add(lInfo);
					}
					
					
				}
			}

		} else {
			Elements div = doc.select("div.update-icp").select("span.red");
			boolean checked = false;
			if (div != null) {
				String msg = div.get(0).text();
				if (msg.indexOf("未找到") > -1 || msg.indexOf("未备案") > -1) {
					checked = true;
				}
			}
			if (!checked) {
				logger.warn("页面不正常:-未备案");
				logger.debug(doc.outerHtml());
			}
			info.setBeiAn(false);
		}
	}

	public void parse(String html) {
		Document doc = Jsoup.parse(html);
		parse(doc);

	}
}
