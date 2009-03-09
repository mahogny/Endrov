package util2.integrateExpression;


import java.io.File;
import java.util.Arrays;

import util2.ConnectImserv;
import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.imageset.Imageset;
import endrov.imagesetImserv.EvImserv;
import endrov.shell.Shell;
import endrov.util.EvParallel;



public class MakeProfiles
	{

	
	public static void doProfile(EvData data)
		{
		
		String channelName="GFP";

		//Naming of lineages
		//AP20:<name>    (20 slices)
		//AP1:<name>     (over time only, 1 slice)
		//T20-10:<name>  (tissue level, by mapping cube, 20x10x10 with 20 along major axis)
		//C:<name>       (cellular level)
		
		if(data.getObjects(Imageset.class).get(0).getChannel(channelName)==null)
			return;
		if(data.getIdObjectsRecursive(Shell.class).isEmpty())
			return;
		
		
		IntExpAP.doProfile(data,IntExpAP.linFor(1, channelName),"exp",channelName,1);

		int apNumSlice=20;
		IntExpAP.doProfile(data,IntExpAP.linFor(apNumSlice, channelName),"exp", channelName,apNumSlice);

		File fileT=IntExpAP.fileFor(data,1,channelName);
		File fileAP=IntExpAP.fileFor(data,apNumSlice,channelName);
		IntExpAP.printProfile(data, IntExpAP.linFor(1, channelName),"exp",channelName,1, fileT);
		IntExpAP.printProfile(data, IntExpAP.linFor(20, channelName),"exp",channelName,apNumSlice, fileAP);

		System.out.println(fileT);
		System.out.println(fileAP);
		
		data.saveData();
		
		
//		printProfile(data, "AP20:CEH-5",expName,channelName,numSubDiv, new File("/tmp/out.txt"));

		
		
		
		/*

		int numSubDivX=20, numSubDivYZ=10;
		IntExpTissue.doProfile(data, "T"+numSubDivX+"-"+numSubDivYZ+":"+name, name, numSubDivX, numSubDivYZ);
		
		IntExpCell.doProfile(data, name, channelName);
		*/

		//Imageset im=data.getObjects(Imageset.class).iterator().next();
		
		
		
		}
	
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
	//	EvData data=EvData.loadFile(new File("/Volumes/TBU_main01/ost4dgood/TB2141_070621_b.ost/"));
		//doProfile(data);

		EvParallel.map_(Arrays.asList(new File("/Volumes/TBU_main01/ost4dgood").listFiles()), new EvParallel.FuncAB<File, Object>(){
			public Object func(File f)
				{
				if(f.getName().endsWith(".ost"))
					{
					//TODO skip
					EvData data=EvData.loadFile(f);
					if(!data.getObjects(Imageset.class).isEmpty())
						doProfile(data);
					}
				return null;
				}
		
		});
		
		
		//Does not reach here. crash?
		
		System.exit(0);
		}
	
	public static void main2(String[] args)
		{
		try
			{
			Log.listeners.add(new StdoutLog());
			EV.loadPlugins();

			
			
			System.out.println("Connecting");
			String url=ConnectImserv.url;
			String query="not trash and CCM";
			EvImserv.EvImservSession session=EvImserv.getSession(new EvImserv.ImservURL(url));
			String[] imsets=session.conn.imserv.getDataKeys(query);
			//TODO make a getDataKeysWithTrash, exclude by default?
			System.out.println("Loading imsets");
			
			for(String s:imsets)
				{
				System.out.println("loading "+s);
				EvData data=EvData.loadFile(url+s);
				doProfile(data);
				}

			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("done");
		System.exit(0);
		}
	}
