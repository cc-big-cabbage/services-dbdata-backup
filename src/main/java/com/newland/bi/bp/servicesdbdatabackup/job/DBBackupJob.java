package com.newland.bi.bp.servicesdbdatabackup.job;
import com.alibaba.fastjson.JSON;
import com.newland.bi.bp.servicesdbdatabackup.services.DBBackupServices;
import com.newland.bi.bp.servicesdbdatabackup.services.WeixinServices;
import com.newland.bi.bp.servicesdbdatabackup.utils.GlobalConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/**
 * @author ：cc
 * @date ：Created in 2019/12/8 9:09
 * @description： 任务定时器
 * @modified By：
 * @version: $
 */
@Component @Slf4j @EnableScheduling public class DBBackupJob {
	@Autowired WeixinServices weixinServices;
	@Autowired DBBackupServices dbBackupServices;
	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 9:18
	 * @desc : 每天12：30和20：30启动备份任务
	 */
	@Scheduled(cron = "0 30 12,22 * * *") public void backup() {
		if (GlobalConst.dbconfList == null || GlobalConst.dbconfList.size() <= 0) {
			weixinServices.sendNotice("没有需要备份的数据库.");
			return;
		}
		GlobalConst.dbconfList.forEach(item -> {
			String keynames = item.getKeynames();
			if (StringUtils.isBlank(keynames)) {
				weixinServices.sendNotice("没有指定需要备份的数据表[" + JSON.toJSONString(item) + "].");
			} else {
				int result = dbBackupServices.backup(item);
				switch (result) {
				case 1001:
					weixinServices.sendNotice("不支持的数据库类型[" + JSON.toJSONString(item) + "].");
					break;
				case -1:
					weixinServices.sendNotice("系统错误[" + JSON.toJSONString(item) + "].");
					break;
				default:
					break;
				}
			}
		});
	}
}
