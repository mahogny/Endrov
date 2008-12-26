package endrov.makeMovie;

import java.io.File;
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
	public EvMovieMaker getInstance(File path, int w, int h, String quality);

	}
