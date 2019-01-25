package com.marksmile.icp.tools;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.core.JFinal;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.marksmile.icp.tools.db.model._MappingKit;
import com.marksmile.icp.tools.services.AiZhanICPService;

/**
 * Hello world!
 *
 */
public class WebFrameService {

	private static Logger logger = LoggerFactory.getLogger(WebFrameService.class);
	private int port;
	private DataSource dataSource = null;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void start() {
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dataSource);
		_MappingKit.mapping(arp);
		arp.start();
		AiZhanICPService.one().start();
		JFinal.main(new String[] { "webapp", String.valueOf(808), "/", "-1" });
	}

	public void stop() {

	}
}
