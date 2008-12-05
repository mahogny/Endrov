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
	
	/**
	 * TODO in next iteration
	 * * blobs
	 * * new image format
	 * 
	 */
	
	
	
	/** Scan recording for channels */
	public void buildDatabase(EvData d);
	
	/** Save meta for all channels */
	public void saveMeta(EvData d);
	

	
	public EvImage getImageLoader(String object, String channel, EvDecimal frame, EvDecimal z);
	
	//how to scan images?
	
	
	/**
	 * How to resave imageset
	 * Save as VS Save a copy
	 * 
	 * Save as = Save a copy + reload.
	 * Easiest to implement, internal pointers might get screwed. worth it, uncommon.
	 * up to EVIOData how it is done
	 * 
	 * 
	 * this motivates keeping info about removed objects etc in EVIOData instead of spreading it.
	 * simplifies operations. 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	//can some scanning be held off? in particular, if just objects are needed, should be possible
	//to load set normally without hacks
	
	//how to save XML?
	
	//How to save images? delete images? channels?
	
	//channel meta
	
	//Image data, blob data and XML data?
	
	
	/**
	 * Get entry for Load Recent or null if not possible
	 */
	public RecentReference getRecentEntry();

	/** 
	 * Directory for auxiliary data. null if one does not exist
	 */
	public File datadir();

	
	}
