package quickCoderProvider;

import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;

public class GearmanEvent implements TranscodeEvent{

	@Override
	public void onSubmitFail(DiskFile diskFile, String errorMessage,
			String jobId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSubmitSuccess(DiskFile diskFile, String jobId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTranscode(DiskFile diskFile, String jobId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTranscodeSuccess(DiskFile diskFile, DiskFile destFile,
			String jobId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTranscode(DiskFile diskFile, int process, String jobId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTranscodeFail(DiskFile diskFile, String errorMessage,
			String jobId) {
		// TODO Auto-generated method stub
		
	}

	

}
