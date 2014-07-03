package quickCoderProvider;

import java.io.File;

import com.boful.convert.core.ConvertProviderConfig;
import com.boful.convert.model.DiskFile;
import com.quickcoder.convert.impl.QuickCoderProvider;

public class GearmanTest {
	public static void main(String[] args) throws Exception {
		ConvertProviderConfig config = new ConvertProviderConfig();
		File file=new File("/home/ligson/.boful/convertServer/convert.xml");
		try {
			config.init(file);
		} catch (Exception e) {
			// TODO: handle exception
		}
		QuickCoderProvider quickCoderProvider = new QuickCoderProvider(config);
		String filePath="/home/ligson/1/1.avi";
		DiskFile diskFile=new DiskFile(filePath);
		DiskFile destFile=new DiskFile("/home/lvy6/视频/163fd1d6fa36758dca662c7341254572_cp.mp4");
		quickCoderProvider.transcodeVideo(diskFile, destFile,null);
		//quickCoderProvider.transcodeVideo(diskFile, destFile,500,300,0,0, null);
		//quickCoderProvider.screenshot(new DiskFile(filePath),500,200,30,new DiskFile("/home/lvy6/test/1.png"));
	}
}
