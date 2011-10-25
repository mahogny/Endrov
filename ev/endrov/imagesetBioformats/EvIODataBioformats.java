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
	public void saveData(EvData d, EvData.FileIOStatusCallback cb)
		{
		try
			{
			
			


			//PixelType pixelType=PixelType.DOUBLE;
			//PixelType pixelType=PixelType.INT32;
			//PixelType pixelType=PixelType.INT8;

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



					//Create map channel -> series ID
					Map<EvPath, EvChannel> channels=d.getIdObjectsRecursive(EvChannel.class);
					//channels=Collections.singletonMap(channels.entrySet().iterator().next().getKey(), channels.entrySet().iterator().next().getValue()); //Temp only one chan
					ArrayList<EvPath> evchanToId=new ArrayList<EvPath>();
					for(EvPath curChan:channels.keySet())
						evchanToId.add(curChan);

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

						//Figure out resolution
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

						int formatType=FormatTools.INT32;

						
						metadata.setPixelsDimensionOrder(DimensionOrder.XYZCT, imageIndex);
						try
							{
							metadata.setPixelsType(PixelType.fromString(FormatTools.getPixelTypeString(formatType)), imageIndex);
							}
						catch (EnumerationException e)
							{
							throw new RuntimeException(e.getMessage());
							}


						boolean isLittleEndian=false; //what is optimal?

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



					ImageWriter writer = new ImageWriter();


					//Delete old file. TODO. can continue writing on it!
					System.out.println("deleting "+basedir);
					basedir.delete();

					//Start writing
					writer.setMetadataRetrieve(metadata);
					writer.setId(basedir.getAbsolutePath());
					writer.setInterleaved(false);
					//writer.setCompression("J2K");

					//Use BIGTIFF if possible. Later this will not be needed
					
					if(writer.getWriter() instanceof OMETiffWriter)
						{
						System.out.println("This is OME-TIFF");
						OMETiffWriter ome=(OMETiffWriter)writer.getWriter();
						//ome.setBigTiff(true);
						ome.setCompression(TiffWriter.COMPRESSION_LZW);
						}

					//Write all the series
					System.out.println("-------------- writing image data ------------------------- "+evchanToId.size());
					for(int imageIndex=0;imageIndex<evchanToId.size();imageIndex++)
						{
						writer.setSeries(imageIndex);

						EvChannel ch=(EvChannel)evchanToId.get(imageIndex).getObject();
						int depth=ch.getStack(ch.getFirstFrame()).getDepth();

						//For each frame
						int curFrameID=0;
						for(EvDecimal frame:ch.getFrames())
							{
							int binDataIndex=0;
							boolean littleEndian = !metadata.getPixelsBinDataBigEndian(imageIndex, binDataIndex).booleanValue();


							//boolean isSigned = pixelType == PixelType.INT8 || pixelType == PixelType.INT16 || pixelType == PixelType.INT32;

							//For each z
							EvStack s=ch.getStack(frame);
							for (int curZ=0; curZ<depth; curZ++) 
								{
								int planeID=curFrameID*depth+curZ;

								System.out.println("writing ch:"+evchanToId.get(imageIndex)+" frame:"+frame+" z:"+curZ+" planeID:"+planeID);

								PixelType pixelType=metadata.getPixelsType(imageIndex);
								int formatType = FormatTools.pixelTypeFromString(pixelType.getValue());

								
								boolean signed=FormatTools.isSigned(formatType);

								//Get and convert to bytes in the specified format, given evpixel format
								EvPixels p=s.getInt(curZ).getPixels(null);
								byte[] plane=null;
								if(formatType==FormatTools.DOUBLE)
									{
									//TODO: mystery. why does it not work? or does it?
									plane=DataTools.doublesToBytes(p.convertToDouble(true).getArrayDouble(), littleEndian);
									}
								else if(formatType==FormatTools.INT32)
									{
									int[] arr;
									/*if(signed)
						  				{
							  			arr=p.convertToInt(false).getArrayInt();
							  			makeUnSigned(arr);
						  				}
						  			else*/
									arr=p.convertToInt(true).getArrayInt();

									/*
						  			for(int b:arr)
						  				System.out.print(b+",");
						  			System.out.println();
									 */

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
									}
								else
									throw new RuntimeException("Unsupported format in bf writer - bug");


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

							curFrameID++;
							}

						curFrameID++;
						}


					writer.close();
					}
				catch (DependencyException e)
					{
					e.printStackTrace();
					}
				catch (ServiceException e)
					{
					e.printStackTrace();
					}
				catch (FormatException e)
					{
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		catch (IOException e)
			{
			e.printStackTrace();
			}
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
		  
	/**
	 * Scan recording for channels and build a file database
	 */
	public void buildDatabase(EvData d)
		{
		//Load metadata from added OSTXML-file. This has to be done first or all image loaders are screwed
		File metaFile=getMetaFile();
		if(metaFile.exists())
			d.loadXmlMetadata(metaFile);

		System.out.println("#series "+imageReader.getSeriesCount());
		
		//HashSet<String> usedImsetNames=new HashSet<String>();
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

			
			
//			if(d.metaObject.containsKey(imsetName))
			//if(usedImsetNames.contains(imsetName)) //In case channel already exist in XML, do not overwrite it
				//imsetName="im-"+imageName;
			//usedImsetNames.add(imsetName);
			
			
			Imageset imset=(Imageset)d.metaObject.get(imsetName);
			if(imset==null)
				d.metaObject.put(imsetName, imset=new Imageset());
			for(String s:new LinkedList<String>(imset.getChannels().keySet()))
				{
				//TODO Keep metaobjects below channel?
				imset.metaObject.remove(s);
				}

			
			
			int sizeT=retrieve.getPixelsSizeT(seriesIndex).getValue();
			int sizeZ=retrieve.getPixelsSizeZ(seriesIndex).getValue();
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

				System.out.println("im: "+imsetName+" ch: "+chanName+" zct: "+sizeZ+" "+sizeC+" "+sizeT);

				for(int curT=0;curT<sizeT;curT++)
					{
					
					//int imageIndexFirstPlane=imageReader.getIndex(0, curC, curT);
					//System.out.println("index of first plane for this time point "+imageIndexFirstPlane);
					
					//Read resolution
					//Note: values are optional!!!
					/*
					Double resX=retrieve.getPixelsPhysicalSizeX(imageIndexFirstPlane); //[um/px]
					Double resY=retrieve.getPixelsPhysicalSizeY(imageIndexFirstPlane); //[um/px]
					Double resZ=retrieve.getPixelsPhysicalSizeZ(imageIndexFirstPlane); //[um/px]*/
					
					PositiveFloat resXf=retrieve.getPixelsPhysicalSizeX(0); //[um/px]
					PositiveFloat resYf=retrieve.getPixelsPhysicalSizeY(0); //[um/px]
					PositiveFloat resZf=retrieve.getPixelsPhysicalSizeZ(0); //[um/px]
					Double resX=1.0;
					Double resY=1.0;
					Double resZ=1.0;
					if(resXf!=null && resXf.getValue()!=0) resX=resXf.getValue();
					if(resYf!=null && resYf.getValue()!=0) resY=resYf.getValue();
					if(resZf!=null && resZf.getValue()!=0) resZ=resZf.getValue();

					//Calculate which frame this is. Note that we only consider the time of the first plane!
					EvDecimal frame=null;
					//Double timeIncrement=retrieve.getPixelsTimeIncrement(imageIndexFirstPlane);   
					Double timeIncrement=retrieve.getPixelsTimeIncrement(0);
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
							Double deltaT=retrieve.getPlaneDeltaT(seriesIndex, imageReader.getIndex(0, curC, curT));
							if(deltaT!=null)
								frame=new EvDecimal(deltaT);
							}
						catch (Exception e)
							{
							System.out.println("Failed to call getPlaneDeltaT");
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
						evim.io=new BioformatsSliceIO(imageReader, seriesIndex, imageReader.getIndex(curZ, curC, curT), basedir, false);
						if(isDicom)
							((BioformatsSliceIO)evim.io).isDicom=true;
						stack.putInt(curZ, evim);
						}
					
					
		
					}
				
				
				
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
		EvLog.listeners.add(new EvLogStdout());
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
		EvData d=EvData.loadFile(new File("/Volumes/TBU_main06/ost3dgood/A12D51070814.ost"));
		
		try
			{
			File out=new File("/home/tbudev3/temp/A12D51070814_double.ome.tiff");
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
