package com.boful.convert.utils;

import org.apache.log4j.Logger;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;

public class OpenOfficeTools {
	private String openOfficeHome;
	private static DefaultOfficeManagerConfiguration config = new DefaultOfficeManagerConfiguration();
	private static OfficeDocumentConverter converter;
	private static OpenOfficeTools openOfficeTools;
	private static OfficeManager officeManager;
	private static Logger logger = Logger.getLogger(OpenOfficeTools.class);

	public static synchronized OpenOfficeTools getInstance(String openOfficeHome) {
		if (openOfficeTools == null) {
			openOfficeTools = new OpenOfficeTools(openOfficeHome);
		}
		return openOfficeTools;
	}

	public String getOpenOfficeHome() {
		return openOfficeHome;
	}

	public void setOpenOfficeHome(String openOfficeHome) {
		this.openOfficeHome = openOfficeHome;
	}

	private OpenOfficeTools(String openOfficeHome) {
		super();
		this.openOfficeHome = openOfficeHome;
		if (openOfficeHome != null) {
			config.setOfficeHome(getOpenOfficeHome());
		}

		// 启动OpenOffice的服务
		officeManager = config.buildOfficeManager();
		logger.debug("启动OpenOffice转码服务！");

		officeManager.start();
		// 连接OpenOffice
		converter = new OfficeDocumentConverter(officeManager);
		logger.debug("启动OpenOffice转码服务成功！");
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		officeManager.stop();
	}

	public void convert2PDF(DiskFile srcFile, DiskFile destFile,
			TranscodeEvent transcodeEvent) {
		try {
			if (converter != null) {
				if (transcodeEvent != null) {
					transcodeEvent.onStartTranscode(srcFile);
				}
				converter.convert(srcFile, destFile);
				if (transcodeEvent != null) {
					transcodeEvent.onTranscodeSuccess(srcFile);
				}
			}
		} catch (Exception e) {
			if (transcodeEvent != null) {
				transcodeEvent.onTranscodeFail(srcFile, e.getMessage());
			}
		}

	}
}
