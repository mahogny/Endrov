package endrov.ev;


import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * Plugin loader
 * @author Johan Henriksson
 */
public class OldPluginInfo
	{
	public String classPath="";
	public Vector<String> className=new Vector<String>();
	public Vector<String> require=new Vector<String>();
	public String name="";
	public String author="";
	public String readme="";
	public String cite="";
	public String doc=null;
	
	private boolean exists;

	private final String suppliedFilename;
	
	/**
	 * Check if there was a plugin in the directory
	 */
	public boolean exists()
		{
		return exists;
		}

	/**
	 * Read directory for plugin information
	 * @param filename
	 */
	public OldPluginInfo(String filename)
		{
		suppliedFilename=filename;
		BufferedReader input = null;
		File file=new File(new File(filename),"PLUGIN.txt");
		exists=false;
		if(file.exists())
			{
			
			classPath=(new File(filename)).getAbsolutePath().
				substring(-1+(new File(".")).getAbsolutePath().length()).replace('/', '.').replace('\\','.');
			
			try
				{
				input = new BufferedReader(new FileReader(file));
				String line = null;
				while (( line = input.readLine()) != null)
					{
					StringTokenizer tok=new StringTokenizer(line);
					if(!tok.hasMoreTokens() || line.charAt(0)=='#')
						continue;
					String cmd=tok.nextToken();
					if(cmd.equals("class"))
						className.add(line.substring(5).trim());
					else if(cmd.equals("name"))
						name=line.substring(4).trim();
					else if(cmd.equals("author"))
						author=line.substring(6).trim();
					else if(cmd.equals("cite"))
						cite=line.substring(4).trim();
					else if(cmd.equals("require"))
						require.add(line.substring(8).trim());
					}
				}
			catch (IOException e) 
				{
				EvLog.printError("plugin read error", e);
				}
			if(className.size()!=0)
				exists=true;
			}
		}
	
	
	/**
	 * Load the plugin
	 */
	public void load()
		{
		if(supported())
			{
			EvLog.printLog("Loading plugin "+classPath);
			for(String cn:className)
				{
				try
					{
					Class<?> foo=ClassLoader.getSystemClassLoader().loadClass(classPath+"."+cn);
					Method m=foo.getDeclaredMethod("initPlugin", new Class[]{});
					m.invoke(foo, new Object[]{});
					}
				catch (ClassNotFoundException e)
					{
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not load EV; is plugins directory ok?");
					}
				catch (Exception e)
					{
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not load EV; plugin "+classPath+" broken");
					}
				}
			}
		else
			EvLog.printLog("Skipping plugin "+classPath+" as it is not supported");
		}
	

	/**
	 * Load the documentation
	 */
	public void loadDoc()
		{
		if(doc==null)
			{
			doc="";
			
			BufferedReader input = null;
			File file=new File(new File(suppliedFilename),"README.txt");
			if(file.exists())
				{
				try
					{
					input = new BufferedReader(new FileReader(file));
					String line = null;
					while (( line = input.readLine()) != null)
						{
						doc+=line+"\n";
						/*
						if(line.equals(""))
							doc+="\n";
						else
							{
							if(line.charAt(0)==' ')
								doc+="\n";
							else
								doc+=" ";
							doc+=line;
							}
						*/
						}
					}
				catch (IOException e) 
					{  
					EvLog.printError("plugin read error",e); 
					}

				}
			}
				
		}
	
	public String toString()
		{
		return classPath;
		}
	
	/**
	 * Check if plugin requirements are fulfilled
	 */
	public boolean supported()
		{
		if(require.size()==0)
			return true;
		else
			{
			for(String s:require)
				{
				if(s.equals("macosx"))
					{
					if(EV.isMac())
						{
						EvLog.printLog("Requires Mac OS X: Ok");
						return true;
						}
					else
						EvLog.printLog("Requires Mac OS X: No");
					}
				}
			return false;
			}
		}
	}
