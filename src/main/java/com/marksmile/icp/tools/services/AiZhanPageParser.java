package com.marksmile.icp.tools.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.StrKit;
import com.marksmile.icp.tools.db.model.BeianDomainInfo;
import com.marksmile.icp.tools.entry.AiZhanPageInfo;
import com.marksmile.icp.tools.entry.IcpInfo;
import com.marksmile.utils.DateUtil;
import com.marksmile.utils.JSONUtil;

public class AiZhanPageParser {
	private static Logger logger = LoggerFactory.getLogger(AiZhanPageParser.class);

	public static List<BeianDomainInfo> parseJson(String domainName, String jsonStr) {
		List<BeianDomainInfo> list = new ArrayList<BeianDomainInfo>();
		JSONObject json = JSONObject.parseObject(jsonStr);
		BeianDomainInfo beianDomainInfo = new BeianDomainInfo();
		String icp = json.getString("icp");//
		if ("未备案".equals(icp)) {
			beianDomainInfo.setBeianType("02");//
			beianDomainInfo.setDomainName(domainName);
		} else {
			beianDomainInfo.setDomainName(json.getString("domains"));
			beianDomainInfo.setBeianCompany(json.getString("company"));
			beianDomainInfo.setBeianNo(json.getString("icp"));
			beianDomainInfo.setBeianCompanyType(json.getString("type"));
			beianDomainInfo.setDomainName(json.getString("domains"));
			long beiAnTime = json.getLong("icp_time");
			beianDomainInfo.setBeianTime(DateUtil.format(new Date(beiAnTime * 1000), DateUtil.YYYY_MM_DD));
			beianDomainInfo.setBeianSiteName(json.getString("name"));
			beianDomainInfo.setBeianSiteUrl(json.getString("homes"));
			beianDomainInfo.setBeianType("01");// 01-已备案;02-未备案
		}
		beianDomainInfo.setStateType("03");// 01-未采集;02-采集中;03-采集完毕;04-采集失败
		beianDomainInfo.setCreateTime(new Date());
		beianDomainInfo.setSourceType("05");// 01-爱站历史;02-爱站List;03-爱站Single;04-爱站实时List;05-爱站实时
		beianDomainInfo.setSpriderDate(new Date());
		list.add(beianDomainInfo);
		JSONArray company_other = json.getJSONArray("company_other");
		if (company_other != null) {
			for (Object object : company_other) {
				JSONObject json2 = (JSONObject) object;
				String domains = json2.getString("domains");
				String[] arr = domains.split("\\|");
				for (String domain : arr) {
					BeianDomainInfo beianDomainInfo2 = new BeianDomainInfo();
					beianDomainInfo2.setDomainName(domain);
					beianDomainInfo2.setBeianCompany(json2.getString("company"));
					beianDomainInfo2.setBeianNo(json2.getString("icp"));
					beianDomainInfo2.setBeianCompanyType(json2.getString("type"));
					long beiAnTime = json2.getLong("icp_time");
					beianDomainInfo2.setBeianTime(DateUtil.format(new Date(beiAnTime * 1000), DateUtil.YYYY_MM_DD));
					beianDomainInfo2.setBeianSiteName(json2.getString("name"));
					beianDomainInfo2.setBeianSiteUrl(json2.getString("homes"));
					beianDomainInfo2.setBeianType("01");// 01-已备案;02-未备案
					beianDomainInfo2.setStateType("03");// 01-未采集;02-采集中;03-采集完毕;04-采集失败
					beianDomainInfo2.setCreateTime(new Date());
					beianDomainInfo2.setSourceType("04");// //
															// 01-爱站历史;02-爱站List;03-爱站Single;04-爱站实时List;05-爱站实时
					long updatetime = json2.getLong("updatetime");
					beianDomainInfo2.setSpriderDate(new Date(updatetime * 1000));
					list.add(beianDomainInfo2);
				}
			}
		}
		return list;

	}

	public static List<BeianDomainInfo> parseDoc(String domainName, String html) {
		List<BeianDomainInfo> list = new ArrayList<BeianDomainInfo>();
		Document doc = Jsoup.parse(html);

		AiZhanPageInfo aiZhanPageInfo = new AiZhanPageInfo();
		aiZhanPageInfo.parse(doc);
		List<String> listHistory = aiZhanPageInfo.getListHistory();
		for (String domain : listHistory) {
			BeianDomainInfo beianDomainInfo = new BeianDomainInfo();
			beianDomainInfo.setDomainName(domain);
			beianDomainInfo.setStateType("01");// 01-未采集;02-采集中;03-采集完毕;04-采集失败
			beianDomainInfo.setCreateTime(new Date());
			beianDomainInfo.setSourceType("01");// 01-爱站历史;02-爱站List;03-爱站Single;04-爱站实时List;05-爱站实时
			list.add(beianDomainInfo);
		}
		BeianDomainInfo beianDomainInfo = new BeianDomainInfo();
		String cacheTime = aiZhanPageInfo.getInfo().getCacheTime();
		if (!StrKit.isBlank(cacheTime)) {
			beianDomainInfo.setSpriderDate(DateUtil.parse(cacheTime, DateUtil.YYYY_MM_DD_HH_MM_SS));
		} else {
			beianDomainInfo.setSpriderDate(new Date());
		}
		beianDomainInfo.setBeianNo(aiZhanPageInfo.getInfo().getBeiAnNo());
		beianDomainInfo.setBeianCompany(aiZhanPageInfo.getInfo().getCommanyName());
		beianDomainInfo.setBeianCompanyType(aiZhanPageInfo.getInfo().getCommanyType());
		beianDomainInfo.setBeianSiteName(aiZhanPageInfo.getInfo().getSiteName());
		beianDomainInfo.setBeianSiteUrl(aiZhanPageInfo.getInfo().getSiteUrl());

		beianDomainInfo.setDomainName(domainName);
		if (!domainName.equals(beianDomainInfo.getDomainName()) && aiZhanPageInfo.getInfo().isBeiAn()) {
			logger.error("域名不一致:[domainName:{}][pageDomainName:{}]", domainName, beianDomainInfo.getDomainName());
		}
		beianDomainInfo.setBeianTime(aiZhanPageInfo.getInfo().getShenHeTime());
		beianDomainInfo.setStateType("03");// 01-未采集;02-采集中;03-采集完毕;04-采集失败
		beianDomainInfo.setCreateTime(new Date());
		beianDomainInfo.setSourceType("03");// 01-爱站历史;02-爱站List;03-爱站Single;04-爱站实时List;05-爱站实时
		beianDomainInfo.setBeianType(aiZhanPageInfo.getInfo().isBeiAn() ? "01" : "02");// 01-已备案;02-未备案
		list.add(beianDomainInfo);

		List<IcpInfo> listCommpayDomain = aiZhanPageInfo.getListCommpayDomains();
		for (IcpInfo icpInfo : listCommpayDomain) {
			
			
			BeianDomainInfo beianDomainInfoList = new BeianDomainInfo();
			beianDomainInfoList.setBeianNo(icpInfo.getBeiAnNo());
			beianDomainInfoList.setBeianCompany(beianDomainInfo.getBeianCompany());
			beianDomainInfoList.setBeianCompanyType(beianDomainInfo.getBeianCompanyType());
			beianDomainInfoList.setDomainName(icpInfo.getDomain());
			beianDomainInfoList.setBeianTime(icpInfo.getShenHeTime());
			beianDomainInfoList.setBeianSiteName(icpInfo.getSiteName());
			beianDomainInfoList.setBeianSiteUrl(icpInfo.getSiteUrl());
			beianDomainInfoList.setStateType("03");// 01-未采集;02-采集中;03-采集完毕;04-采集失败
			beianDomainInfoList.setCreateTime(new Date());
			beianDomainInfoList.setSourceType("02");// 01-爱站历史;02-爱站List;03-爱站Single;04-爱站实时List;05-爱站实时
			beianDomainInfoList.setBeianType("01");// 01-已备案;02-未备案
			beianDomainInfoList.setSpriderDate(beianDomainInfo.getSpriderDate());
			list.add(beianDomainInfoList);
		}
		return list;
	}

	public static void saveToDb(List<BeianDomainInfo> list) {
		for (BeianDomainInfo beianDomainInfo : list) {
			try {
				if (StrKit.isBlank(beianDomainInfo.getDomainName())) {
					logger.warn("domain == null");
					continue;
				}
				

				BeianDomainInfo d = beianDomainInfo.findFirst(
						"select * from cha_icp_beian_domain_info where domain_name= ?",
						beianDomainInfo.getDomainName());
				if (d != null) {
					int nSourceTypeOld = Integer.parseInt(d.getSourceType());
					int nSourceType = Integer.parseInt(beianDomainInfo.getSourceType());
					if (nSourceType > nSourceTypeOld) {
						logger.info("delete: domainName={} sourceType:{}", d.getDomainName(), d.getSourceType());
						d.delete();
					} else {
						continue;
					}
				}
				logger.debug("save: domainName={} sourceType:{}", beianDomainInfo.getDomainName(),
						beianDomainInfo.getSourceType());
				beianDomainInfo.save();
			} catch (Exception e) {
				logger.error(e.getMessage()+"|"+JSONUtil.toJsonString(beianDomainInfo), e);
			}
		}
	}
}
