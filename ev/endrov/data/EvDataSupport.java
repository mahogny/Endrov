package endrov.data;

import java.util.List;

import endrov.util.Tuple;

/**
 * Declaration of support for file formats
 * @author Johan Henriksson
 */
public interface EvDataSupport
	{
	public Integer loadSupports(String file);
	public List<Tuple<String,String[]>> getLoadFormats();
	public EvData load(String file) throws Exception;
	
	public Integer saveSupports(String file);
	public List<Tuple<String,String[]>> getSaveFormats();
	public EvIOData getSaver(EvData d, String file) throws Exception;
	}
