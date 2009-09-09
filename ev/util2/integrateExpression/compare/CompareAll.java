package util2.integrateExpression.compare;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
	private final static File cachedValuesFile=new File("/tmp/comparison.xml");
	
	
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
		
		//Reference nucleus
	//	String refnucName=lin.nuc.keySet().iterator().next();
		
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
		
		int imageMaxTime=100; //Break down to 100 time points
		
		double[][] image=new double[imageMaxTime][numSubDiv];

		//Fit model time using a few markers
		//Times must be relative to a sane time, such that if e.g. venc is missing, linear interpolation still makes sense
		FrameTime ft=new FrameTime();
		NucLineage.Nuc nucABa=coordLin.nuc.get("ABa");
		System.out.println(coordLin.nuc.keySet());
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
		
		NucLineage.Nuc refNuc=lin.nuc.get("_slice0");
		/*
		if(refNuc==null)
			System.out.println("Ref nuc is null");
		if(refNuc.exp.get(expName)==null)
			System.out.println("ref nuc does not have "+expName);*/
		
		//Fill in image
		int lastTime=0;
		for (EvDecimal frame : refNuc.exp.get(expName).level.keySet())
			{
			//Map to image
			int time=(int)ft.interpolateTime(frame).doubleValue();
			System.out.println("curtime: "+time);
			if(time<0)
				time=0;
			else if(time>=imageMaxTime)
				time=imageMaxTime-1;

			
			//For each slice
			for (int i = 0; i<numSubDiv; i++)
				{
				NucLineage.Nuc nuc = lin.nuc.get("_slice"+i);
				NucExp nexp = nuc.exp.get(expName);
				Double level = nexp.level.get(frame);
//				if (level==null)   //SHOULD NOT HAPPEN
//					continue here;
				
				for(int y=lastTime;y<time+1;y++)
					image[time][i]=level;
				}
			lastTime=time;
			}
		
		for(double[] d:image)
			{
			System.out.print(">");
			for(double e:d)
				System.out.print(" "+e);
			System.out.println();
			}

		//If it doesn't go far enough, the rest of the values will be 0.
		//The first values will be a replica of the first frame; should seldom
		//be a problem
		
		return image;
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
	
	
	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		
		//Find recordings to compare
		Set<File> datas=FindAnnotatedStrains.getAnnotated();
		System.out.println(datas);
		Map<Tuple<File,File>, ColocCoefficients> comparison=new HashMap<Tuple<File,File>, ColocCoefficients>();

		//Read past calculated values from disk if they exist
		if(cachedValuesFile.exists() && false)
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
		
		//Do pairwise. For user simplicity, can do symmetric and reflexive
		//Each slice, different bg.
		System.out.println("Calculate pair-wise statistics");
		if(true)
			for(File fa:datas)
				for(File fb:datas)
					{
					Tuple<File,File> key=Tuple.make(fa, fb);
					//Check if cached calculation does not exist
					if(!comparison.containsKey(key))
						{
						System.out.println("todo: "+key);
	
						ensureCalculated(fa);
						ensureCalculated(fb);
	
						EvData dataA=EvData.loadFile(fa);
						EvData dataB=EvData.loadFile(fb);
						
						
						System.out.println("Comparing: "+key);
	
	//					String nameAP="AP"+20+"-GFP";
						String nameT="AP"+1+"-GFP";
						String expName="exp";
						
						ColocCoefficients coeff=new ColocCoefficients();
						double[][] imtA=apToArray(dataA, nameT, expName, coordLineageFor(dataA));
						double[][] imtB=apToArray(dataB, nameT, expName, coordLineageFor(dataB));
						for(int i=0;i<imtA.length;i++)
							coeff.add(imtA[i], imtB[i]);
						
						System.out.println("coeff "+coeff.n+" "+coeff.sumX+" "+coeff.sumXX+" "+coeff.sumY);
						
						System.out.println("pearson "+ coeff.getPearson());
						
						comparison.put(Tuple.make(fa,fb), coeff);
	
						storeCache(comparison);
						
						//TODO maybe store more data
						
						//Load images
	//					File ima=new File(new File(fa,"data"),"foo.png");
						
						
						
						//coeff.add(arrX, arrY)
						
						
						//TODO calc
						
						}
					}
		
		
		
	
		
		
		
		
		
		
		
		
		
		//Turn into HTML
		try
			{
			Set<String> titles=new TreeSet<String>();
			Map<Tuple<String,String>,ColocCoefficients> map=new HashMap<Tuple<String,String>, ColocCoefficients>();
			for(File d:datas)
				titles.add(getName(d));
			for(Tuple<File,File> t:comparison.keySet())
				map.put(Tuple.make(getName(t.fst()), getName(t.snd())), comparison.get(t));
			writeHTML(titles, map, new File("/tmp/table.html"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
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
	public static void storeCache(Map<Tuple<File,File>, ColocCoefficients> comparison)
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
	
	
	/**
	 * 
	 * @param titles
	 * @param map (row, column)
	 * @param targetFile
	 * @throws IOException
	 */
	public static void writeHTML(Set<String> titles, Map<Tuple<String,String>,ColocCoefficients> map, File targetFile) throws IOException
		{
		//rows,columns
//		TreeSet<String> titles=new TreeSet<String>(map.keySet());
		
		StringBuffer sb=new StringBuffer();

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
		sb.append("</tr>");
		
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
				ColocCoefficients val=map.get(Tuple.make(ta,tb));
				sb.append("<td>");
				if(val==null)
					sb.append("?");
				else
					sb.append(""+val.getPearson());
				sb.append("</td>");
				}
			sb.append("</tr>");
			}

		EvFileUtil.writeFile(targetFile,
				EvFileUtil.readFile(EvFileUtil.getFileFromURL(CompareAll.class.getResource("templateCompare.html")))
				.replace("BODY", sb.toString()));
		}
	
	
	
	
	}
