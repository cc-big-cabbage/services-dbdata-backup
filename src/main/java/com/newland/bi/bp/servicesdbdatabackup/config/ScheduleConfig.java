package com.newland.bi.bp.servicesdbdatabackup.config;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
/**
 * @param : * @param null
 * @author : cc
 * @date : 2019/12/8
 * @time : 21:43
 * @desc :
 * @return :
 */
@Configuration @EnableAsync public class ScheduleConfig implements SchedulingConfigurer, AsyncConfigurer {
	/**
	 * 并行任务
	 * @param scheduledTaskRegistrar
	 */
	@Override public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
		TaskScheduler taskScheduler = taskScheduler();
		scheduledTaskRegistrar.setTaskScheduler(taskScheduler);
	}

	/**
	 * 多线程配置
	 * @return
	 */
	@Bean(destroyMethod = "shutdown") public ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(500);
		// 设置线程名前缀
		scheduler.setThreadNamePrefix("task-");
		// 线程内容执行完后60秒停在
		scheduler.setAwaitTerminationSeconds(60);
		// 等待所有线程执行完
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		return scheduler;
	}

	/**
	 * 异步任务
	 * @return
	 */
	@Override public Executor getAsyncExecutor() {
		Executor executor = taskScheduler();
		return executor;
	}

	/**
	 * 异常处理
	 * @return
	 */
	@Override public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}
}