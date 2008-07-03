package endrov.jubio;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.swing.*;

public class Jubio
	{
	private int internaladdr;
	
	private static native int _load(String filename, int slice);
	private static native void _unload(int addr);
	
	private static native int _getWidth(int addr);
	private static native int _getHeight(int addr);
	private static native int _getDepth(int addr);
	
	private static native int _calcMax(int addr);
	
	private static native byte[] _getDataArray(int addr, int slice);

	static
		{
		try
			{
			//System.load("libs/mac/libjubio.jnilib");
			System.loadLibrary("jubio");
			}
		catch (Exception e) //UnsatisfiedLinkError
			{
			JOptionPane.showMessageDialog(null, "Jubio link error");
			e.printStackTrace();
			}
		}
	
	/**
	 * Create a jubio object by loading a slice from a file
	 * @param filename File to load
	 * @param slice Slice to load or all if -1
	 */
	public Jubio(String filename,int slice) throws Exception
		{
		if(!load(filename,slice))
			throw new Exception();
		}
	
	/**
	 * Create a jubio object by loading all slices from a file
	 */
	public Jubio(String filename) throws Exception
		{
		this(filename,-1);
		}

	/**
	 * Create structure but don't load any image
	 */
	private Jubio()
		{
		}

	
	/**
	 * Get width of image
	 */
	public int getWidth()
		{
		return _getWidth(internaladdr);
		}
	
	/**
	 * Get height of image
	 */
	public int getHeight()
		{
		return _getHeight(internaladdr);
		}
	
	/**
	 * Get depth/number of slices of image
	 */
	public int getDepth()
		{
		return _getDepth(internaladdr);
		}
	
	/**
	 * Load graphics data
	 */
	private boolean load(String filename, int slice)
		{
		internaladdr=_load(filename, slice);
		return internaladdr!=0;
		}
	
	
	/**
	 * Unload the graphics data
	 */
	private void unload()
		{
		if(internaladdr!=0)
			_unload(internaladdr);
		internaladdr=0;
		}
	

	/**
	 * Make sure GC removes C-allocated memory
	 */
	protected void finalize() throws Throwable
		{
		unload();
		}
	
	
	

	/**
	 * Get any Jubio slice into java space
	 */
	public BufferedImage getBufferedImage(int slice)
		{
		if(internaladdr!=0)
			{
			int w=getWidth();
			int h=getHeight();
			BufferedImage wim=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
			WritableRaster raster=wim.getRaster();
			
			byte[] bArray=_getDataArray(internaladdr,slice);
			int[] iArray=new int[bArray.length];
			for(int i=0;i<bArray.length;i++)
				iArray[i]=bArray[i];
			raster.setSamples(0,0,w,h,0,iArray);
			
			return wim;
			}
		else
			return null;
		}
	
	/**
	 * Get Jubio slice 0 into java space
	 */
	public BufferedImage getBufferedImage()
		{
		return getBufferedImage(0);
		}
	
	/**
	 * Calculate maximum value at every position
	 * @return New slice
	 */
	public Jubio calcMax()
		{
		Jubio max=new Jubio();
		max.internaladdr=_calcMax(internaladdr);
		return max;
		}
	
	
	}
