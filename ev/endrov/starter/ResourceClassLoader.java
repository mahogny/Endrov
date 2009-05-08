package endrov.starter;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Classloader that can find both JARs, CLASSs (like a URLClassLoader) and also JNI
 * @author Johan Henriksson
 *
 */
public class ResourceClassLoader extends URLClassLoader
	{
	List<String> binfiles;
	
	public ResourceClassLoader(URL[] urls, Collection<String> binfiles, ClassLoader parent)
		{
		super(urls,parent);
		this.binfiles=new LinkedList<String>(binfiles);
		}
	
	protected String findLibrary(String libname)
		{
		//System.out.println("-----------Trying to find library "+libname);

		//Figure out operating system
		String OS=System.getProperty("os.name").toLowerCase();

		//Create OS-specific expected name of library
		if(OS.equals("mac os x"))
			libname="lib"+libname+".jnilib";
		else if(OS.startsWith("windows"))
			libname=libname+".dll";
		else
			libname="lib"+libname+".so";
		
		for(String s:binfiles)
			{
			File f=new File(s,libname);
			if(f.exists())
				return f.getAbsolutePath();
			}
		
		return super.findLibrary(libname);
		}
	
	}
