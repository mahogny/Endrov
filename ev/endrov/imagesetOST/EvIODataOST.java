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
//TODO add a hook to each object which io they belong to?


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
				if(file.isDirectory() && file.getName().endsWith(".ost"))
					return 10;
				else
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
				if(file.getName().endsWith(".ost"))
					return 10;
				else
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
		public HashMap<EvDecimal,HashMap<EvDecimal,File>> diskImageLoader33=new HashMap<EvDecimal,HashMap<EvDecimal,File>>();
		
		public File getDirectory()
			{
			if(currentDir!=null)
				return new File(basedir,currentDir);
			else
				return null;
			}

		public void allocate(EvPath keyName)
			{
			allocate(keyName.getLeafName());
			}
		/**
		 * Reserve a name for this blob
		 */
		public void allocate(String keyName)
			{
			if(currentDir==null)
				{
				//Try one name
				boolean taken=false;
				String dir="blob-"+keyName;
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
						dir="blob-"+keyName+cnt;
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
	private File getCurrentFileFor33(EvChannel ch, EvDecimal frame, EvDecimal slice)
		{
		HashMap<EvDecimal,HashMap<EvDecimal,File>> ce=mapBlobs.get(ch).diskImageLoader33;
		if(ce!=null)
			{
			HashMap<EvDecimal,File> fe=ce.get(frame);
			if(fe!=null)
				return fe.get(slice);
			}
		return null;
		}

	/**
	 * Acceptable path, but not the right location
	 */
	/*
	private File fileShouldBeOld(EvChannel ch, EvDecimal frame, EvDecimal z)
		{
		if(ch.compression==100)
			return buildImagePath33(ch, frame,z,".png");
		else
			return buildImagePath33(ch, frame,z,".jpg"); //TODO jpeg2000 support
		}*/

	/**
	 * What should the filename be, given the compression?
	 */
	private File fileShouldBe(EvChannel ch, EvDecimal frame, EvDecimal z, EvDecimal resZ, EvDecimal dispZ)
		{
		int d=(int)Math.round(z.subtract(dispZ).divide(resZ).doubleValue());
		//TODO don't like the (int), can it all be done with evdecimal?
		String zs="b"+EV.pad(d, 8);
		
		/*
		if(ch.compression==100)
			return buildImagePath33(ch, frame,z,".png");
		else
			return buildImagePath33(ch, frame,z,".jpg"); //TODO jpeg2000 support
		*/
		if(ch.compression==100)
			return buildImagePath33(ch, frame,zs,".png");
		else
			return buildImagePath33(ch, frame,zs,".jpg"); //TODO jpeg2000 support

		}
	
	/** Internal: piece together a path to an image */
	/*
	private File buildImagePath33(EvChannel ch, EvDecimal frame, EvDecimal slice, String ext)
		{
		return buildImagePath(buildFramePath(mapBlobs.get(ch).getDirectory(), frame), slice, ext);
		}
	*/
	private File buildImagePath33(EvChannel ch, EvDecimal frame, String slice, String ext)
		{
		return new File(buildFramePath(mapBlobs.get(ch).getDirectory(), frame), slice+ext);
		}
	
	/** Internal: piece together a path to a channel */
	private File buildChannelPath(Imageset im, String channelName)
		{
		return new File(mapBlobs.get(im).getDirectory(),"ch-"+channelName);
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
	  d.setMetadataNotModified();
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
			saveImages(data); //Important that this is done after saving meta
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
		Map<EvPath,EvChannel> dataImagesets=data.getIdObjectsRecursive(EvChannel.class);
		for(Map.Entry<EvPath, EvChannel> datae:dataImagesets.entrySet())
			{
			EvChannel im=datae.getValue();
			DiskBlob blob=getCreateBlob(im);
			blob.allocate("ch"+datae.getKey());
			datae.getValue().ostBlobID=blob.currentDir;
			if(blob.currentDir==null)
				System.out.println("Warning: set blobid=null when saving");
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
		Map<EvPath,EvChannel> dataChannels=data.getIdObjectsRecursive(EvChannel.class);
		
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
				if(ime.getKey() instanceof EvChannel)
					{
					EvChannel ch=(EvChannel)ime.getKey();

					if(!ch.isGeneratedData)
						{
						//Does the imageset still exist?
						boolean contained=false;
						if(dataChannels.containsValue(ime.getKey()))
							contained=true;
						
						if(contained)
							{
							//Detailed check of which images are still there
							for(Map.Entry<EvDecimal, HashMap<EvDecimal,File>> fe:ime.getValue().diskImageLoader33.entrySet())
								for(Map.Entry<EvDecimal, File> se:fe.getValue().entrySet())
									if(ch.getImageLoader(fe.getKey(),se.getKey())==null)
										toDelete.add(se.getValue());
							}
						else
							{
							//Delete everything
							if(EV.debugMode)
								System.out.println("Deleting entire imageset");
							//for(Map.Entry<String,HashMap<EvDecimal,HashMap<EvDecimal,File>>> ce:ime.getValue().diskImageLoader.entrySet())
								for(Map.Entry<EvDecimal, HashMap<EvDecimal,File>> fe:ime.getValue().diskImageLoader33.entrySet())
									for(Map.Entry<EvDecimal, File> se:fe.getValue().entrySet())
										toDelete.add(se.getValue());
							}
						}
					}
			
			//Newly created imagesets might not have a blob. Need to create these.
			allocateBlobs(data);
			
			//Which images are dirty and need be written?
			//Dirty image := an image that has been modified or has not been written to this OST
			//               or is saved in the wrong location
			for(Map.Entry<EvPath, EvChannel> datae:dataChannels.entrySet())
				{
				//Imageset im=datae.getValue();
				EvChannel ch=datae.getValue();

				//for(Map.Entry<String, EvChannel> ce:im.getChannels().entrySet())
					for(Map.Entry<EvDecimal, EvStack> fe:ch.imageLoader.entrySet())
						for(Map.Entry<EvDecimal, EvImage> ie:fe.getValue().entrySet())
							{
							EvStack stack=fe.getValue();
							
							//Does the image belong to this IO?
							EvIOImage oldio=ie.getValue().io;
							boolean belongsToThisDatasetIO = oldio!=null && oldio instanceof SliceIO && ((SliceIO)oldio).ost==this;
							//System.out.println("belongs to imageset "+belongsToThisDatasetIO);

							//Where should new file be written?
							File newFile=fileShouldBe(ch, fe.getKey(),ie.getKey(), stack.resZ, stack.dispZ);


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
								imCompression.put(evim,ch.compression);
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
			deleteBlobs.removeAll(dataChannels.values());
			for(EvObject ob:deleteBlobs)
				{
				File f=mapBlobs.get(ob).getDirectory();
				if(f!=null && f.exists())
					EvFileUtil.deleteRecursive(f);
				}
			mapBlobs.keySet().removeAll(deleteBlobs);
			
			//Partially delete existing blobs
			for(Map.Entry<EvObject, DiskBlob> ime:mapBlobs.entrySet())
				if(ime.getKey() instanceof EvChannel)
					{
					DiskBlob blob=ime.getValue();
					EvChannel ch=(EvChannel)ime.getKey();

					//Partially delete existing channel directories
					File chanPath=blob.getDirectory();

					//Delete entire frames
					HashSet<EvDecimal> removeFrame=new HashSet<EvDecimal>(blob.diskImageLoader33.keySet());
					removeFrame.removeAll(ch.imageLoader.keySet());
					for(EvDecimal frame:removeFrame)
						{
						File f=buildFramePath(chanPath, frame);
						if(EV.debugMode)
							System.out.println("Totally deleting frame "+frame);
						if(f.exists())
							EvFileUtil.deleteRecursive(f);
						}
					blob.diskImageLoader33.keySet().retainAll(ch.imageLoader.keySet());

					//Delete slices
					for(Map.Entry<EvDecimal, HashMap<EvDecimal,File>> framee:blob.diskImageLoader33.entrySet())
						{
						framee.getValue().keySet().removeAll(ch.imageLoader.get(framee.getKey()).keySet());
						for(File f:framee.getValue().values())
							{
							if(EV.debugMode)
								System.out.println("deleting slice "+f);
							f.delete();
							}
						}
					}
			
			
			//Remove oldio pointers and build new imageloader
			for(EvChannel ch:dataChannels.values())
				{
				DiskBlob blob=getCreateBlob(ch);
				blob.diskImageLoader33.clear();
				
				HashMap<EvDecimal,HashMap<EvDecimal,File>> loaderFrames=new HashMap<EvDecimal, HashMap<EvDecimal,File>>();
				blob.diskImageLoader33=loaderFrames;//.clear();//.put(ce.getKey(), loaderFrames);
				for(Map.Entry<EvDecimal, EvStack> fe:ch.imageLoader.entrySet())
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
			
			System.out.println("done saving");

			//Update the fast-load cache
			saveDatabaseCache33();
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
				cb.fileIOStatus(0.3, "loading images...");
				scanFiles33(d, cb);
				cb.fileIOStatus(0.6, "conversion...");
				convertSlicesToB(d);
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
	private void scanFiles33(EvData data, EvData.FileIOStatusCallback cb)
		{
		//Create blobs
		mapBlobs.clear();

		for(EvChannel channel:data.getIdObjectsRecursive(EvChannel.class).values())
			{
			DiskBlob blob=getCreateBlob(channel);
			blob.currentDir=channel.ostBlobID;

			//Get list of images, from cache or by listing files
			if(!(getDatabaseCacheFile(blob).exists() && loadDatabaseCache33(channel, blob)))
				{
				File blobdir=blob.getDirectory();
				//if(EV.debugMode)
					System.out.println("Scanning for images in "+blobdir);
				scanFilesChannel33(channel, blob);
				}

			//Update the list of images in the imageset object
			//System.out.println("Got: "+blob.diskImageLoader33);
			for(Map.Entry<EvDecimal, HashMap<EvDecimal,File>> fe:blob.diskImageLoader33.entrySet())
				{

				Double useResX=channel.defaultResX;
				Double useResY=channel.defaultResY;
				EvDecimal useResZ=channel.defaultResZ;

					
				
				HashMap<String,String> frameKeys=channel.metaFrame.get(fe.getKey());
				if(frameKeys==null)
					frameKeys=new HashMap<String, String>();

				//Override frame resolution
				if(frameKeys.containsKey("resX"))
					useResX=Double.parseDouble(frameKeys.get("resX"));
				if(frameKeys.containsKey("resY"))
					useResY=Double.parseDouble(frameKeys.get("resY"));
				if(frameKeys.containsKey("resZ"))
					useResZ=new EvDecimal(frameKeys.get("resZ"));

				//Default displacement
				double useDispX=channel.defaultDispX;
				double useDispY=channel.defaultDispY;
				EvDecimal useDispZ=channel.defaultDispZ;

				//Override for each stack
				if(frameKeys.containsKey("dispX"))
					useDispX=Double.parseDouble(frameKeys.get("dispX"));
				if(frameKeys.containsKey("dispY"))
					useDispY=Double.parseDouble(frameKeys.get("dispY"));
				if(frameKeys.containsKey("dispZ"))
					useDispZ=new EvDecimal(frameKeys.get("dispZ"));

				EvStack stack=new EvStack();
				channel.imageLoader.put(fe.getKey(),stack);
				if(useResX==null)
					{
					System.out.println("!!!!! Resolution problem for "+blob.currentDir+" frame "+fe.getKey());
					useResX=1.0;
					useResY=1.0;
					}
				
				
				stack.resX=useResX;
				stack.resY=useResY;
				stack.resZ=useResZ;
				stack.dispX=useDispX;
				stack.dispY=useDispY;
				stack.dispZ=useDispZ;
				//stack.binning=channel.chBinning;
				for(Map.Entry<EvDecimal, File> se:fe.getValue().entrySet())
					{
					EvImage evim=new EvImage();
					evim.io=new SliceIO(this,getCurrentFileFor33(channel, fe.getKey(), se.getKey()));
					//System.out.println("file: "+getCurrentFileFor33(channel, fe.getKey(), se.getKey()));
					stack.put(se.getKey(),evim);
					}
				}
			}

		
		saveDatabaseCache33();
		}
	

	
	
	/**
	 * Scan all files for this channel and build a database
	 */
	private void scanFilesChannel33(EvChannel ch, DiskBlob blob)
		{
		//Rebuild this channel in diskimageloader. note that it is totally separate from metadata
		blob.diskImageLoader33.clear();
		
		File chandir=blob.getDirectory();
		if(!chandir.exists())
			{
			System.out.println("Chan dir not found: "+chandir);
			return;
			}
		File[] framedirs=chandir.listFiles();
		for(File framedir:framedirs)
			if(framedir.isDirectory() && !framedir.getName().startsWith("."))
				{
				EvDecimal framenum=new EvDecimal(framedir.getName());
				
				HashMap<EvDecimal,File> loaderset=new HashMap<EvDecimal,File>();
				blob.diskImageLoader33.put(framenum, loaderset);
				File[] slicefiles=framedir.listFiles();
				for(File f:slicefiles)
					{
					String partname=f.getName();
					if(!partname.startsWith("."))
						{
						partname=partname.substring(0,partname.lastIndexOf('.'));
						try
							{
							
							EvDecimal slice;
							if(partname.startsWith("b"))
								{
								//Need to calculate position
								EvDecimal resZ=ch.defaultResZ;
								EvDecimal dispZ=ch.defaultDispZ;
								if(ch.metaFrame.containsKey(framenum))
									{
									String overrideResZ=ch.metaFrame.get(framenum).get("resZ");
									if(overrideResZ!=null)
										resZ=new EvDecimal(overrideResZ);
									
									String overrideDispZ=ch.metaFrame.get(framenum).get("DispZ");
									if(overrideDispZ!=null)
										dispZ=new EvDecimal(overrideDispZ);
									}
								

								slice=new EvDecimal(partname.substring(1)).multiply(resZ).add(dispZ);
								
								//System.out.println("Found file "+partname);
								}
							else
								slice=new EvDecimal(partname);
							
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
	

	
	public static boolean loadDatabaseCacheMap33(EvChannel ch, HashMap<EvDecimal,HashMap<EvDecimal,File>> c, InputStream cachefile, File blobFile)
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
				int numFrame=Integer.parseInt(in.readLine());


				String channeldirName=blobFile.getAbsolutePath();

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
						
						EvDecimal slice;
						if(s.startsWith("b"))
							{
							//Need to calculate position
							EvDecimal resZ=ch.defaultResZ;
							EvDecimal dispZ=ch.defaultDispZ;
							if(ch.metaFrame.containsKey(frame))
								{
								String overrideZ=ch.metaFrame.get(frame).get("resZ");
								if(overrideZ!=null)
									resZ=new EvDecimal(overrideZ);
								
								String overrideDispZ=ch.metaFrame.get(frame).get("DispZ");
								if(overrideDispZ!=null)
									dispZ=new EvDecimal(overrideDispZ);
								}
							slice=new EvDecimal(s.substring(1)).multiply(resZ).add(dispZ);
							}
						else
							slice=new EvDecimal(s);

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
		catch (Exception e)
			{
			e.printStackTrace();
			return false;
			}

		
		}
	
	
	/**
	 * Load database from cache into file-list. Return if it succeeded. Does not update imageset objects
	 */
	public boolean loadDatabaseCache33(EvChannel ch, DiskBlob blob)
		{
		blob.diskImageLoader33.clear();
		try
			{
			File cacheFile=getDatabaseCacheFile(blob);
			if(EV.debugMode)
				System.out.println("Attempting to load image cache "+cacheFile);
			return loadDatabaseCacheMap33(ch, blob.diskImageLoader33, new FileInputStream(cacheFile), mapBlobs.get(ch).getDirectory());
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
	public void saveDatabaseCache33()
		{
		try
			{
			//System.out.println("# blobs "+mapBlobs.size());
			for(Map.Entry<EvObject, DiskBlob> b:mapBlobs.entrySet())
				if(b.getKey() instanceof EvChannel)
					{
					DiskBlob blob=b.getValue();
					String lastExt="";
					File cFile=getDatabaseCacheFile(blob);
					if(!blob.getDirectory().exists())
						{
						System.out.println("Not creating cache because "+blob.getDirectory()+" does not exist");
						return;
						}
					BufferedWriter w=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cFile),"UTF-8"));

					//w.write("version2\n");
					w.write("version1\n");

					w.write(""+blob.diskImageLoader33.size()+"\n");
					for(EvDecimal frame:blob.diskImageLoader33.keySet())
						{
						//EvDecimal diskFrame=frame.divide(meta.metaTimestep);
						//w.write(""+diskFrame+"\n");
						w.write(""+frame+"\n");
						w.write(""+blob.diskImageLoader33.get(frame).size()+"\n");
						for(Map.Entry<EvDecimal, File> fe:blob.diskImageLoader33.get(frame).entrySet())
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
							if(filename.startsWith("b"))
								w.write(""+filename.substring(0,filename.length()-ext.length())+"\n");
							else
								w.write(""+fe.getKey()+"\n");
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
	
	
	public void convertSlicesToB(EvData data)
		{
		boolean changed=false;
		for(EvChannel ch:data.getIdObjectsRecursive(EvChannel.class).values())
			{
			for(EvStack stack:ch.imageLoader.values())
				{
				for(Map.Entry<EvDecimal,EvImage> e:stack.entrySet())
					{
					EvImage evim=e.getValue();
					SliceIO io=(SliceIO)evim.io;
					
					String curName=io.f.getName();
					if(!curName.startsWith("b"))
						{
						EvDecimal d=e.getKey().subtract(stack.dispZ).divide(stack.resZ);
						int v=(int)Math.round(d.doubleValue());
//						String s="b"+EV.pad(d, 8);
						String s="b"+EV.pad(v, 8);

						File newFile;

						if(curName.endsWith(".jpg") || curName.endsWith(".jpeg"))
							newFile=new File(io.f.getParentFile(),s+".jpg");
						else if(curName.endsWith(".png"))
							newFile=new File(io.f.getParentFile(),s+".png");
						else
							{
							System.out.println("AIIIIEEEEE");
							newFile=null;
							}
		
						if(newFile!=null)
							{
							if(!changed)
								{								
								invalidateDatabaseCache();
								changed=true;
								}
//							System.out.println(curName+"  "+io.f+"  ->  "+newFile);
							io.f.renameTo(newFile);
							io.f=newFile;
							}
						}
					
					
					}
				
				}

			}
		}
	
	
	}
