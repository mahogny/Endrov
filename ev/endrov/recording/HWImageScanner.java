package endrov.recording;

import endrov.roi.ROI;


/**
 * PMTs and scanning mirror, whatever needed to run a scanning confocal that is not covered elsewhere.
 * 
 * Can emulate a camera if the additional features are not needed
 * 
 * @author Johan Henriksson
 */
public interface HWImageScanner extends HWCamera
	{
	
	
	public void setNumberPixels(int width, int height);
	public int getWidth();
	public int getHeight();
	
	/**
	 * Scan the entire area
	 */
	public void scan();
	
	
	
	/**
	 * Scan ROI, which is a binary mask. non-null means to scan. 
	 */
	public void scan(int[] roi);

	
	
	
	
	//public static void foo(){}
	}
