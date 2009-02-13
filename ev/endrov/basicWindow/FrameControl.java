package endrov.basicWindow;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * Synchronized Frame(/Z)-controls
 * @author Johan Henriksson
 */
public class FrameControl
	{
	public static interface Synch
		{
		public int getGroup();
		public EvDecimal getFrame();
//		public Integer getZ(); //slice #
		public EvDecimal getModelZ(); //model coord Z
		
//		public void replicate(double frame, Integer slice);
		public void replicate(EvDecimal frame, EvDecimal slice); //model coord
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
		EvDecimal frame=synch.getFrame();
		EvDecimal z=synch.getModelZ();
		
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
	
	/**
	 * Show time as minutes and seconds
	 */
	public static String frameControlMinutes(EvDecimal d)
		{
		Tuple<EvDecimal,EvDecimal> ms=d.divideRemainder(new EvDecimal(60));
		StringBuffer sb=new StringBuffer();
		//if(!ms.fst().equals(EvDecimal.ZERO))
		sb.append(ms.fst()+"m");
		//if(!ms.fst().equals(EvDecimal.ZERO))
		sb.append(ms.snd()+"s");
		return sb.toString();
		}

	/**
	 * Parse time, in seconds, from a string representation
	 */
	public static EvDecimal parseTime(String s)
		{
		EvDecimal accTime=EvDecimal.ZERO;
		Pattern pvalue=Pattern.compile("([0-9]+(?:[.][0-9]+)?[mhs]?)?([0-9]+(?:[.][0-9]+)?[mhs]?)?([0-9]+(?:[.][0-9]+)?[mhs]?)?");
		Matcher m=pvalue.matcher(s);
		if(!m.matches())
			return null;
		for(int i=1;i<=m.groupCount();i++)
			{
			/*
			System.out.println(m.group(i));
			int pos2=i==m.groupCount() ? s.length() : m.start(i+1);
			String spart=s.substring(m.start(i),pos2);
			System.out.println(spart+" "+spartlen+" "+pos2+" ");
			*/
			String spart=m.group(i);
			if(spart!=null)
				{
				int spartlen=spart.length();
				char lastChar=spart.charAt(spartlen-1);
				if(lastChar=='s')
					accTime=accTime.add(new EvDecimal(spart.substring(0,spartlen-1)));
				else if(lastChar=='m')
					accTime=accTime.add(new EvDecimal(spart.substring(0,spartlen-1)).multiply(new EvDecimal(60)));
				else if(lastChar=='h')
					accTime=accTime.add(new EvDecimal(spart.substring(0,spartlen-1)).multiply(new EvDecimal(3600)));
				else
					accTime=accTime.add(new EvDecimal(spart));
				}
			}
		return accTime;
		}

	public static void main(String[] args)
		{
		System.out.println(parseTime("5.2s"));
		System.out.println(parseTime("1m3s"));
		}
	
	
	}
