/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.neighmap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

import javax.imageio.ImageIO;


import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;

/**
 * Render aligned views of neighmaps
 * @author Johan Henriksson
 *
 */
public class RenderNeighmap
	{
	public static String htmlColorNotNeigh="#ffffff";
	public static String htmlColorNA="#cccccc";
	public static String htmlColorNT="#666666";
	public static String htmlColorSelf="#33ccff";
	public static final int clength=50; //[px]
	public static final int cheight=13; //[px]
	
	/**
	 * Render to HTML-files
	 * @param nmaps Maps to align
	 * @param targetDir Root directory
	 */
	public static void render(Map<String,NeighMap> nmaps, File targetDir) throws IOException
		{
		NumberFormat percentFormat=NumberFormat.getInstance();
		percentFormat.setMinimumFractionDigits(1);
		percentFormat.setMaximumFractionDigits(1);

		
		TreeSet<String> nucNames=new TreeSet<String>();
		for(NeighMap nm:nmaps.values())
			nucNames.addAll(nm.lifetime.keySet());
		
		nmaps.values().iterator().next();

		//Numeric indices
		HashMap<String, Integer> numidMap=new HashMap<String, Integer>();
		int numid=1;
		for(String n:nmaps.keySet())
			numidMap.put(n,numid++);
		
		System.out.println("Writing files");


		File targetdirNeigh=new File(targetDir,"neigh");
		File targetdirTree=new File(targetDir,"tree");
		targetdirNeigh.mkdirs();
		targetdirTree.mkdirs();
		
		String updateTime=new Date().toString();


		//Images for bars
		writeBar(new File(targetdirNeigh,"n_bar.png"),Color.black);
		writeBar(new File(targetdirNeigh,"a_bar.png"),Color.white);
		writeBar(new File(targetdirTree,"n_bar.png"),Color.black);
		writeBar(new File(targetdirTree,"a_bar.png"),Color.white);

		//Write cell list files
		StringBuffer mainSingleOut=new StringBuffer();
		StringBuffer mainTreeOut=new StringBuffer();
		for(String nucName:nucNames)
			{
			mainSingleOut.append("<a href=\""+nucName+"_neigh.htm\">"+nucName+"</a></br>");
			mainTreeOut.append("<a href=\""+nucName+"_neightime.htm\">"+nucName+"</a></br>");
			}
		EvFileUtil.writeFile(new File(targetdirNeigh,"index.htm"),
				EvFileUtil.readFile(EvFileUtil.getFileFromURL(RenderNeighmap.class.getResource("main_single.htm")))
				.replace("BODY", mainSingleOut));
		EvFileUtil.writeFile(new File(targetdirTree,"index.htm"),
				EvFileUtil.readFile(EvFileUtil.getFileFromURL(RenderNeighmap.class.getResource("main_tree.htm")))
				.replace("BODY", mainTreeOut));

		//List datasets
		StringBuffer outDatasets=new StringBuffer();
		for(Map.Entry<String, Integer> e:numidMap.entrySet())
			outDatasets.append(""+e.getValue()+": "+e.getKey()+" <br/>");

		//Write out HTML, cell by cell. Reference lineage is the first one in the list
		//nucName: everything in the file is about this cell
		String neighTemplate=EvFileUtil.readFile(EvFileUtil.getFileFromURL(RenderNeighmap.class.getResource("neigh.htm")));
		for(String nucName:nucNames)
			{
			StringBuffer bodyNeigh=new StringBuffer();
			StringBuffer bodyTime=new StringBuffer();

			//Sub header: lin# & cell name
			StringBuffer subhMain=new StringBuffer();
			StringBuffer subhTime=new StringBuffer(); //f2
			for(Map.Entry<String, Integer> e:numidMap.entrySet())
				{
				subhMain.append("<td><tt>"+e.getValue()+"</tt></td>");
				subhTime.append("<td width=\""+clength+"\"><tt>"+e.getValue()+"</tt></td>");
				}

			//Compare with all other nuclei
			for(String nucName2:nucNames)
				{

				//Get annotation statistics
				int notAnnotated=0; //# not annotated
				int sa=0; //# co-occurance
				for(NeighMap lin:nmaps.values())
					if(isAnnotated(lin, nucName2))
						{
						if(!lin.getCreateListFor(nucName, nucName2).isEmpty())
							sa++;
						}
					else
						notAnnotated++;
				int annotated=nmaps.size()-notAnnotated;

				//Do this cell only if any recording has it as a neighbour
				if(sa!=0)
					{
					//Calculate a color based on the match score
					double hits=(double)sa/(annotated);
					int hc=(int)(255-hits*255);
					String hex=Integer.toHexString(hc);
					if(hex.length()==1)
						hex="0"+hex;
					String scoreColor="#ff"+hex+hex;
					if(nucName.equals(nucName2)) //Itself
						scoreColor=htmlColorSelf;						

					//Name in table
					String line="<tr><td bgcolor=\""+scoreColor+"\"><tt><a href=\"FILE\">"+nucName2+"</a></tt></td><td bgcolor=\""+scoreColor+"\"><tt>"+sa+"/"+annotated+"</tt></td>\n";
					bodyNeigh.append(line.replace("FILE",nucName2+"_neigh.htm"));
					bodyTime.append(line.replace("FILE",nucName2+"_neightime.htm"));

					//Contact map itself
					for(NeighMap lin:nmaps.values())
						{
						double percLifeLen=percentLifetime(nmaps.get(nucName), nucName, nmaps.get(nucName).getCreateListFor(nucName, nucName2));
						
						//Formatting for non-time CCM
						String neighColor;
						String neighString;
						if(!isAnnotated(lin,nucName))	//this (nucname) not annotated
							{
							neighColor=htmlColorNT;
							neighString="<font color=\"#ffffff\">n.t.</font>";
							}
						else if(!isAnnotated(lin,nucName2))	//nucname2 not annotated
							{
							neighColor=htmlColorNA;
							neighString="n.a.";
							}
						else if(!lin.getCreateListFor(nucName, nucName2).isEmpty()) //Is neighbour 
							//if(percLifeLen!=0) //Is neighbour
							{
							neighColor=scoreColor;
							neighString="";
							neighString=percentFormat.format(percLifeLen); //not used?
							}
						else //Is not neighbour
							{
							neighColor=htmlColorNotNeigh;
							neighString="&nbsp;";
							}

						//Basic formatting for time CCM based on non-time CCM
						String timeColor=neighColor;
						if(timeColor==scoreColor)
							timeColor=htmlColorNotNeigh;
						String timeString=neighString;

						//Format time based on when there is overlap
						if(!lin.getCreateListFor(nucName, nucName2).isEmpty())
							{
							if(percLifeLen!=0)
								timeString=getOverlapBar(nmaps.get(nucName), nucName, nmaps.get(nucName).getCreateListFor(nucName, nucName2)).toString();
							else
								timeString=neighString="&nbsp;";
							}

						bodyNeigh.append("<td bgcolor=\""+neighColor+"\"><tt>"+neighString+"</tt></td>\n");
						bodyTime.append("<td bgcolor=\""+timeColor+"\"><tt>"+timeString+"</tt></td>\n");
						}

					}
				}

			//Output entire file
			String out=neighTemplate
				.replace("UPDATETIME",updateTime)
				.replace("NUCNAME", nucName)
				.replace("DATASETS", outDatasets)
				.replace("COLSPAN",""+nmaps.size());
			EvFileUtil.writeFile(new File(targetdirNeigh,nucName+"_neigh.htm"), 
					out
						.replace("CONTACTTABLE", bodyNeigh)
						.replace("SUBHEADER",subhMain));
			EvFileUtil.writeFile(new File(targetdirTree,nucName+"_neightime.htm"), 
					out
						.replace("CONTACTTABLE", bodyTime)
						.replace("SUBHEADER",subhTime));
			}
		
		
		}
	
	
	
	private static double percentLifetime(NeighMap nmap, String name, List<NeighMap.Interval> neigh)
		{
		EvDecimal sum=EvDecimal.ZERO;
		NeighMap.Interval lif=nmap.lifetime.get(name).cut(nmap.validity);
		for(NeighMap.Interval i:neigh)
			sum=sum.add(i.cut(lif).length());
		EvDecimal len=nmap.lifetime.get(name).cut(lif).length();
		return sum.doubleValue()/len.doubleValue();
		}
	

	//TODO only include subset
	private static boolean isAnnotated(NeighMap nm, String nucname)
		{
		return nm.lifetime.containsKey(nucname);
		}
	
	private static String getOverlapBar(NeighMap nmap, String name, List<NeighMap.Interval> neigh)
		{
		StringBuffer imgcode=new StringBuffer();
		NeighMap.Interval lif=nmap.lifetime.get(name).cut(nmap.validity);
		double totalLength=lif.length().doubleValue();
		
		double lastend=0;
		for(NeighMap.Interval i:neigh)
			{
			NeighMap.Interval i2=i.cut(lif);
			double plen=i2.length().doubleValue();
			if(plen>0)
				{
				double falselen=i2.start.doubleValue()-lastend;
				if(falselen>0)
					getOverlapImage(imgcode,(int)(plen*clength/totalLength),false);
				lastend=i2.end.doubleValue();
				getOverlapImage(imgcode,(int)(plen*clength),true);
				}
			}
		return imgcode.toString();
		}
	
	
	/**
	 * Generate optimized HTML for overlaps by using RLE
	 */
	/*
	public static String getOverlapBar(boolean[] neighOverlaps)
		{
		StringBuffer imgcode=new StringBuffer();

		Boolean current=null;
		int len=0;
		for(boolean b:neighOverlaps)
			{
			if(current==null)
				current=b;
			else if(b!=current)
				{
				imgcode.append(getOverlapImage(len,current));
				current=b;
				len=0;
				}
			len++;
			}
		if(len!=0)
			imgcode.append(getOverlapImage(len,current));

		//Safe version
		//for(boolean b:neighOverlaps)
		//	imgcode.append("<img src=\""+(b ? 'n' : 'a')+"_bar.png\">");
		return imgcode.toString();
		}
*/
	
	private static void getOverlapImage(StringBuffer sb, int len, boolean current)
		{
		sb.append("<img width=\""+len+"\" height=\""+cheight+"\" src=\""+(current ? 'n' : 'a')+"_bar.png\">");
		}
	/*
	public static String getOverlapImage(int len, boolean current)
		{
		return "<img width=\""+len+"\" height=\""+cheight+"\" src=\""+(current ? 'n' : 'a')+"_bar.png\">";
		}*/


	/**
	 * Write bar image
	 */
	private static void writeBar(File file, Color col) throws IOException
		{
		BufferedImage bim=new BufferedImage(1,cheight,BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g=bim.createGraphics();
		g.setColor(col);
		g.fillRect(0,0,1,cheight);
		ImageIO.write(bim,"png",file);
		}
	
	
	
	}
