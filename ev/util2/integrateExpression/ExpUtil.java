package util2.integrateExpression;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import endrov.imageset.Imageset;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;

/**
 * Util functions to mess around with expression levels
 */
public class ExpUtil
	{

	
	/**
	 * Correct for background etc. Signal has already been divided by exposure time. 
	 * Try to keep bg signal constant? **this does not work well**. in fact, background is almost constant 
	 */
	public static void correctBG2(NucLineage lin, String expName, SortedSet<EvDecimal> frames, SortedMap<EvDecimal, Double> bgsignal)
		{
		
		double lastBg=bgsignal.get(bgsignal.firstKey());
		for(EvDecimal frame:frames)
			{
			double nextBg=bgsignal.get(frame);
			double diff=nextBg-lastBg;
			
			for(NucLineage.Nuc nuc:lin.nuc.values())
				{
				NucExp nexp=nuc.exp.get(expName);
				if(nexp!=null)
					{
					Double level=nexp.level.get(frame);
					if(level!=null)
						nexp.level.put(frame,level-diff);
					}
				}
			
			lastBg=nextBg;
			}
		
		}

	
	
	/**
	 * Correct for background etc. Signal has already been divided by exposure time.
	 * In addition, make it fit together by forcing average of last frame to be the average of this frame.
	 */
	public static void correctExposureChange(Imageset imset, NucLineage lin, String expName, SortedSet<EvDecimal> frames)
		{
		//Initital correction=none
		double correction=0;
		
		//For all frames
		Double lastExposure=null;
		EvDecimal lastFrame=null;
		for(EvDecimal frame:frames)
			{
			String sExpTime=imset.metaFrame.get(frame).get("exposuretime");
			double expTime=1;
			if(sExpTime!=null)
				expTime=Double.parseDouble(sExpTime);
			
			//Trigger correction
			if(lastExposure!=null && !lastExposure.equals(expTime))
				{
				Double lastAv=helperGetAverageForFrame(lin, expName, lastFrame);
				Double curAv=helperGetAverageForFrame(lin, expName, frame);
				//Note that the last frame has been corrected so to make = we restore the last average
				if(lastAv!=null && curAv!=null)
					{
					correction=curAv-(lastAv+correction);
					System.out.println("------------Frame "+frame+"    Correction "+correction+"     "+lastAv+" "+curAv);
					}
				else
					System.out.println("------------Frame "+frame+"    skipping correction, no samples");
				}
			//else
				//System.out.println("No corr change "+frame);
			lastFrame=frame;
			lastExposure=expTime;
			
			//Correct this frame. Also get 
			for(NucLineage.Nuc nuc:lin.nuc.values())
				{
				NucExp nexp=nuc.exp.get(expName);
				if(nexp!=null)
					{
					Double level=nexp.level.get(frame);
					if(level!=null)
						nexp.level.put(frame,level-correction);
					}
				}
			}
		}
	private static Double helperGetAverageForFrame(NucLineage lin, String expName, EvDecimal frame)
		{
		int count=0;
		double lastAverage=0;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				{
				Double level=nexp.level.get(frame);
				if(level!=null)
					{
					lastAverage+=level;
					count++;
					}
				}
			}
		if(count!=0)
			return lastAverage/count;
		else
			return null;
		}
	
	
	
	
	
	public static void clearExp(NucLineage lin, String expName)
		{
		for(NucLineage.Nuc nuc:lin.nuc.values())
			nuc.exp.remove(expName);
		}
		
	/**
	 * Normalize expression such that maximum is 1.0
	 */
	public static void normalizeSignal(NucLineage lin, String expName)
		{
		Double max=null;
		double normalizedVal=100;
		
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				for(double level:nexp.level.values())
					if(max==null || level>max)
						max=level;
			}

		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				{
				nexp.unit=null;
				HashMap<EvDecimal, Double> newlevel=new HashMap<EvDecimal, Double>();
				for(Map.Entry<EvDecimal, Double> e:nexp.level.entrySet())
					newlevel.put(e.getKey(),normalizedVal*e.getValue()/max);
				nexp.level.clear();
				nexp.level.putAll(newlevel);
				}
			}

		}
	
	/**
	 * Compress signal destructively by only keeping levels where the change is great
	 * 
	 * Integral should be kept the same: 
	 * base functions phi_i(x), node values u_i, coordinates(frames) x_i, expression f(x)
	 * Condition: int_{x_i}^{x_{i+1}} dx sum u_i phi_i(x) = int_{x_i}^{x_{i+1}} dx f(x) 
	 * ...=(u_{i+1}+u_i)(x_{i+1}-x_i)/2
	 * 
	 * Num unknowns=num frames
	 * Num equations=num frames-1
	 * Add one constraint: starting value has to be the safe, u_0=f(x_0)
	 * 
	 * Matrix best solved directly without forming it.
	 * algorithm:
	 * set u_0
	 * until have all u_i, i>=0:
	 *   Solve for u_{i+1}: (u_{i+1}+u_i)(x_{i+1}-x_i)/2 = int_{x_i}^{x_{i+1}} dx f(x)
	 *     => u_{i+1} = 2 int_{x_i}^{x_{i+1}} dx f(x)/(x_{i+1}-x_i) - u_i
	 * 
	 */
	public static void compressSignal(NucLineage lin, String expName)
		{
		
		}
	
	
	}
