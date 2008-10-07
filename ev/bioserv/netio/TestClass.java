package bioserv.netio;

import java.io.IOException;
import java.io.Serializable;


@NetClass()
public class TestClass
	{
	public static void main(String[] arg)
		{
		RMImanager rmi=new RMImanager(null);
		
		rmi.regClass(TestClass.class);
		
		
		
			try
				{
				
				
			
				
				
				rmi.send(new Message(new Serializable[]{new Integer(5)},"foo",null));
				
				
				}
			catch (IOException e)
				{
				e.printStackTrace();
				}
		
		}
	
	
	
	
	
	@NetFunc(name = "foo")
	public static int test(int bar)
		{
		
		System.out.println("test was called "+bar);
		
		return 666;
		}
	}
