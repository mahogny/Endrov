package endrov.starter;

import java.io.*;
import java.util.*;
import javax.swing.*;

//TODO display error if there is one

/**
 * Start EV, automatically collects which jar-files should be linked
 * @author Johan Henriksson
 */
public class Start
	{
	public static boolean printCommand=false;
	public static boolean printJavaLib=false;	
	
	public static void main(String[] args)
		{
		new Start().run(args);
		}
	
	
	private String javaver=System.getProperty("java.specification.version");
	private String OS=System.getProperty("os.name").toLowerCase();
	private String arch=System.getProperty("os.arch").toLowerCase();
	public int vermajor=Integer.parseInt(javaver.substring(0,javaver.indexOf('.')));
	public int verminor=Integer.parseInt(javaver.substring(javaver.indexOf('.')+1));
	public List<String> jarfiles=new LinkedList<String>();
	public List<String> binfiles=new LinkedList<String>();
	private String cpsep=":";
	private String libdir="";
	private String javaexe="java";
//	private String memstring="-Xmx2000M";
	ProcessBuilder pb=new ProcessBuilder("");
	String jarstring=new File(".").getAbsolutePath();
	String binstring="";


	public void collectSystemInfo(String path)
		{
		//Detect OS
		String osExt="";
		cpsep=":";
		if(OS.equals("mac os x"))
			{
//			javaexe="java -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV";
			libdir=path+"libs/mac";
			osExt="mac";
			}
		else if(OS.startsWith("windows"))
			{
//			libdir=path+"libs/windows";
			cpsep=";";
			osExt="win";
			}
		else if(OS.startsWith("linux"))
			{
			if(arch.equals("ppc")) //PowerPC (mac G4 and G5)
				{
//				libdir=path+"libs/linuxPPC";
				pb.environment().put("LD_LIBRARY_PATH", "libs/linuxPPC");
				osExt="linuxPPC";
				}
			else //Assume some sort of x86
				{
//				libdir=path+"libs/linux";
				pb.environment().put("LD_LIBRARY_PATH", "libs/linux");
				osExt="linux";
				}
			}
		else if(OS.startsWith("solaris"))
			{
//			libdir=path+"libs/solaris";
			pb.environment().put("LD_LIBRARY_PATH", "libs/solaris");
			osExt="solaris";
			}
		else
			{
			JOptionPane.showMessageDialog(null, 
					"Your OS + CPU combination is not supported at this moment. We would be happy if you got in\n" +
					"touch so we can support for your platform. If you want to do it yourself it is easy: Get\n" +
					"libraries for your platform (JAI and JOGL), edit endrov/starter/StartGUI.java and recompile.");
			System.exit(1);
			}

		//Collect jarfiles
		collectJars(jarfiles, binfiles, path+"libs", osExt);
//		if(!libdir.equals(""))
//			collectJars(jarfiles, binfiles, libdir, osExt);
		for(String s:jarfiles)
			jarstring+=cpsep+s;
		for(String s:binfiles)
			{
			if(!binstring.equals(""))
				binstring=binstring+cpsep;
			binstring=binstring+s;
			}
		libdir=binstring;
		}
	
	
	public void run(String[] argsa)
		{
		List<String> args=new LinkedList<String>();
		for(String s:argsa)
			args.add(s);
		
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
//				cmdarg.add(memstring);
				if(!libdir.equals("") && !hasSpecifiedLibdir)
					cmdarg.add("-Djava.library.path="+libdir);

				
				//Add arguments from environment file
				File javaenvFile=new File("javaenv.txt");
				if(javaenvFile.exists())
					{
					BufferedReader envReader=new BufferedReader(new FileReader(javaenvFile));
					StringTokenizer envTokenizer=new StringTokenizer(envReader.readLine()," ");
					while(envTokenizer.hasMoreTokens())
						cmdarg.add(envTokenizer.nextToken());
					}
				
				//What to run? additional arguments?
				for(String s:args)
					cmdarg.add(s);
//				if(args.length==0)
//					cmdarg.add("endrov.starter.MW");

				//Output jar-list for mac starter bundles
				if(args.contains("-macstarter"))
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
								{
								if(line.startsWith("Could not create the Java Virtual Machine"))
									{
									JOptionPane.showMessageDialog(null, "Trouble creating virtual machine. Try to reduce the ammount of memory allocated");
//normal output follows									
//									Error occurred during initialization of VM
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
		else
			JOptionPane.showMessageDialog(null, "Your version of Java is too old. At least 1.5 required");
		}

	
	
	/**
	 * Get all jars and add them with path to vector. Recurses when it finds a directory
	 * ending with _inc
	 */
	private static void collectJars(List<String> v,List<String> binfiles,String dir, String osExt)
		{
		File p=new File(dir);
		if(p.exists())
			for(File sub:p.listFiles())
				{
				if(sub.isFile() && (sub.getName().endsWith(".jar") || sub.getName().endsWith(".zip")))
					{
					String toadd=sub.getAbsolutePath();//dir+"/"+sub.getName();
					v.add(toadd);
					if(printJavaLib)
						System.out.println("Adding java library: "+toadd);
					}
				else if(sub.isDirectory() && sub.getName().endsWith("_inc") && !sub.getName().startsWith("."))
					collectJars(v,binfiles, sub.getAbsolutePath(), osExt);
				else if(sub.isDirectory() && sub.getName().equals("bin_"+osExt))
					{
					collectJars(v,binfiles, sub.getAbsolutePath(), osExt);
					
					String toadd=sub.getAbsolutePath();//dir+"/"+sub.getName();
					binfiles.add(toadd);
					if(printJavaLib)
						System.out.println("Adding binary directory: "+toadd);
					}
					
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
