package com.newland.bi.bp.servicesdbdatabackup.services;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.io.File;
/**
 * @author ：cc
 * @date ：Created in 2019/12/8 19:57
 * @description：邮件工具类
 * @modified By：
 * @version: $
 */
@Component @Slf4j public class MailServices {
	@Autowired private JavaMailSender mailSender;

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 20:04
	 * @desc : 普通有奖
	 */
	public void sendSimpleMail(String former, String[] sender, String title, String content) throws Exception {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(former);
		message.setTo(sender);
		message.setSubject(title);
		message.setText(content);
		mailSender.send(message);
	}

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 20:04
	 * @desc : 发送Html邮件
	 */
	public void sendHtmlMail(String former, String[] sender, String title, String content) {
		MimeMessage message = null;
		try {
			message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(former);
			helper.setTo(sender);
			helper.setSubject(title);
			helper.setText(content, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mailSender.send(message);
	}

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 20:04
	 * @desc : 发送带附件的邮件
	 */
	public void sendAttachmentsMail(String former, String[] sender, String title, String content, File attacheFile) {
		MimeMessage message = null;
		try {
			message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(former);
			helper.setTo(sender);
			helper.setSubject(title);
			helper.setText(content);
			FileSystemResource file = new FileSystemResource(attacheFile);
			helper.addAttachment(attacheFile.getName(), file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mailSender.send(message);
	}

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 20:04
	 * @desc : 发送带静态资源的邮件
	 */
	public void sendInlineMail(String former, String[] sender, String title, String content, File attacheFile) {
		MimeMessage message = null;
		try {
			message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(former);
			helper.setTo(sender);
			helper.setSubject(title);
			//第二个参数指定发送的是HTML格式,同时cid:是固定的写法
			helper.setText(content, true);
			FileSystemResource file = new FileSystemResource(attacheFile);
			helper.addInline(attacheFile.getName(), file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mailSender.send(message);
	}
}
