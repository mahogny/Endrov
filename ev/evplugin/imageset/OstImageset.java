package evplugin.imageset;

import java.io.*;
import java.util.*;
//import org.jdom.*;
//import org.jdom.input.*;
import evplugin.ev.*;


/**
 * Support for the native VWB file format
 * @author Johan Henriksson
 */
public class OstImageset extends Imageset
	{
	
	
	
	/** Path to imageset */
	public String basedir;

	/** Is the imageset OST2? */
	public boolean isOst2;
	
	/**
	 * Create a new recording. Basedir points to imageset- ie without the channel name
	 * @param basedir
	 */
	public OstImageset(String basedir)
		{
		this.basedir=basedir;
		this.imageset=(new File(basedir)).getName();
		buildDatabase();
		}
	
	public String toString()
		{
		return getMetadataName();
		}

	

	public File datadir()
		{
		File datadir=new File(basedir,getMetadataName()+"-data");
		datadir.mkdirs();
		return datadir;
		}

	
	/**
	 * Save meta for all channels into RMD-file
	 */
	public void saveMeta()
		{
		saveMeta(new File(basedir,"rmd.xml"));

		if(!isOst2)
			{
			Log.printLog("Renaming files to fit OST2");
			renameOst1ToOst2(basedir);
			buildDatabase();
			}
		
		//Update date of datadir to have it backuped
		touchRecursive(datadir(), System.currentTimeMillis());
		
		//TODO: save images here too
		setMetadataModified(false);
		}
	
	
	/**
	 * Show setup
	 */
	/*
	public void setup()
		{
		JOptionPane.showMessageDialog(null, "No setup for this imageset format");
		}
	*/
	
	/**
	 * Scan recording for channels and build a file database
	 */
	public void buildDatabase()
		{
		File basepath=new File(basedir);
		File metaFile=new File(basepath,"rmd.xml");
		isOst2=metaFile.exists();
		imageset=basepath.getName();
		Log.printLog("is OST2: "+isOst2);
		if(basepath.exists())
			{
			//Load metadata
			if(isOst2)
				{
				loadXmlMetadata(metaFile.getPath());
				for(int oi:metaObject.keySet())
					if(metaObject.get(oi) instanceof ImagesetMeta)
						{
						meta=(ImagesetMeta)metaObject.get(oi);
						metaObject.remove(oi);
						break;
						}
				}
			else
				{
				meta=new ImagesetMeta();
				File[] dirfiles=basepath.listFiles();
				for(File f:dirfiles)
					if(f.isDirectory() && !f.getName().startsWith(".") && !f.getName().endsWith("-data"))
						{
						String fname=f.getName();
						String channelName=fname.substring(fname.lastIndexOf('-')+1);
						ImagesetMeta.Channel mc=meta.getChannel(channelName);
						File chandir=new File(basepath,imageset+"-"+channelName);
						File metafile=new File(chandir,"rmd.txt");
						OstImageset.readVWBMeta(meta, mc, metafile.getPath());
						}
				}

			if(!loadDatabaseCache())
				{
				//Check which files exist
				File[] dirfiles=basepath.listFiles();
				for(File f:dirfiles)
					if(f.isDirectory() && !f.getName().startsWith(".") && !f.getName().endsWith("-data"))
						{
						String fname=f.getName();
						String channelName=fname.substring(fname.lastIndexOf('-')+1);
						Log.printLog("Found channel: "+channelName);
						Channel c=new Channel(meta.getChannel(channelName));
						c.scanFiles();
						channelImages.put(channelName,c);
						}
				saveDatabaseCache();
				}
			}
		else
			Log.printError("Error: Imageset base directory does not exist",null);
		
		}
	
	/**
	 * Get the name of the database cache file
	 */
	private File getDatabaseCacheFile()
		{
//		return new File(basedir,"imagecache.xml");
		return new File(basedir,"imagecache.txt");
		}
	
	/**
	 * Load database from cache. Return if it succeeded
	 */
	public boolean loadDatabaseCache()
		{
		try
			{
			String ext="";
			BufferedReader in = new BufferedReader(new FileReader(getDatabaseCacheFile()));
		 
			String line=in.readLine();
			if(!line.equals("version1"))
				{
				Log.printLog("Image cache wrong version, ignoring");
				return false;
				}
			else
				{
				Log.printLog("Loading imagelist cache");
				
				channelImages.clear();
				int numChannels=Integer.parseInt(in.readLine());
				for(int i=0;i<numChannels;i++)
					{
					String channelName=in.readLine();
					int numFrame=Integer.parseInt(in.readLine());
					ChannelImages c=getChannel(channelName);
					if(c==null)
						{
						c=new Channel(meta.getChannel(channelName));
						channelImages.put(channelName,c);
						}
					for(int j=0;j<numFrame;j++)
						{
						int frame=Integer.parseInt(in.readLine());
						int numSlice=Integer.parseInt(in.readLine());
						TreeMap<Integer,EvImage> loaderset=c.imageLoader.get(frame);
						if(loaderset==null)
							{
							loaderset=new TreeMap<Integer,EvImage>();
							c.imageLoader.put(frame, loaderset);
							}
						
						for(int k=0;k<numSlice;k++)
							{
							String s=in.readLine();
							if(s.startsWith("ext"))
								{
								ext=s.substring(3);
								s=in.readLine();
								}
							int slice=Integer.parseInt(s);
							
							loaderset.put(slice, new EvImageJAI(buildImagePath(channelName, frame, slice, ext).getAbsolutePath()));
							}
						}
					}
				return true;
				}
			}
		catch(FileNotFoundException e)
			{
			return false;
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return false;
			}
		}
	
	public File buildImagePath(String channelName, int frame, int slice, String ext)
		{
		if(isOst2)
			{
			File frameDir=new File(new File(basedir,getMetadataName()+"-"+channelName), EV.pad(frame,8));
			return new File(frameDir,EV.pad(slice, 8)+ext);
			}
		else
			{
			File frameDir=new File(new File(basedir,getMetadataName()+"-"+channelName), channelName+"-"+EV.pad(frame,8));
			return new File(frameDir,channelName+"-"+EV.pad(frame,8)+"-"+EV.pad(slice, 8)+ext);
			}
		}
	
	
	/**
	 * Save database as a cache file
	 */
	public void saveDatabaseCache()
		{
		try
			{
			String lastExt="";
			BufferedWriter w=new BufferedWriter(new FileWriter(getDatabaseCacheFile()));
			
			w.write("version1\n");

			w.write(channelImages.size()+"\n");
			for(ChannelImages c:channelImages.values())
				{
				w.write(c.getMeta().name+"\n");
				w.write(""+c.imageLoader.size()+"\n");
				for(int frame:c.imageLoader.keySet())
					{
					w.write(""+frame+"\n");
					w.write(""+c.imageLoader.get(frame).size()+"\n");
					for(int slice:c.imageLoader.get(frame).keySet())
						{
						EvImage loader=c.getImageLoader(frame, slice);
						File imagefile=new File(loader.sourceName());
						String filename=imagefile.getName();
						String ext="";
						if(filename.indexOf('.')!=-1)
							ext=filename.substring(filename.indexOf('.'));
						
						if(!ext.equals(lastExt))
							{
							w.write("ext"+ext+"\n");
							lastExt=ext;
							}
						
						w.write(""+slice+"\n");
						}
					}
				}
			w.close();
			Log.printLog("Wrote cache file");
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	/**
	 * Invalidate database cache (=deletes cache file)
	 */
	public void invalidateDatabaseCache()
		{
		getDatabaseCacheFile().delete();
		}
	
	
	///// this custom channel is messing up more than helping //////////
	
	/**
	 * OST channel - contains methods for building frame database
	 */
	public class Channel extends Imageset.ChannelImages
		{
		public Channel(ImagesetMeta.Channel channelName)
			{
			super(channelName);
			}
		
		/** Get directory for channel */
		private String channelDir()
			{
			return basedir+"/"+imageset+"-"+getMeta().name+"/";
			}
		
		/**
		 * Scan all files for this channel and build a database
		 */
		public void scanFiles()
			{
			imageLoader.clear();
			
			File chandir=new File(channelDir());
			File[] framedirs=chandir.listFiles();
			for(File framedir:framedirs)
				if(framedir.isDirectory() && !framedir.getName().startsWith("."))
					{
					int framenum;
					if(isOst2)
						framenum=Integer.parseInt(framedir.getName());
					else
						{
						String sframenum=framedir.getName();
						sframenum=sframenum.substring(sframenum.lastIndexOf('-')+1);
						framenum=Integer.parseInt(sframenum);
						}
					
					TreeMap<Integer,EvImage> loaderset=new TreeMap<Integer,EvImage>();
					File[] slicefiles=framedir.listFiles();
					for(File f:slicefiles)
						{
						String partname=f.getName();
						if(!partname.startsWith("."))
							{
							if(isOst2)
								partname=partname.substring(0,partname.lastIndexOf('.'));
							else
								{
								partname=partname.substring(0,partname.lastIndexOf('.'));
								partname=partname.substring(partname.lastIndexOf('-')+1);
								}
							try
								{
								int slicenum=Integer.parseInt(partname);
								loaderset.put(slicenum, new EvImageJAI(f.getAbsolutePath()));
								}
							catch (NumberFormatException e)
								{
								Log.printError("partname: "+partname+" filename "+f.getName()+" framenum "+framenum,e);
								System.exit(1);
								}
							}
						}
					imageLoader.put(framenum, loaderset);
					}
			}
		}
	
	
	/**
	 * Convert files OST1 to OST2. Does not delete the old rmd.txt
	 */
	public static void renameOst1ToOst2(String root)
		{
		File basedir=new File(root);
		
		Log.printLog("Converting to OST2");
		for(File channel:basedir.listFiles())
			if(!channel.getName().endsWith("data") && channel.isDirectory())
				{
				String channelName=channel.getName();
				channelName=channelName.substring(channelName.lastIndexOf('-')+1);
				
				//Rename all frames
				for(File frame:channel.listFiles())
					if(frame.isDirectory())
						{
						File newFrame=new File(channel, frame.getName().substring(frame.getName().lastIndexOf('-')+1));
						if(!frame.renameTo(newFrame))
							{
							Log.printError("Failed to rename "+frame.getAbsolutePath()+" to "+newFrame.getAbsolutePath(),null);
							return;
							}
						}
				
				//For all frames
				for(File frame:channel.listFiles())
					if(frame.isDirectory())
						{
						//Rename slices
						for(File slice:frame.listFiles())
							{
							if(slice.getName().startsWith(channelName))
								{
								String slicename=slice.getName();
								File newSlice=new File(frame, slicename.substring(slicename.lastIndexOf('-')+1));
								if(!slice.renameTo(newSlice))
									Log.printError("Failed to rename "+slice.getAbsolutePath()+" to "+newSlice.getAbsolutePath(),null);
								}
							}
						}
				/*
					else if(frame.getName().equals("rmd.txt"))
						{
						frame.delete();
						}
					*/		
				}
		Log.printLog("Conversion done");
		}
	
	/**
	 * Read corresponding meta data for this channel.
	 * Only meant for loading OST1.
	 */
	public static void readVWBMeta(ImagesetMeta meta, ImagesetMeta.Channel ch, String metaFilename)
		{
		Vector<String> framemeta=new Vector<String>();
		int framecol=0;
		String chOther="";
		
		ParamParse p=new ParamParse(new File(metaFilename));
		while(p.hasData())
			{
			Vector<String> arg=p.nextData();
			if(arg.size()!=0)
				{
				try
					{
					if(arg.get(0).equals("binning"))
						ch.chBinning=Integer.parseInt(arg.get(1));
					else if(arg.get(0).equals("resx"))
						meta.resX=Double.parseDouble(arg.get(1));
					else if(arg.get(0).equals("resy"))
						meta.resY=Double.parseDouble(arg.get(1));
					else if(arg.get(0).equals("resz"))
						meta.resZ=Double.parseDouble(arg.get(1));
				
					else if(arg.get(0).equals("NA"))
						meta.metaNA=Double.parseDouble(arg.get(1));
					else if(arg.get(0).equals("objective"))
						meta.metaObjective=Double.parseDouble(arg.get(1));
					else if(arg.get(0).equals("optivar"))
						meta.metaOptivar=Double.parseDouble(arg.get(1));
					else if(arg.get(0).equals("campix"))
						meta.metaCampix=Double.parseDouble(arg.get(1));
					else if(arg.get(0).equals("slicespacing"))
						meta.metaSlicespacing=Double.parseDouble(arg.get(1));
					else if(arg.get(0).equals("timestep"))
						meta.metaTimestep=Double.parseDouble(arg.get(1));
					else if(arg.get(0).equals("sample"))
						meta.metaSample=arg.get(1);
					else if(arg.get(0).equals("descript"))
						meta.metaDescript=arg.get(1);
					else if(arg.get(0).equals("dispx"))
						ch.dispX=Double.parseDouble(arg.get(1));
					else if(arg.get(0).equals("dispy"))
						ch.dispY=Double.parseDouble(arg.get(1));					
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
						}
					else
						{
						chOther+=arg.get(0);
						for(int i=1;i<arg.size();i++)
							chOther+=" \""+arg.get(i)+"\"";
						chOther+=";\n";
						}
					}
				catch(Exception e)
					{
					Log.printError("Parse error in tag. Skipping "+arg.get(0), e);
					}
				}
			}
		meta.getChannel(ch.name).metaOther.put("evother",chOther);
		}
	}

