package endrov.unsortedImageFilters.trash;





import java.io.*;
import java.lang.reflect.Method;
import java.util.*;


public class UnusedSimpleClassLoader extends ClassLoader 
	{
	private Map<String,Class<?>> classes = new HashMap<String, Class<?>>();

	
	public UnusedSimpleClassLoader()
		{
		super(ClassLoader.getSystemClassLoader());
		}
	
	public Class<?> loadClass(String className) throws ClassNotFoundException 
		{
		System.out.println("want "+className);
		return findClass(className);
		}
	
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
	
	private byte[] loadClassData(String className) throws IOException
		{
		File f = new File(className);
		int size = (int)f.length();
		byte buff[] = new byte[size];
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);
		dis.readFully(buff);
		dis.close();
		return buff;
		}
	
	
	
	public static void main(String[] args)
		{
		try
			{
			ClassLoader cl=new UnusedSimpleClassLoader();
			
			Class<?> c=cl.loadClass("endrov.unsortedImageFilters.AutoLineage");
			
			Method m=c.getMethod("run");
			
			m.invoke(null);
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		}
	}
	
	
	
	
