package endrov.unsortedImageFilters;

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
