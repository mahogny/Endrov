/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.opMakeMovie;

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
