package endrov.data;

import java.io.File;



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
	
	/** Save data */
	public void saveData(EvData d);
	
	
	/**
	 * Get entry for Load Recent or null if not possible
	 */
	public RecentReference getRecentEntry();

	/** 
	 * Directory for auxiliary data. null if one does not exist
	 */
	public File datadir();

	
	}
