package bioserv.netio;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * RMI management. Registration of methods etc.
 * @author Johan Henriksson
 *
 */
public class RMImanager
	{

	
	private HashMap<String, RegMethod> regfunc=new HashMap<String, RegMethod>();
	
	
	
	/**
	 * One registered method
	 */
	public class RegMethod
		{
		private Method m;
		private Class<?>[] c;
		
		public RegMethod(Method m)
			{
			this.m=m;
			c=m.getParameterTypes();
			}
		
		public void recv(Message msg)
			{
			try
				{
				m.invoke(null, msg.unpack(c));
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		
		
		
		}
	

	/**
	 * Register a method
	 */
	public void regFunc(String netname, Method m)
		{
		RegMethod a=new RegMethod(m);
		System.out.println("reg: "+netname);
		regfunc.put(netname, a);
		}
	
	/**
	 * Register all methods in a class, based on if they are annotated
	 */
	public void regClass(Class<?> c)
		{
		for(Method m:c.getDeclaredMethods())
			{
			NetFunc a=m.getAnnotation(NetFunc.class);
			for(Annotation aa:m.getAnnotations())
				System.out.println(aa);
					
			System.out.println("hm "+m+" "+a);
			if(a!=null)
				regFunc(a.name(), m);
			}
		}
	
	
	public void send(Message msg)
		{
		RegMethod m=regfunc.get(msg.getCommand());
		
		if(m!=null)
			m.recv(msg);
		}
	
	}
