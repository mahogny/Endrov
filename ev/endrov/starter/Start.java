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

import endrov.ev.EvBuild;


/**
 * Start Endrov
 * 
 * @author Johan Henriksson
 */
public class Start
	{
	private static boolean printJar=false;	
	
	public static void main(String[] args)
		{
		new Start().run(args);
		}
	
	
	public String mainClass="";
	
	private final String javaver=System.getProperty("java.specification.version");
	private final String arch=System.getProperty("os.arch").toLowerCase();
	private final int javaVerMajor=Integer.parseInt(javaver.substring(0,javaver.indexOf('.')));
	private final int javaVerMinor=Integer.parseInt(javaver.substring(javaver.indexOf('.')+1));
	private String OS=System.getProperty("os.name").toLowerCase();

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
		if(arch.equals("ppc")) //PowerPC (mac G4 and G5)
			platformExt.add("ppc");
		else if(arch.equals("x86_64") || arch.equals("amd64")) 
			platformExt.add("amd64");
		else if(arch.equals("sparc"))
			platformExt.add("sparc");
		else
			platformExt.add("x86");
		
		//Detect OS
		if(OS.equals("mac os x"))
			platformExt.add("mac");
		else if(OS.startsWith("windows"))
			platformExt.add("windows");
		else if(OS.startsWith("linux"))
			platformExt.add("linux");
		else if(OS.startsWith("sunos"))
			platformExt.add("solaris");
		else
			{
			JOptionPane.showMessageDialog(null, 
					"Your OS + CPU combination is not supported at this moment. We would be happy if you got in\n" +
					"touch so we can support for your platform. If you want to do it yourself it is easy: Get\n" +
					"libraries for your platform (JAI and JOGL), edit endrov/starter/StartGUI.java and recompile.");
			System.exit(1);
			}

		
		
		
		System.out.println("before: "+binfiles);
		
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
					binfiles.add(s);
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
		//System.out.println(binfiles);

/*		
		if(binfiles.contains("/usr/lib/jni"))
			{
			binfiles.remove("/usr/lib/jni");
			binfiles.add("/usr/lib/jni");
			}
			*/
		
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
		if(printJar)
			System.out.println("Adding java library: "+toadd);
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
							if(printJar)
								System.out.println("Adding binary directory: "+toadd);
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
		File javaenvFile=null;
		File basedir=new File(".");
		
		
		int numNonflagArg=0;
		List<String> args=new LinkedList<String>();
		for(int argi=0;argi<argsa.length;argi++)
			{
			String curarg=argsa[argi];
			
			if(curarg.equals("--printcommand"))
				printCommand=true;
			else if(curarg.equals("--printjar"))
				printJar=true;
			/*
			 * else if(curarg.equals("--macstarter"))
				{
				//Override detection to spit out mac directories
				OS="mac os x"; 
				printMacStarter=true;
				}*/
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
				System.out.println("This system runs OS:"+OS+" with java:"+javaver+" on arch:"+arch);
				}
			else if(curarg.equals("--classload"))
				useClassLoader=true;
			else if(curarg.equals("--printcp"))
				printClassPath=true;
			else
				{
				if(!curarg.startsWith("--"))
					numNonflagArg++;
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
		
		if(javaVerMajor>1 || (javaVerMajor==1 && javaVerMinor>=5))
			{
			if(useClassLoader)
				runWithClassLoader(args.toArray(new String[]{}));
			else
				runBootstrap(hasSpecifiedLibdir,printCommand, javaenvFile, basedir, args.toArray(new String[]{}));
			}
		else
			JOptionPane.showMessageDialog(null, "Your version of Java is too old. It must be at least 1.5");

		}

	
	
	/**
	 * This is for convenience, those who run the .jar-file straight without a wrapper script. It reinvokes Endrov by running
	 * a command, now with proper VM settings (memory), and tells it to run the classloader
	 */
	private void runBootstrap(boolean hasSpecifiedLibdir, boolean printCommand, File javaenvFile, File basedir, String[] argsa)
		{

		try
			{
			//Generate command
			LinkedList<String> cmdarg=new LinkedList<String>();
			cmdarg.add(javaexe);
			cmdarg.add("-cp");
			//cmdarg.add(".");
			cmdarg.add(basedir.toString());

			//Find java env file
			if(javaenvFile==null)
				{
				for(String s:platformExt)
					{
					javaenvFile=new File(new File("config"),"javaenv."+s+".txt");
					if(javaenvFile.exists())
						break;
					}
				}
			
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
	private void runWithClassLoader(String[] argsa)
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
			}
		catch (Exception e)
			{
			e.printStackTrace();
			System.out.println("Failing to load \""+mainClass+"\"");
			}
		}
	
	
	
	/*
	 * ******************************************************************************************
	 */
	
	/**
	 * Run Endrov given command line. Run through class loader
	 * 
	 * * -- cannot change memory allocation here. CRITICAL
	 * * ++ can change classloader, prepare for better plugin support
	 * * ++ can hide many entries on command line
	 * * ?? can change shared objects dir
	 * 
	 * 
	 */
	/*
	public void runClassLoader(String[] argsa)
		{
		boolean hasSpecifiedLibdir=false;
		boolean printCommand=false;
		File javaenvFile=null;
		File basedir=new File(".");
		
		int numNonflagArg=0;
		List<String> args=new LinkedList<String>();
		for(int argi=0;argi<argsa.length;argi++)
			{
			String curarg=argsa[argi];
			
			if(curarg.equals("--printcommand"))
				printCommand=true;
			else if(curarg.equals("--printjar"))
				printJar=true;
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
				//cp2+=":"+;
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
				//Start another main class
				mainClass=argsa[argi+1];
				argi++;
				}
			else if(curarg.equals("--javaenv"))
				{
				//Use another environment
				javaenvFile=new File(argsa[argi+1]);
				argi++;
				}
			else if(curarg.equals("--archinfo"))
				{
				//Show info about the system
				System.out.println("This system runs OS:"+OS+" with java:"+javaver+" on arch:"+arch);
				}
			else
				{
				if(!curarg.startsWith("--"))
					numNonflagArg++;
				args.add(curarg);
				}
			}
		
		collectSystemInfo(basedir);
		
		

		//Continue if java 1.5+
		if(javaVerMajor>1 || (javaVerMajor==1 && javaVerMinor>=5))
			{
			try
				{
				LinkedList<URL> urls=new LinkedList<URL>();
				for(String s:jarfiles)
					urls.add(new File(s).toURI().toURL());


				//Important: Must NOT use the system class loader - it will take over for current directory
				//and fail to load JAR files
//				URLClassLoader cload=new URLClassLoader(urls.toArray(new URL[]{}),null);
				//URLClassLoader cload=new URLClassLoader(urls.toArray(new URL[]{}),new ResourceClassLoader());
				System.out.println(binfiles);
				ResourceClassLoader cload=new ResourceClassLoader(urls.toArray(new URL[]{}),binfiles, null, Start.class.getClassLoader());
				System.out.println(cload);
				
				Class<?> cl=cload.loadClass(mainClass);
				Method mMethod=cl.getMethod("main", String[].class);
				mMethod.invoke(null, new Object[]{args.toArray(new String[]{})});
				}
			catch (Exception e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			
			}
		else
			JOptionPane.showMessageDialog(null, "Your version of Java is too old. It must be at least 1.5");
		}
	*/
	

	

	
	}

/*
AIX
Digital Unix
FreeBSD
HP UX
Irix
Linux
Mac OS
MPE/iX
Netware 4.11
OS/2
Solaris
Windows 2000
Windows 95
Windows 98
Windows NT
Windows XP*/

//### Detect OS
//UNAME=$(uname)
//if [[ "$UNAME" = Darwin ]]; then
