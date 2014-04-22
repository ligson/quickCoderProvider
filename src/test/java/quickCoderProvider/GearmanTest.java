package quickCoderProvider;

import java.io.File;

import com.boful.convert.core.ConvertProviderConfig;
import com.quickcoder.convert.impl.QuickCoderProvider;

public class GearmanTest {
	public static void main(String[] args) throws Exception {
		ConvertProviderConfig config = new ConvertProviderConfig();
		File file=new File("/home/lvy6/.boful/convertServer/convert.xml");
		try {
			config.init(file);
		} catch (Exception e) {
			// TODO: handle exception
		}
		QuickCoderProvider quickCoderProvider = new QuickCoderProvider(config);
		String filePath="/home/lvy6/视频/163fd1d6fa36758dca662c7341254572.mp4";
		quickCoderProvider.startJob(filePath);
	}
}
