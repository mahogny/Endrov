/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.unsortedImageFilters.unfinished;

/**
 * Segmentation requiring a seeding point
 * @author Johan Henriksson
 *
 */
public class SeedSegmentation
	{

	/*
	 * ITK has a few:
	 * * Connected threshold: neigh intensity difference is I1 < ..< I2
	 * * Neigh connected: also require that all neigh pixels are in interval. user specifies radius
	 * * confidence connected: keep average and variance so far. accept new pixels if in [m-f sigma, m+f sigma]
	 * * a method, given two seeds, keep them not connected
	 * * levelsets
	 * 
	 * 
	 * my idea: might want to seed with areas output from other segmentors
	 * 
	 */
	
	}
