/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverMicromanager;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDeviceObserver;
import endrov.recording.HWCamera;
import endrov.recording.CameraImage;
import endrov.util.EvDecimal;

/**
 * Micro-manager camera
 * 
 * @author Johan Henriksson
 */
public class MMCamera /*extends MMDeviceAdapter*/ implements HWCamera
	{

	EvDecimal expTime = new EvDecimal("100");

	public SortedMap<String, String> getPropertyMap()
		{
		try
			{
			SortedMap<String, String> map=MMutil.getPropMap(mm.core,mmDeviceName);
			map.put("Exposure", ""+expTime);
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
			
			return map;
			}
		catch (Exception e)
			{
			return new TreeMap<String, DevicePropertyType>();
			}
		}

	public String getPropertyValue(String prop)
		{
		try
			{
			if (prop.equals("Exposure"))
				return ""+expTime;
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
			if(prop.equals("Exposure"))
				expTime=new EvDecimal(value);
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
			return MMutil.snap(mm.core, mmDeviceName);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
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
		return MMutil.snapSequence(mm.core, mmDeviceName);
		}

	/*
	 * public int numSequenceLeft() { return mm.core.getRemainingImageCount(); }
	 */

	public double getSequenceCapacityFree()
		{
		return mm.core.getBufferFreeCapacity()
				/(double) mm.core.getBufferTotalCapacity();
		}

	/**
	 * void * getLastImage () const throw (CMMError) void * popNextImage () throw
	 * (CMMError) void * getLastImageMD (unsigned channel, unsigned slice,
	 * Metadata &md) const throw (CMMError) void * popNextImageMD (unsigned
	 * channel, unsigned slice, Metadata &md) throw (CMMError) long
	 * getRemainingImageCount () long getBufferTotalCapacity () long
	 * getBufferFreeCapacity () double getBufferIntervalMs () const bool
	 * isBufferOverflowed () const void setCircularBufferMemoryFootprint (unsigned
	 * sizeMB) throw (CMMError) void intializeCircularBuffer () throw (CMMError)
	 */

	
	
	
	protected MicroManager mm;
	protected String mmDeviceName;

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
	public void addListener(EvDeviceObserver.Listener listener)
		{
		event.addWeakListener(listener);
		}
	public void removeListener(EvDeviceObserver.Listener listener)
		{
		event.remove(listener);
		}
	}
