package endrov.unsortedImageFilters;

//import java.lang.reflect.Method;

import endrov.util.DynamicClass;

public class AutoLineage2
	{
	public static void run()
		{
		//Run autolineage through class loader, avoid java restarts
		
		try
			{
			DynamicClass.callStatic("endrov.unsortedImageFilters.AutoLineage", "run");
/*			
			ClassLoader cl=new UnusedSimpleClassLoader();
			
			Class<?> c=cl.loadClass("endrov.unsortedImageFilters.AutoLineage");
			
			Method m=c.getMethod("run");
			
			m.invoke(null);
	*/		
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		
		}
	}
