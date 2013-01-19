package endrov.recording.widgets;

import endrov.util.math.EvDecimal;

public class RecSettingsSlices
	{
	public static enum ZType
		{
		NUMZ, DZ, ONEZ
		}
	public EvDecimal start, end;
	public EvDecimal dz;
	public Integer numZ;
	public RecSettingsSlices.ZType zType;
	
	@Override
	public String toString()
		{
		return ""+start+" "+end+"  "+dz+"  "+numZ+"  "+zType;
		}
	}