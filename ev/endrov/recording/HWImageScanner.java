package endrov.recording;



/**
 * PMTs and scanning mirror, whatever needed to run a scanning confocal that is not covered elsewhere.
 * 
 * Can emulate a camera if the additional features are not needed
 * 
 * @author Johan Henriksson
 */
public interface HWImageScanner extends HWCamera
	{
	
	
	/**
	 * Set the size of the scanned area
	 * @throws If the size is invalid or the operation is not supported
	 */
	public void setNumberPixels(int width, int height) throws Exception;
	
	public int getWidth();
	public int getHeight();
	
	/**
	 * Scan the entire area
	 * @param status Can be null
	 */
	public void scan(int[] buffer, ScanStatusListener status);
	
	/**
	 * Scan ROI, which is a binary mask. non-null means to scan. 
	 * @param status Can be null
	 */
	public void scan(int[] buffer, ScanStatusListener status, int[] roi);


	/**
	 * Callback for scanner events
	 * @author Johan Henriksson
	 *
	 */
	public static interface ScanStatusListener
		{
		public void lineDone(int y);
		}
	
	}
