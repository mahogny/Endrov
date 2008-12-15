package endrov.imagesetOST;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import endrov.data.*;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.imageset.*;
import endrov.imageset.Imageset.ChannelImages;
import endrov.util.EvDecimal;

public class EvIODataOST implements EvIOData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	

	
	private static class SliceIO implements EvIOImage
		{
		public File f;
		public EvIODataOST ost;
		public EvIOImage oldio;   //Strong pointer! make sure to delete it ASAP
		public SliceIO(EvIODataOST ost, File f)
			{
			this.ost=ost;
			this.f=f;
			}

		public BufferedImage loadJavaImage()
			{
			//This overloading is important during the save stage
			//with some recoding it would be possible to remove it
			if(oldio!=null)
				return oldio.loadJavaImage();
			else
				try
					{
					return ImageIO.read(f);
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
			return null;
			}
		
		public File outputFile(EvIODataOST ost, String channelName, EvDecimal frame, EvDecimal slice, String ext)
			{
			return ost.buildImagePath(channelName, frame, slice, ext);
			}
		
		public String fileFormat()
			{
			String n=f.getName();
			return n.substring(0,n.lastIndexOf("."));
			}

		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                               *
	 *****************************************************************************************************/

	
	/** 
	 * Scanned files. This list reflects what is on harddrive. 
	 * An entry is not removed unless it is also deleted. By comparing this list to the dirty image list,
	 * it is possible to figure out what files to delete. 
	 */
	public HashMap<String,HashMap<EvDecimal,HashMap<EvDecimal,File>>> imageLoader=new HashMap<String, HashMap<EvDecimal,HashMap<EvDecimal,File>>>();

	/** Path to imageset */
	public File basedir;

	
	//TODO rename
	public String getMetadataName()
		{
		String imageset=basedir.getName();
		if(imageset.endsWith(".ost"))
			imageset=imageset.substring(0,imageset.length()-".ost".length());
		return imageset;

		}
	
	
	/**
	 * Get entry for Load Recent or null if not possible
	 */
	public RecentReference getRecentEntry()
		{
		return new RecentReference(getMetadataName(), basedir.getPath());
		}

	/** 
	 * Directory for auxiliary data. null if one does not exist
	 */
	public File datadir()
		{
		File datadir=new File(basedir,"data");
		datadir.mkdirs();
		return datadir;
		}
	
	
	
	
	/**
	 * Create a new recording. Basedir points to imageset- ie without the channel name
	 */
	public EvIODataOST(EvData d, File basedir)
		{
		this.basedir=basedir;
		convert23();
		buildDatabase(d);
		}

	
	
	
	
	
	
	
	
	
	
	/**
	 * Scan all files for this channel and build a database
	 */
	public void scanFilesChannel(Imageset ost, String channelName)
		{
		//imageLoader.clear();
		//imageLoader.remove(channelName);
		
		HashMap<EvDecimal,HashMap<EvDecimal,File>> channelSet=new HashMap<EvDecimal, HashMap<EvDecimal,File>>();
		imageLoader.put(channelName, channelSet);
		
		
		File chandir=buildChannelPath(channelName);
		File[] framedirs=chandir.listFiles();
		for(File framedir:framedirs)
			if(framedir.isDirectory() && !framedir.getName().startsWith("."))
				{
				EvDecimal framenum=new EvDecimal(framedir.getName());

				//EvDecimal realFramenum=framenum.multiply(ost.meta.metaTimestep);
				
				HashMap<EvDecimal,File> loaderset=new HashMap<EvDecimal,File>();
				channelSet.put(framenum, loaderset);
//				imageLoader.put(realFramenum, loaderset);
				File[] slicefiles=framedir.listFiles();
				for(File f:slicefiles)
					{
					String partname=f.getName();
					if(!partname.startsWith("."))
						{
						partname=partname.substring(0,partname.lastIndexOf('.'));
						try
							{
							EvDecimal slice=new EvDecimal(partname);
							loaderset.put(slice, f);
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/******************************************************************************************************
	 *                               old                                                               *
	 *****************************************************************************************************/
	
	public static void initPlugin() {}
	static
		{
		EvData.supportFileFormats.add(new EvDataSupport(){
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
				EvData d=new EvData();
				
				d.io=new EvIODataOST(d, new File(file));
				
				return d;
				}
		});
		
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

			}
		
		
		}
	
	/**
	 * Convert 3 -> 3.1
	 * Timestep and resZ deleted, major file renaming
	 */
	public void convert33d1(EvData d, Imageset im)
		{
		double curv=Double.parseDouble(d.metadataVersion);
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
							File newframe=new File(fchan, EV.pad(cur.multiply(im.metaTimestep), 8));
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
									File newslice=new File(fframe,EV.pad(new EvDecimal(s).divide(im.resZ),8)+ext);
									//System.out.println("renameS2 "+fslice+" "+newslice);
									fslice.renameTo(newslice);
									}
							
							
							
							}
					
					
					
					
					
					}
			
			
			System.out.println("Saving meta 3.1");
			saveMeta(d);
			invalidateDatabaseCache();
			System.out.println("Reloading file listing");
			scanFiles(im);
			filesToImageset(im);

			}
		
		
		}
	

	
	/**
	 * Get name description of this metadata
	 */
	public String toString()
		{
		return getMetadataName();
		}

	

	private File getMetaFile()
		{
		return new File(basedir,"rmd.ostxml");
		}
	
	
	
	public void saveMeta(EvData d, OutputStream os) throws IOException
	  {
	  //Add all objects
	  Document document=d.saveXmlMetadata();
	
	  //Write out to disk
	  Format format=Format.getPrettyFormat();
	  XMLOutputter outputter = new XMLOutputter(format);
	  outputter.output(document, os);
	  d.setMetadataModified(false);
	  }
	public void saveMeta(EvData d, File outfile) throws IOException
	  {
	  FileOutputStream writer2=new FileOutputStream(outfile);
	  saveMeta(d,writer2);
	  writer2.close();
	  }

	
	
	
	
	/**
	 * Save meta for all channels into RMD-file
	 */
	public void saveMeta(EvData d)
		{
		try
			{
			saveMeta(d,getMetaFile());  //TODO, get from old Imageset
			d.setMetadataModified(false);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		Imageset im=getHackImageset(d);
		saveImages(im);
		
		//Update date of datadir to have it backuped
		//touchRecursive(datadir(), System.currentTimeMillis());
		
		}
		
	/*
	public static void touchRecursive(File f, long timestamp)
		{
		f.setLastModified(timestamp);
		File parent=f.getParentFile();
		if(parent!=null)
			touchRecursive(parent,timestamp);
		}*/
	
	
	private File getCurrentFileFor(String channelName, EvDecimal frame, EvDecimal slice)
		{
		HashMap<EvDecimal,HashMap<EvDecimal,File>> ce=imageLoader.get(channelName);
		if(ce!=null)
			{
			HashMap<EvDecimal,File> fe=ce.get(frame);
			if(fe!=null)
				return fe.get(slice);
			}
		return null;
		}
	
	private File fileShouldBe(Imageset im, String channel, EvDecimal frame, EvDecimal z)
		{
		//TODO
		return buildImagePath(channel, frame,z,".png");
		}
	
	
	/**
	 * Save images in this imageset
	 *
	 */
	private void saveImages(Imageset im)
		{
		boolean deleteOk=false;
		try
			{
			//This code looks complicated but solves the following problem:
			//if an image has been moved then it might have to be read into memory or it will be overwritten
			//before it is loaded. cannot always consider this case because it is uncommon and will *eat* memory.
			
			//Images to delete
			Set<File> toDelete=new HashSet<File>();
			//Images that need be read
			Map<File,HashSet<EvImage>> toRead=new HashMap<File, HashSet<EvImage>>();
			//Images to write
			LinkedList<EvImage> toWrite=new LinkedList<EvImage>();
			
			//Which files will be deleted because they are no longer in the imageset?
			for(Map.Entry<String,HashMap<EvDecimal,HashMap<EvDecimal,File>>> ce:imageLoader.entrySet())
				for(Map.Entry<EvDecimal, HashMap<EvDecimal,File>> fe:ce.getValue().entrySet())
					for(Map.Entry<EvDecimal, File> se:fe.getValue().entrySet())
						if(im.getImageLoader(ce.getKey(), fe.getKey(), se.getKey())==null)
							toDelete.add(se.getValue());
			
			//Which images are dirty and need be written?
			//Dirty image := an image that has been modified or has not been written to this OST
			for(Map.Entry<String, Imageset.ChannelImages> ce:im.channelImages.entrySet())
				for(Map.Entry<EvDecimal, TreeMap<EvDecimal,EvImage>> fe:ce.getValue().imageLoader.entrySet())
					for(Map.Entry<EvDecimal, EvImage> ie:fe.getValue().entrySet())
						{
						//Check if dirty
						boolean dirty=false;
						if(ie.getValue().modified())
							dirty=true;
						EvIOImage oldio=ie.getValue().io;
						if(!(oldio!=null && oldio instanceof SliceIO && ((SliceIO)oldio).ost==this))
							dirty=true;
						if(!dirty)
							{
							File file=getCurrentFileFor(ce.getKey(), fe.getKey(), ie.getKey());
							if(file!=null)
								{
								//File output=fileShouldBe(im,ce.getKey(), fe.getKey(),ie.getKey());
								//TODO
								//if(!output.equals(file))
								//	{
									//toDelete.add(file);
									//	dirty=true;
								//	}
								}
							else
								dirty=true;
							}
						
						//If dirty, prepare it to be written
						if(dirty)
							{
							EvImage evim=ie.getValue();
							
							//Mark all dirty. When they are not dirty they need not be written
							evim.isDirty=true; 

							//Mark current file for reading if it is not in memory
							File currentFile=getCurrentFileFor(ce.getKey(), fe.getKey(), ie.getKey());
							if(currentFile!=null && evim.getMemoryImage()!=null)
								{
								HashSet<EvImage> ims=toRead.get(currentFile);
								if(ims==null)
									toRead.put(currentFile, ims=new HashSet<EvImage>());
								ims.add(evim);
								}
							
							File newFile=currentFile;
							if(newFile==null) //TODO better format handling
								newFile=fileShouldBe(im,ce.getKey(), fe.getKey(),ie.getKey());
							
							SliceIO newio=new SliceIO(this,newFile);
							newio.oldio=oldio;
							
							toWrite.add(evim);
							
							}
						
					
						
						
						}
					
			
			//Write the files
			while(!toWrite.isEmpty())
				{
				EvImage evim=toWrite.getFirst();
				if(evim.isDirty)
					{
					//Even an overwrite is equivalent to optional read, then delete.
					//This is important if one slice is for example copied and the original modified.
	
					SliceIO io=(SliceIO)evim.io;

					//Have all images dependent on this file read it into memory
					//As an optimization, put these first in write queue to avoid reading
					//an entire channel into memory before writing it out
					HashSet<EvImage> needToRead=toRead.get(io.f);
					if(needToRead!=null)
						{
						toRead.remove(io.f);
						for(EvImage ci:needToRead)
							{
							ci.setMemoryImage(ci.getJavaImage());
							toWrite.addFirst(ci);
							}
						}

					//Write image to disk
					BufferedImage bim=evim.getJavaImage(); 
					ImageIO.write(bim, io.fileFormat(), io.f);
					
					
					//Mark this image as done
					evim.isDirty=false;
					}
				toWrite.removeFirst();
				}
			
			
			//Delete the files
			if(deleteOk)
				for(File f:toDelete)
					f.delete();
			
			
			
			//Remove empty channel directories
			//Remove empty frame directories
			//TODO
			
			
			//Clear up oldio
			for(Map.Entry<String, Imageset.ChannelImages> ce:im.channelImages.entrySet())
				for(Map.Entry<EvDecimal, TreeMap<EvDecimal,EvImage>> fe:ce.getValue().imageLoader.entrySet())
					for(Map.Entry<EvDecimal, EvImage> ie:fe.getValue().entrySet())
						((SliceIO)ie.getValue().io).oldio=null;
			
			//Update the fast-load cache
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
	public void buildDatabase(EvData d)
		{
		File metaFile=new File(basedir,"rmd.ostxml");
		if(!metaFile.exists())
			System.out.printf("AAIEEE NO METAFILE?? this might mean this is in the OST1 format which has been removed");

		if(basedir.exists())
			{
			
			//Load metadata
			try
				{
				d.metaObject.clear();
				d.loadXmlMetadata(new FileInputStream(metaFile));

				//This is a hack for now, im
				Imageset im=getHackImageset(d);

				if(!loadDatabaseCache())
					scanFiles(im);
				
				
				filesToImageset(im);

				convert33d1(d,im);
				}
			catch (FileNotFoundException e)
				{
				e.printStackTrace();
				}


			
			}
		else
			Log.printError("Error: Imageset base directory does not exist",null);
		}
	
	/**
	 * TODO remove need for this
	 */
	private Imageset getHackImageset(EvData d)
		{
		List<Imageset> ims=d.getObjects(Imageset.class);
		Imageset im=ims.get(0);
		return im;
		}
	

	/**
	 * For every loader, set up an entry in the metadata object
	 */
	private void filesToImageset(Imageset im)
		{
		im.channelImages.clear();
		for(Map.Entry<String,HashMap<EvDecimal,HashMap<EvDecimal,File>>> ce:imageLoader.entrySet())
			{
			ChannelImages chim=im.createChannel(ce.getKey());
			
			for(Map.Entry<EvDecimal, HashMap<EvDecimal,File>> fe:ce.getValue().entrySet())
				{
				TreeMap<EvDecimal, EvImage> stack=new TreeMap<EvDecimal, EvImage>();
				chim.imageLoader.put(fe.getKey(),stack);
				for(Map.Entry<EvDecimal, File> se:fe.getValue().entrySet())
					{
					EvImage evim=new EvImage();
					evim.io=new SliceIO(this,getCurrentFileFor(ce.getKey(), fe.getKey(), se.getKey()));
					
					

					//TODO for early version of OST, is it sustainable?
					evim.resX=im.resX;
					evim.resY=im.resY;
					evim.dispX=chim.getMeta().dispX;
					evim.dispY=chim.getMeta().dispY;
					evim.binning=chim.getMeta().chBinning;
					
					stack.put(se.getKey(),evim);
					}
				}
			}
		
		}
	
	
	private void scanFiles(Imageset im)
		{
		//Check which files exist
		imageLoader.clear();
		File[] dirfiles=basedir.listFiles();
		for(File f:dirfiles)
			if(f.isDirectory() && f.getName().startsWith("ch-"))//!f.getName().startsWith(".") && !f.getName().equals("data"))
				{
				String fname=f.getName();
				String channelName=fname.substring("ch-".length());
				Log.printLog("Found channel: "+channelName);
				scanFilesChannel(im, channelName);
				}
		saveDatabaseCache();
		}
	
	
	
	
	/**
	 * Get the name of the database cache file
	 */
	private File getDatabaseCacheFile()
		{
		return new File(basedir,"imagecache.txt");
		}
	

	/**
	 * Load database from cache into file-list. Return if it succeeded. Does not update imageset objects
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
				
				imageLoader.clear();
				int numChannels=Integer.parseInt(in.readLine());
				for(int i=0;i<numChannels;i++)
					{
					String channelName=in.readLine();
					int numFrame=Integer.parseInt(in.readLine());
					
					HashMap<EvDecimal,HashMap<EvDecimal,File>> c=new HashMap<EvDecimal, HashMap<EvDecimal,File>>();
					imageLoader.put(channelName, c);
					
					String channeldirName=buildChannelPath(channelName).getAbsolutePath();
					
					for(int j=0;j<numFrame;j++)
						{
						EvDecimal frame=new EvDecimal(in.readLine());
						int numSlice=Integer.parseInt(in.readLine());
						HashMap<EvDecimal,File> loaderset=new HashMap<EvDecimal, File>();
						c.put(frame,loaderset);
						
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

							//Generate name of image file, optimized
							StringBuffer imagefilename=new StringBuffer(framedirName);
							EV.pad(slice, 8, imagefilename); 
							imagefilename.append(ext);
							
							loaderset.put(slice,new File(imagefilename.toString()));
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
	

		
	
	

	
	/** Internal: piece together a path to a channel */
	private File buildChannelPath(String channelName)
		{
//		return new File(basedir,imageset+"-"+channelName);
		return new File(basedir,"ch-"+channelName);
		}
	/** Internal: piece together a path to a frame */
	public File buildFramePath(String channelName, EvDecimal frame)
		{
		return new File(buildChannelPath(channelName), EV.pad(frame,8));
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
			BufferedWriter w=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getDatabaseCacheFile()),"UTF-8"));
			
			//w.write("version2\n");
			w.write("version1\n");

			w.write(imageLoader.size()+"\n");
			
			for(Map.Entry<String, HashMap<EvDecimal,HashMap<EvDecimal,File>>> ce:imageLoader.entrySet())
				{
				w.write(ce.getKey()+"\n");
				w.write(""+ce.getValue().size()+"\n");
				for(EvDecimal frame:ce.getValue().keySet())
					{
					//EvDecimal diskFrame=frame.divide(meta.metaTimestep);
					//w.write(""+diskFrame+"\n");
					w.write(""+frame+"\n");
					w.write(""+ce.getValue().get(frame).size()+"\n");
					for(Map.Entry<EvDecimal, File> fe:ce.getValue().get(frame).entrySet())
						{
						File imagefile=fe.getValue();
						String filename=imagefile.getName();
						String ext="";
						if(filename.lastIndexOf('.')!=-1)
							ext=filename.substring(filename.lastIndexOf('.'));
						if(!ext.equals(lastExt))
							{
							w.write("ext"+ext+"\n");
							lastExt=ext;
							}
						w.write(""+fe.getKey()+"\n");
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
	

	
	
	
	
	
	}
