/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.integrateExpression;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Vector3d;

import util2.integrateExpression.IntExp.Integrator;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.shell.Shell;
import endrov.util.EvDecimal;
import endrov.util.ImVector2;
import endrov.util.Tuple;

/**
 * Integrate expression on single-cell level
 * 
 * This version assigns expression to closest nucleus, and uses a shell for a mask
 * 
 * @author Johan Henriksson
 *
 */
public class IntegratorCellClosest implements Integrator
	{
	private NucLineage lin;

	private Map<String, Double> expLevel;
	private Map<String, Integer> nucVol;
	private Map<NucSel, NucLineage.NucInterp> inter;
	private Map<EvDecimal, Double> bg;

	private HashMap<EvDecimal, EvPixels> distanceMap = new HashMap<EvDecimal, EvPixels>();
	private Shell shell;

	private EvDecimal linStart, linEnd;
	
	public IntegratorCellClosest(IntExp integrator, NucLineage lin,
			Map<EvDecimal, Double> bg)
		{
		this.lin = lin;
		this.bg = bg;
		shell = integrator.imset.getIdObjectsRecursive(Shell.class).values().iterator().next();
		
		ExpUtil.clearExp(lin, integrator.expName);
		ExpUtil.clearExp(lin, "CEH-5"); // TEMP
		
		linStart=lin.firstFrameOfLineage().fst();
		linEnd=lin.lastFrameOfLineage().fst();
		}

	public void integrateStackStart(IntExp integrator)
		{
		expLevel = new HashMap<String, Double>();
		nucVol = new HashMap<String, Integer>();
		inter = lin.getInterpNuc(integrator.frame);

		//Fill in 0's directly, removes need for if's later
		for (Map.Entry<NucSel, NucLineage.NucInterp> e : inter.entrySet())
			if(e.getValue().isVisible())
				{
				expLevel.put(e.getKey().snd(), 0.0);
				nucVol.put(e.getKey().snd(), 0);
				}
		}

	
	private boolean includeFrame(IntExp integrator)
		{
		return linStart!=null && integrator.frame.greaterEqual(linStart) && integrator.frame.lessEqual(linEnd);
		}
	
	
	/**
	 * Integrate one plane
	 */
	public void integrateImage(IntExp integrator)
		{
		if(includeFrame(integrator))
			{
			
			// Calculate distance mask lazily
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
		
				ImVector2 dirvec = ImVector2.polar(shell.major, shell.angle);
				ImVector2 startpos = dirvec.add(new ImVector2(shell.midx, shell.midy));
				dirvec = dirvec.normalize().mul(-1);
		
				// Calculate distances
				for (int ay = 0; ay<integrator.pixels.getHeight(); ay++)
					{
					int lineIndex = lenMap.getRowIndex(ay);
					for (int ax = 0; ax<integrator.pixels.getWidth(); ax++)
						{
						// Convert to world coordinates
						ImVector2 pos = new ImVector2(integrator.stack.transformImageWorldX(ax),
								integrator.stack.transformImageWorldY(ay));
		
						// Check if this is within ellipse boundary
						ImVector2 elip = pos.sub(new ImVector2(shell.midx, shell.midy))
								.rotate(shell.angle); // TODO angle? what?
						double len;
						if (1>=elip.y*elip.y/(shell.minor*shell.minor)+elip.x*elip.x
								/(shell.major*shell.major))
							len = pos.sub(startpos).dot(dirvec)/(2*shell.major); 
						// xy . dirvecx = cos(alpha) ||xy|| ||dirvecx||
						else
							len = -1;
						lenMapArr[lineIndex+ax] = len;
						}
					}
				}
		
			// Integrate this area. Go through all pixels and find nucleus, O(w h d #nuc)
			for (int y = 0; y<integrator.pixels.getHeight(); y++)
				{
				int lineIndex = integrator.pixels.getRowIndex(y);
				for (int x = 0; x<integrator.pixels.getWidth(); x++)
					{
					int i = lineIndex+x;
					double len = lenMapArr[i];
					if (len>-1)
						{
						int thisExp=integrator.pixelsLine[i];

						Vector3d thisPosWorld=integrator.stack.transformImageWorld(new Vector3d(x,y,0));
						thisPosWorld.z=integrator.curZ.doubleValue();
						
						String closestNuc=null;
						double closestDistance=0;
						
						//Find closest nucleus
						for (Map.Entry<NucSel, NucLineage.NucInterp> e : inter.entrySet())
							if(e.getValue().isVisible())
								{
								NucLineage.NucPos pos = e.getValue().pos;
								double dx=thisPosWorld.x-pos.x;
								double dy=thisPosWorld.y-pos.y;
								double dz=thisPosWorld.z-pos.z;
								double dist2=dx*dx+dy*dy+dz*dz;
								if(closestNuc==null || dist2<closestDistance)
									{
									closestDistance=dist2;
									closestNuc=e.getKey().snd();
									}
								}

						// Sum up volume and area
						nucVol.put(closestNuc, nucVol.get(closestNuc)+1);
						expLevel.put(closestNuc, expLevel.get(closestNuc)+thisExp);
						}
					}
				}
			
			
			}
		}

	/**
	 * Done integrating one stack
	 */
	public void integrateStackDone(IntExp integrator)
		{
		if(includeFrame(integrator))
			{
			
			// Store value in XML
			for (String nucName : expLevel.keySet())
				{
				// Assumption: a cell does not move to vol=0 in the mid so it is fine to
				// throw away these values.
				// they have to be set to 0 otherwise
				double vol = nucVol.get(nucName);
				if (vol!=0)
					{
					double avg = expLevel.get(nucName)/vol;
					// System.out.println(nucName+" "+avg);
					NucExp exp = lin.nuc.get(nucName).getCreateExp(integrator.expName);
					if (lin.nuc.get(nucName).pos.lastKey().greaterEqual(integrator.frame)
							&&lin.nuc.get(nucName).pos.firstKey().lessEqual(integrator.frame))
						exp.level.put(integrator.frame, avg);
					}
				
				}
			
			}
		}

	/**
	 * Done with all stacks
	 */
	public void done(IntExp integrator,
			TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
		{

		// Use prior correction on this expression as well
		Double max1 = ExpUtil.getSignalMax(lin, integrator.expName);
		if (max1==null)
			System.out.println("max==null, there is no signal!");
		else
			{
			ExpUtil.normalizeSignal(lin, integrator.expName, max1, 0, 1);
			ExpUtil.correctExposureChange(correctedExposure, lin, integrator.expName);
			}

		}


	/**
	 * Project sphere onto plane. Assumes resx=resy
	 * 
	 * @param nucRw
	 *          Radius
	 * @param nucZw
	 *          Relative z
	 */
	public static Double projectSphere(double nucRw, double nucZw, double imageZw)
		{
		double dz = nucZw-imageZw;
		double tf = nucRw*nucRw-dz*dz;
		if (tf>0)
			return Math.sqrt(tf);
		else
			return null;
		}


	}
