package com.newland.bi.bp.servicesdbdatabackup.services;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
/**
 * @author ：cc
 * @date ：Created in 2019/12/8 9:26
 * @description： 微信信息通知服务
 * @modified By：
 * @version: $
 */
@Component @Slf4j public class WeixinServices {
	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 9:27
	 * @desc :
	 */
	public void sendNotice(String msg) {
		log.info(msg);
	}
}
