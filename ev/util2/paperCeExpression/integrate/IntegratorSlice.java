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
import endrov.util.EvListUtil;
import endrov.util.EvMathUtil;
import endrov.util.ImVector2;
import endrov.util.ImVector3d;
import endrov.util.Tuple;

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
	
	
	
	public IntegratorSlice(IntExp integrator, String newLinName, int numSubDiv, Map<EvDecimal, Double> bg)
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
	
	
	//Normalized with inverse length of axis 
	public abstract ImVector3d getDirVec();
	
	public ImVector3d getMidPos()
		{
		return new ImVector3d(shell.midx, shell.midy, shell.midz); 
		}
	
	
	public void integrateImage(IntExp integrator)
		{
		integrator.ensureImageLoaded();
		
		// Calculate distance mask lazily. Assumes shell does not move over time.
		EvPixels lenMap;
		double[] lenMapArr;
		if (distanceMap.containsKey(integrator.curZ))
			{
			lenMap = distanceMap.get(integrator.curZ);
			lenMapArr = lenMap.getArrayDouble();
			}
		else
			{
			lenMap = new EvPixels(EvPixelsType.DOUBLE, integrator.pixels.getWidth(), integrator.pixels.getHeight());
			lenMapArr = lenMap.getArrayDouble();
	
			ImVector3d dirvec = getDirVec();
			ImVector3d midpos3 = getMidPos();
	
			// Calculate distances
			for (int ay = 0; ay<integrator.pixels.getHeight(); ay++)
				{
				int lineIndex = lenMap.getRowIndex(ay);
				for (int ax = 0; ax<integrator.pixels.getWidth(); ax++)
					{
					ImVector2 shellPos2=new ImVector2(shell.midx, shell.midy);

					// Convert to world coordinates
					ImVector2 pos2 = new ImVector2(
							integrator.stack.transformImageWorldX(ax),
							integrator.stack.transformImageWorldY(ay));
					ImVector3d pos3 = new ImVector3d(
							pos2.x,
							pos2.y,
							integrator.curZ.doubleValue());
	
					// Check if this is within ellipse boundary
					ImVector2 elip = pos2.sub(shellPos2).rotate(shell.angle); // TODO angle? what?
					double len;
					if (1>=elip.y*elip.y/(shell.minor*shell.minor)+elip.x*elip.x/(shell.major*shell.major))
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
	
		// Integrate area, separate into AP slices and background
		//TODO: have we really really checked that this is done properly?
		for (int y = 0; y<integrator.pixels.getHeight(); y++)
			{
			int lineIndex = integrator.pixels.getRowIndex(y);
			for (int x = 0; x<integrator.pixels.getWidth(); x++)
				{
				int i = lineIndex+x;
				double len = lenMapArr[i];
				int sliceNum = (int) (len*numSubDiv); // may need to bound in addition
				if(sliceNum>=0 && sliceNum<sliceExp.length)
					{
					sliceExp[sliceNum] += integrator.pixelsLine[i];
					sliceVol[sliceNum]++;

					curBgInside.add(integrator.pixelsLine[i]);
					}
				else if(len==-1) //so things close the embryo will not be considered - likely too bright
					{
					// Measure background. It's all the pixels outside the embryo
					curBgOutside.add(integrator.pixelsLine[i]);
					curBgInt += integrator.pixelsLine[i];
					curBgVol++;
					}
				}
			}
		}
	
	public void integrateStackDone(IntExp integrator)
		{
		// Store background if this integrator is responsible for calculating it
		if (updateBG)
			{
			int medianOutside=EvListUtil.findPercentileInt(curBgOutside.elements(), 0.5, curBgOutside.size());
			int medianInside=EvListUtil.findPercentileInt(curBgInside.elements(), 0.5, curBgInside.size());
			//double avg=(double)curBgInt/curBgVol;
			
			//int thisBG=EvMathUtil.minAll(medianOutside,medianInside,(int)avg);
			int thisBG=EvMathUtil.minAll(medianOutside,medianInside);   //average decreased value in a bad value for one recording!!! I think
			bg.put(integrator.frame, (double)thisBG);
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
			nuc.overrideStart = integrator.ch.imageLoader.firstKey();
			nuc.overrideEnd = integrator.ch.imageLoader.lastKey();
			}
		
		//For AP: calculate how to correct exposure
		if (correctedExposure==null)
			correctedExposure = ExpUtil.calculateCorrectExposureChange20100709(
					integrator.imset, lin, integrator.expName, integrator.channelName, new TreeSet<EvDecimal>(integrator.ch.imageLoader.keySet()), bg);
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
	
			here: for (EvDecimal frame : ch.imageLoader.keySet())
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
