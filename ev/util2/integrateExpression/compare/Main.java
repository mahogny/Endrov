package util2.integrateExpression.compare;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;

import util2.cellContactMap.CellContactMap;
import util2.integrateExpression.FindAnnotatedStrains;

import endrov.flowColocalization.ColocCoefficients;
import endrov.nuc.NucLineage;
import endrov.util.EvFileUtil;
import endrov.util.EvXmlUtil;
import endrov.util.Tuple;

public class Main
	{
	
	/**
	 * Coloc calculation requires two images that can overlap. Generate these from the AP or T lineage
	 */
	public double[] apToArray(NucLineage lin)
		{
		
		//TODO
		
		return new double[0];
		}

	/**
	 * Final graph from XYZ should be 2d with fixed dy/dt 
	 */
	
	
	/**
	 * BG calculation: otsu? could use for first frame at least.
	 * actually follows automatically. solved?
	 */
	
	
	public static void main(String[] args)
		{
		
		//Find recordings to compare
		Set<File> datas=FindAnnotatedStrains.getAnnotated();
		Map<Tuple<File,File>, Double> comparison=new HashMap<Tuple<File,File>, Double>();

		//Read past calculated values from disk if they exist
		File cachedValuesFile=new File("/tmp/comparison.xml");
		if(cachedValuesFile.exists())
			{
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
						comparison.put(Tuple.make(fa, fb), e.getAttribute("value").getDoubleValue());
					}
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		
		//Do pairwise. For user simplicity, can do symmetric and reflexive
		//Each slice, different bg.
		for(File fa:datas)
			for(File fb:datas)
				{
				Tuple<File,File> key=Tuple.make(fa, fb);
				if(!comparison.containsKey(key))
					{
					//Cached calculation does not exist
					
					ColocCoefficients coeff=new ColocCoefficients();

					
					
					
					
					//TODO maybe store more data
					
					//Load images
					File ima=new File(new File(fa,"data"),"foo.png");
					
					
					
					//coeff.add(arrX, arrY)
					
					
					//TODO calc
					
					}
				}
		
		
		
	
		
		
		
		
		
		//Store calculated values for the next time
		try
			{
			Element root=new Element("comparison");

			for(Tuple<File,File> t:comparison.keySet())
				{
				Element e=new Element("c");
				e.setAttribute("fa", t.fst().toString());
				e.setAttribute("fb",t.snd().toString());
				e.setAttribute("value",""+comparison.get(t));
				root.addContent(e);
				}
			Document doc=new Document(root);
			EvXmlUtil.writeXmlData(doc, cachedValuesFile);
			}
		catch (Exception e1)
			{
			e1.printStackTrace();
			}
		
		
		
		
		//Turn into HTML
		try
			{
			Set<String> titles=new TreeSet<String>();
			Map<Tuple<String,String>,Double> map=new HashMap<Tuple<String,String>, Double>();
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
	public static void writeHTML(Set<String> titles, Map<Tuple<String,String>,Double> map, File targetFile) throws IOException
		{
		//rows,columns
//		TreeSet<String> titles=new TreeSet<String>(map.keySet());
		
		StringBuffer sb=new StringBuffer();

		//First line with only titles
		sb.append("<tr>");
		sb.append("<td>&nbsp;</td>");
		for(String t:titles)
			{
			sb.append("<td>");
			for(char c:t.toCharArray())
				{
				sb.append(c);
				sb.append(" ");
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
				double val=map.get(Tuple.make(ta,tb));
				sb.append("<td>");
				sb.append(""+val);
				sb.append("</td>");
				}
			sb.append("</tr>");
			}

		EvFileUtil.writeFile(targetFile,
				EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap.class.getResource("templateCompare.html")))
				.replace("BODY", sb.toString()));
		}
	
	
	
	
	}
