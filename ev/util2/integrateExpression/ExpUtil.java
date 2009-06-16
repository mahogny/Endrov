package util2.integrateExpression;

import java.util.*;

import endrov.imageset.Imageset;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.nuc.NucLineage.Nuc;
import endrov.util.*;

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
	 * Correct for background etc. 
	 * Return table of correction values so it can be applied again. Will be used for non-AP
	 */
	public static TreeMap<EvDecimal, Tuple<Double,Double>> correctExposureChange(Imageset imset, NucLineage lin, String expName, String channelName, SortedSet<EvDecimal> frames)
		{
		//Initital correction=none
		double correctionK=1;
		double correctionM=0;
		
		TreeMap<EvDecimal, Tuple<Double,Double>> historyKM=new TreeMap<EvDecimal, Tuple<Double,Double>>();
		Nuc corrNuc=lin.getNucCreate("correctExp");
		
		int framecount=0;
		
		//For all frames
		Double lastExposure=0.0;
		EvDecimal lastFrame=frames.first();
		for(EvDecimal frame:frames)
			{
			framecount++;
			String sExpTime=imset.getChannel(channelName).metaFrame.get(frame).get("exposuretime");
			double expTime=1;
			if(sExpTime!=null)
				expTime=Double.parseDouble(sExpTime);

//			Double lastAv=helperGetAverageForFrame(lin, expName, lastFrame);
//			Double curAv=helperGetAverageForFrame(lin, expName, frame);

	
			//Values to piece together. Model a*x+b
			/*
			List<Double> lastFit=helperGetAverageForFrameNew(lin, expName, lastFrame);
			List<Double> curFit=helperGetAverageForFrameNew(lin, expName, frame);
			Tuple<Double,Double> newKM=EvMathUtil.fitLinear1D(lastFit, curFit);
			*/
			
			//Values to piece together. Model x+b
			Tuple<Double,Double> newKM=new Tuple<Double, Double>(
					1.0,
					helperGetAverageForFrame(lin, expName, lastFrame)-helperGetAverageForFrame(lin, expName, frame));
			

			
			if(newKM!=null)
				{
				double jmp=Math.abs(newKM.snd()-correctionM);
				if(jmp>0.02) //Better constant! TODO
					{
					System.out.println("Detected sudden jump: "+jmp);
					lastExposure=-1.0;
					}
				}
			
			//Trigger correction
			if(!lastExposure.equals(expTime))
				{
				corrNuc.getPosCreate(frame);
				
				//NOTE! last frame has already been corrected. Hence correction does not depend on last correction!
//				Tuple<Double,Double> newKM=EvMathUtil.fitLinear1D(lastFit, curFit);
				if(newKM!=null)
					{
					correctionK=newKM.fst();
					correctionM=newKM.snd();
					System.out.println("------------Frame "+framecount+"   "+frame+"    Correction "+correctionK+"  "+correctionM);
					}
				else
					System.out.println("------------Frame "+framecount+"    no corr");
				}
			//else
				//System.out.println("No corr change "+frame);
			lastFrame=frame;
			lastExposure=expTime;
			
			historyKM.put(frame,new Tuple<Double, Double>(correctionK,correctionM));
			
			//Correct this frame. Also get 
			for(NucLineage.Nuc nuc:lin.nuc.values())
				{
				NucExp nexp=nuc.exp.get(expName);
				if(nexp!=null)
					{
					Double level=nexp.level.get(frame);
					if(level!=null)
						nexp.level.put(frame,correctionK*level+correctionM);
					}
				}
			}
		return historyKM;
		}
	/*
	private static List<Double> helperGetAverageForFrameNew(NucLineage lin, String expName, EvDecimal frame)
		{
		LinkedList<Double> list=new LinkedList<Double>();
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				{
				Double level=nexp.level.get(frame);
				if(level!=null)
					list.add(level);
				}
			}
		return list;
		}*/
	
	
	
	
	/**
	 * Correct for background etc. Use table of corrections
	 */
	public static void correctExposureChange(TreeMap<EvDecimal, Tuple<Double,Double>> corrections, NucLineage lin, String expName)
		{
		int framecount=0;
		
		//For all frames
		for(EvDecimal frame:corrections.keySet())
			{
			framecount++;

			double correctionK=corrections.get(frame).fst();
			double correctionM=corrections.get(frame).snd();
			
			//Correct this frame. Also get 
			for(NucLineage.Nuc nuc:lin.nuc.values())
				{
				NucExp nexp=nuc.exp.get(expName);
				if(nexp!=null)
					{
					Double level=nexp.level.get(frame);
					if(level!=null)
						nexp.level.put(frame,correctionK*level+correctionM);
					}
				}
			}
		}
	

	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	
	/**
	 * Correct for background etc. Signal has already been divided by exposure time.
	 * In addition, make it fit together by forcing average of last frame to be the average of this frame.
	 */
	public static void correctExposureChangeOld(Imageset imset, NucLineage lin, String expName, SortedSet<EvDecimal> frames)
		{
		//Initital correction=none
		double correction=0;
		
		int framecount=0;
		
		//For all frames
		Double lastExposure=0.0;
		EvDecimal lastFrame=frames.first();
		for(EvDecimal frame:frames)
			{
			framecount++;
			String sExpTime=imset.metaFrame.get(frame).get("exposuretime");
			double expTime=1;
			if(sExpTime!=null)
				expTime=Double.parseDouble(sExpTime);
			
			
			//Trigger correction
			if(!lastExposure.equals(expTime))
				{
				Double lastAv=helperGetAverageForFrame(lin, expName, lastFrame);
				Double curAv=helperGetAverageForFrame(lin, expName, frame);
				if(lastAv!=null && curAv!=null)
					{
					//NOTE! last frame has already been corrected. Hence correction does not depend on last correction!
					correction=curAv-lastAv;
					System.out.println("------------Frame "+frame+"    Correction "+correction+"     "+lastAv+" "+curAv);
					}
				else
					System.out.println("------------Frame "+frame+"    skipping correction, no samples");
				}
			//else
				//System.out.println("No corr change "+frame);
			lastFrame=frame;
			lastExposure=expTime;
			
			double oneav=helperGetAverageForFrame(lin, expName, frame);
			System.out.println(framecount+"   "+oneav+"    "+(oneav-correction));

			
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
		double sum=0;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				{
				Double level=nexp.level.get(frame);
				if(level!=null)
					{
					sum+=level;
					count++;
					}
				}
			}
		if(count!=0)
			return sum/count;
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
	public static void normalizeSignal(NucLineage lin, String expName, double max, double min, double normalizedVal)
		{
//		Double max=getSignalMax(lin, expName);
		double scale=normalizedVal/(max-min);
		
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				{
				nexp.unit=null;
				HashMap<EvDecimal, Double> newlevel=new HashMap<EvDecimal, Double>();
				for(Map.Entry<EvDecimal, Double> e:nexp.level.entrySet())
					newlevel.put(e.getKey(),(e.getValue()-min)*scale);
				nexp.level.clear();
				nexp.level.putAll(newlevel);
				}
			}

		}
	
	/**
	 * Get maximum value for expression
	 */
	public static Double getSignalMax(NucLineage lin, String expName)
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
		return max;
		}
	
	/**
	 * Get minimum value for expression
	 */
	public static Double getSignalMin(NucLineage lin, String expName)
		{
		Double min=null;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			NucExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				for(double level:nexp.level.values())
					if(min==null || level<min)
						min=level;
			}
		return min;
		}
	
	
	}
