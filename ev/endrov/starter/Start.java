package endrov.starter;

import java.io.*;
import java.util.*;
import javax.swing.*;

import endrov.ev.EvBuild;
import endrov.util.EvFileUtil;


//TODO display error if there is one

/**
 * Start EV, automatically collects which jar-files should be linked
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
	
	private String javaver=System.getProperty("java.specification.version");
	private String OS=System.getProperty("os.name").toLowerCase();
	private String osExt="";
	private String arch=System.getProperty("os.arch").toLowerCase();
	private int javaVerMajor=Integer.parseInt(javaver.substring(0,javaver.indexOf('.')));
	private int javarVerMinor=Integer.parseInt(javaver.substring(javaver.indexOf('.')+1));
	private String cpsep=":";
	private String libdir="";
	private String javaexe="java";
	public List<String> jarfiles=new LinkedList<String>();
	public List<String> binfiles=new LinkedList<String>();
	private ProcessBuilder pb=new ProcessBuilder("");
	private String jarstring="";//new File(".").getAbsolutePath(); //may have to keep this due to matlab
	private String binstring="";
	private String archExt="";

	public void collectSystemInfo(String path)
		{
		collectSystemInfo(new File(path));
		}
	public void collectSystemInfo(File path)
		{
		//Detect OS
		cpsep=":";
		
		if(arch.equals("ppc")) //PowerPC (mac G4 and G5)
			archExt="ppc";
		else
			archExt="x86";
		
		if(OS.equals("mac os x"))
			{
//			javaexe="java -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV";
			libdir=path+"libs/mac";
			osExt="mac";
			}
		else if(OS.startsWith("windows"))
			{
			cpsep=";";
			osExt="windows";
			}
		else if(OS.startsWith("linux"))
			{
			osExt="linux";
			}
		else if(OS.startsWith("solaris"))
			osExt="solaris";
		else
			{
			JOptionPane.showMessageDialog(null, 
					"Your OS + CPU combination is not supported at this moment. We would be happy if you got in\n" +
					"touch so we can support for your platform. If you want to do it yourself it is easy: Get\n" +
					"libraries for your platform (JAI and JOGL), edit endrov/starter/StartGUI.java and recompile.");
			System.exit(1);
			}

		jarstring=path.getAbsolutePath();

		//Collect jarfiles
		collectJars(jarfiles, binfiles, new File(path,"libs"), osExt, archExt);
//		if(!libdir.equals(""))
//			collectJars(jarfiles, binfiles, libdir, osExt);
		for(String s:jarfiles)
			jarstring+=cpsep+s;
		String ldlibpath="";
		for(String s:binfiles)
			{
			if(!binstring.equals(""))
				binstring=binstring+cpsep;
			binstring=binstring+s;
			
			if(!ldlibpath.equals(""))
				ldlibpath=ldlibpath+":";
			ldlibpath=ldlibpath+s;
			}
		libdir=binstring;
		pb.environment().put("LD_LIBRARY_PATH", ldlibpath);
		}
	
	public void run(String[] argsa)
		{
		boolean printMacStarter=false;
		boolean hasSpecifiedLibdir=false;
		boolean printCommand=false;
		File javaenvFile=null;
		File basedir=new File(".");
		String cp2="";
		
		int numNonflagArg=0;
		List<String> args=new LinkedList<String>();
		for(int argi=0;argi<argsa.length;argi++)
			{
			String curarg=argsa[argi];
			
			if(curarg.equals("--printcommand"))
				printCommand=true;
			else if(curarg.equals("--printjar"))
				printJar=true;
			else if(curarg.equals("--macstarter"))
				{
				//Override detection to spit out mac directories
				OS="mac os x"; 
				printMacStarter=true;
				}
			else if(args.contains("--version"))
				{
				//Print current version. need to be put in starter jar to work
				System.out.println("Endrov "+EvBuild.version);
				System.exit(0);
				}
			else if(curarg.equals("--cp2"))
				{
				//Additional jars to add to classpath
				cp2+=":"+argsa[argi+1];
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
			else
				{
				if(!curarg.startsWith("--"))
					numNonflagArg++;
				args.add(curarg);
				}
			}
		
		collectSystemInfo(basedir);
		
		System.out.println("This system runs OS:"+OS+" with java:"+javaver+" on arch:"+arch);
		

		//Continue if java 1.5+
		if(javaVerMajor>1 || (javaVerMajor==1 && javarVerMinor>=5))
			{
			try
				{

				//Generate command
				LinkedList<String> cmdarg=new LinkedList<String>();
				cmdarg.add(javaexe);
				cmdarg.add("-cp");
				cmdarg.add(jarstring+cp2);
//				cmdarg.add(memstring);
				if(!libdir.equals("") && !hasSpecifiedLibdir)
					cmdarg.add("-Djava.library.path="+libdir);

				//Add arguments from environment file
				if(javaenvFile==null)
					javaenvFile=new File(new File("config"),"javaenv."+osExt+".txt");
				if(javaenvFile.exists())
					{
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
				
				//What to run? 
				cmdarg.add(mainClass);
				
				//additional arguments?
				for(String s:args)
					cmdarg.add(s);

				//Output jar-list for mac starter bundles
				if(printMacStarter)
					{
					StringTokenizer t=new StringTokenizer(jarstring,":");
					File dot=new File(".");
					int dotlen=dot.getAbsolutePath().length()-1;
					String tot="";
					//jarstring.replace //more efficient but it is a regexp!
					while(t.hasMoreTokens())
						{
						String s=t.nextToken();
						if(!tot.equals(""))
							tot=tot+":";
						tot=tot+"$APPLICATION/../"+s.substring(dotlen);
						}
					System.out.println(tot);
					
					for(String app:new String[]{"EVGUI.app","ImServ.app","OSTdaemon.app"})
						{
						String template=EvFileUtil.readFile(new File(app+"/Contents/Resources/preinfo.txt"));
						File out=new File(app+"/Contents/Info.plist");
						EvFileUtil.writeFile(out, template.replace("JARLIST", tot));
						System.out.println("Wrote to "+out);
						//TODO also write SO-list
						}
					
					
					FileWriter fw=new FileWriter(new File("EVGUI.app/Contents/Resources/jars.txt"));
					fw.write(tot);
					fw.flush();
					fw.close();
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
			JOptionPane.showMessageDialog(null, "Your version of Java is too old. It must be at least 1.5");
		}


	private static void addJar(List<String> v, String toadd)
		{
		v.add(toadd);
		if(printJar)
			System.out.println("Adding java library: "+toadd);
		}
	
	/**
	 * Get all jars and add them with path to vector. 
	 * Recurses when it finds a directory ending with _inc.
	 * 
	 */
	private static void collectJars(List<String> v,List<String> binfiles,File p, String osExt, String archExt)
		{
		if(p.exists())
			for(File sub:p.listFiles())
				{
				if(sub.isFile() && (sub.getName().endsWith(".jar") || sub.getName().endsWith(".zip")))
					{
					addJar(v,sub.getAbsolutePath());
/*					String toadd=sub.getAbsolutePath();
					v.add(toadd);
					if(printJar)
						System.out.println("Adding java library: "+toadd);*/
					}
				else if(sub.isFile() && (sub.getName().endsWith(".paths")))
					{
					//File containing list of jars or libraries
					//to include. This is to be used on systems
					//where jars are present already.
					
					try
						{
						BufferedReader input =  new BufferedReader(new FileReader(sub));
						String line;
						while((line=input.readLine())!=null)
							{
							if(line.startsWith("j:"))
								addJar(v,line.substring(2)); //j:
							else
								binfiles.add(line.substring(2)); //b:
							}
/*							{
							v.add(line);
							if(printJar)
								System.out.println("Adding external java library: "+line);
							}*/
						}
					catch (Exception e)
						{
						e.printStackTrace();
						}
					}
				else if(sub.isDirectory() && sub.getName().endsWith("_inc") && !sub.getName().startsWith(".") && !sub.getName().equals("unused"))
					collectJars(v,binfiles, sub, osExt, archExt);
				else if(sub.isDirectory() && (sub.getName().equals("bin_"+osExt)
						|| sub.getName().equals("bin_"+archExt)))
					{
					collectJars(v,binfiles, sub, osExt, archExt);
					
					String toadd=sub.getAbsolutePath();
					binfiles.add(toadd);
					if(printJar)
						System.out.println("Adding binary directory: "+toadd);
					}
				//else
				//	System.out.println("Unknown file "+sub);
					
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
