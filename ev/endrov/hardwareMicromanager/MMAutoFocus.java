/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareMicromanager;

import java.io.IOException;

import endrov.recording.device.HWAutoFocus;

//could preload list of properties

//mm virtual property: state. map to setstate


/**
 * Micro manager auto focus
 * @author Johan Henriksson
 *
 */
public class MMAutoFocus extends MMDeviceAdapter implements HWAutoFocus
	{
	private double offset=0;
		
	public MMAutoFocus(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		}
	
	/**
	 * Since the MM core can only handle one offset, make sure the right one is set before using one of the other commands
	 */
	private void ensureRightMMoffset()
		{
		try
			{
			double curOffset=mm.core.getAutoFocusOffset();
			if(curOffset!=offset)
				mm.core.setAutoFocusOffset(curOffset);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	private void ensureRightDevice()
		{
		try
			{
			mm.core.setAutoFocusDevice(mmDeviceName);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	public double getLastFocusScore()
		{
		ensureRightDevice();
		ensureRightMMoffset();
		return mm.core.getLastFocusScore();
		}
	public double getCurrentFocusScore()
		{
		ensureRightDevice();
		ensureRightMMoffset();
		return mm.core.getCurrentFocusScore();
		}
	
	
	
	public void fullFocus() throws IOException
		{
		ensureRightDevice();
		ensureRightMMoffset();
		try
			{
			mm.core.fullFocus();
			}
		catch (Exception e)
			{
			throw new IOException("full focus error");
			}
		}
	
	
	public void incrementalFocus() throws IOException //Another exception?
		{
		ensureRightDevice();
		ensureRightMMoffset();
		try
			{
			mm.core.incrementalFocus();
			}
		catch (Exception e)
			{
			throw new IOException("incremental focus error");
			}
		}

		
	
	public void setAutoFocusOffset(double offset)
		{
		try
			{
			ensureRightDevice();
			this.offset=offset;
			mm.core.setAutoFocusOffset(offset);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	
	public double getAutoFocusOffset()
		{
		return offset;
		}
		
	
	
	public void enableContinuousFocus(boolean enable)
		{
		try
			{
			ensureRightDevice();
			mm.core.enableContinuousFocus(enable);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	public boolean isContinuousFocusEnabled()
		{
		try
			{
			ensureRightDevice();
			return mm.core.isContinuousFocusEnabled();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return false;
			}
		}
	public boolean isContinuousFocusLocked()
		{
		try
			{
			ensureRightDevice();
			return mm.core.isContinuousFocusLocked();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return false;
			}
		}
	}
