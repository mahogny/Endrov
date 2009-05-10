package endrov.unsortedImageFilters.trash;





import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


public class OverlayClassLoaderOLD extends URLClassLoader 
	{
	private Map<String,Class<?>> classes = new HashMap<String, Class<?>>();

	private PublicClassLoader parentLoader;
	//private URLClassLoader urlLoader;
	
	private static class PublicClassLoader extends ClassLoader
		{
		public PublicClassLoader(ClassLoader l)
			{
			super(l);
			}
		public Class<?> findLoadedClass2(String name)
			{
			return findLoadedClass(name);
			}
		}
	
	public OverlayClassLoaderOLD(URL[] urls,ClassLoader parent)
		{
		//Big point here: URLClassLoader has no parent, but this classloader has
		super(urls,null);
		parentLoader=new PublicClassLoader(parent);
		//urlLoader=new URLClassLoader(urls,null)
		}
	
	public Class<?> loadClass(String className) throws ClassNotFoundException 
		{
		System.out.println("loadClass1");
		return loadClass(className, false);
		}
	public Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException 
		{
		//Check if already loaded
		System.out.println("loadClass url "+className);
		Class<?> d=findLoadedClass(className);
		if(d!=null)
			return d;
		else
			{
			
			//Check if parent class loader has it
			System.out.println("loadClass local "+className);
			Class<?> c=parentLoader.findLoadedClass2(className);
			System.out.println("failed, trying locally");
			//Load anew if it doesn't
			if(c==null)
				c=parentLoader.loadClass(className);
			if(resolve)
				resolveClass(c);
			return c;
			}
		}
		
	public Class<?> findClass(String className) throws ClassNotFoundException
	{
	System.out.println("findClass "+className);
	return super.findClass(className);
	}
	/*
	public Class<?> findClass(String className)
		{
		
		
		byte classByte[];
		Class<?> result=classes.get(className);
		if(result != null)
			{
			return result;
			}
		
		try
			{
			System.out.println("sysclass?");
			return findSystemClass(className);
			}
		catch(Exception e)
			{
			}
		
		try
			{
			System.out.println("Load anew");
			String classPath = ClassLoader.getSystemResource(className.replace('.',File.separatorChar)+".class").getFile().substring(1);
			System.out.println("to load: "+classPath);
			classByte = loadClassData(classPath);
			result = defineClass(className,classByte,0,classByte.length,null);
			classes.put(className,result);
			return result;
			}
		catch(Exception e)
			{
			return null;
			} 
		}
	
	*/
	
	/*
	public static void main(String[] args)
		{
		try
			{
			ClassLoader cl=new OverlayClassLoader(ClassLoader.getSystemClassLoader());
			
			Class<?> c=cl.loadClass("endrov.unsortedImageFilters.AutoLineage");
			
			Method m=c.getMethod("run");
			
			m.invoke(null);
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		}
		*/
	}
	
	
	
	
