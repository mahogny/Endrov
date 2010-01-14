/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.unsortedImageFilters.unfinished;

public class DICdetection
	{
	/**
	 * idea:
	 * detect individual nucleoli. might able to consider planes fully separate.
	 * subtract these from image? use a simple fitting model with constant background + shape(r)
	 * 
	 * build a space as "shortest distance to a nucleoli". find maximas. 
	 * 
	 */

	/**
	 * idea:
	 * local entropy 
	 * 
	 * hough transform ring with ring(x,y,z,r) and constant thickness.
	 * inside: low entropy
	 * outside: high entropy 
	 * 
	 * 
	 */
	
	
	/**
	 * idea from xs:
	 * 
	 * mixed gaussian model over 3d
	 * flatness is produced from probability
	 * fit with maximum likelihood
	 * 
	 */
	
	
	/**
	 * helping:
	 * detect outside using local thresholding. mark as high entropy
	 * 
	 * 
	 */
	
	}
