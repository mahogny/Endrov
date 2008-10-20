package util2.misc;

import java.io.*;
import java.util.*;

import endrov.data.EvData;
import endrov.data.EvDataXML;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.nuc.*;


//Only consider those within bounds TODO

/*
Vector<String> nucnames=new Vector<String>();
Vector<Vector3d> nmid=new Vector<Vector3d>();
for(Map.Entry<NucPair, NucLineage.NucInterp> entry:inter.entrySet())
	{
	//NucLineage.Nuc nuc=lin.nuc.get(entry.getKey().snd());
	if(entry.getValue().frameBefore!=null) //visibility rule taken from modw
	//if(nuc.firstFrame()<=curframe && nuc.lastFrame()>=curframe)
		{
		nucnames.add(entry.getKey().snd());
		nmid.add(entry.getValue().pos.getPosCopy());
		}
//else
//	System.out.println("bad");
	}

*/
//EvData ost=new EvDataXML("/Volumes/TBU_main02/ostxml/model/stdcelegansNew.ostxml");
//Voronoi voro=new Voronoi(nmid.toArray(new Vector3d[]{}));
//				VoronoiNeigh vneigh=new VoronoiNeigh(voro);

//double avg=((double)cnt/2.0)/(double)curNumCell;
//System.out.println(""+curframe+" "+avg);
//System.out.println(avg+" for "+curNumCell+" cells");


public class AverageNumNeighTime
	{
	
	
	
	
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		//Load lineage
		EvData ost=new EvDataXML("/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/rmd.ostxml");
		NucLineage lin=ost.getObjects(NucLineage.class).iterator().next();
		
		try
			{
			PrintWriter pw=new PrintWriter(new FileWriter("/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/data/henriksson/avgnumneigh.txt"));
			
			//Go through all frames
			for(int curframe=lin.firstFrameOfLineage();curframe<lin.lastFrameOfLineage();curframe++)
				{
				//interpolate
				Map<NucPair, NucLineage.NucInterp> inter=lin.getInterpNuc(curframe);
				
				try
					{
					//Get neighbours
					NucVoronoi nvor=new NucVoronoi(inter,false);
					int curNumCell=nvor.nucnames.size();
					
					//Count number of contacts
					int cnt=0;
					for(Collection<Integer> list:nvor.vneigh.dneigh)
						cnt+=list.size();

					double avg=((double)cnt)/(double)curNumCell;
					
					System.out.println("frame "+curframe+" avg: "+avg+" #c:"+curNumCell+" tot "+cnt);

					pw.println(""+curframe+" "+avg+" "+cnt+" "+curNumCell);
					
					
					
					
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				
				
				}
			
			
			pw.close();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
	
		System.out.println("done");
		System.exit(0);

		}

	}
