package endrov.makeMovie;

import java.awt.image.BufferedImage;

/**
 * One encoder of movies
 * @author Johan Henriksson
 *
 */
public interface EvMovieMaker
	{
//	movieMaker=new QTMovieMaker(moviePath.getAbsolutePath(), c.getWidth(), c.getHeight(), inputCodec, inputQuality);
	public void addFrame(BufferedImage im) throws Exception;
	public void done() throws Exception;
	}
