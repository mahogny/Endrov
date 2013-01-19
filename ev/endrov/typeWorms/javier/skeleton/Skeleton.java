package endrov.typeWorms.javier.skeleton;

import endrov.typeImageset.EvPixels;

/**
 * Abstract definition of a morphological skeleton
 * @author Javier Fernandez
 *
 */
public class Skeleton
	{
	public EvPixels image;
	public int[] dt; // distance transformation of image
	public int w, h; // width and height of image

	/**
	 * General abstraction for a Skeleton. Contains an initial image and a
	 * distance transformation of the given image
	 * 
	 * @param image
	 *          the image from which the skeleton is taken
	 * @param dt
	 *          a distance transformation of image
	 * @param w
	 *          the width of image
	 * @param h
	 *          the height of image
	 */
	public Skeleton(EvPixels image, int[] dt, int w, int h)
		{
		this.image = image;
		this.w = w;
		this.h = h;
		this.dt = new int[dt.length];
		for (int i = 0; i<dt.length; i++)
			this.dt[i] = dt[i];
		}

	/**
	 * General abstraction for a Skeleton. Contains an initial image and a
	 * distance transformation of the given image
	 * 
	 * @param image
	 *          image the image from which the skeleton is taken
	 * @param dt
	 *          a distance transformation of image
	 */
	public Skeleton(EvPixels image, int[] dt)
		{
		this.image = image;
		this.w = image.getWidth();
		this.h = image.getHeight();
		this.dt = new int[dt.length];
		for (int i = 0; i<dt.length; i++)
			this.dt[i] = dt[i];
		}

	}
