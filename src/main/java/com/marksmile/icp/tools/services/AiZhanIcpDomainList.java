package com.marksmile.icp.tools.services;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.json.Json;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.marksmile.icp.tools.db.model.BeianDomainInfo;
import com.marksmile.icp.tools.db.model._MappingKit;
import com.marksmile.icp.tools.entry.SaveInfo;
import com.marksmile.utils.DateUtil;
import com.marksmile.utils.FileUtil;
import com.marksmile.utils.HttpClientKit;
import com.marksmile.utils.JSONUtil;

public class AiZhanIcpDomainList {
	
	public static DataSource getDataSource() {
		PropKit.use("jdbc.properties");
		String url = PropKit.get("db.url");
		String username = PropKit.get("db.username");
		String password = PropKit.get("db.password");

		DruidPlugin druidPlugin = new DruidPlugin(url, username, password);
		druidPlugin.start();
		return druidPlugin.getDataSource();
	}

	public static void main(String[] args) throws Exception {
		
		ActiveRecordPlugin arp = new ActiveRecordPlugin(getDataSource());
		_MappingKit.mapping(arp);
		arp.start();
		
		
		
		// String domainName = "5858.com";
		// String fileName = "aizhan_01.html";
		String domainName = "buzzmaster.cn";
		String fileName = "aizhan_09.html";
		// String domainName = "5858_yyyyy.com";
		// String fileName = "aizhan_03.html";
		File file = new File(fileName);
		if (!file.exists()) {
			String url = String.format("https://icp.aizhan.com/%s/", domainName);
			HttpClientKit clientKit = new HttpClientKit();
			String html = clientKit.exeGetMethodForString(url);
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			fos.write(html.getBytes());
			fos.close();
		}

		String html = FileUtil.getFileInfo(new File(fileName));
		List<BeianDomainInfo> list = AiZhanPageParser.parseDoc(domainName, html);
		for (BeianDomainInfo beianDomainInfo : list) {
			System.out.println(JSONUtil.toJsonString(beianDomainInfo));
		}
		
//		AiZhanPageParser.saveToDb(list);
//		main2(args);
	}

	public static void main2(String[] args) throws Exception {
		String domainName = "5858.com";
		String fileName = "aizhan_05.json";
		String jsonStr = FileUtil.getFileInfo(new File(fileName));
		List<BeianDomainInfo> list = AiZhanPageParser.parseJson(domainName, jsonStr);
		for (BeianDomainInfo beianDomainInfo : list) {
			System.out.println(JSONUtil.toJsonString(beianDomainInfo));
		}

		AiZhanPageParser.saveToDb(list);

	}
}
