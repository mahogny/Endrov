package endrov.imagesetOST;

//note: renaming channel will require all EvImageOST to be renamed as well

import javax.swing.*;

import java.io.*;
import java.util.*;

import endrov.data.*;
import endrov.ev.*;
import endrov.imageset.*;
import endrov.util.EvDecimal;


/**
 * Support for the native OST file format
 * @author Johan Henriksson
 */
public class OstImageset extends Imageset
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	public static void initPlugin() {}
	static
		{
		supportFileFormats.add(new EvDataSupport(){
			public Integer supports(String fileS)
				{
				File file=new File(fileS);
				if(file.isDirectory())
					{
					if(file.getName().endsWith(".ost")) //OST3+
						return 10;
					else //OST2
						for(File f:file.listFiles())
							if(f.isFile() && (f.getName().equals("rmd.xml") || f.getName().equals("rmd.ostxml")))
								return 10;
					}
				return null;
				}
			public EvData load(String file) throws Exception
				{
				return new OstImageset(new File(file));
				}
		});
		
		}

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	/** List of images that existed when it was loaded. This will be used to save the image as some channels and files need be deleted */
	public HashMap<String,ChannelImages> ostLoadedImages=new HashMap<String,ChannelImages>();

	
	/** Path to imageset */
	public File basedir;

	/**
	 * Create a new recording. Basedir points to imageset- ie without the channel name
	 * @param basedir
	 */
	public OstImageset(File basedir)
		{
		this.basedir=basedir;
		convert23();
		//convert33prime(); //should disappear ASAP, unsafe
		buildDatabase();
		}
	
	/**
	 * Convert OST2 -> OST3
	 */
	public void convert23()
		{
		String ostname=basedir.getName();
		if(ostname.endsWith(".ost"))
			ostname=ostname.substring(0,ostname.length()-".ost".length());
		File rmdfile=new File(basedir,"rmd.xml");
		if(rmdfile.exists())
			{
			System.out.println("Detected OST2.x. Updating to 3");
			
			//Rename rmd
			rmdfile.renameTo(new File(basedir,"rmd.ostxml"));
			
			//Rename channels
			for(File child:basedir.listFiles())
				{
				String n=child.getName();
				if(child.isDirectory() && n.startsWith(ostname))
					{
					n=n.substring((ostname+"-").length());
					File newname;
					if(n.equals("data"))
						newname=new File(basedir,"data");
					else
						newname=new File(basedir,"ch-"+n);
					child.renameTo(newname);
					}
				}
			
			//Add .ost to directory
			//Fails because it is normally locked
			/*
			File newbasedir=new File(new File(basedir).getParentFile(),ostname+".ost");
			new File(basedir).renameTo(newbasedir);
			basedir=newbasedir.toString();
			*/
			}
		
		
		}
	
	/**
	 * Convert 3 -> 3.1
	 * Timestep and resZ deleted, major file renaming
	 */
	public void convert33d1()
		{
		double curv=Double.parseDouble(metadataVersion);
		if(curv<3.1)
			{
			System.out.println("Updating files 3->3.1");
			
			//For all channels
			for(File fchan:basedir.listFiles())
				if(fchan.isDirectory() && fchan.getName().startsWith("ch-"))
					{
					
					//For all frames, prepend "n"
					for(File fframe:fchan.listFiles())
						if(fframe.isDirectory() && !fframe.getName().startsWith("."))
							{
							File newframe=new File(fchan,"n"+fframe.getName());
							//System.out.println("rename "+fframe+" "+newframe);
							fframe.renameTo(newframe);
							}
					//For all frames, remove "n" and multiply
					for(File fframe:fchan.listFiles())
						if(fframe.isDirectory() && fframe.getName().startsWith("n"))
							{
							String s=fframe.getName();
							EvDecimal cur=new EvDecimal(s.substring(1));
							File newframe=new File(fchan, EV.pad(cur.multiply(meta.metaTimestep), 8));
							//System.out.println("rename "+fframe+" "+newframe);
							fframe.renameTo(newframe);
							}
					
					//For all frames
					for(File fframe:fchan.listFiles())
						if(fframe.isDirectory() && !fframe.getName().startsWith("."))
							{
							//For all slices, prepend "n"
							for(File fslice:fframe.listFiles())
								if(fslice.isFile() && !fslice.getName().startsWith("."))
									{
									File newslice=new File(fframe,"n"+fslice.getName());
									//System.out.println("renameS1 "+fslice+" "+newslice);
									fslice.renameTo(newslice);
									}
							//For all slices, remove "n" and divide
							for(File fslice:fframe.listFiles())
								if(fslice.isFile() && fslice.getName().startsWith("n"))
									{
									String s=fslice.getName().substring(1);
									String ext=s.substring(s.lastIndexOf("."));
									s=s.substring(0,s.lastIndexOf("."));
									File newslice=new File(fframe,EV.pad(new EvDecimal(s).divide(meta.resZ),8)+ext);
									//System.out.println("renameS2 "+fslice+" "+newslice);
									fslice.renameTo(newslice);
									}
							
							
							
							}
					
					
					
					
					
					}
			
			
			System.out.println("Saving meta 3.1");
			saveMeta();
			invalidateDatabaseCache();
			System.out.println("Reloading file listing");
			scanFiles();
			}
		
		
		}
	
	public void convert33prime()
		{
		//Rename channels
		for(File child:basedir.listFiles())
			{
			String n=child.getName();
			if(child.isDirectory() && !n.startsWith(".") && !n.startsWith("ch-") && !n.equals("data"))
				{
				File newname=new File(basedir,"ch-"+n);
				System.out.println(""+n+" to "+newname);
				child.renameTo(newname);
				}
			}
		}

	
	/**
	 * Get name description of this metadata
	 */
	public String toString()
		{
		return getMetadataName();
		}

	
	/**
	 * Get directory for this imageset where any datafiles can be stored
	 */
	public File datadir()
		{
//		File datadir=new File(basedir,getMetadataName()+"-data");
		File datadir=new File(basedir,"data");
		datadir.mkdirs();
		return datadir;
		}

	
	/**
	 * Save meta for all channels into RMD-file
	 */
	public void saveMeta()
		{
		try
			{
			saveMeta(new File(basedir,"rmd.ostxml"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
//		saveMeta(new File(basedir,"rmd.xml"));
		saveImages();
		
		//Update date of datadir to have it backuped
		touchRecursive(datadir(), System.currentTimeMillis());
		
		setMetadataModified(false);
		}
	
	public static void touchRecursive(File f, long timestamp)
		{
		f.setLastModified(timestamp);
		File parent=f.getParentFile();
		if(parent!=null)
			touchRecursive(parent,timestamp);
		}
	
	
	/**
	 * Save images in this imageset
	 *
	 */
	private void saveImages()
		{
		boolean deleteOk=false;
		try
			{
			//NOTE: keyset for the maps is linked internally. This means this set should NOT directly be messed with but we make a copy.

			//Removed channels: Delete those directories.
			HashSet<String> removedChanNames=new HashSet<String>(ostLoadedImages.keySet());
			removedChanNames.removeAll(channelImages.keySet());
			for(String s:removedChanNames)
				{
				System.out.println("rc: "+s);
				deleteOk=deleteRecursive(buildChannelPath(s),deleteOk);
				}
			
			//New channels: Create directories
			HashSet<String> newChanNames=new HashSet<String>(channelImages.keySet());
			newChanNames.remove(ostLoadedImages.keySet());
			for(String s:newChanNames)
				{
				System.out.println("nc: "+s);
				buildChannelPath(s).mkdirs();
				}

			//Go through all channels
			for(String channelName:channelImages.keySet())
				{
				Channel newCh=(Channel)getChannel(channelName);
				Channel oldCh=(Channel)ostLoadedImages.get(channelName);

				if(oldCh!=null)
					{
					//Removed frames: delete directories
					HashSet<EvDecimal> removedFrames=new HashSet<EvDecimal>(oldCh.imageLoader.keySet());
					removedFrames.removeAll(newCh.imageLoader.keySet());
					for(EvDecimal frame:removedFrames)
						{
						System.out.println("rf: "+frame);
						deleteOk=deleteRecursive(buildFramePath(channelName, frame),deleteOk);
						}
					
					//New frames: create directories
					HashSet<EvDecimal> newFrames=new HashSet<EvDecimal>(newCh.imageLoader.keySet());
					newFrames.removeAll(oldCh.imageLoader.keySet());
					for(EvDecimal frame:newFrames)
						{
						System.out.println("cf: "+frame);
						buildFramePath(channelName, frame).mkdir();
						}
					}
				else
					{
					//All frames are new: create directories
					for(EvDecimal frame:newCh.imageLoader.keySet())
						{
						System.out.println("cf: "+frame);
						buildFramePath(channelName, frame).mkdir();
						}
					}
				
				//Go through frames
				for(EvDecimal frame:newCh.imageLoader.keySet())
					{
					TreeMap<EvDecimal, EvImage> newSlices=newCh.imageLoader.get(frame);
					TreeMap<EvDecimal, EvImage> oldSlices=null;
					if(oldCh!=null)
						oldSlices=oldCh.imageLoader.get(frame);
					
					//Removed slices: delete files
					if(oldSlices!=null)
						{
						HashSet<EvDecimal> removedImages=new HashSet<EvDecimal>(oldSlices.keySet());
						removedImages.removeAll(newSlices.keySet());
						for(EvDecimal z:removedImages)
							{
							System.out.println("rz: "+z);
							Channel.EvImageOST im=(Channel.EvImageOST)oldSlices.get(z);
							File zdir=new File(im.jaiFileName());
							deleteOk=deleteRecursive(zdir,deleteOk);
							}
						}
					
					//Go through slices
					for(EvDecimal z:newSlices.keySet())
						{
						Channel.EvImageOST newIm=(Channel.EvImageOST)newSlices.get(z);
						if(newIm.modified())
							{
							//Delete old image - it might have a different file extension
							if(oldSlices!=null)
								{
								Channel.EvImageOST oldIm=(Channel.EvImageOST)oldSlices.get(z);
								if(oldIm!=null)
									{
									deleteOk=dialogDelete(deleteOk);
									if(deleteOk)
										(new File(oldIm.jaiFileName())).delete();
									}
								}
							//Save new image
							newIm.saveImage();
							}
						}
					}
				}
			
			
			//Remember new state
			replicateLoadedFiles();
			saveDatabaseCache();
			}
		catch (Exception e)
			{
			Log.printError("Error saving OST", e);
			}
		}


	public static boolean dialogDelete(boolean ok)
		{
		if(!ok)
			ok=JOptionPane.showConfirmDialog(null, "OST needs deletion. Do you really want to proceed? (keep a backup ready)","EV",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
		return ok;
		}
	
	/**
	 * Delete recursively. 
	 */
	public static boolean deleteRecursive(File f, boolean ok) throws IOException
		{
		ok=dialogDelete(ok);
		if(ok)
			{
			if(f.isDirectory())
				for(File c:f.listFiles())
					deleteRecursive(c, ok);
			f.delete();
			}
		return ok;	
		}
	
	/**
	 * Scan recording for channels and build a file database
	 */
	public void buildDatabase()
		{
		File metaFile=new File(basedir,"rmd.ostxml");
//		File metaFile=new File(basepath,"rmd.xml");
		if(!metaFile.exists())
			System.out.printf("AAIEEE NO METAFILE?? this might mean this is in the OST1 format which has been removed");

		//Get descriptive name of imageset
		imageset=basedir.getName();
		if(imageset.endsWith(".ost"))
			imageset=imageset.substring(0,imageset.length()-".ost".length());
		
		if(basedir.exists())
			{
			//Load metadata
			try
				{
				loadImagesetXmlMetadata(new FileInputStream(metaFile));
				convert33d1();
				}
			catch (FileNotFoundException e)
				{
				e.printStackTrace();
				}
			/*loadXmlMetadata(metaFile.getPath());
			for(String oi:metaObject.keySet())
				if(metaObject.get(oi) instanceof ImagesetMeta)
					{
					meta=(ImagesetMeta)metaObject.get(oi);
					metaObject.remove(oi);
					break;
					}*/

			if(!loadDatabaseCache())
				{
				scanFiles();
				}
			}
		else
			Log.printError("Error: Imageset base directory does not exist",null);
		replicateLoadedFiles();
		}
	
	private void scanFiles()
		{
		//Check which files exist
		channelImages.clear();
		File[] dirfiles=basedir.listFiles();
		for(File f:dirfiles)
			if(f.isDirectory() && f.getName().startsWith("ch-"))//!f.getName().startsWith(".") && !f.getName().equals("data"))
				{
				String fname=f.getName();
				String channelName=fname.substring("ch-".length());
//				String channelName=fname.substring(fname.lastIndexOf('-')+1);
				Log.printLog("Found channel: "+channelName);
				Channel c=new Channel(meta.getCreateChannelMeta(channelName));
				c.scanFiles(this);
				channelImages.put(channelName,c);
				}
		saveDatabaseCache();
		}
	
	/**
	 * Make a copy of current list of loaders to ostLoadedImages. Meta is set to null in this copy.
	 */
	private void replicateLoadedFiles()
		{
		ostLoadedImages.clear();
		for(String channelName:channelImages.keySet())
			{
			Imageset.ChannelImages oldCh=getChannel(channelName);
			Imageset.ChannelImages newCh=new Channel(null);
			ostLoadedImages.put(channelName, newCh);
			for(EvDecimal frame:oldCh.imageLoader.keySet())
				{
				TreeMap<EvDecimal, EvImage> oldFrames=oldCh.imageLoader.get(frame);
				TreeMap<EvDecimal, EvImage> newFrames=new TreeMap<EvDecimal, EvImage>();
				newCh.imageLoader.put(frame, newFrames);
				for(EvDecimal z:oldFrames.keySet())
					{
					Channel.EvImageOST oldIm=(Channel.EvImageOST)oldFrames.get(z);
					Channel.EvImageOST newIm=((Channel)newCh).newEvImage(oldIm.jaiFileName());
					newFrames.put(z, newIm);
					}
				}
			}
		}
	
	
	/**
	 * Get the name of the database cache file
	 */
	private File getDatabaseCacheFile()
		{
		return new File(basedir,"imagecache.txt");
		}
	

	
	//would be nice to factor out loaddatabasecache
	
	/**
	 * Load database from cache. Return if it succeeded
	 */
	public boolean loadDatabaseCache()
		{
		try
			{
			String ext="";
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(getDatabaseCacheFile()),"UTF-8"));
			
			String line=in.readLine();
			if(!line.equals("version1")) //version1
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
						c=new Channel(meta.getCreateChannelMeta(channelName));
						channelImages.put(channelName,c);
						}
					
					String channeldirName=buildChannelPath(channelName).getAbsolutePath();
					
					for(int j=0;j<numFrame;j++)
						{
						EvDecimal frame=new EvDecimal(in.readLine());
						//EvDecimal realFrame=frame.multiply(meta.metaTimestep);
						
						int numSlice=Integer.parseInt(in.readLine());
//						TreeMap<EvDecimal,EvImage> loaderset=c.imageLoader.get(realFrame);
						TreeMap<EvDecimal,EvImage> loaderset=c.imageLoader.get(frame);
						if(loaderset==null)
							{
							//A sorted linked list would make set generation linear time
							loaderset=new TreeMap<EvDecimal,EvImage>();
							c.imageLoader.put(frame, loaderset);
//							c.imageLoader.put(realFrame, loaderset);
							}
						
						
						//Generate name of frame directory, optimized. windows support?
						StringBuffer framedirName=new StringBuffer(channeldirName);
						framedirName.append('/');
						EV.pad(frame, 8, framedirName);  
						framedirName.append('/');
						
						
						for(int k=0;k<numSlice;k++)
							{
							String s=in.readLine();
							if(s.startsWith("ext"))
								{
								ext=s.substring(3);
								s=in.readLine();
								}
							EvDecimal slice=new EvDecimal(s);
//							EvDecimal realSlice=new EvDecimal(in.readLine());

							//Generate name of image file, optimized
							StringBuffer imagefilename=new StringBuffer(framedirName);
							EV.pad(slice, 8, imagefilename); 
							imagefilename.append(ext);
							
							EvImage evim=((Channel)c).newEvImage(imagefilename.toString());
							loaderset.put(slice, evim); //TODO TODO  ost3 hack
//							loaderset.put(realSlice, evim); //TODO TODO  ost3 hack
							
							//System.out.println("## "+slice+"\t"+realSlice);
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
	


	protected ChannelImages internalMakeChannel(ImagesetMeta.Channel ch)
		{
		return new Channel(ch);
		}
		
	
	

	
	/** Internal: piece together a path to a channel */
	private File buildChannelPath(String channelName)
		{
//		return new File(basedir,imageset+"-"+channelName);
		return new File(basedir,"ch-"+channelName);
		}
	/** Internal: piece together a path to a frame */
	public File buildFramePath(String channelName, EvDecimal frame)
		{
		return new File(buildChannelPath(channelName), EV.pad(frame.intValue(),8)); //TODO bd wrong?
		}
	/** Internal: piece together a path to an image */
	public File buildImagePath(String channelName, EvDecimal frame, EvDecimal slice, String ext)
		{
		return buildImagePath(buildFramePath(channelName, frame), slice, ext);
		}
	/** Internal: piece together a path to an image */
	public File buildImagePath(File parent, EvDecimal slice, String ext)
		{
//		File.pathSeparatorChar
//		EV.pad(slice, 8)+ext
		return new File(parent,EV.pad(slice.intValue(), 8)+ext); //TODO bd wrong?
		}
	
	
	
	/**
	 * Invalidate database cache (=deletes cache file)
	 */
	public void invalidateDatabaseCache()
		{
		getDatabaseCacheFile().delete();
		}
	
	
	

	
	
	/**
	 * Save database as a cache file
	 */
	public void saveDatabaseCache()
		{
		try
			{
			String lastExt="";
			BufferedWriter w=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getDatabaseCacheFile()),"UTF-8"));
			
			//w.write("version2\n");
			w.write("version1\n");

			w.write(channelImages.size()+"\n");
			for(ChannelImages c:channelImages.values())
				{
				w.write(c.getMeta().name+"\n");
				w.write(""+c.imageLoader.size()+"\n");
				for(EvDecimal frame:c.imageLoader.keySet())
					{
					//EvDecimal diskFrame=frame.divide(meta.metaTimestep);
					//w.write(""+diskFrame+"\n");
					w.write(""+frame+"\n");
					w.write(""+c.imageLoader.get(frame).size()+"\n");
					for(EvDecimal realSlice:c.imageLoader.get(frame).keySet())
						{
						EvImageJAI loader=(EvImageJAI)c.getImageLoader(frame, realSlice);
						File imagefile=new File((loader).jaiFileName());
						String filename=imagefile.getName();
						String ext="";
						if(filename.lastIndexOf('.')!=-1)
							ext=filename.substring(filename.lastIndexOf('.')); //TODO enforce file ending in spec
						//String slice=filename.substring(0,filename.length()-ext.length());   //TODO ost3 hack
						
						if(!ext.equals(lastExt))
							{
							w.write("ext"+ext+"\n");
							lastExt=ext;
							}
						
						//w.write(""+slice+"\n");
						w.write(""+realSlice+"\n");
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
	

	
	
	
	
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////
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
		
	
		
		/**
		 * Scan all files for this channel and build a database
		 */
		public void scanFiles(OstImageset ost)
			{
			imageLoader.clear();
			
			File chandir=buildChannelPath(getMeta().name);
			File[] framedirs=chandir.listFiles();
			for(File framedir:framedirs)
				if(framedir.isDirectory() && !framedir.getName().startsWith("."))
					{
					EvDecimal framenum=new EvDecimal(framedir.getName());

					//EvDecimal realFramenum=framenum.multiply(ost.meta.metaTimestep);
					
					TreeMap<EvDecimal,EvImage> loaderset=new TreeMap<EvDecimal,EvImage>();
					imageLoader.put(framenum, loaderset);
//					imageLoader.put(realFramenum, loaderset);
					File[] slicefiles=framedir.listFiles();
					for(File f:slicefiles)
						{
						String partname=f.getName();
						if(!partname.startsWith("."))
							{
							partname=partname.substring(0,partname.lastIndexOf('.'));
							try
								{
								//EvDecimal realSlicenum=new EvDecimal(partname).divide(ost.meta.resZ); //TODO TODO OST3-hack
								//loaderset.put(realSlicenum, newEvImage(f.getAbsolutePath()));

								EvDecimal slice=new EvDecimal(partname);
								//EvDecimal slice=new EvDecimal(partname).divide(ost.meta.resZ);
								loaderset.put(slice, newEvImage(f.getAbsolutePath()));
								}
							catch (NumberFormatException e)
								{
								Log.printError("partname: "+partname+" filename "+f.getName()+" framenum "+framenum,e);
								System.exit(1);
								}
							}
						}
					}
			}

		protected EvImage internalMakeLoader(EvDecimal frame, EvDecimal z)
			{
			return newEvImage(buildImagePath(getMeta().name, frame, z, ".png").getAbsolutePath()); //png?
			}
		
		
		public EvImageOST newEvImage(String filename)
			{
			return new EvImageOST(filename);
			}
		
		
		private class EvImageOST extends EvImageJAI
			{
			public EvImageOST(String filename){super(filename);}

			public int getBinning(){return getMeta().chBinning;}
			public double getDispX(){return getMeta().dispX;}
			public double getDispY(){return getMeta().dispY;}
			public double getResX(){return meta.resX;}
			public double getResY(){return meta.resY;}
			
			/*
			public void finalize()
				{
				System.out.println("Removing ostimage ");
				}
				*/
			}
		}
	
	
	public void finalize()
		{
		System.out.println("finalize ost");
		}
	
	
	public RecentReference getRecentEntry()
		{
		return new RecentReference(getMetadataName(), basedir.getPath());
		}
	}

