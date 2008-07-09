package endrov.data;


public interface EvDataSupport
	{
	public Integer supports(String file);
	public EvData load(String file) throws Exception;
	}
