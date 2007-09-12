package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import evplugin.ev.EV;
import evplugin.ev.ParamParse;
import evplugin.imageset.*;

public class Join12
	{
	public static void join12(File basedir)
		{
		File xmlfile=new File(basedir,"rmd.xml");
		File bakfile=new File(xmlfile.getAbsolutePath()+".bak");
		if(bakfile.exists())
			return;
		
		OstImageset rec=new OstImageset(basedir.getAbsolutePath());
		if(rec.isOst2)
			{
			boolean hasframe=false;
			for(String channel:rec.meta.channel.keySet())
				{
				File rmdfile=new File(new File(rec.basedir,rec.getMetadataName()+"-"+channel),"rmd.txt");
				if(rmdfile.exists())
					{
					hasframe=true;
					System.out.println("Ch "+channel);
					rec.meta.metaFrame.clear();

					try
						{
						copyRecursive(xmlfile, bakfile);
						}
					catch (IOException e)
						{
						e.printStackTrace();
						}
					
					
					readVWBMeta(rec.meta, rec.meta.channel.get(channel), rmdfile.getAbsolutePath());
					}
				}
			
			if(hasframe)
				{
				System.out.println("has rmd. resave");
				rec.saveMeta();
				}
			}
		}

	private static void waitProcess(Process p) throws IOException
	{
	BufferedReader stdInput = new BufferedReader(new 
      InputStreamReader(p.getInputStream()));
  while ((stdInput.readLine()) != null);
	}
	
	/**
	 * Corresponds to cp -r. Will use current time.
	 */
	public static void copyRecursive(File from, File to) throws IOException
		{
		String f=from.getPath();
		String t=to.getPath();
		System.out.println("Copy "+f+" to "+t);
		waitProcess(Runtime.getRuntime().exec(new String[]{"/bin/cp","-R", f,t}));
		}
	
	
	/**
	 * Read corresponding meta data for this channel.
	 * Only meant for loading OST1.
	 */
	public static void readVWBMeta(ImagesetMeta meta, ImagesetMeta.Channel ch, String metaFilename)
		{
		Vector<String> framemeta=new Vector<String>();
		int framecol=0;
		
		ParamParse p=new ParamParse(new File(metaFilename));
		while(p.hasData())
			{
			Vector<String> arg=p.nextData();
			if(arg.size()!=0)
				{
				try
					{
					if(arg.get(0).equals("binning"))
						;
					else if(arg.get(0).equals("resx"))
						;
					else if(arg.get(0).equals("resy"))
						;
					else if(arg.get(0).equals("resz"))
						;
					else if(arg.get(0).equals("NA"))
						;
					else if(arg.get(0).equals("objective"))
						;
					else if(arg.get(0).equals("optivar"))
						;
					else if(arg.get(0).equals("campix"))
						;
					else if(arg.get(0).equals("slicespacing"))
						;
					else if(arg.get(0).equals("timestep"))
						;
					else if(arg.get(0).equals("sample"))
						;
					else if(arg.get(0).equals("descript"))
						;
					else if(arg.get(0).equals("dispx"))
						;
					else if(arg.get(0).equals("dispy"))
						;
					else if(arg.get(0).equals("framemeta"))
						{
						framemeta.clear();
						for(int i=1;i<arg.size();i++)
							{
							framemeta.add(arg.get(i));
							if(arg.get(i).equals("frame"))
								framecol=i-1;
							}
						meta.metaFrame.clear();
						}
					else if(arg.get(0).equals("framedata"))
						{
						Vector<String> data=new Vector<String>();
						int frame=Integer.parseInt(arg.get(framecol+1));
						for(int i=1;i<arg.size();i++)
							data.add(arg.get(i));
						
						for(int i=0;i<data.size();i++)
							if(i!=framecol)
								ch.getMetaFrame(frame).put(framemeta.get(i),data.get(i));
						//System.out.println("framedata");
						}
					else
						;
					}
				catch(Exception e)
					{
					e.printStackTrace();
					System.out.println(e.getMessage());
					System.out.println("Parse error in tag. Skipping "+arg.get(0));
					}
				}
			}
		}
	
	
	/**
	 * Entry point
	 * @param args Command line arguments
	 */
	public static void main(String[] arg)
		{
		EV.loadPlugins();

		if(arg.length==0)
			arg=new String[]{"/Volumes/TBU_xeon01_500GB01/daemonoutputfixed/"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					join12(file);
		}
	}
