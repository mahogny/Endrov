/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeLineage.expression;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Vector3d;

import cern.colt.list.tint.IntArrayList;

import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.typeLineage.Lineage;
import endrov.typeLineage.LineageExp;
import endrov.typeLineage.expression.IntegrateExp.Integrator;
import endrov.typeShell.Shell;
import endrov.util.collection.EvListUtil;
import endrov.util.collection.Tuple;
import endrov.util.io.EvFileUtil;
import endrov.util.math.EvDecimal;
import endrov.util.math.EvMathUtil;
import endrov.util.math.ImVector2d;
import endrov.util.math.ImVector3d;

/**
 * Integrate expression along AP-axis
 * @author Johan Henriksson
 *
 */
public abstract class IntegratorSlice implements Integrator
	{
	private int numSubDiv;
	private HashMap<EvDecimal, EvPixels> distanceMap = new HashMap<EvDecimal, EvPixels>();
	protected Shell shell;
	private double[] sliceExp; //Must be double for values to fit
	private int[] sliceVol;
	private Lineage lin = new Lineage();
	//private String newLinName;
	
	public Map<EvDecimal, Double> bg;
	public TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure;
	private boolean updateBG; // variable if to calculate bg
	
//	private double curBgInt = 0;
//	private int curBgVol = 0;
	
	//List<Integer> curBg=new ArrayList<Integer>();
	private IntArrayList curBgOutside=new IntArrayList(); 
	private IntArrayList curBgInside=new IntArrayList();
	
	
	
	public IntegratorSlice(IntegrateExp integrator, /*String newLinName,*/ int numSubDiv, Map<EvDecimal, Double> bg)
		{
		this.numSubDiv = numSubDiv;
//		this.newLinName = newLinName;
		
		//Use pre-calculated value for BG
		if (bg!=null)
			{
			this.bg = Collections.unmodifiableMap(bg);   //bg;
			updateBG = false;
			}
		else
			{
			this.bg = new HashMap<EvDecimal, Double>();
			updateBG = true;
			}
	
		// TODO need to group lineage and shell. introduce a new object?
//		integrator.imset.metaObject.put(newLinName, lin);
		// imset.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
		
		Collection<Shell> shells=integrator.imset.getIdObjectsRecursive(Shell.class).values();
		if(shells.isEmpty())
			throw new RuntimeException("No shell found, objects: "+integrator.imset.metaObject.keySet());
		shell = shells.iterator().next();
	
		// Virtual nuc for AP
		for (int i = 0; i<numSubDiv; i++)
			lin.getCreateParticle("_slice"+i);
		}
	
	public void integrateStackStart(IntegrateExp integrator)
		{
		curBgInside.clear();
		curBgOutside.clear();
		
//		curBgInt = 0;
//		curBgVol = 0;
		// Zero out arrays
		sliceExp = new double[numSubDiv];
		sliceVol = new int[numSubDiv];
		}
	
	
	//Normalized with inverse length of axis 
	public abstract ImVector3d getDirVec();
	
	public ImVector3d getMidPos()
		{
		return new ImVector3d(shell.midx, shell.midy, shell.midz); 
		}
	
	
	public void integrateImage(IntegrateExp integrator)
		{
		integrator.ensureImageLoaded();
		
		// Calculate distance mask lazily. Assumes shell does not move over time.
		double[] lenMapArr;
		if (distanceMap.containsKey(integrator.curZint))
			{
			EvPixels lenMap = distanceMap.get(integrator.curZint);
			lenMapArr = lenMap.getArrayDouble();
			}
		else
			{
			EvPixels lenMap = new EvPixels(EvPixelsType.DOUBLE, integrator.pixels.getWidth(), integrator.pixels.getHeight());
			lenMapArr = lenMap.getArrayDouble();
	
			ImVector3d dirvec = getDirVec();
			ImVector3d midpos3 = getMidPos();
			ImVector2d shellPos2=new ImVector2d(shell.midx, shell.midy);
			double invShellMajor2=1.0/(shell.major*shell.major);
			double invShellMinor2=1.0/(shell.minor*shell.minor);
			//double curZ=integrator.curZ.doubleValue();
			
			// Calculate distances
			for (int ay = 0; ay<integrator.pixels.getHeight(); ay++)
				{
				int lineIndex = lenMap.getRowIndex(ay);
				for (int ax = 0; ax<integrator.pixels.getWidth(); ax++)
					{
					//double worldAX=integrator.stack.transformImageWorldX(ax);
					//double worldAY=integrator.stack.transformImageWorldY(ay);
					
					Vector3d pos2prim = integrator.stack.transformImageWorld(new Vector3d(ax,ay,integrator.curZint));
					final ImVector2d pos2 = new ImVector2d(pos2prim.x,pos2prim.y);//new ImVector2(worldAX,worldAY);
					
					//final ImVector2 pos2 = new ImVector2(worldAX,worldAY);
					final ImVector3d pos3 = new ImVector3d(pos2prim.x,pos2prim.y,pos2prim.z);
//					final ImVector3d pos3 = new ImVector3d(pos2.x,pos2.y,curZ);
	
					// Check if this is within ellipse boundary
					final ImVector2d elip = pos2.sub(shellPos2).rotate(shell.angle); // TODO angle? what?
					double len;
					if (1>=elip.y*elip.y*invShellMinor2+elip.x*elip.x*invShellMajor2)
						{
						// xy . dirvecx = cos(alpha) ||xy|| ||dirvecx||
						len = pos3.sub(midpos3).dot(dirvec)+0.5;   
						//goes from -0.5 to 0.5 before addition if using proper ellipse. projection makes it wrong!!!
						}
					else
						len = -1;
					lenMapArr[lineIndex+ax] = len;
					}
				}
			}
	
		// Integrate area, separate into slices and background
		//TODO: have we really really checked that this is done properly?
		for (int y = 0; y<integrator.pixels.getHeight(); y++)
			{
			int lineIndex = integrator.pixels.getRowIndex(y);
			for (int x = 0; x<integrator.pixels.getWidth(); x++)
				{
				int i = lineIndex+x;
				double len = lenMapArr[i];
				int sliceNum = (int) (len*numSubDiv); // may need to bound in addition
				int thisPixelValue=integrator.pixelsLine[i];
				if(sliceNum>=0 && sliceNum<sliceExp.length)
					{
					sliceExp[sliceNum] += thisPixelValue;
					sliceVol[sliceNum]++;

					curBgInside.add(thisPixelValue);
					}
				else if(len==-1) //so things close the embryo will not be considered - likely too bright
					{
					// Measure background. It's all the pixels outside the embryo
					curBgOutside.add(thisPixelValue);
//					curBgInt += thisPixelValue;
//					curBgVol++;
					}
				}
			}
		}
	
	/**
	 * Stable median calculation. 
	 */
	public double calcStableMedian(double lowerFrac, double upperFrac, IntArrayList list)
		{
/*
		list.trimToSize();

		double lowerValue=EvListUtil.findPercentileInt(list.elements(), lowerFrac);
		double upperValue=EvListUtil.findPercentileInt(list.elements(), upperFrac);
		
		problem: multiples of a value. thus the mean will be skewed in direction of border with the most repeats!
		*/
		
		
		
		 // below is the original. it Relies on bucket-sorting. It should be perfectly possible to write a linear-time stable median
		 // based on the normal linear-time median algorithm. This is needed to handle non-8bit images.
		 // Value might be off a bit but not much (need to think of indexing), and it doesn't matter for this application

		int numbins=66000;
		int[] elem=list.elements();
		int numElem=list.size(); 
		 
		//Calculate histogram
		int[] histogram=new int[numbins];
		//int[] elem=curBgOutside.elements();
		//int numElem=curBgOutside.size();
		for(int i=0;i<numElem;i++)
			histogram[elem[i]]++;
		
		int jumpElem=0;
		int lowerIndex=(int)(numElem*lowerFrac);
		int upperIndex=(int)(numElem*upperFrac);
		int sum=0;
		int cnt=0;
		for(int i=0;i<numbins;i++)
			{
			int thisNum=histogram[i];
			int take=Math.min(upperIndex,jumpElem+thisNum)-Math.max(lowerIndex, jumpElem);
			if(take>0)
				{
				sum+=take*i;
				cnt+=take;
				}
			jumpElem+=thisNum;
			}
		return (double)sum/cnt;
		
		}
	
	public void integrateStackDone(IntegrateExp integrator)
		{
		// Store background if this integrator is responsible for calculating it
		if (updateBG)
			{
			double stableMedianOutside=calcStableMedian(0.4,0.6, curBgOutside);
			double stableMedianInside=calcStableMedian(0.4,0.6, curBgInside);
			/*
			int medianOutside=EvListUtil.findPercentileInt(curBgOutside.elements(), 0.5, curBgOutside.size());
			int medianInside=EvListUtil.findPercentileInt(curBgInside.elements(), 0.5, curBgInside.size());
			double avg=(double)curBgInt/curBgVol;
			
			double thisBG=EvMathUtil.minAll(medianOutside,medianInside,avg);
			*/
			
			double thisBG=Math.min(stableMedianOutside, stableMedianInside);
			
			//int thisBG=EvMathUtil.minAll(medianOutside,medianInside);   //average decreased value in a bad value for one recording!!! I think
			bg.put(integrator.frame, thisBG);
			//bg.put(integrator.frame, (double)median/curBg.size());
			//bg.put(integrator.frame, curBgInt/curBgVol);
			}
	
		// Store pattern in lineage
		for (int i = 0; i<numSubDiv; i++)
			{
			//double avg = (double) sliceExp[i]/(double) sliceVol[i];
			double avg = (double) sliceExp[i]/(double) sliceVol[i] - bg.get(integrator.frame);
		//	avg /= integrator.expTime;
	
			Lineage.Particle nuc = lin.getCreateParticle("_slice"+i);
			LineageExp exp = nuc.getCreateExp(integrator.expName);
			exp.level.put(integrator.frame, avg);
			
			if(Double.isInfinite(avg) || Double.isNaN(avg))
				System.out.println("Slice inf or nan, frame: "+integrator.frame+"    "+getClass().getSimpleName()+"   vol: "+sliceVol[i]+"    bg: "+bg.get(integrator.frame)+"    slice: "+i);
//			System.out.println(" "+avg);
			}
//		System.out.println();
	
		}
	
	public Lineage done(IntegrateExp integrator, TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
		{
		// Set override start and end times
		for (int i = 0; i<numSubDiv; i++)
			{
			Lineage.Particle nuc = lin.getCreateParticle("_slice"+i);
			nuc.overrideStart = integrator.ch.getFirstFrame();
			nuc.overrideEnd = integrator.ch.getLastFrame();
			}
		
		//For AP: calculate how to correct exposure
		if (correctedExposure==null)
			correctedExposure = ExpUtil.calculateCorrectExposureChange20100709(
					integrator.imset, integrator.ch, lin, integrator.expName, new TreeSet<EvDecimal>(integrator.ch.getFrames()), bg);
		this.correctedExposure=correctedExposure;

		//Correct for exposure changes
		ExpUtil.correctExposureChange(this.correctedExposure, lin,	integrator.expName);

		
		/*
		System.out.println();
		System.out.println("After exp correct: "+lin.getCreateNuc("_slice0").exp.get(integrator.expName).level);
*/
	
		// This is only for the eye
		double sigMax = ExpUtil.getSignalMax(lin, integrator.expName);
		double sigMin = ExpUtil.getSignalMin(lin, integrator.expName);
	//	System.out.println("-----------old signal min: "+numSubDiv+"   "+ExpUtil.getSignalMin(lin, integrator.expName));
		ExpUtil.normalizeSignal(lin, integrator.expName, sigMax, sigMin, 1);
	//	System.out.println("-----------new signal min: "+numSubDiv+"   "+ExpUtil.getSignalMin(lin, integrator.expName));
	
		
		/*
		System.out.println();
		System.out.println("final exp: "+lin.getCreateNuc("_slice0").exp.get(integrator.expName).level.values());
*/
		return lin;
		}
	
	/**
	 * Store profile as array on disk
	 */
	public void profileForGnuplot(IntegrateExp integrator, File file)
		{
		//Imageset imset = integrator.imset;//data.getObjects(Imageset.class).get(0);
		//EvChannel ch = integrator.ch;//imset.getChannel(integrator.channelName);
		//NucLineage lin = (NucLineage) imset.metaObject.get(newLinName);
		try
			{
			StringBuffer outf = new StringBuffer();
	
			here: for (EvDecimal frame : integrator.ch.getFrames())
				{
				outf.append(""+frame+"\t");
				for (int i = 0; i<numSubDiv; i++)
					{
					Lineage.Particle nuc = lin.particle.get("_slice"+i);
					LineageExp nexp = nuc.exp.get(integrator.expName);
					Double level = nexp.level.get(frame);
					if (level==null)
						continue here;
					outf.append(level);
					outf.append("\t");
					}
				outf.append("\n");
				}
			EvFileUtil.writeFile(file, outf.toString());
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	}
