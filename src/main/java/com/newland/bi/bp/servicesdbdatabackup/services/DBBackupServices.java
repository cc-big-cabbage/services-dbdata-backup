package com.newland.bi.bp.servicesdbdatabackup.services;
import com.newland.bi.bp.servicesdbdatabackup.bean.DBConfBean;
import com.newland.bi.bp.servicesdbdatabackup.utils.GlobalConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
/**
 * @author ：cc
 * @date ：Created in 2019/12/8 9:49
 * @description：
 * @modified By：
 * @version: $
 */
@Component @Slf4j public class DBBackupServices {
	@Autowired MysqlBackupServices mysqlBackupServices;

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 9:18
	 * @desc : 每天12：30和20：30启动备份任务
	 * 业务处理逻辑
	 * 1、检查需要备份的数据库
	 * 2、通过关键字扫描需要备份的表（去重）
	 * 3、动态创建数据库链接
	 * 5、访问元数据，生成建表脚本
	 * 6、生成sql导入脚本
	 * 7、生成csv数据文件
	 * 8、备份到svn
	 * 9、备份到github
	 * 10、发送微信消息通知
	 */
	public int backup(DBConfBean dbConfBean) {
		try {
			//判断目录是否存在，不存在则创建
			File baseFile = new File(GlobalConst.backupFilePath);
			if (!baseFile.exists()) {
				FileUtils.forceMkdir(baseFile);
			}
			File backupFilePath = new File(
					baseFile.getAbsoluteFile() + File.separator + DateFormatUtils.format(System.currentTimeMillis(), "yyyyMM") + File.separator + DateFormatUtils.format(System.currentTimeMillis(),
																																										 "yyyyMMdd") + File.separator
							+ DateFormatUtils.format(System.currentTimeMillis(), "HH"));
			//判断数据库类型
			String dbType = dbConfBean.getDbType();
			if ("mysql".equalsIgnoreCase(dbType)) {
				return mysqlBackupServices.backupMysql(backupFilePath, dbConfBean);
			} else if ("oracle".equalsIgnoreCase(dbType)) {
			} else {
				return 1001;
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
}
