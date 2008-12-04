package endrov.data;

import java.io.File;

import endrov.imageset.EvImage;
import endrov.util.EvDecimal;


/**
 * Handle loading and saving of data. 
 * 
 * 
 * @author Johan Henriksson
 *
 */
public interface EvIOData
	{
	
	
	
	/** Scan recording for channels */
	public void buildDatabase();
	
	/** Save meta for all channels */
	public void saveMeta();
	
	/** 
	 * Directory for auxiliary data. null if one does not exist
	 */
	public File datadir();

	
	public EvImage getImageLoader(String object, String channel, EvDecimal frame, EvDecimal z);
	
	//how to scan images?
	
	//can some scanning be held off? in particular, if just objects are needed, should be possible
	//to load set normally without hacks
	
	//how to save XML?
	
	//How to save images? delete images? channels?
	
	//channel meta
	
	//Image data, blob data and XML data?
	
	
//io must have RecentReference
	
	}
