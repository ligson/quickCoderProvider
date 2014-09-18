package com.quickcoder.convert.impl;

import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;

public class MyTranscodeEvent implements TranscodeEvent{

	@Override
	public void onSubmitFail(DiskFile diskFile, String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSubmitSuccess(DiskFile diskFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTranscode(DiskFile diskFile) {
		// TODO Auto-generated method stub
		System.out.println(diskFile + "开始转码");
	}

	@Override
	public void onTranscodeSuccess(DiskFile diskFile,DiskFile destFile) {
		// TODO Auto-generated method stub
		System.out.println(diskFile + "转码转码成功");
	}

	@Override
	public void onTranscode(DiskFile diskFile, int process) {
		// TODO Auto-generated method stub
		System.out.println(diskFile.getName() + ":" + process);
	}

	@Override
	public void onTranscodeFail(DiskFile diskFile, String errorMessage) {
		// TODO Auto-generated method stub
		System.out.println(diskFile.getName() + ":" + errorMessage);
	}

}
