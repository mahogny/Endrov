package util;

import endrov.data.EvData;
import endrov.ev.*;
import endrov.imageset.Imageset;
import endrov.imagesetImserv.EvImserv;


/**
 * This example shows how to query imserv for recordings and then loop through them. This approach means
 * you no longer have to edit the source code to change which imagesets should be processed, and there is no
 * messing around with filenames
 * 
 * @author Johan Henriksson
 */
public class BatchImServ
	{

	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		EvLog.listeners.add(new StdoutLog());
		EV.loadPlugins();

		try
			{
			//////////////////////
			//user="", host="localhost", ask for password
//			String url="imserv://@host/";
			
			//////////////////////
			//user="", host="localhost", password=""
			String url="imserv://:@host/";

			//////////////////////
			//Everything
			String query="*";
			
			
			//////////////////////
			EvImserv.EvImservSession session=EvImserv.getSession(new EvImserv.ImservURL(url));
			String[] imsets=session.conn.imserv.getDataKeys(query);
			for(String s:imsets)
				{
				System.out.println("trying "+s);
				
//				Imageset im=EvImserv.getImageset(url+s);
				EvData data=EvData.loadFile(url+s);
//			Imageset im=EvImserv.getImageset(url+s); 
				Imageset im=data.getObjects(Imageset.class).iterator().next();
				
				System.out.println("metadata name "+data.getMetadataName());
				System.out.println("dont warn "+im);
				
				}
			
			
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}

		
		
		
		
		
				
		
		}

	}
	
			
	
	