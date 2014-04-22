package quickCoderProvider;

import com.quickcoder.convert.impl.GearmanProvider;

public class GearmanTest {
	public static void main(String[] args) throws Exception {
		GearmanProvider gearmanProvider = new GearmanProvider();
		String filePath="/home/lvy6/视频/163fd1d6fa36758dca662c7341254572.mp4";
		gearmanProvider.transcode(filePath);
	}
}
