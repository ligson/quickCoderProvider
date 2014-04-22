package com.boful.convert.utils;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;

public class FFMpegUtils {

	private static final Logger LOGGER = Logger.getLogger(FFMpegUtils.class);

	/**
	 * 转码参数
	 * 
	 * @param ffmpegPath
	 *            ffmpeg路径
	 * @param mediaInfoPath
	 * 
	 * @param srcPath
	 *            源文件路径
	 * @param destPath
	 *            输出文件路径
	 * @param b
	 *            bitrate设置比特率，缺省200kb/s
	 * @param r
	 *            fps设置帧频 缺省25
	 * @param size
	 *            视频宽高1024x786
	 * @param aspect
	 *            设置横纵比 4:3 16:9 或 1.3333 1.7777
	 * @param vcodec
	 *            codec强制使用codec编解码方式。如果用copy表示原始编解码数据必须被拷贝
	 * @param qscale
	 *            使用固定的视频量化标度(VBR)
	 * @param ab
	 *            设置音频码率
	 * @param ac
	 *            设置通道 缺省为1
	 * @param ar
	 *            设置音频采样率
	 * @param acodec
	 *            设置音频解码器
	 * @param transcodeEvent
	 *            转码消息通知，允许为空
	 * @throws IOException
	 */
	public static void transcode(String ffmpegPath, String mediaInfoPath,
			String srcPath, String destPath, int b, int r, String size,
			String aspect, String vcodec, int qscale, int ab, int ac, int ar,
			String acodec, TranscodeEvent transcodeEvent) throws IOException {
		File ffmpeg = new File(ffmpegPath);
		if (!ffmpeg.exists()) {
			throw new FileNotFoundException("ffmpeg not found");
		}
		File src = new File(srcPath);
		File dest = new File(destPath);
		if ((!src.exists())) {
			throw new FileNotFoundException("src file not found!");
		}
		if (dest.exists()) {
			dest.delete();
		}
		if (!dest.createNewFile()) {
			throw new IOException("create dest file(" + destPath + ") fail!");
		}

		StringBuffer cmd = new StringBuffer();
		cmd.append(ffmpegPath);
		cmd.append(" -i " + srcPath);
		if (b != -1) {
			cmd.append(" -b " + b);
		}
		if (r != -1) {
			cmd.append(" -r " + r);
		}
		if (size != null) {
			if ((!size.contains("-1x-1")) && (!size.contains("-3x-3"))) {
				cmd.append(" -s " + size);
			}
		}
		if (aspect != null) {
			cmd.append(" -aspect " + aspect);
		}
		if (vcodec != null) {
			cmd.append(" -vcodec " + vcodec);
		} else {
			cmd.append(" -vcodec libx264");
		}

		if (qscale != -1) {
			cmd.append(" -qscale " + qscale);
		} else {
			cmd.append(" -qscale 6");
		}
		if (ab != -1) {
			cmd.append(" -ab " + ab);
		}
		if (ac != -1) {
			cmd.append(" -ac " + ac);
		}
		if (ar != -1) {
			cmd.append(" -ar " + ar);
		}
		if (acodec != null) {
			cmd.append(" -acodec " + acodec);
		}

		cmd.append(" -vpre ultrafast ");
		cmd.append(" -crf 26 ");
		cmd.append(" -y " + destPath);

		LOGGER.info("命令行：" + cmd);

		// 开始转码文件
		DiskFile diskFile = new DiskFile(srcPath);
		if (transcodeEvent != null) {
			transcodeEvent.onStartTranscode(diskFile);
		}

		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(cmd.toString());
		InputStream errorInputStream = process.getErrorStream();
		String line;

		BufferedReader errorReader = new BufferedReader(new InputStreamReader(
				errorInputStream));
		long duration = MediaInfoUtils.getDuration(mediaInfoPath, new File(
				srcPath));
		boolean isFail = true;
		while ((line = errorReader.readLine()) != null) {
			// time=00:00:03.63 bitrate=
			LOGGER.debug(line);
			Pattern pattern = Pattern.compile("time=(.*?) bitrate=");
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				isFail = false;
				String group = matcher.group();
				group = group.replaceAll("time=", "");
				group = group.replaceAll(" bitrate=", "");
				long offset = TimeLengthUtils.StringToNumber(group);
				double p = (offset * 1.00 / duration) * 100;
				if (transcodeEvent != null) {
					transcodeEvent.onTranscode(diskFile, (int) p);
				}
				LOGGER.debug("ffmpeg 转码进度:" + (int) p);
			}
		}

		errorInputStream.close();
		errorReader.close();

		if (!isFail) {
			LOGGER.debug(diskFile + "转码成功！");
		} else {
			LOGGER.debug(diskFile + "转码失败！");
			throw new IOException("转码失败尝试mencoder转码！");
		}
		if (transcodeEvent != null) {
			if (!isFail) {
				transcodeEvent.onTranscode(diskFile, 100);
				transcodeEvent.onTranscodeSuccess(diskFile);
			} else {
				transcodeEvent.onTranscodeFail(diskFile, line);
			}
		}

	}

	/**
	 * 默认转码选项
	 * 
	 * @param ffmpegPath
	 *            ffmpeg路径
	 * @param mediaInfoPath
	 *            mediaInfoPath
	 * @param srcPath
	 *            源文件路径
	 * @param destPath
	 *            目标路径
	 * @param transcodeEvent
	 *            转码消息通知，允许为空
	 * @throws FileNotFoundException
	 */
	public static void transcode(String ffmpegPath, String mediaInfoPath,
			String srcPath, String destPath, TranscodeEvent transcodeEvent)
			throws FileNotFoundException, IOException {
		transcode(ffmpegPath, mediaInfoPath, srcPath, destPath, -1, -1, null,
				null, null, -1, -1, -1, -1, null, transcodeEvent);
	}

	/**
	 * 默认转码选项
	 * 
	 * @param ffmpegPath
	 *            ffmpeg路径
	 * @param srcPath
	 *            源文件路径
	 * @param destPath
	 *            目标路径
	 * @throws FileNotFoundException
	 */
	public static void transcode(String ffmpegPath, String mediaInfoPath,
			String srcPath, String destPath) throws FileNotFoundException,
			IOException {
		transcode(ffmpegPath, mediaInfoPath, srcPath, destPath, -1, -1, null,
				null, null, -1, -1, -1, -1, null, null);
	}

	/**
	 * 转到目标分辨率
	 * 
	 * @param ffmpegPath
	 *            ffmpeg路径
	 * @param srcPath
	 *            源文件路径
	 * @param destPath
	 *            目标文件路径
	 * @param size
	 *            目标尺寸(320x280)
	 * @param transcodeEvent
	 *            转码消息通知，允许为空
	 * @throws FileNotFoundException
	 */
	public static void transcode(String ffmpegPath, String mediaInfoPath,
			String srcPath, String destPath, String size,
			TranscodeEvent transcodeEvent) throws FileNotFoundException,
			IOException {
		transcode(ffmpegPath, mediaInfoPath, srcPath, destPath, -1, -1, size,
				null, null, -1, -1, -1, -1, null, transcodeEvent);
	}

	/**
	 * 转到目标分辨率
	 * 
	 * @param ffmpegPath
	 * @param mediaInfoPath
	 * @param srcPath
	 * @param destPath
	 * @param size
	 * @param videoBitrate
	 * @param audioBitrate
	 * @param transcodeEvent
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void transcode(String ffmpegPath, String mediaInfoPath,
			String srcPath, String destPath, String size, int videoBitrate,
			int audioBitrate, TranscodeEvent transcodeEvent)
			throws FileNotFoundException, IOException {
		transcode(ffmpegPath, mediaInfoPath, srcPath, destPath, videoBitrate,
				-1, size, null, null, -1, audioBitrate, -1, -1, null,
				transcodeEvent);
	}

	/**
	 * 
	 * @param src
	 *            原文件
	 * @param width
	 * @param height
	 * @param position
	 *            多少秒的视频截图
	 * @param dest
	 *            截图后的图片路径
	 * @param ffmpegPath
	 *            ffmpeg路径
	 * @return
	 */
	public static boolean videoScreenShot(DiskFile src, String mediaInfoPath,
			int width, int height, int position, DiskFile dest,
			String ffmpegPath) {
		boolean flag = false;

		StringBuffer stringBuilder = new StringBuffer();
		stringBuilder.append(ffmpegPath);

		stringBuilder.append(" -ss ");
		if (position <= 0) {
			position = (int) MediaInfoUtils.getDuration(mediaInfoPath, src);
			position = (position <= 3) ? 1 : (position / 3);

			stringBuilder.append(position);
		} else {
			stringBuilder.append(position);
		}

		stringBuilder.append(" -i ");
		stringBuilder.append(src.getAbsolutePath());
		stringBuilder.append(" -r 1 ");
		stringBuilder.append(" -f image2 ");

		String size = null;
		if ((width != -1 && height != -1) && (width != 0 && height != 0)) {
			size = width + "*" + height;
		} else {
			size = 600 + "*" + 800;
		}
		LOGGER.debug("视频长宽: " + size);
		stringBuilder.append(" -s ");
		stringBuilder.append(size);
		stringBuilder.append(" -y ");
		stringBuilder.append(dest.getAbsolutePath());
		LOGGER.debug("视频截图语句:" + stringBuilder);
		try {

			Process process = Runtime.getRuntime().exec(
					stringBuilder.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				LOGGER.debug(line);
			}
			return flag = true;
		} catch (Exception e) {
			LOGGER.error("视频截图异常: " + e.getMessage(), e);
			return flag = false;
		}

	}

}
