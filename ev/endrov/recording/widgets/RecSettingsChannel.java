/**
 * 
 */
package endrov.recording.widgets;

import java.util.LinkedList;
import java.util.List;

import endrov.util.math.EvDecimal;


/**
 * Channel settings for acquisition
 * 
 * @author Johan Henriksson
 *
 */
public class RecSettingsChannel
	{
	public static class OneChannel
		{
		public String name;
		public EvDecimal exposure;
		public boolean adjustRangeByExposure;
		
		public int zIncrement=1;  //Always >=1
		public int zFirst;
		public Integer zLast;
		
		public int tIncrement=1;  //Always >=1
		public int tFirst;
		public Integer tLast;
		
		public int averaging=1;  //Always >=1
		}
	
	public String configGroup;
	public List<RecSettingsChannel.OneChannel> channels=new LinkedList<RecSettingsChannel.OneChannel>();
	}