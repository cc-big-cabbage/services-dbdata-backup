package com.newland.bi.bp.servicesdbdatabackup.config;
import com.newland.bi.bp.servicesdbdatabackup.bean.DBConfBean;
import com.newland.bi.bp.servicesdbdatabackup.services.DBBackupServices;
import com.newland.bi.bp.servicesdbdatabackup.services.WeixinServices;
import com.newland.bi.bp.servicesdbdatabackup.utils.GlobalConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.print.attribute.standard.NumberUp;
/**
 * 启动加载类
 */
@Slf4j @Component @Order(value = 1) public class StartupRunner implements CommandLineRunner {
	@Autowired ConfigurableApplicationContext context;
	@Autowired Environment environment;
	@Autowired WeixinServices weixinServices;
	@Autowired DBBackupServices dbBackupServices;

	@Override public void run(String... args) throws Exception {
		log.info(">>>>>>>>>>>>>>> 启动初始化任务 >>>>>>>>>>>>>>>");
		initConf();
		log.info("<<<<<<<<<<<<<<<< 完成初始化任务 <<<<<<<<<<<<<<<");
	}

	/**
	 * 初始化配置文件
	 */
	public void initConf() {
		try {
			//加载待备份数据库信息
			String dbnamestr = environment.getProperty("newland.dbconfig.dbname");
			String[] dbnames = dbnamestr.split(",");
			for (String dbname : dbnames) {
				String ip = environment.getProperty("newland.dbconfig." + dbname + ".ip");
				String port = environment.getProperty("newland.dbconfig." + dbname + ".port");
				String dbType = environment.getProperty("newland.dbconfig." + dbname + ".db-type");
				String username = environment.getProperty("newland.dbconfig." + dbname + ".username");
				String password = environment.getProperty("newland.dbconfig." + dbname + ".password");
				String keynames = environment.getProperty("newland.dbconfig." + dbname + ".key-names");
				int maxTotal = NumberUtils.toInt(environment.getProperty("newland.dbconfig." + dbname + ".max-total"), 1000);
				if (maxTotal > 100000) {
					maxTotal = 100000;
				}
				DBConfBean dbConfBean = new DBConfBean(ip, port, dbType, username, password, keynames, maxTotal);
				GlobalConst.dbconfList.add(dbConfBean);
			}
			log.info("GlobalConst.dbconfList=" + GlobalConst.dbconfList);
			//加载备份主目录
			GlobalConst.backupFilePath = environment.getProperty("newland.backup-file-path");
			log.info("GlobalConst.backupFilePath=" + GlobalConst.backupFilePath);
			GlobalConst.dbconfList.forEach(item -> {
				int result = dbBackupServices.backup(item);
				switch (result) {
				case 1001:
					weixinServices.sendNotice("不支持的数据库类型[" + item.getDbType() + "].");
					break;
				default:
					break;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
