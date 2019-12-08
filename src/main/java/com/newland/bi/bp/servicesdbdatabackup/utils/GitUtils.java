package com.newland.bi.bp.servicesdbdatabackup.utils;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
/**
 * @param : * @param null
 * @author : cc
 * @date : 2019/12/8
 * @time : 16:10
 * @desc : Git操作工具类
 * @return :
 */
@Slf4j public class GitUtils {
	public static void main(String[] args) throws Exception {
		GitHub github = new GitHubBuilder().withOAuthToken("33c1fdccf4ec319cb56a4431247e941972845195 ").build();
	}
}
