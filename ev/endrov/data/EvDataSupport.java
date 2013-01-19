/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import java.io.IOException;
import java.util.List;

import endrov.util.collection.Tuple;

/**
 * Declaration of support for file formats
 * @author Johan Henriksson
 */
public interface EvDataSupport
	{
	public Integer loadSupports(String file);
	public List<Tuple<String,String[]>> getLoadFormats();
	public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception;
	
	public Integer saveSupports(String file);
	public List<Tuple<String,String[]>> getSaveFormats();
	public EvIOData getSaver(EvData d, String file) throws IOException;
	}
