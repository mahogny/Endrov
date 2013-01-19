/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineage.expression;

import java.text.NumberFormat;
import java.util.*;



import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;

import endrov.data.EvContainer;
import endrov.imageset.EvChannel;
import endrov.lineage.Lineage;
import endrov.lineage.LineageExp;
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
	public static void correctBG2(Lineage lin, String expName, SortedSet<EvDecimal> frames, SortedMap<EvDecimal, Double> bgsignal)
		{
		
		double lastBg=bgsignal.get(bgsignal.firstKey());
		for(EvDecimal frame:frames)
			{
			double nextBg=bgsignal.get(frame);
			double diff=nextBg-lastBg;
			
			for(Lineage.Particle nuc:lin.particle.values())
				{
				LineageExp nexp=nuc.exp.get(expName);
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
	public static TreeMap<EvDecimal, Tuple<Double,Double>> calculateCorrectExposureChange20100709(EvContainer imset, EvChannel ch, Lineage lin, String expName, /*String channelName, */SortedSet<EvDecimal> frames, Map<EvDecimal, Double> bg)
		{
		Double minBG=null;
		for(double d:bg.values())
			if(minBG==null || d<minBG)
				minBG=d;

		//Initial correction: only subtract background. If signal is normalized then no subtraction is really needed
		double correctionK=1;
		double correctionM=0;//-minBG;
		
		TreeMap<EvDecimal, Tuple<Double,Double>> historyKM=new TreeMap<EvDecimal, Tuple<Double,Double>>();
		//Nuc corrNuc=lin.getCreateNuc("correctExp");
		
//		int framecount=0;

		//Check if exposure time exists for any frames. Then jumps can be assumed to only occur during exposure time changes
		boolean detectByJumps=true;
		for(EvDecimal frame:frames)
			if(ch.getMetaFrame(frame).containsKey("exposuretime")/* ||
					imset.getMetaFrame(frame).containsKey("exposuretime")*/)
				{
				detectByJumps=false;
				break;
				}
		if(detectByJumps)
			System.out.println("Exposure time does not exist - enabling jump detection");
		
		//For all frames
		Double lastExposure=0.0;
		EvDecimal lastFrame=frames.first();
		for(EvDecimal frame:frames)
			if(bg.containsKey(frame))
				{
//				framecount++;
				double expTime=1;
				String sExpTime=ch.getMetaFrame(frame).get("exposuretime");/*
				if(sExpTime==null)
					sExpTime=imset.getMetaFrame(frame).get("exposuretime");*/
				if(sExpTime!=null)
					expTime=Double.parseDouble(sExpTime);
				
				
				Tuple<Double,Double> newKM=null;   
				
				/////////////// for now do not try to detect sudden jumps!!!! ///////////////
				boolean suddenJump=false;
				
				//Calculate new correction if needed
				if(!lastExposure.equals(expTime) || suddenJump)
					{
					/**
					 * Model of correction: outsig=sig*k+m
					 * Let ' be next time, then demand:
					 * outsig=outsig'
					 * bg=bg'
					 */
					double bg1=bg.get(lastFrame);
					double bg2=bg.get(frame);
					
					bg1=bg2=0; //temp. the right thing?
					
					double s1=helperGetAverageForFrame(lin, expName, lastFrame);
					double s2=helperGetAverageForFrame(lin, expName, frame);
					double k1=correctionK;
					double m1=correctionM;
	
					//Solve as Ax=b. If it is singular, keep old correction. Singularity does not occur easily though.
					DoubleMatrix2D A=new DenseDoubleMatrix2D(new double[][]{
						{bg2,1},
						{s2, 1}
					});
					DoubleMatrix2D b=new DenseDoubleMatrix2D(new double[][]{
						{bg1*k1+m1},
						{ s1*k1+m1}
					});
					DoubleAlgebra alg=new DoubleAlgebra();
					DoubleMatrix2D x=alg.solve(A, b);
					
					//Only accept new values if they appear to be sane
					newKM=Tuple.make(x.getQuick(0, 0), x.getQuick(0, 1));
					//Tuple<Double,Double> km=Tuple.make(x.getQuick(0, 0), x.getQuick(0, 1));
					if(alg.det(A)!=0 
							&& !Double.isNaN(newKM.fst()) && !Double.isInfinite(newKM.fst())
							&& !Double.isNaN(newKM.snd()) && !Double.isInfinite(newKM.snd())
							&& newKM.fst()>0)
						;
					else
						{
						System.out.println("BAAAAAAAAAAAAAAAD KM: "+newKM);
						newKM=null;
						}
					
					
					}
	
				
				if(newKM!=null)
					{
					correctionK=newKM.fst();
					correctionM=newKM.snd();
					System.out.println("------------Frame "+frame+"    Correction "+correctionK+"  "+correctionM);
					}
				else
					System.out.println("------------Frame "+frame+"    no corr");
				System.out.println("                                                                               BG now "+(correctionK*bg.get(frame)+correctionM)+"      orig value "+bg.get(frame));
				
				lastFrame=frame;
				lastExposure=expTime;
				
				historyKM.put(frame,new Tuple<Double, Double>(correctionK,correctionM));
				}
		return historyKM;
		}
	

	
	
	/**
	 * Correct for background etc. Use given table of corrections
	 */
	public static void correctExposureChange(TreeMap<EvDecimal, Tuple<Double,Double>> corrections, Lineage lin, String expName)
		{
		//For all frames
		for(EvDecimal frame:corrections.keySet())
			{
			double correctionK=corrections.get(frame).fst();
			double correctionM=corrections.get(frame).snd();
			
			//Correct this frame
			for(Lineage.Particle nuc:lin.particle.values())
				{
				LineageExp nexp=nuc.exp.get(expName);
				if(nexp!=null)
					{
					Double level=nexp.level.get(frame);
					if(level!=null)
						nexp.level.put(frame,correctionK*level+correctionM);
					}
				}
			}
		}
	
	/**
	 * Correct for background etc. Use given table of corrections
	 */
	public static void correctExposureChange(TreeMap<EvDecimal, Tuple<Double,Double>> corrections, EvDecimal frame, double[] sig)
		{
		double correctionK=corrections.get(frame).fst();
		double correctionM=corrections.get(frame).snd();
		
		for(int i=0;i<sig.length;i++)
			sig[i]=correctionK*sig[i]+correctionM;
		}
	
	
	

	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	
	private static Double helperGetAverageForFrame(Lineage lin, String expName, EvDecimal frame)
		{
		int count=0;
		double sum=0;
		for(Lineage.Particle nuc:lin.particle.values())
			{
			LineageExp nexp=nuc.exp.get(expName);
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
	
	
	
	
	
	public static void clearExp(Lineage lin, String expName)
		{
		for(Lineage.Particle nuc:lin.particle.values())
			nuc.exp.remove(expName);
		}
		
	/**
	 * Normalize expression such that maximum is 1.0
	 */
	public static void normalizeSignal(Lineage lin, String expName, double max, double min, double normalizedVal)
		{
		double scale=normalizedVal/(max-min);
		
		for(Lineage.Particle nuc:lin.particle.values())
			{
			LineageExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				{
				nexp.unit=null;
				Map<EvDecimal, Double> newlevel=new HashMap<EvDecimal, Double>();
				for(Map.Entry<EvDecimal, Double> e:nexp.level.entrySet())
					newlevel.put(e.getKey(),(e.getValue()-min)*scale);
				nexp.level.clear();
				nexp.level.putAll(newlevel);
				}
			}

		}
	
	public static void normalizeSignal(double[] sig, double max, double min, double normalizedVal)
		{
		double scale=normalizedVal/(max-min);
		
		for(int i=0;i<sig.length;i++)
			sig[i]=(sig[i]-min)*scale;
		}
	
	/**
	 * Get maximum value for expression
	 */
	public static Double getSignalMax(Lineage lin, String expName)
		{
		Double max=null;
		for(Lineage.Particle nuc:lin.particle.values())
			{
			LineageExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				for(double level:nexp.level.values())
					if(max==null || level>max)
						max=level;
			}
		return max;
		}
	
	/**
	 * Get maximum value for expression
	 */
	public static Double getSignalMax(Collection<double[][][]> sig)
		{
		Double ret=null;
		for(double[][][] v:sig)
			for(double[][] vv:v)
				for(double[] vvv:vv)
					for(double level:vvv)
						if(ret==null || level>ret)
							ret=level;
		return ret;
		}
	
	/**
	 * Get minimum value for expression
	 */
	public static Double getSignalMin(Lineage lin, String expName)
		{
		Double min=null;
		for(Lineage.Particle nuc:lin.particle.values())
			{
			LineageExp nexp=nuc.exp.get(expName);
			if(nexp!=null)
				for(double level:nexp.level.values())
					if(min==null || level<min)
						min=level;
			}
		return min;
		}

	/**
	 * Get minimum value for expression
	 */
	public static Double getSignalMin(Collection<double[][][]> sig)
		{
		Double ret=null;
		for(double[][][] v:sig)
			for(double[][] vv:v)
				for(double[] vvv:vv)
					for(double level:vvv)
						if(ret==null || level<ret)
							ret=level;
		return ret;
		}



	public static Tuple<String, String> nameDateFromOSTName(String n)
		{
		//String orig=n;
		n=n.substring(0,n.indexOf(".ost"));
		
		int u1=n.indexOf('_');
		String strainName;
		if(u1==-1)
			{
			strainName=n;
			n="";
			}
		else
			{
			strainName=n.substring(0, u1);
			n=n.substring(u1+1);
			}
		
		int u2=n.indexOf('_');
		String date;
		if(u2==-1)
			{
			date=n;
			n="";
			}
		else
			{
			date=n.substring(0, u2);
			}
		
		
		if(isNumber(date) && date.length()==6)
			{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMinimumIntegerDigits(2);
			int year=2000+Integer.parseInt(date.substring(0,2));
			int month=Integer.parseInt(date.substring(2,4));
			int day=Integer.parseInt(date.substring(4,6));
		
			date=year+""+nf.format(month)+""+nf.format(day);
			}
		else
			date="20060101";
		
		
		//TODO
		//Fallback: actually name of gene
		
		return new Tuple<String, String>(strainName,date);
		}
	
	

	public static boolean isNumber(String s)
		{
		for(char c:s.toCharArray())
			if(!Character.isDigit(c))
				return false;
		return true;
		}
	
	
	}
