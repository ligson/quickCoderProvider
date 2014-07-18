package com.quickcoder.convert.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.apache.log4j.Logger;
import org.gearman.Gearman;
import org.gearman.GearmanClient;
import org.gearman.GearmanJobEvent;
import org.gearman.GearmanJobEventCallback;
import org.gearman.GearmanJobEventType;
import org.gearman.GearmanJoin;
import org.gearman.GearmanServer;

import com.boful.common.file.utils.FileUtils;
import com.boful.convert.core.ConvertProviderConfig;
import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;

/***
 * 
 * @author lvy6
 * 
 */
public class GearmanConvert implements GearmanJobEventCallback<String> {
	private static Logger logger = Logger.getLogger(GearmanConvert.class);
	private static GearmanClient client;
	private static Gearman gearman;
	private String ftpUserName;
	private String ftpUserPassword;
	private String ftpUserHome;
	private String ftpHost;
	private int ftpPort;
	private String transcodeSvrAddress;
	private int transcodeSvrPort;
	private String memcachedAddress;
	private int memcachedPort;
	private TranscodeEvent transcodeEvent;

	private Map<String, String> jobMap = new HashMap<String, String>();

	public GearmanConvert() {
		// TODO Auto-generated constructor stub
	}

	GearmanConvert(QuickCoderProvider quickCoderProvider,
			TranscodeEvent transcodeEvent) {
		this.transcodeEvent = transcodeEvent;
		init(quickCoderProvider);
	}

	/***
	 * 为GearmanConvert对象赋值,并启动转码服务器
	 * 
	 * @param quickCoderProvider
	 *            实体类
	 */
	public void init(QuickCoderProvider quickCoderProvider) {
		// 初始化赋值
		transcodeSvrAddress = quickCoderProvider.getTranscodeSvrAddress();
		transcodeSvrPort = quickCoderProvider.getTranscodeSvrPort();
		ftpUserName = quickCoderProvider.getFtpUserName();
		ftpUserPassword = quickCoderProvider.getFtpUserPassword();
		ftpUserHome = quickCoderProvider.getFtpUserHome();
		ftpPort = quickCoderProvider.getFtpPort();
		ftpHost = quickCoderProvider.getFtpHost();
		memcachedAddress = quickCoderProvider.getMemcachedAddress();
		memcachedPort = quickCoderProvider.getMemcachedPort();
		try {
			gearman = Gearman.createGearman();
			client = gearman.createGearmanClient();
			GearmanServer server = gearman.createGearmanServer(
					transcodeSvrAddress, transcodeSvrPort);
			client.addServer(server);
			String clientId = "nts_client";
			client.setClientID(clientId);
			logger.debug("clientId: " + clientId);
			System.out.println("转码服务器启动成功!");
			logger.debug("转码服务器启动成功!");
		} catch (Exception e) {
			logger.debug("转码服务器启动失败!原因: " + e.getMessage());
		}
	}

	/***
	 * 加入转码任务
	 * 
	 * @param cmd
	 *            转码命令
	 * @param jobId
	 */
	private void start(final String cmd, final String jobId) {
		final GearmanConvert gearmanConvert = this;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				String workName = "worker_convert";
				try {
					GearmanJoin<String> join = client.submitJob(workName, cmd
							.toString().getBytes("GBK"), jobId.getBytes(),
							jobId, gearmanConvert);
					System.out.println("workName :" + workName);
					join.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	public static void shutdown() {
		if (gearman != null) {
			gearman.shutdown();
		}
	}

	/***
	 * 转码命令封装
	 * 
	 * @param diskFile
	 *            转码文件
	 * @param destFile
	 *            输出文件
	 * @param width
	 * @param height
	 * @param videoBitrate
	 *            视频码率
	 * @param audioBitrate
	 *            音频码率
	 * @param transcodeEvent
	 *            转码事件
	 * @throws Exception
	 */
	void gearmanTranscode(DiskFile diskFile, DiskFile destFile, int width,
			int height, int videoBitrate, int audioBitrate) throws Exception {
		String jobId = UUID.randomUUID().toString();
		String filePath = diskFile.getAbsolutePath();
		jobMap.put(jobId, filePath);
		StringBuffer cmd = new StringBuffer();
		// ##############################拼接文件路径开始#################################
		cmd.append(" -i ftp://" + ftpUserName + ":" + ftpUserPassword + "@"
				+ ftpHost + ":" + ftpPort);
		// 判断传递过来的文件是否以"ftpUserHome"属性值开头,以"ftpUserHome"属性值开头替换为空
		if (diskFile.getParentFile().getAbsolutePath().startsWith(ftpUserHome)) {
			String subName =diskFile.getAbsolutePath().replaceFirst(ftpUserHome, "");
			cmd.append(subName);
		} else {
			cmd.append(diskFile.getAbsolutePath());
		}
		// ##############################拼接文件路径结束#################################
		// ##############################拼接输出路径开始#################################
		cmd.append(" -o ftp://" + ftpUserName + ":" + ftpUserPassword + "@"
				+ ftpHost + ":" + ftpPort);
		// 判断传递过来的文件是否以"ftpUserHome"属性值开头,以"ftpUserHome"属性值开头替换为空
		if (destFile.getParentFile().getAbsolutePath().startsWith(ftpUserHome)) {
			String subName =destFile.getAbsolutePath().replaceFirst(ftpUserHome, "");
			cmd.append(subName);
		} else {
			cmd.append(destFile.getAbsolutePath());
		}
		// ##############################拼接输出路径结束#################################
		cmd.append(" -guid " + jobId);
		cmd.append(" -memserver " + memcachedAddress + ":" + memcachedPort);
		if ((width != 0) && (height != 0) && (width != -1) && (height != -1)) {
			cmd.append("  -w " + width);
			cmd.append("  -h " + height);
		}
		// #######################################视频码率###########################
		if (videoBitrate != -1) {
			Double doubleVideo = Double.parseDouble(videoBitrate + "");
			cmd.append(" -v " + doubleVideo / 1000 + "bps");
		}
		// #######################################音频码率###########################
		if (audioBitrate != -1) {
			Double doubleAudio = Double.parseDouble(audioBitrate + "");
			cmd.append(" -a " + doubleAudio / 1000 + "bps");
		}
		cmd.append(" -head ");
		System.out.println(cmd);
		start(cmd.toString(), jobId);
	}

	@Override
	public void onEvent(String str, GearmanJobEvent event) {
		// System.out.println(new String(str) + ":");
		displayEvent(str, event);
	}

	private void displayEvent(String str, GearmanJobEvent event) {
		DiskFile diskFile = null;
		if (jobMap.size() > 0) {
			String filePath = jobMap.get(str);
			diskFile = new DiskFile(filePath);
		}
		GearmanJobEventType eventType = event.getEventType();
		if (eventType == GearmanJobEventType.GEARMAN_JOB_SUCCESS) {
			// System.out.println("GEARMAN_JOB_SUCCESS");
			transcodeEvent.onTranscodeSuccess(diskFile);
		} else if (eventType == GearmanJobEventType.GEARMAN_SUBMIT_FAIL) {
			// System.out.println("GEARMAN_SUBMIT_FAIL");
			transcodeEvent.onSubmitFail(diskFile, "GEARMAN_SUBMIT_FAIL");
			shutdown();
		} else if (eventType == GearmanJobEventType.GEARMAN_JOB_FAIL) {
			// System.out.println("GEARMAN_JOB_FAIL");
			transcodeEvent.onTranscodeFail(diskFile, "GEARMAN_JOB_FAIL");
			shutdown();
		} else if (eventType == GearmanJobEventType.GEARMAN_JOB_DATA) {
			System.out.println("GEARMAN_JOB_DATA");
		} else if (eventType == GearmanJobEventType.GEARMAN_JOB_WARNING) {
			System.out.println("GEARMAN_JOB_WARNING");
		} else if (eventType == GearmanJobEventType.GEARMAN_JOB_STATUS) {
			// System.out.println("GEARMAN_JOB_STATUS");
			// #######################进度###########################
			transcodeEvent.onTranscode(diskFile,
					Integer.parseInt(new String(event.getData())));
			System.out.println(new String(event.getData()));
		} else if (eventType == GearmanJobEventType.GEARMAN_SUBMIT_SUCCESS) {
			// System.out.println("GEARMAN_SUBMIT_SUCCESS");
			transcodeEvent.onSubmitSuccess(diskFile);
		} else if (eventType == GearmanJobEventType.GEARMAN_EOF) {
			System.out.println("GEARMAN_EOF");
			shutdown();
		} else {
			System.out.println(new String(event.getData()));
		}
	}
}
