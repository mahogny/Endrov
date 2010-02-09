/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.cellContactMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;

import util2.ConnectImserv;

//import endrov.data.*;
import endrov.data.EvData;
import endrov.ev.*;
import endrov.imagesetImserv.EvImserv;
import endrov.nuc.*;
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
public class CellContactMap
	{
	public static String htmlColorNotNeigh="#ffffff";
	public static String htmlColorNA="#cccccc";
	public static String htmlColorNT="#666666";
	public static String htmlColorSelf="#33ccff";
	public static final int clength=50; //[px]
	public static final int cheight=13; //[px]
//	public static EvDecimal frameInc=new EvDecimal(5);
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static class OneLineage
		{
		public Map<EvDecimal,NucVoronoi> fcontacts=new HashMap<EvDecimal, NucVoronoi>();
		public NucLineage lin;
		public String name;
		public int numid;
		//nuc -> nuc -> frames
		public Map<String,Map<String,SortedSet<EvDecimal>>> contactsf=new TreeMap<String, Map<String,SortedSet<EvDecimal>>>();
		//nuc -> lifetime
		public Map<String,Integer> lifelen=new HashMap<String,Integer>();

		public EvDecimal frameInc=new EvDecimal(30);
		//20 steps=150s!
		
		public TreeSet<EvDecimal> framesTested=new TreeSet<EvDecimal>();
		
		public void addLifelen(String a)
			{
			Integer len=lifelen.get(a);
			if(len==null)
				len=0;
			len++;
			lifelen.put(a,len);
			}
		
		public void addFrame(String a, String b, EvDecimal f)
			{
			addFrame1(a, b, f);
			addFrame1(b, a, f);
			}
		private void addFrame1(String a, String b, EvDecimal f)
			{
			Map<String,SortedSet<EvDecimal>> na=contactsf.get(a);
			if(na==null)
				contactsf.put(a,na=new TreeMap<String,SortedSet<EvDecimal>>());
			SortedSet<EvDecimal> sa=na.get(b);
			if(sa==null)
				na.put(b, sa=new TreeSet<EvDecimal>());
			sa.add(f);
			}
		
		
		public void calcneigh(TreeSet<String> nucNames)
			{
			//Prepare different indexing
			for(String n:nucNames)
				{
				Map<String,SortedSet<EvDecimal>> u=new HashMap<String, SortedSet<EvDecimal>>();
				for(String m:nucNames)
					u.put(m,new TreeSet<EvDecimal>());
				contactsf.put(n, u);
				}

			//Prepare life length
			for(String n:nucNames)
				lifelen.put(n, 0);

			//temp: only do model
			//if(!name.equals("celegans2008.2"))
			//	return;
			
			PrintWriter pw=null;
			try
				{
				if(name.equals("celegans2008.2"))
					pw=new PrintWriter(new FileWriter(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/numneigh.txt")));
				}
			catch (IOException e1)
				{
				System.out.println("failed to open neigh count output");
				System.exit(1);
				}
			
			
			//Go through all frames
			int numframes=0;
			for(EvDecimal curframe=lin.firstFrameOfLineage().fst();curframe.less(lin.lastFrameOfLineage().fst());curframe=curframe.add(frameInc))
				{
				framesTested.add(curframe);
				numframes++;
				/////////////////////////////
//                                  				if(numframes>200)					break;
                                  				////////////////////
                                  				
				//interpolate
				Map<NucSel, NucLineage.NucInterp> inter=lin.getInterpNuc(curframe);
				if(curframe.intValue()%100==0)
					System.out.println(curframe);
				try
					{
					//Eliminate cells not in official list or invisible
					Map<NucSel, NucLineage.NucInterp> interclean=new HashMap<NucSel, NucLineage.NucInterp>();
					int numRealNuc=interclean.size();
					for(Map.Entry<NucSel, NucLineage.NucInterp> e:inter.entrySet())
						if(e.getValue().isVisible() && nucNames.contains(e.getKey().snd()))
							interclean.put(e.getKey(), e.getValue());
					int numcleancell=interclean.size();
					inter=interclean;
					
					//Add false nuclei at distance to make voronoi calc possible
					if(!inter.isEmpty())
						{
						double r=3000; //300 is about the embryo. embryo is not centered in reality.
						
						NucLineage.NucInterp i1=new NucLineage.NucInterp();
						i1.pos=new NucLineage.NucPos();
						i1.frameBefore=EvDecimal.ZERO;
						i1.pos.x=r;

						NucLineage.NucInterp i2=new NucLineage.NucInterp();
						i2.pos=new NucLineage.NucPos();
						i2.frameBefore=EvDecimal.ZERO;
						i2.pos.x=-r;

						NucLineage.NucInterp i3=new NucLineage.NucInterp();
						i3.pos=new NucLineage.NucPos();
						i3.frameBefore=EvDecimal.ZERO;
						i3.pos.y=-r;

						NucLineage.NucInterp i4=new NucLineage.NucInterp();
						i4.pos=new NucLineage.NucPos();
						i4.frameBefore=EvDecimal.ZERO;
						i4.pos.y=-r;

						inter.put(new NucSel(null,":::1"), i1);
						inter.put(new NucSel(null,":::2"), i2);
						inter.put(new NucSel(null,":::3"), i3);
						inter.put(new NucSel(null,":::4"), i4);
						}
					
//					System.out.println("# inter "+inter.size());
					
					//Get neighbours
					NucVoronoi nvor=new NucVoronoi(inter,true);
					fcontacts.put(curframe, nvor);
					//TODO if parent neigh at this frame, remove child?
					
					//Turn into more suitable index ordering for later use
					for(Tuple<String, String> e:nvor.getNeighPairSet())
						addFrame(e.fst(),e.snd(),curframe);
					//Calculate lifelen
					for(Map.Entry<NucSel, NucLineage.NucInterp> e:inter.entrySet())
						addLifelen(e.getKey().snd());
					
					//Count neigh
					if(pw!=null)
						{
						int numContact=nvor.getNeighPairSetIndex().size()-numRealNuc;
						pw.println(""+curframe+"\t"+numContact+"\t"+numcleancell);
						}
					
					}
				catch (Exception e)
					{
//					e.printStackTrace();
					}
				}
			if(pw!=null)
				pw.close();

			/*
			if(name.equals("celegans2008.2"))
				{
				try
					{
					PrintWriter cp=new PrintWriter(new FileWriter(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/contactdur.txt")));
					for(String n1:contactsf.keySet())
						if(lin.nuc.containsKey(n1))
							for(String n2:contactsf.get(n1).keySet())
								if(lin.nuc.containsKey(n2) && 
										n2.compareTo(n1)>0 && //Remove duplicate and self-contacts 
//										lin.nuc.get(n1).child.size()>1 && lin.nuc.get(n2).child.size()>1)
									!lin.nuc.get(n1).child.isEmpty() && !lin.nuc.get(n2).child.isEmpty()) //For consistency with diff table
									{
									SortedSet<EvDecimal> s=contactsf.get(n1).get(n2);
									if(!s.isEmpty())
										{
										cp.println(""+(s.last().subtract(s.first()).add(1))+"\t"+s.first());  //+1 Can be discussed
										}
									}
					cp.close();
					//System.exit(0); //temp
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
				}
				*/

			}
		}
	
	
	
	public static void writeLineageNeighDistances(OneLineage lin) throws IOException
		{
		PrintWriter pw=new PrintWriter(new FileWriter(new File("/Volumes/TBU_main03/userdata/cellcontactmap/dist.csv")));
		for(Map.Entry<EvDecimal, NucVoronoi> entry:lin.fcontacts.entrySet())
			{
//			NucLineage thelin=lins.get(0).lin;
			Map<NucSel,NucLineage.NucInterp> inter=lin.lin.getInterpNuc(entry.getKey());
			Map<String,NucLineage.NucInterp> inters=new HashMap<String, NucLineage.NucInterp>();
			for(Map.Entry<NucSel, NucLineage.NucInterp> e:inter.entrySet())
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
	
	public static int countTrue(boolean[] b)
		{
		int count=0;
		for(boolean c:b)
			if(c)
				count++;
		return count;
		}
	
	public static int getDivRound(String name)
		{
		if(name.startsWith("AB"))
			return name.length()-2+0;
		else if(name.startsWith("C"))
			return name.length()-1+2;
		else if(name.startsWith("D"))
			return name.length()-1+3;
		else if(name.endsWith("'"))
			return Integer.parseInt(name.substring(1, 2))-1;
		else if(name.equals("EMS"))
			return 1;
		else if(name.startsWith("E"))
			return name.length()-1+2;
		else if(name.startsWith("Z"))
			return name.length()-1+4;
		else if(name.startsWith("MS"))
			return name.length()-2+2;
		else
			return 0;
		}
	
	/*
	public static boolean maybeOverlaps(String name1, String name2)
		{
		if(name1.equals(name2))
			return false;
		return true;
		}
*/	
	
	public static double getOverlapPercent(OneLineage lin, String nucName, String nucName2)
		{
		return countTrue(getOverlaps(lin, nucName, nucName2))/(double)clength;
		}

	/**
	 * Calculate overlap array for two cells
	 */
	public static boolean[] getOverlaps(OneLineage lin, String nucName, String nucName2)
		{
	


		boolean[] neighOverlaps=new boolean[clength];
		
		if(lin.contactsf.get(nucName)==null)
			return neighOverlaps;
		
		
		Set<EvDecimal> ov=lin.contactsf.get(nucName).get(nucName2);
		
		if(ov==null || ov.isEmpty())
			return neighOverlaps;
		NucLineage.Nuc thisNuc=lin.lin.nuc.get(nucName);
		EvDecimal firstFrame=thisNuc.getFirstFrame();
		EvDecimal lastFrame=thisNuc.getLastFrame();
		//This could be a potential fix for the last-frame-is-not-in-contact problem.
		//Problem is, it does so by ignoring the last part which has other problems.
		//EvDecimal lastFrame=thisNuc.pos.lastKey();
		
		
		SortedMap<EvDecimal,Boolean> isNeighMap=new TreeMap<EvDecimal, Boolean>();
		//Could restrict better using lifetime
		for(EvDecimal f:lin.framesTested.tailSet(firstFrame))
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
			isNeighMap.put(thisNuc.getLastFrame(),true);
		
		
		

		EvDecimal lifeLenFrames=(lastFrame.subtract(firstFrame));

		for(int curp=0;curp<clength;curp++)
			{
			//For each curp, map to time, check which keyframe (for neighcheck) is closest.
			//Check if it is a neighbour in this frame
			EvDecimal m=firstFrame.add(lifeLenFrames.multiply(curp+0.5).divide(clength));
			
			EvDecimal closestFrame=EvListUtil.closestFrame(isNeighMap, m);
//			if(closestFrame!=null)
			if(isNeighMap.get(closestFrame))
				neighOverlaps[curp]=true;
			}
		
		
		/*
		for(int curp=0;curp<clength;curp++)
			{
			EvDecimal m=lin.lin.nuc.get(nucName).firstFrame().add(lifeLenFrames.multiply(curp).divide(clength)); //corresponding frame
			SortedSet<EvDecimal> frames=lin.contactsf.get(nucName).get(nucName2);
			if(frames.contains(m) || !frames.headSet(m).isEmpty() && !frames.tailSet(m).isEmpty())
				neighOverlaps[curp]=true;
			}
		*/
		/*
		for(int curp=0;curp<clength;curp++)
			{

			//Idea here is to slide a window over of size frameInc. it *should* it something since it has the same spacing
			EvDecimal m=lin.lin.nuc.get(nucName).firstFrame().add(lifeLenFrames.multiply(curp).divide(clength));
			
			//TODO: subtract 1, and timestep?
			
			SortedSet<EvDecimal> frames=lin.contactsf.get(nucName).get(nucName2);
			SortedSet<EvDecimal> tailframes=frames.tailSet(m);
			if(!tailframes.isEmpty() && tailframes.iterator().next().subtract(m).lessEqual(lin.frameInc))
				neighOverlaps[curp]=true;
			}
		*/
		return neighOverlaps;
		}
	
	
	
	
	
	
	public static void doesChildrenSplit(OneLineage theCE, File f, Set<String> nucNames) throws IOException
		{
		StringBuffer outSplitChild=new StringBuffer();
		for(String name:nucNames)
			{
			NucLineage.Nuc nuc=theCE.lin.nuc.get(name);
			if(nuc!=null && nuc.child.size()==2)
				{
				String cn1=nuc.child.first();
				String cn2=nuc.child.last();
				if(EvArrayUtil.all(getOverlaps(theCE, cn1, cn2)) || EvArrayUtil.all(getOverlaps(theCE, cn2, cn1)))
					;
				else
					outSplitChild.append(cn1+"\t"+cn2+"\n");
				}
			}
		EvFileUtil.writeFile(f, outSplitChild.toString());

		}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	public static void main(String[] args)
		{
		try
			{
			EvLog.listeners.add(new EvLogStdout());
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
			String url=ConnectImserv.url;
			String query="not trash and CCM";
			EvImserv.EvImservSession session=EvImserv.getSession(new EvImserv.ImservURL(url));
			String[] imsets=session.conn.imserv.getDataKeys(query);
//			String[] imsets=new String[]{"celegans2008.2"};
			//TODO make a getDataKeysWithTrash, exclude by default?
			System.out.println("Loading imsets");
			
			
			for(String s:imsets)
				{
				System.out.println("loading "+s);
				EvData data=EvData.loadFile(url+s);
				OneLineage olin=new OneLineage();
				olin.lin=data.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
				olin.name=data.getMetadataName();
				if(olin.name.equals("AnglerUnixCoords"))
					olin.frameInc=new EvDecimal("1"); //One cell can be like 20 frames
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

			
//			Collections.so
			
			//Compare CE and A model
			OneLineage theCE=orderedLin.get("celegans2008.2");
			OneLineage theA=orderedLin.get("AnglerUnixCoords");
			//LinkedList<Tuple<Double,String>> outDiffList=new LinkedList<Tuple<Double,String>>();
			StringBuffer outDiffList2=new StringBuffer();
			//LinkedList<String> listDiff=new LinkedList<String>();
			
			//Find cells in common for AE and CE
			HashSet<String> ceaNames=new HashSet<String>(theCE.lin.nuc.keySet());
			ceaNames.retainAll(theA.lin.nuc.keySet());
			for(String s:theA.lin.nuc.keySet())
				if(theA.lin.nuc.get(s).pos.isEmpty())
					ceaNames.remove(s);

			//Which cells are not in common? log
			StringBuffer hasCellDiff=new StringBuffer();
			hasCellDiff.append("---- Cells in CE but not AE -----\n");
			for(String s:theCE.lin.nuc.keySet())
				if(!theCE.lin.nuc.get(s).pos.isEmpty())
					if(!ceaNames.contains(s))
						hasCellDiff.append(s+"\n");
			hasCellDiff.append("---- Cells in AE but not CE -----\n");
			for(String s:theA.lin.nuc.keySet())
				if(!theA.lin.nuc.get(s).pos.isEmpty()) //true if has a child
					if(!ceaNames.contains(s)) //true if not contains
						hasCellDiff.append(s+"\n");
			hasCellDiff.append("------\n");
			EvFileUtil.writeFile(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/hasCellDiff.txt"), hasCellDiff.toString());
			
			//Skip cells which are beyond a certain time
			//Taken from /Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/volstats.txt  manually
			EvDecimal cutoffFrame=new EvDecimal(8590);
			for(String name:theCE.lin.nuc.keySet())
				{
				NucLineage.Nuc nuc=theCE.lin.nuc.get(name);
				if(nuc.pos.isEmpty() || nuc.pos.firstKey().greater(cutoffFrame))
					ceaNames.remove(name);
				}
			
			for(String name:ceaNames)
				for(String name2:ceaNames)
/*			for(String name:nucNames)
				for(String name2:nucNames)*/
					if(!name.equals(name2))
						{
						boolean ceHasChild=!theCE.lin.nuc.get(name).child.isEmpty() && !theCE.lin.nuc.get(name2).child.isEmpty();
						boolean aHasChild=!theA.lin.nuc.get(name).child.isEmpty() && !theA.lin.nuc.get(name2).child.isEmpty();

						double c1=getOverlapPercent(theCE, name, name2);
						double c2=getOverlapPercent(theA, name, name2);

						if(c1+c2!=0)
							{
							NucLineage.Nuc nuc=theCE.lin.nuc.get(name);
							double dur=nuc.pos.isEmpty() ? 0 : nuc.getLastFrame().add(EvDecimal.ONE).subtract(nuc.getFirstFrame()).doubleValue();
							if(ceHasChild && aHasChild)
								outDiffList2.append(""+c1+"\t"+c2+"\t"+dur+"\t"+name+"\t"+name2+"\n");
							}

						}

			//Durations of contacts
			StringBuffer outDuration=new StringBuffer();
			StringBuffer outDuration2=new StringBuffer();
			for(String name:nucNames)
				for(String name2:nucNames)
					if(!name.equals(name2))
						{
						boolean ceBothHasChild=!theCE.lin.nuc.get(name).child.isEmpty() && !theCE.lin.nuc.get(name2).child.isEmpty();
						NucLineage.Nuc nuc=theCE.lin.nuc.get(name);
						NucLineage.Nuc nuc2=theCE.lin.nuc.get(name2);
						double dur=nuc.pos.isEmpty() ? 0 : nuc.getLastFrame().add(EvDecimal.ONE).subtract(nuc.getFirstFrame()).doubleValue();
						//duration should never ==0!!!
						if(ceBothHasChild && nuc.getFirstFrame().less(cutoffFrame) && nuc2.getFirstFrame().less(cutoffFrame))
							{
							if(!theCE.contactsf.get(name).get(name2).isEmpty()) //have frame in common
								{
								double c1=getOverlapPercent(theCE, name, name2);
								if(c1>0)
									{
									outDuration.append(""+dur*c1+"\t"+nuc.getFirstFrame()+"\t"+nuc.getLastFrame()+"\n");
									outDuration2.append(name+"\t"+name2+"\t"+dur*c1+"\t"+nuc.getFirstFrame()+"\t"+nuc.getLastFrame()+"\n");
									}
								//System.out.println("percent no contact!!!!! "+name+"  "+name2 +" "+theCE.contactsf.get(name).get(name2));
								}
							}
						}
			
					/*
					EvDecimal lifeLenFrames1=theCE.lin.nuc.get(name).pos.isEmpty() ? 
							EvDecimal.ZERO : theCE.lin.nuc.get(name).lastFrame().subtract(theCE.lin.nuc.get(name).firstFrame());
					EvDecimal lifeLenFrames2=theA.lin.nuc.get(name2).pos.isEmpty() ? 
							EvDecimal.ZERO : theA.lin.nuc.get(name2).lastFrame().subtract(theA.lin.nuc.get(name2).firstFrame());

					
					int framediff=numFrames1-numFrames2;
					EvDecimal first1=reflin.nuc.get(name).firstFrame();
					EvDecimal first2=reflin.nuc.get(name2).firstFrame();
					if(first1!=null && first2!=null)
						if(first1.less(new EvDecimal(2*3600+30*60)) && 
								first2.less(new EvDecimal(2*3600+30*60))	) //Within reasonable time
							if(Math.abs(framediff)>3)
								if(maybeOverlaps(name, name2))
									outDiffList.add(new Tuple<Integer, String>(framediff,framediff+"\t"+name+"\t"+name2+"\n"));
//									outDiff.append(framediff+"\t"+name+"\t"+name2+"\n");
						//listDiff.add();
						 * */
			/*
			Collections.sort(outDiffList, new Comparator<Tuple<Double,String>>(){
				public int compare(Tuple<Double, String> o1, Tuple<Double, String> o2)
					{
					return o1.fst().compareTo(o2.fst());
					}
			});
			StringBuffer outDiff=new StringBuffer();
			for(Tuple<Double,String> e:outDiffList)
				outDiff.append(e.snd());
			EvFileUtil.writeFile(new File("/Volumes/TBU_main03/userdata/cellcontactmap/CEAdiff.txt"), outDiff.toString());
			*/
			EvFileUtil.writeFile(new File("/Volumes/TBU_main03/userdata/cellcontactmap/CEAdiff.txt"), outDiffList2.toString());
			EvFileUtil.writeFile(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/contactdurNEW2.txt"), outDuration.toString());
			EvFileUtil.writeFile(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/contactdurNEW2.b.csv"), outDuration2.toString());
			
			
/*
			//Figure out duration
			StringBuffer outDuration=new StringBuffer();
			for(String name:nucNames)
				for(String name2:nucNames)
					if(name.compareTo(name2)>0)
						if(theCE.lin.nuc.containsKey(name) && theCE.lin.nuc.containsKey(name2) &&
							!theCE.lin.nuc.get(name).child.isEmpty() && !theCE.lin.nuc.get(name2).child.isEmpty())
								{
								SortedSet<EvDecimal> s=theCE.contactsf.get(name).get(name2);
								NucLineage.Nuc nuc=theCE.lin.nuc.get(name);
								
								int c1=countTrue(getOverlaps(theCE, name, name2));
								if(c1>0)
									outDuration.append(""+c1*(nuc.getLastFrame().add(EvDecimal.ONE).subtract(nuc.getFirstFrame()).doubleValue())/(double)clength+"\t"+s.first()+"\n");
								
								}
			EvFileUtil.writeFile(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/contactdurNEW2.txt"), outDuration.toString());
			*/
			
			
			//Does children split?
			doesChildrenSplit(theCE, new File("/Volumes/TBU_main03/userdata/cellcontactmap/splitchild.txt"),nucNames);
			doesChildrenSplit(theA, new File("/Volumes/TBU_main03/userdata/cellcontactmap/splitchildAngler.txt"),nucNames);
			 
			
			
			/////// HTML files for all contacts

			File targetdirNeigh=new File("/Volumes/TBU_main03/userdata/cellcontactmap/neigh/");
			File targetdirTree=new File("/Volumes/TBU_main03/userdata/cellcontactmap/tree/");
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
					EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap.class.getResource("main_single.htm")))
					.replace("BODY", mainSingleOut));
			EvFileUtil.writeFile(new File(targetdirTree,"index.htm"),
					EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap.class.getResource("main_tree.htm")))
					.replace("BODY", mainTreeOut));
			EvFileUtil.writeFile(new File(targetdirTree,"style.css"),
					EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap.class.getResource("style.css"))));

			//List datasets
			StringBuffer outDatasets=new StringBuffer();
			for(OneLineage lin:lins)
				outDatasets.append(""+lin.numid+": "+lin.name+" <br/>");

			//Write out HTML, cell by cell. Reference lineage is the first one in the list
			//nucName: everything in the file is about this cell
			String neighTemplate=EvFileUtil.readFile(EvFileUtil.getFileFromURL(CellContactMap.class.getResource("neigh.htm")));
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
							if(lin.lifelen.get(nucName)==0)// || percLifeLen<1)
								percLifeLen=0;

							//Formatting for non-time CCM
							String neighColor;
							String neighString;
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
//									EvDecimal lifeLenFrames=(lin.lin.nuc.get(nucName).lastFrame().subtract(lin.lin.nuc.get(nucName).firstFrame()));

									boolean[] neighOverlaps=getOverlaps(lin, nucName, nucName2);
									/*
									new boolean[clength];

									for(int curp=0;curp<clength;curp++)
										{
										
										EvDecimal m=lin.lin.nuc.get(nucName).firstFrame().add(lifeLenFrames.multiply(curp).divide(clength)); //corresponding frame
										SortedSet<EvDecimal> frames=lin.contactsf.get(nucName).get(nucName2);
										if(frames.contains(m) || !frames.headSet(m).isEmpty() && !frames.tailSet(m).isEmpty())
											neighOverlaps[curp]=true;
										
										
										}
*/
									//Convert frame overlap to image
									timeString=getOverlapBar(neighOverlaps).toString();
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

