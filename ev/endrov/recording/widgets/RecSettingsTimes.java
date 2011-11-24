package endrov.recording.widgets;

import endrov.hardware.EvDevicePath;
import endrov.util.EvDecimal;

public class RecSettingsTimes
	{
	public static enum TimeType
		{
		NUMT, SUMT, ONET, TRIGGER
		}
	public EvDecimal dt;
	public Integer numT;
	public RecSettingsTimes.TimeType tType;
	public EvDecimal sumTime;
	
	public EvDevicePath trigger;
	
	public EvDecimal freq; //null means maximum rate
	}