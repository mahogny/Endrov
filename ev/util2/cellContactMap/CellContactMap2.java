package util2.cellContactMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;

//import endrov.data.*;
import endrov.ev.*;
import endrov.imageset.Imageset;
import endrov.imagesetImserv.EvImserv;
import endrov.nuc.*;
import endrov.util.*;


/**
 * Calculate cell contact map
 * 
 * about 40min on xeon
 * 
 * @author Johan Henriksson, Jurgen Hench
 *
 */
public class CellContactMap2
	{
	public static String htmlColorNotNeigh="#ffffff";
	public static String htmlColorNA="#cccccc";
	public static String htmlColorNT="#666666";
	public static String htmlColorSelf="#33ccff";
	public static final int clength=50; //[px]
	public static final int cheight=20; //[px]
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static class OneLineage
		{
		public Map<Integer,NucVoronoi> fcontacts=new HashMap<Integer, NucVoronoi>();
		public NucLineage lin;
		public String name;
		public int numid;
		//nuc -> nuc -> frames
		public Map<String,Map<String,SortedSet<Integer>>> contactsf=new TreeMap<String, Map<String,SortedSet<Integer>>>();
		//nuc -> lifetime
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
			Map<String,SortedSet<Integer>> na=contactsf.get(a);
			if(na==null)
				contactsf.put(a,na=new TreeMap<String,SortedSet<Integer>>());
			SortedSet<Integer> sa=na.get(b);
			if(sa==null)
				na.put(b, sa=new TreeSet<Integer>());
			sa.add(f);
			}
		
		
		public void calcneigh(TreeSet<String> nucNames)
			{
			//Prepare different indexing
			for(String n:nucNames)
				{
				Map<String,SortedSet<Integer>> u=new HashMap<String, SortedSet<Integer>>();
				for(String m:nucNames)
					u.put(m,new TreeSet<Integer>());
				contactsf.put(n, u);
				}

			//Prepare life length
			for(String n:nucNames)
				lifelen.put(n, 0);
			
			
			//Go through all frames
//			int numframes=0;
			for(int curframe=lin.firstFrameOfLineage();curframe<lin.lastFrameOfLineage();curframe++)
				{
//				numframes++;
				/////////////////////////////
                                  				if(curframe>1400)					break;
                                  				////////////////////
                                  				
				//interpolate
				Map<NucPair, NucLineage.NucInterp> inter=lin.getInterpNuc(curframe);
				if(curframe%100==0)
					System.out.println(curframe);
				try
					{
					//Eliminate cells not in official list or invisible
					Map<NucPair, NucLineage.NucInterp> interclean=new HashMap<NucPair, NucLineage.NucInterp>();
					for(Map.Entry<NucPair, NucLineage.NucInterp> e:inter.entrySet())
						if(e.getValue().isVisible() && nucNames.contains(e.getKey().snd()))
							interclean.put(e.getKey(), e.getValue());
					inter=interclean;
					
					//Add false nuclei at distance to make voronoi calc possible
					if(!inter.isEmpty())
						{
						double r=3000; //300 is about the embryo. embryo is not centered in reality.
						
						NucLineage.NucInterp i1=new NucLineage.NucInterp();
						i1.pos=new NucLineage.NucPos();
						i1.frameBefore=0;
						i1.pos.x=r;

						NucLineage.NucInterp i2=new NucLineage.NucInterp();
						i2.pos=new NucLineage.NucPos();
						i2.frameBefore=0;
						i2.pos.x=-r;

						NucLineage.NucInterp i3=new NucLineage.NucInterp();
						i3.pos=new NucLineage.NucPos();
						i3.frameBefore=0;
						i3.pos.y=-r;

						NucLineage.NucInterp i4=new NucLineage.NucInterp();
						i4.pos=new NucLineage.NucPos();
						i4.frameBefore=0;
						i4.pos.y=-r;

						inter.put(new NucPair(null,":::1"), i1);
						inter.put(new NucPair(null,":::2"), i2);
						inter.put(new NucPair(null,":::3"), i3);
						inter.put(new NucPair(null,":::4"), i4);
						}
					
//					System.out.println("# inter "+inter.size());
					
					//Get neighbours
					NucVoronoi nvor=new NucVoronoi(inter,true);
					fcontacts.put(curframe, nvor);
					//TODO if parent neigh at this frame, remove child
					
					//Turn into more suitable index ordering for later use
					for(Tuple<String, String> e:nvor.getNeighPairSet())
						addFrame(e.fst(),e.snd(),curframe);
					//Calculate lifelen
					for(Map.Entry<NucPair, NucLineage.NucInterp> e:inter.entrySet())
						addLifelen(e.getKey().snd());
					
					//Calculate area
					nvor.calcContactArea();
					}
				catch (Exception e)
					{
//					e.printStackTrace();
					}
				}
			}
		}
	
	
	
	public static void writeLineageNeighDistances(OneLineage lin) throws IOException
		{
		PrintWriter pw=new PrintWriter(new FileWriter(new File("/Volumes/TBU_main03/userdata/cellcontactmap/dist.csv")));
		for(Map.Entry<Integer, NucVoronoi> entry:lin.fcontacts.entrySet())
			{
//			NucLineage thelin=lins.get(0).lin;
			Map<NucPair,NucLineage.NucInterp> inter=lin.lin.getInterpNuc(entry.getKey());
			Map<String,NucLineage.NucInterp> inters=new HashMap<String, NucLineage.NucInterp>();
			for(Map.Entry<NucPair, NucLineage.NucInterp> e:inter.entrySet())
				inters.put(e.getKey().snd(),e.getValue());

			boolean first=true;
			for(Tuple<String,String> pair:entry.getValue().getNeighPairSet())
				if(inters.containsKey(pair.fst()) && inters.containsKey(pair.snd()) && !pair.fst().startsWith(":") && !pair.snd().startsWith(":"))
					{
					if(!first)
						pw.print(',');
					Vector3d vA=inters.get(pair.fst()).pos.getPosCopy();
					Vector3d vB=inters.get(pair.snd()).pos.getPosCopy();
					vA.sub(vB);
					pw.print(vA.length());
					first=false;
					}
			pw.println();
			}
		pw.close();
		
		
		}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args)
		{
		try
			{
			Log.listeners.add(new StdoutLog());
			EV.loadPlugins();

			NumberFormat percentFormat=NumberFormat.getInstance();
			percentFormat.setMinimumFractionDigits(1);
			percentFormat.setMaximumFractionDigits(1);

			List<OneLineage> lins=new LinkedList<OneLineage>();

			/*
			//Load from disk right away
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
			
			//Load all
			for(String s:files)
				{
				//Load lineage
				EvData ost=new EvDataXML(s);
				OneLineage olin=new OneLineage();
				olin.lin=ost.getObjects(NucLineage.class).iterator().next();
				olin.name=s.toString();
				lins.add(olin);
//			lins.add(new OneLineage(s));
				}
			NucLineage reflin=new EvDataXML("/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/rmd.ostxml").getObjects(NucLineage.class).iterator().next();
				*/

			//bottle neck: building imageset when not needed. fix in Endrov3/OST4
			
			///////////
			System.out.println("Connecting");
			String url="imserv://:@localhost/";
			String query="not trash and CCM";
			EvImserv.EvImservSession session=EvImserv.getSession(new EvImserv.ImservURL(url));
//			String[] imsets=session.conn.imserv.getDataKeys(query);
			String[] imsets=new String[]{"celegans2008.2"};
			//TODO make a getDataKeysWithTrash, exclude by default?
			System.out.println("Loading imsets");
			
			
			for(String s:imsets)
				{
				System.out.println("loading "+s);
				Imageset im=EvImserv.getImageset(url+s);
				OneLineage olin=new OneLineage();
				olin.lin=im.getObjects(NucLineage.class).iterator().next();
				olin.name=im.getMetadataName();
				lins.add(olin);
				}
			NucLineage reflin=EvImserv.getImageset(url+"celegans2008.2").getObjects(NucLineage.class).iterator().next();
			//////////

			final TreeSet<String> nucNames=new TreeSet<String>(reflin.nuc.keySet());



			//Calc neigh
			lins=EvParallel.map(lins, new EvParallel.FuncAB<OneLineage, OneLineage>(){
			public OneLineage func(OneLineage in)
				{
				in.calcneigh(nucNames);
				return in;
				}
			});

			//Output distances
//			writeLineageNeighDistances(lins.get(0));
			
			//Order by name
			Map<String,OneLineage> orderedLin=new TreeMap<String, OneLineage>();
			for(OneLineage lin:lins)
				orderedLin.put(lin.name, lin);
			int numid=1;
			lins=new LinkedList<OneLineage>();
			for(OneLineage lin:orderedLin.values())
				{
				lin.numid=numid++;
				lins.add(lin);
				}

			System.out.println("Writing files");


			File targetdirNeigh=new File("/Volumes/TBU_main03/userdata/cellcontactmapArea/neigh/");
			File targetdirTree=new File("/Volumes/TBU_main03/userdata/cellcontactmapArea/tree/");
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
					EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap2.class.getResource("main_single.htm")))
					.replace("BODY", mainSingleOut));
			EvFileUtil.writeFile(new File(targetdirTree,"index.htm"),
					EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap2.class.getResource("main_tree.htm")))
					.replace("BODY", mainTreeOut));

			//List datasets
			StringBuffer outDatasets=new StringBuffer();
			for(OneLineage lin:lins)
				outDatasets.append(""+lin.numid+": "+lin.name+" <br/>");

			//Write out HTML, cell by cell. Reference lineage is the first one in the list
			//nucName: everything in the file is about this cell
			String neighTemplate=EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap2.class.getResource("neigh.htm")));
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
				for(String nucName2:nucNames)
					{

					//Get annotation statistics
					int notAnnotated=0; //# not annotated
					int sa=0; //# co-occurance
					for(OneLineage lin:lins)
						if(isAnnotated(lin.lin, nucName2))
							{
							if(!lin.contactsf.get(nucName).get(nucName2).isEmpty())
								sa++;
							}
						else
							notAnnotated++;
					int annotated=lins.size()-notAnnotated;

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
						for(OneLineage lin:lins)
							{
							double percLifeLen=100*(double)lin.contactsf.get(nucName).get(nucName2).size()/lin.lifelen.get(nucName);
							if(lin.lifelen.get(nucName)==0 || percLifeLen<1)
								percLifeLen=0;

							//Formatting for non-time CCM
							String neighString;
							String neighColor;
							if(!isAnnotated(lin.lin,nucName))	//this (nucname) not annotated
								{
								neighColor=htmlColorNT;
								neighString="<font color=\"#ffffff\">n.t.</font>";
								}
							else if(!isAnnotated(lin.lin,nucName2))	//nucname2 not annotated
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
							if(!lin.contactsf.get(nucName).get(nucName2).isEmpty())
								{
								if(percLifeLen!=0)
									{
									int lifeLenFrames=Math.round((lin.lin.nuc.get(nucName).lastFrame()-lin.lin.nuc.get(nucName).firstFrame()));

									/*
									boolean[] neighOverlaps=new boolean[clength];
									for(int curp=0;curp<clength;curp++)
										{
										int m=(int)(curp*lifeLenFrames/(double)clength+lin.lin.nuc.get(nucName).firstFrame()); //corresponding frame
										SortedSet<Integer> frames=lin.contactsf.get(nucName).get(nucName2);
										if(frames.contains(m) || !frames.headSet(m).isEmpty() && !frames.tailSet(m).isEmpty())
											neighOverlaps[curp]=true;
										}*/
									
									
									double[] neighOverlapsD=new double[clength];
									double max=0;
									for(int curp=0;curp<clength;curp++)
										{
										int m=(int)(curp*lifeLenFrames/(double)clength+lin.lin.nuc.get(nucName).firstFrame()); //corresponding frame
										SortedSet<Integer> frames=lin.contactsf.get(nucName).get(nucName2);
										if(frames.contains(m) || (!frames.headSet(m).isEmpty() && !frames.tailSet(m).isEmpty()))
											{ //() change
											int frame1;
											int frame2;
											if(frames.contains(m))
												frame1=frame2=m;
											else
												{
												frame1=frames.headSet(m).last();
												frame2=frames.tailSet(m).first();
												}
											
											
											Double carea1=lin.fcontacts.get(frame1).contactArea.get(nucName).get(nucName2);
											Double carea2=lin.fcontacts.get(frame2).contactArea.get(nucName).get(nucName2);
											double tarea1=lin.fcontacts.get(frame1).totArea.get(nucName);
											double tarea2=lin.fcontacts.get(frame2).totArea.get(nucName);
											System.out.println("tot area "+nucName+"    "+tarea1+" "+tarea2);
											if(carea1==null) carea1=0.0;
											if(carea2==null) carea2=0.0;
											if(tarea1<0 || tarea2<0)
												neighOverlapsD[curp]=-1;
											else
												{
												neighOverlapsD[curp]=
													EvGeomUtil.interpolate(frame1, carea1/tarea1, frame2, carea2/tarea2, m);
												//neighOverlapsD[curp]=1; /////
												if(max<neighOverlapsD[curp])
													max=neighOverlapsD[curp];
												}
											}
										}
									for(int curp=0;curp<clength;curp++)
										if(neighOverlapsD[curp]>=0)
											neighOverlapsD[curp]/=max;

									//Convert frame overlap to image
									timeString=getOverlapBar(neighOverlapsD, targetdirTree).toString();
									
									}
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
					.replace("COLSPAN",""+lins.size());
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
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("done");
		System.exit(0);
		}
	
	public static boolean isAnnotated(NucLineage lin, String nucname)
		{
		return lin.nuc.containsKey(nucname) && !lin.nuc.get(nucname).pos.isEmpty();
		}
	

	
	private static int barImageNum=0;
	public static String getOverlapBar(double[] neighOverlaps, File root) throws IOException
		{
		BufferedImage bim=new BufferedImage(clength,cheight,BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g=bim.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,clength,cheight);
		File rootbar=new File(root,"bars");
		
		for(int i=0;i<clength;i++)
			{
			int lev=(int)(neighOverlaps[i]*cheight);
			if(lev<0)
				{
				g.setColor(Color.blue);
				lev=-lev;
				}
			else
				g.setColor(Color.black);
			g.fillRect(i, cheight-1-lev, 1, lev);
			}
		String tf="bar_"+barImageNum+".png";
		barImageNum++;
		rootbar.mkdirs();
		File outfile=new File(rootbar,tf);
		ImageIO.write(bim,"png",outfile);
		
		return "<img width=\""+clength+"\" height=\""+cheight+"\" src=\"bars/"+tf+"\">";
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



//rewrite this code using retain
//int fa=lifeLenFrames/clength;
/*nextcurp: */
/*
int m=curp*lifeLenFrames/clength; //corresponding frame
int fl=(int)Math.floor(m);
int ce=(int)Math.ceil(m);
//if(ce>=0 && ce<=lifeLenFrames && fl>=0 && fl<=lifeLenFrames)
	for(int x:lin.contactsf.get(nucName).get(nucName2))
		{
		int nf=Math.round(x-lin.lin.nuc.get(nucName).firstFrame());
		if(nf==ce || nf==fl)
			{
			neighOverlaps[curp]=true;
			continue nextcurp; //Optimization
			}
		}

 */
//if(percLifeLen>1)
/* || nucName.equals(nucName2)*/

//Override color for match on itself when it exists
/*							if(nucName.equals(nucName2) && lin.lin.nuc.containsKey(nucName))
								{
//								timeString="";
//								neighString="100%";
								neighColor=timeColor=htmlColorSelf;
								}*/


//TODO stretch image
/*									StringBuffer imgcode=new StringBuffer();
									for(boolean b:neighOverlaps)
										imgcode.append("<img src=\""+(b ? 'n' : 'a')+"_bar.png\">");*/


//Override occurance for self-referal
//if(nucName.equals(nucName2))
//sa=occurance;

