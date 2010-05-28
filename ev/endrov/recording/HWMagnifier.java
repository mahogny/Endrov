/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

/**
 * Devices that support magnification or resolution also implements this
 * 
 * Resolution of a pixel, [px/um], is calculated as getResMag*getResMag*getResMag...getResMag.
 * The camera has to return unit [px/um] while all other magnifiers are unitless [-].
 * 
 * @author Johan Henriksson
 *
 */
public interface HWMagnifier 
	{

	/**
	 * X resolution or magnification, [px/um] or []
	 */
	public double getResMagX();

	/**
	 * Y resolution or magnification, [px/um] or []
	 */
	public double getResMagY();
	
	}
