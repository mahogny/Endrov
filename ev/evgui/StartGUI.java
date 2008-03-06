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
	public static void main(String[] args)
		{
		run(args);
		}
	
	public static void run(String[] args)
		{
		String javaver=System.getProperty("java.specification.version");
		String OS=System.getProperty("os.name");
		String cpsep=":";
		
		int vermajor=Integer.parseInt(javaver.substring(0,javaver.indexOf('.')));
		int verminor=Integer.parseInt(javaver.substring(javaver.indexOf('.')+1));
		
		if(vermajor>1 || (vermajor==1 && verminor>=5))
			{
			try
				{
				String libdir="";
				String javaexe="java";
				String memstring="-Xmx700M";
				//String entrypoint="";
				ProcessBuilder pb=new ProcessBuilder("");
											
				//Detect OS
				if(OS.equals("Mac OS X"))
					{
//					javaexe="java -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV";
					libdir="libs/mac";
					}
				else if(OS.startsWith("Windows"))
					{
					libdir="libs/windows";
					cpsep=";";
					}
				else //Assume linux or equivalent
					{
					libdir="libs/linux";
					pb.environment().put("LD_LIBRARY_PATH", "libs/linux");
					}
				
				//Collect jarfiles
				Vector<String> jarfiles=new Vector<String>();
				collectJars(jarfiles, "libs");
				//collectJars(jarfiles, "libs/ome");
				//collectJars(jarfiles, "libs/ParallelColt/lib");
				collectJars(jarfiles, libdir);
				String jarstring=new File(".").getAbsolutePath();
				for(String s:jarfiles)
					jarstring+=cpsep+s;
				
				//Generate command
				LinkedList<String> cmdarg=new LinkedList<String>();
				cmdarg.add(javaexe);
				cmdarg.add("-cp");
				cmdarg.add(jarstring);
				cmdarg.add(memstring);
				cmdarg.add("-Djava.library.path="+libdir);

				//What to run? additional arguments?
				if(args.length>0)
					for(String s:args)
						cmdarg.add(s);
				else
					cmdarg.add("evgui.GUI");
				
				
				if(args.length>0 && args[args.length-1].equals("-macstarter"))
					{
					StringTokenizer t=new StringTokenizer(jarstring,":");
					File dot=new File(".");
					int dotlen=dot.getAbsolutePath().length()-1;
					String tot="";
					while(t.hasMoreTokens())
						{
						String s=t.nextToken();
						if(!tot.equals(""))tot=tot+":";
						tot=tot+"$APP_PACKAGE/../"+s.substring(dotlen);
						}
					System.out.println(tot);
					}
				
				//Run process
				pb.command(cmdarg);
				Process p=pb.start();
				InputStreamReader isr = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(isr);
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
	private static void collectJars(Vector<String> v,String dir)
		{
		File p=new File(dir);
		for(File sub:p.listFiles())
			{
			if(sub.isFile() && (sub.getName().endsWith(".jar") || sub.getName().endsWith(".zip")))
				{
				String toadd=sub.getAbsolutePath();//dir+"/"+sub.getName();
				v.add(toadd);
//				v.add(sub.getAbsolutePath());
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
