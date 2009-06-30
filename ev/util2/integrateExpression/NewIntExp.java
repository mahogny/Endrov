package util2.integrateExpression;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.vecmath.Vector3d;

import endrov.coordinateSystem.CoordinateSystem;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.*;
import endrov.imageset.*;
import endrov.nuc.*;
import endrov.shell.Shell;
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
public class NewIntExp
	{

	public static void main2(String arg[])
		{
		EvLog.listeners.add(new StdoutLog());
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
		}

	public static void main(String arg[])
		{
		EvLog.listeners.add(new StdoutLog());
		EV.loadPlugins();

		//doOne(new File("/home/tbudev3/TB2141_070621_b.ost"));
		// doOne(new File("/Volumes2/TBU_main01/ost4dgood/TB2141_070621_b.ost/"));

		doOne(new File("/Volumes/TBU_main03/daemon/output/TB2111_090123.ost"));
	/*	
		for(File f:new File("/Volumes/TBU_main03/daemon/output").listFiles())
			if(f.getName().endsWith(".ost"))
				doOne(f);
*/		
		
		System.exit(0);
		}

	public static void doOne(File f)
		{
		EvData data = EvData.loadFile(f);

		int numSubDiv = 20;
		String channelName = "GFP";
		String expName = "exp"; // Neutral name

		// Fixing time is not done now. (what about gnuplot?).
		// makes checking difficult if applied
		// => forced to apply when merging, and searching if searching in original
		// => output both frames and model time in gnuplot files?

		String newLinNameT = linFor(1, channelName);
		String newLinNameAP = linFor(numSubDiv, channelName);

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

		// Decide on integrators
		LinkedList<Integrator> ints = new LinkedList<Integrator>();

//		boolean hasShell=!data.getIdObjects(Shell.class).isEmpty();
		
		NewIntExp integrator = new NewIntExp(data, expName, channelName);
		
		IntegratorAP intAP = new IntegratorAP(integrator, newLinNameAP, numSubDiv,
				null);
		IntegratorAP intT = new IntegratorAP(integrator, newLinNameT, 1, intAP.bg);

		IntegratorXYZ intXYZ = new IntegratorXYZ(integrator, newLinNameAP,
				numSubDiv, intAP.bg);

		ints.add(intAP);
		ints.add(intT);

		if (lin!=null&&intXYZ.setupCS(lin))
			ints.add(intXYZ);
		else
			intXYZ = null;

		IntegratorCell intC = null;
		if (lin!=null)
			{
			intC = new IntegratorCell(integrator, lin, intAP.bg);
			ints.add(intC);
			}

		// todo check which lin to use, add to list if one exists

		// Run integrators
		integrator.doProfile(ints);
		// integrator.doProfile(intAP,intT);

		// Wrap up, store in OST
		// Use common correction factors for exposure
		intAP.done(integrator, null);
		intT.done(integrator, intAP.correctedExposure);
		if (intXYZ!=null)
			intXYZ.done(integrator, intAP.correctedExposure);
		if (intC!=null)
			intC.done(integrator, intAP.correctedExposure);

		// Put integral in file for use by Gnuplot
		intAP.profileForGnuplot(integrator, fileFor(data, numSubDiv, channelName));

		// TODO
		// compression?

		data.saveData();

		}

	public static String linFor(int numSubDiv, String channelName)
		{
		return "AP"+numSubDiv+"-"+channelName;
		}

	public static File fileFor(EvData data, int numSubDiv, String channelName)
		{
		// TODO: later, use blobs or similar?
		File datadir = data.io.datadir();
		// return new File(datadir,"AP"+numSubDiv+"-"+channelName);
		return new File(datadir, "AP"+numSubDiv+"-"+channelName+"c"); // TODO temp
		}

	public interface Integrator
		{
		public void integrateStackStart(NewIntExp images);

		public void integrateImage(NewIntExp images);

		public void integrateStackDone(NewIntExp images);
		}

	public EvDecimal frame;
	public EvDecimal curZ;
	public EvStack stack;
	public EvImage im;
	public EvPixels pixels;
	public int[] pixelsLine;
	public double expTime = 1; // For missing frames, use last frame
	public EvData data;
	// public String newLinName;
	public String expName;
	public String channelName;
	public EvChannel ch;
	public Imageset imset;

	/**
	 * Lazily load images
	 */
	public void ensureImageLoaded()
		{
		if (pixels==null)
			{
			pixels = im.getPixels().getReadOnly(EvPixels.TYPE_INT);
			pixelsLine = pixels.getArrayInt();
			}
		}

	public NewIntExp(EvData data, String expName, String channelName)
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
		 * new EvDecimal("0")); EvPixels p=new EvPixels(EvPixels.TYPE_INT,10,10);
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

			System.out.println();
			System.out.println(data+"    frame "+frame+" / "+firstframe+" - "
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
			for (Map.Entry<EvDecimal, EvImage> eim : stack.entrySet())
				{
				curZ = eim.getKey();
				// Load images lazily (for AP not really needed)
				im = eim.getValue();
				// EvPixels pixels=null;
				// int[] pixelsLine=null;

				pixels = null;

				for(Integrator i : ints)
					i.integrateImage(this);

				}

			for(Integrator i : ints)
				i.integrateStackDone(this);

			}

		}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	public static class IntegratorAP implements Integrator
		{
		int numSubDiv;
		HashMap<EvDecimal, EvPixels> distanceMap = new HashMap<EvDecimal, EvPixels>();
		Shell shell;
		int[] sliceExp;
		int[] sliceVol;
		NucLineage lin = new NucLineage();;
		String newLinName;

		Map<EvDecimal, Double> bg = new HashMap<EvDecimal, Double>();
		TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure;
		boolean updateBG = true; // variable if to update bg

		double curBgInt = 0;
		int curBgVol = 0;

		public IntegratorAP(NewIntExp integrator, String newLinName, int numSubDiv,
				Map<EvDecimal, Double> bg)
			{
			this.numSubDiv = numSubDiv;
			this.newLinName = newLinName;
			if (bg!=null)
				{
				this.bg = bg;
				updateBG = false;
				}
			sliceExp = new int[numSubDiv];
			sliceVol = new int[numSubDiv];

			// TODO need to group lineage and shell. introduce a new object?
			integrator.imset.metaObject.put(newLinName, lin);
			// imset.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
			shell = integrator.imset.getIdObjectsRecursive(Shell.class).values()
					.iterator().next();

			integrator.imset.metaObject.put(newLinName, lin);

			// Virtual nuc for AP
			for (int i = 0; i<numSubDiv; i++)
				lin.getNucCreate("_slice"+i);
			}

		public void integrateStackStart(NewIntExp integrator)
			{
			curBgInt = 0;
			curBgVol = 0;
			}

		public void integrateImage(NewIntExp integrator)
			{
			integrator.ensureImageLoaded();

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
				lenMap = new EvPixels(EvPixels.TYPE_DOUBLE, integrator.pixels
						.getWidth(), integrator.pixels.getHeight());
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
							len = pos.sub(startpos).dot(dirvec)/(2*shell.major); // xy .
																																		// dirvecx =
																																		// cos(alpha)
																																		// ||xy||
																																		// ||dirvecx||
						else
							len = -1;
						lenMapArr[lineIndex+ax] = len;
						}
					}
				}

			// Integrate this area
			for (int y = 0; y<integrator.pixels.getHeight(); y++)
				{
				int lineIndex = integrator.pixels.getRowIndex(y);
				for (int x = 0; x<integrator.pixels.getWidth(); x++)
					{
					int i = lineIndex+x;
					double len = lenMapArr[i];
					if (len>-1)
						{
						int sliceNum = (int) (len*numSubDiv); // may need to bound in
																									// addition
						sliceExp[sliceNum] += integrator.pixelsLine[i];
						sliceVol[sliceNum]++;
						}
					else
						{
						// Measure background
						curBgInt += integrator.pixelsLine[i];
						curBgVol++;
						}
					}
				}
			}

		public void integrateStackDone(NewIntExp integrator)
			{
			// Store background
			if (updateBG)
				bg.put(integrator.frame, curBgInt/curBgVol);

			// Store pattern in lineage
			for (int i = 0; i<numSubDiv; i++)
				{
				double avg = (double) sliceExp[i]/(double) sliceVol[i]
						-bg.get(integrator.frame);
				avg /= integrator.expTime;

				NucLineage.Nuc nuc = lin.getNucCreate("_slice"+i);
				NucExp exp = nuc.getExpCreate(integrator.expName);
				exp.level.put(integrator.frame, avg);

				}

			}

		public void done(NewIntExp integrator,
				TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
			{
			// Set override start and end times
			for (int i = 0; i<numSubDiv; i++)
				{
				NucLineage.Nuc nuc = lin.getNucCreate("_slice"+i);
				nuc.overrideStart = integrator.ch.imageLoader.firstKey();
				nuc.overrideEnd = integrator.ch.imageLoader.lastKey();
				}

			// Normalization is needed before exposure correction to make sure the
			// threshold for
			// detecting jumps always works
			ExpUtil.normalizeSignal(lin, integrator.expName, ExpUtil.getSignalMax(
					lin, integrator.expName), 0, 1);

			if (correctedExposure!=null)
				{
				ExpUtil.correctExposureChange(correctedExposure, lin,
						integrator.expName);
				}
			else
				{
				this.correctedExposure = ExpUtil.correctExposureChange(
						integrator.imset, lin, integrator.expName, integrator.channelName, new TreeSet<EvDecimal>(
								integrator.ch.imageLoader.keySet()));
				}

			// This is only for the eye
			double sigMax = ExpUtil.getSignalMax(lin, integrator.expName);
			double sigMin = ExpUtil.getSignalMin(lin, integrator.expName);
			ExpUtil.normalizeSignal(lin, integrator.expName, sigMax, sigMin, 1);
			}

		/**
		 * Store profile as array on disk
		 */
		public void profileForGnuplot(NewIntExp integrator, File file)
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

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

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

	private static int min2(int a, int b)
		{
		return a<b ? a : b;
		}

	private static int max2(int a, int b)
		{
		return a>b ? a : b;
		}

	public static class IntegratorCell implements Integrator
		{
		NucLineage lin;

		Map<String, Double> expLevel;
		Map<String, Integer> nucVol;
		Map<NucSel, NucLineage.NucInterp> inter;
		Map<EvDecimal, Double> bg;

		public IntegratorCell(NewIntExp integrator, NucLineage lin,
				Map<EvDecimal, Double> bg)
			{
			this.lin = lin;
			this.bg = bg;
			ExpUtil.clearExp(lin, integrator.expName);
			ExpUtil.clearExp(lin, "CEH-5"); // TEMP
			}

		public void integrateStackStart(NewIntExp integrator)
			{
			expLevel = new HashMap<String, Double>();
			nucVol = new HashMap<String, Integer>();
			inter = lin.getInterpNuc(integrator.frame);
			}

		public void integrateImage(NewIntExp integrator)
			{
			double imageZw = integrator.curZ.doubleValue();

			// For all nuc
			for (Map.Entry<NucSel, NucLineage.NucInterp> e : inter.entrySet())
			// if(e.getKey().getRight().equals("ABarappaa"))
			// if(e.getKey().getRight().equals("AB"))
				{
				String nucName = e.getKey().snd();
				NucLineage.NucPos pos = e.getValue().pos;

				Double pr = projectSphere(pos.r, pos.z, imageZw);
				if (pr!=null)
					{
					int midSx = (int) integrator.stack.transformWorldImageX(pos.x);
					int midSy = (int) integrator.stack.transformWorldImageY(pos.y);
					int rS = (int) integrator.stack.scaleWorldImageX(pr);
					if (rS>0)
						{
						if (!expLevel.containsKey(nucName))
							{
							expLevel.put(nucName, 0.0);
							nucVol.put(nucName, 0);
							}

						integrator.ensureImageLoaded();

						// Integrate this area
						int sy = max2(midSy-rS, 0);
						int ey = min2(midSy+rS, integrator.pixels.getHeight());
						int sx = max2(midSx-rS, 0);
						int ex = min2(midSx+rS, integrator.pixels.getWidth());
						int area = 0;
						double exp = 0;
						for (int y = sy; y<ey; y++)
							{
							int lineIndex = integrator.pixels.getRowIndex(y);
							for (int x = sx; x<ex; x++)
								{
								int dx = x-midSx;
								int dy = y-midSy;
								if (dx*dx+dy*dy<rS*rS)
									{
									int v = integrator.pixelsLine[lineIndex+x];
									area++;
									exp += v;
									}
								}
							}

						// Sum up volume and area
						nucVol.put(nucName, nucVol.get(nucName)+area);
						expLevel.put(nucName, expLevel.get(nucName)+exp);
						}
					}
				}

			}

		public void integrateStackDone(NewIntExp integrator)
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
					double avg = expLevel.get(nucName)/vol-bg.get(integrator.frame);
					avg /= integrator.expTime;
					// System.out.println(nucName+" "+avg);
					NucExp exp = lin.nuc.get(nucName).getExpCreate(integrator.expName);
					if (lin.nuc.get(nucName).pos.lastKey().greaterEqual(integrator.frame)
							&&lin.nuc.get(nucName).pos.firstKey().lessEqual(integrator.frame))
						exp.level.put(integrator.frame, avg);
					}
				}
			}

		public void done(NewIntExp integrator,
				TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
			{

			// Use prior correction on this expression as well
			Double max1 = ExpUtil.getSignalMax(lin, integrator.expName);
			if (max1==null)
				System.out.println("max==null, there is no signal!");
			else
				{
				ExpUtil.normalizeSignal(lin, integrator.expName, max1, 0, 1);
				ExpUtil.correctExposureChange(correctedExposure, lin,
						integrator.expName);
				}

			}

		}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Cube overlay
	 */
	public static class IntegratorXYZ implements Integrator
		{
		int numSubDiv;
		HashMap<EvDecimal, Vector3i[][]> indexMap = new HashMap<EvDecimal, Vector3i[][]>(); // z->y,x
		int[][][] sliceExp; // z,y,x
		int[][][] sliceVol; // z,y,x
		NucLineage lin;

		Map<EvDecimal, Double> bg = new HashMap<EvDecimal, Double>();
		TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure;

		CoordinateSystem cs;

		public IntegratorXYZ(NewIntExp integrator, String newLinName,
				int numSubDiv, Map<EvDecimal, Double> bg)
			{
			this.numSubDiv = numSubDiv;
			this.bg = bg;

			// TODO need to group lineage and shell. introduce a new object?
			lin = new NucLineage();

			// Virtual nuc
			for (int i = 0; i<numSubDiv; i++)
				for (int j = 0; j<numSubDiv; j++)
					for (int k = 0; k<numSubDiv; k++)
						lin.getNucCreate("xyz_"+i+"_"+j+"_"+k);

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
			NucLineage.Nuc nucABa = refLin.nuc.get("ABa");
			NucLineage.Nuc nucABp = refLin.nuc.get("ABp");
			NucLineage.Nuc nucEMS = refLin.nuc.get("EMS");

			if (nucP2==null||nucABa==null||nucABp==null||nucEMS==null
					||nucP2.pos.isEmpty()||nucABa.pos.isEmpty()||nucABp.pos.isEmpty()
					||nucEMS.pos.isEmpty())
				{
				System.out
						.println("Does not have required 4-cell stage marked, will not produce cube");
				return false;
				}
			else
				{
				System.out.println("Will do XYZ");
				}

			Vector3d posP2 = nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy();
			Vector3d posABa = nucABa.pos.get(nucABa.pos.lastKey()).getPosCopy();
			Vector3d posABp = nucABp.pos.get(nucABp.pos.lastKey()).getPosCopy();
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

			// Enlarge by 20%
			cs = new CoordinateSystem();
			double scale = 1.35;
			cs.setFromTwoVectors(v1, v2, v1.length()*scale, v2.length()*scale, v2
					.length()
					*scale, mid);

			return true;
			}

		public void integrateStackStart(NewIntExp integrator)
			{
			// Zero out arrays
			sliceExp = new int[numSubDiv][numSubDiv][numSubDiv];
			sliceVol = new int[numSubDiv][numSubDiv][numSubDiv];
			}

		public void integrateImage(NewIntExp integrator)
			{
			integrator.ensureImageLoaded();

			EvChannel chIndexX = integrator.imset.getCreateChannel("indX");
			EvChannel chIndexY = integrator.imset.getCreateChannel("indY");
			EvChannel chIndexZ = integrator.imset.getCreateChannel("indZ");

			// Calculate index map lazily
			EvStack stackX=chIndexX.imageLoader.get(EvDecimal.ZERO);
			EvImage indX = chIndexX.getImageLoader(EvDecimal.ZERO, integrator.curZ);
			EvPixels pX;
			EvPixels pY;
			EvPixels pZ;
			if (indX==null)
				{
				indX = chIndexX.createImageLoader(EvDecimal.ZERO, integrator.curZ);
				stackX=chIndexX.imageLoader.get(EvDecimal.ZERO);
				EvImage indY = chIndexY.createImageLoader(EvDecimal.ZERO,	integrator.curZ);
				EvImage indZ = chIndexZ.createImageLoader(EvDecimal.ZERO,	integrator.curZ);
				EvStack stackY=chIndexY.imageLoader.get(EvDecimal.ZERO);
				EvStack stackZ=chIndexZ.imageLoader.get(EvDecimal.ZERO);
				int w = integrator.pixels.getWidth();
				int h = integrator.pixels.getHeight();
				pX = new EvPixels(EvPixels.TYPE_INT, w, h);
				pY = new EvPixels(EvPixels.TYPE_INT, w, h);
				pZ = new EvPixels(EvPixels.TYPE_INT, w, h);
				indX.setPixelsReference(pX);
				indY.setPixelsReference(pY);
				indZ.setPixelsReference(pZ);

				chIndexX.chBinning = chIndexY.chBinning = chIndexZ.chBinning = 4;
				stackX.binning = stackY.binning = stackZ.binning = 4;

				int[] lineX = pX.getArrayInt();
				int[] lineY = pY.getArrayInt();
				int[] lineZ = pZ.getArrayInt();

				// Calculate indices
				for (int ay = 0; ay<integrator.pixels.getHeight(); ay++)
					{
					for (int ax = 0; ax<integrator.pixels.getWidth(); ax++)
						{
						// Convert to world coordinates
						Vector3d pos = new Vector3d(integrator.stack.transformImageWorldX(ax),
								integrator.stack.transformImageWorldY(ay), integrator.curZ
										.doubleValue());

						Vector3d insys = cs.transformToSystem(pos);

						int cx = (int) ((insys.x+0.5)*numSubDiv);
						int cy = (int) ((insys.y+0.5)*numSubDiv);
						int cz = (int) ((insys.z+0.5)*numSubDiv);

						int index = pX.getPixelIndex(ax, ay);
						if (cx>=0&&cy>=0&&cz>=0&&cx<numSubDiv&&cy<numSubDiv&&cz<numSubDiv)
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
			else
				{
				pX = chIndexX.getImageLoader(EvDecimal.ZERO, integrator.curZ)
						.getPixels();
				pY = chIndexY.getImageLoader(EvDecimal.ZERO, integrator.curZ)
						.getPixels();
				pZ = chIndexZ.getImageLoader(EvDecimal.ZERO, integrator.curZ)
						.getPixels();
				}

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
		public void integrateStackDone(NewIntExp integrator)
			{
			// Store pattern in lineage
			for (int az = 0; az<numSubDiv; az++)
				for (int ay = 0; ay<numSubDiv; ay++)
					for (int ax = 0; ax<numSubDiv; ax++)
						{
						double curbg = bg.get(integrator.frame);
						double vol = sliceVol[az][ay][ax];
						double avg = vol==0 ? 0 : (double) sliceExp[az][ay][ax]/vol-curbg;
						avg /= integrator.expTime;

						NucLineage.Nuc nuc = lin.nuc.get("xyz_"+ax+"_"+ay+"_"+az);
						NucExp exp = nuc.getExpCreate(integrator.expName);
						exp.level.put(integrator.frame, avg);
						// System.out.println(exp.level);
						}

			}

		/**
		 * All frames processed
		 */
		public void done(NewIntExp integrator,
				TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
			{
			// Normalization is needed before exposure correction to make sure the
			// threshold for
			// detecting jumps always works
			ExpUtil.normalizeSignal(lin, integrator.expName, ExpUtil.getSignalMax(
					lin, integrator.expName), 0, 1);
			ExpUtil.correctExposureChange(correctedExposure, lin, integrator.expName);

			int binning = 16;

			// This is only for the eye
			double sigMax = ExpUtil.getSignalMax(lin, integrator.expName);
			double sigMin = ExpUtil.getSignalMin(lin, integrator.expName);
			ExpUtil.normalizeSignal(lin, integrator.expName, sigMax, sigMin, 255);

			// Store expression as a new channel
			EvChannel chanxyz = integrator.imset.getCreateChannel("XYZ");
			chanxyz.chBinning = binning;
			for (EvDecimal frame : lin.nuc.get("xyz_0_0_0").exp
					.get(integrator.expName).level.keySet())
				{
				System.out.println("frame "+frame);
				for (int az = 0; az<numSubDiv; az++)
					{
					EvImage evim = chanxyz.createImageLoader(frame, new EvDecimal(az));
					EvStack stack = chanxyz.imageLoader.get(frame);
					EvPixels p = new EvPixels(EvPixels.TYPE_INT, numSubDiv, numSubDiv);
					// EvPixels p=new EvPixels(EvPixels.TYPE_DOUBLE, numSubDiv,
					// numSubDiv);
					evim.setPixelsReference(p);
					stack.resX = stack.resY = 1;
					stack.binning = binning;
					int[] line = p.getArrayInt();
					// double[] line=p.getArrayDouble();
					for (int ay = 0; ay<numSubDiv; ay++)
						for (int ax = 0; ax<numSubDiv; ax++)
							{
							NucLineage.Nuc nuc = lin.nuc.get("xyz_"+ax+"_"+ay+"_"+az);
							line[p.getPixelIndex(ax, ay)] = (int) (double) nuc.exp
									.get(integrator.expName).level.get(frame);
							// line[p.getPixelIndex(x,
							// y)]=(double)nuc.exp.get(integrator.expName).level.get(frame);
							}
					}
				}

			// System.exit(0); ///////////

			}

		}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	}
