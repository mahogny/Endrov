/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareMicromanager;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDeviceObserver;
import endrov.recording.CameraImage;
import endrov.recording.device.HWCamera;
import endrov.util.EvDecimal;

/**
 * Micro-manager camera
 * 
 * @author Johan Henriksson
 */
public class MMCamera implements HWCamera
	{
	protected MicroManager mm;
	protected String mmDeviceName;

	private EvDecimal expTime = new EvDecimal("100");
	private String forceBPP = "None";
	
	private final static String propExposure="Exposure";
	private final static String propForceBitType="Force pixel format";
	
	private final static String propRoiX="roiX";
	private final static String propRoiY="roiY";
	private final static String propRoiWidth="roiWidth";
	private final static String propRoiHeight="roiHeight";

	
	
	private DevicePropertyType typeFroceBitType=DevicePropertyType.getEditableCategoryState(new String[]{"None","8-bit int","16-bit int","32-bit int"});

	public SortedMap<String, String> getPropertyMap()
		{
		try
			{
			SortedMap<String, String> map=MMutil.getPropMap(mm.core,mmDeviceName);
			map.put("Exposure", ""+expTime);
			
			CameraROI roi=getCameraROI();
			map.put(propRoiX, ""+roi.x);
			map.put(propRoiY, ""+roi.y);
			map.put(propRoiWidth, ""+roi.w);
			map.put(propRoiHeight, ""+roi.h);
			
			map.put(propForceBitType, ""+forceBPP);
			
			return map;
			}
		catch (Exception e)
			{
			return new TreeMap<String, String>();
			}
		}

	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		TreeMap<String, DevicePropertyType> map=new TreeMap<String, DevicePropertyType>();
		try
			{
			for(String propName:MMutil.convVector(mm.core.getDevicePropertyNames(mmDeviceName)))
				{
				DevicePropertyType p=new DevicePropertyType();
				List<String> allowedValues=MMutil.convVector(mm.core.getAllowedPropertyValues(mmDeviceName, propName));
				for(int i=0;i<allowedValues.size();i++)
					p.categories.add(allowedValues.get(i));
				
				p.readOnly=mm.core.isPropertyReadOnly(mmDeviceName, propName);
				
				p.hasRange=mm.core.hasPropertyLimits(mmDeviceName, propName);
				p.rangeLower=mm.core.getPropertyLowerLimit(mmDeviceName, propName);
				p.rangeUpper=mm.core.getPropertyUpperLimit(mmDeviceName, propName);

				if(p.categories.size()==2 && p.categories.contains("0") && p.categories.contains("1"))
					p.isBoolean=true;
				
				map.put(propName,p);
				}
			
			map.put("Exposure", new DevicePropertyType());
			map.put(propForceBitType, typeFroceBitType);
			
			map.put(propRoiX, DevicePropertyType.getEditableIntState());
			map.put(propRoiY, DevicePropertyType.getEditableIntState());
			map.put(propRoiWidth, DevicePropertyType.getEditableIntState());
			map.put(propRoiHeight, DevicePropertyType.getEditableIntState());
			
			return map;
			}
		catch (Exception e)
			{
			return new TreeMap<String, DevicePropertyType>();
			}
		}

	
	/**
	 * Camera ROI
	 * 
	 * @author Johan Henriksson
	 *
	 */
	public static class CameraROI
		{
		public CameraROI()
			{
			}
		
		public CameraROI(int x, int y, int w, int h)
			{
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			}

		public int x, y, w, h;
		}
	
	/**
	 * Get current camera ROI
	 */
	public CameraROI getCameraROI()
		{
		try
			{
			int[] x=new int[1];
			int[] y=new int[1];
			int[] w=new int[1];
			int[] h=new int[1];
			if(!mm.core.getCameraDevice().equals(mmDeviceName))
				mm.core.setCameraDevice(mmDeviceName);
			mm.core.getROI(x,y,w,h);
			return new CameraROI(x[0],y[0],w[0],h[0]);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
		}
	
	/**
	 * Set the camera ROI
	 */
	public void setCameraROI(CameraROI roi)
		{
		try
			{
			if(!mm.core.getCameraDevice().equals(mmDeviceName))
				mm.core.setCameraDevice(mmDeviceName);
			mm.core.setROI(roi.x,roi.y,roi.w,roi.h);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	/**
	 * Set camera ROI to show everything
	 */
	public void resetCameraROI()
		{
		try
			{
			if(!mm.core.getCameraDevice().equals(mmDeviceName))
				mm.core.setCameraDevice(mmDeviceName);
			mm.core.clearROI();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	public String getPropertyValue(String prop)
		{
		try
			{
			if (prop.equals(propExposure))
				return ""+expTime;
			else if(prop.equals(propForceBitType))
				return ""+forceBPP;
			else if(prop.equals(propRoiX))
				return ""+getCameraROI().x;
			else if(prop.equals(propRoiY))
				return ""+getCameraROI().y;
			else if(prop.equals(propRoiWidth))
				return ""+getCameraROI().w;
			else if(prop.equals(propRoiHeight))
				return ""+getCameraROI().h;
			else
				return mm.core.getProperty(mmDeviceName, prop);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return "<mm exception>";
			}
		}

	public void setPropertyValue(String prop, String value)
		{
		try
			{
			if(prop.equals(propExposure))
				expTime=new EvDecimal(value);
			else if(prop.equals(propForceBitType))
				forceBPP=value;
			else if(prop.equals(propRoiX) || prop.equals(propRoiY) || prop.equals(propRoiWidth) || prop.equals(propRoiHeight))
				{
				CameraROI roi=getCameraROI();
				
				if(prop.equals(propRoiX))
					roi.x=Integer.parseInt(value);
				else if(prop.equals(propRoiY))
					roi.y=Integer.parseInt(value);
				else if(prop.equals(propRoiWidth))
					roi.w=Integer.parseInt(value);
				else if(prop.equals(propRoiHeight))
					roi.h=Integer.parseInt(value);
				
				setCameraROI(roi);
				}
			else
				mm.core.setProperty(mmDeviceName, prop, value);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	public MMCamera(MicroManager mm, String mmDeviceName)
		{
		this.mm=mm;
		this.mmDeviceName=mmDeviceName;
		}

	public CameraImage snap()
		{
		try
			{
//			mm.core.set
			//core.set
//			int bpp=(int)core.getBytesPerPixel();
	//		int numComponent=(int)core.getNumberOfComponents();

			mm.core.setExposure(expTime.doubleValue());
			return MMutil.snap(mm.core, mmDeviceName, forceBPP);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
		}
	
	public int getCamWidth()
		{
		return (int)mm.core.getImageWidth();
		}
	public int getCamHeight()
		{
		return (int)mm.core.getImageHeight();
		}

	private double getRes()
		{
		return 1;
		// TODO
		// TODO
		// TODO
		}

	public double getResMagX()
		{
		// TODO find from micromanager calibration?
		return getRes();
		}

	public double getResMagY()
		{
		return getRes();
		}

	public void startSequenceAcq(/* int numImages, */double interval)
			throws Exception
		{
		mm.core.prepareSequenceAcquisition(mmDeviceName);
		/*
		 * if(numImages==null) {
		 */
		mm.core.setCameraDevice(mmDeviceName);
		mm.core.setExposure(expTime.doubleValue());
		mm.core.startContinuousSequenceAcquisition(interval);
		/*
		 * } else mm.core.startSequenceAcquisition(mmDeviceName, numImages,
		 * interval, false);
		 */
		}

	public void stopSequenceAcq()
		{
		try
			{
			mm.core.stopSequenceAcquisition(mmDeviceName);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	public boolean isDoingSequenceAcq()
		{
		try
			{
			return mm.core.isSequenceRunning(mmDeviceName);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return false;
			}
		}

	public CameraImage snapSequence() throws Exception
		{
		return MMutil.snapSequence(mm.core, mmDeviceName, forceBPP);
		}

	/*
	 * public int numSequenceLeft() { return mm.core.getRemainingImageCount(); }
	 */

	public double getSequenceCapacityFree()
		{
		return mm.core.getBufferFreeCapacity()/(double) mm.core.getBufferTotalCapacity();
		}

	
	

	public String getDescName()
		{
		try
			{
			if(mmDeviceName.equals("Core"))
				return "uManager virtual device";
			else
				return mm.core.getProperty(mmDeviceName, "Description");
			}
		catch (Exception e)
			{
			return "<mm exception>";
			}
		}
	
	
	
	public Boolean getPropertyValueBoolean(String prop)
		{
		return getPropertyValue(prop).equals("1");
		}
	
	public void setPropertyValue(String prop, boolean value)
		{
		setPropertyValue(prop, value ? "1" : "0");
		}

	
	public boolean hasConfigureDialog()
		{
		return false;
		}
	
	public void openConfigureDialog(){}

	

	public EvDeviceObserver event=new EvDeviceObserver();
	public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.addWeakListener(listener);
		}
	public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.remove(listener);
		}
	}
