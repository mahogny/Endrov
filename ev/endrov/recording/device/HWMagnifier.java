/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.device;

/**
 * Devices that support magnification or resolution also implements this
 * 
 * Resolution of a pixel, [um/px]
 * The camera has to return unit [px/um] while all other magnifiers are unitless [-].
 * 
 * @author Johan Henriksson
 *
 */
public interface HWMagnifier 
	{

	/**
	 * X resolution or magnification, [um/px] or []
	 */
	public double getResMagX();

	/**
	 * Y resolution or magnification, [um/px] or []
	 */
	public double getResMagY();
	
	}
