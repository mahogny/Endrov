package util2.cellContactMap;

import java.util.*;

import util2.ConnectImserv;

import endrov.ev.*;
import endrov.imageset.Imageset;
import endrov.imagesetImserv.EvImserv;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;


/**
 * calculate volume statistics
 * @author Johan Henriksson
 */
public class VolStats
	{
	public static boolean showNeigh=false;
	public static boolean saveNormalized=true;
	public static int NUMTRY=0;

	public static NucLineage loadLin() throws Exception
		{
		System.out.println("Connecting");
		String url=ConnectImserv.url;
		/*EvImserv.EvImservSession session=*/EvImserv.getSession(new EvImserv.ImservURL(url));
		System.out.println("Loading imsets");
		
		String s="celegans2008.2";
		
		System.out.println("loading "+s);
		Imageset im=EvImserv.getImageset(url+s); 
		//TODO: should be able to go trough session to avoid url+s
		for(NucLineage lin:im.getObjects(NucLineage.class))
			return lin;
		throw new Exception("did not find");
		}
	
	
	
	/**
	 * Find the first keyframe ever mentioned in a lineage object
	 */
	public static int firstFrameOfLineage(NucLineage lin)
		{
		Integer minframe=null;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			if(minframe==null || nuc.pos.firstKey()<minframe)
				minframe=nuc.pos.firstKey();
			}
		return minframe;
		}
	
	
	public static int lastOkFrame(NucLineage lin)
		{
		int lastFrame=Integer.MAX_VALUE;
		for(String nuc:lin.nuc.keySet())
			{
			if(lin.nuc.get(nuc).child.size()<2)
				{
				int f=lin.nuc.get(nuc).pos.lastKey();
				if(f<lastFrame)
					lastFrame=f;
				}
			}
		return lastFrame;
		}
	
	
	public static void calcVolStat(NucLineage lin) throws Exception
		{
		final int fminframe=firstFrameOfLineage(lin);
		final int fmaxframe=lastOkFrame(lin);
		
		for(int curframe=fminframe;curframe<fmaxframe;curframe++)
			{
			if(curframe%30==0)
				System.out.println("frame "+curframe);

			//Interpolate for this frame
			Map<NucPair, NucLineage.NucInterp> interp=lin.getInterpNuc(curframe);
			//Only keep visible nuclei
			Set<NucPair> visibleNuc=new HashSet<NucPair>();
			for(Map.Entry<NucPair, NucLineage.NucInterp> e:interp.entrySet())
				if(e.getValue().isVisible())
					visibleNuc.add(e.getKey());
			interp.keySet().retainAll(visibleNuc);
			
			//Count #cells for this frame
			int numCellsNow=interp.size();
			
			//Total nuclei volume
			double totNucVol=0;
			for(Map.Entry<NucPair, NucLineage.NucInterp> entry:interp.entrySet())
				{
				double r=entry.getValue().pos.r;
				totNucVol+=4*Math.PI*r*r*r/3;
				}
			
			//Output
			System.out.println(""+curframe+"\t"+numCellsNow+"\t"+totNucVol);
			}
			
		}
	

	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		try
			{
			Log.listeners.add(new StdoutLog());
			EV.loadPlugins();
			
			calcVolStat(loadLin());
			
			System.out.println("Done");
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		System.exit(0);
		}

	}
	
	

	
	