package endrov.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Dynamic loading of classes
 * @author Johan Henriksson
 *
 */
public class DynamicClass
	{
	private static URL[] urls;
	static
		{
		try
			{
			String path=System.getProperty("java.class.path",".");
			StringTokenizer tok=new StringTokenizer(path,""+File.pathSeparatorChar);
			LinkedList<URL> toks=new LinkedList<URL>();
			while(tok.hasMoreTokens())
				toks.add(new File(tok.nextToken()).toURI().toURL());
			System.out.println(toks);
			urls=toks.toArray(new URL[]{});
			}
		catch (MalformedURLException e)
			{
			//Not allowed to happen
			e.printStackTrace();
			}
		}
	
	
	/**
	 * Create an instance of an object. All calls have to be done using reflection; makes most sense
	 * when using a dynamically typed language
	 */
	public static Object newInstance(String className) 
		{
		try
			{
			URLClassLoader cl=new URLClassLoader(urls);
			Class<?> c=cl.loadClass(className);
			return c.newInstance();
			}
		catch (ClassNotFoundException e)
			{
			e.printStackTrace();
			}
		catch (InstantiationException e)
			{
			e.printStackTrace();
			}
		catch (IllegalAccessException e)
			{
			e.printStackTrace();
			}
		return null;
		}
	
	/**
	 * Load the class and call a static method that returns void
	 */
	public static void callStatic(String className, String method) 
		{
		try
			{
			URLClassLoader cl=new URLClassLoader(urls);
			Class<?> c=cl.loadClass(className);
			Method m=c.getMethod(method);
			m.invoke(null);
			}
		catch (ClassNotFoundException e)
			{
			e.printStackTrace();
			}
		catch (IllegalAccessException e)
			{
			e.printStackTrace();
			}
		catch (IllegalArgumentException e)
			{
			e.printStackTrace();
			}
		catch (InvocationTargetException e)
			{
			e.printStackTrace();
			}
		catch (SecurityException e)
			{
			e.printStackTrace();
			}
		catch (NoSuchMethodException e)
			{
			e.printStackTrace();
			}
		}
	
	
	}
