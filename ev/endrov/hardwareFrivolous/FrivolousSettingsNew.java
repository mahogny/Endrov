/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareFrivolous;

public class FrivolousSettingsNew extends FrivolousSettings{
	public double	lambda			= 550; // 400 - 700nm
	public double	indexRefr		= 1.0; // 1.0 - 1.56
	public double	pixelSpacing	= 30;
	public double	na				= 0.95; //0.25 - 0.95
	public double	sa				= 0.0;
	public int		w				= 512; //Width of "camera"
	public int		h				= 512;
	public double	offsetZ			= 0;
}
