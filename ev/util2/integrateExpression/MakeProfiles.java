package util2.integrateExpression;


import util2.ConnectImserv;
import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.imagesetImserv.EvImserv;



public class MakeProfiles
	{

	
	public static void doProfile(EvData data, String name)
		{
		
		String channelName="GFP";

		//Naming of lineages
		//AP20:<name>    (20 slices)
		//AP1:<name>     (over time only, 1 slice)
		//T20-10:<name>  (tissue level, by mapping cube, 20x10x10 with 20 along major axis)
		//C:<name>       (cellular level)
		
		int apNumSlice=20;
		IntExpAP.doProfile(data,"AP"+apNumSlice+":"+name,name, channelName,apNumSlice);
		
		IntExpAP.doProfile(data,"AP1:"+name,name,channelName,1);

		int numSubDivX=20, numSubDivYZ=10;
		IntExpTissue.doProfile(data, "T"+numSubDivX+"-"+numSubDivYZ+":"+name, name, numSubDivX, numSubDivYZ);
		
		IntExpCell.doProfile(data, name, channelName);
		//data.saveData(); NOOO

		//Imageset im=data.getObjects(Imageset.class).iterator().next();
		
		
		
		}
	
	
	public static void main(String[] args)
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
				doProfile(data, s);
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
