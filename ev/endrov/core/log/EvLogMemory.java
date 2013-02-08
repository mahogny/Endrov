/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.core.log;



//maybe it should be line oriented rather than size oriented?

/**
 * Log listener: Print to console
 * 
 * @author Johan Henriksson
 */
public class EvLogMemory extends EvLog
	{
	private StringBuffer past=new StringBuffer("");
	
	private static int maxlen=5000;
	
	private void cut()
		{
		int len=past.length();
		if(len>maxlen)
			past=new StringBuffer(past.substring(len-maxlen));
		}
	
	/**
	 * Log debugging information
	 */
	public void listenDebug(String s)
		{
		past.append(s).append("\n");
		cut();
		}


	
	/**
	 * Log an error
	 * @param s Human readable description, may be null
	 * @param e Low-level error, may be null
	 */
	public void listenError(String s, Throwable e)
		{
		if(s!=null)
			past.append(s).append("\n");
		if(e!=null)
			{
			past.append("Exception message: ").append("\n");
			past.append(logPrintString(e));
			}
		cut();
		}

	/**
	 * Log normal/expected message
	 */
	public void listenLog(String s)
		{
		past.append(s).append("\n");
		cut();
		}
	
	public String get()
		{
		return past.toString();
		}
	
	}
