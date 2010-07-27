/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperStdCelegans.makeCellContactMap2;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import util2.ConnectImserv;

import endrov.data.EvData;
import endrov.ev.*;
import endrov.imageset.Imageset;
import endrov.imagesetImserv.EvImserv;
import endrov.neighmap.NeighMap;
import endrov.nuc.NucLineage;
import endrov.util.*;

//stdcelegans vs celegans2008.2?


/***
 * Do this afterwards
 * 
 * 
 * 
 * if(name.equals("celegans2008.2"))
				{
				try
					{
					PrintWriter cp=new PrintWriter(new FileWriter(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/contactdur.txt")));
					for(String n1:contactsf.keySet())
						if(lin.nuc.containsKey(n1))
							for(String n2:contactsf.get(n1).keySet())
								if(lin.nuc.containsKey(n2) && n2.compareTo(n1)>0 && 
										lin.nuc.get(n1).child.size()>1 && lin.nuc.get(n2).child.size()>1)
									{
									SortedSet<EvDecimal> s=contactsf.get(n1).get(n2);
									if(!s.isEmpty())
										{
										cp.println(""+(s.last().subtract(s.first()).add(1))+"\t"+s.first());
										}
									}
					cp.close();
					System.exit(0);
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
				}
 * 
 */


/**
 * separate this
 * 
 * 			PrintWriter pw=null;
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

 */


/**
 * 
 * unused
 * 
 * 	private static void writeLineageNeighDistances(OneLineage lin) throws IOException
		{
		PrintWriter pw=new PrintWriter(new FileWriter(new File("/Volumes/TBU_main03/userdata/cellcontactmap/dist.csv")));
		for(Map.Entry<EvDecimal, NucVoronoi> entry:lin.fcontacts.entrySet())
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
 * 
 */



/**
 * Calculate cell contact map.
 * Output number of contacts for each frame.
 * 
 * about 40min on xeon
 * 
 * @author Johan Henriksson, Jurgen Hench
 *
 */
public class UtilMakeCellContactMap
	{
	public static void main(String[] args)
		{
		try
			{
			EvLog.listeners.add(new EvLogStdout());
			EV.loadPlugins();

			NumberFormat percentFormat=NumberFormat.getInstance();
			percentFormat.setMinimumFractionDigits(1);
			percentFormat.setMaximumFractionDigits(1);

		
			

			//bottle neck: building imageset when not needed. fix in Endrov3/OST4
			
			///////////
			System.out.println("Connecting");
			String url=ConnectImserv.url;
			String query="not trash and CCM";
			EvImserv.EvImservSession session=EvImserv.getSession(new EvImserv.ImservURL(url));
			String[] imsets=session.conn.imserv.getDataKeys(query);
			//TODO make a getDataKeysWithTrash, exclude by default?
			System.out.println("Loading imsets");
			
			TreeMap<String, NucLineage> lins=new TreeMap<String, NucLineage>();
			
			for(String s:imsets)
				{
				System.out.println("loading "+s);
				EvData data=EvData.loadFile(url+s);
				Imageset im=data.getObjects(Imageset.class).iterator().next();
				
				lins.put(data.getMetadataName(), im.getObjects(NucLineage.class).iterator().next());
				}
			NucLineage reflin=EvImserv.getImageset(url+"celegans2008.2").getObjects(NucLineage.class).iterator().next();
			//////////

			final TreeSet<String> nucNames=new TreeSet<String>(reflin.nuc.keySet());

			//Calc neigh
			Map<NucLineage,NeighMap> nmaps=EvParallel.map(lins, new EvParallel.FuncAB<Tuple<String,NucLineage>, Tuple<NucLineage,NeighMap>>(){
			public Tuple<NucLineage,NeighMap> func(Tuple<String,NucLineage> in)
				{
				NeighMap nm=MakeCellContactMap.calculateCellMap(in.snd(), nucNames, null, null, new EvDecimal(60));
				return new Tuple<NucLineage, NeighMap>(in.snd(), nm);
				}
			});

			//Save neighmaps
			EvData dataOut=new EvData();
			for(Map.Entry<String, NucLineage> e:lins.entrySet())
				dataOut.metaObject.put(e.getKey(), nmaps.get(e.getValue()));
			dataOut.saveDataAs(new File("/Volumes/TBU_main03/userdata/newcellcontactsmap.ost"));
			
			

			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("done");
		System.exit(0);
		}
	

	}

