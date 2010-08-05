/**
 * 
 */
package endrov.recording.widgets;

import endrov.util.EvDecimal;

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
	}