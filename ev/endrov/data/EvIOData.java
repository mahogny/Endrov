/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import java.io.File;
import java.io.IOException;



/**
 * Handle loading and saving of data 
 * 
 * @author Johan Henriksson
 *
 */
public interface EvIOData
	{
	
	/**
	 * TODO in next iteration
	 * * blobs
	 * * new image format
	 * 
	 */
	//can some scanning be held off? in particular, if just objects are needed, should be possible
	//to load set normally without hacks
	
	//how to save XML?
	
	//Image data, blob data and XML data?
	
	public String getMetadataName();

	
	
	/** Scan recording for channels */
	public void buildDatabase(EvData d);
	
	/** 
	 * Save data
	 */
	public void saveData(EvData d, EvData.FileIOStatusCallback cb) throws IOException;
	
	
	/**
	 * Get entry for Load Recent or null if not possible
	 */
	public RecentReference getRecentEntry();

	/** 
	 * Directory for auxiliary data. null if one does not exist
	 */
	public File datadir();

	
	}
