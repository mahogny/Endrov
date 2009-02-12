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
		
		
		IntExpAP.doProfile(data,name,20);
		
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
