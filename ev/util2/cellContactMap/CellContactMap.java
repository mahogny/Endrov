package util2.cellContactMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

import javax.imageio.ImageIO;


import endrov.data.EvData;
import endrov.data.EvDataXML;
import endrov.ev.*;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import endrov.nuc.NucVoronoi;
import endrov.util.EvFileUtil;
import endrov.util.EvParallel;
import endrov.util.Tuple;

/**
 * Calculate cell contact map
 * @author Johan Henriksson
 *
 */
public class CellContactMap
	{

	public static class OneLineage
		{
		public Map<Integer,NucVoronoi> fcontacts=new HashMap<Integer, NucVoronoi>();
		public NucLineage lin;
		public String name;
		public int numid;
		
		//nuc -> nuc -> frames
		public Map<String,Map<String,Set<Integer>>> contactsf=new TreeMap<String, Map<String,Set<Integer>>>();
		
		public Map<String,Integer> lifelen=new HashMap<String,Integer>();
		
		public void addLifelen(String a)
			{
			Integer len=lifelen.get(a);
			if(len==null)
				len=0;
			len++;
			lifelen.put(a,len);
			}
		
		public void addFrame(String a, String b, int f)
			{
			addFrame1(a, b, f);
			addFrame1(b, a, f);
			}
		private void addFrame1(String a, String b, int f)
			{
			Map<String,Set<Integer>> na=contactsf.get(a);
			if(na==null)
				contactsf.put(a,na=new TreeMap<String,Set<Integer>>());
			Set<Integer> sa=na.get(b);
			if(sa==null)
				na.put(b, sa=new TreeSet<Integer>());
			sa.add(f);
			}
		
		
		public OneLineage(String file)
			{
			//Load lineage
			EvData ost=new EvDataXML(file);
			lin=ost.getObjects(NucLineage.class).iterator().next();
			name=file.toString();
			}
		
		
		public void calcneigh(TreeSet<String> nucNames)
			{
			//Prepare different indexing
			for(String n:nucNames)
				{
				Map<String,Set<Integer>> u=new HashMap<String, Set<Integer>>();
				for(String m:nucNames)
					u.put(m,new HashSet<Integer>());
				contactsf.put(n, u);
				}

			//Prepare life length
			for(String n:nucNames)
				lifelen.put(n, 0);
			
			//Go through all frames
			int numframes=0;
			for(int curframe=lin.firstFrameOfLineage();curframe<lin.lastFrameOfLineage();curframe++)
				{
				numframes++;
                                  //				if(numframes>30)					break;
				//interpolate
				Map<NucPair, NucLineage.NucInterp> inter=lin.getInterpNuc(curframe);
				if(curframe%100==0)
					System.out.println(curframe);
				try
					{
					//Get neighbours
					NucVoronoi nvor=new NucVoronoi(inter);
					fcontacts.put(curframe, nvor);
					//TODO override: add a ~ a relation
					//TODO
					//TODO if parent neigh at this frame, remove child
					
					//Turn into more suitable index ordering for later use
					for(Tuple<String, String> e:nvor.getNeighPairSet())
						addFrame(e.fst(),e.snd(),curframe);
					//Calculate lifelen
					for(Map.Entry<NucPair, NucLineage.NucInterp> e:inter.entrySet())
						if(e.getValue().isVisible())
							addLifelen(e.getKey().snd());
					}
				catch (Exception e)
					{
//					e.printStackTrace();
					}
				}
			}
		}
	
	
	
	//TODO: use imserv instead
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		NumberFormat percentFormat=NumberFormat.getInstance();
		percentFormat.setMinimumFractionDigits(1);
		percentFormat.setMaximumFractionDigits(1);
		
		
		String[] files=new String[]{"/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords.ost/rmd.ostxml",
				"/Volumes/TBU_main02/ost4dgood/N2_071116.ost/rmd.ostxml",
				"/Volumes/TBU_main02/ost4dgood/N2_071114.ost/rmd.ostxml",
				//"/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/rmd.ostxml",
				"/Volumes/TBU_main02/ostxml/model/stdcelegansNew4.ostxml",
				"/Volumes/TBU_main02/ost4dgood/TB2164_080118.ost/rmd.ostxml",
				"/Volumes/TBU_main02/ost4dgood/TB2142_071129.ost/rmd.ostxml",
						"/Volumes/TBU_main03/ost4dgood/TB2167_0804016.ost/rmd.ostxml",
							"/Volumes/TBU_main02/ost4dgood/N2greenLED080206.ost/rmd.ostxml",
				};
		
		List<OneLineage> lins=new LinkedList<OneLineage>();
		
		//Load all
		for(String s:files)
			lins.add(new OneLineage(s));

		
		
		NucLineage reflin=new EvDataXML("/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/rmd.ostxml").getObjects(NucLineage.class).iterator().next();
		final TreeSet<String> nucNames=new TreeSet<String>(reflin.nuc.keySet());
			
		
		
		//Calc neigh
		lins=EvParallel.map(lins, new EvParallel.FuncAB<OneLineage, OneLineage>(){
			public OneLineage func(OneLineage in)
				{
				in.calcneigh(nucNames);
				return in;
				}
		});
		
		//Order by name
		Map<String,OneLineage> orderedLin=new TreeMap<String, OneLineage>();
		for(OneLineage lin:lins)
			orderedLin.put(lin.name, lin);
		int numid=0;
		lins=new LinkedList<OneLineage>();
		for(OneLineage lin:orderedLin.values())
			{
			lin.numid=numid++;
			lins.add(lin);
			}
		
		System.out.println("Writing files");
		
		
		File targetdir=new File("/Volumes/TBU_main03/userdata/cellcontactmap/");
		
		String updateTime=new Date().toString();
		
		try
			{
			String neighTemplate=EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap.class.getResource("neigh.htm")));
			
			//Images for bars
			writeBar(new File(targetdir,"n_bar.png"),Color.black);
			writeBar(new File(targetdir,"a_bar.png"),Color.white);
			
			//Write cell list files
			StringBuffer mainSingleOut=new StringBuffer();
			StringBuffer mainTreeOut=new StringBuffer();
			mainSingleOut.append(EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap.class.getResource("main_single_header.htm"))));
			mainTreeOut.append(EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap.class.getResource("main_tree_header.htm"))));
			for(String nucName:nucNames)
				{
				mainSingleOut.append("<a href=\""+nucName+"_neigh.htm\">"+nucName+"</a></br>");
				mainTreeOut.append("<a href=\""+nucName+"_neightime.htm\">"+nucName+"</a></br>");
				}
			mainSingleOut.append("</body></html>");
			mainTreeOut.append("</body></html>");
			EvFileUtil.writeFile(new File(targetdir,"main_single.htm"),mainSingleOut.toString());
			EvFileUtil.writeFile(new File(targetdir,"main_tree.htm"),mainTreeOut.toString());
			
			//List datasets
			StringBuffer outDatasets=new StringBuffer();
			for(OneLineage lin:lins)
				outDatasets.append(""+lin.numid+": "+lin.name+" <br/>");

			int clength=50; //[px]

			
			//Write out HTML, cell by cell. Reference lineage is the first one in the list
			//nucName: everything in the file is about this cell
			for(String nucName:nucNames)
				{
				StringBuffer bodyNeigh=new StringBuffer();
				StringBuffer bodyTime=new StringBuffer();
				
				//Sub header: lin# & cell name
				StringBuffer subhMain=new StringBuffer();
				StringBuffer subhTime=new StringBuffer(); //f2
				for(OneLineage lin:lins)
					{
					subhMain.append("<td><tt>"+lin.numid+"</tt></td>");
					subhTime.append("<td width=\""+clength+"\"><tt>"+lin.numid+"</tt></td>");
					}
				
				//Compare with all other nuclei
				nextNuc2: for(String nucName2:nucNames)
					{
					int notAnnotated=0;
					int sa=0;
					for(OneLineage lin:lins)
						if(lin.lin.nuc.containsKey(nucName2))
							{
							//Count co-occurance in the recordings where nucname2 is annotated
							if(!lin.contactsf.get(nucName).get(nucName2).isEmpty())
								sa++;
							}
						else
							//Count number of recordings in which this cell (nucname2) is not annotated
							notAnnotated++;
					int annotated=lins.size()-notAnnotated;
					
					//Skip this cell if none of the recordings has it as a neighbour
					if(sa==0 && !nucName.equals(nucName2))
						continue nextNuc2;
					
					double hits=(double)sa/(annotated);
					int hc=(int)(255-hits*255);
					
					String hex=Integer.toHexString(hc);
					if(hex.length()==1)
						hex="0"+hex;
					String htcolor="#ff"+hex+hex;
					if(nucName.equals(nucName2)) //Itself
						htcolor="#33ccff";
					
					
					Map<OneLineage,String> lincolor=new HashMap<OneLineage, String>();
					Map<OneLineage,String> linstr=new HashMap<OneLineage, String>();
					Map<OneLineage,Boolean> linannot=new HashMap<OneLineage, Boolean>();
					for(OneLineage lin:lins)
						{
						String thiscolor;
						String thisstr;
						boolean thisannot;
						//this (nucname) not annotated
						if(!lin.lin.nuc.containsKey(nucName))
							{
							thiscolor="#666666";
							thisstr="<font color=\"#ffffff\">n.t.</font>";
							thisannot=false;
							}
						//nucname2 not annotated
						else if(!lin.lin.nuc.containsKey(nucName2))
							{
							thiscolor="#cccccc";
							thisstr="n.a.";
							thisannot=false;
							}
						//Is not neighbour
						else if(lin.contactsf.get(nucName).get(nucName2).isEmpty())
							{
							thiscolor="#ffffff";
							thisstr="&nbsp;";
							thisannot=true;
							}
						//Is neighbour
						else
							{
							thiscolor=htcolor;
							thisstr="an";
							thisannot=true;
							}
						
						lincolor.put(lin, thiscolor);
						linstr.put(lin,thisstr);
						linannot.put(lin,thisannot);
						}
					
					//Name in table
					String line="<tr><td bgcolor=\""+htcolor+"\"><tt><a href=\"FILE\">"+nucName2+"</a></tt></td><td bgcolor=\""+htcolor+"\"><tt>"+sa+"/"+annotated+"</tt></td>\n";
					bodyNeigh.append(line.replace("FILE",nucName2+"_neigh.htm"));
					bodyTime.append(line.replace("FILE",nucName2+"_neightime.htm"));

					//Contact map itself
					for(OneLineage lin:lins)
						{
						String col=lincolor.get(lin);
						double perc=100*(double)lin.contactsf.get(nucName).get(nucName2).size()/lin.lifelen.get(nucName);
						StringBuffer imgcode=new StringBuffer();
						String stri="";
						if(!linannot.get(lin))
							stri=linstr.get(lin);
						else if(perc>1 && !lin.contactsf.get(nucName).get(nucName2).isEmpty())
							{
							stri=percentFormat.format(perc);
							int sl=Math.round((lin.lin.nuc.get(nucName).lastFrame()-lin.lin.nuc.get(nucName).firstFrame()));
							
							boolean[] neighOverlaps=new boolean[clength];

							//rewrite this code using retain
							int fa=sl/clength;
							nextcurp: for(int curp=0;curp<clength;curp++)
								{
								int m=curp*fa;
								int fl=(int)Math.floor(m);
								int ce=(int)Math.ceil(m);
								if(ce>=0 && ce<=sl && fl>=0 && fl<=sl)
									for(int x:lin.contactsf.get(nucName).get(nucName2))
										{
										int nf=Math.round(x-lin.lin.nuc.get(nucName).firstFrame());
										if(nf==ce || nf==fl)
											{
											neighOverlaps[curp]=true;
											continue nextcurp; //Optimization
											}
										}
								}
							
							//Convert frame overlap to image
							//TODO stretch image
							for(boolean b:neighOverlaps)
								{
								char c=b ? 'n' : 'a';
								imgcode.append("<img src=\""+c+"_bar.png\">");
								}
							
							}
						else
							{
							stri="&nbsp;";
							}
						bodyNeigh.append("<td bgcolor=\""+col+"\"><tt>"+stri+"</tt></td>\n");

						//replace if
						if(stri.indexOf('n')!=-1)
							;
						else
							{
							stri=imgcode.toString();
							col="#ffffff";
							}
						bodyTime.append("<td bgcolor=\""+col+"\"><tt>"+stri+"</tt></td>\n");
						}
				
					}
				
				

				//Output
				String out=neighTemplate
					.replace("SUBHEADER",subhMain)
					.replace("NUCNAME", nucName)
					.replace("COLSPAN",""+lins.size())
					.replace("DATASETS", outDatasets)
					.replace("UPDATETIME",updateTime);
				
				EvFileUtil.writeFile(new File(targetdir,nucName+"_neigh.htm"), out.replace("CONTACTTABLE", bodyNeigh));
				EvFileUtil.writeFile(new File(targetdir,nucName+"_neightime.htm"), out.replace("CONTACTTABLE", bodyTime));
				}
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
	
		System.out.println("done");
		System.exit(0);
		}
	
	public static void writeBar(File file, Color col) throws IOException
		{
		int cheight=13; //[px]
		BufferedImage bim=new BufferedImage(1,cheight,BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g=bim.createGraphics();
		g.setColor(col);
		g.fillRect(0,0,1,cheight);
		ImageIO.write(bim,"png",file);
		}
	}
