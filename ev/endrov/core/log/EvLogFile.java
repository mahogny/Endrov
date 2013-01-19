/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.core.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Log listener: Write to file
 * @author Johan Henriksson
 *
 */
public class EvLogFile extends EvLog
	{
	private BufferedWriter out;
	
	public EvLogFile(File filename)
		{
		try 
			{
			filename.getParentFile().mkdirs();
			out = new BufferedWriter(new FileWriter(filename, true));
			for(int i=0;i<5;i++)
				out.write("\n");
			out.write("--------------------------- new session "+new Date()+"---------------------------");
			}
		catch (IOException e)
			{
			System.out.println("Error opening log file "+filename);
			e.printStackTrace();
			}
		}

	@Override
	public void listenDebug(String s)
		{
		try
			{
			out.write(s);
			out.write("\n");
			out.flush();
			}
		catch (IOException e)
			{
			System.out.println("Error writing to log file");
			}
		}

	@Override
	public synchronized void listenError(String s, Exception e)
		{
		try
			{
			if(s!=null)
				{
				out.write(s);
				out.write("\n");
				}
			if(e!=null)
				{
				out.write("Exception message: ");
				out.write("\n");
				out.write(logPrintString(e));
				}
			out.flush();
			}
		catch (IOException e1)
			{
			System.out.println("Error writing to log file");
			}
		}

	@Override
	public synchronized void listenLog(String s)
		{
		try
			{
			out.write(s);
			out.write("\n");
			out.flush();
			}
		catch (IOException e)
			{
			System.out.println("Error writing to log file");
			}
		}

	}
