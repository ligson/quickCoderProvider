package com.quickcoder.convert.impl;

import java.io.File;
import java.util.UUID;

import org.gearman.Gearman;
import org.gearman.GearmanClient;
import org.gearman.GearmanJobEvent;
import org.gearman.GearmanJobEventCallback;
import org.gearman.GearmanJobEventType;
import org.gearman.GearmanJoin;
import org.gearman.GearmanServer;

import com.boful.common.file.utils.FileUtils;

public class MyConvert implements GearmanJobEventCallback<String> {

	Gearman gearman = Gearman.createGearman();
	GearmanClient client = gearman.createGearmanClient();

	private void start(final String cmd, final String jobId) {
		final MyConvert gearmanConvert = this;
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
	private void stop(){
		gearman.shutdown();
	}

	public void test() {
		GearmanServer server = gearman
				.createGearmanServer("192.168.1.46", 4730);
		client.addServer(server);
		String clientId = "nts_client";
		client.setClientID(clientId);
		System.out.println("转码服务器启动成功!");

		StringBuffer cmd = new StringBuffer();
		String jobId = UUID.randomUUID().toString();

		cmd.append(" -i ftp://ftptest:password@192.168.1.201:21/home/ftptest/video/demo.wmv ");

		cmd.append("-o ftp://ftptest:password@192.168.1.201:21/home/ftptest/video/demo_cq1.mp4 ");

		//cmd.append("-path c:/temp ");
		cmd.append(" -guid " + jobId + " ");
		cmd.append(" -vMode 1 ");
		cmd.append(" -a 12800 ");
		cmd.append(" -memserver  192.168.1.46:" + 11211 + " ");

		// cmd.append("-user "+uploadUser+" -pass "+uploadPass+" ");
		/*
		 * if(width!=0){ cmd.append("-w "+width+" "); } if(height!=0){
		 * cmd.append("-h "+height+" "); }
		 */
		cmd.append("-head ");
		System.out.println(cmd);
		start(cmd.toString(), jobId);
	}

	public static void main(String[] args) {
		MyConvert myConvert = new MyConvert();
		myConvert.test();
	}

	@Override
	public void onEvent(String arg0, GearmanJobEvent event) {
		GearmanJobEventType eventType = event.getEventType();
		if (eventType == GearmanJobEventType.GEARMAN_JOB_SUCCESS) {
			System.out.println("GEARMAN_JOB_SUCCESS");
		} else if (eventType == GearmanJobEventType.GEARMAN_SUBMIT_FAIL) {
			System.out.println("GEARMAN_SUBMIT_FAIL");
			stop();
		} else if (eventType == GearmanJobEventType.GEARMAN_JOB_FAIL) {
			System.out.println("GEARMAN_JOB_FAIL");
			stop();
		} else if (eventType == GearmanJobEventType.GEARMAN_JOB_DATA) {
			System.out.println("GEARMAN_JOB_DATA");
		} else if (eventType == GearmanJobEventType.GEARMAN_JOB_WARNING) {
			System.out.println("GEARMAN_JOB_WARNING");
		} else if (eventType == GearmanJobEventType.GEARMAN_JOB_STATUS) {
			System.out.println("GEARMAN_JOB_STATUS");
			//进度
			System.out.println(new String(event.getData()));
		} else if (eventType == GearmanJobEventType.GEARMAN_SUBMIT_SUCCESS) {
			System.out.println("GEARMAN_SUBMIT_SUCCESS");
		} else if (eventType == GearmanJobEventType.GEARMAN_EOF) {
			System.out.println("GEARMAN_EOF");
			stop();
		} else {
			System.out.println(new String(event.getData()));
		}

	}
}
