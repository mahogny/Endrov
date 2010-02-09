/**
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.biceps;

import java.util.concurrent.Semaphore;

public class ReturnClass
	{

	public Object returnval;
	public Semaphore sem=new Semaphore(0);
	
	/*
	@SuppressWarnings("unused")
	public void run(Integer o)
		{
//		System.out.println("cb "+o);
		returnval=o;
		sem.release();
		}
	
	*/
	}
