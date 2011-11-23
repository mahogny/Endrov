/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareMicromanager;

import java.util.*;

import endrov.recording.CameraImage;

import mmcorej.*;

/**
 * Micro-manager utility functions
 * @author Johan Henriksson
 *
 */
public class MMutil
	{
	/**
	 * STL vector to list of strings
	 */
	public static List<String> convVector(StrVector v)
		{
		ArrayList<String> al=new ArrayList<String>((int)v.size());
		for (int j=0; j<v.size(); j++)
			al.add(v.get(j));
		return al;
		}
	
	/**
	 * STL vector to string
	 */
	public static String convVector(CharVector v)
		{
		StringBuffer bf=new StringBuffer();
		for (int j=0; j<v.size(); j++)
			bf.append(v.get(j));
		return bf.toString();
		}

	/**
	 * Produce STL vector
	 */
	public static CharVector convString(String s)
		{
		CharVector v=new CharVector();
		for(int i=0;i<s.length();i++)
			v.add(s.charAt(i));
		return v;
		}
	
	public static List<String> getLoadedDevices(CMMCore core)
		{
		return MMutil.convVector(core.getLoadedDevices());
		}
	
	public static SortedMap<String,String> getPropMap(CMMCore core, String device) throws Exception
		{
		SortedMap<String,String> map=new TreeMap<String, String>();
		for(String key:convVector(core.getDevicePropertyNames(device)))
			map.put(key,core.getProperty(device, key));
		return map;
		}
	
	/**
	 * Snap one image
	 */
	public static CameraImage snap(CMMCore core, String device) throws Exception
		{
		if(!core.getCameraDevice().equals(device))
			core.setCameraDevice(device);

		int bpp=(int)core.getBytesPerPixel();
		int numComponent=(int)core.getNumberOfComponents();
		int bitdepth=(int)core.getImageBitDepth(); //How many bits of dynamic range are to be expected from the camera. This value should be used only as a guideline - it does not guarante that image buffer will contain only values from the returned dynamic range.

		//System.out.println("bpp "+bpp+"   #comp "+numComponent+"    bitdepth "+bitdepth);
		
		//Micromanager supports pixels packed in a special way
		String p=core.getProperty(device, "PixelType");
		if(p.equals("32bitRGB"))
			numComponent=3;
		//Might want to handle this in a totally different way
		
		core.snapImage();

		Object arr;
		if(core.getNumberOfComponents()==1)
			{
			arr=core.getImage();//core.getLastImage();//;

			//If it is a 16-bit image then it must be casted to 32-bit to handle signedness
			if(bpp==2 && bitdepth==16)
				{
				short[] oldarr=(short[])arr;
				int[] newarr=new int[oldarr.length];
				for(int i=0;i<oldarr.length;i++)
					{
					int v=oldarr[i];
					if(v<0)
						v+=32768*2;
					newarr[i]=v;
					}
				arr=newarr;
				bpp=4;
				}
			
			}
		else
			throw new RuntimeException("color cam not supported");
			//arr=core.getRGB32Image();

		if(bpp!=1)
			System.out.println("Got camera bpp "+bpp);

		CameraImage im=new CameraImage(
				(int)core.getImageWidth(),
				(int)core.getImageHeight(),
				bpp,
				arr,
				numComponent
				);
		return im;
		}
	
	
	/**
	 * Snap one image
	 */
	public static CameraImage snapSequence(CMMCore core, String device) throws Exception
		{
		if(!core.getCameraDevice().equals(device))
			core.setCameraDevice(device);

		if(core.getRemainingImageCount()==0)
			return null;
		
		int bpp=(int)core.getBytesPerPixel();
		int numComponent=(int)core.getNumberOfComponents();
		
		//bug workaround???
		String p=core.getProperty(device, "PixelType");
		if(p.equals("32bitRGB"))
			numComponent=3;
		
		Object arr=core.popNextImage();
		if(arr==null)
			return null;

		CameraImage im=new CameraImage(
				(int)core.getImageWidth(),
				(int)core.getImageHeight(),
				bpp,
				arr,
				numComponent
				);
		return im;
		}

	
	}
