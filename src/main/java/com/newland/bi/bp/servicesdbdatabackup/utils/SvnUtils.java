package com.newland.bi.bp.servicesdbdatabackup.utils;
import java.io.*;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
/**
 * @param : * @param null
 * @author : cc
 * @date : 2019/12/8
 * @time : 21:44
 * @desc :
 * @return :
 */
public class SvnUtils {
	private SVNClientManager clientManager;
	private ISVNAuthenticationManager authManager;
	private SVNRepository repository;

	/**
	 * @param svnUrl      svn地址
	 * @param svnUsername svn用户名称
	 * @param svnPasswd   svn用户密码
	 * @throws SVNException 异常信息
	 */
	public SvnUtils(String svnUrl, String svnUsername, String svnPasswd) throws SVNException {
		try {
			this.createDefaultAuthenticationManager(svnUsername, svnPasswd);
			this.authSvn(svnUrl);
		} catch (SVNException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * 通过不同的协议初始化版本库
	 */
	private void setupLibrary() {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
		FSRepositoryFactory.setup();
	}

	/**
	 * @param username svn用户名称
	 * @param password svn用户密码
	 * @throws SVNException 异常信息
	 */
	private void createDefaultAuthenticationManager(String username, String password) throws SVNException {
		try {
			// 身份验证
			authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
		} catch (Exception e) {
			throw new RuntimeException("SVN身份认证失败：" + e.getMessage());
		}
	}

	/**
	 * 验证登录svn
	 * @param svnUrl 用户svn的仓库地址
	 * @throws SVNException 异常信息
	 */
	private void authSvn(String svnUrl) throws SVNException {
		// 初始化版本库
		setupLibrary();
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnUrl));
		} catch (SVNException e) {
			throw new RuntimeException("SVN创建库连接失败：" + e.getMessage());
		}
		// 创建身份验证管理器
		repository.setAuthenticationManager(authManager);
		DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
		try {
			//创建SVN实例管理器
			clientManager = SVNClientManager.newInstance(options, authManager);
		} catch (Exception e) {
			throw new RuntimeException("SVN实例管理器创建失败：" + e.getMessage());
		}
	}

	/**
	 * 添加文件和目录到版本控制下
	 * @param wcPath 工作区路径
	 * @throws SVNException 异常信息
	 */
	private void addEntry(File wcPath) throws SVNException {
		try {
			clientManager.getWCClient().doAdd(new File[]{wcPath}, true, false, false, SVNDepth.INFINITY, false, false, true);
		} catch (SVNException e) {
			throw new RuntimeException("SVN添加文件到版本控制下失败：" + e.getMessage());
		}
	}

	/**
	 * 将工作副本提交到svn
	 * @param wcPath        被提交的工作区路径
	 * @param keepLocks     是否在SVN仓库中打开或不打开文件
	 * @param commitMessage 提交信息
	 * @return 返回信息
	 * @throws SVNException 异常信息
	 */
	private SVNCommitInfo commit(File wcPath, boolean keepLocks, String commitMessage) throws SVNException {
		try {
			return clientManager.getCommitClient().doCommit(new File[]{wcPath}, keepLocks, commitMessage, null, null, false, false, SVNDepth.INFINITY);
		} catch (SVNException e) {
			throw new RuntimeException("SVN提交失败：" + e.getMessage());
		}
	}

	/**
	 * 确定path是否是一个工作空间
	 * @param path 文件路径
	 * @return 返回信息
	 * @throws SVNException 异常信息
	 */
	private boolean isWorkingCopy(File path) throws SVNException {
		if (!path.exists()) {
			return false;
		}
		try {
			if (null == SVNWCUtil.getWorkingCopyRoot(path, false)) {
				return false;
			}
		} catch (SVNException e) {
			throw new RuntimeException("确定path是否是一个工作空间 失败：" + e.getMessage());
		}
		return true;
	}

	/**
	 * 确定一个URL在SVN上是否存在
	 * @param url svn访问地址
	 * @return 返回信息
	 * @throws SVNException 异常信息
	 */
	private boolean isURLExist(SVNURL url) throws SVNException {
		try {
			SVNRepository svnRepository = SVNRepositoryFactory.create(url);
			svnRepository.setAuthenticationManager(authManager);
			SVNNodeKind nodeKind = svnRepository.checkPath("", -1);
			return nodeKind == SVNNodeKind.NONE ? false : true;
		} catch (SVNException e) {
			throw new RuntimeException("确定一个URL在SVN上是否存在失败：" + e.getMessage());
		}
	}

	/**
	 * 递归检查不在版本控制的文件，并add到svn
	 * @param wc 检查的文件
	 * @throws SVNException 异常信息
	 */
	private void checkVersiondDirectory(File wc) throws SVNException {
		if (!SVNWCUtil.isVersionedDirectory(wc)) {
			this.addEntry(wc);
		}
		if (wc.isDirectory()) {
			for (File sub : wc.listFiles()) {
				if (sub.isDirectory() && sub.getName().equals(".svn")) {
					continue;
				}
				checkVersiondDirectory(sub);
			}
		}
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * @param sPath 被删除目录的文件
	 * @return 目录删除成功返回true，否则返回false
	 */
	private boolean deleteDirectory(String sPath) {
		//如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		//如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		//删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			//删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			} //删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
		}
		if (!flag) {
			return false;
		}
		//删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除单个文件
	 * @param sPath 被删除文件的文件
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	private boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * @param sPath 要删除的目录或文件
	 * @return 删除成功返回 true，否则返回 false。
	 */
	private boolean DeleteFolder(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 判断目录或文件是否存在
		if (!file.exists()) {  // 不存在返回 false
			return flag;
		} else {
			// 判断是否为文件
			if (file.isFile()) {  // 为文件时调用删除文件方法
				return deleteFile(file.getAbsolutePath());
			} else {  // 为目录时调用删除目录方法
				return deleteDirectory(file.getAbsolutePath());
			}
		}
	}

	/**
	 * 更新SVN工作区
	 * @param wcPath           工作区路径
	 * @param updateToRevision 更新版本
	 * @param depth            update的深度：目录、子目录、文件
	 * @return 返回信息
	 * @throws SVNException 异常信息
	 */
	private long update(File wcPath, SVNRevision updateToRevision, SVNDepth depth) throws SVNException {
		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);
		try {
			return updateClient.doUpdate(wcPath, updateToRevision, depth, false, false);
		} catch (SVNException e) {
			throw new RuntimeException("更新SVN工作区失败：" + e.getMessage());
		}
	}

	/**
	 * SVN仓库文件检出
	 * @param url      文件url
	 * @param revision 检出版本
	 * @param destPath 目标路径
	 * @param depth    checkout的深度，目录、子目录、文件
	 * @return 返回信息
	 * @throws SVNException 异常信息
	 */
	private long checkout(SVNURL url, SVNRevision revision, File destPath, SVNDepth depth) throws SVNException {
		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);
		try {
			return updateClient.doCheckout(url, destPath, revision, revision, depth, false);
		} catch (SVNException e) {
			throw new RuntimeException("检出SVN仓库失败：" + e.getMessage());
		}
	}

	/**
	 * @param svnUrl    svn地址
	 * @param workspace 工作区
	 * @param filepath  上传的文件地址
	 * @param filename  文件名称
	 * @throws SVNException 异常信息
	 */
	private void checkWorkCopy(String svnUrl, String workspace, String filepath, String filename) throws SVNException {
		SVNURL repositoryURL = null;
		try {
			repositoryURL = SVNURL.parseURIEncoded(svnUrl);
		} catch (SVNException e) {
			throw new RuntimeException("解析svnUrl失败：" + e.getMessage());
		}
		String fPath = "";
		if (filepath.indexOf("/") != -1) {
			fPath = filepath.substring(0, filepath.lastIndexOf("/"));
		}
		File wc = new File(workspace + "/" + fPath);
		File wc_project = new File(workspace + "/" + fPath);
		SVNURL projectURL = null;
		try {
			projectURL = repositoryURL.appendPath(filename, false);
		} catch (SVNException e) {
			throw new RuntimeException("解析svnUrl文件失败：" + e.getMessage());
		}
		if (!this.isWorkingCopy(wc)) {
			if (!this.isURLExist(projectURL)) {
				this.checkout(repositoryURL, SVNRevision.HEAD, wc, SVNDepth.EMPTY);
			} else {
				this.checkout(projectURL, SVNRevision.HEAD, wc_project, SVNDepth.INFINITY);
			}
		} else {
			this.update(wc, SVNRevision.HEAD, SVNDepth.INFINITY);
		}
	}

	/**
	 * 循环删除.svn目录
	 * @param spath
	 */
	private void deletePointSVN(String spath) {
		File wc = new File(spath);
		for (File sub : wc.listFiles()) {
			if (sub.isDirectory() && sub.getName().equals(".svn")) {
				this.deleteDirectory(sub.getAbsolutePath());
				continue;
			}
			if (sub.isDirectory()) {
				deletePointSVN(sub.getAbsolutePath());
			}
		}
	}

	/**
	 * @param svnUrl      svn地址
	 * @param workspace   工作区
	 * @param filepath    上传的文件地址
	 * @param filename    上传的文件名称
	 * @param isOverwrite 是否覆盖
	 * @throws SVNException 异常信息
	 */
	public void upload(String svnUrl, String workspace, String filepath, String filename, Boolean isOverwrite) throws SVNException {
		String svnfilePath = svnUrl + "/" + filename;
		//开始前删除以前的.svn文件目录
		deletePointSVN(workspace);
		boolean flag = this.isURLExist(SVNURL.parseURIDecoded(svnfilePath));
		if (flag) {
			if (isOverwrite) {
				this.uploadFile(svnUrl, workspace, filepath, filename);
			}
		} else {
			this.uploadFile(svnUrl, workspace, filepath, filename);
		}
		//结束后删除当前的.svn文件目录
		deletePointSVN(workspace);
	}

	/**
	 * @param svnUrl    svn地址
	 * @param workspace 工作区
	 * @param filepath  上传的文件地址
	 * @param filename  文件名称
	 * @throws SVNException 异常信息
	 */
	private void uploadFile(String svnUrl, String workspace, String filepath, String filename) throws SVNException {
		this.checkWorkCopy(svnUrl, workspace, filepath, filename);
		File file = new File(workspace + "/" + filepath);
		this.checkVersiondDirectory(file);
		this.commit(file, false, "commit file:" + file);
	}

	public static void main(String[] args) throws SVNException {
		try {
			String svnUrl = "http://192.168.18.200/svn/repository/hello";
			String username = "admin";
			String passwd = "admin";
			String workspace = "C:\\test";
			String upfile = "/q/w/a.txt,b.txt";
			Boolean isOverwrite = true;
			SvnUtils svnDeal = new SvnUtils(svnUrl, username, passwd);
			String[] fileArray = upfile.split(",");
			for (int i = 0; i < fileArray.length; i++) {
				String filePath = fileArray[i];
				String filename = "";
				if (filePath.indexOf("/") != -1) {
					filename = filePath.substring(filePath.lastIndexOf("/"));
				} else {
					filename = filePath;
				}
				svnDeal.upload(svnUrl, workspace, filePath, filename, isOverwrite);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
