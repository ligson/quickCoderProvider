package com.boful.convert.utils;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import com.boful.convert.model.DiskFile;

public class DocumentUtils {
	private static final Logger LOGGER = Logger.getLogger(DocumentUtils.class);

	public static long documentCount(DiskFile src) {
		long count = 0;
		try {
			PDDocument pdDocument = PDDocument.load(src);

			count = pdDocument.getNumberOfPages();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * 
	 * @param src
	 *            原文件
	 * @param width
	 * @param height
	 * @param position
	 *            需要截取第几页文档
	 * @param dest
	 *            截取后的图片路径
	 * @return
	 */
	public static boolean documentScreentShot(DiskFile src, int position, DiskFile dest) {
		boolean flag = false;
		try {
			PDDocument pdDocument = PDDocument.load(src);
			LOGGER.debug(pdDocument);
			int resolution;
			try {
				resolution = Toolkit.getDefaultToolkit().getScreenResolution();
			} catch (HeadlessException e) {
				LOGGER.debug(e.getMessage());
				resolution = 96;
			}
			
			//总页数
			int pageCount = pdDocument.getNumberOfPages();
			LOGGER.debug("总页数:" + pageCount);
			List<PDPage> pages = pdDocument.getDocumentCatalog().getAllPages();
			
			//位置计算
			if (position > pages.size()) {
				LOGGER.debug("要截取的页数已经超过极限!");
				position = 1;
			}
			if (position <= -1) {
				if (pageCount >= 3) {
					position = pageCount / 3;
				} else {
					position = 1;
				}
			}
			if(pageCount==1){
				position=0;
			}
			PDPage page = pages.get(position);
			LOGGER.debug(page);
			BufferedImage image = page.convertToImage(
					BufferedImage.TYPE_INT_RGB, resolution);
			FileOutputStream fileOutputStream = new FileOutputStream(dest);
			ImageIO.write(image, dest.getFileType(), fileOutputStream);
			
			pdDocument.close();
			fileOutputStream.close();
			return flag = true;
		} catch (Exception e) {
			LOGGER.debug("文档截图异常: " + e.getMessage(), e);
			return flag = false;
		}
	}

}
