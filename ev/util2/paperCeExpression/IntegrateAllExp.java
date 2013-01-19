package util2.paperCeExpression;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import util2.paperCeExpression.collectData.PaperCeExpressionUtil;

import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.Imageset;
import endrov.typeLineage.Lineage;
import endrov.typeLineage.expression.IntegrateExp;
import endrov.typeLineage.expression.IntegratorCellClosest;
import endrov.typeLineage.expression.IntegratorSliceAP;
import endrov.typeLineage.expression.IntegratorSliceDV;
import endrov.typeLineage.expression.IntegratorSliceLR;
import endrov.typeLineage.expression.IntegratorXYZ;
import endrov.typeLineage.expression.IntegrateExp.Integrator;
import endrov.typeLineage.util.LineageMergeUtil;
import endrov.util.collection.Tuple;
import endrov.util.io.EvFileUtil;
import endrov.util.math.EvDecimal;

public class IntegrateAllExp
	{

	/**
	 * Check if it really should be included
	 */
	public static void checkToInclude(File f)
		{
		EvData data = EvData.loadFile(f);
	
		Map<EvPath, EvChannel> obs = data.getIdObjectsRecursive(EvChannel.class);
		boolean toInclude = false;
	
		for (EvPath p : obs.keySet())
			if (p.getLeafName().equals("GFP"))
				toInclude = true;
	
		if (toInclude)
			{
			boolean hasShell = !data.getIdObjectsRecursive(EvChannel.class).isEmpty();
			boolean hasLineage = !data.getIdObjectsRecursive(Lineage.class)
					.isEmpty();
			System.out.println("Include: "+f+"    "+(hasShell ? "shell" : "")+"    "
					+(hasLineage ? "lineage" : ""));
			}
	
		}

	public static boolean isDone(File f)
		{
		File ftagCalcDone = new File(f, "tagCalcDone4d.txt");
		if (ftagCalcDone.exists())
			return true;
		else
			return false;
		}

	public static void markDone(File f)
		{
		File ftagCalcDone = new File(f, "tagCalcDone4d.txt");
		try
			{
			ftagCalcDone.createNewFile();
			}
		catch (IOException e1)
			{
			e1.printStackTrace();
			}
		}

	/**
	 * Integrate everything for one recording. Store on disk
	 */
	public static boolean doOne(File f, boolean forceDo)
		{
		try
			{
			System.gc();
	
			if (isDone(f))
				{
				System.out.println("Already done: "+f);
				return true;
				}
			// rm /Volumes/TBU_main06/ost4dgood/*.ost/tagCalcDone4d.txt
	
			final EvData data = EvData.loadFile(f);
	
			// Do RFP or GFP?
			boolean doGFP = false;
			for (EvPath path : data.getIdObjectsRecursive(EvChannel.class).keySet())
				{
				System.out.println(path);
				if (path.getLeafName().equals("GFP"))
					doGFP = true;
				}
	
			System.out.println("Doing: "+f+"\t\tGFP:"+doGFP);
	
			int numSubDiv = 20;
			String channelName = doGFP ? "GFP" : "RFP";
			String expName = "exp"; // Neutral name
	
			if (!data.getIdObjectsRecursive(EvChannel.class).containsKey(
					new EvPath(data, "im", channelName)))
				{
				System.out.println("Does not contain any channel! "+f);
				return false;
				}
	
			// Fixing time is not done now. (what about gnuplot?).
			// makes checking difficult if applied
			// => forced to apply when merging, and searching if searching in original
			// => output both frames and model time in gnuplot files?
	
			String newLinNameT = IntegrateAllExp.linForAP(1, channelName);
			String newLinNameAP = IntegrateAllExp.linForAP(numSubDiv, channelName);
			String newLinNameLR = IntegrateAllExp.linForLR(numSubDiv, channelName);
			String newLinNameDV = IntegrateAllExp.linForDV(numSubDiv, channelName);
	
			IntegrateExp.refLin=IntegrateAllExp.getRefLin(data);
	
			// Decide on integrators
			LinkedList<Integrator> integrators = new LinkedList<Integrator>();
	
			// boolean hasShell=!data.getIdObjects(Shell.class).isEmpty();
	
			Imageset imset = data.getObjects(Imageset.class).get(0);
			final EvChannel ch = imset.getChannel(channelName);
			if (ch==null)
				throw new RuntimeException("No such channel "+channelName);
	
			//Order of integrators matters!
			IntegrateExp integrator = new IntegrateExp(imset, ch, expName);
	
			// AP-level expression
			IntegratorSliceAP intAP = new IntegratorSliceAP(integrator, numSubDiv, null);
			integrators.add(intAP);
			
			//T-level expression
			IntegratorSliceAP intT = new IntegratorSliceAP(integrator, 1, intAP.bg);
			integrators.add(intT);
	
			// DV - requires an angle
			IntegratorSliceDV intDV = new IntegratorSliceDV(integrator, numSubDiv, intAP.bg);
			if (IntegrateExp.refLin!=null && intDV.setupCS(IntegrateExp.refLin))
				integrators.add(intDV);
			else
				intDV=null;
	
			// LR - requires an angle
			IntegratorSliceLR intLR = new IntegratorSliceLR(integrator, numSubDiv, intAP.bg);
			if (IntegrateExp.refLin!=null && intLR.setupCS(IntegrateExp.refLin))
				integrators.add(intLR);
			else
				intLR=null;
	
			// XYZ cube level expression
			IntegratorXYZ intXYZ = new IntegratorXYZ(integrator, newLinNameAP, numSubDiv, intAP.bg);
			if (IntegrateExp.refLin!=null && intXYZ.setupCS(IntegrateExp.refLin))
				integrators.add(intXYZ);
			else
				intXYZ = null;
	
			// Cell level expression 
			IntegratorCellClosest intC = null;
			String nameEstCell="estcell";
			imset.metaObject.remove(nameEstCell);
			if (IntegrateExp.refLin!=null)
				{
				/*
				if(channelName.equals("RFP"))
					{
					//Use manually created lineage
					intC = new IntegratorCellClosest(integrator, IntegrateExp.refLin, intAP.bg, false);
					integrators.add(intC);
					}
				else*/
					{
					//Just superimpose the model, normalized!
					
					try
						{
						Lineage newlin=LineageMergeUtil.mapModelToRec(IntegrateExp.refLin, PaperCeExpressionUtil.loadModel());
						
						imset.metaObject.put(nameEstCell, newlin);
						
						intC = new IntegratorCellClosest(integrator, newlin, intAP.bg, false);
						integrators.add(intC);
						}
					catch (Exception e1)
						{
						e1.printStackTrace();
						}
					}
				}
			
			//Disable integration
			/*intXYZ=null;
			intDV=null;
			intLR=null;
			intC=null;*/
			///
			
			// Run integrators
			integrator.integrateAll(new IntegrateExp.IntegratorCallback()
				{
				public boolean status(IntegrateExp integrator)
					{
					System.out.println(data+"    integrating frame "+integrator.frame+" / "+ch.getFirstFrame()+" - "+ch.getLastFrame());
					return true;
					}
				public void fail(Exception e)
					{
					e.printStackTrace();
					}
				
				}, integrators);
	
			// Wrap up, store in OST
			// Use common correction factors for exposure
			Lineage getLinAP=intAP.done(integrator, null);
			imset.metaObject.put(newLinNameAP, getLinAP);
						
			Lineage getLinT=intT.done(integrator, intAP.correctedExposure);
			imset.metaObject.put(newLinNameT, getLinT);
			
			if(intDV!=null)
				{
				Lineage getLin=intDV.done(integrator, intAP.correctedExposure);
				imset.metaObject.put(newLinNameDV, getLin);
				}
			if(intLR!=null)
				{
				Lineage getLin=intLR.done(integrator, intAP.correctedExposure);
				imset.metaObject.put(newLinNameLR, getLin);
				}
			if (intXYZ!=null)
				{
				intXYZ.done(integrator, intAP.correctedExposure);
				//imset.metaObject.put(newLinName, getLin);
				}
			if (intC!=null)
				{
				intC.done(integrator, intAP.correctedExposure);
				}
	
			IntegrateAllExp.storeCorrection(f, intAP.correctedExposure, intAP.bg);
	
			// Put integral in file for use by Gnuplot
			intAP.profileForGnuplot(integrator, IntegrateAllExp.fileForAP(data, numSubDiv, channelName));
			if(intDV!=null)
				intDV.profileForGnuplot(integrator, IntegrateAllExp.fileForDV(data, numSubDiv, channelName));
			if(intLR!=null)
				intLR.profileForGnuplot(integrator, IntegrateAllExp.fileForLR(data, numSubDiv, channelName));
	
			System.out.println("Saving OST");
			data.saveData();
			System.out.println("ok, done with "+f);
	
			markDone(f);
	
			return true;
			}
		catch (Exception e)
			{
			System.out.println("------------------- exception, halting doOne!");
			e.printStackTrace();
			return false;
			}
		}

	public static void storeCorrection(File f, TreeMap<EvDecimal, Tuple<Double, Double>> correction, Map<EvDecimal, Double> bg)
		{
		File tw = new File(new File(f, "data"), "expcorr.txt");
		StringBuffer bf = new StringBuffer();
		for (Map.Entry<EvDecimal, Tuple<Double, Double>> e : correction.entrySet())
			bf.append(e.getKey()+"\t"+e.getValue().fst()+"\t"+e.getValue().snd()+"\t"+bg.get(e.getKey())+"\n");
		try
			{
			EvFileUtil.writeFile(tw, bf.toString());
			}
		catch (IOException e1)
			{
			e1.printStackTrace();
			}
		}

	public static String linForAP(int numSubDiv, String channelName)
		{
		return "AP"+numSubDiv+"-"+channelName;
		}

	public static String linForLR(int numSubDiv, String channelName)
		{
		return "LR"+numSubDiv+"-"+channelName;
		}

	public static String linForDV(int numSubDiv, String channelName)
		{
		return "DV"+numSubDiv+"-"+channelName;
		}

	public static Lineage getRefLin(EvData data)
		{
		Map<EvPath, Lineage> lins = data.getIdObjectsRecursive(Lineage.class);
		for (Map.Entry<EvPath, Lineage> e : lins.entrySet())
			if (!e.getKey().getLeafName().startsWith("AP") && !e.getKey().getLeafName().startsWith("LR") && !e.getKey().getLeafName().startsWith("DV") && !e.getKey().getLeafName().startsWith("estcell"))
				{
				System.out.println("found lineage "+e.getKey());
				return e.getValue();
				}
		return null;
		}

	public static File fileForAP(EvData data, int numSubDiv, String channelName)
		{
		File datadir = data.io.datadir();
		return new File(datadir, "AP"+numSubDiv+"-"+channelName+"c");
		}

	public static File fileForLR(EvData data, int numSubDiv, String channelName)
		{
		File datadir = data.io.datadir();
		return new File(datadir, "LR"+numSubDiv+"-"+channelName+"c");
		}

	public static File fileForDV(EvData data, int numSubDiv, String channelName)
		{
		File datadir = data.io.datadir();
		return new File(datadir, "DV"+numSubDiv+"-"+channelName+"c");
		}

	public static void main(String arg[])
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
		
		if (arg.length>0)
			{
			doOne(new File(arg[0]), true);
			}
		else
			{
			List<File> list = new ArrayList<File>();
			for (File parent : new File[]
				{ new File("/Volumes/TBU_main06/ost4dgood"), })  //////////////////TODO pimai!!!!
				for (File f : parent.listFiles())
					if (f.getName().endsWith(".ost"))
						list.add(f);
		
			// Cheap concurrency
			Collections.shuffle(list);
		
			for (File f : list)
				if (new File(f, "tagDone4d.txt").exists())
					{
					try
						{
						doOne(f, false); // Force recalc
						}
					catch (Exception e)
						{
						e.printStackTrace();
						}
		
					try
						{
						Thread.sleep(2000);
						}
					catch (InterruptedException e)
						{
						}
		
					System.gc();
					}
				else
					System.out.println("Skipping: "+f);
		
			}
		
		System.exit(0);
		}

	
	
	}
