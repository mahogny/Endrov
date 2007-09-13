package evplugin.ev;

import java.util.*;


/**
 * Receiver of message and log events
 * 
 * @author Johan Henriksson
 */
public abstract class Log
	{
	/******************************************************************************************************
	 *                               Interface                                                            *
	 *****************************************************************************************************/

	public abstract void listenDebug(String s);
	public abstract void listenError(String s, Exception e);
	public abstract void listenLog(String s);
	
	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/

	public static HashSet<Log> listeners=new HashSet<Log>();

	/**
	 * Keep track of what has happen in memory in case one wants to look at it aposteriori
	 */
	public static MemoryLog memoryLog=new MemoryLog();
	static
	{
	listeners.add(memoryLog);
	}
	
	/**
	 * Log debugging information
	 */
	public static void printDebug(String s)
		{
		for(Log l:listeners)
			l.listenDebug(s);
		}

	/**
	 * Log an error
	 * @param s Human readable description, may be null
	 * @param e Low-level error, may be null
	 */
	public static void printError(String s, Exception e)
		{
		for(Log l:listeners)
			l.listenError(s,e);
		}

	/**
	 * Log normal/expected message
	 */
	public static void printLog(String s)
		{
		for(Log l:listeners)
			l.listenLog(s);
		}

	}
