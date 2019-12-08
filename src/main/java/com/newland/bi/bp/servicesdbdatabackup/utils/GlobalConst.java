package com.newland.bi.bp.servicesdbdatabackup.utils;
import com.newland.bi.bp.servicesdbdatabackup.bean.DBConfBean;

import java.util.ArrayList;
import java.util.List;
/**
 * @ClassName GlobalConst
 * @Description 一句话描述功能
 * @Author cc
 * @Date 2019/12/07 16:37
 */
public class GlobalConst {
	/**
	 * 备份数据库信息
	 */
	public static List<DBConfBean> dbconfList = new ArrayList<>(10);
	/**
	 * 备份文件主目录
	 */
	public static String backupFilePath = "";
	/**
	 * 发送邮件标示
	 */
	public static String sendMailFlag = "0";
	/**
	 * 接收邮件
	 */
	public static String mailFrom = "chradium@163.com";
	public static String[] mailTos = null;
}
