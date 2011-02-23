/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.integrate;

import java.io.File;
import java.text.NumberFormat;
import java.util.*;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import util2.paperCeExpression.compare.CompareAll;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;

import endrov.coordinateSystem.CoordinateSystem;
import endrov.data.EvData;
import endrov.frameTime.FrameTime;
import endrov.imageset.Imageset;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
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
	public static TreeMap<EvDecimal, Tuple<Double,Double>> calculateCorrectExposureChange20100709(Imageset imset, NucLineage lin, String expName, String channelName, SortedSet<EvDecimal> frames, Map<EvDecimal, Double> bg)
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
		
		int framecount=0;

		//Check if exposure time exists for any frames. Then jumps can be assumed to only occur during exposure time changes
		boolean detectByJumps=true;
		for(EvDecimal frame:frames)
			if(imset.getChannel(channelName).getMetaFrame(frame).containsKey("exposuretime") ||
					imset.getMetaFrame(frame).containsKey("exposuretime"))
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
				framecount++;
				double expTime=1;
				String sExpTime=imset.getChannel(channelName).getMetaFrame(frame).get("exposuretime");
				if(sExpTime==null)
					sExpTime=imset.getMetaFrame(frame).get("exposuretime");
				if(sExpTime!=null)
					expTime=Double.parseDouble(sExpTime);
				
				/*
				//Trigger correction based on sudden jumps
				boolean suddenJump=false;
				if(newKM!=null)
					{
					double jmp=Math.abs(newKM.snd()-correctionM);
					if(jmp>0.02) //Better constant? Input is normalized, might work. Could use percentile stats 
						{
						System.out.println("Detected sudden jump: "+jmp);
						suddenJump=true;
						}
					}
				*/
				
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
					
					
					//might crash!
					/*
					System.out.println("         check1 "+(correctionK*bg1+correctionM)+"      orig value "+bg1);
					System.out.println("         check1 "+(newKM.fst()*bg2+newKM.snd())+"      orig value "+bg2);
					System.out.println("         check2 "+(correctionK*s1+correctionM)+"      orig value "+s1);
					System.out.println("         check2 "+(newKM.fst()*s2+newKM.snd())+"      orig value "+s2);
					*/
					}
	
				
				//newKM=null; //temp: disable correction
				//newKM=Tuple.make(Math.random(), 0.0);//temp: check that correction is applied
				
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
	 * Correct for background etc. 
	 * Return table of correction values so it can be applied again. Will be used for non-AP
	 */
	/*
	public static TreeMap<EvDecimal, Tuple<Double,Double>> correctExposureChangeOldPre20100709(Imageset imset, NucLineage lin, String expName, String channelName, SortedSet<EvDecimal> frames)
		{
		//Initital correction=none
		double correctionK=1;
		double correctionM=0;
		
		TreeMap<EvDecimal, Tuple<Double,Double>> historyKM=new TreeMap<EvDecimal, Tuple<Double,Double>>();
		Nuc corrNuc=lin.getCreateNuc("correctExp");
		
		int framecount=0;
		
		
		//////////////////////////TODO any exposure times given at all?
		
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
			else
				{
				System.out.println("!!!!!!!!!! no exposure time for frame "+frame);
				}

//			Double lastAv=helperGetAverageForFrame(lin, expName, lastFrame);
//			Double curAv=helperGetAverageForFrame(lin, expName, frame);

	*/
			//Values to piece together. Model a*x+b
			/*
			List<Double> lastFit=helperGetAverageForFrameNew(lin, expName, lastFrame);
			List<Double> curFit=helperGetAverageForFrameNew(lin, expName, frame);
			Tuple<Double,Double> newKM=EvMathUtil.fitLinear1D(lastFit, curFit);
			*/
		/*	
			//Values to piece together. Model x+b
			Tuple<Double,Double> newKM=new Tuple<Double, Double>(
					1.0,
					helperGetAverageForFrame(lin, expName, lastFrame)-helperGetAverageForFrame(lin, expName, frame));
			
			//Trigger correction based on sudden jumps
			boolean suddenJump=false;
			if(newKM!=null)
				{
				double jmp=Math.abs(newKM.snd()-correctionM);
				if(jmp>0.02) //Better constant? Input is normalized, might work. Could use percentile stats 
					{
					System.out.println("Detected sudden jump: "+jmp);
					suddenJump=true;
					//lastExposure=-1.0;
					}
				}
			
			
			/////////////// for now do not try to detect sudden jumps!!!! ///////////////
			suddenJump=false;
			
			//Trigger correction
			if(!lastExposure.equals(expTime) || suddenJump)
				{
				corrNuc.getCreatePos(frame);
				
				//NOTE! last frame has already been corrected. Hence correction does not depend on last correction!
//				Tuple<Double,Double> newKM=EvMathUtil.fitLinear1D(lastFit, curFit);
				if(newKM!=null)
					{
					correctionK=newKM.fst();
					correctionM=newKM.snd();
					System.out.println("------------Frame "+frame+"    Correction "+correctionK+"  "+correctionM);
					}
				else
					System.out.println("------------Frame "+frame+"    no corr");
				}
			//else
				//System.out.println("No corr change "+frame);
			lastFrame=frame;
			lastExposure=expTime;
			
			historyKM.put(frame,new Tuple<Double, Double>(correctionK,correctionM));
			
			//Correct this frame
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
		}*/
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
	 * Correct for background etc. Use given table of corrections
	 */
	public static void correctExposureChange(TreeMap<EvDecimal, Tuple<Double,Double>> corrections, NucLineage lin, String expName)
		{
		//For all frames
		for(EvDecimal frame:corrections.keySet())
			{
			double correctionK=corrections.get(frame).fst();
			double correctionM=corrections.get(frame).snd();
			
			//Correct this frame
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
	
	/**
	 * Correct for background etc. Signal has already been divided by exposure time.
	 * In addition, make it fit together by forcing average of last frame to be the average of this frame.
	 */
	/*
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
		
		*/
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
//		Double max=getSignalMax(lin, expName);
		double scale=normalizedVal/(max-min);
		
		for(int i=0;i<sig.length;i++)
			sig[i]=(sig[i]-min)*scale;
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



	public static Vector3d getLastPosABp(NucLineage refLin)
		{
		NucLineage.Nuc nucABp = refLin.nuc.get("ABp");
		if (nucABp!=null && !nucABp.pos.isEmpty())
			{
			Vector3d posABp = nucABp.pos.get(nucABp.pos.lastKey()).getPosCopy();
			return posABp;
			}
		else 
			{
			NucLineage.Nuc nucABpl = refLin.nuc.get("ABpl");
			NucLineage.Nuc nucABpr = refLin.nuc.get("ABpr");
			if (nucABpl!=null && nucABpr!=null  && !nucABpl.pos.isEmpty() && !nucABpr.pos.isEmpty())
				{
				Vector3d a = nucABpl.pos.get(nucABpl.pos.lastKey()).getPosCopy();
				Vector3d b = nucABpr.pos.get(nucABpr.pos.lastKey()).getPosCopy();
				a.add(b);
				a.scale(0.5);
				return a;
				}
			else
				return null;
			}
		}
	
	public static Vector3d getLastPosABa(NucLineage refLin)
		{
		NucLineage.Nuc nucABa = refLin.nuc.get("ABa");
		if (nucABa!=null && !nucABa.pos.isEmpty())
			{
			Vector3d posABa = nucABa.pos.get(nucABa.pos.lastKey()).getPosCopy();
			return posABa;
			}
		else 
			{
			NucLineage.Nuc nucABal = refLin.nuc.get("ABal");
			NucLineage.Nuc nucABar = refLin.nuc.get("ABar");
			if (nucABal!=null && nucABar!=null  && !nucABal.pos.isEmpty() && !nucABar.pos.isEmpty())
				{
				Vector3d a = nucABal.pos.get(nucABal.pos.lastKey()).getPosCopy();
				Vector3d b = nucABar.pos.get(nucABar.pos.lastKey()).getPosCopy();
				a.add(b);
				a.scale(0.5);
				return a;
				}
			else
				return null;
			}
		}



	/**
	 * Super-impose standard model onto recording by rotation, scaling and changing times 
	 */
	public static NucLineage mapModelToRec(NucLineage reflin)
		{
		EvData stdcelegans=EvData.loadFile(new File("/Volumes/TBU_main06/ost4dgood/celegans2008.2.ost"));
		NucLineage stdlin=stdcelegans.getIdObjectsRecursive(NucLineage.class).values().iterator().next();

		//Time normalization
		//FrameTime ftRef=CompareAll.buildFrametime(IntExp.refLin);
		FrameTime ftRef=CompareAll.buildFrametime(reflin);
		FrameTime ftStd=CompareAll.buildFrametime(stdlin);
		
		//Coordinate transform
		CoordinateSystem csStd=ExpUtil.singlecellCSfromLin(stdlin);
		CoordinateSystem csRef=ExpUtil.singlecellCSfromLin(reflin);
		Matrix4d mStd=csStd.getTransformToSystem();   //This should put std into the origo, more or less
		Matrix4d mRef=csRef.getTransformToWorld();    //The inverse of putting ref in the origo
//		Matrix4d mStd=csStd.getTransformToWorld();
//		Matrix4d mRef=csRef.getTransformToSystem();
		Matrix4d transform=new Matrix4d();
		transform.mul(mRef, mStd);
		
		//// temp
//		transform.set(mStd);
		
		//// temp
		
		//For all nuclei
		for(NucLineage.Nuc n:stdlin.nuc.values())
			{
			Map<EvDecimal,NucLineage.NucPos> newpos=new HashMap<EvDecimal, NucLineage.NucPos>();
			for(Map.Entry<EvDecimal, NucLineage.NucPos> e:new HashMap<EvDecimal, NucLineage.NucPos>(n.pos).entrySet())
				{
				NucLineage.NucPos nucpos=e.getValue();
	
				//Normalize coordinate
				Vector4d pos=new Vector4d(nucpos.x,nucpos.y,nucpos.z,1);
				transform.transform(pos);
				nucpos.x=pos.x;
				nucpos.y=pos.y;
				nucpos.z=pos.z;
	
				//Normalize time
				EvDecimal oldframe=e.getKey();
				EvDecimal newframe=ftRef.mapTime2Frame(ftStd.mapFrame2Time(oldframe));	
				newpos.put(newframe,nucpos);
				}
			n.pos.clear();
			n.pos.putAll(newpos);
			}
	
		//Remove other "expression patterns" from model
		for(NucLineage.Nuc n:stdlin.nuc.values())
			n.exp.clear();
		
		return stdlin;
		}



	/**
	 * Set up coordinate system
	 */
	public static CoordinateSystem singlecellCSfromLin(NucLineage refLin)
		{
		NucLineage.Nuc nucP2 = refLin.nuc.get("P2'");
		NucLineage.Nuc nucEMS = refLin.nuc.get("EMS");
	
		Vector3d posABp = getLastPosABp(refLin);
		Vector3d posABa = getLastPosABa(refLin);
	
		if (nucP2==null||posABa==null||posABp==null||nucEMS==null
				||nucP2.pos.isEmpty()
				||nucEMS.pos.isEmpty())
			{
			System.out.println("Does not have enough cells marked, no single-cell");
			return null;
			}
	
		Vector3d posP2 = nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy();
		Vector3d posEMS = nucEMS.pos.get(nucEMS.pos.lastKey()).getPosCopy();
	
		Vector3d v1 = new Vector3d();
		Vector3d v2 = new Vector3d();
		v1.sub(posABa, posP2);
		v2.sub(posEMS, posABp);
	
		// By using all 4 cells for mid it should be less sensitive to
		// abberrations
		Vector3d mid = new Vector3d();
		mid.add(posABa);
		mid.add(posABp);
		mid.add(posEMS);
		mid.add(posP2);
		mid.scale(0.25);
	
		// Create coordinate system. Enlarge by 20%
		CoordinateSystem cs = new CoordinateSystem();
		double scale = 1.35;
		cs.setFromTwoVectors(v1, v2, v1.length()*scale, v2.length()*scale, v2.length()*scale, mid);
	
		return cs;
		}
	
	
	}
