package util2.integrateExpression.compare;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;

import util2.integrateExpression.FindAnnotatedStrains;
import util2.integrateExpression.IntExp;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowColocalization.ColocCoefficients;
import endrov.frameTime.FrameTime;
import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;
import endrov.util.EvXmlUtil;
import endrov.util.Tuple;

/**
 * Pairwise comparison of recordings
 * @author Johan Henriksson
 *
 */
public class CompareAll
	{
	private final static File cachedValuesFileT=new File("/tmp/comparisonT.xml");
	private final static File cachedValuesFileAP=new File("/tmp/comparisonAP.xml");
	private final static File cachedValuesFileXYZ=new File("/tmp/comparisonXYZ.xml");
	
	private final static int imageMaxTime=100; //Break down to 100 time points

	
	/**
	 * Normalize time between recordings
	 */
	private static FrameTime buildFrametime(NucLineage coordLin)
		{
		//Fit model time using a few markers
		//Times must be relative to a sane time, such that if e.g. venc is missing, linear interpolation still makes sense
		FrameTime ft=new FrameTime();
		System.out.println("has nucs: "+coordLin.nuc.keySet());
		
		NucLineage.Nuc nucABa=coordLin.nuc.get("ABa");
		ft.add(nucABa.pos.firstKey(), new EvDecimal("0").multiply(imageMaxTime));

		NucLineage.Nuc nucGast=coordLin.nuc.get("gast"); //Gastrulation
		if(nucGast!=null)
			ft.add(nucGast.pos.firstKey(), new EvDecimal("0.1").multiply(imageMaxTime));

		NucLineage.Nuc nucVenc=coordLin.nuc.get("venc"); //Ventral enclosure
		if(nucVenc!=null)
			ft.add(nucVenc.pos.firstKey(), new EvDecimal("0.43").multiply(imageMaxTime));

		NucLineage.Nuc nuc2ft=coordLin.nuc.get("2ftail"); //2-fold tail
		if(nuc2ft!=null)
			ft.add(nuc2ft.pos.firstKey(), new EvDecimal("0.54").multiply(imageMaxTime));

		System.out.println("ftmap "+ft.mapFrame2time);
		
		//times from BC10075_070606
		// "go to frame" seems buggy
		// ABa 3h1m10s      10870     0
		// gast 4h7m40s     14860     0.1
		// venc 7h49m20s    28160     0.43
		// 2ftail 8h59m20s  32360     0.54
		
		System.out.println("should be 0: "+ft.interpolateTime(nucABa.pos.firstKey()).doubleValue());
		//System.out.println("should be 0: "+ft.interpolateTime(nuc2ft.pos.firstKey()).doubleValue());
		
		return ft;
		}
	
	
	/**
	 * Coloc calculation requires two images that can overlap. Generate these from the AP or T lineage
	 */
	public static double[][] apToArray(EvData data, String newLinName, String expName, NucLineage coordLin)
		{
		Imageset imset = data.getObjects(Imageset.class).get(0);
		NucLineage lin = null;

		//Find lineage
		for(Map.Entry<EvPath, NucLineage> e:imset.getIdObjectsRecursive(NucLineage.class).entrySet())
			{
			if(e.getKey().getLeafName().equals(newLinName))
				{
				System.out.println(e);
				lin=e.getValue();
				break;
				}
			}
		if(lin==null)
			throw new RuntimeException("No lineage");
		
		//Autodetect number of subdivisions
		int numSubDiv=0;
		for(String nn:lin.nuc.keySet())
			if(nn.startsWith("_slice"))
				{
				int curnum=Integer.parseInt(nn.substring("_slice".length()));
				numSubDiv=Math.max(curnum+1,numSubDiv);
				}
			else
				System.out.println("Strange exp: "+nn);
		System.out.println("Detected subdiv "+numSubDiv);
		
		double[][] image=new double[imageMaxTime][numSubDiv];
		
		FrameTime ft=buildFrametime(coordLin);
		
		//Fill in image
		int lastTime=0;
		//System.out.println("curtime: ");
		NucLineage.Nuc refNuc=lin.nuc.get("_slice0");
		for (EvDecimal frame : refNuc.exp.get(expName).level.keySet())
			{
			//Map to image
			int time=(int)ft.interpolateTime(frame).doubleValue();
			//System.out.println("curtime: "+time);
			//System.out.print(time+" ");
			if(time<0)
				time=0;
			else if(time>=imageMaxTime)
				break;
			//time=imageMaxTime-1;
			
			//For each slice
			for (int i = 0; i<numSubDiv; i++)
				{
				NucLineage.Nuc nuc = lin.nuc.get("_slice"+i);
				NucExp nexp = nuc.exp.get(expName);
				Double level = nexp.level.get(frame);
				for(int y=lastTime;y<time+1;y++)
					image[time][i]=level;
				}
			lastTime=time;
			}
		//System.out.println();
		
		//System.out.println("numSubDiv: "+numSubDiv);
		/*
		for(double[] d:image)
			{
			System.out.print("im>");
			for(double e:d)
				System.out.print(" "+e);
			System.out.println();
			}*/
		//TODO warn for bad recordings. maybe obvious from result?

		//If it doesn't go far enough, the rest of the values will be 0.
		//The first values will be a replica of the first frame; should seldom
		//be a problem
		
		return image;
		}
	
	public static double channelAverageDt(EvChannel chan)
		{
		return chan.imageLoader.lastKey().subtract(chan.imageLoader.firstKey()).doubleValue()/chan.imageLoader.size();
		}
	
	
	/**
	 * Coloc over XYZ
	 */
	public static ColocCoefficients colocXYZ(EvData dataA, EvData dataB, NucLineage coordLinA, NucLineage coordLinB)
		{
		Imageset imsetA = dataA.getObjects(Imageset.class).get(0);
		Imageset imsetB = dataA.getObjects(Imageset.class).get(0);
		
		FrameTime ftA=buildFrametime(coordLinA);
		FrameTime ftB=buildFrametime(coordLinB);

		if(ftA.getNumPoints()<2 || ftB.getNumPoints()<2)
			{
			//Bad data survival
			ColocCoefficients coloc=new ColocCoefficients();
			return coloc;
			}
		
		EvChannel chanA=(EvChannel)imsetA.getChild("GFP"); 
		EvChannel chanB=(EvChannel)imsetB.getChild("GFP");
		
		//Figure out how many steps to take
		double dt=channelAverageDt(chanA);
		EvDecimal frame0A=ftA.interpolateFrame(new EvDecimal(0));
		EvDecimal frame100A=ftA.interpolateFrame(new EvDecimal(100));
		int numSteps=frame100A.subtract(frame0A).divide(new EvDecimal(dt)).intValue();
		System.out.println("Num steps "+numSteps);
		
		//Compare channels
		ColocCoefficients coloc=new ColocCoefficients();
		int cnt=0;
		for(double time=0;time<imageMaxTime;time+=1.0/numSteps)
			{
			//Corresponding frames
			EvDecimal frameA=ftA.interpolateFrame(new EvDecimal(time));
			EvDecimal frameB=ftB.interpolateFrame(new EvDecimal(time));
			
			//If outside range, stop calculating
			if(frameA.less(chanA.imageLoader.firstKey()) || frameA.greater(chanA.imageLoader.firstKey()) ||
					frameB.less(chanB.imageLoader.firstKey()) || frameB.greater(chanB.imageLoader.firstKey()))
				{
				//System.out.println("Skip: "+frameA+"\t"+frameB);
				continue;
				}
			cnt++;
			//Use closest frame in each
			EvStack stackA=chanA.imageLoader.get(chanA.closestFrame(frameA));
			EvStack stackB=chanB.imageLoader.get(chanB.closestFrame(frameB));
			
			//Compare each slice. Same number of slices since it has been normalized
			int numz=stackA.getDepth();
			for(int i=0;i<numz;i++)
				{
				double[] arrA=stackA.get(new EvDecimal(i)).getPixels().convertToDouble(true).getArrayDouble();
				double[] arrB=stackB.get(new EvDecimal(i)).getPixels().convertToDouble(true).getArrayDouble();
				coloc.add(arrA, arrB);
				}
			}
		System.out.println("Num xyz compared: "+cnt);
		
		return coloc;
		}
	
	
	
	

	/**
	 * Final graph from XYZ should be 2d with fixed dy/dt 
	 */
	
	
	/**
	 * BG calculation: otsu? could use for first frame at least.
	 * actually follows automatically. solved?
	 */
	
	
	public static void ensureCalculated(File f)
		{
		IntExp.doOne(f);
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
					if(datas.contains(fa) && datas.contains(fb))
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
	
	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		
		Set<String> argsSet=new HashSet<String>();
		for(String s:args)
			argsSet.add(s);
		
		//Find recordings to compare
		Set<File> datas=FindAnnotatedStrains.getAnnotated();
		System.out.println(datas);

		//Read past calculated values from disk 
		Map<Tuple<File,File>, ColocCoefficients> comparisonT=new HashMap<Tuple<File,File>, ColocCoefficients>();
		Map<Tuple<File,File>, ColocCoefficients> comparisonAP=new HashMap<Tuple<File,File>, ColocCoefficients>();
		Map<Tuple<File,File>, ColocCoefficients> comparisonXYZ=new HashMap<Tuple<File,File>, ColocCoefficients>();
		if(!argsSet.contains("nocache"))
			{
			comparisonT=loadCache(datas, cachedValuesFileT);
			comparisonAP=loadCache(datas, cachedValuesFileAP);
			comparisonXYZ=loadCache(datas, cachedValuesFileXYZ);
			}
		//Map<Tuple<File,File>, ColocCoefficients> comparison=new HashMap<Tuple<File,File>, ColocCoefficients>();
		
		//Do pairwise. For user simplicity, can do symmetric and reflexive
		//Each slice, different bg.
		System.out.println("Calculate pair-wise statistics");
		if(!argsSet.contains("nocalc"))
			for(File fa:datas)
				for(File fb:datas)
					{
					Tuple<File,File> key=Tuple.make(fa, fb);
					//Check if cached calculation does not exist
					if(!comparisonT.containsKey(key) || !comparisonAP.containsKey(key) || !comparisonXYZ.containsKey(key))
						{
						System.out.println("todo: "+key);
	
						ensureCalculated(fa);
						ensureCalculated(fb);
	
						EvData dataA=EvData.loadFile(fa);
						EvData dataB=EvData.loadFile(fb);
						
						System.out.println("Comparing: "+key);
	
						String expName="exp";
						
						//Slices: T
						String nameT="AP"+1+"-GFP";
						ColocCoefficients coeffT=new ColocCoefficients();
						double[][] imtA=apToArray(dataA, nameT, expName, coordLineageFor(dataA));
						double[][] imtB=apToArray(dataB, nameT, expName, coordLineageFor(dataB));
						for(int i=0;i<imtA.length;i++)
							coeffT.add(imtA[i], imtB[i]);
						comparisonT.put(Tuple.make(fa,fb), coeffT);

						//Slices: AP
						String nameAP="AP"+20+"-GFP";
						ColocCoefficients coeffAP=new ColocCoefficients();
						double[][] imapA=apToArray(dataA, nameAP, expName, coordLineageFor(dataA));
						double[][] imapB=apToArray(dataB, nameAP, expName, coordLineageFor(dataB));
						for(int i=0;i<imtA.length;i++)
							coeffAP.add(imapA[i], imapB[i]);
						comparisonAP.put(Tuple.make(fa,fb), coeffAP);

						//Slices: XYZ
						ColocCoefficients coeffXYZ=colocXYZ(dataA, dataB, coordLineageFor(dataA), coordLineageFor(dataB));
						comparisonXYZ.put(Tuple.make(fa,fb), coeffXYZ);
												
						//Store down this value too
						storeCache(comparisonT, cachedValuesFileT);
						storeCache(comparisonAP, cachedValuesFileAP);
						storeCache(comparisonXYZ, cachedValuesFileXYZ);

						//Temp
						System.out.println("coeffT "+coeffT.n+" "+coeffT.sumX+" "+coeffT.sumXX+" "+coeffT.sumY);
						System.out.println("coeffAP "+coeffAP.n+" "+coeffAP.sumX+" "+coeffAP.sumXX+" "+coeffAP.sumY);
						System.out.println("pearsonT "+ coeffT.getPearson());

						}
					}
		
		
	
		writeHTMLfromFiles(datas, comparisonT, new File("/tmp/"),"T");
		writeHTMLfromFiles(datas, comparisonAP, new File("/tmp/"),"AP");
		writeHTMLfromFiles(datas, comparisonXYZ, new File("/tmp/"),"XYZ");
		
		
		
		
		System.exit(0);
		}

	
	public static NucLineage coordLineageFor(EvContainer data)
		{
		NucLineage lin=null;
		//Find lineage
		for(Map.Entry<EvPath, NucLineage> e:data.getIdObjectsRecursive(NucLineage.class).entrySet())
			if(!e.getKey().getLeafName().startsWith("AP"))
				lin=e.getValue();
		return lin;
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
//				e.setAttribute("value",""+comparison.get(t));
				root.addContent(e);
				}
			Document doc=new Document(root);
			EvXmlUtil.writeXmlData(doc, cachedValuesFile);
			}
		catch (Exception e1)
			{
			e1.printStackTrace();
			}
		}
	
	
	/**
	 * How to get gene name from strain name?
	 * genotype makes more sense. deffiz claims it exists as a field
	 */

	public static String getName(File data)
		{
		return data.getName();
		}
	
	
	public static void writeHTMLfromFiles(Set<File> datas, Map<Tuple<File,File>, ColocCoefficients> comparison, File targetFile, String profType)
		{
		//Turn into HTML
		try
			{
			Set<String> titles=new TreeSet<String>();
			Map<Tuple<String,String>,ColocCoefficients> map=new HashMap<Tuple<String,String>, ColocCoefficients>();
			for(File d:datas)
				titles.add(getName(d));
			for(Tuple<File,File> t:comparison.keySet())
				map.put(Tuple.make(getName(t.fst()), getName(t.snd())), comparison.get(t));
			writeHTML(titles, map, targetFile, profType);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	
	public abstract static class TableWriter
		{
		public StringBuffer sb=new StringBuffer();
		
		public TableWriter(Set<String> titles)
			{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);
			
			//First line with only titles
			sb.append("<tr>");
			sb.append("<td>&nbsp;</td>");
			for(String t:titles)
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
			for(String ta:titles)
				{
				//Title
				sb.append("<tr>");
				sb.append("<td>");
				sb.append(ta);
				sb.append("</td>");
				
				for(String tb:titles)
					{
					Double val=getValue(ta, tb);
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
		
		public abstract Double getValue(String ta, String tb);
		}

	
	/**
	 * Write HTML-files
	 * @param titles
	 * @param map (row, column)
	 * @param targetDir
	 */
	public static void writeHTML(Set<String> titles, final Map<Tuple<String,String>,ColocCoefficients> map, File targetDir, String profType) throws IOException
		{
		TableWriter twPearson=new TableWriter(titles){
			public Double getValue(String ta, String tb)
				{
				ColocCoefficients val=map.get(Tuple.make(ta,tb));
				return val==null ? null : val.getPearson();
				}};

		TableWriter twManders1=new TableWriter(titles){
		public Double getValue(String ta, String tb)
			{
			ColocCoefficients val=map.get(Tuple.make(ta,tb));
			return val==null ? null : val.getMandersX();
			}};

		TableWriter twK1=new TableWriter(titles){
		public Double getValue(String ta, String tb)
			{
			ColocCoefficients val=map.get(Tuple.make(ta,tb));
			return val==null ? null : val.getKX();
			}};
			
			

		String template=EvFileUtil.readFile(EvFileUtil.getFileFromURL(CompareAll.class.getResource("templateCompare.html")));
		EvFileUtil.writeFile(new File(targetDir,"table"+profType+"Pearson.html"),template.replace("COEFF","Pearson").replace("BODY", twPearson.sb.toString()));
		EvFileUtil.writeFile(new File(targetDir,"table"+profType+"Manders1.html"),template.replace("COEFF","Manders<sub>1</sub>").replace("BODY", twManders1.sb.toString()));
		EvFileUtil.writeFile(new File(targetDir,"table"+profType+"K1.html"),template.replace("COEFF","k<sub>1</sub>").replace("BODY", twK1.sb.toString()));

		}
	
	
	
	
	}
