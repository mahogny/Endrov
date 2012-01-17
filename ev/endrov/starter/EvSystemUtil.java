package endrov.starter;

import java.io.File;

public class EvSystemUtil
	{

	/**
	 * Report which directory to store application specific configurations in
	 * http://standards.freedesktop.org/basedir-spec/basedir-spec-latest.html
	 */
	static File getGlobalConfigDir()
		{
		String e=System.getenv("XDG_CONFIG_HOME");
		if(e==null)
			{
			//TODO Mac might have Libraries
			if(EvSystemUtil.isWindows())
				return new File("C:\\config");
			else if(EvSystemUtil.isMac())
				return new File(new File(EvSystemUtil.getHomeDir(),"Library"),"Application Support");
			else
				return new File(EvSystemUtil.getHomeDir(),".config");
			}
		else
			return new File(e);
		}

	public static File getGlobalConfigEndrovDir()
		{
		return new File(getGlobalConfigDir(),"endrov");
		}

	/**
	 * Get name of config file in case it is stored as an individual file
	 */
	public static File getPersonalConfigFileName()
		{
		return new File(getGlobalConfigEndrovDir(),"config.xml");
		}

	/**
	 * Get name of config file in case it is stored as an individual file
	 */
	public static File getSystemConfigFileName()
		{
		return new File(getGlobalConfigEndrovDir(),"sysconfig.xml");
		}
	
	/**
	 * Get name of javaenv file in case it is stored as an individual file
	 */
	public static File getJavaenvWriteFileName()
		{
		return new File(getGlobalConfigEndrovDir(),"javaenv.txt");
		}
	public static File getJavaenvReadFileName()
		{
		File f=getJavaenvReadFileName();
		if(f.exists())
			return f;
		else
			{
			String platformExt;
			if(EvSystemUtil.OS.equals("mac os x"))
				platformExt="mac";
			else if(EvSystemUtil.OS.startsWith("windows"))
				platformExt="windows";
			else if(EvSystemUtil.OS.startsWith("linux"))
				platformExt="linux";
			else if(EvSystemUtil.OS.startsWith("sunos"))
				platformExt="solaris";
			else
				platformExt="other";
			return new File(new File("config"),"javaenv."+platformExt+".txt");
			}
		}


	
	/**
	 * Get name of log file
	 */
	public static File getLogFileName()
		{
		return new File(getGlobalConfigEndrovDir(),"log.txt");
		}

	/**
	 * Directory where all user data normally is stored
	 */
	public static File getHomeDir()
		{
		if(EvSystemUtil.isWindows())
			return new File("C:\\");
		else
			{
			String e=System.getenv("HOME");
			if(e!=null)
				return new File(e);
			else
				return new File("/");
			}
		}

	/**
	 * Check if the system is running a mac
	 */
	public static boolean isMac()
		{
		return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
		}

	/**
	 * Check if the system is running Windows
	 */
	public static boolean isWindows()
		{
		return System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1;
		}

	/**
	 * Check if the system is running Linux
	 */
	public static boolean isLinux()
		{
		return System.getProperty("os.name").toUpperCase().indexOf("LINUX") != -1;
		}

	public static boolean isX86()
		{
		return System.getProperty("os.arch").contains("86");
		}

	public static boolean isPPC()
		{
		return System.getProperty("os.arch").contains("ppc");
		}

	public static final String javaver=System.getProperty("java.specification.version");
	public static final String arch=System.getProperty("os.arch").toLowerCase();
	public static final int javaVerMajor=Integer.parseInt(javaver.substring(0,javaver.indexOf('.')));
	public static final int javaVerMinor=Integer.parseInt(javaver.substring(javaver.indexOf('.')+1));
	public static String OS=System.getProperty("os.name").toLowerCase();

	}
