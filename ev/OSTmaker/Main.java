package OSTmaker;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import endrov.data.EvData;
import endrov.ev.*;
import endrov.imageset.Imageset;
import endrov.imagesetOST.deleteSaveOSTThread;

/**
 * Import imagesets in varying formats
 * @author Johan Henriksson
 */
public class Main
	{
	public static HashMap<String, Integer> chancomp=new HashMap<String, Integer>();
	
	/**
	 * Convert one imageset
	 */
	public static boolean convert(File infile)
		{
		try
			{
			String outfilename=infile.getName();
			if(outfilename.indexOf(".")!=-1)
				{
				outfilename=outfilename.substring(0,outfilename.lastIndexOf("."));
				File outfile=new File(infile.getParentFile(),outfilename);
				
				System.out.println("Loading imageset "+infile.getAbsolutePath());
				EvData data=EvData.loadFile(infile);
				Imageset inim=data.getObjects(Imageset.class).iterator().next();
//				BioformatsImageset inim=new BioformatsImageset(infile.getAbsolutePath());
				
				//Set compression
				for(String chname:inim.channelImages.keySet())
					if(chancomp.containsKey(chname))
						inim.channelMeta.get(chname).compression=chancomp.get(chname);
				
				//the save system could now be replaced by writable OST imagesets.
				//problem though: lack a system to set compression rates, write locks are in, rather ugly in general
				System.out.println("Saving to: "+outfile);
				new CompleteBatch(new deleteSaveOSTThread(inim, outfile.getAbsolutePath()));
				return true;
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			System.out.println("Failed to convert imageset "+infile.getAbsolutePath());
			}
		return false;
		}
		
	
	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		Vector<File> infiles=new Vector<File>();
		
		if(args.length==0)
			{
			new GUI();
			}
		else
			{
			//Parse input
			for(int i=0;i<args.length;i++)
				{
				String ta=args[i];
				

				if(ta.equals("-c"))
					{
					String channel=args[i+1];
					int comp=Integer.parseInt(args[i+2]);
					chancomp.put(channel,comp);
					i+=2;
					}
				else if(ta.equals("-h") || ta.equals("--help"))
					{
					printHelp();
					System.exit(1);
					}
				else if(ta.startsWith("-"))
					{
					System.out.println("Unknown argument: "+ta);
					printHelp();
					System.exit(1);
					}
				else
					{
					File f=new File(ta);
					if(!f.exists())
						{
						System.out.println("File does not exist: "+ta);
						System.exit(1);
						}
					infiles.add(f);
					}
				}
			
			//Convert
			if(infiles.isEmpty())
				{
				System.out.println("No input files");
				printHelp();
				System.exit(1);
				}
			else
				{
/*				System.out.println("Starting Endrov");
				Log.listeners.add(new StdoutLog());
				EV.loadPlugins();
				System.out.println();
	*/			
				for(File infile:infiles)
					convert(infile);
				}
			}
		}

	
	
	public static void printHelp()
		{
		System.out.println("Endrov OST maker - Convert imagesets to OST");
		System.out.println("--------------------------------------------------------------------------------------------------------");
		System.out.println("java -jar OSTmaker.jar [arguments] INFILE1 INFILE2 ...");
		System.out.println("-h                       This help message");
		System.out.println("-c CHANNEL COMPRESSION   Set compression level for channel, 0 to 100, where 100 corresponds to lossless.");
		
		
		
		
		}
	
	
	}
