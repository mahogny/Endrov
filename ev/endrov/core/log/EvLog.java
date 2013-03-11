/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.core.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;



/**
 * Receiver of message and log events
 * 
 * @author Johan Henriksson
 */
public abstract class EvLog
	{
	/******************************************************************************************************
	 *                               Interface                                                            *
	 *****************************************************************************************************/

	public abstract void listenDebug(String s);
	public abstract void listenError(String s, Throwable e);
	public abstract void listenLog(String s);
	
	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/

	private static HashSet<EvLog> listeners=new HashSet<EvLog>();

	/**
	 * Add another listener, if not already added
	 */
	public static void addListener(EvLog log)
		{
		synchronized (listeners)
			{
			//An entirely new set is created - this means that currently printing logs than just move on as usual, iterating on the old copy
			HashSet<EvLog> newListeners=new HashSet<EvLog>(listeners);
			if(!newListeners.contains(log))
				newListeners.add(log);
			listeners=newListeners;
			}
		}
	
	/**
	 * Remove a listener
	 */
	public static void removeListener(EvLog log)
		{
		synchronized (listeners)
			{
			//An entirely new set is created - this means that currently printing logs than just move on as usual, iterating on the old copy
			HashSet<EvLog> newListeners=new HashSet<EvLog>(listeners);
				newListeners.remove(log);
			listeners=newListeners;
			}
		}

	
	/**
	 * Keep track of what has happen in memory in case one wants to look at it aposteriori
	 */
	public static EvLogMemory memoryLog=new EvLogMemory();
	static
	{
	addListener(memoryLog);
	}
	
	/**
	 * Log debugging information
	 */
	public static void printDebug(String s)
		{
		for(EvLog l:getImmutableListeners())
			l.listenDebug(s);
		}

	/**
	 * Log an error
	 * @param s Human readable description, may be null
	 * @param e Low-level error, may be null
	 */
	public static void printError(String s, Throwable e)
		{
		for(EvLog l:getImmutableListeners())
			l.listenError(s,e);
		}

	public static void printError(Throwable e)
		{
		printError(null,e);
		}
	
	
	/**
	 * Log normal/expected message
	 */
	public static void printLog(String s)
		{
		for(EvLog l:getImmutableListeners())
			l.listenLog(s);
		}
	
	/**
	 * Get an immutable list of listeners - can be iterated over without race conditions
	 */
	private static Collection<EvLog> getImmutableListeners()
		{
		synchronized (listeners)
			{
			return listeners;
			}
		}

	
	/**
	 * Take an exception and print the error to a string
	 */
	public static String logPrintString(Throwable e)
		{
		StringWriter sw=new StringWriter();
		PrintWriter s2=new PrintWriter(sw);
		e.printStackTrace(s2);
		s2.flush();
		return sw.toString();
		}
	
	}
