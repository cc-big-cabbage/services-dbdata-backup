package com.newland.bi.bp.servicesdbdatabackup.config;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
/**
 * RestTemplate配置
 * 这是一种JavaConfig的容器配置，用于spring容器的bean收集与注册，并通过参数传递的方式实现依赖注入。
 * "@Configuration"注解标注的配置类，都是spring容器配置类，springboot通过"@EnableAutoConfiguration"
 * 注解将所有标注了"@Configuration"注解的配置类，全部注入spring容器中。
 *
 * @author cc
 */
@Configuration public class RestTemplateConfig {
	@Autowired private Environment env;
	Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);
	@Bean public RestTemplate getRestTemplateSinger() {
		SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
		int readTimeout = NumberUtils.toInt(env.getProperty("newland.config.restTemplate.readTimeout"), 30000);
		int connectTimeout = NumberUtils.toInt(env.getProperty("newland.config.restTemplate.connectTimeout"), 5000);
		logger.debug("readTimeout=" + readTimeout);
		logger.debug("connectTimeout=" + connectTimeout);
		simpleClientHttpRequestFactory.setConnectTimeout(connectTimeout);
		simpleClientHttpRequestFactory.setReadTimeout(readTimeout);
		return new RestTemplate(simpleClientHttpRequestFactory);
	}
}
