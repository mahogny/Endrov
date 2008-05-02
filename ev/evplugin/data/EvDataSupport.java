package evplugin.data;

import java.io.File;

public interface EvDataSupport
	{
	public Integer supports(File file);
	public EvData load(File file) throws Exception;
	}
