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
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.PositiveInteger;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.*;
import loci.formats.in.OMETiffReader;
import loci.formats.meta.*;
import loci.formats.out.OMETiffWriter;
import loci.formats.out.OMEXMLWriter;
import loci.formats.services.OMEXMLService;
import endrov.data.*;
import endrov.imageset.*;
import endrov.imagesetOST.EvIODataOST;
import endrov.util.EvDecimal;
import endrov.util.Tuple;


//how to write the files:
//https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/utils/MinimumWriter.java


//metaretriever getPixelsBigEndian
//in imageraeder, int getPixelType();
//http://hudson.openmicroscopy.org.uk/job/LOCI/javadoc/loci/formats/FormatTools.html   types

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
			throw new MissingLibraryException(OMETiffReader.NO_OME_XML_MSG, de);
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
			
			//TODO other formats supported too!
			
			if(basedir.getName().endsWith(".ome.tif"))  //and .ome.tiff
				{
				if(!basedir.exists())
					{

					
					
					// http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/utils/ConvertToOmeTiff.java;hb=HEAD
					
					/*
					
					try
						{
						ImageWriter writer = new ImageWriter();
						
				
//						OMETiffWriter writer = new OMETiffWriter();
	//				  writer.setBigTiff(true);

				


					  // record metadata to OME-XML format
					  ServiceFactory factory = new ServiceFactory();
					  OMEXMLService service = factory.getInstance(OMEXMLService.class);
					  
					  

						//Create map channel -> series ID
					  Map<EvPath, EvChannel> channels=d.getIdObjectsRecursive(EvChannel.class);
					  ArrayList<EvPath> chanToId=new ArrayList<EvPath>();
					  for(EvPath curChan:channels.keySet())
					  	chanToId.add(curChan);
					  
					  //Create metadata for each series
					  IMetadata metadata = service.createOMEXMLMetadata();
					  
					  int datasetIndex=0;
					  metadata.setDatasetID("id0", datasetIndex);
					  metadata.setDatasetName("name0", datasetIndex);
					  metadata.setDatasetDescription("desc0", datasetIndex);
					  
					  
					  for (int imageIndex=0; imageIndex<imageCount; imageIndex++) 
					  	{
					  	//One "image" per channel!
					  	//as many datasets as channels
					  	//Exception: RGB handling? to store tiffs etc, need to be able to merge
					  	
					  	
					  //TODO "image" metadata, image in a dataset, nothing critical right now

					  	//TODO image to dataset ref
					  	//metadata.setImageDatasetRef(arg0, arg1, arg2)

					  	//image can have an optional ID and name
					  	
					  	
						  //metadata.setImageAcquiredDate(arg0, imageIndex);
						  //metadata.setImageDescription(arg0, imageIndex);
						  //metadata.setImageName(arg0, imageIndex);
						  

					  	
					  	//channelcount for a given imageIndex
					  	for (int channelIndex=0; channelIndex<1; channelIndex++) 
					  		{
					  	
					  		//TODO a lot of metadata for the channel
					  		
					  		
					  		
					  		}
					  	
					  	
					  	
					  	
					  	metadata.setPixelsPhysicalSizeX(1.0, imageIndex);
					  	metadata.setPixelsPhysicalSizeY(1.0, imageIndex);
					  	metadata.setPixelsPhysicalSizeZ(1.0, imageIndex);
					  	
					  	metadata.setPixelsSizeX(new PositiveInteger(500), imageIndex);
					  	metadata.setPixelsSizeY(new PositiveInteger(500), imageIndex);
					  	metadata.setPixelsSizeZ(new PositiveInteger(500), imageIndex);
					  	metadata.setPixelsSizeC(new PositiveInteger(1), imageIndex);
					  	metadata.setPixelsSizeT(new PositiveInteger(500), imageIndex);
					  
						  metadata.setPixelsDimensionOrder(DimensionOrder.XYZCT, imageIndex);
						  
						  metadata.setPixelsType(PixelType.DOUBLE, imageIndex);
						  
						  for(int planeIndex=0;planeIndex<5;planeIndex++)
						  	{
						  	//Each XY-plane ....
						  	
						  	
							  metadata.setPlaneDeltaT(1.0, imageIndex, planeIndex);
							  metadata.setPlaneExposureTime(1.0, imageIndex, planeIndex);
							  
							  //Position with image
							  metadata.setPlaneTheC(new NonNegativeInteger(0), imageIndex, planeIndex);
							  metadata.setPlaneTheZ(new NonNegativeInteger(0), imageIndex, planeIndex);
							  metadata.setPlaneTheT(new NonNegativeInteger(0), imageIndex, planeIndex);
							  
							  //For stage
							  metadata.setPlanePositionX(1.0, imageIndex, planeIndex);
							  metadata.setPlanePositionY(1.0, imageIndex, planeIndex);
							  metadata.setPlanePositionZ(1.0, imageIndex, planeIndex);
							  
							  
							  
						  	}
						  
						  
						  }
						  
					  
					  
					  
					  
					  
					  //TODO fill in metadata
					  

					  // Overview
					  // http://www.ome-xml.org/wiki/CompliantSpecification
					  // THE file to understand how to write metadata:
					  // http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/meta/MetadataConverter.java;h=3780d383239aa519be1ae149d3a875825dfd03ef;hb=HEAD
					  
					  //Start writing
					  writer.setMetadataRetrieve(metadata);
					  writer.setId(basedir.getAbsolutePath());
					  //writer.setCompression("J2K");

					  //Use BIGTIFF if possible. Later this will not be needed
					  if(writer.getWriter() instanceof OMETiffWriter)
					  	{
					  	System.out.println("ometiff! "+writer.getWriter());
					  	((OMETiffWriter)writer.getWriter()).setBigTiff(true);
					  	}

					  //Write all the series
					  for(int curChanID=0;curChanID<channels.size();curChanID++)
					  	{
					  	EvChannel curChan=channels.get(curChanID);
					  	
					  	writer.setSeries(curChanID);
					  	
					  	

					  	for (int curZ=0; curZ<3; curZ++) 
					  		{
					  		byte[] plane=null;

					  		
					  		writer.saveBytes(curZ, plane);
					  		}
					  	curChanID++;
					  	}

					  writer.close();
						}
					catch (DependencyException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
					catch (ServiceException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
					catch (FormatException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}

*/
					
					
					
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
	

	//Consider using this instead
	/*
	private static int getPlaneIndex(IFormatReader r, int planeNum) 
		{
		MetadataRetrieve retrieve = (MetadataRetrieve) r.getMetadataStore();
		int imageIndex = r.getSeries();
		int planeCount = retrieve.getPlaneCount(imageIndex, 0);
		int[] zct = r.getZCTCoords(planeNum);
		for (int i=0; i<planeCount; i++) 
			{
			Integer theC = retrieve.getPlaneTheC(imageIndex, 0, i);
			Integer theT = retrieve.getPlaneTheT(imageIndex, 0, i);
			Integer theZ = retrieve.getPlaneTheZ(imageIndex, 0, i);
			if (zct[0] == theZ.intValue() && zct[1] == theC.intValue() && zct[2] == theT.intValue())
				return i;
			}
		return -1;
		}
	*/

	
	/*
	@SuppressWarnings("deprecation")
	private static EvDecimal parseBFDate(String s)
		{
		//2002-06-17T18:35:59
		//Note that there is no time zone here. Use the local one. 
		try
			{
			int year=Integer.parseInt(s.substring(0,4));
			int month=Integer.parseInt(s.substring(5,7));
			int day=Integer.parseInt(s.substring(8,10));
			int hour=Integer.parseInt(s.substring(11,13));
			int minute=Integer.parseInt(s.substring(14,16));
			int second=Integer.parseInt(s.substring(17,19));
			Date d=new Date(year-1900,month-1,day,hour,minute,second);
			return new EvDecimal(d.getTime());
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
		}
		*/
	
	/**
	 * Scan recording for channels and build a file database
	 */
	//@SuppressWarnings("unchecked") 
	public void buildDatabase(EvData d)
		{
		//Load metadata from added OSTXML-file. This has to be done first or all image loaders are screwed
		File metaFile=getMetaFile();
		if(metaFile.exists())
			d.loadXmlMetadata(metaFile);

		System.out.println("#series "+imageReader.getSeriesCount());
		
		HashSet<String> usedImsetNames=new HashSet<String>();
		for(int seriesIndex=0;seriesIndex<imageReader.getSeriesCount();seriesIndex++)
			{
			//Setting series will re-populate the metadata store as well
			imageReader.setSeries(seriesIndex);
			
			System.out.println("bioformats looking at series "+seriesIndex);

			String imageName=retrieve.getImageName(seriesIndex);

			//On windows, bio-formats uses the entire path. This is ugly so cut off the part
			//until the last file
			if(imageName.contains("\\"))
				imageName=imageName.substring(imageName.lastIndexOf('\\'));
			
			//The image name usually sucks, don't do this anymore!
			//String imsetName=imageName==null || imageName.equals("") ? "im"+seriesIndex : "im-"+imageName;
			String imsetName="im"+seriesIndex;
			
			
//			if(d.metaObject.containsKey(imsetName))
			if(usedImsetNames.contains(imsetName)) //In case channel already exist in XML, do not overwrite it
				imsetName="im-"+imageName;
			usedImsetNames.add(imsetName);
			
			Imageset imset=(Imageset)d.metaObject.get(imsetName);
			if(imset==null)
				d.metaObject.put(imsetName, imset=new Imageset());
			for(String s:new LinkedList<String>(imset.getChannels().keySet()))
				{
				//TODO Keep metaobjects below channel?
				imset.metaObject.remove(s);
				}

			
			
			PositiveInteger sizeT=retrieve.getPixelsSizeT(seriesIndex);
			PositiveInteger sizeZ=retrieve.getPixelsSizeZ(seriesIndex);
			PositiveInteger sizeC=retrieve.getPixelsSizeC(seriesIndex);
			//int sizeX=retrieve.getPixelsSizeX(seriesIndex).getValue();
			//int sizeY=retrieve.getPixelsSizeY(seriesIndex).getValue();
			
			
			for(int curC=0;curC<sizeC.getValue();curC++)
				{
				//EvChannel ch=new EvChannel();
				EvChannel ch=imset.getCreateChannel("ch"+curC);
				
				
				
				for(int curT=0;curT<sizeT.getValue();curT++)
					{
					
					int imageIndexFirstPlane=imageReader.getIndex(0, curC, curT);
					
					System.out.println("index "+imageIndexFirstPlane);
					
					//Read resolution
					//Note: values are optional!!!
					/*
					Double resX=retrieve.getPixelsPhysicalSizeX(imageIndexFirstPlane); //[um/px]
					Double resY=retrieve.getPixelsPhysicalSizeY(imageIndexFirstPlane); //[um/px]
					Double resZ=retrieve.getPixelsPhysicalSizeZ(imageIndexFirstPlane); //[um/px]*/
					
					Double resX=retrieve.getPixelsPhysicalSizeX(0); //[um/px]
					Double resY=retrieve.getPixelsPhysicalSizeY(0); //[um/px]
					Double resZ=retrieve.getPixelsPhysicalSizeZ(0); //[um/px]

					System.out.println("Detected resolution: "+resX+"    "+resY+"   "+resZ);
					
					if(resX==null || resX==0) resX=1.0;
					if(resY==null || resY==0) resY=1.0;
					if(resZ==null || resZ==0) resZ=1.0;
					
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
							Double deltaT=retrieve.getPlaneDeltaT(0, 0);
							if(deltaT!=null)
								frame=new EvDecimal(deltaT);
							}
						catch (Exception e)
							{
							System.out.println("Failed to call getPlaneDeltaT");
							}
						}
					

					//Create stack
					EvStack stack=new EvStack();
					ch.putStack(frame, stack);
					stack.setRes(resX,resY,resZ);
					
					//Fill stack with planes
					for(int curZ=0;curZ<sizeZ.getValue();curZ++)
						{
						EvImage evim=new EvImage();
						evim.io=new BioformatsSliceIO(imageReader, imageReader.getIndex(curZ, curC, curT), basedir, false);
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

	
	}
