package util2.integrateExpression;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import util2.integrateExpression.IntExp.Integrator;
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
public class IntegratorAP implements Integrator
	{
	private int numSubDiv;
	private HashMap<EvDecimal, EvPixels> distanceMap = new HashMap<EvDecimal, EvPixels>();
	private Shell shell;
	private double[] sliceExp; //Must be double for values to fit
	private int[] sliceVol;
	private NucLineage lin = new NucLineage();
	private String newLinName;
	
	public Map<EvDecimal, Double> bg = new HashMap<EvDecimal, Double>();
	public TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure;
	private boolean updateBG = true; // variable if to update bg
	
	private double curBgInt = 0;
	private int curBgVol = 0;
	
	public IntegratorAP(IntExp integrator, String newLinName, int numSubDiv, Map<EvDecimal, Double> bg)
		{
		this.numSubDiv = numSubDiv;
		this.newLinName = newLinName;
		sliceExp = new double[numSubDiv];
		sliceVol = new int[numSubDiv];
		
		//Use pre-calculated value for BG
		if (bg!=null)
			{
			this.bg = bg;
			updateBG = false;
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
		curBgInt = 0;
		curBgVol = 0;
		}
	
	public void integrateImage(IntExp integrator)
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
					int sliceNum = (int) (len*numSubDiv); // may need to bound in addition
					sliceExp[sliceNum] += integrator.pixelsLine[i];
					sliceVol[sliceNum]++;
					}
				else
					{
					// Measure background. It's all pixels outside the embryo
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
			bg.put(integrator.frame, curBgInt/curBgVol);
	
		// Store pattern in lineage
		for (int i = 0; i<numSubDiv; i++)
			{
			double avg = (double) sliceExp[i]/(double) sliceVol[i] - bg.get(integrator.frame);
			avg /= integrator.expTime;
	
			NucLineage.Nuc nuc = lin.getCreateNuc("_slice"+i);
			NucExp exp = nuc.getCreateExp(integrator.expName);
			exp.level.put(integrator.frame, avg);
			}
	
		}
	
	public void done(IntExp integrator,
			TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
		{
		// Set override start and end times
		for (int i = 0; i<numSubDiv; i++)
			{
			NucLineage.Nuc nuc = lin.getCreateNuc("_slice"+i);
			nuc.overrideStart = integrator.ch.imageLoader.firstKey();
			nuc.overrideEnd = integrator.ch.imageLoader.lastKey();
			}
/*
		System.out.println("---------------------------------------");
		
		System.out.println();
		System.out.println("Removed bg: "+bg);
		
		System.out.println();
		System.out.println("Before norm: "+lin.getCreateNuc("_slice0").exp.get(integrator.expName).level);
*/
		// Normalization is needed before exposure correction to make sure the
		// threshold for
		// detecting jumps always works
		ExpUtil.normalizeSignal(lin, integrator.expName, ExpUtil.getSignalMax(lin, integrator.expName), 0, 1);

		/*
		System.out.println();
		System.out.println("After norm: "+lin.getCreateNuc("_slice0").exp.get(integrator.expName).level);
		*/
		
		if (correctedExposure!=null)
			{
			//Used for T-expression
			/*
			System.out.println();
			System.out.println("Exp correction: "+correctedExposure);
			*/
			
			ExpUtil.correctExposureChange(correctedExposure, lin,	integrator.expName);
			}
		else
			{
			//Used for AP
			this.correctedExposure = ExpUtil.correctExposureChange(
					integrator.imset, lin, integrator.expName, integrator.channelName, new TreeSet<EvDecimal>(
							integrator.ch.imageLoader.keySet()));
			}
		
		/*
		System.out.println();
		System.out.println("After exp correct: "+lin.getCreateNuc("_slice0").exp.get(integrator.expName).level);
*/
	
		// This is only for the eye
		double sigMax = ExpUtil.getSignalMax(lin, integrator.expName);
		double sigMin = ExpUtil.getSignalMin(lin, integrator.expName);
		ExpUtil.normalizeSignal(lin, integrator.expName, sigMax, sigMin, 1);
	
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