package com.quickcoder.convert.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.core.impl.utils.FFMpegUtils;
import com.boful.convert.core.impl.utils.MencoderUtils;
import com.boful.convert.model.DiskFile;

class VideoConvert {

	private static Logger logger = Logger.getLogger(VideoConvert.class);
	private String ffmpegPath;
	private String mencoderPath;
	private String mediaInfoPath;
	
	

	public void convert(DiskFile src, DiskFile dest, int width, int height,
			int videoBitrate, int audioBitrate, TranscodeEvent transcodeEvent) {

		// mencoder
		if (src.getFileType().equalsIgnoreCase("rmvb")) {
			convertRmvbToMp4(src, dest, width, height, videoBitrate,
					audioBitrate, transcodeEvent);
		} else {
			try {
				FFMpegUtils.transcode(ffmpegPath, mediaInfoPath,
						src.getAbsolutePath(), dest.getAbsolutePath(), width
								+ "x" + height, transcodeEvent);
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
				transcodeEvent.onTranscodeFail(src, e.getMessage());
			} catch (IOException e) {
				transcodeEvent.onTranscodeFail(src, e.getMessage());
			}
		}
	}

	private void convertRmvbToMp4(DiskFile src, DiskFile dest, int width,
			int height, int videoBitrate, int audioBitrate,
			TranscodeEvent transcodeEvent) {
		try {
			MencoderUtils mencoderUtils = new MencoderUtils(mencoderPath,
					ffmpegPath, mediaInfoPath, width, height, videoBitrate,
					audioBitrate, transcodeEvent, src, dest);
			mencoderUtils.transcodeToAVI();

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			if (transcodeEvent != null) {
				transcodeEvent.onTranscodeFail(src, e.getMessage());
			}

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			if (transcodeEvent != null) {
				transcodeEvent.onTranscodeFail(src, e.getMessage());
			}
		}
	}

	public String getFfmpegPath() {
		return ffmpegPath;
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	public String getMencoderPath() {
		return mencoderPath;
	}

	public void setMencoderPath(String mencoderPath) {
		this.mencoderPath = mencoderPath;
	}

	public String getMediaInfoPath() {
		return mediaInfoPath;
	}

	public void setMediaInfoPath(String mediaInfoPath) {
		this.mediaInfoPath = mediaInfoPath;
	}
}
