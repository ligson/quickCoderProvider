package quickCoderProvider;

import java.io.File;

import com.boful.convert.core.ConvertProviderConfig;
import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;
import com.boful.test.MyTranscodeEvent;
import com.quickcoder.convert.impl.QuickCoderProvider;

public class GearmanTest {
	public static void main(String[] args) throws Exception {
		ConvertProviderConfig config = new ConvertProviderConfig();
		File file=new File("I:/coderepos/svn/quickCoderProvider/src/main/resources/convert.xml");
		try {
			config.init(file);
		} catch (Exception e) {
			// TODO: handle exception
		}
		QuickCoderProvider quickCoderProvider = new QuickCoderProvider(config);
		TranscodeEvent transcodeEvent = new GearmanEvent();
		//System.out.println(transcodeEvent.hashCode());
		String filePath="/home/ftptest/video/demo.wmv";
		DiskFile diskFile=new DiskFile(filePath);
		DiskFile destFile=new DiskFile("/home/ftptest/video/demo222.mp4");
		quickCoderProvider.transcodeVideo(diskFile, destFile, transcodeEvent);
		//quickCoderProvider.transcodeVideo(diskFile, destFile,500,300,100,500,transcodeEvent);
		//quickCoderProvider.transcodeVideo(diskFile, destFile,500,300,0,0, null);
		//quickCoderProvider.screenshot(new DiskFile(filePath),500,200,30,new DiskFile("/home/lvy6/test/1.png"));
	}
}
