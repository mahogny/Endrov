/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.integrate;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Vector2d;

import cern.colt.list.tint.IntArrayList;

import util2.paperCeExpression.integrate.IntExp.Integrator;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.Imageset;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.shell.Shell;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;
import endrov.util.ImVector2;
import endrov.util.Tuple;

/**
 * Integrate expression along AP-axis
 * @author Johan Henriksson
 *
 */
public class OldIntegratorAP implements Integrator
	{
	private int numSubDiv;
	private HashMap<Integer, EvPixels> distanceMap = new HashMap<Integer, EvPixels>();
	private Shell shell;
	private double[] sliceExp; //Must be double for values to fit
	private int[] sliceVol;
	private NucLineage lin = new NucLineage();
	private String newLinName;
	
	public Map<EvDecimal, Double> bg;
	public TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure;
	private boolean updateBG; // variable if to calculate bg
	
	private double curBgInt = 0;
	private int curBgVol = 0;
	
	//List<Integer> curBg=new ArrayList<Integer>();
	private IntArrayList curBgOutside=new IntArrayList(); 
	private IntArrayList curBgInside=new IntArrayList();
	
	
	
	public OldIntegratorAP(IntExp integrator, String newLinName, int numSubDiv, Map<EvDecimal, Double> bg)
		{
		this.numSubDiv = numSubDiv;
		this.newLinName = newLinName;
		
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
		integrator.imset.metaObject.put(newLinName, lin);
		// imset.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
		shell = integrator.imset.getIdObjectsRecursive(Shell.class).values().iterator().next();
	
		integrator.imset.metaObject.put(newLinName, lin);
	
		// Virtual nuc for AP
		for (int i = 0; i<numSubDiv; i++)
			lin.getCreateNuc("_slice"+i);
		}
	
	public void integrateStackStart(IntExp integrator)
		{
		curBgInside.clear();
		curBgOutside.clear();
		
		curBgInt = 0;
		curBgVol = 0;
		// Zero out arrays
		sliceExp = new double[numSubDiv];
		sliceVol = new int[numSubDiv];
		}
	
	
	
	
	public void integrateImage(IntExp integrator)
		{
		integrator.ensureImageLoaded();
	
		// Calculate distance mask lazily. Assumes shell does not move over time.
		EvPixels lenMap;
		double[] lenMapArr;
		if (distanceMap.containsKey(integrator.curZint))
			{
			lenMap = distanceMap.get(integrator.curZint);
			lenMapArr = lenMap.getArrayDouble();
			}
		else
			{
			lenMap = new EvPixels(EvPixelsType.DOUBLE, integrator.pixels.getWidth(), integrator.pixels.getHeight());
			lenMapArr = lenMap.getArrayDouble();
	
			ImVector2 dirvec = ImVector2.polar(shell.major, shell.angle);
			ImVector2 startpos = dirvec.add(new ImVector2(shell.midx, shell.midy));
			dirvec = dirvec.normalize().mul(-1);
	
			// Calculate distances
			for (int ay = 0; ay<integrator.pixels.getHeight(); ay++)
				{
				int lineIndex = lenMap.getRowIndex(ay);
				for (int ax = 0; ax<integrator.pixels.getWidth(); ax++)
					{
					Vector2d p=integrator.stack.transformImageWorld(new Vector2d(ax,ay));
					
					// Convert to world coordinates
					ImVector2 pos = new ImVector2(p.x,p.y);
							//integrator.stack.transformImageWorldX(ax),
							//integrator.stack.transformImageWorldY(ay));
	
					// Check if this is within ellipse boundary
					ImVector2 elip = pos.sub(new ImVector2(shell.midx, shell.midy)).rotate(shell.angle); // TODO angle? what?
					double len;
					if (1>=elip.y*elip.y/(shell.minor*shell.minor)+elip.x*elip.x/(shell.major*shell.major))
						len = pos.sub(startpos).dot(dirvec)/(2*shell.major); 
					// xy . dirvecx = cos(alpha) ||xy|| ||dirvecx||
					else
						len = -1;
					lenMapArr[lineIndex+ax] = len;
					}
				}
			}
	
		// Integrate area, separate into AP slices and background
		//TODO: have we really really checked that this is done properly?
		for (int y = 0; y<integrator.pixels.getHeight(); y++)
			{
			int lineIndex = integrator.pixels.getRowIndex(y);
			for (int x = 0; x<integrator.pixels.getWidth(); x++)
				{
				int i = lineIndex+x;
				double len = lenMapArr[i];
				if (len>-1)
					{
					int sliceNum = (int) (len*numSubDiv); // may need to bound in addition
					sliceExp[sliceNum] += integrator.pixelsLine[i];
					sliceVol[sliceNum]++;
					
					curBgInside.add(integrator.pixelsLine[i]);
					}
				else
					{
					// Measure background. It's all pixels outside the embryo
					
					curBgOutside.add(integrator.pixelsLine[i]);
					
					curBgInt += integrator.pixelsLine[i];
					curBgVol++;
					}
				}
			}
		}
	

	/**
	 * Stable median calculation. Relies on bucket-sorting. It should be perfectly possible to write a linear-time stable median
	 * based on the normal linear-time median algorithm. This is needed to handle non-8bit images.
	 * Value might be off a bit but not much (need to think of indexing), and it doesn't matter for this application
	 */
	public double calcStableMedian(double lowerFrac, double upperFrac, int[] elem, int numElem)
		{
		//Calculate histogram
		int[] histogram=new int[256];
		//int[] elem=curBgOutside.elements();
		//int numElem=curBgOutside.size();
		for(int i=0;i<numElem;i++)
			histogram[elem[i]]++;
		
		int jumpElem=0;
		int lowerIndex=(int)(numElem*lowerFrac);
		int upperIndex=(int)(numElem*upperFrac);
		int sum=0;
		int cnt=0;
		for(int i=0;i<256;i++)
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

	/*
	//Slow but straight-forward. O(n log n)
	public double calcStableMedian(double lowerFrac, double upperFrac)
		{
		int[] tempArr=new int[curBgOutside.size()];
		System.arraycopy(curBgOutside.elements(), 0, tempArr, 0, tempArr.length);
		Arrays.sort(tempArr);
		int cnt=0;
		int sum=0;
		for(int i=(int)(tempArr.length*lowerFrac);i<tempArr.length*upperFrac;i++)
			{
			sum+=tempArr[i];
			cnt++;
			}
		return (double)sum/cnt;
		}
		*/
	
	public void integrateStackDone(IntExp integrator)
		{
		// Store background if this integrator is responsible for calculating it
		if (updateBG)
			{
			double stableMedianOutside=calcStableMedian(0.4,0.6, curBgOutside.elements(), curBgOutside.size());
			double stableMedianInside=calcStableMedian(0.4,0.6, curBgInside.elements(), curBgInside.size());
			
//			int medianOutside=EvListUtil.findPercentileInt(curBgOutside.elements(), 0.5, curBgOutside.size());
			//int medianOutside=EvListUtil.findPercentileInt(curBgOutside.elements(), 0.51, curBgOutside.size());
//			int medianInside=EvListUtil.findPercentileInt(curBgInside.elements(), 0.5, curBgInside.size());
//			double avg=(double)curBgInt/curBgVol;
			
			//double thisBG=EvMathUtil.minAllInt(medianOutside,medianInside,(int)avg);
			//double thisBG=EvMathUtil.minAll(medianOutside,medianInside,avg);
			double thisBG=Math.min(stableMedianOutside, stableMedianInside);
//			System.out.println("bgs "+medianOutside+"   "+medianInside+"     "+avg+"           "+thisBG);

			System.out.println("bgs "+stableMedianOutside+"      "+stableMedianInside);
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
	
			NucLineage.Nuc nuc = lin.getCreateNuc("_slice"+i);
			NucExp exp = nuc.getCreateExp(integrator.expName);
			exp.level.put(integrator.frame, avg);
			}
	
		}
	
	public void done(IntExp integrator,	TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
		{
		// Set override start and end times
		for (int i = 0; i<numSubDiv; i++)
			{
			NucLineage.Nuc nuc = lin.getCreateNuc("_slice"+i);
			nuc.overrideStart = integrator.ch.getFirstFrame();
			nuc.overrideEnd = integrator.ch.getLastFrame();
			}
		
		//For AP: calculate how to correct exposure
		if (correctedExposure==null)
			correctedExposure = ExpUtil.calculateCorrectExposureChange20100709(
					integrator.imset, lin, integrator.expName, integrator.channelName, new TreeSet<EvDecimal>(integrator.ch.getFrames()), bg);
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
		}
	
	/**
	 * Store profile as array on disk
	 */
	public void profileForGnuplot(IntExp integrator, File file)
		{
		Imageset imset = integrator.data.getObjects(Imageset.class).get(0);
		EvChannel ch = imset.getChannel(integrator.channelName);
		NucLineage lin = (NucLineage) imset.metaObject.get(newLinName);
		try
			{
			StringBuffer outf = new StringBuffer();
	
			here: for (EvDecimal frame : ch.getFrames())
				{
				outf.append(""+frame+"\t");
				for (int i = 0; i<numSubDiv; i++)
					{
					NucLineage.Nuc nuc = lin.nuc.get("_slice"+i);
					NucExp nexp = nuc.exp.get(integrator.expName);
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
