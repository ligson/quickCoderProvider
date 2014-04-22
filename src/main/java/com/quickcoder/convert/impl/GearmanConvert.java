package com.quickcoder.convert.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.gearman.Gearman;
import org.gearman.GearmanClient;
import org.gearman.GearmanJobEvent;
import org.gearman.GearmanJobEventCallback;
import org.gearman.GearmanJobEventType;
import org.gearman.GearmanJoin;
import org.gearman.GearmanServer;

import com.boful.common.file.utils.FileUtils;
import com.boful.convert.model.DiskFile;

public class GearmanConvert implements GearmanJobEventCallback<String>{
	private static Logger logger = Logger.getLogger(GearmanConvert.class);
	private static GearmanClient client;
	private static Gearman gearman;
	private static String transcodeIp;
	private static int transcodePort;
	private static String memIp;
	private static int memPort;
	private static String uploadIp;
	private static int uploadPort;
	private static String uploadUser;
	private static String uploadPass;
	private static String tempPath;
	private static String svrAddress;
	public GearmanConvert() {
		init();
	}
	public void init(){
		URL url=Thread.currentThread().getContextClassLoader().getResource("Transcode.properties");
		InputStream is = null;
		try {
			is = new FileInputStream(url.getFile());
			Properties p=new Properties();
			p.load(is);
			transcodeIp= p.getProperty("transcode.ip");
			transcodePort=Integer.parseInt(p.getProperty("transcode.port"));
			memIp=p.getProperty("memcached.ip");
			memPort=Integer.parseInt(p.getProperty("memcached.port"));
			uploadIp=p.getProperty("upload.ip");
			uploadPort=Integer.parseInt(p.getProperty("upload.port"));
			uploadUser=p.getProperty("upload.user");
			uploadPass=p.getProperty("upload.pass");
			tempPath=p.getProperty("transcode.tempPath");
			svrAddress=p.getProperty("transcode.svrAddress");
			Gearman gearman = Gearman.createGearman();
			client = gearman.createGearmanClient();
			GearmanServer server = gearman.createGearmanServer(transcodeIp, transcodePort);
			client.addServer(server);
			String clientId = "nts_client";
			client.setClientID(clientId);
			logger.debug("clientId: " + clientId);
			System.out.println("转码服务器启动成功!");
			logger.debug("转码服务器启动成功!");
		} catch (Exception e) {
			logger.debug("转码服务器启动失败!原因: "+e.getMessage());
		}
	}
	private void start(final String cmd,final String jobId){
		final GearmanConvert gearmanConvert = this;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				String workName = "worker_convert";
				try {
					GearmanJoin<String> join = client.submitJob(workName,cmd.toString().getBytes("GBK"), jobId.getBytes(),jobId,gearmanConvert);
					System.out.println("workName :"+workName);
					join.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}
	public void shutdown(){
		if(gearman != null){
				gearman.shutdown();
		}	
	}
	public void gearmanTranscode(DiskFile diskFile, DiskFile destFile, int width,int height) throws Exception{
		String filePath=diskFile.getAbsolutePath();
		System.out.println("filePath :"+diskFile.getAbsolutePath());
		String jobId = UUID.randomUUID().toString();
		StringBuffer cmd = new StringBuffer();
		if(svrAddress!=""&&svrAddress!="127.0.0.1"&&svrAddress!="localhost"){
			cmd.append("-i bmsp://ADDR="+svrAddress+":1680;FILE="+filePath+";PFG=2; ");
			if(destFile!=null&&destFile.exists()){
				cmd.append("-o "+tempPath+"/"+destFile.getName()+" ");
			}else {
				cmd.append("-o "+tempPath+"/"+FileUtils.getFilePrefix(new File(filePath).getName())+"_cq.mp4 ");
			}
			cmd.append("-path "+diskFile.getParentFile().getAbsolutePath()+" ");
		}else {
			cmd.append("-i "+filePath+" ");
			if(destFile!=null&&destFile.exists()){
				cmd.append("-o "+destFile.getName()+" ");
			}else {
				cmd.append("-o "+new File(filePath+"_cq.mp4").getAbsolutePath()+" ");
			}
			cmd.append("-path "+diskFile.getParentFile().getAbsolutePath()+" ");
		}
		cmd.append("-guid "+jobId+" ");
		cmd.append("-memserver "+memIp+":"+memPort+" ");
		cmd.append("-addr "+uploadIp+" -port "+uploadPort+" ");
		cmd.append("-user "+uploadUser+" -pass "+uploadPass+" ");
		if(width!=0){
			cmd.append("-w "+width+" ");
		}
		if(height!=0){
			cmd.append("-h "+height+" ");
		}
		cmd.append("-head ");
		System.out.println(cmd);
		start(cmd.toString(), jobId);
	}
	@Override
	public void onEvent(String str, GearmanJobEvent event) {
		System.out.println(new String(str) + ":");
        displayEvent(str, event);
	}
	private static void displayEvent(String str, GearmanJobEvent event){
		GearmanJobEventType eventType =  event.getEventType();
		if(eventType==GearmanJobEventType.GEARMAN_JOB_SUCCESS){
			System.out.println("GEARMAN_JOB_SUCCESS");
		}else if(eventType==GearmanJobEventType.GEARMAN_SUBMIT_FAIL){
			System.out.println("GEARMAN_SUBMIT_FAIL");
		}else if(eventType==GearmanJobEventType.GEARMAN_JOB_FAIL){
			System.out.println("GEARMAN_JOB_FAIL");
		}else if(eventType==GearmanJobEventType.GEARMAN_JOB_DATA){
			System.out.println("GEARMAN_JOB_DATA");
		}else if (eventType==GearmanJobEventType.GEARMAN_JOB_WARNING) {
			System.out.println("GEARMAN_JOB_WARNING");
		}else if (eventType==GearmanJobEventType.GEARMAN_JOB_STATUS) {
			System.out.println("GEARMAN_JOB_STATUS");
		}else if (eventType==GearmanJobEventType.GEARMAN_SUBMIT_SUCCESS) {
			System.out.println("GEARMAN_SUBMIT_SUCCESS");
		}else if (eventType==GearmanJobEventType.GEARMAN_EOF) {
			System.out.println("GEARMAN_EOF");
		}else {
			System.out.println(new String(event.getData()));
		}
	}
}
