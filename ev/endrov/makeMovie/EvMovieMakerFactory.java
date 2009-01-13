package endrov.makeMovie;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * Movie encoders
 * @author Johan Henriksson
 *
 */
public interface EvMovieMakerFactory
	{
	public static Vector<EvMovieMakerFactory> makers=new Vector<EvMovieMakerFactory>();
	
	/** Also implement toString. or just tostring? */
	public String getName();
	
	/**
	 * File rename is allowed to make it fit format
	 */
	public EvMovieMaker getInstance(File path, int w, int h, String quality) throws Exception;

	/**
	 * Get a list of associated quality levels
	 */
	public List<String> getQualities();
	
	/**
	 * Get the default quality. Should be the same pointer as in qualities list
	 * OR NOT
	 */
	public String getDefaultQuality();
	}
