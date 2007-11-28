package evplugin.basicWindow;

import java.util.*;

/**
 * Synchronized Frame(/Z)-controls
 * @author Johan Henriksson
 */
public class FrameControl
	{
	public static interface Synch
		{
		public int getGroup();
		public double getFrame();
		public Integer getZ();
		
		public void replicate(double frame, Integer slice);
		}

	/** List of all framecontrols. Used if grouped */
//	private static HashSet<FrameControl.Synch> controls=new HashSet<FrameControl.Synch>();
	private static WeakHashMap<FrameControl.Synch, Object> controls=new WeakHashMap<FrameControl.Synch, Object>();
	
	
	/**
	 * Called whenever there are new settings. These are then transfered to all other
	 * frame controls within the same group.
	 */
	public static void replicateSettings(Synch synch)
		{
		int group=synch.getGroup();
		double frame=synch.getFrame();
		Integer z=synch.getZ();
		
		for(FrameControl.Synch c:FrameControl.controls.keySet())
			if(c.getGroup()==group && c!=synch)
				c.replicate(frame, z);
		}
	
	/**
	 * Check if a group number has been used
	 */
	private static boolean groupUsed(int id)
		{
		for(FrameControl.Synch c:FrameControl.controls.keySet())
			if(c.getGroup()==id)
				return true;
		return false;
		}
	
	/**
	 * Get a unique group number
	 */
	public static int getUniqueGroup()
		{
		int id=0;
		while(groupUsed(id))
			id++;
		return id;
		}
	
	/**
	 * Remove a synchronized control
	 */
	public static void remove(FrameControl.Synch s)
		{
		controls.remove(s);
		}
	
	/**
	 * Add a synchronized control
	 */
	public static void add(FrameControl.Synch s)
		{
		controls.put(s,null);
		}
	
	}
