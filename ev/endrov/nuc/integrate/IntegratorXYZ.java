/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nuc.integrate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Vector3d;

import endrov.coordinateSystem.CoordinateSystem;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.nuc.NucLineage;
import endrov.nuc.NucRemapUtil;
import endrov.nuc.integrate.IntegrateExp.Integrator;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;
import endrov.util.Tuple;

/**
 * Integrate expression on an overlaid cube. Goes into a full stack in the end,
 * but this might be difficult to visualize.
 * 
 * @author Johan Henriksson
 *
 */
public class IntegratorXYZ implements Integrator
	{
	private int numSubDiv;
	private double[][][] sliceExp; // z,y,x
	private int[][][] sliceVol; // z,y,x

	private Map<EvDecimal, Double> bg;
	private CoordinateSystem cs;
	private Map<EvDecimal,double[][][]> expMap=new HashMap<EvDecimal, double[][][]>(); //z y x
	
	public IntegratorXYZ(IntegrateExp integrator, String newLinName, int numSubDiv, Map<EvDecimal, Double> bg)
		{
		this.numSubDiv = numSubDiv;
		this.bg = bg;

		integrator.imset.metaObject.remove("indX");
		integrator.imset.metaObject.remove("indY");
		integrator.imset.metaObject.remove("indZ");
		}

	
	
	
	/**
	 * Set up coordinate system, return if successful
	 */
	public boolean setupCS(NucLineage refLin)
		{
		NucLineage.Nuc nucP2 = refLin.nuc.get("P2'");
		NucLineage.Nuc nucEMS = refLin.nuc.get("EMS");

		Vector3d posABp = NucRemapUtil.getLastPosABp(refLin);
		Vector3d posABa = NucRemapUtil.getLastPosABa(refLin);

		if (nucP2==null||posABa==null||posABp==null||nucEMS==null
				||nucP2.pos.isEmpty()
				||nucEMS.pos.isEmpty())
			{
			System.out.println("Does not have enough cells marked, will not produce cube");
			return false;
			}
		else
			{
			System.out.println("Will do XYZ");
			}

		Vector3d posP2 = nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy();
		//Vector3d posABa = nucABa.pos.get(nucABa.pos.lastKey()).getPosCopy();
		//Vector3d posABp = nucABp.pos.get(nucABp.pos.lastKey()).getPosCopy();
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

		// Create coordinate system. Enlarge
		cs = new CoordinateSystem();
		double scale = 1.35;
		cs.setFromTwoVectors(v1, v2, v1.length()*scale, v2.length()*scale, v2.length()*scale, mid);

		return true;
		}

	public void integrateStackStart(IntegrateExp integrator)
		{
		// Zero out arrays
		sliceExp = new double[numSubDiv][numSubDiv][numSubDiv];
		sliceVol = new int[numSubDiv][numSubDiv][numSubDiv];
		}
	
	/**
	 * Calculate index map lazily
	 */
	private void ensureIndMapCalculated(IntegrateExp integrator)
		{
		ProgressHandle progh=new ProgressHandle();
		
		EvChannel chIndexX = integrator.imset.getCreateChannel("indX");
		EvChannel chIndexY = integrator.imset.getCreateChannel("indY");
		EvChannel chIndexZ = integrator.imset.getCreateChannel("indZ");
		
		if(chIndexX.getStack(EvDecimal.ZERO)==null)
			{
			int w = integrator.pixels.getWidth();
			int h = integrator.pixels.getHeight();
			int d = integrator.stack.getDepth();
			
			EvStack stackIndexX=new EvStack();//.getCreateFrame(EvDecimal.ZERO);
			EvStack stackIndexY=new EvStack();//chIndexY.getCreateFrame(EvDecimal.ZERO);
			EvStack stackIndexZ=new EvStack();//chIndexZ.getCreateFrame(EvDecimal.ZERO);
			
			stackIndexX.allocate(w, h, d, EvPixelsType.INT, integrator.stack);
			stackIndexY.allocate(w, h, d, EvPixelsType.INT, integrator.stack);
			stackIndexZ.allocate(w, h, d, EvPixelsType.INT, integrator.stack);
			
			
			chIndexX.putStack(EvDecimal.ZERO, stackIndexX);
			chIndexY.putStack(EvDecimal.ZERO, stackIndexY);
			chIndexZ.putStack(EvDecimal.ZERO, stackIndexZ);
			
			for(int az=0;az<integrator.stack.getDepth();az++)
				{
				EvPixels pX=stackIndexX.getInt(az).getPixels(progh);
				EvPixels pY=stackIndexX.getInt(az).getPixels(progh);
				EvPixels pZ=stackIndexX.getInt(az).getPixels(progh);

				int[] lineX = pX.getArrayInt();
				int[] lineY = pY.getArrayInt();
				int[] lineZ = pZ.getArrayInt();

				// Calculate indices
				for(int ay = 0; ay<integrator.pixels.getHeight(); ay++)
					{
					for(int ax = 0; ax<integrator.pixels.getWidth(); ax++)
						{
						// Convert to world coordinates
						Vector3d pos = integrator.stack.transformImageWorld(new Vector3d(ax,ay,integrator.curZint));
//						Vector3d pos = new Vector3d(integrator.stack.transformImageWorldX(ax),
//								integrator.stack.transformImageWorldY(ay), integrator.curZ.doubleValue());

						Vector3d insys = cs.transformToWorld(pos);

						int cx = (int) ((insys.x+0.5)*numSubDiv);
						int cy = (int) ((insys.y+0.5)*numSubDiv);
						int cz = (int) ((insys.z+0.5)*numSubDiv);

						int index = pX.getPixelIndex(ax, ay);
						if (cx>=0 && cy>=0 && cz>=0 && 
								cx<numSubDiv && cy<numSubDiv && cz<numSubDiv)
							{
							lineX[index] = cx;
							lineY[index] = cy;
							lineZ[index] = cz;
							}
						else
							lineX[index] = -1;
						}
					}
				}
			

			}
		}

	public void integrateImage(IntegrateExp integrator)
		{
		ProgressHandle progh=new ProgressHandle();
		
		ensureIndMapCalculated(integrator);
		integrator.ensureImageLoaded();

		//Load precalculated index
		EvChannel chIndexX = integrator.imset.getCreateChannel("indX");
		EvChannel chIndexY = integrator.imset.getCreateChannel("indY");
		EvChannel chIndexZ = integrator.imset.getCreateChannel("indZ");
		EvPixels pX = chIndexX.getStack(EvDecimal.ZERO).getInt(integrator.curZint).getPixels(progh);
		EvPixels pY = chIndexY.getStack(EvDecimal.ZERO).getInt(integrator.curZint).getPixels(progh);
		EvPixels pZ = chIndexZ.getStack(EvDecimal.ZERO).getInt(integrator.curZint).getPixels(progh);

		// Integrate this area
		int[] lineX = pX.getArrayInt();
		int[] lineY = pY.getArrayInt();
		int[] lineZ = pZ.getArrayInt();
		for (int i = 0; i<integrator.pixelsLine.length; i++)
			{
			int cx = lineX[i];
			if (cx!=-1)
				{
				int cy = lineY[i];
				int cz = lineZ[i];
				sliceExp[cz][cy][cx] += integrator.pixelsLine[i];
				sliceVol[cz][cy][cx]++;
				}
			}

		}

	/**
	 * One stack processed
	 */
	public void integrateStackDone(IntegrateExp integrator)
		{
		double[][][] out=new double[numSubDiv][numSubDiv][numSubDiv];
		
		// Store pattern in lineage
		for (int az = 0; az<numSubDiv; az++)
			for (int ay = 0; ay<numSubDiv; ay++)
				for (int ax = 0; ax<numSubDiv; ax++)
					{
					double curbg = bg.get(integrator.frame);
					double vol = sliceVol[az][ay][ax];
					double avg;
					
					if(vol==0)
						avg=0;
					else
						avg=sliceExp[az][ay][ax]/vol-curbg;    //(sliceExp[az][ay][ax]/vol-curbg)/integrator.expTime;   //normalization done later
					
					out[az][ay][ax]=avg;
					}
		expMap.put(integrator.frame, out);
		}

	/**
	 * All frames processed
	 */
	public void done(IntegrateExp integrator, TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
		{
		for(Map.Entry<EvDecimal, double[][][]> e:expMap.entrySet())
			for(double[][] vv:e.getValue())
				for(double[] vvv:vv)
					ExpUtil.correctExposureChange(correctedExposure, e.getKey(),vvv);

		ProgressHandle progh=new ProgressHandle();
		
		//Make in range for output                                     TODO does it here even matter if exposure was corrected????
		double sigMax = ExpUtil.getSignalMax(expMap.values());
		double sigMin = ExpUtil.getSignalMin(expMap.values());
		for(double[][][] v:expMap.values())
			for(double[][] vv:v)
				for(double[] vvv:vv)
					ExpUtil.normalizeSignal(vvv, sigMax, sigMin, 255);
		
		// Store expression as a new channel
		EvChannel chanxyz = new EvChannel();//.imset.getCreateChannel("XYZ");
		//chanxyz.imageLoader.clear();
		integrator.imset.metaObject.put("XYZ", chanxyz);
		
		for (EvDecimal frame : new LinkedList<EvDecimal>(expMap.keySet()))
			{
			EvStack stack = new EvStack();//.getCreateFrame(frame);
			chanxyz.putStack(frame, stack);
			stack.allocate(numSubDiv, numSubDiv, numSubDiv, EvPixelsType.DOUBLE, null);
			stack.setRes(16,16,1); //Arbitrary
			
			for (int az = 0; az<numSubDiv; az++)
				{
				double[][] planeExp=expMap.get(frame)[az];
				
				EvPixels p = stack.getInt(az).getPixels(progh);
				double[] line = p.getArrayDouble();
				for (int ay = 0; ay<numSubDiv; ay++)
					for (int ax = 0; ax<numSubDiv; ax++)
						line[p.getPixelIndex(ax, ay)] = planeExp[ay][ax];
				}
			
			//To conserve memory it is now possible to remove the old array
			expMap.remove(frame);
			}
		}

	
	
	
	
	}
