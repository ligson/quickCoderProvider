package com.boful.convert.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;



public class SwfTools {

	private static Logger logger = Logger.getLogger(SwfTools.class);

	public static void transcode(String pdf2swfPath, DiskFile srcFile,
			DiskFile destFile, TranscodeEvent transcodeEvent) {
		Runtime runtime = Runtime.getRuntime();
		StringBuffer cmd = new StringBuffer(pdf2swfPath + " ");
		cmd.append(srcFile.getAbsolutePath() + " ");
		cmd.append("-o " + destFile.getAbsolutePath() + " ");
		cmd.append("-T 9 -f ");
		if (transcodeEvent != null) {
			transcodeEvent.onStartTranscode(srcFile);
		}
		try {
			logger.debug("命令行:" + cmd);
			Process process = runtime.exec(cmd.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				logger.debug(line);
			}
			reader.close();
			if (transcodeEvent != null) {
				transcodeEvent.onTranscodeSuccess(srcFile);
			}
		} catch (IOException e) {
			logger.debug(e.getMessage());
			if (transcodeEvent != null) {
				transcodeEvent.onTranscodeFail(srcFile, e.getMessage());
			}
		}
	}
}
