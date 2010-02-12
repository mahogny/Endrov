/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverMicromanager;

import endrov.recording.HWCamera;
import endrov.recording.CameraImage;

/**
 * Micro-manager camera
 * @author Johan Henriksson
 *
 */
public class MMCamera extends MMDeviceAdapter implements HWCamera
	{

	public MMCamera(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		}

	public CameraImage snap()
		{
		try
			{
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
		//TODO
		//TODO
		//TODO
		}
	
	public double getResMagX()
		{
		//TODO find from micromanager calibration?
		return getRes();
		}

	public double getResMagY()
		{
		return getRes();
		}
	
	
	
	
	
	public void startSequenceAcq(/*int numImages, */double interval) throws Exception
		{
		mm.core.prepareSequenceAcquisition(mmDeviceName);
		/*if(numImages==null)
			{*/
			mm.core.setCameraDevice(mmDeviceName);
			mm.core.startContinuousSequenceAcquisition(interval);
/*			}
		else
			mm.core.startSequenceAcquisition(mmDeviceName, numImages, interval, false);*/
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
	public int numSequenceLeft()
		{
		return mm.core.getRemainingImageCount();
		}*/
	
	public double getSequenceCapacityFree()
		{
		return mm.core.getBufferFreeCapacity()/(double)mm.core.getBufferTotalCapacity();
		}
	
	
	/**
	 * 
	 * 
void * 	getLastImage () const throw (CMMError)
void * 	popNextImage () throw (CMMError)
void * 	getLastImageMD (unsigned channel, unsigned slice, Metadata &md) const throw (CMMError)
void * 	popNextImageMD (unsigned channel, unsigned slice, Metadata &md) throw (CMMError)
long 	getRemainingImageCount ()
long 	getBufferTotalCapacity ()
long 	getBufferFreeCapacity ()
double 	getBufferIntervalMs () const
bool 	isBufferOverflowed () const
void 	setCircularBufferMemoryFootprint (unsigned sizeMB) throw (CMMError)
void 	intializeCircularBuffer () throw (CMMError)
	 * 
	 * 
	 */

	
	}
