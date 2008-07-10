package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import endrov.data.EvData;
import endrov.data.EvDataXML;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import endrov.nuc.NucVoronoi;

public class AverageNumNeighTime
	{
	
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		
		EvData ost=new EvDataXML("/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/rmd.ostxml");
//		EvData ost=new EvDataXML("/Volumes/TBU_main02/ostxml/model/stdcelegansNew.ostxml");
		
		NucLineage lin=ost.getObjects(NucLineage.class).iterator().next();
		
		try
			{
			PrintWriter pw=new PrintWriter(new FileWriter("/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/henriksson/avgnumneigh.txt"));
			
			
			for(int curframe=lin.firstFrameOfLineage();curframe<lin.lastFrameOfLineage();curframe++)
				{
				Map<NucPair, NucLineage.NucInterp> inter=lin.getInterpNuc(curframe);
				
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
//				else
//					System.out.println("bad");
					}
				
				*/
				
				try
					{
					NucVoronoi nvor=new NucVoronoi(inter);
					int curNumCell=nvor.nucnames.size();
//					Voronoi voro=new Voronoi(nmid.toArray(new Vector3d[]{}));
	//				VoronoiNeigh vneigh=new VoronoiNeigh(voro);
					
					int cnt=0;
					for(List<Integer> list:nvor.vneigh.dneigh)
						cnt+=list.size();

					double avg=((double)cnt/2.0)/(double)curNumCell;
					
					System.out.println("frame "+curframe+" avg: "+avg+" #c:"+curNumCell+" tot "+cnt/2.0);

//					System.out.println(""+curframe+" "+avg);
					pw.println(""+curframe+" "+avg+" "+cnt/2+" "+curNumCell);
					
					
//				System.out.println(avg+" for "+curNumCell+" cells");
					
					
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
