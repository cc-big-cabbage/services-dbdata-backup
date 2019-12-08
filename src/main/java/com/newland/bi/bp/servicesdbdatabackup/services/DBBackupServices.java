package com.newland.bi.bp.servicesdbdatabackup.services;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.newland.bi.bp.servicesdbdatabackup.bean.DBConfBean;
import com.newland.bi.bp.servicesdbdatabackup.utils.DBUtils;
import com.newland.bi.bp.servicesdbdatabackup.utils.GlobalConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
/**
 * @author ：cc
 * @date ：Created in 2019/12/8 9:49
 * @description：
 * @modified By：
 * @version: $
 */
@Component @Slf4j public class DBBackupServices {
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
		//判断数据库类型
		String dbType = dbConfBean.getDbType();
		if ("mysql".equalsIgnoreCase(dbType)) {
			return backupMysql(dbConfBean);
		} else if ("oracle".equalsIgnoreCase(dbType)) {
		} else {
			return 1001;
		}
		return 0;
	}

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 9:56
	 * @desc :
	 */
	public int backupMysql(DBConfBean dbConfBean) {
		Connection conn = null;
		try {
			//建立数据库链接
			conn = DBUtils.getMysqlConn(dbConfBean.getIp(), dbConfBean.getPort(), dbConfBean.getUsername(), dbConfBean.getPasswd());
			//获取关键字
			String[] keynames = dbConfBean.getKeynames().split(",");
			StringBuilder sql = new StringBuilder();
			sql.append(" select * from information_schema.tables  where ");
			for (int i = 0; i < keynames.length; i++) {
				if (i > 0) {
					sql.append(" or ");
				}
				sql.append(" upper(table_name) like upper('" + keynames[i] + "')  ");
			}
			//log.info("sql = " + sql);
			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			ResultSet rs = pstmt.executeQuery();
			List<JSONObject> tableList = new ArrayList<>(100);
			while (rs.next()) {
				JSONObject table = new JSONObject();
				table.put("table_schema", rs.getString("table_schema"));
				table.put("table_name", rs.getString("table_name"));
				table.put("table_rows", rs.getString("table_rows"));
				table.put("data_length", rs.getString("data_length"));
				tableList.add(table);
			}
			//生成建表脚本
			getCreateTableSql(conn, tableList);
			//log.info(JSON.toJSONString(tableList));
			DBUtils.close(rs);
			DBUtils.close(pstmt);
			String tableSqlFile = saveBackupCreateTableSqlFile(dbConfBean, tableList);
			log.info("tableSqlFile=" + tableSqlFile);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			DBUtils.close(conn);
		}
		return 0;
	}

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 10:22
	 * @desc :
	 */
	public void getCreateTableSql(Connection conn, List<JSONObject> tableList) {
		tableList.forEach(item -> {
			try {
				PreparedStatement pstmt = conn.prepareStatement("describe " + item.get("table_schema") + "." + item.get("table_name"));
				ResultSet rs = pstmt.executeQuery();
				StringBuilder createTable = new StringBuilder();
				createTable.append(" create table " + item.get("table_schema") + "." + item.get("table_name") + "( \n");
				int index = 0;
				while (rs.next()) {
					if (index++ > 0) {
						createTable.append(",");
					}
					createTable.append(rs.getString("field") + "\t" + rs.getString("type") + "\n");
				}
				createTable.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8 \n");
				item.put("create_table_sql", createTable);
				//log.info(createTable.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 10:40
	 * @desc : 生成建表脚本文件
	 */
	public String saveBackupCreateTableSqlFile(DBConfBean dbConfBean, List<JSONObject> tableList) {
		try {
			//判断目录是否存在，不存在则创建
			File baseFile = new File(GlobalConst.backupFilePath);
			if (!baseFile.exists()) {
				FileUtils.forceMkdir(baseFile);
			}
			//按月份创建子目录
			File subFile = new File(baseFile.getAbsoluteFile() + File.separator + DateFormatUtils.format(System.currentTimeMillis(), "yyyyMM"));
			FileUtils.forceMkdir(subFile);
			File tableSqlFile = new File(subFile.getAbsoluteFile() + File.separator + dbConfBean.getIp() + DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMddHHmm") + ".sql");
			List<String> dataList = new ArrayList<>(1000);
			tableList.forEach(item -> {
				dataList.add(item.getString("create_table_sql"));
			});
			FileUtils.writeLines(tableSqlFile, dataList);
			return tableSqlFile.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
