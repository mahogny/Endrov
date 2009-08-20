package endrov.nuc.ccm;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import javax.imageio.ImageIO;
import endrov.ev.*;
import endrov.util.*;


/**
 * Calculate cell contact map.
 * Output number of contacts for each frame.
 * 
 * about 40min on xeon
 * 
 * @author Johan Henriksson, Jurgen Hench
 *
 */
public class CellContactMapHTML
	{
	public static String htmlColorNotNeigh="#ffffff";
	public static String htmlColorNA="#cccccc";
	public static String htmlColorNT="#666666";
	public static String htmlColorSelf="#33ccff";
	public static final int clength=50; //[px]
	public static final int cheight=13; //[px]
	

	/**
	 * Calculate overlap array for two cells
	 */
	private static boolean[] getOverlaps(CellContactMap ccm, String nucName, String nucName2)
		{
		boolean[] neighOverlaps=new boolean[clength];
		
		if(ccm.contactsf.get(nucName)==null)
			return neighOverlaps;
		
		
		Set<EvDecimal> ov=ccm.contactsf.get(nucName).get(nucName2);
		
		if(ov==null || ov.isEmpty())
			return neighOverlaps;
		//NucLineage.Nuc thisNuc=ccm.lin.nuc.get(nucName);
		EvDecimal firstFrame=ccm.firstFrame.get(nucName);
		EvDecimal lastFrame=ccm.lastFrame.get(nucName);
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
	
	
	
	
	public static void generateHTML(Map<String,CellContactMap> orderedCCM, File targetdirTree)
		{
		try
			{
			EvLog.listeners.add(new EvLogStdout());
			EV.loadPlugins();

			NumberFormat percentFormat=NumberFormat.getInstance();
			percentFormat.setMinimumFractionDigits(1);
			percentFormat.setMaximumFractionDigits(1);



			Map<CellContactMap,Integer> numidMap=new HashMap<CellContactMap, Integer>();
			
			//Order by name
			int numid=1;
			for(CellContactMap lin:orderedCCM.values())
				{
				numidMap.put(lin,numid);
				numid++;
				}

			//Which nuclei are covered, in total?
			TreeSet<String> nucNames=new TreeSet<String>();
			for(CellContactMap e:orderedCCM.values())
				nucNames.addAll(e.allNuc);

			
			/////// HTML files for all contacts
			targetdirTree.mkdirs();
			
			String updateTime=new Date().toString();

			//Images for bars
			writeBar(new File(targetdirTree,"n_bar.png"),Color.black);
			writeBar(new File(targetdirTree,"a_bar.png"),Color.white);

			//Write cell list files
			StringBuffer mainSingleOut=new StringBuffer();
			StringBuffer mainTreeOut=new StringBuffer();
			for(String nucName:orderedCCM.keySet())
				{
				mainSingleOut.append("<a href=\""+nucName+"_neigh.htm\">"+nucName+"</a></br>");
				mainTreeOut.append("<a href=\""+nucName+"_neightime.htm\">"+nucName+"</a></br>");
				}
			EvFileUtil.writeFile(new File(targetdirTree,"index.htm"),
					EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMapHTML.class.getResource("main_tree.htm")))
					.replace("BODY", mainTreeOut));
			EvFileUtil.writeFile(new File(targetdirTree,"style.css"),
					EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMapHTML.class.getResource("style.css"))));

			//List datasets
			StringBuffer outDatasets=new StringBuffer();
			for(Map.Entry<String,CellContactMap> e:orderedCCM.entrySet())
				outDatasets.append(""+numidMap.get(e.getValue())+": "+e.getKey()+" <br/>");

			//Write out HTML, cell by cell. Reference lineage is the first one in the list
			//nucName: everything in the file is about this cell
			String neighTemplate=EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMapHTML.class.getResource("neigh.htm")));
			for(String nucName:nucNames)
				{
				//StringBuffer bodyNeigh=new StringBuffer();
				StringBuffer bodyTime=new StringBuffer();

				//Sub header: lin# & cell name
				//StringBuffer subhMain=new StringBuffer();
				StringBuffer subhTime=new StringBuffer(); //f2
				for(Map.Entry<String,CellContactMap> e:orderedCCM.entrySet())
					{
					//subhMain.append("<td><tt>"+numidMap.get(lin)+"</tt></td>");
					subhTime.append("<td width=\""+clength+"\"><tt>"+numidMap.get(e.getValue())+"</tt></td>");
					}

				//Compare with all other nuclei
				for(String nucName2:nucNames)
					{

					//Get annotation statistics
					int notAnnotated=0; //# not annotated
					int sa=0; //# co-occurance
					for(Map.Entry<String,CellContactMap> e:orderedCCM.entrySet())
						{
						CellContactMap lin=e.getValue();
						if(isAnnotated(lin, nucName2))
							{
							if(!lin.contactsf.get(nucName).get(nucName2).isEmpty())
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
						for(Map.Entry<String,CellContactMap> e:orderedCCM.entrySet())
							{
							CellContactMap ccm=e.getValue();
							
							double percLifeLen=100*(double)ccm.contactsf.get(nucName).get(nucName2).size()/ccm.lifelen.get(nucName);
							if(ccm.lifelen.get(nucName)==0)// || percLifeLen<1)
								percLifeLen=0;

							//Formatting for non-time CCM
							String neighColor;
							String neighString;
							if(!isAnnotated(ccm,nucName))	//this (nucname) not annotated
								{
								neighColor=htmlColorNT;
								neighString="<font color=\"#ffffff\">n.t.</font>";
								}
							else if(!isAnnotated(ccm,nucName2))	//nucname2 not annotated
								{
								neighColor=htmlColorNA;
								neighString="n.a.";
								}
							else if(percLifeLen!=0) //Is neighbour
								{
								neighColor=scoreColor;
								neighString=percentFormat.format(percLifeLen);
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
							if(!ccm.contactsf.get(nucName).get(nucName2).isEmpty())
								{
								if(percLifeLen!=0)
									{
									boolean[] neighOverlaps=getOverlaps(ccm, nucName, nucName2);
									//Convert frame overlap to image
									timeString=getOverlapBar(neighOverlaps).toString();
									}
								else
									timeString=neighString="&nbsp;";
								}

							//bodyNeigh.append("<td bgcolor=\""+neighColor+"\"><tt>"+neighString+"</tt></td>\n");
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
		System.out.println("done");
		System.exit(0);
		}
	
	
	//TODO only add nucs which are not pos-empty
	public static boolean isAnnotated(CellContactMap lin, String nucname)
		{
		return lin.allNuc.contains(nucname);
		}
	

	/**
	 * Generate optimized HTML for overlaps by using RLE
	 */
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

	public static String getOverlapImage(int len, boolean current)
		{
		return "<img width=\""+len+"\" height=\""+cheight+"\" src=\""+(current ? 'n' : 'a')+"_bar.png\">";
		}


	/**
	 * Write bar image
	 */
	public static void writeBar(File file, Color col) throws IOException
		{
		BufferedImage bim=new BufferedImage(1,cheight,BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g=bim.createGraphics();
		g.setColor(col);
		g.fillRect(0,0,1,cheight);
		ImageIO.write(bim,"png",file);
		}
	}