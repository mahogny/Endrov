package endrov.imagesetOST;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import endrov.data.*;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.imageset.*;
import endrov.util.*;

//TODO what happens if an object with a blobid from another imageset is moved to this imageset?


/**
 * I/O for OST-files and derived file formats
 * 
 * @author Johan Henriksson
 *
 */
public class EvIODataOST implements EvIOData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	
	public static void initPlugin() {}
	static
		{
		EvData.supportedFileFormats.add(new EvDataSupport(){
			public Integer loadSupports(String fileS)
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
			public List<Tuple<String,String[]>> getLoadFormats()
				{
				LinkedList<Tuple<String,String[]>> formats=new LinkedList<Tuple<String,String[]>>(); 
				formats.add(new Tuple<String, String[]>("OST",new String[]{".ost"}));
				return formats;
				}
			public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception
				{
				EvIODataOST io=new EvIODataOST(new File(file));
				EvData d=new EvData();
				io.initialLoad(d,cb);
				d.io=io;
				return d;
				}
			public Integer saveSupports(String fileS)
				{
				File file=new File(fileS);
				if(file.getName().endsWith(".ost")) //OST3+
					return 10;
				return null;
				}
			public List<Tuple<String,String[]>> getSaveFormats(){return getLoadFormats();}
			public EvIOData getSaver(EvData d, String file) throws IOException
				{
				return new EvIODataOST(new File(file));
				}
		});
		
		}
	
	/******************************************************************************************************
	 *                               Slice I/O class                                                      *
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

		public EvPixels loadJavaImage()
			{
			//This overloading is important during the save stage
			//with some recoding it would be possible to remove it
			if(oldio!=null)
				return oldio.loadJavaImage();
			else
				{
				BufferedImage im=EvCommonImageIO.loadJavaImage(f, null);
				if(im==null)
					return null;
				else
					return new EvPixels(im);
				}
			}
		
		public File outputFile(EvIODataOST ost, Imageset im, String channelName, EvDecimal frame, EvDecimal slice, String ext)
			{
			return ost.buildImagePath(im, channelName, frame, slice, ext);
			}
		
		public String toString()
			{
			if(oldio==null)
				return "write "+f;
			else
				return "write "+f+" using "+oldio;
			}
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                               *
	 *****************************************************************************************************/

	

	
	/**
	 * "Blob" - binary data for an object
	 */
	private class DiskBlob
		{
		private String currentDir; 
		//e.g. "imset-im", whatever was used when everything was read into memory
		//can be null, then it has not allocated a diskblob

		/** 
		 * Scanned files. This list reflects what is on harddrive. 
		 * An entry is not removed unless it is also deleted. By comparing this list to the dirty image list,
		 * it is possible to figure out what files to delete. 
		 */
		public HashMap<String,HashMap<EvDecimal,HashMap<EvDecimal,File>>> diskImageLoader=new HashMap<String, HashMap<EvDecimal,HashMap<EvDecimal,File>>>();
		
		public File getDirectory()
			{
			if(currentDir!=null)
				return new File(basedir,currentDir);
			else
				return null;
			}
		
		/**
		 * Reserve a name for this blob
		 */
		public void allocate(EvPath keyName)
			{
			if(currentDir==null)
				{
				//Try one name
				boolean taken=false;
				String dir="imset-"+keyName.getLeafName();
				for(DiskBlob blob:mapBlobs.values())
					if(blob.currentDir!=null)
						if(blob.currentDir.equals(dir))
							taken=true;
				//If name taken, try other names
				if(taken)
					{
					int cnt=0;
					do
						{
						taken=false;
						dir="imset-x"+cnt;
						for(DiskBlob blob:mapBlobs.values())
							if(blob.currentDir!=null)
								if(blob.currentDir.equals(dir))
									taken=true;
						cnt++;
						}while(taken);
					}
				currentDir=dir;
				if(EV.debugMode)
					System.out.println("allocating blob "+currentDir);
				new File(basedir,currentDir).mkdirs();
				}
			}
		}

	//Has to use object as reference, not name. otherwise rename will not work
	//As an effect, a delete will not remove objects from memory but only a save will. It is possible to reduce this
	//memory overhead using a weakhashmap and an additional vector but this complicates the design
	private HashMap<EvObject, DiskBlob> mapBlobs=new HashMap<EvObject, DiskBlob>();
	
	private DiskBlob getCreateBlob(EvObject ob)
		{
		DiskBlob blob=mapBlobs.get(ob);
		if(blob==null)
			{
			blob=new DiskBlob();
			mapBlobs.put(ob,blob);
			//listBlobs.add(blob);
			}
		return blob;
		}
	
	
	/** Path to imageset */
	public File basedir;


	/**
	 * Create a new recording. Basedir points to imageset- ie without the channel name
	 */
	public EvIODataOST(File basedir)
		{
		this.basedir=basedir;
		}

	public void initialLoad(EvData d,EvData.FileIOStatusCallback cb)
		{
		convert2_3();
		buildDatabase(d, cb);
		}

	/**
	 * Get name description of this metadata
	 */
	public String getMetadataName()
		{
		String imageset=basedir.getName();
		if(imageset.endsWith(".ost"))
			imageset=imageset.substring(0,imageset.length()-".ost".length());
		return imageset;
		}

	/**
	 * Get name description of this metadata
	 */
	public String toString()
		{
		return getMetadataName();
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
	 * What is the current filename for an image?
	 */
	private File getCurrentFileFor(Imageset im,String channelName, EvDecimal frame, EvDecimal slice)
		{
		HashMap<EvDecimal,HashMap<EvDecimal,File>> ce=mapBlobs.get(im).diskImageLoader.get(channelName);
		if(ce!=null)
			{
			HashMap<EvDecimal,File> fe=ce.get(frame);
			if(fe!=null)
				return fe.get(slice);
			}
		return null;
		}
	
	/**
	 * What should the filename be, given the compression?
	 */
	private File fileShouldBe(Imageset im, String channel, EvDecimal frame, EvDecimal z)
		{
		if(im.getChannel(channel).compression==100)
			return buildImagePath(im, channel, frame,z,".png");
		else
			return buildImagePath(im, channel, frame,z,".jpg"); //TODO jpeg2000 support
		}

	
	/** Internal: piece together a path to a channel */
	private File buildChannelPath(Imageset im, String channelName)
		{
		return new File(mapBlobs.get(im).getDirectory(),"ch-"+channelName);
		}
	private static File buildChannelPath(File blobpath, String channelName)
		{
		return new File(blobpath,"ch-"+channelName);
		}
	
	/** Internal: piece together a path to a frame */
	private static File buildFramePath(File channelPath, EvDecimal frame)
		{
		return new File(channelPath, EV.pad(frame,8));
		}
	
	private File buildFramePath(Imageset im, String channelName, EvDecimal frame)
		{
		return buildFramePath(buildChannelPath(im,channelName), frame);
		}
	
	/** Internal: piece together a path to an image */
	private File buildImagePath(Imageset im, String channelName, EvDecimal frame, EvDecimal slice, String ext)
		{
		return buildImagePath(buildFramePath(im, channelName, frame), slice, ext);
		}
	
	/** Internal: piece together a path to an image */
	private static File buildImagePath(File framePath, EvDecimal slice, String ext)
		{
		return new File(framePath,EV.pad(slice, 8)+ext);
		}
	
	/** internal: name of metadata file */
	private File getMetaFile()
		{
		return new File(basedir,"rmd.ostxml");
		}

	
	
	
	

	
	/******************************************************************************************************
	 *                               Save data                                                            *
	 *****************************************************************************************************/
	
	
	public static void saveMeta(EvData d, OutputStream os) throws IOException
	  {
	  //Add all objects
	  Document document=d.saveXmlMetadata();
	
	  //Write out to disk
	  Format format=Format.getPrettyFormat();
	  XMLOutputter outputter = new XMLOutputter(format);
	  outputter.output(document, os);
	  d.setMetadataModified(false);
	  }
	public static void saveMeta(EvData d, File outfile) throws IOException
	  {
	  FileOutputStream writer2=new FileOutputStream(outfile);
	  saveMeta(d,writer2);
	  writer2.close();
	  }

	

	public void saveMetaDataOnly(EvData d, EvData.FileIOStatusCallback cb) throws IOException
		{
		basedir.mkdirs();

		//Make sure to store the right IDs in metadata
		for(Map.Entry<EvObject, DiskBlob> e:mapBlobs.entrySet())
			e.getKey().ostBlobID=e.getValue().currentDir;
		saveMeta(d,getMetaFile());  
		d.setMetadataModified(false);
		}
		
	/**
	 * Save all data
	 */
	public void saveData(EvData data, EvData.FileIOStatusCallback cb)
		{
		try
			{
			allocateBlobs(data);
			saveMetaDataOnly(data, cb);
			saveImages(data);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
		
	/**
	 * Before writing data each imageset need a blob ID
	 */
	private void allocateBlobs(EvData data)
		{
		Map<EvPath,Imageset> dataImagesets=data.getIdObjectsRecursive(Imageset.class);
		for(Map.Entry<EvPath, Imageset> datae:dataImagesets.entrySet())
			{
			Imageset im=datae.getValue();
			DiskBlob blob=getCreateBlob(im);
			blob.allocate(datae.getKey());
			datae.getValue().ostBlobID=blob.currentDir;
			if(blob.currentDir==null)
				System.out.println("Warning: set blobid=null when saving");
/*				if(blob.currentDir==null)
				{
				blob.currentDir="imset-"+datae.getKey();
				System.out.println("allocating blob "+blob.currentDir);
				new File(basedir,blob.currentDir).mkdirs();
				}*/
			
			System.out.println("id "+datae.getKey()+"    "+datae.getValue().ostBlobID);
			}
		
		
		}
	
	/**
	 * Save images.
	 * 
   * This code looks complicated but solves the following problem:
   * if an image has been moved then it might have to be read into
   * memory or it will be overwritten before it is loaded. cannot
   * always consider this case because it is uncommon and will *eat* memory.
	 * 
	 */
	private void saveImages(EvData data)
		{
		
		//Map<String,Imageset> dataImagesets=data.getIdObjects(Imageset.class);
		Map<EvPath,Imageset> dataImagesets=data.getIdObjectsRecursive(Imageset.class);
		
		try
			{
			//Images to delete
			Set<File> toDelete=new HashSet<File>();
			//Images that need be read
			Map<File,HashSet<EvImage>> toRead=new HashMap<File, HashSet<EvImage>>();
			//Images to write
			LinkedList<EvImage> toWrite=new LinkedList<EvImage>();
			//What compression to use
			Map<EvImage,Integer> imCompression=new HashMap<EvImage, Integer>();
			
			//Which files will be deleted because they are no longer in the imageset?
			for(Map.Entry<EvObject, DiskBlob> ime:mapBlobs.entrySet()) //this works for the non-weakhashmap
				if(ime.getKey() instanceof Imageset)
					{
					//Does the imageset still exist?
					boolean contained=false;
					if(dataImagesets.containsValue(ime.getKey()))
						contained=true;
					
					Imageset im=(Imageset)ime.getKey();
					if(contained)
						{
						//Detailed check of which images are still there
						for(Map.Entry<String,HashMap<EvDecimal,HashMap<EvDecimal,File>>> ce:ime.getValue().diskImageLoader.entrySet())
							for(Map.Entry<EvDecimal, HashMap<EvDecimal,File>> fe:ce.getValue().entrySet())
								for(Map.Entry<EvDecimal, File> se:fe.getValue().entrySet())
									if(im.getImageLoader(ce.getKey(), fe.getKey(), se.getKey())==null)
										toDelete.add(se.getValue());
						}
					else
						{
						//Delete everything
						if(EV.debugMode)
							System.out.println("Deleting entire imageset");
						for(Map.Entry<String,HashMap<EvDecimal,HashMap<EvDecimal,File>>> ce:ime.getValue().diskImageLoader.entrySet())
							for(Map.Entry<EvDecimal, HashMap<EvDecimal,File>> fe:ce.getValue().entrySet())
								for(Map.Entry<EvDecimal, File> se:fe.getValue().entrySet())
									toDelete.add(se.getValue());
						}
					}
			
			//Newly created imagesets might not have a blob. Need to create these.
			//allocateBlobs();
			
			//Which images are dirty and need be written?
			//Dirty image := an image that has been modified or has not been written to this OST
			//               or is saved in the wrong location
			for(Map.Entry<EvPath, Imageset> datae:dataImagesets.entrySet())
				{
				Imageset im=datae.getValue();
				for(Map.Entry<String, EvChannel> ce:im.getChannels().entrySet())
					for(Map.Entry<EvDecimal, EvStack> fe:ce.getValue().imageLoader.entrySet())
						for(Map.Entry<EvDecimal, EvImage> ie:fe.getValue().entrySet())
							{
							//Does the image belong to this IO?
							EvIOImage oldio=ie.getValue().io;
							boolean belongsToThisDatasetIO = oldio!=null && oldio instanceof SliceIO && ((SliceIO)oldio).ost==this;
							//System.out.println("belongs to imageset "+belongsToThisDatasetIO);

							//Where should new file be written?
							File newFile=fileShouldBe(im,ce.getKey(), fe.getKey(),ie.getKey());

							//Check if dirty
							boolean dirty=!belongsToThisDatasetIO;
							if(ie.getValue().modified())
								dirty=true;
							if(!dirty)
								{
								//Check location of file. if location is wrong then it has to be resaved.
								//Is there any case the code can get here and oldio2.f is null? I don't think so.
								//File ending is not considered. to change compression, mark the file dirty
								SliceIO oldio2=(SliceIO)oldio;
								String currentFileName=oldio2.f.getAbsolutePath();
								String newFileName=newFile.getAbsolutePath();
								if(!currentFileName.substring(0, currentFileName.lastIndexOf(".")).equals(
										newFileName.substring(0, newFileName.lastIndexOf("."))))
									dirty=true;
								}
							
							//If dirty, prepare it to be written
							if(dirty)
								{
								EvImage evim=ie.getValue();
								
								//Mark all dirty. When they are not dirty they need not be written
								evim.isDirty=true;

								//Mark current file for reading if it is not in memory.
								//This is used whenever the image is taken from this IO to avoid overwriting,
								//otherwise the picture will be read from another source and nothing
								//can be guaranteed.
								if(evim.getMemoryImage()==null && belongsToThisDatasetIO)
									{
									File currentFile=((SliceIO)oldio).f;
									//File currentFile=getCurrentFileFor(ce.getKey(), fe.getKey(), ie.getKey());
									if(currentFile!=null)
										{
										if(EV.debugMode)
											System.out.println("adding to read"+currentFile);
										HashSet<EvImage> ims=toRead.get(currentFile);
										if(ims==null)
											toRead.put(currentFile, ims=new HashSet<EvImage>());
										ims.add(evim);
										
										if(!currentFile.equals(newFile))
											toDelete.add(newFile);
										}
									}
								
								
								SliceIO newio=new SliceIO(this,newFile);
								newio.oldio=oldio;
								evim.io=newio;
								
								toWrite.add(evim);
								if(EV.debugMode)
									System.out.println(evim.io);
								imCompression.put(evim,ce.getValue().compression);
								}
							
							}
				}
					
			
			if(EV.debugMode)
				{
				System.out.println("to read");
				for(Map.Entry<File, HashSet<EvImage>> entry:toRead.entrySet())
					System.out.println(entry.getKey());
				}
			
			System.out.println("writing files...");
			
			//Write the files
			while(!toWrite.isEmpty())
				{
				EvImage evim=toWrite.poll();
				//It might have been written already due to rescheduling
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
							ci.setMemoryImage(ci.getPixels());
							if(EV.debugMode)
								System.out.println("reading image. need write soon "+ci);
							toWrite.addFirst(ci);
							}
						}

					//Write image to disk
					BufferedImage bim=evim.getPixels().quickReadOnlyAWT();
					if(EV.debugMode)
						System.out.println("write "+io.f);
					io.f.getParentFile().mkdirs(); //TODO optimize. cache which exist?
					
					EvCommonImageIO.saveImage(bim, io.f, imCompression.get(evim));
					
					//Mark this image as done
					evim.isDirty=false;
					
					//Do not delete this image. Just in case some other operation got a strange idea
					toDelete.remove(io.f);
					}
				}
						
			//Delete the files
			System.out.println("Deleting files");
			for(File f:toDelete)
				{
				if(EV.debugMode)
					System.out.println("delete "+f);
				f.delete();
				}
			
			//Totally delete non-existing blobs
			HashSet<EvObject> deleteBlobs=new HashSet<EvObject>(mapBlobs.keySet());
			deleteBlobs.removeAll(dataImagesets.values());
			for(EvObject ob:deleteBlobs)
				{
				File f=mapBlobs.get(ob).getDirectory();
				if(f!=null && f.exists())
					EvFileUtil.deleteRecursive(f);
				}
			mapBlobs.keySet().removeAll(deleteBlobs);
			
			//Partially delete existing blobs
			for(Map.Entry<EvObject, DiskBlob> ime:mapBlobs.entrySet())
				if(ime.getKey() instanceof Imageset)
					{
					DiskBlob blob=ime.getValue();
					Imageset im=(Imageset)ime.getKey();

					//Totally delete non-existing channel directories
					File blobDir=blob.getDirectory();
					for(String oldChannel:blob.diskImageLoader.keySet())
						if(!im.metaObject.containsKey(oldChannel))
							{
							File chdir=new File(blobDir,"ch-"+oldChannel);
							if(EV.debugMode)
								System.out.println("Deleting entire channel: "+oldChannel);
							if(chdir.exists())
								EvFileUtil.deleteRecursive(chdir);
							}
					blob.diskImageLoader.keySet().retainAll(im.getChannels().keySet());

					//Partially delete existing channel directories
					for(String channelName:blob.diskImageLoader.keySet())
						{
						File chanPath=buildChannelPath(im,channelName);
						
						//Delete entire frames
						HashSet<EvDecimal> removeFrame=new HashSet<EvDecimal>(blob.diskImageLoader.get(channelName).keySet());
						removeFrame.removeAll(im.getChannel(channelName).imageLoader.keySet());
						for(EvDecimal frame:removeFrame)
							{
							File f=buildFramePath(chanPath, frame);
							if(EV.debugMode)
								System.out.println("Totally deleting frame "+frame);
							if(f.exists())
								EvFileUtil.deleteRecursive(f);
							}
						blob.diskImageLoader.get(channelName).keySet().retainAll(im.getChannel(channelName).imageLoader.keySet());
						
						//Delete slices
						for(Map.Entry<EvDecimal, HashMap<EvDecimal,File>> framee:blob.diskImageLoader.get(channelName).entrySet())
							{
							framee.getValue().keySet().removeAll(im.getChannel(channelName).imageLoader.get(framee.getKey()).keySet());
							for(File f:framee.getValue().values())
								{
								if(EV.debugMode)
									System.out.println("deleting slice "+f);
								f.delete();
								}
							}
						}
					}
			
			
			//Remove oldio pointers and build new imageloader
			for(Imageset im:dataImagesets.values())
				{
				DiskBlob blob=getCreateBlob(im);
				blob.diskImageLoader.clear();
				for(Map.Entry<String, EvChannel> ce:im.getChannels().entrySet())
					{
					HashMap<EvDecimal,HashMap<EvDecimal,File>> loaderFrames=new HashMap<EvDecimal, HashMap<EvDecimal,File>>();
					blob.diskImageLoader.put(ce.getKey(), loaderFrames);
					for(Map.Entry<EvDecimal, EvStack> fe:ce.getValue().imageLoader.entrySet())
						{
						HashMap<EvDecimal,File> loaderSlices=new HashMap<EvDecimal, File>();
						loaderFrames.put(fe.getKey(),loaderSlices);
						for(Map.Entry<EvDecimal, EvImage> ie:fe.getValue().entrySet())
							{
							SliceIO sio=(SliceIO)ie.getValue().io;
							sio.oldio=null;
							loaderSlices.put(ie.getKey(), sio.f);
							}
						}
					}
				}
			
			System.out.println("done saving");

			//Update the fast-load cache
			saveDatabaseCache();
			}
		catch (Exception e)
			{
			EvLog.printError("Error saving OST", e);
			}
		}

	

		
	
	
	
	/******************************************************************************************************
	 *                               Load images                                                          *
	 *****************************************************************************************************/

	/**
	 * Scan recording for channels and build a file database
	 */
	public void buildDatabase(EvData d)
		{
		buildDatabase(d,null);
		}
	public void buildDatabase(EvData d, EvData.FileIOStatusCallback cb)
		{
		File metaFile=new File(basedir,"rmd.ostxml");
		if(!metaFile.exists())
			EvLog.printError("PROBLEM! There is no rmd.ostxml. This is not conforming to the OST standard",null);

		if(basedir.exists())
			{
			//Load metadata
			try
				{
				d.metaObject.clear();
				cb.fileIOStatus(0, "loading meta...");
				d.loadXmlMetadata(new FileInputStream(metaFile));
				cb.fileIOStatus(0.3, "conversion if needed...");
				convert3_3d2(d,cb);
				cb.fileIOStatus(0.6, "loading images...");
				scanFiles(d, cb);
				}
			catch (FileNotFoundException e)
				{
				EvLog.printError("Could not load OST", e);
				}

			
			}
		else
			EvLog.printError("Fatal: Imageset base directory does not exist",null);
		}
	

	
	
	
	/**
	 * Scan files for all channels and build a database
	 */
	private void scanFiles(EvData data, EvData.FileIOStatusCallback cb)
		{
		//Create blobs
		mapBlobs.clear();
		
		for(Imageset im:data.getIdObjectsRecursive(Imageset.class).values())
			{
			DiskBlob blob=getCreateBlob(im);
			blob.currentDir=im.ostBlobID;
			if(blob.currentDir==null)
				{
				System.out.println("Fixed blob id. this should only occur during 3->3.2 transition");
				blob.currentDir=".";
				}
			else
				if(EV.debugMode)
					System.out.println("Found blob "+blob.currentDir);
			
			//Get list of images, from cache or by listing files
			if(!(getDatabaseCacheFile(blob).exists() && loadDatabaseCache(im, blob)))
				{
				File blobdir=blob.getDirectory();
				if(EV.debugMode)
					System.out.println("Scanning for images in "+blobdir);
				for(File chanf:blobdir.listFiles())
					if(chanf.isDirectory() && chanf.getName().startsWith("ch-"))
						{
						String channelName=chanf.getName().substring("ch-".length());
						EvLog.printLog("Found channel: "+channelName);
						scanFilesChannel(im, channelName, blob);
						}
				}
			
			//Update the list of images in the imageset object
			for(Map.Entry<String,HashMap<EvDecimal,HashMap<EvDecimal,File>>> ce:blob.diskImageLoader.entrySet())
				{
				EvChannel channel=im.getCreateChannel(ce.getKey());
				
				for(Map.Entry<EvDecimal, HashMap<EvDecimal,File>> fe:ce.getValue().entrySet())
					{
					EvStack stack=new EvStack();
					channel.imageLoader.put(fe.getKey(),stack);
					//TODO properly move metadata
					stack.resX=im.resX;
					stack.resY=im.resY;
					stack.dispX=channel.dispX;
					stack.dispY=channel.dispY;
					stack.binning=channel.chBinning;
					for(Map.Entry<EvDecimal, File> se:fe.getValue().entrySet())
						{
						EvImage evim=new EvImage();
						evim.io=new SliceIO(this,getCurrentFileFor(im,ce.getKey(), fe.getKey(), se.getKey()));
												
						stack.put(se.getKey(),evim);
						}
					}
				}
			}
		
		saveDatabaseCache();
		}
	
	
	
	/**
	 * Scan all files for this channel and build a database
	 */
	private void scanFilesChannel(Imageset imset, String channelName, DiskBlob blob)
		{
		//Rebuild this channel in diskimageloader. note that it is totally separate from metadata
		HashMap<EvDecimal,HashMap<EvDecimal,File>> channelSet=new HashMap<EvDecimal, HashMap<EvDecimal,File>>();
		blob.diskImageLoader.put(channelName, channelSet);
		
		File chandir=buildChannelPath(imset, channelName);
		File[] framedirs=chandir.listFiles();
		for(File framedir:framedirs)
			if(framedir.isDirectory() && !framedir.getName().startsWith("."))
				{
				//Backwards compat
				if(framedir.getName().indexOf('-')!=-1)
					{
					//Somehow this is a really old OST. remove chan-
					File nf=new File(framedir.getParentFile(),framedir.getName().substring(framedir.getName().indexOf('-')+1));
					if(EV.debugMode)
						System.out.println(nf);
					framedir.renameTo(nf);
					framedir=nf;
					}				
				
				EvDecimal framenum=new EvDecimal(framedir.getName());
				
				HashMap<EvDecimal,File> loaderset=new HashMap<EvDecimal,File>();
				channelSet.put(framenum, loaderset);
				File[] slicefiles=framedir.listFiles();
				for(File f:slicefiles)
					{
					String partname=f.getName();
					if(!partname.startsWith("."))
						{
						if(partname.lastIndexOf('-')!=-1)
							{
							//Somehow this is a really old OST. remove xxx-
							File nf=new File(f.getParentFile(),partname.substring(partname.lastIndexOf('-')+1));
							f.renameTo(nf);
							f=nf;
							partname=f.getName();
							}
						
						
						partname=partname.substring(0,partname.lastIndexOf('.'));
						try
							{
							EvDecimal slice=new EvDecimal(partname);
							loaderset.put(slice, f);
							}
						catch (NumberFormatException e)
							{
							EvLog.printError("partname: "+partname+" filename "+f.getName()+" framenum "+framenum,e);
							System.exit(1);
							}
						}
					}
				}
		}

	
	/******************************************************************************************************
	 *                               Image cache                                                          *
	 *****************************************************************************************************/

	/**
	 * Get the name of the database cache file
	 */
	private File getDatabaseCacheFile(DiskBlob blob)
		{
		return new File(new File(basedir, blob.currentDir),"imagecache.txt");
		}
	

	public static boolean loadDatabaseCacheMap(HashMap<String,HashMap<EvDecimal,HashMap<EvDecimal,File>>> diskImageLoader, InputStream cachefile, File blobFile)
		{
		try
			{
			String ext="";
			BufferedReader in = new BufferedReader(new InputStreamReader(cachefile,"UTF-8"));
			String line=in.readLine();
			if(!line.equals("version1")) //version1
				{
				EvLog.printLog("Image cache wrong version, ignoring");
				return false;
				}
			else
				{
				//Log.printLog("Loading imagelist cache "+blobFile);
				int numChannels=Integer.parseInt(in.readLine());
				for(int i=0;i<numChannels;i++)
					{
					String channelName=in.readLine();
					int numFrame=Integer.parseInt(in.readLine());

					HashMap<EvDecimal,HashMap<EvDecimal,File>> c=new HashMap<EvDecimal, HashMap<EvDecimal,File>>();
					diskImageLoader.put(channelName, c);

					
					
					String channeldirName=buildChannelPath(blobFile,channelName).getAbsolutePath();

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
				}
			return true;
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return false;
			}

		
		}
	
	
	/**
	 * Load database from cache into file-list. Return if it succeeded. Does not update imageset objects
	 */
	public boolean loadDatabaseCache(Imageset im, DiskBlob blob)
		{
		blob.diskImageLoader.clear();
		try
			{
			File cacheFile=getDatabaseCacheFile(blob);
			if(EV.debugMode)
				System.out.println("Attempting to load image cache "+cacheFile);
			return loadDatabaseCacheMap(blob.diskImageLoader, new FileInputStream(cacheFile), mapBlobs.get(im).getDirectory());
			}
		catch (FileNotFoundException e)
			{
			return false;
			}
		}
	
	
	/**
	 * Invalidate database cache (=deletes cache file)
	 */
	public void invalidateDatabaseCache()
		{
		for(DiskBlob blob:mapBlobs.values())
			getDatabaseCacheFile(blob).delete();
		}

	
	/**
	 * Save database as a cache file
	 */
	public void saveDatabaseCache()
		{
		try
			{
			//System.out.println("# blobs "+mapBlobs.size());
			for(DiskBlob blob:mapBlobs.values())
				{
				String lastExt="";
				File cFile=getDatabaseCacheFile(blob);
				BufferedWriter w=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cFile),"UTF-8"));
	
				//w.write("version2\n");
				w.write("version1\n");
	
				w.write(blob.diskImageLoader.size()+"\n");
	
				for(Map.Entry<String, HashMap<EvDecimal,HashMap<EvDecimal,File>>> ce:blob.diskImageLoader.entrySet())
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
				if(EV.debugMode)
					EvLog.printLog("Wrote cache file "+cFile);
				}
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}


	
	
	/******************************************************************************************************
	 *                               Converters for upgrading                                             *
	 *****************************************************************************************************/
	
	/**
	 * Convert OST2 -> OST3
	 */
	public void convert2_3()
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
	 * Convert 3 -> 3.2
	 * Timestep and resZ deleted, major file renaming. imageset directories
	 */
	public void convert3_3d2(EvData d,EvData.FileIOStatusCallback cb)
		{
		EvDecimal curv=new EvDecimal(d.metadataVersion);
		
		boolean chExists=false;
		for(File f:basedir.listFiles())
			if(f.getName().startsWith("ch-"))
				chExists=true;
		
		if(curv.less(new EvDecimal("3.2")) || chExists)
			{
			cb.fileIOStatus(0.9, "Converting to OST 3.2, this might take a while");
			EvLog.printLog("Updating files 3->3.2");
			//With SSHFS+mac there is a problem: renames hang the system!
			
			mapBlobs.clear(); //Make sure blob gets the right blobid

			File oldcache=new File(basedir,"imagecache.txt");
			if(oldcache.exists())
				oldcache.delete();

			//Hack when there is no imageset object
			if(d.getObjects(Imageset.class).isEmpty())
				{
				Imageset im=new Imageset();
				d.metaObject.put("im", im);
				im.resX=im.resY=im.resZ=1;
				im.metaTimestep=1;
				}
			
			//There is only one imageset in these dated formats
			List<Imageset> ims=d.getObjects(Imageset.class);
			if(ims.size()>0)
				{
				Imageset im=ims.get(0);
				im.ostBlobID="imset-im";
				DiskBlob blob=getCreateBlob(im);
				blob.currentDir=im.ostBlobID;
				
				//For all channels
				for(File fchan:basedir.listFiles())
					if(fchan.isDirectory() && fchan.getName().startsWith("ch-"))
						{
						
						//For all frames, prepend "n"
						for(File fframe:fchan.listFiles())
							if(fframe.isDirectory() && !fframe.getName().startsWith("."))
								{
								File newframe=new File(fchan,"n"+fframe.getName());
								fframe.renameTo(newframe);
								}
						//For all frames, remove "n" and multiply
						for(File fframe:fchan.listFiles())
							if(fframe.isDirectory() && fframe.getName().startsWith("n"))
								{
								String s=fframe.getName();
								EvDecimal cur=new EvDecimal(s.substring(1));
								File newframe=new File(fchan, EV.pad(cur.multiply(new EvDecimal(im.metaTimestep)), 8));
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
										fslice.renameTo(newslice);
										}
								}
						}
	
				//Move all files under imset-im
				File imdir=new File(basedir,"imset-im");
				imdir.mkdir();
				for(File child:basedir.listFiles())
					if(child.getName().startsWith("ch-"))
						child.renameTo(new File(imdir,child.getName()));
	
				//All objects go beneath im as this is what was the old intention
				d.metaObject.remove("im");
				im.metaObject.putAll(d.metaObject);
				d.metaObject.clear();
				d.metaObject.put("im",im);
				}
			else
				System.out.println("No imageset found");
			
			System.out.println("Saving meta 3.2");
			try
				{
				saveMetaDataOnly(d, null);
				}
			catch (IOException e)
				{
				e.printStackTrace();
				}
			//			saveData(d, null);
			invalidateDatabaseCache();
//			System.out.println("Reloading file listing");
//			scanFiles(d,EvData.deafFileIOCB);
				
			}
		}
	
	
	
	}
