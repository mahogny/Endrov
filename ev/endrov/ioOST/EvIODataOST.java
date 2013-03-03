/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ioOST;

import java.io.*;
import java.util.*;

import javax.vecmath.Vector3d;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import endrov.core.EndrovCore;
import endrov.core.EndrovUtil;
import endrov.core.log.EvLog;
import endrov.data.*;
import endrov.typeImageset.*;
import endrov.util.*;
import endrov.util.collection.Tuple;
import endrov.util.io.EvFileUtil;
import endrov.util.math.EvDecimal;

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
	
	
	
	/******************************************************************************************************
	 *                               Slice I/O class                                                      *
	 *****************************************************************************************************/

	private static class SliceIO extends EvImageReader
		{
		public File f;
		public EvIODataOST ost;
		public EvImageReader oldio;   //Strong pointer! make sure to delete it ASAP
		public SliceIO(EvIODataOST ost, File f)
			{
			this.ost=ost;
			this.f=f;
			}

		public EvPixels eval(ProgressHandle progh)
			{
			//This overloading is important during the save stage.
			//with some recoding it would be possible to remove it
			if(oldio!=null)
				return oldio.get(progh);
			else
				{
				return EvCommonImageIO.loadImagePlane(f, null);
				}
			}
		
		public String toString()
			{
			if(oldio==null)
				return "write "+f;
			else
				return "write "+f+" using "+oldio;
			}
		
		public File getRawJPEGData()
			{
			return defaultGetRawJPEG(f);
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
		public HashMap<EvDecimal,HashMap<Integer,File>> diskImageLoader33=new HashMap<EvDecimal,HashMap<Integer,File>>();
		
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
				if(EndrovCore.debugMode)
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
			mapBlobs.put(ob,blob=new DiskBlob());
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
	private File getCurrentFileFor33(EvChannel ch, EvDecimal frame, Integer slice)
		{
		HashMap<EvDecimal,HashMap<Integer,File>> ce=mapBlobs.get(ch).diskImageLoader33;
		if(ce!=null)
			{
			HashMap<Integer,File> fe=ce.get(frame);
			if(fe!=null)
				return fe.get(slice);
			}
		return null;
		}
	
	


	/**
	 * What should the filename be, given the compression?
	 */
	private File fileShouldBe(EvChannel ch, EvDecimal frame, int z)
		{
		int d=z;
		//int d=(int)Math.round(z.subtract(dispZ).divide(resZ).doubleValue());
		//TODO don't like the (int), can it all be done with evdecimal?
		String zs="b"+EndrovUtil.pad(d, 8);
		
		if(ch.compression==100)
			return buildImagePath33(ch, frame,zs,".png");
		else
			return buildImagePath33(ch, frame,zs,".jpg"); //TODO jpeg2000 support

		}
	
	/** Internal: piece together a path to an image */
	private File buildImagePath33(EvChannel ch, EvDecimal frame, String slice, String ext)
		{
		return new File(buildFramePath(mapBlobs.get(ch).getDirectory(), frame), slice+ext);
		}
	
	/** Internal: piece together a path to a frame */
	private static File buildFramePath(File channelPath, EvDecimal frame)
		{
		return new File(channelPath, EndrovUtil.pad(frame,8));
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
	  //d.setMetadataNotModified();
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
			data.setMetadataNotModified();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
		
	/**
	 * Before writing data each imageset need a blob ID
	 */
	private void allocateBlobs(EvContainer data)
		{
		Map<EvPath,EvChannel> dataChannels=data.getIdObjectsRecursive(EvChannel.class);
		for(Map.Entry<EvPath, EvChannel> datae:dataChannels.entrySet())
			{
			//Get a name for the blob. Must not contain special characters
			StringBuffer sb=new StringBuffer("ch");
			for(char c:datae.getKey().getLeafName().toCharArray())
				if(Character.isLetterOrDigit(c))
					sb.append(c);
			
			EvChannel ch=datae.getValue();
			DiskBlob blob=getCreateBlob(ch);
			blob.allocate(sb.toString());
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
			Map<File,HashSet<EvImagePlane>> toRead=new HashMap<File, HashSet<EvImagePlane>>();
			//Images to write
			LinkedList<EvImagePlane> toWrite=new LinkedList<EvImagePlane>();
			//What compression to use
			Map<EvImagePlane,Integer> imCompression=new HashMap<EvImagePlane, Integer>();
			
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
							for(Map.Entry<EvDecimal, HashMap<Integer,File>> fe:ime.getValue().diskImageLoader33.entrySet())
								for(Map.Entry<Integer, File> se:fe.getValue().entrySet())
									{
									EvStack stack=ch.getStack(new ProgressHandle(), fe.getKey());  //TODO show status at least
									if(stack!=null)
										{
										//Check this slice
										if(!stack.hasPlaneDEPRECATED(se.getKey())) //This will throw an exception if it doesn't exist!
										//if(stack.loaders.get(se.getKey())==null)
											toDelete.add(se.getValue());
										}
									else
										{
										//Delete entire stack
										toDelete.add(se.getValue());
										}
									}
							}
						else
							{
							//Delete everything
							if(EndrovCore.debugMode)
								System.out.println("Deleting entire imageset");
							//for(Map.Entry<String,HashMap<EvDecimal,HashMap<EvDecimal,File>>> ce:ime.getValue().diskImageLoader.entrySet())
								for(Map.Entry<EvDecimal, HashMap<Integer,File>> fe:ime.getValue().diskImageLoader33.entrySet())
									for(Map.Entry<Integer, File> se:fe.getValue().entrySet())
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
				EvChannel ch=datae.getValue();

//					for(Map.Entry<EvDecimal, EvStack> fe:ch.imageLoader.entrySet())
					for(EvDecimal feFrame:ch.getFrames())
						{
						EvStack stack=ch.getStack(new ProgressHandle(), feFrame);  //TODO show progress at least
						for(int az=0;az<stack.getDepth();az++)
						//for(Map.Entry<EvDecimal, EvImage> ie:fe.getValue().entrySet())
							if(stack.hasPlaneDEPRECATED(az))  //TODO should not be needed
							{
							//EvStack stack=fe.getValue();
							EvImagePlane evim=stack.getPlane(az);
							
							//Does the image belong to this IO?
							EvImageReader oldio=evim.io;
							boolean belongsToThisDatasetIO = oldio!=null && oldio instanceof SliceIO && ((SliceIO)oldio).ost==this;
							//System.out.println("belongs to imageset "+belongsToThisDatasetIO);

							//Where should new file be written?
							File newFile=fileShouldBe(ch, feFrame,az);
							
							//Check if dirty
							boolean dirty=!belongsToThisDatasetIO;
							if(evim.modified())
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
										if(EndrovCore.debugMode)
											System.out.println("adding to read"+currentFile);
										HashSet<EvImagePlane> ims=toRead.get(currentFile);
										if(ims==null)
											toRead.put(currentFile, ims=new HashSet<EvImagePlane>());
										ims.add(evim);
										
										if(!currentFile.equals(newFile))
											toDelete.add(currentFile);
										}
									}
								
								
								//System.out.println("File should be "+newFile+" blob: "+getCreateBlob(ch).currentDir);
								
								SliceIO newio=new SliceIO(this,newFile);
								newio.oldio=oldio;
								evim.io=newio;
								
								toWrite.add(evim);
								if(EndrovCore.debugMode)
									System.out.println(evim.io);
								imCompression.put(evim,ch.compression);
								}
							
							}
						}
				}
					
			
			if(EndrovCore.debugMode)
				{
				System.out.println("to read");
				for(Map.Entry<File, HashSet<EvImagePlane>> entry:toRead.entrySet())
					System.out.println(entry.getKey());
				}
			
			System.out.println("writing files...");
			
			HashSet<File> existingDirs=new HashSet<File>();
			
			//Write the files
			while(!toWrite.isEmpty())
				{
				EvImagePlane evim=toWrite.poll();
				//It might have been written already due to rescheduling
				if(evim.isDirty)
					{
					//Even an overwrite is equivalent to optional read, then delete.
					//This is important if one slice is for example copied and the original modified.
					SliceIO io=(SliceIO)evim.io;

					//Have all images dependent on this file read it into memory
					//As an optimization, put these first in write queue to avoid reading
					//an entire channel into memory before writing it out
					HashSet<EvImagePlane> needToRead=toRead.get(io.f);
					if(needToRead!=null)
						{
						toRead.remove(io.f);
						for(EvImagePlane ci:needToRead)
							{
							ci.setMemoryImage(ci.getPixels(new ProgressHandle()));
//							if(EV.debugMode)
							System.out.println("reading image. need write soon "+ci+" (for "+io.f+")");
							toWrite.addFirst(ci);
							}
						}


					//Create parent directory. 
					//To avoid excessive file tree I/O, cache which ones have already been created
					File parentFile=io.f.getParentFile();
					if(!existingDirs.contains(parentFile))
						{
						parentFile.mkdirs();
						existingDirs.add(parentFile);
						}

					
					//Write image to disk. It might turn out in the last minute that the file format
					//does not work because of non-8 bits; then change
					File fToWrite=io.f;
					fToWrite=EvCommonImageIO.saveImagePlane(evim.getPixels(new ProgressHandle()), io.f, imCompression.get(evim));
					io.f=fToWrite;
					
					//Mark image as on disk, safe to unload
					evim.ioIsNowOnDisk(new ProgressHandle());
					
					//Make sure to not delete this file
					toDelete.remove(io.f);
					
					}
				}
						
			//Delete the files
			System.out.println("Deleting files");
			for(File f:toDelete)
				{
				//if(EV.debugMode)
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
					removeFrame.removeAll(ch.getFrames());
					for(EvDecimal frame:removeFrame)
						{
						File f=buildFramePath(chanPath, frame);
						if(EndrovCore.debugMode)
							System.out.println("Totally deleting frame "+frame);
						if(f.exists())
							EvFileUtil.deleteRecursive(f);
						}
					blob.diskImageLoader33.keySet().retainAll(ch.getFrames());

					//Delete slices
					for(Map.Entry<EvDecimal, HashMap<Integer,File>> framee:blob.diskImageLoader33.entrySet())
						{
						EvStack stack=ch.getStack(new ProgressHandle(), framee.getKey());
						//framee.getValue().keySet().removeAll(stack.keySet());
						for(int az=0;az<stack.getDepth();az++)
							framee.getValue().remove(az);
						for(File f:framee.getValue().values())
							{
							if(EndrovCore.debugMode)
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
				
				HashMap<EvDecimal,HashMap<Integer,File>> loaderFrames=new HashMap<EvDecimal, HashMap<Integer,File>>();
				blob.diskImageLoader33=loaderFrames;//.clear();//.put(ce.getKey(), loaderFrames);
				//for(Map.Entry<EvDecimal, EvStack> fe:ch.imageLoader.entrySet())
				for(EvDecimal feFrame:ch.getFrames())
					{
					EvStack feStack=ch.getStack(new ProgressHandle(), feFrame);
					HashMap<Integer,File> loaderSlices=new HashMap<Integer, File>();
					loaderFrames.put(feFrame,loaderSlices);
					//for(Map.Entry<Integer, EvImage> ie:fe.getValue().entrySet())
					for(int az=0;az<feStack.getDepth();az++)
						{
						EvImagePlane evim=feStack.getPlane(az);
						SliceIO sio=(SliceIO)evim.io;
						sio.oldio=null;
//						loaderSlices.put(ie.getKey(), sio.f);
						loaderSlices.put(az, sio.f);
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
				//convertSlicesToB(d);
				
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

		for(Map.Entry<EvPath, EvChannel> entry:data.getIdObjectsRecursive(EvChannel.class).entrySet())
			{
			EvChannel channel=entry.getValue();
			
			DiskBlob blob=getCreateBlob(channel);
			blob.currentDir=channel.ostBlobID;

			//Get list of images, from cache or by listing files
			if(!(getDatabaseCacheFile(blob).exists() && loadDatabaseCache33(channel, blob)))
				{
				File blobdir=blob.getDirectory();
				//if(EV.debugMode)
					System.out.println("Scanning for images in "+blobdir);
				if(!scanFilesChannel33(channel, blob))
					{
					//Delete this channel object if blob not found. Old recordings from the demon contains empty channels
					EvPath p=entry.getKey();
					EvContainer c=p.getParent().getObject();
					c.metaObject.remove(p.getLeafName());
					}
				}

			//Update the list of images in the imageset object
			//System.out.println("Got: "+blob.diskImageLoader33);
			for(Map.Entry<EvDecimal, HashMap<Integer,File>> fe:blob.diskImageLoader33.entrySet())
				{

				Double useResX=channel.defaultResX;
				Double useResY=channel.defaultResY;
				Double useResZ=channel.defaultResZ;

					
				
				HashMap<String,String> frameKeys=channel.metaFrame.get(fe.getKey());
				if(frameKeys==null)
					frameKeys=new HashMap<String, String>();

				//Override frame resolution
				if(frameKeys.containsKey("resX"))
					useResX=Double.parseDouble(frameKeys.get("resX"));
				if(frameKeys.containsKey("resY"))
					useResY=Double.parseDouble(frameKeys.get("resY"));
				if(frameKeys.containsKey("resZ"))
					useResZ=Double.parseDouble(frameKeys.get("resZ"));

				//Make a copy of default displacement
				Vector3d useDisp=new Vector3d(channel.defaultDisp);
/*				double useDispX=channel.defaultDispX;
				double useDispY=channel.defaultDispY;
				double useDispZ=channel.defaultDispZ;*/

				//Override for each stack
				if(frameKeys.containsKey("dispX"))
					useDisp.x=-Double.parseDouble(frameKeys.get("dispX"));
				if(frameKeys.containsKey("dispY"))
					useDisp.y=-Double.parseDouble(frameKeys.get("dispY"));
				if(frameKeys.containsKey("dispZ"))
					useDisp.z=-Double.parseDouble(frameKeys.get("dispZ"));

				EvStack stack=new EvStack();
				if(useResX==null)
					{
					System.out.println("!!!!! Resolution problem for "+blob.currentDir+" frame "+fe.getKey());
					useResX=1.0;
					useResY=1.0;
					}
				
				stack.setRes(useResX, useResY, useResZ);
				stack.setDisplacement(useDisp);
				//stack.binning=channel.chBinning;
				for(Map.Entry<Integer, File> se:fe.getValue().entrySet())
					{
					EvImagePlane evim=new EvImagePlane();
					evim.io=new SliceIO(this,getCurrentFileFor33(channel, fe.getKey(), se.getKey()));
					//System.out.println("file: "+getCurrentFileFor33(channel, fe.getKey(), se.getKey()));
					//stack.loaders.put(se.getKey(),evim);
					stack.putPlane(se.getKey(),evim);
					}

				//This avoids problems with some old recordings
				if(stack.getDepth()!=0)
					channel.putStack(fe.getKey(),stack);
				}
			}

		
		try
			{
			saveDatabaseCache33();
			}
		catch (Exception e)
			{
			System.out.println("Failed to store imagecache. continuing anyway. "+e.getMessage());
			}
		}
	

	
	
	/**
	 * Scan all files for this channel and build a database
	 */
	private boolean scanFilesChannel33(EvChannel ch, DiskBlob blob)
		{
		//Rebuild this channel in diskimageloader. note that it is totally separate from metadata
		blob.diskImageLoader33.clear();
		
		File chandir=blob.getDirectory();
		if(!chandir.exists())
			{
			System.out.println("Chan dir not found: "+chandir);
			return false;
			}
		File[] framedirs=chandir.listFiles();
		for(File framedir:framedirs)
			if(framedir.isDirectory() && !framedir.getName().startsWith("."))
				{
				EvDecimal framenum=new EvDecimal(framedir.getName());
				
				HashMap<Integer,File> loaderset=new HashMap<Integer,File>();
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
							Integer z=Integer.parseInt(partname.substring(1));

							loaderset.put(z, f);
							}
						catch (NumberFormatException e)
							{
							EvLog.printError("partname: "+partname+" filename "+f.getName()+" framenum "+framenum,e);
							System.exit(1);
							}
						}
					}
				}
		return true;
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
	

	
	public static boolean loadDatabaseCacheMap33(EvChannel ch, HashMap<EvDecimal,HashMap<Integer,File>> c, InputStream cachefile, File blobFile)
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
					HashMap<Integer,File> loaderset=new HashMap<Integer, File>();
					c.put(frame,loaderset);

					//Generate name of frame directory, optimized. windows support?
					StringBuffer framedirName=new StringBuffer(channeldirName);
					framedirName.append('/');
					EndrovUtil.pad(frame, 8, framedirName);  
					framedirName.append('/');


					for(int k=0;k<numSlice;k++)
						{
						String s=in.readLine();
						try
							{
							if(s.startsWith("ext"))
								{
								ext=s.substring(3);
								s=in.readLine();
								}
							
							StringBuffer imagefilename;
							

							int z=Integer.parseInt(s.substring(1));

							//Generate name of image file, optimized
							imagefilename=new StringBuffer(framedirName);
							imagefilename.append(s);
							imagefilename.append(ext);


							loaderset.put(z,new File(imagefilename.toString()));

							
							}
						catch (Exception e)
							{
							c.clear();
							e.printStackTrace();
							System.out.println("Bad line: "+s);
							System.out.println("Gracefully giving up on cache file");
							return false;
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
	public boolean loadDatabaseCache33(EvChannel ch, DiskBlob blob)
		{
		blob.diskImageLoader33.clear();
		try
			{
			File cacheFile=getDatabaseCacheFile(blob);
			if(EndrovCore.debugMode)
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
	public void saveDatabaseCache33() throws IOException
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
					for(Map.Entry<Integer, File> fe:blob.diskImageLoader33.get(frame).entrySet())
						{
						File imagefile=fe.getValue();
						//System.out.println("want to write down "+fe.getValue());
						String filename=imagefile.getName();
						String ext="";
						if(filename.lastIndexOf('.')!=-1)
							ext=filename.substring(filename.lastIndexOf('.'));
						if(!ext.equals(lastExt))
							{
							w.write("ext"+ext+"\n");
							lastExt=ext;
							}
						//if(filename.startsWith("b"))
							w.write(""+filename.substring(0,filename.length()-ext.length())+"\n");
						//else
							//w.write(""+fe.getKey()+"\n");
						}
					}

				w.close();
				if(EndrovCore.debugMode)
					EvLog.printLog("Wrote cache file "+cFile);
				}
		}

	
	
	/******************************************************************************************************
	 *                               Converters for upgrading                                             *
	 *****************************************************************************************************/
	
	
	/**
	 * In the past there was no z-resolution. This should be fixed! Assume there is an ok z-resolution.
	 * now there should be a plane for each z 0,1,2,3,4,5.... ints. recalculate planes, rename files.
	 * 
	 * so, this is actually done for old converted recordings!!! but not for new images from the microscope =)(/&%¤)(/&%¤!!!!
	 * 
	 * currently ostdaemon gets stacks where some images are blank; these are not to be included.
	 * 
	 * proper way from applescript: write resZ for each stack (ci...) and dispz based on skipslices. OSTdaemon can then
	 * ignore counting blank slices.  ACTUALLY. no need to write for each stack. skipslices is for the entire channel!!!!
	 * should be easy to change
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	/*
	public void fixOnePlaneForEachInt()
		{
		
		//Check directory 
		
		
		EvStack oldStack=new EvStack(); //TODO
		
		DiskBlob blob=null;
		
		//Figure out plane distance
		
		
		EvChannel ch=null;
		
		frameok: for(EvDecimal frame:ch.imageLoader.keySet())
			{
			

			
			if(oldStack.loaders.size()>1)
				{
				Iterator<EvDecimal> itz=oldStack.loaders.keySet().iterator();
				EvDecimal z0=itz.next();
				EvDecimal z1=itz.next();
				
				EvStack newStack=new EvStack();
				newStack.resX=oldStack.resX;
				newStack.resY=oldStack.resY;
				newStack.dispX=oldStack.dispX;
				newStack.dispY=oldStack.dispY;
				newStack.resZ=z1.subtract(z0);
				newStack.dispZ=z0;

				
				if(oldStack.resZ.equals(newStack.resZ) && oldStack.dispZ.equals(newStack.dispZ))
					{
					//This stack is fine!!
					continue frameok;
					}
				

				//Prepend temp_ to all names - No name collisions in later rename
				for(int i:new LinkedList<Integer>(blob.diskImageLoader33.get(frame).keySet()))
					{
					File oldfile=blob.diskImageLoader33.get(frame).get(i);
					File newfile=new File(oldfile.getParentFile(),"temp_"+oldfile.getName());
					oldfile.renameTo(newfile);
					blob.diskImageLoader33.get(frame).put(i, newfile);
					}
				

				
				int zi=0;
				for(EvDecimal oldz:oldStack.loaders.keySet())
					{
					newStack.putInt(zi,oldStack.loaders.get(oldz));
					
					Map<Integer,File> oldfilemap=blob.diskImageLoader33.get(frame);
					HashMap<Integer,File> newfilemap=new HashMap<Integer, File>();
					
					EvFileUtil.fileEnding(oldfilemap.get(oldz))
					
					newfilemap=new 
					
					
					//Rename file!!!
					//public HashMap<EvDecimal,HashMap<Integer,File>> diskImageLoader33=new HashMap<EvDecimal,HashMap<Integer,File>>();
					
					
					
					zi++;
					}
				
				
				
				
				
				}
			
			
			}
		
		
		
		
		
		}
	*/
	
	
	public static void main(String[] args)
		{
		EndrovCore.loadPlugins();
		/*
		File root=new File("/Volumes/TBU_extra03/test/");
		File f2=new File(root,"TB2111_P700_100130_stack3.ost");
		
		//TODO do an experiment first!!!!
		fix1(f2);*/
		
		/*
		for(File f:new File("/Volumes/TBU_extra03/ost3dgood").listFiles())
			if(f.getName().endsWith(".ost"))
				fix1(f);*/
		
		/*
		for(File f:new File("/Volumes/TBU_extra03/ost4dgood").listFiles())
			if(f.getName().endsWith(".ost"))
				fix1(f);
		for(File f:new File("/Volumes/TBU_extra03/tosort").listFiles())
			if(f.getName().endsWith(".ost"))
				fix1(f);
				*/
		
		
		System.exit(0);
		}
	
	/**
	 * Only for use within TBU
	 */
	/*
	public static void fix1(File ostfile)
		{		
		System.out.println("doing ----- "+ostfile);
		try
			{
			System.out.println(ostfile);
			EvData data=EvData.loadFile(ostfile);
			EvIODataOST io=(EvIODataOST)data.io;
			
			for(String channelName:new String[]{"GFP","RFP"})
				{

				//Set new resolution
				EvChannel ch=(EvChannel)data.getChild(new EvPath("im",channelName));
				File chblob=new File(ostfile, "blob-ch"+channelName);
				
				if(ch==null || !chblob.exists())
					continue;
				
				ch.defaultResZ=1.5;
				ch.defaultDisp.z=1;
				for(EvDecimal frame:ch.getFrames())
					{
					EvStack stack=ch.getStack(frame);
					stack.resZ=ch.defaultResZ;
					// stack.dispZ=ch.defaultDispZ; //need to be replaced to run again
					}
				
				if(!new File(ostfile,"converted.txt").exists())
				{
					for(File framedir:chblob.listFiles())
						if(framedir.isDirectory())
							{
							for(int oldz=0;oldz<=22;oldz++)
								{
								File oldfile=new File(framedir, "b"+EV.pad(oldz*3+2, 8)+".png");
								File newfile=new File(framedir, "b"+EV.pad(oldz, 8)+".png");
								oldfile.renameTo(newfile);
//								System.out.println(oldfile+"   ->   "+newfile);
								}
							}
					
				}
				
				//Delete cache
				File cacheFile=new File(chblob,"imagecache.txt");
				if(cacheFile.exists())
					cacheFile.delete();
				}

			//Store metadata
			io.saveMetaDataOnly(data, null);
			
			new File(ostfile,"converted.txt").createNewFile();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
		
		
		
		}
	*/
	
	public void close() throws IOException
		{
		}


	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedFileFormats.add(new EvIODataReaderWriterDeclaration(){
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
	
	}
