/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imagesetBioformats;

/*import java.awt.RenderingHints;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.RasterOp;*/
import java.io.*;
import java.util.*;

import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.PositiveInteger;

import loci.common.DataTools;
import loci.common.RandomAccessInputStream;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.*;
import loci.formats.meta.*;
import loci.formats.out.OMETiffWriter;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import endrov.data.*;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.imageset.*;
import endrov.imagesetOST.EvIODataOST;
import endrov.util.EvDecimal;
import endrov.util.Tuple;


//how to write the files:
//https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/utils/MinimumWriter.java


//metaretriever getPixelsBigEndian
//in imageraeder, int getPixelType();
//http://hudson.openmicroscopy.org.uk/job/LOCI/javadoc/loci/formats/FormatTools.html   types
//http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/utils/ConvertToOmeTiff.java;hb=HEAD


/**
 * Support for proprietary formats through LOCI Bioformats
 * 
 * @author Johan Henriksson (binding to library only)
 */
public class EvIODataBioformats implements EvIOData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final boolean debug=false;
	
	/******************************************************************************************************
	 *                               Image I/O class                                                      *
	 *****************************************************************************************************/
	
	/** Path to imageset */
	public File basedir;

	
	public IFormatReader imageReader=null;
	public IMetadata retrieve=null;
	
	/**
	 * Open a new recording
	 */
	public EvIODataBioformats(File basedir) throws Exception
		{
		this.basedir=basedir;
		}
	
	private void load(EvData d) throws Exception
		{

		imageReader=new ImageReader();
		
		//Populate OME-XML i.e. actually parse metadata
		imageReader.setOriginalMetadataPopulated(true);
		try 
			{
			ServiceFactory factory = new ServiceFactory();
			OMEXMLService service = factory.getInstance(OMEXMLService.class);
			retrieve=service.createOMEXMLMetadata(null, null);
			imageReader.setMetadataStore(retrieve);

			System.out.println("bioformats set id "+basedir);
			imageReader.setId(basedir.getAbsolutePath());
			}
		catch (DependencyException de) 
			{
			throw new Exception(de.getMessage());
			}
		catch (ServiceException se) 
			{
			throw new FormatException(se);
			}
		catch (loci.formats.FormatException fe)
			{
			System.out.println("Screwed up file? "+basedir);
			throw fe;
			}
		
		System.out.println("bioformats adding channel separator");
		imageReader=new ChannelSeparator(imageReader);
		
		/*DimensionSwapper sw=new DimensionSwapper(imageReader);
		sw.swapDimensions("XYZTC");
		imageReader=sw;*/
		
		System.out.println("bioformats building database");
		buildDatabase(d);		
		}

	public File datadir()
		{
		return basedir.getParentFile();
		}

	/**
	 * This plugin saves metadata into FILENAME.ostxml. This function constructs the name
	 * 
	 * TODO: call it bfxml instead?
	 */
	private File getMetaFile()
		{
		return new File(basedir.getParent(),basedir.getName()+".ostxml");
		}
	
	/**
	 * Save data to disk
	 */
	public void saveData(EvData d, EvData.FileIOStatusCallback cb) throws IOException
		{
		
			
			//I think this is only about how the image is currently stored in image. thus I only have to pick one mode.
			boolean isLittleEndian=false; //what is optimal?

			//TODO other formats supported too!


			//TODO find the right type


			//Do not overwrite an existing file
			if(!basedir.exists())
				{


				//TODO  use  hasDirtyChannels(d) in case file exists

				// 



				try
					{




					// record metadata to OME-XML format
					ServiceFactory factory = new ServiceFactory();
					OMEXMLService service = factory.getInstance(OMEXMLService.class);



					//Create map channel -> BFID. 
					//Currently each channel is stored separately, with each channel in its own series.
					//In the future it might be better to try and find channels with common sizes and merge them.
					//Also, it would be good if the BFID could be kept from before, for writing extra data 
					Map<EvPath, EvChannel> mapPathChannels=d.getIdObjectsRecursive(EvChannel.class);
					//channels=Collections.singletonMap(channels.entrySet().iterator().next().getKey(), channels.entrySet().iterator().next().getValue()); //Temp only one chan
					ArrayList<EvPath> evchanToId=new ArrayList<EvPath>();
					for(Map.Entry<EvPath, EvChannel> curChan:mapPathChannels.entrySet())
						{
						curChan.getValue().ostBlobID=new BFID(evchanToId.size(),0).toIDstring();
						evchanToId.add(curChan.getKey());
						}

					Set<Integer> imageIdIsJPEG=new HashSet<Integer>();
					
					
					
					System.out.println("channels "+evchanToId);

					//Create metadata for each series
					IMetadata metadata = service.createOMEXMLMetadata();

					/*
					  for(int datasetIndex=0;datasetIndex<1;datasetIndex++)
					  	{
						  metadata.setDatasetID("id"+datasetIndex, datasetIndex);
						  metadata.setDatasetName("name"+datasetIndex, datasetIndex);
						  metadata.setDatasetDescription("desc"+datasetIndex, datasetIndex);

						  //dataset group?
					  	}
					 */

					//Map<EvImage,Integer> planeIDs=new HashMap<EvImage, Integer>();


					//int sumOldPlaneId=0;
					
					//Mapping: One "bf image" per "evchannel"
					for (int imageIndex=0; imageIndex<evchanToId.size(); imageIndex++) 
						{
						EvPath pathToChan=evchanToId.get(imageIndex);
						EvChannel ch=(EvChannel)pathToChan.getObject();
						System.out.println("getting ch "+ch+" for path "+evchanToId.get(imageIndex));


						//all image in one dataset

						//Find a free name (for now, no uniqueness check)
						String imageName=pathToChan.getLeafName();

						metadata.setImageID(imageName, imageIndex);
						metadata.setImageName(imageName, imageIndex);
						//					  	metadata.setImageDatasetRef(metadata.getDatasetID(0),imageIndex,0);


						//Map frames to timeIDs
						/*
						  Map<EvDecimal,Integer> timeID=new HashMap<EvDecimal, Integer>();
						  int curTimeID=0;
						  for(EvDecimal frame:ch.getFrames())
						  	timeID.put(frame,curTimeID++);*/

						

						//Pixel format. This is the default choice
						int saveFormatType=FormatTools.INT32;
						
						
						//int saveFormatType=FormatTools.INT8;  //works
						//int saveFormatType=FormatTools.INT16;  //works
						//int saveFormatType=FormatTools.UINT16;  //does not work
						//int saveFormatType=FormatTools.INT32;   //works
						//int saveFormatType=FormatTools.DOUBLE;  //once loaded as int32!! is it caching? but it works

						//boolean isJPEG=false;
						
						//Figure out resolution and pixeltype
						int depth=0;
						int width=0;
						int height=0;
						double resX=1, resY=1, resZ=1;
						if(!ch.getFrames().isEmpty())
							{
							EvStack s=ch.getFirstStack(null);
							if(s==null)
								throw new RuntimeException("Couldn't read first stack in memory");

							System.out.println("width "+s.getWidth());
							width=s.getWidth();
							height=s.getHeight();
							depth=s.getDepth();

							resX=s.resX;
							resY=s.resY;
							resZ=s.resZ;
							
							EvImage evim=s.getFirstImage();
							if(evim.io.getRawJPEGData()!=null)
								{
								//isJPEG=true;
								imageIdIsJPEG.add(imageIndex);
								saveFormatType=FormatTools.INT8; //just invent something?

								
								
								
								//Let the JPEGs decide endianess?
								//boolean isLittleEndian=false; //what is optimal?

								}
							else
								{
								EvPixels p=evim.getPixels(null);
								if(p.getType()==EvPixelsType.DOUBLE)
									saveFormatType=FormatTools.DOUBLE;
								else if(p.getType()==EvPixelsType.FLOAT)
									saveFormatType=FormatTools.FLOAT;
								else if(p.getType()==EvPixelsType.UBYTE || p.getType()==EvPixelsType.AWT)
									saveFormatType=FormatTools.INT8;
								else if(p.getType()==EvPixelsType.SHORT)
									saveFormatType=FormatTools.INT16; 
								else
									saveFormatType=FormatTools.INT32; 
								}
							}

						//TODO verify all stacks the same size. can wait until write


						//One "image" per evchannel!
						//as many datasets as channels
						//Exception: RGB handling? to store tiffs etc, need to be able to merge


						//TODO "image" metadata, image in a dataset, nothing critical right now

						//TODO image to dataset ref
						//metadata.setImageDatasetRef(arg0, arg1, arg2)

						//image can have an optional ID and name

						//TODO pull this out of our metadata
						//metadata.setImageAcquiredDate(arg0, imageIndex);
						//metadata.setImageDescription(arg0, imageIndex);
						//metadata.setImageName(arg0, imageIndex);



						//Here we only use one channel per image. Later, multiplex
						for (int channelIndex=0; channelIndex<1; channelIndex++) 
							{
							metadata.setChannelID("ch"+channelIndex, imageIndex, channelIndex);
							metadata.setChannelName("chn"+channelIndex, imageIndex, channelIndex);
							metadata.setChannelSamplesPerPixel(new PositiveInteger(1), imageIndex, channelIndex);


							//TODO a lot of metadata for the channel


							}




						metadata.setPixelsPhysicalSizeX(new PositiveFloat(resX), imageIndex);
						metadata.setPixelsPhysicalSizeY(new PositiveFloat(resY), imageIndex);
						metadata.setPixelsPhysicalSizeZ(new PositiveFloat(resZ), imageIndex);

						metadata.setPixelsSizeX(new PositiveInteger(width), imageIndex);
						metadata.setPixelsSizeY(new PositiveInteger(height), imageIndex);
						metadata.setPixelsSizeZ(new PositiveInteger(depth), imageIndex);
						metadata.setPixelsSizeC(new PositiveInteger(1), imageIndex);
						metadata.setPixelsSizeT(new PositiveInteger(ch.getFrames().size()), imageIndex);

						System.out.println("chan: "+imageName+" xyzct "+width+" "+height+" "+depth+" "+metadata.getPixelsSizeC(imageIndex)+" "+metadata.getPixelsSizeT(imageIndex));

						
						
						
						
						metadata.setPixelsDimensionOrder(DimensionOrder.XYZCT, imageIndex);
						try
							{
							metadata.setPixelsType(PixelType.fromString(FormatTools.getPixelTypeString(saveFormatType)), imageIndex);
							}
						catch (EnumerationException e)
							{
							throw new RuntimeException(e.getMessage());
							}



						int binDataIndex=0; //hmmmmm. TODO what is this?
						metadata.setPixelsBinDataBigEndian(!isLittleEndian, imageIndex, binDataIndex);

						metadata.setPixelsID("pixelsid"+imageIndex, imageIndex);

						//For each XY-plane
						int curEvFrameID=0;
						for(EvDecimal frame:ch.getFrames())
							{
							for(int curEvZ=0;curEvZ<depth;curEvZ++)
								{
								int planeIndex=curEvFrameID*depth+curEvZ;
								//int planeIndex=sumOldPlaneId;
								//sumOldPlaneId++;


								metadata.setPlaneDeltaT(frame.doubleValue(), imageIndex, planeIndex);
								//metadata.setPlaneExposureTime(1.0, imageIndex, planeIndex);   //TODO

								//Position with image
								metadata.setPlaneTheC(new NonNegativeInteger(0), imageIndex, planeIndex);
								metadata.setPlaneTheZ(new NonNegativeInteger(curEvZ), imageIndex, planeIndex);
								metadata.setPlaneTheT(new NonNegativeInteger(curEvFrameID), imageIndex, planeIndex);

								//TODO stage information
								/*
								  metadata.setPlanePositionX(0.0, imageIndex, planeIndex);
								  metadata.setPlanePositionY(0.0, imageIndex, planeIndex);
								  metadata.setPlanePositionZ(0.0, imageIndex, planeIndex);
								 */
								}
							curEvFrameID++;
							}


						}


					System.out.println("size "+metadata.getPixelsSizeX(0));
					System.out.println("imagecount "+metadata.getImageCount());

					MetadataTools.verifyMinimumPopulated(metadata);


					// Overview
					// http://www.ome-xml.org/wiki/CompliantSpecification
					// THE file to understand how to write metadata:
					// http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/meta/MetadataConverter.java;h=3780d383239aa519be1ae149d3a875825dfd03ef;hb=HEAD


					//Delete old file. TODO. can continue writing on it!
					System.out.println("deleting "+basedir);
					basedir.delete();


					//Set up writers. Set appropriate settings
					ImageWriter writer = new ImageWriter();
					writer.setWriteSequentially(true); //This has to be set here. Not later!
					writer.getWriter(OMETiffWriter.class).setCompression(TiffWriter.COMPRESSION_LZW);
					((OMETiffWriter)writer.getWriter(OMETiffWriter.class)).setBigTiff(true);

					
					writer.setMetadataRetrieve(metadata);
					writer.setId(basedir.getAbsolutePath());

					if(writer.getWriter() instanceof OMETiffWriter)
						{
						System.out.println("This is OME-TIFF");
						//OMETiffWriter ome=(OMETiffWriter)writer.getWriter();
	
						//ome.setCompression(TiffWriter.COMPRESSION_LZW);
						//ome.setBigTiff(true); 
						}
					else
						imageIdIsJPEG.clear(); //Do not attempt to write JPEGs
						
					
					
					//Start writing
					writer.setInterleaved(false);
					//writer.setCompression("J2K");

					//Use BIGTIFF if possible. Later this will not be needed
					

					//Write all the series
					System.out.println("-------------- writing image data -------------------------, #series: "+evchanToId.size());
					//sumOldPlaneId=0;
					for(int imageIndex=0;imageIndex<evchanToId.size();imageIndex++)
						{
						writer.setSeries(imageIndex);

						EvChannel ch=(EvChannel)evchanToId.get(imageIndex).getObject();
						int depth=ch.getStack(ch.getFirstFrame()).getDepth();

						System.out.println("series: "+imageIndex+" ch: "+ch);

//////////////// before the first write here, it seeks to the beginning. and overwrites. why?						
						
						//For each frame
						int curFrameID=0;
						for(EvDecimal frame:ch.getFrames())
							{
							//Write a JPEG stack. Only if input data is JPEG, and the output supports JPEG
							if(imageIdIsJPEG.contains(imageIndex))
								{
								//For each z
								EvStack s=ch.getStack(frame);
								for (int curZ=0; curZ<depth; curZ++) 
									{
	//								int planeID=sumOldPlaneId;
//									sumOldPlaneId++;
									int planeID=curFrameID*depth+curZ;

//									System.out.println("writing jpeg ch:"+evchanToId.get(imageIndex)+" frame:"+frame+" z:"+curZ+" planeID:"+planeID);
									EvImage evim=s.getInt(curZ);
									if(evim==null)
										throw new IOException("Missing plane "+curZ +" at frame "+frame);
									
									File jpegFile=evim.io.getRawJPEGData();
									
									if(jpegFile==null)
										throw new IOException("Expected all planes for one channel to be jpeg, if any. Not found: "+frame+" z:"+curZ);
									
									RandomAccessInputStream in = new RandomAccessInputStream(jpegFile.getAbsolutePath());
							    byte[] jpegBytes = new byte[(int) in.length()];
							    in.readFully(jpegBytes);
							    in.close();
									
							    ImageReader readerJPEG = new ImageReader();
								  readerJPEG.setId(jpegFile.getAbsolutePath());
								  
								  OMETiffWriter ome=(OMETiffWriter)writer.getWriter();
								  
								  
								  try
										{
										ome.saveJPEG(planeID, jpegBytes, 
												readerJPEG.getSizeX(), readerJPEG.getSizeY(), 
												readerJPEG.isLittleEndian(), readerJPEG.getPixelType(), readerJPEG.getRGBChannelCount());
										}
									catch (Exception e)
										{
										throw new IOException("Error storing frame: "+frame+" plane: "+curZ+" - "+e.getMessage());
										}
								  
								  
									}
								}
							else
								{
								//Write a non-JPEG stack
								int binDataIndex=0;
								boolean littleEndian = !metadata.getPixelsBinDataBigEndian(imageIndex, binDataIndex).booleanValue();


								//boolean isSigned = pixelType == PixelType.INT8 || pixelType == PixelType.INT16 || pixelType == PixelType.INT32;

								//For each z
								EvStack s=ch.getStack(frame);
								for (int curZ=0; curZ<depth; curZ++) 
									{
									int planeID=curFrameID*depth+curZ;
									//int planeID=sumOldPlaneId;
									//sumOldPlaneId++;

//									System.out.println("writing ch:"+evchanToId.get(imageIndex)+" frame:"+frame+" z:"+curZ+" planeID:"+planeID);

									PixelType pixelType=metadata.getPixelsType(imageIndex);
									int formatType = FormatTools.pixelTypeFromString(pixelType.getValue());

									
									//boolean signed=FormatTools.isSigned(formatType);

									EvImage evim;
									try
										{
										evim=s.getInt(curZ);
										if(evim==null)
											throw new IOException("Plane is null");
										}
									catch (Exception e)
										{
										throw new IOException("Failed to get slice "+curZ+" frame "+frame);
										}
									
									
									//Get and convert to bytes in the specified format, given evpixel format
									EvPixels p;
									try
										{
										p=evim.getPixels(null);
										}
									catch (Exception e)
										{
										throw new IOException("Failed to get slice "+curZ+" frame "+frame+" due to null");
										}
									byte[] plane=null;
									if(formatType==FormatTools.FLOAT)
										{
										//TODO: mystery. why does it not work? or does it?
										plane=DataTools.floatsToBytes(p.convertToFloat(true).getArrayFloat(), littleEndian);
										
//										System.out.println("pixel type DOUBLE");
										
										}
									else if(formatType==FormatTools.DOUBLE)
										{
										//TODO: mystery. why does it not work? or does it?
										plane=DataTools.doublesToBytes(p.convertToDouble(true).getArrayDouble(), littleEndian);
										
//										System.out.println("pixel type DOUBLE");
										
										}
									else if(formatType==FormatTools.INT16)
										{
										short[] arr;

										arr=p.convertToShort(true).getArrayShort();
										arr = DataTools.makeSigned(arr); 

										if(debug)
											{
											System.out.println("save pixel type INT16:");
							  			for(int b:arr)
							  				System.out.print(b+",");
							  			System.out.println();
											}

										plane=DataTools.shortsToBytes(arr, littleEndian);
										}
									else if(formatType==FormatTools.UINT16)   //This does not work yet!
										{
										short[] arr;

										arr=p.convertToShort(true).getArrayShort();
										arr = DataTools.makeSigned(arr); ///???? 

										if(debug)
											{
											System.out.println("save pixel type UINT16:");
							  			for(int b:arr)
							  				System.out.print(b+",");
							  			System.out.println();
											}

										plane=DataTools.shortsToBytes(arr, littleEndian);
										}
									else if(formatType==FormatTools.INT32)
										{
										int[] arr;

										arr=p.convertToInt(true).getArrayInt();
										arr = DataTools.makeSigned(arr);  //TODO good?
										if(debug)
											{
											System.out.println("save pixel type INT32:");
							  			for(int b:arr)
							  				System.out.print(b+",");
							  			System.out.println();
											}

										plane=DataTools.intsToBytes(arr, littleEndian); //TODO !
										}
									else if(formatType==FormatTools.INT8)
										{
										byte[] arr;
										/*if(signed)
							  				{
								  			arr=p.convertToUByte(false).getArrayUnsignedByte();
								  			makeUnSigned(arr);
							  				}
							  			else*/
										arr=p.convertToUByte(true).getArrayUnsignedByte();
										plane=arr;
										
//										System.out.println("pixel type INT8");

										}
									
//TODO: UINT8									
									
									else
										throw new RuntimeException("Unsupported format in bf writer - (bug). format: "+formatType);


									/*
							  		System.out.println("islittleendian "+littleEndian);
						  			for(int b:plane)
						  				System.out.print(b+",");
						  			System.out.println();
									 */
									
									//TODO use MetadataTools.createLSID(arg0, arg1) to create IDs

									//TODO Have a look at DateTools.
									
									writer.saveBytes(planeID, plane);
									}
								
								}
							
							
							

							curFrameID++;
							}

						curFrameID++;
						}


					writer.close();
					}
				catch (Exception e)
					{
					e.printStackTrace();
					System.out.println("deleting incomplete file "+basedir);
					basedir.delete();
					
					throw new IOException("Error storing "+basedir+", "+e.getMessage());
					}





				}


				
			/*
			else if(basedir.getName().endsWith(".jpg") || basedir.getName().endsWith(".jpeg") || basedir.getName().endsWith(".png"))
				{
				String fileEnding=EvFileUtil.fileEnding(basedir);
				
				//TODO use ImageIO
				
				Map<EvPath, EvChannel> channels=d.getIdObjectsRecursive(EvChannel.class);
				EvChannel ch=channels.values().iterator().next();
				EvStack stack=ch.getFirstStack(null);
				EvImage evim=stack.getInt(0);
				if(evim.isDirty)
					{
					ImageWriter wr=new ImageWriter();
					
					
					JPEGWriter wr=new JPEGWriter();
					
					ImageIO.write(im, fileEnding, basedir);
					evim.isDirty=false;
					}
				
				
				
				}*/
			
			
			
			
			EvIODataOST.saveMeta(d, getMetaFile());
			d.setMetadataNotModified();
			
		}
	

	/*
	public static byte[] makeUnSigned(byte[] b) {
	    for (int i=0; i<b.length; i++) {
	      b[i] = (byte) (b[i] - 128);
		    }
		    return b;
		  }
		
		  public static short[] makeUnSigned(short[] s) {
		    for (int i=0; i<s.length; i++) {
		      s[i] = (short) (s[i] - 32768);
		    }
		    return s;
		  }
		
		  public static int[] makeUnSigned(int[] i) {
		    for (int j=0; j<i.length; j++) {
		      i[j] = (int) (i[j] - 2147483648L);
		    }
		    return i;
		  }*/
	
	
	/**
	 * Check if there is a channel that need be written. Generated data is not written.
	 */
	public boolean hasDirtyChannels(EvContainer d)
		{
		for(EvChannel ch:d.getIdObjectsRecursive(EvChannel.class).values())
			if(!ch.isGeneratedData && ch.isDirty())
				return true;
		return false;
		}
		  
	
	
	private boolean hasBFIDmapping(EvData d)
		{
		for(EvChannel ch:d.getObjects(EvChannel.class))
			if(ch.ostBlobID!=null && ch.ostBlobID.startsWith("bf:"))
				return true;
		return false;
		}
	
	
	private static class BFID
		{
		int series;
		int color;
		
		public BFID(int series, int color)
			{
			super();
			this.series = series;
			this.color = color;
			}

		public String toIDstring()
			{
			return "bf:"+series+":"+color;
			}
		}
	
	private static BFID parseBFID(String bfid) 
		{
		if(bfid==null)
			return null;
		StringTokenizer stok=new StringTokenizer(bfid, ":");
		if(stok.hasMoreTokens())
			{
			String first=stok.nextToken();
			if(first.equals("bf"))
				{
				String series=stok.nextToken();
				String color=stok.nextToken();
				return new BFID(
						Integer.parseInt(series),
						Integer.parseInt(color));
				}
			}
		return null;
		}
	
	/**
	 * Scan recording for channels and build a file database
	 */
	public void buildDatabase(EvData d)
		{
		//Load metadata from added OSTXML-file. This has to be done first or all image loaders are screwed
		boolean generateBfidMapping=true;
		File metaFile=getMetaFile();
		if(metaFile.exists())
			{
			d.loadXmlMetadata(metaFile);
			if(!hasBFIDmapping(d))
				generateBfidMapping=false;
			}

		
		//Generate channel mappings
		if(generateBfidMapping)
			{
			System.out.println("No BF-ID mapping to channels - creating new channels");
			
			for(int seriesIndex=0;seriesIndex<imageReader.getSeriesCount();seriesIndex++)
				{
				//Setting series will re-populate the metadata store as well
				imageReader.setSeries(seriesIndex);
				System.out.println("bioformats looking at series "+seriesIndex);
				
				String imsetName=retrieve.getImageName(seriesIndex);
				System.out.println("-------------- got image name "+imsetName);
				if(imsetName==null)
					imsetName="im"+seriesIndex;
				else
					{
					//On windows, bio-formats uses the entire path. This is ugly so cut off the part until the last file 
					//if(imsetName.contains("\\"))
					//	imsetName=imsetName.substring(imsetName.lastIndexOf('\\'));
					}

				if(imsetName.equals(""))
					imsetName="im"+seriesIndex;

				Imageset imset=(Imageset)d.metaObject.get(imsetName);
				if(imset==null)
					d.metaObject.put(imsetName, imset=new Imageset());
				for(String s:new LinkedList<String>(imset.getChannels().keySet()))
					{
					//TODO Keep metaobjects below channel?
					imset.metaObject.remove(s);
					}
				
				int sizeC=retrieve.getPixelsSizeC(seriesIndex).getValue();
				
				for(int curC=0;curC<sizeC;curC++)
					{
					//Figure out name of channel
					String chanName=null;
					
					try
						{
						chanName=retrieve.getChannelName(seriesIndex, curC);
						}
					catch (Exception e1)
						{
						//Ugly hack! report to bioformats
						e1.printStackTrace();
						}
					
					
					if(chanName==null)
						chanName="ch"+curC;

					EvChannel ch=imset.getCreateChannel(chanName);
					
					//Generate the BFID
					String bfid="bf:"+seriesIndex+":"+curC;
					ch.ostBlobID=bfid;
					}
			
				}
			}
		

		//Populate channels
		for(Map.Entry<EvPath, EvChannel> che:d.getIdObjectsRecursive(EvChannel.class).entrySet())
			{
			EvChannel ch=che.getValue();
			BFID bfid=parseBFID(ch.ostBlobID);
			if(bfid!=null)
				{
				imageReader.setSeries(bfid.series);

				int sizeT=retrieve.getPixelsSizeT(bfid.series).getValue();
				int sizeZ=retrieve.getPixelsSizeZ(bfid.series).getValue();
				//	System.out.println("im: "+imsetName+" ch: "+chanName+" zct: "+sizeZ+" "+sizeC+" "+sizeT);
				for(int curT=0;curT<sizeT;curT++)
					{

					
					PositiveFloat resXf=retrieve.getPixelsPhysicalSizeX(bfid.series); //[um/px]
					PositiveFloat resYf=retrieve.getPixelsPhysicalSizeY(bfid.series); //[um/px]
					PositiveFloat resZf=retrieve.getPixelsPhysicalSizeZ(bfid.series); //[um/px]
					Double resX=1.0;
					Double resY=1.0;
					Double resZ=1.0;
					if(resXf!=null && resXf.getValue()!=0) resX=resXf.getValue();
					if(resYf!=null && resYf.getValue()!=0) resY=resYf.getValue();
					if(resZf!=null && resZf.getValue()!=0) resZ=resZf.getValue();

					//Calculate which frame this is. Note that we only consider the time of the first plane!
					EvDecimal frame=null;
					//Double timeIncrement=retrieve.getPixelsTimeIncrement(imageIndexFirstPlane);   
					Double timeIncrement=retrieve.getPixelsTimeIncrement(bfid.series);
					if(timeIncrement!=null)
						//Time increment [s] is optional
						frame=new EvDecimal(curT*timeIncrement);
					else
						{
						frame=new EvDecimal(curT);

						//Time since beginning of experiment [s] is optional
						//Double deltaT=retrieve.getPlaneDeltaT(imageIndexFirstPlane, 0);
						try
							{
							//Double deltaT=retrieve.getPlaneDeltaT(bfid.series, imageReader.getIndex(bfid.series, bfid.color, curT));
							Double deltaT=retrieve.getPlaneDeltaT(bfid.series, imageReader.getIndex(0, bfid.color, curT));               //TODO verify!!!
							if(deltaT!=null)
								frame=new EvDecimal(deltaT);
							}
						catch (Exception e)
							{
							System.out.println("Failed to call getPlaneDeltaT, "+e.getMessage());
							}
						}
					

					

					boolean isDicom=imageReader.getFormat().equals("DICOM");//imageReader instanceof DicomReader;
					//System.out.println("isdicom "+isDicom+" "+imageReader.getFormat());

					//Create stack
					EvStack stack=new EvStack();
					ch.putStack(frame, stack);
					stack.setRes(resX,resY,resZ);

					//Fill stack with planes
					for(int curZ=0;curZ<sizeZ;curZ++)
						{
						EvImage evim=new EvImage();
						evim.io=new BioformatsSliceIO(imageReader, bfid.series, imageReader.getIndex(curZ, bfid.color, curT), basedir, false);
						if(isDicom)
							((BioformatsSliceIO)evim.io).isDicom=true;
						stack.putInt(curZ, evim);
						}
					}


				}
			else
				{
				//Artifact from old bad implementations. Delete this channel
				EvPath path=che.getKey();
				System.out.println("Discarding artifact channel "+path);
				path.getParent().getObject().removeMetaObjectByValue(ch);
				}
			
			}
		
		
		// http://hudson.openmicroscopy.org.uk/job/LOCI/javadoc/
		
		}


	public RecentReference getRecentEntry()
		{
		return new RecentReference(getMetadataName(), basedir.getPath());
		}

	public String getMetadataName()
		{
		String imageset=basedir.getName();
		return imageset;
		}

	

	@Override
	protected void finalize() throws Throwable
		{
		super.finalize();
		if(imageReader!=null)
			{
			//System.out.println("Closed eviodatabioformats for "+basedir);
			imageReader.close();
			imageReader=null;
			}
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedFileFormats.add(new EvDataSupport(){
			public Integer loadSupports(String fileS)
				{
				//ImageReader r=new ImageReader();
				//r.setId(fileS);

				
				//ImageReader r=new ImageReader(); //Possible to get all suffixes and match
				
				File file=new File(fileS);
				return file.isFile() ? 100 : null; //Low priority; need to find a way to check extensions
				}
			
			
			public List<Tuple<String,String[]>> getLoadFormats()
				{
				ImageReader r=new ImageReader();
				//TreeSet<String> sufs=new TreeSet<String>();
				LinkedList<Tuple<String,String[]>> formats=new LinkedList<Tuple<String,String[]>>(); 
				for(IFormatHandler h:r.getReaders())
					{
					/*
					StringBuffer sb=new StringBuffer();
					sb.append(h.getFormat()+" (");
					boolean first=true;
					for(String suf:h.getSuffixes())
						{
						sufs.add(suf);
						if(!first)
							sb.append(", ");
						first=false;
						sb.append(suf);
						}
					sb.append(")");*/
					formats.add(new Tuple<String,String[]>(h.getFormat(),h.getSuffixes()));
					}				
				return formats;
				}
			
			
			public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception
				{
				if(!new File(file).exists())
					throw new Exception("File does not exist");
				EvData d=new EvData();
				EvIODataBioformats io=new EvIODataBioformats(new File(file));
				io.load(d);
				d.io=io;
				
				return d;
				}
			
			
			public Integer saveSupports(String file)
				{
				LinkedList<Tuple<String,String[]>> formats=new LinkedList<Tuple<String,String[]>>(); 
				Set<String> suffixes=new HashSet<String>();
				for(IFormatHandler h:new ImageWriter().getWriters())
					{
					formats.add(new Tuple<String,String[]>(h.getFormat(),h.getSuffixes()));
					for(String s:h.getSuffixes())
						suffixes.add(s);
					}
				
				for(String s:suffixes)
					if(file.endsWith(s))
						return 100;
				return null;
				}
			
			
			public List<Tuple<String,String[]>> getSaveFormats()
				{
				LinkedList<Tuple<String,String[]>> formats=new LinkedList<Tuple<String,String[]>>(); 
				for(IFormatHandler h:new ImageWriter().getWriters())
					formats.add(new Tuple<String,String[]>(h.getFormat(),h.getSuffixes()));
				return formats;
				}
				
				
			public EvIOData getSaver(EvData d, String file) throws IOException
				{
				try
					{
					EvIODataBioformats io;
					io = new EvIODataBioformats(new File(file));
					return io;
					}
				catch (Exception e)
					{
					e.printStackTrace();
					throw new IOException(e.getMessage());
					}
				}
		});
		}

	
	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
	
		/*
		
		EvData d=EvData.loadFile(new File("/home/tbudev3/test.png"));
		
		try
			{
			d.saveDataAs(new File("/home/tbudev3/foo.ome"));
			d.saveDataAs(new File("/home/tbudev3/foo.ome.tiff"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
*/
//		EvData d=EvData.loadFile(new File("/Volumes/TBU_main06/ost3dgood/A12D51070814.ost"));
	
		
		EvData d=new EvData();
		Imageset imset=new Imageset();
		d.metaObject.put("im", imset);
		EvChannel ch=imset.getCreateChannel("ch");
		
		EvStack stack=new EvStack();
		ch.putStack(EvDecimal.ZERO, stack);
		
		int w=30;
		int h=20;
		int depth=2;
		
		
		/*
		stack.allocate(w, h, depth, EvPixelsType.UBYTE, null);
		byte[] arr=stack.getInt(0).getPixels(null).getArrayUnsignedByte();
		for(int ax=0;ax<w;ax++)
			for(int ay=0;ay<h;ay++)
				arr[ay*w+ax]=(byte)((ax+ay)%100);
		*/
		
		/*
		stack.allocate(w, h, depth, EvPixelsType.INT, null);
		int[] arr=stack.getInt(0).getPixels(null).getArrayInt();
		for(int ax=0;ax<w;ax++)
			for(int ay=0;ay<h;ay++)
				arr[ay*w+ax]=((ax+ay)%1000);
				*/
		
		stack.allocate(w, h, depth, EvPixelsType.DOUBLE, null);
		double[] arr=stack.getInt(0).getPixels(null).getArrayDouble();
		for(int ax=0;ax<w;ax++)
			for(int ay=0;ay<h;ay++)
				arr[ay*w+ax]=((ax+ay)%1000);
		
		
		
		try
			{
			//File out=new File("/home/tbudev3/temp/A12D51070814_double.ome.tiff");
			File out=new File("/home/tbudev3/temp/test.ome.tiff");
			out.delete();
			d.saveDataAs(out);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		

		
		System.exit(0);
		
		
		}
	
	}
