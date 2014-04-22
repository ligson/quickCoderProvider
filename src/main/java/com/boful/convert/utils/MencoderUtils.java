package com.boful.convert.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;

public class MencoderUtils {

	private static final Logger LOGGER = Logger.getLogger(MencoderUtils.class);


		public void process(String cmd) {
			if (getTranscodeEvent() != null) {
				getTranscodeEvent().onStartTranscode(
						getSrc());
			}
			Runtime runtime = Runtime.getRuntime();
			Pattern pattern1 = Pattern.compile("([ ]*\\d+%)");
			Pattern pattern2 = Pattern.compile("\\d+");
			try {
				Process process = runtime.exec(cmd);
				InputStream errorStream = process.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(errorStream, "UTF-8"));
				String line = null;
				boolean isFail = true;
				while ((line = bufferedReader.readLine()) != null) {
					Matcher matcher1 = pattern1.matcher(line);
					if (matcher1.find()) {
						String group1 = matcher1.group();
						Matcher matcher2 = pattern2.matcher(group1);
						if (matcher2.find()) {
							int processNum = Integer.parseInt(matcher2.group());
							isFail = true;
							if (getTranscodeEvent() != null) {
								getTranscodeEvent().onTranscode(
										getSrc(), processNum);
							}
							LOGGER.debug(getSrc()
									+ "mencoder transcode process :"
									+ processNum + " %");
						}
					}
				}

				errorStream.close();
				bufferedReader.close();

				if (getTranscodeEvent() != null) {
					getTranscodeEvent().onTranscode(
							getSrc(), 100);
					getTranscodeEvent().onTranscodeSuccess(
							getSrc());
				}
				LOGGER.debug(getSrc() + " transcode success!");
				File fileTmp = new File(getSrc().getParentFile(),
						getSrc().getName() + ".avi");
				transcodeToMp4(fileTmp.getAbsolutePath(),
						getDest().getAbsolutePath());
			} catch (Exception e) {
				if (getTranscodeEvent() != null) {
					getTranscodeEvent().onTranscodeFail(
							getSrc(), e.getMessage());
				}
				LOGGER.error(getSrc() + "transcode fail!", e);
			}

		}


	private String mencoderPath;

	public void transcodeToAVI() throws IOException {
		File mencoder = new File(mencoderPath);

		if (!mencoder.exists()) {
			throw new FileNotFoundException("mencoder not found!");
		}
		if (!src.exists()) {
			throw new FileNotFoundException("source file not found!");
		}
		if (dest.exists()) {
			dest.delete();
		}
		dest.createNewFile();

		StringBuffer cmd = new StringBuffer();
		cmd.append(mencoderPath);
		cmd.append(" " + src.getAbsolutePath());
		File fileTmp = new File(src.getParentFile(), src.getName() + ".avi");
		cmd.append(" -o " + fileTmp.getAbsolutePath());
		cmd.append(" -oac mp3lame ");
		cmd.append(" -ovc xvid");
		if (videoBitrate > 0) {
			cmd.append(" -xvidencopts bitrate=" + videoBitrate);
		} else {
			cmd.append(" -xvidencopts bitrate=600");
		}

		if (audioBitrate > 0) {
			cmd.append(" -lameopts preset=" + audioBitrate);
		} else {
			cmd.append(" -lameopts preset=64");
		}

		cmd.append(" -of avi ");
		if (width <= 0) {
			width = -3;
		}
		if (height <= 0) {
			height = -3;
		}
		if (!(height <= 0 && width <= 0)) {
			cmd.append(" -vf scale=" + width + ":" + height + ",harddup");
		}
		LOGGER.info("命令行:" + cmd);
		process(cmd.toString());
	}

	private String ffmpegPath;
	private String mediaInfoPath;
	private int width;
	private int height;
	private int videoBitrate;
	private int audioBitrate;
	private TranscodeEvent transcodeEvent;
	private DiskFile src;
	private DiskFile dest;

	public MencoderUtils(String mencoderPath, String ffmpegPath,
			String mediaInfoPath, int width, int height, int videoBitrate,
			int audioBitrate, TranscodeEvent transcodeEvent, DiskFile src,
			DiskFile dest) {
		super();
		this.mencoderPath = mencoderPath;
		this.ffmpegPath = ffmpegPath;
		this.mediaInfoPath = mediaInfoPath;
		this.width = width;
		this.height = height;
		this.videoBitrate = videoBitrate;
		this.audioBitrate = audioBitrate;
		this.transcodeEvent = transcodeEvent;
		this.src = src;
		this.dest = dest;
	}

	private void transcodeToMp4(String aviFilePath, String destFilePath) {
		try {
			FFMpegUtils.transcode(ffmpegPath, mediaInfoPath, aviFilePath,
					destFilePath, width + "x" + height, videoBitrate,
					audioBitrate, transcodeEvent);
			File file = new File(aviFilePath);
			if(file.exists()){
				file.delete();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getMencoderPath() {
		return mencoderPath;
	}

	public void setMencoderPath(String mencoderPath) {
		this.mencoderPath = mencoderPath;
	}

	public String getFfmpegPath() {
		return ffmpegPath;
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	public String getMediaInfoPath() {
		return mediaInfoPath;
	}

	public void setMediaInfoPath(String mediaInfoPath) {
		this.mediaInfoPath = mediaInfoPath;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getVideoBitrate() {
		return videoBitrate;
	}

	public void setVideoBitrate(int videoBitrate) {
		this.videoBitrate = videoBitrate;
	}

	public int getAudioBitrate() {
		return audioBitrate;
	}

	public void setAudioBitrate(int audioBitrate) {
		this.audioBitrate = audioBitrate;
	}

	public TranscodeEvent getTranscodeEvent() {
		return transcodeEvent;
	}

	public void setTranscodeEvent(TranscodeEvent transcodeEvent) {
		this.transcodeEvent = transcodeEvent;
	}

	public DiskFile getSrc() {
		return src;
	}

	public void setSrc(DiskFile src) {
		this.src = src;
	}

	public DiskFile getDest() {
		return dest;
	}

	public void setDest(DiskFile dest) {
		this.dest = dest;
	}
}
