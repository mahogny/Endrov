/**
 * 
 */
package endrov.recording.widgets;

import endrov.util.EvDecimal;

public class RecSettingsTimes
{
public static enum TimeType
	{
	NUMT, SUMT, ONET
	}
public EvDecimal dt;
public Integer numT;
public RecSettingsTimes.TimeType tType;
public EvDecimal sumTime;

public EvDecimal freq; //null means maximum rate
}