package com.boful.convert.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class MediaInfoUtils {
	private static Logger logger = Logger.getLogger(MediaInfoUtils.class);

	public static long getDuration(String mediaInfoPath, File src) {
		StringBuffer stringBuffer = new StringBuffer(mediaInfoPath);
		stringBuffer.append("  ");
		stringBuffer.append(src.getAbsolutePath());
		Runtime runtime = Runtime.getRuntime();
		try {
			logger.debug(stringBuffer);
			Process process = runtime.exec(stringBuffer.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("Duration")) {
					break;
				}
			}
			reader.close();

			if (line == null) {
				return 0;
			}
			if (line.startsWith("Duration")) {
				int hour = 0;
				int min = 0;
				int second = 0;
				int millsecond = 0;

				int index = line.indexOf(":");
				if (index > 0) {
					String duraString = line.substring(index + 1);
					Pattern hourPattern = Pattern.compile("\\d+h");
					Matcher hourMatcher = hourPattern.matcher(line);
					if (hourMatcher.find()) {
						try {
							hour = Integer.parseInt(hourMatcher.group()
									.replace("h", ""));
						} catch (Exception e) {
						}
					}

					Pattern minPattern = Pattern.compile("\\d+mn");
					Matcher minMatcher = minPattern.matcher(line);
					if (minMatcher.find()) {
						try {
							min = Integer.parseInt(minMatcher.group().replace(
									"mn", ""));
						} catch (Exception e) {
						}
					}

					Pattern secondPattern = Pattern.compile("\\d+s");
					Matcher secondMatcher = secondPattern.matcher(line);
					if (secondMatcher.find()) {
						try {
							second = Integer.parseInt(secondMatcher.group()
									.replace("s", ""));
						} catch (Exception e) {
						}
					}

					Pattern minSecondPattern = Pattern.compile("\\d+ms");
					Matcher minSecondMatcher = minSecondPattern.matcher(line);
					if (minSecondMatcher.find()) {
						try {
							millsecond = Integer.parseInt(minSecondMatcher
									.group().replace("ms", ""));
						} catch (Exception e) {
						}
					}

				}

				logger.debug(hour + "h" + min + "mn" + second + "s"
						+ millsecond + "ms");
				return hour * 3600 + min * 60 + second;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	// kbps
	public static long getOverAllBitRate(String mediaInfoPath, File src) {
		StringBuffer stringBuffer = new StringBuffer(mediaInfoPath);
		stringBuffer.append("  ");
		stringBuffer.append(src.getAbsolutePath());
		Runtime runtime = Runtime.getRuntime();
		try {
			logger.debug(stringBuffer);
			Process process = runtime.exec(stringBuffer.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("Overall bit rate")) {
					break;
				}
			}
			reader.close();

			if (line == null) {
				return 0;
			}
			if (line.startsWith("Overall bit rate")) {
				int index = line.indexOf(":");
				String bitString = line.substring(index + 1);

				try {
					int bit = Integer.parseInt(bitString.replace("Kbps", "")
							.replace(" ", ""));
					logger.debug(bit + " Kbps");
					return bit;
				} catch (Exception e) {
				}
			}
		} catch (Exception exception) {

		}
		return 0;
	}

	public static void main(String[] args) {
		File file = new File("/store/store/清华大学“金紫荆“微电影大赛42号_楼长的背包_李瑜.avi");
		File meidaInfo = new File("/usr/bin/mediainfo");
		System.out.println(getDuration(meidaInfo.getAbsolutePath(), file));
		System.out
				.println(getOverAllBitRate(meidaInfo.getAbsolutePath(), file));

	}
}
