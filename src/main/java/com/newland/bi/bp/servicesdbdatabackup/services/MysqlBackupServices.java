package com.newland.bi.bp.servicesdbdatabackup.services;
import com.alibaba.fastjson.JSONObject;
import com.newland.bi.bp.servicesdbdatabackup.bean.DBConfBean;
import com.newland.bi.bp.servicesdbdatabackup.utils.DBUtils;
import com.newland.bi.bp.servicesdbdatabackup.utils.GlobalConst;
import com.newland.bi.bp.servicesdbdatabackup.utils.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/**
 * @author ：cc
 * @date ：Created in 2019/12/8 9:49
 * @description：
 * @modified By：
 * @version: $
 */
@Component @Slf4j public class MysqlBackupServices {
	//固定线程池
	ExecutorService executorService = Executors.newFixedThreadPool(10);
	@Autowired private MailServices mailServices;

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 9:56
	 * @desc :
	 */
	public int backupMysql(File backupFilePath, DBConfBean dbConfBean) {
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
			log.debug("sql = " + sql);
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
			DBUtils.close(rs);
			DBUtils.close(pstmt);
			//开始生成备份文件
			File mysqlBackupFilePath = new File(backupFilePath.getAbsolutePath() + File.separator + "mysql");
			FileUtils.forceMkdir(backupFilePath);
			String tableSqlFile = saveBackupCreateTableSqlFile(mysqlBackupFilePath, dbConfBean, tableList);
			List<String> dataSqlFileList = saveBackupTableDataSqlFile(conn, mysqlBackupFilePath, dbConfBean, tableList);
			//生成压缩包
			File zipFile = new File(
					backupFilePath.getAbsoluteFile() + File.separator + "[mysql][" + dbConfBean.getIp() + "]" + DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMddHHmm") + ".zip");
			FileOutputStream fos1 = new FileOutputStream(zipFile);
			ZipUtils.toZip(mysqlBackupFilePath.getAbsolutePath(), fos1, true);
			//上传到git---网络问题，修改为发送邮件方式备份
			if ("1".equals(GlobalConst.sendMailFlag)) {
				mailServices.sendAttachmentsMail(GlobalConst.mailFrom, GlobalConst.mailTos, "新大陆业务产品部重要备份" + zipFile.getName(), zipFile.getName(), zipFile);
			}
			//发送邮件后，则删除本机备份，保留zip
			FileUtils.deleteDirectory(mysqlBackupFilePath);
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
	public void getCreateTableSql(Connection conn, List<JSONObject> tableList) throws Exception {
		for (JSONObject item : tableList) {
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
				log.debug(createTable.toString());
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
	}

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 10:40
	 * @desc : 生成建表脚本文件
	 */
	public String saveBackupCreateTableSqlFile(File backupFilePath, DBConfBean dbConfBean, List<JSONObject> tableList) throws Exception {
		try {
			//添加readme文件
			File readme = new File(backupFilePath.getAbsoluteFile() + File.separator + "readme.txt");
			FileUtils.write(readme, "backup times  " + DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"), "utf-8");
			File tableSqlFile = new File(backupFilePath.getAbsoluteFile() + File.separator + "[mysql][create sql][" + dbConfBean.getIp() + "]create_table.sql");
			List<String> dataList = new ArrayList<>(1000);
			tableList.forEach(item -> {
				dataList.add(item.getString("create_table_sql"));
			});
			FileUtils.writeLines(tableSqlFile, dataList);
			return tableSqlFile.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * @param : * @param null
	 * @return :
	 * @author : cc
	 * @date : 2019/12/8
	 * @time : 10:40
	 * @desc : 生成数据表sql脚本数据文件
	 */
	public List<String> saveBackupTableDataSqlFile(Connection conn, File backupFilePath, DBConfBean dbConfBean, List<JSONObject> tableList) throws Exception {
		List<String> fileList = new ArrayList<>();
		CountDownLatch countDownLatch = new CountDownLatch(tableList.size());
		for (JSONObject item : tableList) {
			executorService.execute(new Runnable() {
				@Override public void run() {
					try {
						List<String> sqlList = new ArrayList<>(10000);
						String tablename = getTableName(item);
						PreparedStatement pstmt = conn.prepareStatement("select * from " + tablename + " limit " + dbConfBean.getMaxTotal());
						ResultSet rs = pstmt.executeQuery();
						ResultSetMetaData rsmd = rs.getMetaData();
						int cols = rsmd.getColumnCount();
						String colvalue = "";
						while (rs.next()) {
							StringBuilder insertSql = new StringBuilder();
							insertSql.append(" insert into " + tablename + " ( ");
							for (int i = 1; i <= cols; i++) {
								if (i > 1) {
									insertSql.append(",");
								}
								insertSql.append(rsmd.getColumnName(i));
							}
							insertSql.append(" ) values (");
							for (int i = 1; i <= cols; i++) {
								if (i > 1) {
									insertSql.append(",");
								}
								//判断字段类型
								if (rsmd.getColumnClassName(i).equals(String.class.getName())) {
									colvalue = rs.getString(rsmd.getColumnName(i));
									if (colvalue != null) {
										colvalue = colvalue.replace("'", "\\'");
										colvalue = colvalue.replace("\r", " ");
										colvalue = colvalue.replace("\n", " ");
										insertSql.append("'" + colvalue + "'");
									} else {
										insertSql.append(colvalue);
									}
								} else {
									insertSql.append(rs.getString(rsmd.getColumnName(i)));
								}
							}
							insertSql.append(");");
							sqlList.add(insertSql.toString());
						}
						if (sqlList.size() > 0) {
							File dataFile = new File(backupFilePath.getAbsoluteFile() + File.separator + "[mysql][data sql][" + dbConfBean.getIp() + "]" + tablename + ".sql");
							FileUtils.writeLines(dataFile, sqlList);
							fileList.add(dataFile.getAbsolutePath());
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						countDownLatch.countDown();
					}
				}
			});
		}
		//等待2个小时
		countDownLatch.await(2, TimeUnit.HOURS);
		return fileList;
	}

	public String getTableName(JSONObject item) {
		return item.getString("table_schema") + "." + item.getString("table_name");
	}
}
