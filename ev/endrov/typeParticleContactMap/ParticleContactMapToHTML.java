/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleContactMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import javax.imageio.ImageIO;
import endrov.util.collection.EvListUtil;
import endrov.util.io.EvFileUtil;
import endrov.util.math.EvDecimal;


/**
 * Generate HTML from CCMs
 * 
 * @author Johan Henriksson, Jurgen Hench
 *
 */
public class ParticleContactMapToHTML
	{
	private static String htmlColorNotNeigh="#ffffff";
	private static String htmlColorNA="#cccccc";
	private static String htmlColorNT="#666666";
	private static String htmlColorSelf="#33ccff";
	private static final int clength=50; //[px]
	private static final int cheight=13; //[px]
	

	/**
	 * Calculate overlap array for two cells
	 */
	private static boolean[] getOverlaps(ParticleContactMap ccm, String nucName, String nucName2)
		{
		boolean[] neighOverlaps=new boolean[clength];
		
		if(ccm.contactFrames.get(nucName)==null)
			return neighOverlaps;
		
		
		Set<EvDecimal> ov=ccm.contactFrames.get(nucName).get(nucName2);
		
		if(ov==null || ov.isEmpty())
			return neighOverlaps;
		EvDecimal firstFrame=ccm.particleInfo.get(nucName).firstFrame;
		EvDecimal lastFrame=ccm.particleInfo.get(nucName).lastFrame;
		//This could be a potential fix for the last-frame-is-not-in-contact problem.
		//Problem is, it does so by ignoring the last part which has other problems.
		//EvDecimal lastFrame=thisNuc.pos.lastKey();
		
		
		SortedMap<EvDecimal,Boolean> isNeighMap=new TreeMap<EvDecimal, Boolean>();
		//Could restrict better using lifetime
		for(EvDecimal f:ccm.framesTested.tailSet(firstFrame))
			if(f.lessEqual(lastFrame))
				isNeighMap.put(f, false);
		
		//Consult map for all frames
		for(EvDecimal f:ov)
			isNeighMap.put(f, true);

		//It also exists at the last frame, given override etc.
		//Hack for full self-contact. Often a cell has no keyframe at the frame of division.
		//This causes a drop in contact.
		//The problem is again discreteness, it has minor implications
		//otherwise but a better solution would be nice
		if(nucName.equals(nucName2))
			isNeighMap.put(lastFrame,true);

		EvDecimal lifeLenFrames=(lastFrame.subtract(firstFrame));

		for(int curp=0;curp<clength;curp++)
			{
			//For each curp, map to time, check which keyframe (for neighcheck) is closest.
			//Check if it is a neighbour in this frame
			EvDecimal m=firstFrame.add(lifeLenFrames.multiply(curp+0.5).divide(clength));
			
			EvDecimal closestFrame=EvListUtil.closestFrame(isNeighMap, m);
			if(isNeighMap.get(closestFrame))
				neighOverlaps[curp]=true;
			}

		return neighOverlaps;
		}
	
	
	
	/**
	 * For a set of cell contact maps, generate HTML representation. Place in given directory
	 */
	public static void generateHTML(Map<String,ParticleContactMap> orderedCCM, File targetdirTree)
		{
		try
			{
			NumberFormat percentFormat=NumberFormat.getInstance();
			percentFormat.setMinimumFractionDigits(1);
			percentFormat.setMaximumFractionDigits(1);

			Map<ParticleContactMap,Integer> numidMap=new HashMap<ParticleContactMap, Integer>();
			
			//Order by name
			int numid=1;
			for(ParticleContactMap lin:orderedCCM.values())
				{
				numidMap.put(lin,numid);
				numid++;
				}

			//Which particles are covered, in total?
			TreeSet<String> nucNames=new TreeSet<String>();
			for(ParticleContactMap e:orderedCCM.values())
				nucNames.addAll(e.particleInfo.keySet());
			
			/////// HTML files for all contacts
			if(targetdirTree.exists())
				EvFileUtil.deleteRecursive(targetdirTree);
			targetdirTree.mkdirs();
			
			String updateTime=new Date().toString();

			//Images for bars
			writeBar(new File(targetdirTree,"n_bar.png"),Color.black);
			writeBar(new File(targetdirTree,"a_bar.png"),Color.white);

			//Write cell list files
			StringBuffer mainTreeOut=new StringBuffer();
			for(String nucName:nucNames)
				mainTreeOut.append("<a href=\""+nucName+"_neightime.htm\">"+nucName+"</a></br>");
			EvFileUtil.writeFile(new File(targetdirTree,"index.htm"),
					EvFileUtil.readFile(EvFileUtil.getFileFromURL(ParticleContactMapToHTML.class.getResource("main_tree.htm")))
					.replace("BODY", mainTreeOut));
			EvFileUtil.writeFile(new File(targetdirTree,"style.css"),
					EvFileUtil.readFile(EvFileUtil.getFileFromURL(ParticleContactMapToHTML.class.getResource("style.css"))));

			//List datasets
			StringBuffer outDatasets=new StringBuffer();
			for(Map.Entry<String,ParticleContactMap> e:orderedCCM.entrySet())
				outDatasets.append(""+numidMap.get(e.getValue())+": "+e.getKey()+" <br/>");

			//Write out HTML, cell by cell. Reference lineage is the first one in the list
			//nucName: everything in the file is about this cell
			String neighTemplate=EvFileUtil.readFile(EvFileUtil.getFileFromURL(ParticleContactMapToHTML.class.getResource("neigh.htm")));
			for(String nucName:nucNames)
				{
				StringBuffer bodyTime=new StringBuffer();

				//Sub header: lin# & cell name
				StringBuffer subhTime=new StringBuffer(); //f2
				for(Map.Entry<String,ParticleContactMap> e:orderedCCM.entrySet())
					subhTime.append("<td width=\""+clength+"\"><tt>"+numidMap.get(e.getValue())+"</tt></td>");

				//Compare with all other particles
				for(String nucName2:nucNames)
					{
					//Get annotation statistics
					int notAnnotated=0; //# not annotated
					int sa=0; //# co-occurance
					for(Map.Entry<String,ParticleContactMap> e:orderedCCM.entrySet())
						{
						ParticleContactMap ccm=e.getValue();
						if(isAnnotated(ccm, nucName2))
							{
							if(!ccm.contactFrames.get(nucName).get(nucName2).isEmpty())
								sa++;
							}
						else
							notAnnotated++;
						}
					int annotated=orderedCCM.size()-notAnnotated;

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
						bodyTime.append(line.replace("FILE",nucName2+"_neightime.htm"));

						//Contact map itself
						for(Map.Entry<String,ParticleContactMap> e:orderedCCM.entrySet())
							{
							ParticleContactMap ccm=e.getValue();
							
							//Formatting for non-time CCM
							String timeColor;
							String timeString;
							
							if(!isAnnotated(ccm,nucName))	//this (nucname) not annotated
								{
								timeColor=htmlColorNT;
								timeString="<font color=\"#ffffff\">n.t.</font>";
								}
							else if(!isAnnotated(ccm,nucName2))	//nucname2 not annotated
								{
								timeColor=htmlColorNA;
								timeString="n.a.";
								}
							else
								{
								timeColor=htmlColorNotNeigh;
								timeString="&nbsp;";
								boolean[] neighOverlaps=getOverlaps(ccm, nucName, nucName2);
								timeString=getOverlapBar(neighOverlaps).toString();
								}

							bodyTime.append("<td bgcolor=\""+timeColor+"\"><tt>"+timeString+"</tt></td>\n");
							}

						}
					}

				
				//Output entire file
				String out=neighTemplate
					.replace("UPDATETIME",updateTime)
					.replace("NUCNAME", nucName)
					.replace("DATASETS", outDatasets)
					.replace("COLSPAN",""+orderedCCM.size());
				EvFileUtil.writeFile(new File(targetdirTree,nucName+"_neightime.htm"), 
						out
							.replace("CONTACTTABLE", bodyTime)
							.replace("SUBHEADER",subhTime));
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	private static boolean isAnnotated(ParticleContactMap lin, String nucName)
		{
		return lin.particleInfo.containsKey(nucName);
		}
	

	/**
	 * Generate optimized HTML for overlaps by using RLE
	 */
	private static String getOverlapBar(boolean[] neighOverlaps)	
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

		//Safe version, but generates tons of images = slow
		//for(boolean b:neighOverlaps)
		//	imgcode.append("<img src=\""+(b ? 'n' : 'a')+"_bar.png\">");
		return imgcode.toString();
		}

	private static String getOverlapImage(int len, boolean current)
		{
		return "<img width=\""+len+"\" height=\""+cheight+"\" src=\""+(current ? 'n' : 'a')+"_bar.png\">";
		}


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
