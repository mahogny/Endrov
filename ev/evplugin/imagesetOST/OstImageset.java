package evplugin.imagesetOST;

//note: renaming channel will require all EvImageOST to be renamed as well

import javax.swing.*;

import java.io.*;
import java.util.*;

import evplugin.data.*;
import evplugin.ev.*;
import evplugin.imageset.*;
import evplugin.script.*;


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
		Script.addCommand("loadost", new CmdLoadOST());
		
		supportFileFormats.add(new EvDataSupport(){
			public Integer supports(File file)
				{
				if(file.isDirectory())
					for(File f:file.listFiles())
						if(f.isFile() && f.getName().equals("rmd.xml"))
							return 10;
				return null;
				}
			public EvData load(File file) throws Exception
				{
				return new OstImageset(file.getAbsolutePath());
				}
		});
		
/*		
		EvDataMenu.extensions.add(new DataMenuExtension()
			{
			public void buildOpen(JMenu menu)
				{
				final JMenuItem miLoadVWBImageset=new JMenuItem("Load OST imageset");
				addMetamenu(menu, miLoadVWBImageset);
				final JMenuItem miLoadVWBImagesetPath=new JMenuItem("Load OST imageset by path");
				addMetamenu(menu, miLoadVWBImagesetPath);

				
				ActionListener listener=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						if(e.getSource()==miLoadVWBImageset)
							{
							JFileChooser chooser = new JFileChooser();
					    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				    	chooser.setCurrentDirectory(new File(EvData.getLastDataPath()));
					    int returnVal = chooser.showOpenDialog(null); //null=window
					    if(returnVal == JFileChooser.APPROVE_OPTION)
					    	{
					    	String filename=chooser.getSelectedFile().getAbsolutePath();
					    	EvData.setLastDataPath(chooser.getSelectedFile().getParent());
					    	load(filename);
					    	}
							}
						else if(e.getSource()==miLoadVWBImagesetPath)
							{
							String clipboardString=null;
							try
								{
								clipboardString=(String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
								}
							catch(Exception e2)
								{
								System.out.println("Failed to get text from clipboard");
								}
							if(clipboardString==null)
								clipboardString="";
							String fileName=JOptionPane.showInputDialog("Path",clipboardString);
							if(fileName!=null)
								{
								File thefile=new File(fileName);
								if(thefile.exists())
									load(fileName);
								else
									JOptionPane.showMessageDialog(null, "Path does not exist");
								}
							}
						}

					public void load(String filename)
						{
						//Show loading dialog
			    	//dialog doesn't really show, but better than nothing
			    	JFrame loadingWindow=new JFrame(EV.programName); 
			    	loadingWindow.setLayout(new GridLayout(1,1));
			    	loadingWindow.add(new JLabel("Loading imageset"));
			    	loadingWindow.pack();
			    	loadingWindow.setBounds(200, 200, 300, 50);
			    	loadingWindow.setVisible(true);
			    	loadingWindow.repaint();
			
			    	//Load imageset and add to list
			    	try {EvData.metadata.add(new OstImageset(filename));}
						catch (Exception e){}
						
						//Close down dialog, update windows
			    	BasicWindow.updateWindows();
			    	loadingWindow.dispose();
						}
					
					};
				miLoadVWBImageset.addActionListener(listener);
				miLoadVWBImagesetPath.addActionListener(listener);
				}
			public void buildSave(JMenu menu, EvData meta)
				{
				}
			});
			*/
		
		}

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	/** List of images that existed when it was loaded. This will be used to save the image as some channels and files need be deleted */
	public HashMap<String,ChannelImages> ostLoadedImages=new HashMap<String,ChannelImages>();

	
	/** Path to imageset */
	public String basedir;
	
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
		saveImages();
		
		//Update date of datadir to have it backuped
		touchRecursive(datadir(), System.currentTimeMillis());
		
		setMetadataModified(false);
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
					HashSet<Integer> removedFrames=new HashSet<Integer>(oldCh.imageLoader.keySet());
					removedFrames.removeAll(newCh.imageLoader.keySet());
					for(Integer frame:removedFrames)
						{
						System.out.println("rf: "+frame);
						deleteOk=deleteRecursive(buildFramePath(channelName, frame),deleteOk);
						}
					
					//New frames: create directories
					HashSet<Integer> newFrames=new HashSet<Integer>(newCh.imageLoader.keySet());
					newFrames.removeAll(oldCh.imageLoader.keySet());
					for(Integer frame:newFrames)
						{
						System.out.println("cf: "+frame);
						buildFramePath(channelName, frame).mkdir();
						}
					}
				else
					{
					//All frames are new: create directories
					for(Integer frame:newCh.imageLoader.keySet())
						{
						System.out.println("cf: "+frame);
						buildFramePath(channelName, frame).mkdir();
						}
					}
				
				//Go through frames
				for(int frame:newCh.imageLoader.keySet())
					{
					TreeMap<Integer, EvImage> newSlices=newCh.imageLoader.get(frame);
					TreeMap<Integer, EvImage> oldSlices=null;
					if(oldCh!=null)
						oldSlices=oldCh.imageLoader.get(frame);
					
					//Removed slices: delete files
					if(oldSlices!=null)
						{
						HashSet<Integer> removedImages=new HashSet<Integer>(oldSlices.keySet());
						removedImages.removeAll(newSlices.keySet());
						for(int z:removedImages)
							{
							System.out.println("rz: "+z);
							Channel.EvImageOST im=(Channel.EvImageOST)oldSlices.get(z);
							File zdir=new File(im.jaiFileName());
							deleteOk=deleteRecursive(zdir,deleteOk);
							}
						}
					
					//Go through slices
					for(int z:newSlices.keySet())
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
		File basepath=new File(basedir);
		File metaFile=new File(basepath,"rmd.xml");
		if(!metaFile.exists())
			System.out.printf("AAIEEE NO METAFILE?? this might mean this is in the OST1 format which has been removed");
		imageset=basepath.getName();
		if(basepath.exists())
			{
			//Load metadata
			loadXmlMetadata(metaFile.getPath());
			for(int oi:metaObject.keySet())
				if(metaObject.get(oi) instanceof ImagesetMeta)
					{
					meta=(ImagesetMeta)metaObject.get(oi);
					metaObject.remove(oi);
					break;
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
						Channel c=new Channel(meta.getCreateChannelMeta(channelName));
						c.scanFiles();
						channelImages.put(channelName,c);
						}
				saveDatabaseCache();
				}
			}
		else
			Log.printError("Error: Imageset base directory does not exist",null);
		replicateLoadedFiles();
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
			for(int frame:oldCh.imageLoader.keySet())
				{
				TreeMap<Integer, EvImage> oldFrames=oldCh.imageLoader.get(frame);
				TreeMap<Integer, EvImage> newFrames=new TreeMap<Integer, EvImage>();
				newCh.imageLoader.put(frame, newFrames);
				for(int z:oldFrames.keySet())
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
	
	
	
	/**
	 * Load database from cache. Return if it succeeded
	 */
	public boolean loadDatabaseCache()
		{
//		boolean first=true;
		
		
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
				
				long currentTime=System.currentTimeMillis();
				
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
						int frame=Integer.parseInt(in.readLine());
						int numSlice=Integer.parseInt(in.readLine());
						TreeMap<Integer,EvImage> loaderset=c.imageLoader.get(frame);
						if(loaderset==null)
							{
							//A sorted linked list would make set generation linear time
							loaderset=new TreeMap<Integer,EvImage>();
							c.imageLoader.put(frame, loaderset);
							}
						
						
						//Generate name of frame directory, optimized. windows support?
//						String framedirName=buildFramePath(channelName, frame).getAbsolutePath()+File.pathSeparatorChar;
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
							int slice=Integer.parseInt(s);

							//Generate name of image file, optimized
							StringBuffer imagefilename=new StringBuffer(framedirName);
							EV.pad(slice, 8, imagefilename);
							imagefilename.append(ext);
							
							
							//loaderset.put(slice, ((Channel)c).newEvImage(buildImagePath(parentchannelName, frame, slice, ext).getAbsolutePath()));
							//String imagefilename=framedirName+EV.pad(slice, 8)+ext;
							
							EvImage evim=((Channel)c).newEvImage(imagefilename.toString());
							
							loaderset.put(slice, evim);
							
							}
						}
					}
				
				System.out.println(" timeC "+(System.currentTimeMillis()-currentTime));
				
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
		return new File(basedir,imageset+"-"+channelName);
		}
	/** Internal: piece together a path to a frame */
	public File buildFramePath(String channelName, int frame)
		{
		return new File(buildChannelPath(channelName), EV.pad(frame,8));
		}
	/** Internal: piece together a path to an image */
	public File buildImagePath(String channelName, int frame, int slice, String ext)
		{
		return buildImagePath(buildFramePath(channelName, frame), slice, ext);
		}
	/** Internal: piece together a path to an image */
	public File buildImagePath(File parent, int slice, String ext)
		{
//		File.pathSeparatorChar
//		EV.pad(slice, 8)+ext
		return new File(parent,EV.pad(slice, 8)+ext);
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
						File imagefile=new File(((EvImageJAI)loader).jaiFileName());
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
		public void scanFiles()
			{
			imageLoader.clear();
			
			File chandir=buildChannelPath(getMeta().name);
			File[] framedirs=chandir.listFiles();
			for(File framedir:framedirs)
				if(framedir.isDirectory() && !framedir.getName().startsWith("."))
					{
					int framenum=Integer.parseInt(framedir.getName());
					
					TreeMap<Integer,EvImage> loaderset=new TreeMap<Integer,EvImage>();
					File[] slicefiles=framedir.listFiles();
					for(File f:slicefiles)
						{
						String partname=f.getName();
						if(!partname.startsWith("."))
							{
							partname=partname.substring(0,partname.lastIndexOf('.'));
							try
								{
								int slicenum=Integer.parseInt(partname);
								loaderset.put(slicenum, newEvImage(f.getAbsolutePath()));
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

		protected EvImage internalMakeLoader(int frame, int z)
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
		return new RecentReference(getMetadataName(), basedir);
		}
	}

