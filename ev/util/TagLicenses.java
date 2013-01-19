/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;

import java.io.File;
import java.io.IOException;

import endrov.util.io.EvFileUtil;


/**
 * Add the license header to all files
 * @author Johan Henriksson
 *
 */
public class TagLicenses
	{
	private static String header=
		"/"+"***\n"+
		" * Copyright (C) 2010 Johan Henriksson\n"+
		" * This code is under the Endrov / BSD license. See www.endrov.net\n"+
		" * for the full text and how to cite.\n"+
		" */\n";
	
	
	public static void recurse(File root) throws IOException
		{
		for(File f:root.listFiles())
			{
			if(f.getName().endsWith(".java"))
				{
				String content=EvFileUtil.readFile(f);
				
				//Ensure it has not been added
				if(content.indexOf("Copyright")==-1 && content.indexOf("COPYRIGHT")==-1)
					{
					System.out.println("Should do "+f);
					//System.out.println(header+content);
					
					content=header+content;
					EvFileUtil.writeFile(f, content);
					}
				
				
				}
			else if(f.isDirectory())
				recurse(f);
			
			}
		
		
		}
	
	
	public static void main(String[] args)
		{
		File root=new File(".");
			
		try
			{
			recurse(root);
			
			System.out.println(header);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
		}
	}
