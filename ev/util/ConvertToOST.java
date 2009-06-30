package util;

import java.io.File;
import java.io.IOException;

import endrov.data.EvData;
import endrov.ev.*;

/**
 * Convert from any other format to OST
 * @author Johan Henriksson
 *
 */
public class ConvertToOST
	{
	public static void main(String[] args)
		{
		try
			{
			if(args.length==0)
				{
				System.out.println("Give paths to all files to convert as parameters");
				System.exit(1);
				}

			//Init
			EvLog.listeners.add(new StdoutLog());
			EV.loadPlugins();
			
			//For every input file
			for(String fileName:args)
				{
				File file=new File(fileName);
				if(!file.exists())
					System.out.println("Does not exist, skipping: "+file);
				else
					{
					System.out.println("Converting: "+file);
					EvData data=EvData.loadFile(file);
					File newFile=new File(file.getParent(),file.getName()+".ost");
					data.saveDataAs(newFile);
					}
				}
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}

	}
