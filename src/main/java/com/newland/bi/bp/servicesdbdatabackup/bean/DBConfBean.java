package com.newland.bi.bp.servicesdbdatabackup.bean;
import lombok.Data;
import org.springframework.stereotype.Component;
/**
 * @ClassName DBConfBean
 * @Description 数据库配置信息
 * @Author cc
 * @Date 2019/12/07 16:32
 */
@Component @Data public class DBConfBean {
	private String ip = "";
	private String port = "";
	private String jdbcUrl = "";
	private String username = "";
	private String dbType = "";
	private String passwd = "";
	private String keynames = "";
	private int maxTotal = 100000;

	public DBConfBean() {
	}

	public DBConfBean(String ip, String port, String dbType, String username, String passwd, String keynames, int maxTotal) {
		this.ip = ip;
		this.port = port;
		this.dbType = dbType;
		this.username = username;
		this.passwd = passwd;
		this.keynames = keynames;
		this.maxTotal = maxTotal;
	}
}
