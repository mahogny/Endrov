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
		run("ev.jar",args);
		}
	
	public static void run(String mainjar, String[] args)
		{
		String javaver=System.getProperty("java.specification.version");
		String OS=System.getProperty("os.name");
		
		int vermajor=Integer.parseInt(javaver.substring(0,javaver.indexOf('.')));
		int verminor=Integer.parseInt(javaver.substring(javaver.indexOf('.')+1));
		
		if(vermajor>1 || (vermajor==1 && verminor>=5))
			{
			try
				{
				String libdir="";
				String javaexe="java";
				String memstring="-Xmx700M";
				String entrypoint="evgui.GUI";
							
				//Run something else?
				if(args.length>0)
					{
					entrypoint="";
					for(String s:args)
						entrypoint+=" "+s;
					}
				
				//Detect OS
				if(OS.equals("Mac OS X"))
					{
					javaexe="java -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV";
					libdir="libs/mac";
					}
				else if(OS.startsWith("Windows"))
					{
					libdir="libs/windows";
					}
				else //Assume linux or equivalent
					{
					libdir="libs/linux";
					Runtime.getRuntime().exec("export LD_LIBRARY_PATH=libs/linux");
					}
				
				//Collect jarfiles
				Vector<String> jarfiles=new Vector<String>();
				collectJars(jarfiles, "libs");
				collectJars(jarfiles, libdir);
				String jarstring="-cp .";
				if(mainjar!=null)
					jarstring+=":"+mainjar;
				for(String s:jarfiles)
					jarstring+=":"+s;
				
				//Execute command
				String cmd=javaexe+
				" "+jarstring+
				" "+memstring+
				" -Djava.library.path="+libdir+
				" "+entrypoint;
				
				System.out.println(cmd);
				Runtime.getRuntime().exec(cmd);
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
	
	private static void collectJars(Vector<String> v,String dir)
		{
		File p=new File(dir);
		for(File sub:p.listFiles())
			if(sub.isFile() && sub.getName().endsWith(".jar"))
				v.add(dir+"/"+sub.getName());
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
