package com.quickcoder.convert.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;
import org.artofsolving.jodconverter.office.OfficeException;

import com.boful.convert.core.ConvertProvider;
import com.boful.convert.core.ConvertProviderConfig;
import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;
import com.boful.convert.utils.DocumentUtils;
import com.boful.convert.utils.FFMpegUtils;
import com.boful.convert.utils.ImageMagickUtils;
import com.boful.convert.utils.MediaInfoUtils;
import com.boful.convert.utils.OpenOfficeTools;
import com.boful.convert.utils.SwfTools;

public class QuickCoderProvider extends ConvertProvider {
	private static Logger logger = Logger.getLogger(QuickCoderProvider.class);
	private String mediaInfoPath;
	private String ffmpegPath;
	private String imageMagickSearchPath;
	private String transcodeDocumentEnable;
	private String openOfficeHome;
	private String pdf2swfPath;
	private String mencoderPath;
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
	
	public String getMencoderPath() {
		return mencoderPath;
	}
	public void setMencoderPath(String mencoderPath) {
		this.mencoderPath = mencoderPath;
	}
	public void setPdf2swfPath(String pdf2swfPath) {
		this.pdf2swfPath = pdf2swfPath;
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
				method.invoke(this, fieldValue);

			}

		}

	}
	
	@Override
	public long getLogicLength(DiskFile diskFile) {
		if(diskFile.isVideo()){
			return MediaInfoUtils.getDuration(mediaInfoPath, diskFile);
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
			openOfficeTools = OpenOfficeTools.getInstance(openOfficeHome);
		} catch (OfficeException exception) {
			logger.error("启动文档转码服务失败！", exception);
		}

		if (openOfficeTools != null) {
			openOfficeTools.convert2PDF(diskFile, destFile, transcodeEvent);
		} else {
			logger.error("文档转码服务未正常启动！");
		}

	}

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
	@Override
	public void transcodeVideo(DiskFile diskFile, DiskFile destFile,
			TranscodeEvent transcodeEvent) {
		super.transcodeVideo(diskFile, destFile, transcodeEvent);
	}
	@Override
	public void transcodeVideo(DiskFile diskFile, DiskFile destFile, int width,
			int height, int videoBitrate, int audioBitrate,
			TranscodeEvent transcodeEvent) {
		VideoConvert videoConvert = new VideoConvert();
		videoConvert.setFfmpegPath(ffmpegPath);
		videoConvert.setMediaInfoPath(mediaInfoPath);
		videoConvert.setMencoderPath(mencoderPath);

		videoConvert.convert(diskFile, destFile, width, height, videoBitrate,
				audioBitrate, transcodeEvent);
	}

}
