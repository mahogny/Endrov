/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.core.log;



/**
 * Log listener: Print to console
 * 
 * @author Johan Henriksson
 */
public class EvLogStdout extends EvLog
	{
	/**
	 * Log debugging information
	 */
	public void listenDebug(String s)
		{
		System.out.println(s);
		}

	/**
	 * Log an error
	 * @param s Human readable description, may be null
	 * @param e Low-level error, may be null
	 */
	public void listenError(String s, Throwable e)
		{
		if(s!=null)
			System.out.println(s);
		if(e!=null)
			{
			System.out.println("Exception message: "+e.getMessage());
			e.printStackTrace();
			}
		}

	/**
	 * Log normal/expected message
	 */
	public void listenLog(String s)
		{
		System.out.println(s);
		}
	
	}
