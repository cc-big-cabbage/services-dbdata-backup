package com.newland.bi.bp.servicesdbdatabackup.utils;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 * @author ：cc
 * @date ：Created in 2019/12/8 9:42
 * @description：
 * @modified By：
 * @version: $
 */
@Slf4j public class DBUtils {
	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 9:43
	 * @desc :
	 */
	public static Connection getMysqlConn(String ip, String port, String username, String passwd) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			//mysql链接sys实例
			String jdburl = "jdbc:mysql://" + ip + ":" + port + "/mysql?useSSL=false&serverTimezone=UTC";
			Connection conn = DriverManager.getConnection(jdburl, username, passwd);
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public static void close(PreparedStatement pstmt) {
		try {
			if (pstmt != null) {
				pstmt.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public static void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
