package evgui;

import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * Start EV, automatically collects which jar-files should be linked
 * @author Johan Henriksson
 */
public class StartGUI
	{
	public static boolean printCommand=false;
	public static boolean printJavaLib=false;
	
	
	public static void main(String[] args)
		{
		new StartGUI().run(args);
		}
	
	
	private String javaver=System.getProperty("java.specification.version");
	private String OS=System.getProperty("os.name").toLowerCase();
	private String arch=System.getProperty("os.arch").toLowerCase();
	public int vermajor=Integer.parseInt(javaver.substring(0,javaver.indexOf('.')));
	public int verminor=Integer.parseInt(javaver.substring(javaver.indexOf('.')+1));
	public List<String> jarfiles=new LinkedList<String>();
	private String cpsep=":";
	private String libdir="";
	private String javaexe="java";
	private String memstring="-Xmx700M";
	ProcessBuilder pb=new ProcessBuilder("");
	String jarstring=new File(".").getAbsolutePath();


	public void collectSystemInfo(String path)
		{
		//Detect OS
		if(OS.equals("mac os x"))
			{
//			javaexe="java -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV";
			libdir=path+"libs/mac";
			}
		else if(OS.startsWith("windows"))
			{
			libdir=path+"libs/windows";
			cpsep=";";
			}
		else if(OS.startsWith("linux"))
			{
			if(arch.equals("ppc")) //PowerPC (mac G4 and G5)
				{
				libdir=path+"libs/linuxPPC";
				pb.environment().put("LD_LIBRARY_PATH", "libs/linuxPPC");
				}
			else //Assume some sort of x86
				{
				libdir=path+"libs/linux";
				pb.environment().put("LD_LIBRARY_PATH", "libs/linux");
				}
			}
		else
			{
			JOptionPane.showMessageDialog(null, 
					"Your OS + CPU combination is not supported at this moment. We would be happy if you got in\n" +
					"touch so we can support for your platform. If you want to do it yourself it is easy: Get\n" +
					"libraries for your platform (JAI and JOGL), edit evgui/StartGUI.java and recompile.");
			System.exit(1);
			}

		//Collect jarfiles
		collectJars(jarfiles, path+"libs");
		if(!libdir.equals(""))
			collectJars(jarfiles, libdir);
		for(String s:jarfiles)
			jarstring+=cpsep+s;
		}
	
	
	public void run(String[] args)
		{
		collectSystemInfo("");
		
		System.out.println("This system runs OS:"+OS+" with java:"+javaver+" on arch:"+arch);
		
		boolean hasSpecifiedLibdir=false;
		for(String s:args)
			if(s.startsWith("-Djava.library.path="))
				hasSpecifiedLibdir=true;


		if(vermajor>1 || (vermajor==1 && verminor>=5))
			{
			try
				{

				//Generate command
				LinkedList<String> cmdarg=new LinkedList<String>();
				cmdarg.add(javaexe);
				cmdarg.add("-cp");
				cmdarg.add(jarstring);
				cmdarg.add(memstring);
				if(!libdir.equals("") && !hasSpecifiedLibdir)
					cmdarg.add("-Djava.library.path="+libdir);

				//What to run? additional arguments?
				if(args.length>0)
					for(String s:args)
						cmdarg.add(s);
				else
					cmdarg.add("evgui.MW");

				//Output jar-list for mac starter bundles
				if(args.length>0 && args[args.length-1].equals("-macstarter"))
					{
					StringTokenizer t=new StringTokenizer(jarstring,":");
					File dot=new File(".");
					int dotlen=dot.getAbsolutePath().length()-1;
					String tot="";
					while(t.hasMoreTokens())
						{
						String s=t.nextToken();
						if(!tot.equals(""))
							tot=tot+":";
						tot=tot+"$APP_PACKAGE/../"+s.substring(dotlen);
						}
					System.out.println(tot);
					System.exit(0);
					}

				//Run process
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
								System.err.println(line);
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
		else
			JOptionPane.showMessageDialog(null, "Your version of Java is too old. At least 1.5 required");
		}

	
	
	/**
	 * Get all jars and add them with path to vector. Recurses when it finds a directory
	 * ending with _inc
	 */
	private static void collectJars(List<String> v,String dir)
		{
		File p=new File(dir);
		for(File sub:p.listFiles())
			{
			if(sub.isFile() && (sub.getName().endsWith(".jar") || sub.getName().endsWith(".zip")))
				{
				String toadd=sub.getAbsolutePath();//dir+"/"+sub.getName();
				v.add(toadd);
//				v.add(sub.getAbsolutePath());
				if(printJavaLib)
					System.out.println("Adding java library: "+toadd);
				}
			else if(sub.isDirectory() && sub.getName().endsWith("_inc") && !sub.getName().startsWith("."))
				collectJars(v,sub.getAbsolutePath());
			}
		}


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
