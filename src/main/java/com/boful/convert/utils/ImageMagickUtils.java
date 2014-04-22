package com.boful.convert.utils;

import java.io.File;
import java.io.IOException;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

/**
 * 图片缩放、压缩
 * 
 * @author ligson
 * 
 */
public class ImageMagickUtils {
	private static ConvertCmd cmd = new ConvertCmd();

	/**
	 * 图片resize
	 * 
	 * @param src
	 * @param dest
	 * @param width
	 *            -1 自适应
	 * @param height
	 *            -1 自适应
	 * @param imageMagickBaseHome
	 * @throws Exception
	 */
	public static void resize(File src, File dest, int width, int height,
			String imageMagickBaseHome) throws Exception {
		if (imageMagickBaseHome != null) {
			ConvertCmd.setGlobalSearchPath(imageMagickBaseHome);
		}
		IMOperation op = new IMOperation();
		op.addImage(src.getAbsolutePath());
		if (width > 0 && height > 0) {
			op.resize(width, height, "!");
		} else {
			op.resize(width, height);
		}

		op.addImage(dest.getAbsolutePath());
		cmd.run(op);
	}
}
