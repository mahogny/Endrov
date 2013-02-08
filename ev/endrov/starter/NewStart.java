/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.starter;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import javax.swing.*;

import endrov.core.EvBuild;


/**
 * Start Endrov
 * 
 * @author Johan Henriksson
 */
public class NewStart
	{
	public static void main(String[] args)
		{
		new NewStart().run(args);
		}
	
	
	//public String mainClass="";
	
	private String javaexe="java";
	private LinkedList<String> platformExt=new LinkedList<String>();
	public LinkedList<String> jarfiles=new LinkedList<String>();
	public LinkedList<String> binfiles=new LinkedList<String>();

	
	public void collectSystemInfo(String path)
		{
		collectSystemInfo(new File(path));
		}
	
	/**
	 * Collection information such as location of JAR-files etc
	 */
	public void collectSystemInfo(File path)
		{
		platformExt.clear();
		
		//Detect architecture
		if(EvSystemUtil.arch.equals("ppc")) //PowerPC (mac G4 and G5)
			platformExt.add("ppc");
		else if(EvSystemUtil.arch.equals("x86_64") || EvSystemUtil.arch.equals("amd64")) 
			platformExt.add("amd64");
		else if(EvSystemUtil.arch.equals("sparc"))
			platformExt.add("sparc");
		else
			platformExt.add("x86");
		
		//Detect OS
		if(EvSystemUtil.OS.equals("mac os x"))
			platformExt.add("mac");
		else if(EvSystemUtil.OS.startsWith("windows"))
			platformExt.add("windows");
		else if(EvSystemUtil.OS.startsWith("linux"))
			platformExt.add("linux");
		else if(EvSystemUtil.OS.startsWith("sunos"))
			platformExt.add("solaris");
		else
			{
			JOptionPane.showMessageDialog(null, 
					"Your OS + CPU combination is not supported at this moment. We would be happy if you got in\n" +
					"touch so we can support for your platform. If you want to do it yourself it is easy: Get\n" +
					"libraries for your platform (JAI and JOGL), edit endrov/starter/StartGUI.java and recompile.");
			System.exit(1);
			}

		
		
		/**
		 * Have to add system extensions and libraries as well when the system class loader is not used
		 */
		String libpath=System.getProperty("java.library.path");
		if(libpath!=null)
			{
			StringTokenizer stok=new StringTokenizer(libpath,File.pathSeparator);
			while(stok.hasMoreTokens())
				{
				String s=stok.nextToken();
				if(!s.equals("."))          //TODO: or path? 
					{
					File root=new File(s);
					if(root.exists())
						for(File f:root.listFiles())
							if(f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) //QTJava is .zip
								jarfiles.add(f.getAbsolutePath());
					}
				}
			}


		//Collect jarfiles
		jarfiles.add(path.getAbsolutePath());
		collectJars(jarfiles, binfiles, new File(path,"libs"), platformExt);

		if(libpath!=null)
			{
			StringTokenizer stok=new StringTokenizer(libpath,File.pathSeparator);
			while(stok.hasMoreTokens())
				{
				String s=stok.nextToken();
				if(!s.equals("."))          //TODO: or path? 
					binfiles.add(s);
				}
			}

		
		}
	
	/**
	 * Produce a :-string out of all jars
	 */
	public String getJarString()
		{
		String jarstring="";//endrovRoot.getAbsolutePath();
		for(String s:jarfiles)
			{
			if(!jarstring.equals(""))
				jarstring+=File.pathSeparatorChar;
			jarstring+=s;
			}
		return jarstring;
		}

	/**
	 * Produce a :-string out of all binary directories
	 */
	public String getBinString()
		{
		String binstring="";
		for(String s:binfiles)
			{
			if(!binstring.equals(""))
				binstring=binstring+File.pathSeparatorChar;
			binstring=binstring+s;
			}
		return binstring;
		}
	
	

	/**
	 * Add jar file to list. Show it if requested
	 */
	private static void addJar(LinkedList<String> v, String toadd)
		{
		v.addFirst(toadd);
		}
	
	/**
	 * Get all jars and add them with path to vector. 
	 * Recurses when it finds a directory ending with _inc.
	 */
	private static void collectJars(LinkedList<String> v,LinkedList<String> binfiles,File p, Collection<String> platformExt)
		{
		if(p.exists())
			for(File sub:p.listFiles())
				{
				if(sub.isFile() && (sub.getName().endsWith(".jar") || sub.getName().endsWith(".zip")))
					addJar(v,sub.getAbsolutePath());
				else if(sub.isFile() && (sub.getName().endsWith(".paths")))
					{
					//File containing list of jars or libraries to include.
					//This is used on systems where jars are present already e.g. Debian
					
					try
						{
						BufferedReader input =  new BufferedReader(new FileReader(sub));
						String line;
						while((line=input.readLine())!=null)
							{
							if(line.startsWith("j:"))
								addJar(v,line.substring(2)); //j:
							else if(line.startsWith("b:"))
								binfiles.add(line.substring(2)); //b:
							}
						input.close();
						}
					catch (Exception e)
						{
						e.printStackTrace();
						}
					}
				else if(sub.isDirectory() && sub.getName().endsWith("_inc") && !sub.getName().startsWith(".") && !sub.getName().equals("unused"))
					collectJars(v,binfiles, sub, platformExt);
				else 
					{
					for(String oneExt:platformExt)
						if(sub.isDirectory() && sub.getName().equals("bin_"+oneExt))
							{
							collectJars(v,binfiles, sub, platformExt);
							String toadd=sub.getAbsolutePath();
							binfiles.add(toadd);
							}
					}
				}
		}
	
	
	
	
	
	/**
	 * Run Endrov given command line.
	 */
	public void run(String[] argsa)
		{
		//boolean printMacStarter=false;
		boolean hasSpecifiedLibdir=false;
		boolean printCommand=false;
		boolean useClassLoader=false;
		boolean printClassPath=false;
		File javaenvFile=EvSystemUtil.getJavaenvReadFileName();
		File basedir=new File(".");
		String mainClass="";
		
//		int numNonflagArg=0;
		List<String> args=new LinkedList<String>();
		for(int argi=0;argi<argsa.length;argi++)
			{
			String curarg=argsa[argi];
			
			if(curarg.equals("--printcommand"))
				printCommand=true;
			else if(args.contains("--version"))
				{
				//Print current version. need to be put in starter jar to work
				System.out.println("Endrov "+EvBuild.version);
				System.exit(0);
				}
			else if(curarg.equals("--cp2"))
				{
				//Additional jars to add to classpath
				jarfiles.add(argsa[argi+1]);
				argi++;
				}
			else if(curarg.equals("--libpath2"))
				{
				binfiles.add(argsa[argi+1]);
				argi++;
				}
			else if(curarg.equals("--basedir"))
				{
				//Override current directory
				basedir=new File(argsa[argi+1]);
				argi++;
				}
			else if(curarg.equals("--main"))
				{
				//Override current directory
				mainClass=argsa[argi+1];
				argi++;
				}
			else if(curarg.equals("--javaenv"))
				{
				//Override current directory
				javaenvFile=new File(argsa[argi+1]);
				argi++;
				}
			else if(curarg.equals("--archinfo"))
				{
				//Show info about the system
				System.out.println("This system runs OS:"+EvSystemUtil.OS+" with java:"+EvSystemUtil.javaver+" on arch:"+EvSystemUtil.arch);
				System.exit(0);
				}
			else if(curarg.equals("--classload"))
				useClassLoader=true;
			else if(curarg.equals("--printcp"))
				printClassPath=true;
			else if(curarg.equals("--help"))
				{
				System.out.println("--printcommand, version, cp2, libpath2, basedir, main, javaenv, archinfo, classload, printcp, help");
				System.exit(0);
				}
			else
				{
	//			if(!curarg.startsWith("--"))
		//			numNonflagArg++;
				args.add(curarg);
				}
			}
		
		collectSystemInfo(basedir);
		
		if(printClassPath)
			{
			String out=".";
			for(String s:jarfiles)
				out=out+":"+s;
			System.out.print(out);
			System.exit(0);
			}
		
		if(EvSystemUtil.javaVerMajor>1 || (EvSystemUtil.javaVerMajor==1 && EvSystemUtil.javaVerMinor>=5))
			{
			if(useClassLoader)
				runWithClassLoader(mainClass, args.toArray(new String[]{}));
			else
				runBootstrap(mainClass, hasSpecifiedLibdir,printCommand, javaenvFile, basedir, args.toArray(new String[]{}));
			}
		else
			JOptionPane.showMessageDialog(null, "Your version of Java is too old. It must be at least 1.5");

		}

	
	
	/**
	 * This is for convenience, those who run the .jar-file straight without a wrapper script. It reinvokes Endrov by running
	 * a command, now with proper VM settings (memory), and tells it to run the classloader
	 */
	private void runBootstrap(String mainClass, boolean hasSpecifiedLibdir, boolean printCommand, File javaenvFile, File basedir, String[] argsa)
		{

		try
			{
			//Generate command
			LinkedList<String> cmdarg=new LinkedList<String>();
			cmdarg.add(javaexe);
			cmdarg.add("-cp");
			//cmdarg.add(".");
			cmdarg.add(basedir.toString());
			
			//Add arguments from environment file
			if(javaenvFile.exists())
				{
				System.out.println("Using environment from "+javaenvFile);
				BufferedReader envReader=new BufferedReader(new FileReader(javaenvFile));
				String line=envReader.readLine();
				if(line!=null)
					{
					StringTokenizer envTokenizer=new StringTokenizer(line," ");
					while(envTokenizer.hasMoreTokens())
						{
						String tok=envTokenizer.nextToken();
						cmdarg.add(tok);
						System.out.println("Java environment flag: "+tok);
						}
					}
				envReader.close();
				}
			System.out.println("Using environment: "+javaenvFile);
			
			
			//What to run? Doesn't matter because we specify the main class
			cmdarg.add("endrov.starter.StartGUI");

			//Run the same main class
			cmdarg.add("--main");
			cmdarg.add(mainClass);
			//Change to classloading mode
			cmdarg.add("--classload");

			//additional arguments?
			for(String s:argsa)
				cmdarg.add(s);

			//Run process
			ProcessBuilder pb=new ProcessBuilder("");
			pb.command(cmdarg);
			if(printCommand)
				{
				String totalCmd="";
				for(String s:pb.command())
					totalCmd+=s+" ";
				System.out.println(totalCmd);
				}
			final Process p=pb.start();

			//Pass on errors
			new Thread()
				{
				public synchronized void run()
					{
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line;
					try
						{
						while ( (line = br.readLine()) != null)
							{
							if(line.startsWith("Could not create the Java Virtual Machine"))
								{
								JOptionPane.showMessageDialog(null, "Trouble creating virtual machine. Try to reduce the ammount of memory allocated");
								//normal output follows									
								//Error occurred during initialization of VM
								//Could not reserve enough space for object heap
								}
							System.err.println(line);
							}
						}
					catch (IOException e)
						{
						e.printStackTrace();
						}
					}
				}.start();

				//Pass on output
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ( (line = br.readLine()) != null)
					System.out.println(line);
				try
					{
					p.waitFor();
					}
				catch (InterruptedException e)
					{
					e.printStackTrace();
					}
				System.out.println("Process exited");

			}
		catch (IOException e)
			{
			JOptionPane.showMessageDialog(null, "Was unable to exec command. Full error:\n"+e.getMessage());
			e.printStackTrace();
			}

		}	
	
	/**
	 * Start Endrov through class loader. This is how the final step should be done.
	 * * it allows better control of where files are loaded from and when, needed for plugin architecture
	 * * single process - killing this process will kill everything, unlike when Endrov runs as a subprocess
	 * * jar-files and shared objects need not be passed on the command line which makes Endrov difficult to start on Mac
	 *   and creates a shitty ps -ax
	 * but:
	 * * memory settings cannot be changed since it is a VM option. This requires a startup script or the bootstrap run  
	 */
	private void runWithClassLoader(String mainClass, String[] argsa)
		{
		try
			{
			LinkedList<URL> urls=new LinkedList<URL>();
			for(String s:jarfiles)
				urls.add(new File(s).toURI().toURL());


			//Important: Must NOT use the system class loader - it will take over for current directory
			//and fail to load JAR files
			
			System.out.println("Bin files: "+binfiles);
			
			ResourceClassLoader cload=new ResourceClassLoader(urls.toArray(new URL[]{}),binfiles, null);

			Class<?> cl=cload.loadClass(mainClass);
			Method mMethod=cl.getMethod("main", String[].class);
			mMethod.invoke(null, new Object[]{argsa});
			
//			cload.close();   only java7
			}
		catch (Exception e)
			{
			e.printStackTrace();
			System.out.println("Failing to load \""+mainClass+"\"");
			}
		}
	
	

	

	
	}
