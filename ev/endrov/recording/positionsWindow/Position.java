/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.positionsWindow;

import java.io.Serializable;

import endrov.basicWindow.EvColor;

/**
 * Stores a stage configuration
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */
public class Position implements Serializable
	{

	private static final long serialVersionUID = 1L;

	private AxisInfo[] info;
	private EvColor color;
	private String name;

	public Position(AxisInfo[] axisInfo, String name)
		{
		this.name = name;

		info = new AxisInfo[axisInfo.length];
		for (int i = 0; i<axisInfo.length; i++)
			{
			info[i] = axisInfo[i];
			}

		this.color = new EvColor("White", 1, 1, 1, 1);

		}

	public EvColor getColor()
		{
		return color;
		}

	public AxisInfo[] getAxisInfo()
		{
		return info;
		}

	public String getName()
		{
		return name;
		}

	public void setName(String name)
		{
		this.name = name;
		}

	public String toString()
		{
		String arrayInfo = "";
		for (int i = 0; i<info.length; i++)
			arrayInfo = arrayInfo+" "+info[i];
		return (name+arrayInfo);
		}
	}
