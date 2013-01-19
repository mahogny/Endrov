/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import endrov.gui.EvColor;

/**
 * Stores a stage configuration
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */
public class StoredStagePosition implements Serializable
	{
	private static final long serialVersionUID = 1L;

	private StoredStagePositionAxis[] info;
	private EvColor color;
	private String name;

	public StoredStagePosition(StoredStagePositionAxis[] axisInfo, String name)
		{
		this.name = name;

		info = new StoredStagePositionAxis[axisInfo.length];
		for (int i = 0; i<axisInfo.length; i++)
			info[i] = axisInfo[i];

		this.color = new EvColor("White", 1, 1, 1, 1);
		}

	public EvColor getColor()
		{
		return color;
		}

	public StoredStagePositionAxis[] getAxisInfo()
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
		return name+arrayInfo;
		}
	
	
	public void goTo()
		{
		
		Map<String, Double> gotoPos = new HashMap<String, Double>();
		for (int i = 0; i<getAxisInfo().length; i++)
			{
			StoredStagePositionAxis ai=getAxisInfo()[i];
			gotoPos.put(
					ai.getDevice().getAxisName()[getAxisInfo()[i].getAxis()], 
					ai.getValue());
			}
		RecordingResource.setStagePos(gotoPos);
		}
	
	
	}
