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
				
				rmi.regClass(Test.class);
				
				
				try
					{
					rmi.send(new Message(new Serializable[]{new Integer(5)},"foo",null));
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
				
				/*
				PrintWriter pw=new PrintWriter(conn.getOutputStream());
				pw.println("foo");
				pw.flush();
				*/
				
				}
			};
		
		try
			{
			Listener sconn=new Listener(port, mgr);
			sconn.start();
			
			RMImanager rmic=new RMImanager("localhost",port);
			
			
			
			
			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}

		
		System.exit(0);
		}
	
	
	/*
	public static void main(String[] arg)
		{
		
		
		
			try
				{
				
				RMImanager rmi=new RMImanager(null);
				
				rmi.regClass(TestClass.class);
				
			
				
				
				rmi.send(new Message(new Serializable[]{new Integer(5)},"foo",null));
				
				
				}
			catch (IOException e)
				{
				e.printStackTrace();
				}
		
		}*/
	
	
	
	
	
	@NetFunc(name = "foo")
	public static int test(int bar)
		{
		
		System.out.println("test was called "+bar);
		
		return 666;
		}
	}
