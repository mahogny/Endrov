/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2;

import java.io.File;
import java.io.IOException;

import endrov.util.EvFileUtil;

public class ConvHTML
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		File root=new File("/Volumes3/TBU_main03/userdata/cellcontactmap/tree/");
		
		try
			{
			for(File f:root.listFiles())
				{
				if(f.getName().endsWith(".htm"))
					{
					System.out.println(f);
					String con=EvFileUtil.readFile(f);
					con=con.replace("<html>", "<html><link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />");
					EvFileUtil.writeFile(f, con);
					}
				
				
				}
			}
		catch (IOException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		
		}

	}
