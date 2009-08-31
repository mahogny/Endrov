package util2.integrateExpression;

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
	 * @author Johan Henriksson
	 *
	 */
	public interface Integrator
		{
		public void integrateStackStart(IntExp images);
		public void integrateImage(IntExp images);
		public void integrateStackDone(IntExp images);
		}

	
	public EvDecimal frame;
	public EvDecimal curZ;
	public EvStack stack;
	public EvImage im;
	public EvPixels pixels;
	public int[] pixelsLine;
	public double expTime = 1; // For missing frames, use last frame
	public EvData data;
	public String expName;
	public String channelName;
	public EvChannel ch;
	public Imageset imset;
	
	
	
	
/*
	public static void main2(String arg[])
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		EvParallel.map_(Arrays.asList(new File("/Volumes2/TBU_main01/ost4dgood")
				.listFiles()), new EvParallel.FuncAB<File, Object>()
			{
				public Object func(File f)
					{
					if (f.getName().endsWith(".ost"))
						{
						EvData data = EvData.loadFile(f);
						if (!data.getObjects(Imageset.class).isEmpty())
							doOne(f);
						}
					return null;
					}
			});

		System.exit(0);
		}*/

	public static void main(String arg[])
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		//doOne(new File("/home/tbudev3/TB2141_070621_b.ost"));
		// doOne(new File("/Volumes2/TBU_main01/ost4dgood/TB2141_070621_b.ost/"));
//		doOne(new File("/Volumes/TBU_main03/daemon/output/TB2111_090123.ost"));
		
		for(File parent:new File[]{
				new File("/Volumes/TBU_main01/ost4dgood"),
				new File("/Volumes/TBU_main02/ost4dgood"),
				new File("/Volumes/TBU_main03/ost4dgood"),
				new File("/Volumes/TBU_main04/ost4dgood"),
		})
			for(File f:parent.listFiles())
				if(f.getName().endsWith(".ost"))
					{
					if(new File(f,"tagDone4d.txt").exists())
						doOne(f);
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

		Map<EvPath,EvChannel> obs=data.getIdObjectsRecursive(EvChannel.class);
		boolean toInclude=false;

		for(EvPath p:obs.keySet())
			if(p.getLeafName().equals("GFP"))
				toInclude=true;

		if(toInclude)
			{
			boolean hasShell=!data.getIdObjectsRecursive(EvChannel.class).isEmpty();
			boolean hasLineage=!data.getIdObjectsRecursive(NucLineage.class).isEmpty();
			System.out.println("Include: "+f+"    "+(hasShell?"shell":"")+"    "+(hasLineage?"lineage":""));
			}
			
		}

	public static void doOne(File f)
		{
		File ftagCalcDone=new File(f, "tagCalcDone4d.txt");
		
		if(ftagCalcDone.exists())
			{
			System.out.println("Already done: "+f);
			return;
			}
		
		
		EvData data = EvData.loadFile(f);

		System.out.println("Doing: "+f);
		
		
		int numSubDiv = 20;
		String channelName = "GFP";
		String expName = "exp"; // Neutral name

		// Fixing time is not done now. (what about gnuplot?).
		// makes checking difficult if applied
		// => forced to apply when merging, and searching if searching in original
		// => output both frames and model time in gnuplot files?

		String newLinNameT = linForAP(1, channelName);
		String newLinNameAP = linForAP(numSubDiv, channelName);

		// Not the optimal way of finding the lineage
		NucLineage lin = null;
		Map<EvPath, NucLineage> lins = data.getIdObjectsRecursive(NucLineage.class);
		for (Map.Entry<EvPath, NucLineage> e : lins.entrySet())
			// if(!e.getKey().getLeafName().equals(newLinNameT) &&
			// !e.getKey().getLeafName().equals(newLinNameAP))
			if (!e.getKey().getLeafName().startsWith("AP"))
				{
				System.out.println("found lineage "+e.getKey());
				lin = e.getValue();
				System.out.println(lin);
				}
		// lin=null;

		//Decide on integrators
		LinkedList<Integrator> ints = new LinkedList<Integrator>();

//		boolean hasShell=!data.getIdObjects(Shell.class).isEmpty();
		
		IntExp integrator = new IntExp(data, expName, channelName);

		//AP-level expression. A single slice gives expression over time
		IntegratorAP intAP = new IntegratorAP(integrator, newLinNameAP, numSubDiv, null);
		IntegratorAP intT = new IntegratorAP(integrator, newLinNameT, 1, intAP.bg);
		ints.add(intAP);
		ints.add(intT);

		//XYZ cube level expression
		IntegratorXYZnew2 intXYZ = new IntegratorXYZnew2(integrator, newLinNameAP,
				numSubDiv, intAP.bg);
		if (lin!=null && intXYZ.setupCS(lin))
			ints.add(intXYZ);
		else
			intXYZ = null;

		
		//Cell level expression if there is a lineage 
		//TODO: no! check if the lineage is complete enough
		IntegratorCell intC = null;
		if(lin!=null)
			{
			intC = new IntegratorCell(integrator, lin, intAP.bg);
			ints.add(intC);
			}

		// TODO check which lin to use, add to list if one exists

		// Run integrators
		integrator.doProfile(ints);

		// Wrap up, store in OST
		// Use common correction factors for exposure
		intAP.done(integrator, null);
		intT.done(integrator, intAP.correctedExposure);
		if (intXYZ!=null)
			intXYZ.done(integrator, intAP.correctedExposure);
		if (intC!=null)
			intC.done(integrator, intAP.correctedExposure);

		// Put integral in file for use by Gnuplot
		intAP.profileForGnuplot(integrator, fileForAP(data, numSubDiv, channelName));

		System.out.println("Saving OST");
		data.saveData();
		System.out.println("ok");

		try
			{
			ftagCalcDone.createNewFile();
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

	public static File fileForAP(EvData data, int numSubDiv, String channelName)
		{
		// TODO: later, use blobs or similar?
		File datadir = data.io.datadir();
		// return new File(datadir,"AP"+numSubDiv+"-"+channelName);
		return new File(datadir, "AP"+numSubDiv+"-"+channelName+"c"); // TODO temp
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

		/*
		 * //Test: write image EvChannel ch=imset.getCreateChannel("XYZ"); EvImage
		 * evim=ch.createImageLoader(imset.getChannel("GFP").imageLoader.firstKey(),
		 * new EvDecimal("0")); EvPixels p=new EvPixels(EvPixelsType.TYPE_INT,10,10);
		 * evim.setPixelsReference(p); data.saveData(); System.exit(0);
		 */

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
		System.out.println("num frames: "
				+imset.getChannel(channelName).imageLoader.size());
		EvDecimal firstframe = ch.imageLoader.firstKey();
		EvDecimal lastFrame = ch.imageLoader.lastKey();
		for (EvDecimal frame : ch.imageLoader.keySet())
		// if(frame.less(new EvDecimal("30000")) && frame.greater(new
		// EvDecimal("29000")))
			{
			this.frame = frame;

			//System.out.println();
			System.out.println(data+"    integrating frame "+frame+" / "+firstframe+" - "
					+lastFrame);

			// Get exposure time
			//String sExpTime = imset.getMetaFrame(frame).get("exposuretime");
			String sExpTime = imset.getChannel("GFP").getMetaFrame(frame).get("exposuretime");
			if (sExpTime!=null)
				expTime = Double.parseDouble(sExpTime);
			else
				System.out.println("No exposure time, frame "+frame);

			for (Integrator i : ints)
				i.integrateStackStart(this);

			// For all z
			stack=ch.imageLoader.get(frame);
			EvImage[] imArr=stack.getImages();
			for(int az=0;az<imArr.length;az++)
			//for (Map.Entry<EvDecimal, EvImage> eim : stack.entrySet())
				{
				curZ = new EvDecimal(stack.transformImageWorldZ(az));//eim.getKey();
				// Load images lazily (for AP not really needed)
				im = imArr[az]; 
				pixels = null;

				for(Integrator i : ints)
					i.integrateImage(this);

				}

			for(Integrator i : ints)
				i.integrateStackDone(this);

			}

		}



	}
