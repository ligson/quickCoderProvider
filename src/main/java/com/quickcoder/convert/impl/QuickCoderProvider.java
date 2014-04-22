package com.quickcoder.convert.impl;

import com.boful.convert.core.ConvertProvider;
import com.boful.convert.core.ConvertProviderConfig;
import com.boful.convert.core.TranscodeEvent;
import com.boful.convert.model.DiskFile;

public class QuickCoderProvider extends ConvertProvider {

	public QuickCoderProvider(ConvertProviderConfig config) {
		super(config);
	}

	@Override
	public long getLogicLength(DiskFile arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSupportTranscode(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void screenshot(DiskFile arg0, int arg1, int arg2, int arg3,
			DiskFile arg4) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] supportFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void transcode2PDF(DiskFile arg0, DiskFile arg1, TranscodeEvent arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transcode2SWF(DiskFile arg0, DiskFile arg1, TranscodeEvent arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transcodeVideo(DiskFile arg0, DiskFile arg1, int arg2,
			int arg3, int arg4, int arg5, TranscodeEvent arg6) {
		// TODO Auto-generated method stub

	}

}
