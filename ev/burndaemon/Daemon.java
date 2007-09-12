package burndaemon;

import java.io.*;
import java.util.*;



/*
 * mkdir /Volumes/TBU_xeon01_500GB01/burnpathes/TBU1186062873
ln -s /Volumes/TBU_xeon01_500GB01/daemonoutputfixed/PS3504_070723 /Volumes/TBU_xeon01_500GB01/burnpathes/TBU1186062873/
ln -s /Volumes/TBU_xeon01_500GB01/daemonoutputfixed/TB2124_070724 /Volumes/TBU_xeon01_500GB01/burnpathes/TBU1186062873/
mkisofs -posix-L -joliet-long -R -V TBU1186062873 -o /Volumes/TBU_xeon01_500GB02/TBU1186062873.iso /Volumes/TBU_xeon01_500GB01/burnpathes/TBU1186062873
drutil burn -verify -eject /Volumes/TBU_xeon01_500GB02/TBU1186062873.iso

 */




/**
 * Burn daemon - takes directories in a folder and writes them to CD/DVD
 * @author Johan Henriksson
 */
public class Daemon extends Thread
	{
	private boolean shutDown;
	private boolean isRunning;
	
	/** Shut down whenever possible */
	public void shutDown() {shutDown=true;}
	/** Start whenever possible */
	public void go() {shutDown=false;}
	/** Check if daemon is running */	
	public boolean isRunning() {return isRunning;}
	
	private DaemonListener daemonListener;
	
	/** Cached size of directories */
	HashMap<String,Integer> dirSizes=new HashMap<String,Integer>();
	/** Times when some directory was burned */
	HashMap<String,Vector<Long>> burnTimes=new HashMap<String,Vector<Long>>();

	
	
	private String pathInput="";
	private String pathTemp="burndaemon";
	private String deviceName="atapi";
	private String burnTimeFile="burndaemon/burntimes.txt";
	
	//http://www.osta.org/technology/dvdqa/dvdqa6.htm
	//long maxMediaSize=4589843; //without file system
	long maxMediaSize=4300000;
	//4458232 partition reported capacity for dmg
	

	
	/**
	 * Files sorted by size
	 */
	public static class FileSortable implements Comparable
		{
		public File f;
		public int size;
		
		public int compareTo(Object o)
			{
			FileSortable oo=(FileSortable)o;
			if(size<oo.size)
				return -1;
			else if(size>oo.size)
				return 1;
			else
				return 0;
			}
		}
	
	
	
	/**
	 * Create daemon
	 * @param listener Listener for log events. Can be null.
	 */
	public Daemon(DaemonListener listener)
		{
		daemonListener=listener;
		}
	
	/**
	 * Add to log
	 */
	public void log(String s)
		{
		if(daemonListener!=null)
			daemonListener.daemonLog(s);
		}
	
	private void log(String[] s)
		{
		String total="";
		for(String s2:s)
			total=total+s2+" ";
		log(total);
		}
	
	public void error(String s, Exception e)
		{
		if(daemonListener!=null)
			daemonListener.daemonError(s,e);
		}
	
	
	/**
	 * Pause while "shut down"
	 */
	private void shutDownLoop()
		{
		if(shutDown)
			{
			log("Stopped");
			isRunning=false;
			while(shutDown)
				try {sleep(500);}
				catch (InterruptedException e){}
			isRunning=true;
			log("Started");
			}
		}
	
	/**
	 * The main function of the thread
	 */
	public void run()
		{
		readBurnTimes();
		shutDown=true;
		shutDownLoop();
		for(;;)
			{
			shutDownLoop();

			File inputDir=new File(pathInput);
			if(inputDir.exists() && inputDir.isDirectory())
				{
				TreeSet<FileSortable> sortedFiles=new TreeSet<FileSortable>();
				
				//Calculate size of directories
				int toburnSize=0;
				for(File f:inputDir.listFiles())
					if(!f.getName().startsWith(".") && f.exists())
						{
						shutDownLoop();
						if(	needsBurn(f.getName()))
							{
							try
								{
								FileSortable fs=new FileSortable();
								fs.f=f;
								fs.size=dirSize(f);
								toburnSize+=fs.size;
								sortedFiles.add(fs);
								}
							catch (IOException e)
								{
								error(null,e);
								}
							}
						else
							log("No need to write "+f.getName());
						}
				log("Total left to burn: "+(toburnSize/1024)+"MiB");
				
				Vector<File> files=getFilesToBurn(sortedFiles);
				
				//Burn one image
				if(!files.isEmpty())
					burnSession(files);
				}
			else
				error("Could not find pathInput, "+inputDir.getAbsolutePath(),null);
			
			//Wait a bit until next scan, otherwise the program will waste cpu
			try {sleep(500);}
			catch (InterruptedException e){}
			}
		}

	
	/**
	 * Select files to take. Destroys argument
	 */
	public Vector<File> getFilesToBurn(SortedSet<FileSortable> fs)
		{
		Vector<File> take=new Vector<File>();
		int totalSize=0;

		while(!fs.isEmpty())
			{
			FileSortable test=fs.last();
			fs.remove(fs.last());

			if(totalSize+test.size<maxMediaSize)
				{
				take.add(test.f);
				totalSize+=test.size;
				log("Including "+test.f.getName());
				}
			else if(totalSize==0)
				log("Warning: Too big to fit on media alone: "+test.f.getName());
			}
		
		log("Total size: "+(totalSize/1024)+"MiB");
		
		return take;
		}
	
	
	
	/**
	 * Get the total size of a directory
	 */
	public int dirSize(File f) throws IOException
		{
		if(dirSizes.containsKey(f.getName()))
			return dirSizes.get(f.getName());
		else
			{
			//Call DU
			log("Calculating size of "+f.getName());
			Process p=Runtime.getRuntime().exec(new String[]{"/usr/bin/du","-k","-d","0",f.getAbsolutePath()});
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String firstLine=stdInput.readLine();
		  while ((stdInput.readLine()) != null);
			
		  //Parse out size in kilobytes
		  StringTokenizer tok=new StringTokenizer(firstLine,"\t");
		  String sizes=tok.nextToken();
		  int size=Integer.parseInt(sizes);
		  
		  //Store in cache
		  dirSizes.put(f.getName(), size);
		  return size;
			}
		}
	
	
	/**
	 * Check if a file is in need of recording
	 */
	public boolean needsBurn(String name)
		{
		return !burnTimes.containsKey(name);
		}

	/**
	 * Add a time of recording
	 */
	public void addBurnTime(String name, long time)
		{
		Vector<Long> tv=burnTimes.get(name);
		if(tv==null)
			{
			tv=new Vector<Long>();
			burnTimes.put(name, tv);
			}
		tv.add(time);
		writeBurnTimes();
		}
	

	
	/**
	 * One burning session
	 */
	public void burnSession(Vector<File> files)
		{
		try
			{
			long recordId=System.currentTimeMillis()/1000;
			
			//Create place to put everything in
			File tempDir=new File(pathTemp);
			File rootDir=new File(tempDir,"temprec");
			File isoFile=new File(tempDir,"tempiso.iso");
			rootDir.mkdirs();
			
			//Delete old symlinks in case there are any
			for(File f:rootDir.listFiles())
				f.delete();
			
			//Create symlinks
			for(File f:files)
				{
				File link=new File(rootDir,f.getName());
				makeSymLink(f, link);
				}
			
			//Make iso
			log("Making ISO");
			String[] isoargs=new String[]{"/usr/bin/mkisofs","-posix-L","-joliet-long","-R","-V",""+recordId,"-o",isoFile.getAbsolutePath(),rootDir.getAbsolutePath()};
			log(isoargs);
			waitProcess(Runtime.getRuntime().exec(isoargs));
			
			//Wait for user
			log("Ready to burn. Insert media. Then close and choose ignore in the popup. Finally, hit start");
			ejectCD();
			shutDown=true;
			shutDownLoop();
			
			//Burn
			log("Burning");
			//closeCD();
			String[] burnargs=new String[]{"/usr/bin/drutil","burn","-drive",deviceName, "-verify","-eject", isoFile.getAbsolutePath()};
			log(burnargs);
			waitProcess(Runtime.getRuntime().exec(burnargs));
			ejectCD();
			
			//Note burn file
			for(File f:files)
				addBurnTime(f.getName(), recordId);

			//Show what to label
			String label="Please label media: ("+recordId+") ";
			for(File f:files)
				label+=f.getName()+" ";
			log(label);
			
			//Remove ISO
			log("Removing ISO");
			isoFile.delete();
			
			//Delete symlinks
			for(File f:rootDir.listFiles())
				f.delete();
			rootDir.delete();
			}
		catch (IOException e)
			{
			error(null,e);
			}
		}


	
	
	/**
	 * Store burn times on disk
	 */
	public void writeBurnTimes()
		{
		try
			{
			BufferedWriter signalfile = new BufferedWriter(new FileWriter(burnTimeFile));
			for(String name:burnTimes.keySet())
				{
				signalfile.write(name);
				for(Long time:burnTimes.get(name))
					signalfile.write("\t"+time);
				signalfile.write("\n");
				}
			signalfile.close();
			}
		catch (IOException e)
			{
			error("Error writing burn times file",e);
			}
		}

	/**
	 * Read burn times from disk
	 */
	public void readBurnTimes()
		{
		if(!(new File(burnTimeFile)).exists())
			log("No burn times file");
		else
			{
			log("Reading burn times file");
			burnTimes.clear();
			BufferedReader input = null;
	    try
	    	{
	    	input = new BufferedReader( new FileReader(burnTimeFile) );
	    	String line = null;
	    	while (( line = input.readLine()) != null)
	    		{
	    		StringTokenizer tok=new StringTokenizer(line,"\t");
	    		if(!tok.hasMoreTokens())
	    			continue;
	    		String name=tok.nextToken();
	    		while(tok.hasMoreTokens())
	    			{
	    			long time=Long.parseLong(tok.nextToken());
	    			addBurnTime(name, time);
	    			}
	    		}
	    	}
	    catch(Exception e)
	    	{
	    	error(null,e);
	    	}
			}
		}
	

	
	/**
	 * Update configuration
	 */
	public void readConfig(String filename)
		{
		log("Reading config file "+filename);
		BufferedReader input = null;
    try
    	{
    	input = new BufferedReader( new FileReader(filename) );
    	String line = null;
    	while (( line = input.readLine()) != null)
    		{
    		StringTokenizer tok=new StringTokenizer(line);
    		if(!tok.hasMoreTokens())
    			continue;
    		String cmd=tok.nextToken();
    		if(cmd.equals("pathinput"))
    			{
    			String path=tok.nextToken();
    			File f=new File(path);
    			pathInput=f.getAbsolutePath();
    			log("Set pathInput = "+pathInput);
    			}
    		else if(cmd.equals("device"))
    			{
    			deviceName=tok.nextToken();
    			log("Set device = "+deviceName);
    			String[] burnargs=new String[]{"/usr/bin/drutil","-drive",deviceName,"info"};
    			waitProcess(Runtime.getRuntime().exec(burnargs));
    			}
    		else
    			log("Unknown parameter in config file: "+cmd);
    		}
    	}
    catch(Exception e)
    	{
    	error(null,e);
    	}
		}
	
	
	

	/**
	 * Wait for a process and also print what is going on
	 */
	public void waitProcess(Process p) throws IOException
		{
		BufferedReader stdInput = new BufferedReader(new 
	      InputStreamReader(p.getInputStream()));
		String line;
	  while ((line=stdInput.readLine()) != null)
	  	log(line);
		}

	
	
	/**
	 * Eject CD
	 */
	public void ejectCD() throws IOException
		{
		waitProcess(Runtime.getRuntime().exec(new String[]{
				"/usr/bin/drutil","-drive",deviceName, "tray","eject"
		}));
		}

	/**
	 * Close CD
	 */
	public void closeCD() throws IOException
		{
		waitProcess(Runtime.getRuntime().exec(new String[]{
				"/usr/bin/drutil","-drive",deviceName, "tray","close"
		}));
		try {sleep(3000);}
		catch (InterruptedException e){}
		}
	
	
	/**
	 * Make a symbolic link
	 */
	public void makeSymLink(File f, File newLink) throws IOException
		{
		if(newLink.exists())
			newLink.delete();
		waitProcess(Runtime.getRuntime().exec(new String[]{"/bin/ln","-s",f.getAbsolutePath(),newLink.getAbsolutePath()}));
		}


	
	}