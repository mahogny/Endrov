package util2.integrateExpression;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;

public class ExpUtil
	{

	
	/**
	 * Correct for background etc. Signal has already been divided by exposure time. Need to shift up/down whenever there is a jump.
	 * TODO Try to keep bg signal constant? 
	 */
	public static void correctBG(NucLineage lin, String expName, SortedSet<EvDecimal> frames, SortedMap<EvDecimal, Double> bgsignal)
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
		
		/*
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				{
				nexp.level.get(key)
				}
			}
		lin.nuc.
		
		*/
		
		
		}
	
	/**
	 * Normalize expression such that maximum is 1.0
	 */
	public static void normalizeSignal(NucLineage lin, String expName)
		{
		Double max=null;
		
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
				HashMap<EvDecimal, Double> newlevel=new HashMap<EvDecimal, Double>();
				for(Map.Entry<EvDecimal, Double> e:nexp.level.entrySet())
					newlevel.put(e.getKey(),e.getValue()/max);
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
