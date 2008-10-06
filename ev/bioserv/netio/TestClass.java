package bioserv.netio;


public class TestClass
	{
	public static void main(String[] arg)
		{
		RMImanager rmi=new RMImanager();
		
		rmi.regClass(TestClass.class);
		
		
		}
	
	
	
	
	
	@NetFunc(name = "foo")
	public static int test(int bar)
		{
		
		return 666;
		}
	}
