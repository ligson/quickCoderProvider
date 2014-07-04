package com.quickcoder.convert.impl;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;

import com.boful.convert.core.ConvertProvider;
import com.boful.convert.core.ConvertProviderConfig;
import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.core.impl.utils.DocumentUtils;
import com.boful.convert.core.impl.utils.FFMpegUtils;
import com.boful.convert.core.impl.utils.ImageMagickUtils;
import com.boful.convert.core.impl.utils.OpenOfficeTools;
import com.boful.convert.core.impl.utils.SwfTools;
import com.boful.convert.model.DiskFile;
import com.boful.convert.utils.MediaInfo;

public class QuickCoderProvider extends ConvertProvider {
	private static Logger logger = Logger.getLogger(QuickCoderProvider.class);
	private String mediaInfoPath;
	private String ffmpegPath;
	private String imageMagickSearchPath;
	private String transcodeDocumentEnable;
	private String openOfficeHome;
	private String pdf2swfPath;
	private String ftpUserName;
	private String ftpUserPassword;
	private String ftpUserHome;
	private String ftpHost;
	private int ftpPort;
	private String transcodeSvrAddress;
	private int transcodeSvrPort ;
	private String memcachedAddress;
	private int memcachedPort;
	
	

	public String getMediaInfoPath() {
		return mediaInfoPath;
	}

	public void setMediaInfoPath(String mediaInfoPath) {
		this.mediaInfoPath = mediaInfoPath;
	}

	public String getFfmpegPath() {
		return ffmpegPath;
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	public String getImageMagickSearchPath() {
		return imageMagickSearchPath;
	}

	public void setImageMagickSearchPath(String imageMagickSearchPath) {
		this.imageMagickSearchPath = imageMagickSearchPath;
	}

	public String getTranscodeDocumentEnable() {
		return transcodeDocumentEnable;
	}

	public void setTranscodeDocumentEnable(String transcodeDocumentEnable) {
		this.transcodeDocumentEnable = transcodeDocumentEnable;
	}

	public String getOpenOfficeHome() {
		return openOfficeHome;
	}

	public void setOpenOfficeHome(String openOfficeHome) {
		this.openOfficeHome = openOfficeHome;
	}

	public String getPdf2swfPath() {
		return pdf2swfPath;
	}

	public void setPdf2swfPath(String pdf2swfPath) {
		this.pdf2swfPath = pdf2swfPath;
	}

	public String getFtpUserName() {
		return ftpUserName;
	}

	public void setFtpUserName(String ftpUserName) {
		this.ftpUserName = ftpUserName;
	}

	public String getFtpUserPassword() {
		return ftpUserPassword;
	}

	public void setFtpUserPassword(String ftpUserPassword) {
		this.ftpUserPassword = ftpUserPassword;
	}

	public String getFtpUserHome() {
		return ftpUserHome;
	}

	public void setFtpUserHome(String ftpUserHome) {
		this.ftpUserHome = ftpUserHome;
	}

	public String getFtpHost() {
		return ftpHost;
	}

	public void setFtpHost(String ftpHost) {
		this.ftpHost = ftpHost;
	}

	public int getFtpPort() {
		return ftpPort;
	}

	public void setFtpPort(int ftpPort) {
		this.ftpPort = ftpPort;
	}

	public String getTranscodeSvrAddress() {
		return transcodeSvrAddress;
	}

	public void setTranscodeSvrAddress(String transcodeSvrAddress) {
		this.transcodeSvrAddress = transcodeSvrAddress;
	}

	public int getTranscodeSvrPort() {
		return transcodeSvrPort;
	}

	public void setTranscodeSvrPort(int transcodeSvrPort) {
		this.transcodeSvrPort = transcodeSvrPort;
	}

	public String getMemcachedAddress() {
		return memcachedAddress;
	}

	public void setMemcachedAddress(String memcachedAddress) {
		this.memcachedAddress = memcachedAddress;
	}

	public int getMemcachedPort() {
		return memcachedPort;
	}

	public void setMemcachedPort(int memcachedPort) {
		this.memcachedPort = memcachedPort;
	}

	public QuickCoderProvider(ConvertProviderConfig config) {
		super(config);
		try {
			voluation(config.getHosts().get(0).getParams());
		} catch (Exception e) {
			logger.error("params复制错误，可能参数不正确：", e);
		}
	}

	public QuickCoderProvider(String name, String description, String ip,
			int port, Map<String, String> params) throws Exception {
		super(name, description, ip, port, params);
		voluation(params);
	}

	/**
	 * 为BofulConvertProvider对象赋值
	 * 
	 * @param params
	 * @return BofulConvertProvider对象
	 */
	public void voluation(Map<String, String> params) throws Exception {
		Class<?> cls = this.getClass();
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			String fieldValue = params.get(fieldName);
			if (fieldValue != null) {
				String methodName = "set"
						+ fieldName.substring(0, 1).toUpperCase()
						+ fieldName.substring(1);
				Method method;
				logger.debug(fieldName + "=" + params.get(fieldName));
				method = cls.getDeclaredMethod(methodName, field.getType());
				logger.debug("调用方法:" + method.getName());
				if(field.getType().getName().equals("int")||field.getType().getName().equals("Integer")){
					method.invoke(this,Integer.parseInt(fieldValue));
				}else {
					method.invoke(this, fieldValue);
				}				

			}

		}

	}
	/**
	 * 获取文件时长
	 */
	@Override
	public long getLogicLength(DiskFile diskFile) {
		if (diskFile.isVideo()) {
			MediaInfo mediaInfo;
			try {
				mediaInfo = new MediaInfo(new File(mediaInfoPath), diskFile);
				return mediaInfo.getVideoInfo().getDuration();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if (diskFile.getName().endsWith("pdf")) {
			return DocumentUtils.documentCount(diskFile);
		}
		return 0;
	}

	@Override
	public boolean isSupportTranscode(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * 文件截图
	 */
	@Override
	public void screenshot(DiskFile src, int width, int height, int position,
			DiskFile dest) throws Exception {
		// TODO Auto-generated method stub
		if (!src.exists()) {
			src.createNewFile();
		}
		// 判断是否为视频
		if (src.isVideo()) {
			if (position <= -1) {
				Long length = getLogicLength(src);
				int value = length.intValue();
				position = value / 3;
			}
			boolean flag = FFMpegUtils.videoScreenShot(src, mediaInfoPath,
					width, height, position, dest, ffmpegPath);
			if (flag) {
				logger.debug("视频截图成功!");
			}

		}

		// 判断是否是文档
		if (src.getName().endsWith("pdf")) {
			boolean flag = DocumentUtils.documentScreentShot(src, position,
					dest);
			if (flag) {
				logger.debug("文档截图成功");
				ImageMagickUtils.resize(dest, dest, width, height,
						imageMagickSearchPath);
			}
		}

		// 是图片
		if (src.isImage()) {
			ImageMagickUtils.resize(src, dest, width, height,
					imageMagickSearchPath);
		}
	}

	@Override
	public String[] supportFormat() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * 文档转码
	 */
	@Override
	public void transcode2PDF(DiskFile diskFile, DiskFile destFile,
			TranscodeEvent transcodeEvent) {
		// TODO Auto-generated method stub
		if ("false".equals(getTranscodeDocumentEnable())) {
			if (transcodeEvent != null) {
				transcodeEvent.onTranscodeFail(diskFile, "文档转码服务未启用！");
			}
			return;
		}
		OpenOfficeTools openOfficeTools = null;
		try {
			openOfficeTools = new OpenOfficeTools(openOfficeHome);
		} catch (Exception exception) {
			logger.error("启动文档转码服务失败！", exception);
		}

		if (openOfficeTools != null) {
			openOfficeTools.convert2PDF(diskFile, destFile, transcodeEvent);
		} else {
			logger.error("文档转码服务未正常启动！");
		}

	}
	/**
	 * 文档转码
	 */
	@Override
	public void transcode2SWF(DiskFile srcFile, DiskFile destFile,
			TranscodeEvent transcodeEvent) {
		if (srcFile != null && srcFile.exists()
				&& srcFile.getAbsolutePath().endsWith("pdf")) {
			SwfTools.transcode(pdf2swfPath, srcFile, destFile, transcodeEvent);
		} else {
			if (transcodeEvent != null) {
				transcodeEvent.onTranscodeFail(srcFile, "不是pdf文件！");
			}
		}
	}
	/**
	 * 视频转码
	 */
	public void transcodeVideo(DiskFile diskFile, DiskFile destFile,
			TranscodeEvent transcodeEvent) {
		super.transcodeVideo(diskFile, destFile, transcodeEvent);
	}
	/**
	 * 视频转码
	 */
	@Override
	public void transcodeVideo(DiskFile diskFile, DiskFile destFile, int width,
			int height, int videoBitrate, int audioBitrate,
			TranscodeEvent transcodeEvent) {
		try {
			GearmanConvert convert = new GearmanConvert(this,transcodeEvent);
			convert.gearmanTranscode(diskFile, destFile, width, height,videoBitrate,audioBitrate);
		} catch (Exception e) {
			logger.debug(e.getMessage());
		}
	}
	/**
	 * 音频转码
	 */
	@Override
	public void transcodeAudio(DiskFile diskFile, DiskFile destFile,
			int audioBitrate, TranscodeEvent transcodeEvent) {
		// TODO Auto-generated method stub
		
	}

}
