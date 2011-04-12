/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nuc.integrate;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.nuc.NucLineage.Nuc;
import endrov.nuc.integrate.IntegrateExp.Integrator;
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

	private Map<String, Double> nucSumExp;
	private Map<String, Integer> nucVol;
	private Map<NucSel, NucLineage.NucInterp> inter;
	private Map<EvDecimal, Double> bg;

	private HashMap<Integer, EvPixels> shellDistanceMap = new HashMap<Integer, EvPixels>();
	private Shell shell;

	private EvDecimal linStart, linEnd;
	
	private boolean useNucleiRadius;
	private boolean subtractCytoplasmaBackground=false;
	
	private Map<String,Double> nucSumBG=new HashMap<String, Double>();
	private Map<String,Integer> nucVolBG=new HashMap<String, Integer>();
	
	public IntegratorCellClosest(IntegrateExp integrator, NucLineage lin,	Map<EvDecimal, Double> bg, boolean useNucleiRadius)
		{
		this.lin = lin;
		this.bg = bg;
		this.useNucleiRadius=useNucleiRadius;
		shell = integrator.imset.getIdObjectsRecursive(Shell.class).values().iterator().next();
		
		ExpUtil.clearExp(lin, integrator.expName);
		//ExpUtil.clearExp(lin, "CEH-5"); // TEMP
		
		linStart=specialFirstFrame(lin);
		linEnd=specialLastFrame(lin);
		}

	private static boolean considerCell(String name)
		{
		return !(name.equals("lastframe") || name.equals("gast") || name.equals("venc") || name.equals("2ftail") || name.startsWith("shell"));
		}

	private static EvDecimal specialLastFrame(NucLineage lin)
		{
		EvDecimal found=null;
		for(Map.Entry<String, Nuc> n:lin.nuc.entrySet())
			if(considerCell(n.getKey()))
				if(found==null || (!n.getValue().pos.isEmpty() && n.getValue().getLastFrame().greater(found)))
					found=n.getValue().getLastFrame();
		return found;
		}
	private static EvDecimal specialFirstFrame(NucLineage lin)
		{
		EvDecimal found=null;
		for(Map.Entry<String, Nuc> n:lin.nuc.entrySet())
			if(considerCell(n.getKey()))
				if(found==null || (!n.getValue().pos.isEmpty() && n.getValue().getFirstFrame().less(found)))
					found=n.getValue().getLastFrame();
		return found;
		}

	
	
	public void integrateStackStart(IntegrateExp integrator)
		{
		nucSumExp = new HashMap<String, Double>();
		nucVol = new HashMap<String, Integer>();
		inter = lin.getInterpNuc(integrator.frame);

		nucSumBG = new HashMap<String, Double>();
		nucVolBG = new HashMap<String, Integer>();
		
		//Fill in 0's directly, removes need for if's later
		for (Map.Entry<NucSel, NucLineage.NucInterp> e : inter.entrySet())
			if(e.getValue().isVisible())
				{
				nucSumExp.put(e.getKey().snd(), 0.0);
				nucVol.put(e.getKey().snd(), 0);
				
				nucSumBG.put(e.getKey().snd(), 0.0);
				nucVolBG.put(e.getKey().snd(), 0);
				}
		}

	
	private boolean includeFrame(IntegrateExp integrator)
		{
		return linStart!=null && integrator.frame.greaterEqual(linStart) && integrator.frame.lessEqual(linEnd);
		}
	
	
	/**
	 * Integrate one plane
	 */
	public void integrateImage(IntegrateExp integrator)
		{
		if(includeFrame(integrator))
			{
			
			// Calculate distance mask lazily
			EvPixels lenMap;
			double[] lenMapArr;
			if (shellDistanceMap.containsKey(integrator.curZint))
				{
				lenMap = shellDistanceMap.get(integrator.curZint);
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
						Vector2d pos2 = integrator.stack.transformImageWorld(new Vector2d(ax,ay));
						ImVector2 pos = new ImVector2(pos2.x, pos2.y);
						//ImVector2 pos = new ImVector2(integrator.stack.transformImageWorldX(ax), integrator.stack.transformImageWorldY(ay));
		
						// Check if this is within ellipse boundary
						ImVector2 elip = pos.sub(new ImVector2(shell.midx, shell.midy))
								.rotate(shell.angle); // TODO angle? what?
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

						Vector3d thisPosWorld=integrator.stack.transformImageWorld(new Vector3d(x,y,integrator.curZint));
						//thisPosWorld.z=integrator.curZ.doubleValue();
						
						//if(useNucleiRadius)
						//{
						//Use voronoi approximation
						String closestNuc=null;
						double closestDistance=0;

						boolean isInBg=false;

						//Find closest nucleus
						for (Map.Entry<NucSel, NucLineage.NucInterp> e : inter.entrySet())
							{
							if(e.getValue().isVisible() && considerCell(e.getKey().snd()))
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
									isInBg=useNucleiRadius && dx*dx+dy*dy>pos.r*pos.r; //2D
									//nisInBg=dx*dx+dy*dy+dz*dz>pos.r*pos.r; //3D
									}
								}
							}

						// Sum up volume and area
						if(isInBg)
							{
							nucVolBG.put(closestNuc, nucVolBG.get(closestNuc)+1);
							nucSumBG.put(closestNuc, nucSumBG.get(closestNuc)+thisExp);
							}
						else
							{
							nucVol.put(closestNuc, nucVol.get(closestNuc)+1);
							nucSumExp.put(closestNuc, nucSumExp.get(closestNuc)+thisExp);
							}
						}



							/*
							{
							boolean usedPixel=false;
							//Integrate only what is inside the nuclei
							for (Map.Entry<NucSel, NucLineage.NucInterp> e : inter.entrySet())
								{
								if(e.getValue().isVisible() && considerCell(e.getKey().snd()))
									{
									NucLineage.NucPos pos = e.getValue().pos;
									double dx=thisPosWorld.x-pos.x;
									double dy=thisPosWorld.y-pos.y;
									double dz=thisPosWorld.z-pos.z;
									double dist2=dx*dx+dy*dy+dz*dz;
									
									if(dist2<pos.r*pos.r)
										{
										// Sum up volume and area
										String nucName=e.getKey().snd();
										nucVol.put(nucName, nucVol.get(nucName)+1);
										nucSumExp.put(nucName, nucSumExp.get(nucName)+thisExp);
										usedPixel=true;
										}
									
									else if(dist2<pos.r*pos.r*1.5*1.5)
										{
										//It has to be far away enough to be considered background
										usedPixel=true;
										}
										
									}
								}
							
							if(!usedPixel)
								{
								//This pixel can be considered background!
								sumBgAroundCells+=thisExp;
								countBgAroundCells++;
								}
								
							}*/
						/*else
							{
							//Use voronoi approximation
							String closestNuc=null;
							double closestDistance=0;
													
							//Find closest nucleus
							for (Map.Entry<NucSel, NucLineage.NucInterp> e : inter.entrySet())
								{
								if(e.getValue().isVisible() && considerCell(e.getKey().snd()))
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
								}

							// Sum up volume and area
							nucVol.put(closestNuc, nucVol.get(closestNuc)+1);
							nucSumExp.put(closestNuc, nucSumExp.get(closestNuc)+thisExp);
							}
						
						
						}*/
					}
				}
			
			
			}
		}

	/**
	 * Done integrating one stack
	 */
	public void integrateStackDone(IntegrateExp integrator)
		{
		if(includeFrame(integrator))
			{
			double curBg=bg.get(integrator.frame);

			/*
			//Correction for this integrator only - using cell plasma for background
			double curBg=0;
			double frameCellCorrection=sumBgAroundCells/countBgAroundCells;
			curBg+=frameCellCorrection;
			System.out.println("correction "+frameCellCorrection);
			*/
			
			// Store value in XML
			for (String nucName : nucSumExp.keySet())
				{
				// Assumption: a cell does not move to vol=0 in the mid so it is fine to
				// throw away these values.
				// they have to be set to 0 otherwise
				double vol = nucVol.get(nucName);
				if (vol!=0)
					{
					EvDecimal lastFrame=lin.nuc.get(nucName).getLastFrame();
					if(lastFrame==null)
						lin.nuc.get(nucName).pos.lastKey();
					EvDecimal firstFrame=lin.nuc.get(nucName).getFirstFrame();
					
					if (lastFrame.greaterEqual(integrator.frame) && firstFrame.lessEqual(integrator.frame))
						{
						
						
						if(subtractCytoplasmaBackground && useNucleiRadius)
							{
							int thisVol=nucVolBG.get(nucName);
							double thisSum=nucSumBG.get(nucName);
							if(thisVol!=0)
								{
								double thisCellBg=thisSum/thisVol;
								
								double avg = nucSumExp.get(nucName)/vol-thisCellBg;
								NucExp exp = lin.nuc.get(nucName).getCreateExp(integrator.expName);
								exp.level.put(integrator.frame, avg);
								}
							}
						else
							{
							double avg = nucSumExp.get(nucName)/vol-curBg;
							NucExp exp = lin.nuc.get(nucName).getCreateExp(integrator.expName);
							exp.level.put(integrator.frame, avg);
							}

						}
					}
				
				}
			
			}
		}

	
	public static void assumeLeastExpressionIsBackground(NucLineage lin, String expName)
		{
		TreeSet<EvDecimal> frames=new TreeSet<EvDecimal>();
		for(NucLineage.Nuc n:lin.nuc.values())
			{
			NucExp exp=n.exp.get(expName);
			if(exp!=null)
				frames.addAll(n.exp.get(expName).level.keySet());
			}
		
		for(EvDecimal frame:frames)
			{
			Double lowestLevel=null;
			for(NucLineage.Nuc n:lin.nuc.values())
				{
				NucExp exp=n.exp.get(expName);
				if(exp!=null)
					{
					Double thisLevel=n.exp.get(expName).level.get(frame);
					if(thisLevel!=null && (lowestLevel==null || thisLevel<lowestLevel))
						lowestLevel=thisLevel;
					}
				}
			for(NucLineage.Nuc n:lin.nuc.values())
				{
				NucExp exp=n.exp.get(expName);
				if(exp!=null)
					{
					Double thisLevel=n.exp.get(expName).level.get(frame);
					if(thisLevel!=null)
						n.exp.get(expName).level.put(frame, thisLevel-lowestLevel);
					}
				}
			}
		}
	
	/**
	 * Done with all stacks
	 */
	public void done(IntegrateExp integrator,	TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
		{

		// Use prior correction on this expression as well
		Double max1 = ExpUtil.getSignalMax(lin, integrator.expName);
		if (max1==null)
			System.out.println("max==null, there is no signal!");
		else
			{
			//Check: it was in the other order before!!! might affect generated data
			ExpUtil.correctExposureChange(correctedExposure, lin, integrator.expName);
			ExpUtil.normalizeSignal(lin, integrator.expName, max1, 0, 1);
			
			
			//This should only be enabled if the user knows this is fine!
			if(false)
				assumeLeastExpressionIsBackground(lin, integrator.expName);
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
