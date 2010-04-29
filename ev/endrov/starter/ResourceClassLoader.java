/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.starter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
//import java.util.StringTokenizer;

/**
 * Classloader that can find both JARs, CLASSs (like a URLClassLoader) and also JNI
 * @author Johan Henriksson
 *
 */
public class ResourceClassLoader extends URLClassLoader
	{
	private List<String> binfiles;

	/*
	private SpecialClassLoader parentResources;
	
	private static class SpecialClassLoader extends URLClassLoader
		{
		public SpecialClassLoader(ClassLoader parent)
			{
			super(new URL[]{},parent);
			}
		
		public String breakingFindLibrary(String libname)
			{
			return findLibrary(libname);
			}
		}
	*/
	
	public ResourceClassLoader(URL[] urls, Collection<String> binfiles, ClassLoader parent)
		{
		super(urls,parent);
		this.binfiles=new LinkedList<String>(binfiles);
		//parentResources=new SpecialClassLoader(libParent);
		}
	
	
	
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
		{
		Class<?> cl=super.findClass(name);
		//System.out.println("Find class: "+name+" got "+cl);
		return cl;
		}


	


	@Override
	protected synchronized Class<?> loadClass(String arg0, boolean arg1)
			throws ClassNotFoundException
		{
		//System.out.println("Load class "+arg0);
		return super.loadClass(arg0, arg1);
		}




	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException
		{
		//System.out.println("Load class "+name);
		return super.loadClass(name);
		}




	protected String findLibrary(String libname)
		{
		//Figure out operating system
		String OS=System.getProperty("os.name").toLowerCase();

		//Create OS-specific expected name of library
		if(OS.equals("mac os x"))
			libname="lib"+libname+".jnilib";
		else if(OS.startsWith("windows"))
			libname=libname+".dll";
		else
			libname="lib"+libname+".so";

		//System.out.println("findlibrary "+libname);
		
		for(String s:binfiles)
			{
			File f=new File(s,libname);
			if(f.exists())
				return f.getAbsolutePath();
			}

		String dellib=super.findLibrary(libname);
		//System.out.println("Delegating find library for "+libname+" , found "+dellib);
		return dellib;
		}
	
	
	public static Collection<String> getBinDirs(ClassLoader cl)
		{
		try
			{
			//There is some magic that disallows this
			if(cl instanceof ResourceClassLoader)
			//if(cl.getClass().getCanonicalName().equals("endrov.starter.ResourceClassLoader"))
				return new LinkedList<String>(((ResourceClassLoader)cl).binfiles);
			else
				{
				System.out.println(cl);
				return getBinDirs(cl.getParent());
				}
			}
		catch (Exception e)
			{
			//e.printStackTrace();
			return new LinkedList<String>();
			}
		}

	@Override
	public URL findResource(String name)
		{
		//System.out.println("find resource "+name);
		return super.findResource(name);
		}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException
		{
		//System.out.println("find resources "+name);
		return super.findResources(name);
		}


	
	
	
	}
