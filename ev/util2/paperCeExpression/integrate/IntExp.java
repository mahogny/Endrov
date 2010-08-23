/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.integrate;

import java.io.File;
import java.io.IOException;
import java.util.*;



import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.*;
import endrov.imageset.*;
import endrov.nuc.*;
import endrov.util.*;

//TODO all warnings into some file

/**
 * All integrations are done at the same time to reduce disk I/O. Images not
 * needed are discarded through lazy evaluation. Background is subtracted. This
 * only affects the first frame, but sets a proper 0. COULD put background into
 * first frame of correction instead. Is this better? The cubemap contains too
 * much data for the lineage window. 20x20x20=8000 tracks, impossible. also EATS
 * space, especially if stored as XML. So, it has to be stored as images.
 * Originally as channel "mod-GFP". Overlap is not possible due to
 * reorientation. Frame-time remap will not be done until assembly.
 * 
 * @author Johan Henriksson
 */
public class IntExp
	{

	/**
	 * Integrator of expression. Integrating every type at the same time saves a lot of I/O
	 * 
	 * @author Johan Henriksson
	 */
	public interface Integrator
		{
		public void integrateStackStart(IntExp images);

		public void integrateImage(IntExp images);

		public void integrateStackDone(IntExp images);
		}

	public static NucLineage refLin = null;
	public EvDecimal frame;
	public int curZint;
	public EvDecimal curZ;
	public EvStack stack;
	public EvImage im;
	public EvPixels pixels;
	public int[] pixelsLine;
	//public double expTime = 1; // For missing frames, use last frame
	public EvData data;
	public String expName;
	public String channelName;
	public EvChannel ch;
	public Imageset imset;

	public static void main(String arg[])
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		if (arg.length>0)
			{
			doOne(new File(arg[0]), true);
			}
		else
			{
			List<File> list = new ArrayList<File>();
			for (File parent : new File[]
				{ new File("/Volumes/TBU_main06/ost4dgood"), })
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
			boolean hasLineage = !data.getIdObjectsRecursive(NucLineage.class)
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

			EvData data = EvData.loadFile(f);

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
					new EvPath("im", channelName)))
				{
				System.out.println("Does not contain any channel! "+f);
				return false;
				}

			// Fixing time is not done now. (what about gnuplot?).
			// makes checking difficult if applied
			// => forced to apply when merging, and searching if searching in original
			// => output both frames and model time in gnuplot files?

			String newLinNameT = linForAP(1, channelName);
			String newLinNameAP = linForAP(numSubDiv, channelName);
			String newLinNameLR = linForLR(numSubDiv, channelName);
			String newLinNameDV = linForDV(numSubDiv, channelName);

			refLin=getRefLin(data);

			// Decide on integrators
			LinkedList<Integrator> integrators = new LinkedList<Integrator>();

			// boolean hasShell=!data.getIdObjects(Shell.class).isEmpty();

			//Order of integrators matters!
			IntExp integrator = new IntExp(data, expName, channelName);

			// AP-level expression
			//IntegratorAP intAP = new IntegratorAP(integrator, newLinNameAP,	numSubDiv, null);
			IntegratorSliceAP intAP = new IntegratorSliceAP(integrator, newLinNameAP,	numSubDiv, null);
			integrators.add(intAP);
			
			//T-level expression
			OldIntegratorAP intT = new OldIntegratorAP(integrator, newLinNameT, 1, intAP.bg);
			integrators.add(intT);

			// DV - requires an angle
			IntegratorSliceDV intDV = new IntegratorSliceDV(integrator, newLinNameDV, numSubDiv, intAP.bg);
			if (refLin!=null && intDV.setupCS(refLin))
				integrators.add(intDV);
			else
				intDV=null;

			// LR - requires an angle
			IntegratorSliceLR intLR = new IntegratorSliceLR(integrator, newLinNameLR, numSubDiv, intAP.bg);
			if (refLin!=null && intLR.setupCS(refLin))
				integrators.add(intLR);
			else
				intLR=null;

			// XYZ cube level expression
			IntegratorXYZ intXYZ = new IntegratorXYZ(integrator, newLinNameAP, numSubDiv, intAP.bg);
			if (refLin!=null && intXYZ.setupCS(refLin))
				integrators.add(intXYZ);
			else
				intXYZ = null;

			// Cell level expression if there is a lineage
			IntegratorCellClosest intC = null;
			if (refLin!=null)
				{
				if (channelName.equals("RFP"))
					{
					intC = new IntegratorCellClosest(integrator, refLin, intAP.bg);
					integrators.add(intC);
					}
				else
					{
					//Just superimpose the model, normalized!
					
					NucLineage newlin=ExpUtil.mapModelToRec(refLin);
					
					Imageset imset=data.getIdObjectsRecursive(Imageset.class).values().iterator().next();
					imset.metaObject.put("estcell", newlin);
					
					intC = new IntegratorCellClosest(integrator, newlin, intAP.bg);
					integrators.add(intC);
					}
				}
			
			//Disable integration
			/*intXYZ=null;
			intDV=null;
			intLR=null;
			intC=null;*/
			///
			
			// Run integrators
			integrator.doProfile(integrators);

			// Wrap up, store in OST
			// Use common correction factors for exposure
			intAP.done(integrator, null);
			storeCorrection(f, intAP.correctedExposure, intAP.bg);
			intT.done(integrator, intAP.correctedExposure);
			if(intDV!=null)
				intDV.done(integrator, intAP.correctedExposure);
			if(intLR!=null)
				intLR.done(integrator, intAP.correctedExposure);
			if (intXYZ!=null)
				intXYZ.done(integrator, intAP.correctedExposure);
			if (intC!=null)
				intC.done(integrator, intAP.correctedExposure);

			// Put integral in file for use by Gnuplot
			intAP.profileForGnuplot(integrator, fileForAP(data, numSubDiv, channelName));
			if(intDV!=null)
				intDV.profileForGnuplot(integrator, fileForDV(data, numSubDiv, channelName));
			if(intLR!=null)
				intLR.profileForGnuplot(integrator, fileForLR(data, numSubDiv, channelName));

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
	
	public static NucLineage getRefLin(EvData data)
		{
		Map<EvPath, NucLineage> lins = data.getIdObjectsRecursive(NucLineage.class);
		for (Map.Entry<EvPath, NucLineage> e : lins.entrySet())
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

	/**
	 * Lazily load images
	 */
	public void ensureImageLoaded()
		{
		if (pixels==null)
			{
			pixels = im.getPixels().getReadOnly(EvPixelsType.INT);
			pixelsLine = pixels.getArrayInt();
			}
		}

	public IntExp(EvData data, String expName, String channelName)
		{
		this.data = data;
		// this.newLinName=newLineName;
		this.expName = expName;
		this.channelName = channelName;
		imset = data.getObjects(Imageset.class).get(0);
		ch = imset.getChannel(channelName);

		if (ch==null)
			throw new RuntimeException("No such channel "+channelName);
		}

	/**
	 * Run all integrators
	 */
	public void doProfile(Collection<Integrator> ints)
		{
		doProfile(ints.toArray(new Integrator[] {}));
		}

	/**
	 * Run all integrators
	 */
	public void doProfile(Integrator... ints)
		{
		// For all frames
		System.out.println("num frames: "+ch.imageLoader.size());

		EvDecimal firstframe = ch.imageLoader.firstKey();
		EvDecimal lastFrame = ch.imageLoader.lastKey();
	
		//lastFrame=new EvDecimal("14400");  ///temp!!!
		
		if(refLin!=null)
			{
			NucLineage.Nuc nuc=refLin.nuc.get("lastframe");
			if(nuc!=null)
				{
				lastFrame=nuc.pos.firstKey();
				}
			}
		
		
		for (EvDecimal frame : ch.imageLoader.keySet())
			if(frame.lessEqual(lastFrame))
				{
				this.frame = frame;
	
				System.out.println(data+"    integrating frame "+frame+" / "+firstframe+" - "+lastFrame);
	
				for (Integrator i : ints)
					i.integrateStackStart(this);
	
				// For all z
				stack = ch.imageLoader.get(frame);
				EvImage[] imArr = stack.getImages();
				for (int az = 0; az<imArr.length; az++)
					{
					curZint = az;
					curZ = new EvDecimal(stack.transformImageWorldZ(az));
					// Load images lazily (for AP not really needed)
					im = imArr[az];
					pixels = null;
	
					for (Integrator i : ints)
						i.integrateImage(this);
					}
	
				for (Integrator i : ints)
					i.integrateStackDone(this);
	
				}

		}

	}
