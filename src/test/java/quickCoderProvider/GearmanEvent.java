package quickCoderProvider;

import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;

public class GearmanEvent implements TranscodeEvent{

	@Override
	public void onSubmitFail(DiskFile diskFile, String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSubmitSuccess(DiskFile diskFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTranscode(DiskFile diskFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTranscodeSuccess(DiskFile diskFile, DiskFile destFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTranscode(DiskFile diskFile, int process) {
		// TODO Auto-generated method stub
		System.out.println(process);
		
	}

	@Override
	public void onTranscodeFail(DiskFile diskFile, String errorMessage) {
		// TODO Auto-generated method stub
		
	}

}
