/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

/**
 * String FIFOs (buffer)
 */
public class StringFIFO
	{
	private String fifoIn="";

	/**
	 * Send virtual incoming data
	 */
	public synchronized void addFifoIn(String s)
		{
		fifoIn=fifoIn+s;
		notify();
		}
	
	public synchronized String readUntilTerminal(String term)
		{
		for(;;)
			{
			try
				{
				int i=fifoIn.indexOf(term);
				if(i!=-1)
					{
					String take=fifoIn.substring(0, i+term.length());
					fifoIn=fifoIn.substring(i+term.length());
					return take;
					}
				wait();
				}
			catch (InterruptedException e)
				{
				}
			}
		}
	
	public synchronized String nonblockingRead()
		{
		String s=fifoIn;
		fifoIn="";
		return s;
		}
	}
