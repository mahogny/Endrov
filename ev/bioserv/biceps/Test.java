/**
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.biceps;

import java.io.*;

import bioserv.biceps.Listener.IncomingManager;


@NetClass()
public class Test
	{
	
	
	public static void main(String[] arg) 
		{
		int port=4444;
		
		
		IncomingManager mgr=new IncomingManager()
			{
			public void newConnection(RMImanager rmi)
				{
				System.out.println("new inc conn "+rmi);
				
				rmi.regClass(Test.class,null);

				try
					{
					Integer r=(Integer)rmi.call(new Serializable[]{5}, "foo");
					System.out.println("returned "+r+" ===");
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
				
				
				/*
				try
					{
					Thread.sleep(500);
					
					rmi.send(Message.withCallback(new Serializable[]{1},"hej",new Callback(){
					public void run(Object o)
						{
						System.out.println("cb3 "+o);
						}
				}));
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
*/
				
				
				
				
				/*
				try
					{
					rmi.send(new Message(new Serializable[]{new Integer(5)},"foo",null));
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
				*/
				}
			};
		
		try
			{
			Listener sconn=new Listener(port, mgr);
			sconn.start();
			
			RMImanager rmic=RMImanager.connect("localhost",port);
			
			rmic.regClass(Test.class,null);
			
			/*
			System.out.println("===sending==");
			
			rmic.send(Message.withCallback(new Serializable[]{1},"foo",new Object(){
				@SuppressWarnings("unused")
				public void run(Integer o)
					{
					System.out.println("cb "+o);
					}
			}));
			*/
/*
			rmic.send(Message.withCallback(new Serializable[]{2},"foo",new Callback(){
			public void run(Object o)
				{
				System.out.println("cb2 "+o);
				}
		}));*/

			
			
			//initital commands
			//"login" user -> challenge
			//"challenge" svar -> bool
			//   send h(h(passwd) || challenge) 
			//  1. can issue system info calls here
			//  2. register other commands
			
			Thread.sleep(2000);
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}

		
		System.out.println("===done===");
		System.exit(0);
		}
	
	
	
	
	
	
	
	@NetFunc(name = "foo")
//	public static void foo(int bar)
	public static int foo(int bar)
		{
		System.out.println("foo_called "+bar);
		return 666;
		}
	
	
	@NetFunc(name = "hej")
	public static int hej(int bar)
		{
		System.out.println("bar_called "+bar);
		return 777;
		}
	}
