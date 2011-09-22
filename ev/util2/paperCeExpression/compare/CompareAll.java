/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.compare;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.jdom.Document;
import org.jdom.Element;

import util2.paperCeExpression.IntegrateAllExp;
import util2.paperCeExpression.collectData.PaperCeExpressionUtil;
import util2.paperCeExpression.profileRenderer.RenderHTML;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowColocalization.ColocCoefficients;
import endrov.frameTime.FrameTime;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.lineage.Lineage;
import endrov.lineage.LineageExp;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;
import endrov.util.EvListUtil;
import endrov.util.EvParallel;
import endrov.util.EvXmlUtil;
import endrov.util.ProgressHandle;
import endrov.util.Tuple;

/**
 * Pairwise comparison of recordings
 * @author Johan Henriksson
 *
 */
public class CompareAll
	{
	public final static String expName="exp";
	
	
	private final static int imageMaxTime=100; //Break down to 100 time points

	
	//In the end, we can choose to not compare all time points
	private final static int imageStartTime=0; //unused
	private final static int imageEndTime=60;   //must be less than max

	public final static File outputBaseDir=new File("/home/tbudev3/expsummary-"+imageStartTime+"-"+imageEndTime);

	public final static File cachedValuesFileT=new File(outputBaseDir,"comparisonT.xml");
	public final static File cachedValuesFileAP=new File(outputBaseDir,"comparisonAP.xml");
	public final static File cachedValuesFileDV=new File(outputBaseDir,"comparisonDV.xml");
	public final static File cachedValuesFileLR=new File(outputBaseDir,"comparisonLR.xml");
	public final static File cachedValuesFileXYZ=new File(outputBaseDir,"comparisonXYZ.xml");
	public final static File cachedValuesFileSS=new File(outputBaseDir,"comparisonSS.xml");

	/**
	 * Normalize time between recordings
	 */
	public static FrameTime buildFrametime(Lineage coordLin)
		{
		//Fit model time using a few markers
		//Times must be relative to a sane time, such that if e.g. venc is missing, linear interpolation still makes sense
		FrameTime ft=new FrameTime();
		//System.out.println("has nucs: "+coordLin.nuc.keySet());
		
		Lineage.Particle nucABa=coordLin.particle.get("ABa");
		if(nucABa!=null)
			ft.add(nucABa.pos.firstKey(), new EvDecimal("0").multiply(imageMaxTime));
		else
			{
			//Best possible replacement, almost same time
			Lineage.Particle nucEMS=coordLin.particle.get("EMS");
			if(nucEMS!=null)
				ft.add(nucEMS.pos.firstKey(), new EvDecimal("0").multiply(imageMaxTime));
			
			}

		Lineage.Particle nucGast=coordLin.particle.get("gast"); //Gastrulation
		if(nucGast!=null)
			ft.add(nucGast.pos.firstKey(), new EvDecimal("0.1").multiply(imageMaxTime));

		Lineage.Particle nucVenc=coordLin.particle.get("venc"); //Ventral enclosure
		if(nucVenc!=null)
			ft.add(nucVenc.pos.firstKey(), new EvDecimal("0.43").multiply(imageMaxTime));

		Lineage.Particle nuc2ft=coordLin.particle.get("2ftail"); //2-fold tail
		if(nuc2ft!=null)
			ft.add(nuc2ft.pos.firstKey(), new EvDecimal("0.54").multiply(imageMaxTime));

		Lineage.Particle nucMSppapp=coordLin.particle.get("MSppapp"); //MSppapp
		if(nucMSppapp!=null)
			ft.add(nucMSppapp.pos.firstKey(), new EvDecimal("0.27").multiply(imageMaxTime));

		//System.out.println("ftmap "+ft.mapFrame2time);
		
		//times from BC10075_070606
		// "go to frame" seems buggy
		// ABa 3h1m10s      10870     0
		// gast 4h7m40s     14860     0.1
		// venc 7h49m20s    28160     0.43
		// 2ftail 8h59m20s  32360     0.54
		
		//to be used for the model since there is no gast nor venc		
		// MSppapp 0.27
		
		//System.out.println("should be 0: "+ft.interpolateTime(nucABa.pos.firstKey()).doubleValue());
		//System.out.println("should be 0: "+ft.interpolateTime(nuc2ft.pos.firstKey()).doubleValue());
		
		return ft;
		}
	
	
	/**
	 * Coloc calculation requires two images that can overlap. Generate these from the AP or T lineage
	 */
	public static double[][] apToArray(EvData data, String newLinName, String expName, Lineage coordLin)
		{
		Imageset imset = data.getObjects(Imageset.class).get(0);
		Lineage lin = null;

		//Find lineage
		for(Map.Entry<EvPath, Lineage> e:imset.getIdObjectsRecursive(Lineage.class).entrySet())
			{
			if(e.getKey().getLeafName().equals(newLinName))
				{
				lin=e.getValue();
				break;
				}
			}
		if(lin==null)
			throw new RuntimeException("No lineage "+newLinName);
		
		//Autodetect number of subdivisions
		int numSubDiv=0;
		for(String nn:lin.particle.keySet())
			if(nn.startsWith("_slice"))
				{
				int curnum=Integer.parseInt(nn.substring("_slice".length()));
				numSubDiv=Math.max(curnum+1,numSubDiv);
				}
			else
				System.out.println("Strange exp: "+nn);
		
		double[][] image=new double[imageMaxTime][];//[numSubDiv];
		
		Lineage.Particle refNuc=lin.particle.get("_slice0");
		LineageExp expressionPattern=refNuc.exp.get(expName);
		if(expressionPattern==null)
			return image;
		
		FrameTime ft=buildFrametime(coordLin);
		
		//Fill in image
		int lastTime=0;
		for (EvDecimal frame : expressionPattern.level.keySet())
			{
			//Map to image
			int time=(int)ft.mapFrame2Time(frame).doubleValue();
			if(time<0)
				time=0;
			else if(time>=imageMaxTime)
				break;
			
			//For each slice
			image[time]=new double[numSubDiv];
			for (int i = 0; i<numSubDiv; i++)
				{
				Lineage.Particle nuc = lin.particle.get("_slice"+i);
				LineageExp nexp = nuc.exp.get(expName);
				Double level = nexp.level.get(frame);
				for(int y=lastTime;y<time+1;y++)
					image[time][i]=level;
				}
			lastTime=time;
			}
		
		//If it doesn't go far enough, the rest of the arrays will be null.
		//The first values will be a replica of the first frame; should seldom
		//be a problem
		
		return image;
		}
	
	public static double channelAverageDt(EvChannel chan)
		{
		return chan.getLastFrame().subtract(chan.getFirstFrame()).doubleValue()/chan.getFrames().size();
		}
	
	
	
	/**
	 * Coloc over XYZ
	 */
	public static ColocCoefficients colocXYZ(ProgressHandle progh, EvData dataA, EvData dataB, Lineage coordLinA, Lineage coordLinB, String chanNameA, String chanNameB)
		{
		Imageset imsetA = dataA.getObjects(Imageset.class).get(0);
		Imageset imsetB = dataB.getObjects(Imageset.class).get(0);
		
		FrameTime ftA=buildFrametime(coordLinA);
		FrameTime ftB=buildFrametime(coordLinB);

		if(ftA.getNumPoints()<2 || ftB.getNumPoints()<2)
			{
			//Bad data survival
			System.out.println("!!!!! too few timepoints for XYZ comparison "+ftA.getNumPoints() + "  "+ ftB.getNumPoints());
			return new ColocCoefficients();
			}
		
		EvChannel chanA=(EvChannel)imsetA.getChild(chanNameA); 
		EvChannel chanB=(EvChannel)imsetB.getChild(chanNameB);

		//Bad data survival
		if(chanA==null)
			{
			System.out.println("!!!!! missing channel in A: "+chanNameA);
			return new ColocCoefficients();
			}
		if(chanB==null)
			{
			System.out.println("!!!!! missing channel in B: "+chanNameB);
			return new ColocCoefficients();
			}
		
		//Figure out how many steps to take
		int numSteps=100;
		
		//Compare channels
		ColocCoefficients coloc=new ColocCoefficients();
		int cnt=0;
		for(double time=0;time<imageEndTime;time+=imageMaxTime/(double)numSteps)   
			{
			//Corresponding frames
			EvDecimal frameA=ftA.mapTime2Frame(new EvDecimal(time));
			EvDecimal frameB=ftB.mapTime2Frame(new EvDecimal(time));
			
			if(chanA.getFrames().isEmpty())
				throw new RuntimeException("No images in channel from A");
			if(chanB.getFrames().isEmpty())
				throw new RuntimeException("No images in channel from B");
			
			//If outside range, do not bother with this time point
			if(frameA.less(chanA.getFirstFrame()) || frameA.greater(chanA.getLastFrame()) ||
					frameB.less(chanB.getFirstFrame()) || frameB.greater(chanB.getLastFrame()))
				continue;
			
			//Use closest frame in each
			EvStack stackA=chanA.getStack(progh, chanA.closestFrame(frameA));
			EvStack stackB=chanB.getStack(progh, chanB.closestFrame(frameB));

			//Compare each slice. Same number of slices since it has been normalized
			if(stackA.getDepth()!=stackB.getDepth())
				throw new RuntimeException("Different number of slices in Z from frames "+frameA+" vs "+frameB+"    --    "+stackA.getDepth()+" vs "+stackB.getDepth());
			int numz=stackA.getDepth();
			for(int i=0;i<numz;i++)
				{
				EvPixels pA=stackA.getInt(i).getPixels(progh);
				EvPixels pB=stackB.getInt(i).getPixels(progh);
				if(pA==null || pB==null)
					System.out.println("Null pixels at frame "+frameA+" vs "+frameB);
				if(pA.getType()==EvPixelsType.FLOAT && pB.getType()==EvPixelsType.FLOAT)
					{
					//Optimized case
					float[] arrA=pA.getArrayFloat();
					float[] arrB=pB.getArrayFloat();
					coloc.add(arrA, arrB);
					}
				else
					{
					//General case
					double[] arrA=pA.convertToDouble(true).getArrayDouble();
					double[] arrB=pB.convertToDouble(true).getArrayDouble();
					coloc.add(arrA, arrB);
					}
				}
			cnt++;
			}
		System.out.println("Num xyz compared: "+cnt);
		
		return coloc;
		}
	
	
	/**
	 * Code from ImageJ, fire LUT.
	 * should if possible use the same as gnuplot
	 * @param reds
	 * @param greens
	 * @param blues
	 * @return
	 */
	/*
	private int fire(byte[] reds, byte[] greens, byte[] blues) 
		{
		int[] r = {0,0,1,25,49,73,98,122,146,162,173,184,195,207,217,229,240,252,255,255,255,255,255,255,255,255,255,255,255,255,255,255};
		int[] g = {0,0,0,0,0,0,0,0,0,0,0,0,0,14,35,57,79,101,117,133,147,161,175,190,205,219,234,248,255,255,255,255};
		int[] b = {0,61,96,130,165,192,220,227,210,181,151,122,93,64,35,5,0,0,0,0,0,0,0,0,0,0,0,35,98,160,223,255};
		for (int i=0; i<r.length; i++) 
			{
			reds[i] = (byte)r[i];
			greens[i] = (byte)g[i];
			blues[i] = (byte)b[i];
			}
		return r.length;
		}
	*/
	

	
	/**
	 * Generate overview graph for XYZ expression. Graph size is fixed so it throws out a lot of information.
	 * This is why this code is separate from coloc analysis
	 */
	public static void fancyGraphXYZ(ProgressHandle progh, EvData dataA, Lineage coordLinA, File outputFile, String chanNameA) throws IOException
		{
		Imageset imsetA = dataA.getObjects(Imageset.class).get(0);
		
		FrameTime ftA=buildFrametime(coordLinA);

		//Bad data survival
		if(ftA.getNumPoints()<2)
			{
			System.out.println("Cannot make XYZ graph");
			return;
			}
		else
			System.out.println("Making XYZ summary file");
		
		EvChannel chanA=(EvChannel)imsetA.getChild(chanNameA); 

		//Graph size is fixed
		int numSteps=50; //?
		
		int xyzSize=20;
		
		//xyzSize x xyzSize xyzSize columns
		BufferedImage bim=new BufferedImage((xyzSize+2)*xyzSize, numSteps*(xyzSize+2), BufferedImage.TYPE_3BYTE_BGR);
		
		//Compare channels
		for(int time=0;time<numSteps;time++)
			{
			//Corresponding frame
			EvDecimal modelTime=new EvDecimal(time*imageMaxTime/(double)numSteps);
			System.out.println("Modeltime: "+modelTime);
			EvDecimal frameA=ftA.mapTime2Frame(modelTime);
			
			//If outside range, stop calculating
			if(frameA.less(chanA.getFirstFrame()) || frameA.greater(chanA.getLastFrame()))
				continue;
						
			//Use closest frame
			EvDecimal closestFrameA=chanA.closestFrame(frameA);
			EvStack stackA=chanA.getStack(progh, closestFrameA);

			System.out.println("doing frame "+closestFrameA);

			//Compare each slice. Same number of slices since it has been normalized
			int numz=stackA.getDepth();
			if(numz!=xyzSize)
				System.out.println("---------------------------------------------------------------- wtf. numz "+numz+"  should be "+xyzSize+"  for frame: "+closestFrameA+"  channameA "+chanNameA);
			for(int cz=0;cz<xyzSize;cz++)
				{
				EvPixels p=stackA.getInt(cz).getPixels(progh).convertToDouble(true);
				double[] inarr=p.getArrayDouble();
				double arrmin=getMin(inarr);
				double arrmax=getMax(inarr);
				
				WritableRaster raster=bim.getRaster();
				for(int ay=0;ay<xyzSize;ay++)
					for(int ax=0;ax<xyzSize;ax++)
						{
						double val=(inarr[ay*p.getWidth()+ax]-arrmin)/(arrmax-arrmin);
						if(val>1)
							System.out.println("val: val");
						double scale=255;
						raster.setPixel(cz*(xyzSize+2)+ax, (xyzSize+2)*time+ay, new double[]{scale*Math.sin(2*Math.PI*val),scale*val*val*val,scale*Math.sqrt(val)}); //bgr
						/**
						 * gnuplot palette equation:
						 * rgb: 
						 * sqrt(x) 
						 * x³
						 * sin(360x)
						 */
						}
				}
			}
		
		ImageIO.write(bim, "png", outputFile);
		System.out.println("wrote "+outputFile);
		}
	
	private static double getMin(double[] arr)
		{
		Double ret=null;
		for(double d:arr)
			if(ret==null || d<ret)
				ret=d;
		return ret;
		}
	
	private static double getMax(double[] arr)
		{
		Double ret=null;
		for(double d:arr)
			if(ret==null || d>ret)
				ret=d;
		return ret;
		}
	
	public static boolean ensureCalculated(File f)
		{
		return IntegrateAllExp.doOne(f,false);   //Does not force recalculation
		}
	
	public static Map<Tuple<File,File>, ColocCoefficients> loadCache(Set<File> datas, File cachedValuesFile)
		{
		//Read past calculated values from disk if they exist
		Map<Tuple<File,File>, ColocCoefficients> comparison=new HashMap<Tuple<File,File>, ColocCoefficients>();
		if(cachedValuesFile.exists())
			{
			System.out.println("Read stats calculated before");
			try
				{
				Document doc=EvXmlUtil.readXML(cachedValuesFile);
				Element root=doc.getRootElement();
				for(Object o:root.getChildren())
					{
					Element e=(Element)o;
					File fa=new File(e.getAttributeValue("fa"));
					File fb=new File(e.getAttributeValue("fb"));
					if(datas==null || (datas.contains(fa) && datas.contains(fb)))
						{
						ColocCoefficients c=new ColocCoefficients();
						c.fromXML(e);
						comparison.put(Tuple.make(fa, fb), c);
						}
					}
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		return comparison;
		}
	
	public static <E> Collection<E> randomOrder(Collection<E> in)
		{
		List<E> out=new ArrayList<E>(in);
		Collections.shuffle(out);
		return out;
		}
	
	/**
	 * Calculate the colocalization given two processed AP images
	 */
	public static ColocCoefficients colocSliceTime(double[][] imA, double[][] imB)
		{
		ColocCoefficients coeff=new ColocCoefficients();
		for(int i=0;i<imageEndTime;i++)
			if(imA[i]!=null && imB[i]!=null)
				{
				double[] a=imA[i];
				double[] b=imB[i];
				for(int j=0;j<a.length;j++)
					{
					double aa=a[j], bb=b[j];
					if(!(Double.isInfinite(aa) || Double.isNaN(aa) || 
							Double.isInfinite(bb) || Double.isNaN(bb)))
						coeff.add(aa, bb);
					}
				}
				//coeff.add(imA[i], imB[i]);
		return coeff;
		}

	public static String getChanFor(EvData data)
		{
		Imageset imsetA = data.getObjects(Imageset.class).get(0);
		String chanNameA=imsetA.getChild("GFP")!=null ? "GFP" : "RFP";
		return chanNameA;
		}
	
	
	public static void doGraphsFor(File in, EvData data, String chanName)
		{
		//Check if XYZ summary generated. This should not be repeated as it is expensive! 
		//Only have to check the first image
		try
			{
			File outputFileXYZimageA=new File(new File(in,"data"),"expXYZ.png");
			if(!outputFileXYZimageA.exists())
				//fancyGraphXYZ(data, coordLineageFor(data), outputFileXYZimageA, chanName);
				fancyGraphXYZ(new ProgressHandle(), data, coordLineageFor(data), outputFileXYZimageA, "XYZ");
			}
		catch (IOException e1)
			{
			e1.printStackTrace();
			}
		
		//Slices: T
		try
			{
			double[][] imArray=apToArray(data, "AP"+1+"-"+chanName, expName, coordLineageFor(data));
			RenderHTML.toTimeImage(imArray, in, ""+in.getName());
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		//Slices: AP
		try
			{
			double[][] imArray=apToArray(data, "AP"+20+"-"+chanName, expName, coordLineageFor(data));
			RenderHTML.toSliceTimeImage(imArray, in, ""+in.getName(),"AP");
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		//Slices: LR
		try
			{
			double[][] imArray=apToArray(data, "LR"+20+"-"+chanName, expName, coordLineageFor(data));
			RenderHTML.toSliceTimeImage(imArray, in, ""+in.getName(),"LR");
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}

		//Slices: DV
		try
			{
			double[][] imArray=apToArray(data, "DV"+20+"-"+chanName, expName, coordLineageFor(data));
			RenderHTML.toSliceTimeImage(imArray, in, ""+in.getName(),"DV");
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	
	public static Lineage coordLineageFor(EvContainer data)
		{
		Lineage lin=null;
		//Find lineage
		for(Map.Entry<EvPath, Lineage> e:data.getIdObjectsRecursive(Lineage.class).entrySet())
			if(!e.getKey().getLeafName().startsWith("AP"))
				{
				if(e.getValue()==null)
					System.out.println("!!!!! lineage is null in tree");
				return e.getValue();
				}
		System.out.println("no lineage. got: "+data.getIdObjectsRecursive(Lineage.class).keySet());
		return null;
		}
	
	
	/**
	 * Store calculated values for the next time
	 */
	public static void storeCache(Map<Tuple<File,File>, ColocCoefficients> comparison, File cachedValuesFile)
		{
		try
			{
			Element root=new Element("comparison");

			for(Tuple<File,File> t:comparison.keySet())
				{
				Element e=new Element("c");
				e.setAttribute("fa", t.fst().toString());
				e.setAttribute("fb",t.snd().toString());
				comparison.get(t).toXML(e);
				root.addContent(e);
				}
			Document doc=new Document(root);
			cachedValuesFile.getParentFile().mkdirs();
			EvXmlUtil.writeXmlData(doc, cachedValuesFile);
			}
		catch (Exception e1)
			{
			e1.printStackTrace();
			}
		}
	
	
	public static void writeHTMLfromFiles(Set<File> datas, Map<Tuple<File,File>, ColocCoefficients> comparison, File targetFile, String profType)
		{
		//Turn into HTML
		try
			{
			//Set<String> titles=new TreeSet<String>();
			Map<String, File> titleMap=new TreeMap<String, File>(); 
			//Map<Tuple<String,String>,ColocCoefficients> map=new HashMap<Tuple<String,String>, ColocCoefficients>();
			for(File d:datas)
				titleMap.put(PaperCeExpressionUtil.getGeneName(d)+" ("+d.getName()+")",d);
				//titles.add(CompareSQL.getGeneName(d));
			//for(Tuple<File,File> t:comparison.keySet())
				//map.put(t, comparison.get(t));
			writeHTML(titleMap, comparison, targetFile, profType);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	
	public abstract static class TableWriter<E>
		{
		public StringBuffer sb=new StringBuffer();
		
		public TableWriter(Map<String,E> titles)
			{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);
			
			//First line with only titles
			sb.append("<tr>");
			sb.append("<td>&nbsp;</td>");
			for(String t:titles.keySet())
				{
				sb.append("<td valign=\"top\">");
				for(char c:t.toCharArray())
					{
					sb.append(c);
					sb.append("<br/>");
					}
				sb.append("</td>");
				}
			sb.append("</tr>\n");
			
			//All other lines
			for(String ta:titles.keySet())
				{
				//Title
				sb.append("<tr>");
				sb.append("<td>");
				sb.append(ta);
				sb.append("</td>");
				
				for(String tb:titles.keySet())
					{
					Double val=getValue(titles.get(ta), titles.get(tb));
					sb.append("<td>");
					if(val==null)
						sb.append("?");
					else if(val.isInfinite())
						sb.append("Inf");
					else if(val.isNaN())
						sb.append("NaN");
					else
						sb.append(""+nf.format(val));
					sb.append("</td>");
					}
				sb.append("</tr>\n");
				}
			}
		
		public abstract Double getValue(E ta, E tb);
		}

	
	/**
	 * Write HTML-files
	 * @param titles
	 * @param map (row, column)
	 * @param targetDir
	 */
	public static void writeHTML(Map<String, File> titles, final Map<Tuple<File,File>,ColocCoefficients> map, File targetDir, String profType) throws IOException
		{
		TableWriter<File> twPearson=new TableWriter<File>(titles){
			public Double getValue(File ta, File tb)
				{
				ColocCoefficients val=map.get(Tuple.make(ta,tb));
				return val==null ? null : val.getPearson();
				}};

		TableWriter<File> twManders1=new TableWriter<File>(titles){
		public Double getValue(File ta, File tb)
			{
			ColocCoefficients val=map.get(Tuple.make(ta,tb));
			return val==null ? null : val.getMandersX();
			}};

		TableWriter<File> twK1=new TableWriter<File>(titles){
		public Double getValue(File ta, File tb)
			{
			ColocCoefficients val=map.get(Tuple.make(ta,tb));
			return val==null ? null : val.getKX();
			}};
			
			

		String template=EvFileUtil.readFile(EvFileUtil.getFileFromURL(CompareAll.class.getResource("templateCompare.html")));
		EvFileUtil.writeFile(new File(targetDir,"table"+profType+"Pearson.html"),template.replace("COEFF","Pearson").replace("BODY", twPearson.sb.toString()));
		EvFileUtil.writeFile(new File(targetDir,"table"+profType+"Manders1.html"),template.replace("COEFF","Manders<sub>1</sub>").replace("BODY", twManders1.sb.toString()));
		EvFileUtil.writeFile(new File(targetDir,"table"+profType+"K1.html"),template.replace("COEFF","k<sub>1</sub>").replace("BODY", twK1.sb.toString()));

		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		new PaperCeExpressionUtil(); //Get password right away so it doesn't stop later
		
		
		/// temp
//		doGraphsFor(new File("/Volumes/TBU_main06/ost4dgood/ceh37_030306.ost"), EvData.loadFile(new File("/Volumes/TBU_main06/ost4dgood/ceh37_030306.ost")), "XYZ");
//		System.exit(0);
		/// temp
		
		
		//Do things in parallel. Not too many CPUs, case memory issues
		int numThread=EvParallel.numThread;
//		numThread=1;   ///////////////4 seems optimal
		numThread=4;
		System.out.println("Will use #threads  "+numThread);

		
		Set<String> argsSet=new HashSet<String>();
		for(String s:args)
			argsSet.add(s);
		
		//Find recordings to compare
		Set<File> datas=PaperCeExpressionUtil.getAnnotated(); 
		//Set<File> datas=IntExpFileUtil.getTestSet();
		
		//Use only test set?
		if(argsSet.contains("test"))
			datas=PaperCeExpressionUtil.getTestSet();
		
		//Use only calculated recordings?
		if(argsSet.contains("onlycalculated"))
			{
			System.out.println("---- only calculated");
			Set<File> datas2=new TreeSet<File>();
			for(File f:datas)
				if(IntegrateAllExp.isDone(f))
					datas2.add(f);
			datas=datas2;
			}
		
		System.out.println(datas);
		System.out.println("Number of annotated strains: "+datas.size());

		//Read past calculated values from disk 
		final Map<Tuple<File,File>, ColocCoefficients> comparisonT;
		final Map<Tuple<File,File>, ColocCoefficients> comparisonAP;
		final Map<Tuple<File,File>, ColocCoefficients> comparisonDV;
		final Map<Tuple<File,File>, ColocCoefficients> comparisonLR;
		final Map<Tuple<File,File>, ColocCoefficients> comparisonXYZ;
		if(!argsSet.contains("nocache"))
			{
			comparisonT=loadCache(datas, cachedValuesFileT);
			comparisonAP=loadCache(datas, cachedValuesFileAP);
			comparisonDV=loadCache(datas, cachedValuesFileDV);
			comparisonLR=loadCache(datas, cachedValuesFileLR);
			comparisonXYZ=loadCache(datas, cachedValuesFileXYZ);
			}
		else
			{
			comparisonT=new TreeMap<Tuple<File,File>, ColocCoefficients>();
			comparisonAP=new TreeMap<Tuple<File,File>, ColocCoefficients>();
			comparisonDV=new TreeMap<Tuple<File,File>, ColocCoefficients>();
			comparisonLR=new TreeMap<Tuple<File,File>, ColocCoefficients>();
			comparisonXYZ=new TreeMap<Tuple<File,File>, ColocCoefficients>();
			}

		if(argsSet.contains("cleanunused"))
			{
			//TODO!!!!
			}
		
		//Quick listing what must be calculated
		for(File f:datas)
			if(!IntegrateAllExp.isDone(f))
				System.out.println("---- need to do: "+f);

		
		numThread=4;
		System.out.println("for integration, Will use #threads  "+numThread);

		///////////////// Integrate signal in each recording //////////////
		EvParallel.map(numThread,new LinkedList<File>(datas), new EvParallel.FuncAB<File,Object>(){
			public Object func(File in)
				{
				//System.out.println("starting      "+in);
				try
					{
					boolean needGraph=!IntegrateAllExp.isDone(in);
					if(ensureCalculated(in) && needGraph)
						{
						System.out.println("Doing graph for "+in);
						EvData data=EvData.loadFile(in);
						String chanName=getChanFor(data);
						doGraphsFor(in, data, chanName);
						}
					//System.out.println("ending        "+in);
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				return null;
				}
		});
		
		final Object comparisonLock=new Object();
		
		
		///////////////// Compare each two recordings //////////////////////////////

		if(!argsSet.contains("nocomparison"))
			{

			numThread=2;
			System.out.println("for comparison, Will use #threads  "+numThread);

			System.out.println("Calculate pair-wise statistics");
			EvParallel.map_(numThread,new LinkedList<Tuple<File,File>>(EvListUtil.productSet(datas, datas)), new EvParallel.FuncAB<Tuple<File,File>,Object>(){
			public Object func(Tuple<File,File> key)
				{
				File fa=key.fst();
				File fb=key.snd();
				boolean containsKey;
				synchronized(comparisonLock)
					{
					containsKey=comparisonT.containsKey(key) && comparisonAP.containsKey(key) && comparisonXYZ.containsKey(key);
					//what about the other ones?
					}

				//Check if cached calculation does not exist
				if(!containsKey)
					{
					try
						{


						System.out.println("doing "+fa+"   "+fb+"    "+new Date());

						boolean calculatedA=IntegrateAllExp.isDone(fa);
						boolean calculatedB=IntegrateAllExp.isDone(fb);

						if(!calculatedA)
							System.out.println("Not calculated, there must be a problem: "+fa);
						if(!calculatedB)
							System.out.println("Not calculated, there must be a problem: "+fb);

						if(calculatedA && calculatedB)
							{
							System.out.println("Calculating "+key);

							//May not load one dataset twice at the same time. Can cause problems with cache files!!!!!! (probably minor ones)
							EvData dataA;
							EvData dataB;
							System.out.println("loading "+fa+"   "+fb);
							synchronized (fa)
								{
								dataA=EvData.loadFile(fa);
								}
							synchronized (fb)
								{
								dataB=EvData.loadFile(fb);
								}
							System.out.println("done loading "+fa+"   "+fb);

							Imageset imsetA = dataA.getObjects(Imageset.class).get(0);
							Imageset imsetB = dataB.getObjects(Imageset.class).get(0);

							String chanNameA=imsetA.getChild("GFP")!=null ? "GFP" : "RFP";
							String chanNameB=imsetB.getChild("GFP")!=null ? "GFP" : "RFP";

							System.out.println("Comparing: "+key);

							//Slices: T
							try
								{
								double[][] imtA=apToArray(dataA, "AP"+1+"-"+chanNameA, expName, coordLineageFor(dataA));
								double[][] imtB=apToArray(dataB, "AP"+1+"-"+chanNameB, expName, coordLineageFor(dataB));
								ColocCoefficients coeffT=colocSliceTime(imtA, imtB);
								synchronized (comparisonLock)
									{
									comparisonT.put(Tuple.make(fa,fb), coeffT);
									}
								}
							catch (Exception e)
								{
								e.printStackTrace();
								}

							//Slices: AP
							try
								{
								double[][] imapA=apToArray(dataA, "AP"+20+"-"+chanNameA, expName, coordLineageFor(dataA));
								double[][] imapB=apToArray(dataB, "AP"+20+"-"+chanNameB, expName, coordLineageFor(dataB));
								ColocCoefficients coeff=colocSliceTime(imapA, imapB);
								synchronized (comparisonLock)
									{
									comparisonAP.put(Tuple.make(fa,fb), coeff);
									}
								}
							catch (Exception e)
								{
								e.printStackTrace();
								}

							//Slices: DV
							try
								{
								double[][] imapA=apToArray(dataA, "DV"+20+"-"+chanNameA, expName, coordLineageFor(dataA));
								double[][] imapB=apToArray(dataB, "DV"+20+"-"+chanNameB, expName, coordLineageFor(dataB));
								ColocCoefficients coeff=colocSliceTime(imapA, imapB);
								synchronized (comparisonLock)
									{
									comparisonDV.put(Tuple.make(fa,fb), coeff);
									}
								}
							catch (Exception e)
								{
								e.printStackTrace();
								}

							//Slices: LR
							try
								{
								double[][] imapA=apToArray(dataA, "LR"+20+"-"+chanNameA, expName, coordLineageFor(dataA));
								double[][] imapB=apToArray(dataB, "LR"+20+"-"+chanNameB, expName, coordLineageFor(dataB));
								ColocCoefficients coeff=colocSliceTime(imapA, imapB);
								synchronized (comparisonLock)
									{
									comparisonLR.put(Tuple.make(fa,fb), coeff);
									}
								}
							catch (Exception e)
								{
								e.printStackTrace();
								}

							//Slices: XYZ
							ColocCoefficients coeffXYZ=colocXYZ(new ProgressHandle(), dataA, dataB, coordLineageFor(dataA), coordLineageFor(dataB), "XYZ","XYZ");
							synchronized (comparisonLock)
								{
								comparisonXYZ.put(Tuple.make(fa,fb), coeffXYZ);   ///////////////// symmetry?
								}

							//Store down this value too
							synchronized (comparisonLock)
								{
								storeCache(comparisonT, cachedValuesFileT);
								storeCache(comparisonAP, cachedValuesFileAP);
								storeCache(comparisonDV, cachedValuesFileDV);
								storeCache(comparisonLR, cachedValuesFileLR);
								storeCache(comparisonXYZ, cachedValuesFileXYZ);
								}
							}

						}
					catch (Exception e)
						{
						System.out.println("Exception for "+fa+" "+fb+"   "+e.getMessage());
						e.printStackTrace();
						}
					//System.gc();
					//System.out.println("total mem "+Runtime.getRuntime().totalMemory());

					}
				else
					System.out.println("Already compared "+key);

				
				return null;
				}
			});
			
			}
		
			
		
		
		if(true)
			{
			try
				{
				System.out.println("Making summary HTML");
				RenderHTML.makeSummaryHTML(new File(outputBaseDir,"exphtml"), datas);
				}
			catch (IOException e)
				{
				e.printStackTrace();
				}
			}
	
		File intStatsDir=new File(outputBaseDir,"intstats");
		intStatsDir.mkdirs();
		writeHTMLfromFiles(datas, comparisonT, intStatsDir,"T");
		writeHTMLfromFiles(datas, comparisonAP, intStatsDir,"AP");
		writeHTMLfromFiles(datas, comparisonXYZ, intStatsDir,"XYZ");
		
		
		
		
		System.exit(0);
		}

	
	
	
	}
