/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareMicromanager;


import endrov.recording.device.HWSpatialLightModulator;
import endrov.typeImageset.EvPixels;


/**
 * Micro-manager Spatial light modulator
 * @author Johan Henriksson
 *
 */
public class MMSpatialLightModulator extends MMState implements HWSpatialLightModulator
	{
	public MMSpatialLightModulator(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		}

	
	public void setImage(EvPixels p)
		{
		
		}

	public void setImage(EvPixels r, EvPixels g, EvPixels b)
		{
		
		}

	public int getWidth()
		{
		return (int)mm.core.getSLMWidth(mmDeviceName);
		}

	public int getHeight()
		{
		return (int)mm.core.getSLMHeight(mmDeviceName);
		}

	
	public int getNumberOfComponents()
		{
		return (int)mm.core.getSLMNumberOfComponents(mmDeviceName);
		}

	
	/*
	 * TODO
	 * 
	void 	setSLMImage (const char *deviceLabel, unsigned char *pixels) throw (CMMError)
	void 	setSLMImage (const char *deviceLabel, imgRGB32 pixels) throw (CMMError)
	void 	displaySLMImage (const char *deviceLabel) throw (CMMError)
	unsigned 	getSLMBytesPerPixel (const char *deviceLabel) const 
*/
	
	//Not in interface yet
	/*
	public void setOrigin()
		{
		mm.core.setOriginXY(mmDeviceName);
		}
	
	public void stop()
		{
		mm.core.stop(mmDeviceName);
		}
	*/
	
	}
