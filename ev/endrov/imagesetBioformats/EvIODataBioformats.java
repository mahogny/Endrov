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

import loci.formats.*;
import loci.formats.meta.*;
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
	public EvIODataBioformats(EvData d, File basedir) throws Exception
		{
		this.basedir=basedir;
		if(!basedir.exists())
			throw new Exception("File does not exist");

		imageReader=new ImageReader();
		retrieve=MetadataTools.createOMEXMLMetadata();
		imageReader.setMetadataStore(retrieve);
		
		System.out.println("bioformats set id "+basedir);
		imageReader.setId(basedir.getAbsolutePath());
		System.out.println("bioformats adding channel separator");
		imageReader=new ChannelSeparator(imageReader);
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
			/*
			Map<EvPath,EvChannel> channels=d.getIdObjectsRecursive(EvChannel.class);
			
			for(Map.Entry<EvPath, EvChannel> ch:channels.entrySet())
				{
				//imageindex, pixelindex
				
				
				
				
				
				
				
				}
			
			*/
			
			/*
			
			int pixelType=FormatTools.DOUBLE;
			
			
			// create metadata object with minimum required metadata fields
			IMetadata meta = MetadataTools.createOMEXMLMetadata();
			meta.createRoot();
			
			//meta.getDimensionsPhysicalSizeX(arg0, arg1);
			
			
			meta.setPixelsBigEndian(Boolean.TRUE, 0, 0);
			meta.setPixelsDimensionOrder("XYZCT", 0, 0);
			meta.setPixelsPixelType(FormatTools.getPixelTypeString(pixelType), 0, 0);
			meta.setPixelsSizeX(w, 0, 0);
			meta.setPixelsSizeY(h, 0, 0);
			meta.setPixelsSizeZ(1, 0, 0);
			meta.setPixelsSizeC(1, 0, 0);
			meta.setPixelsSizeT(1, 0, 0);
			meta.setLogicalChannelSamplesPerPixel(1, 0, 0);
			
			
			 // write image plane to disk
			IFormatWriter writer = new ImageWriter();
			writer.setMetadataRetrieve(meta);
			writer.setId(basedir.getAbsolutePath());
			boolean isLast=true;
			writer.saveBytes(img, isLast);
			writer.close();
			*/
			
			

			
			
			//FormatTools, DOUBLE, FLOAT, INT16, INT32, INT8, UINT16, UINT32, UINT8
			
			
			//DataInputStream di=new DataInputStream(new ByteArrayInputStream(bytes));
/*
			DataTools.floatsToBytes(arg0, arg1);
			
			int type=imageReader.getPixelType();
			int bpp=FormatTools.getBytesPerPixel(type);
			boolean isFloat = type == FormatTools.FLOAT || type == FormatTools.DOUBLE;
			boolean isLittle = imageReader.isLittleEndian();
			boolean isSigned = type == FormatTools.INT8 || type == FormatTools.INT16 || type == FormatTools.INT32;
			Object bfpixels = DataTools.makeDataArray(bytes, bpp, isFloat, isLittle);
			
	*/		
			
			/*
			Imageset im=d.getObjects(Imageset.class).iterator().next();
			
			
				
				
				
				//Hoping this is enough to save metadata which I do not convert
				writer.setMetadataRetrieve(retrieve);
				writer.setId(basedir.getPath());
				
				
				
				
				 saveImage(Image image, int series, boolean lastInSeries, boolean last)
         Saves the given image to the given series in the current file.
				
				}
			catch (FormatException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			*/
			
			
			
			
			
			
			
			
			
			
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

		
		//Bioformats has ImageIndex and imagePlaneIndex
		//MetadataRetrieve retrieve = (MetadataRetrieve)imageReader.getMetadataStore();

		/*for(Object o:(Set)imageReader.getMetadata().entrySet())
			{
			Map.Entry e=(Map.Entry)o;
			System.out.println("> \""+e.getKey()+"\" \""+e.getValue()+"\"");
			}*/

		
		HashSet<String> usedChannelNames=new HashSet<String>();
		
		for(int seriesIndex=0;seriesIndex<imageReader.getSeriesCount();seriesIndex++)
			{
			//String getImageDescription(int imageIndex);
			//String getPixelsDimensionOrder(int imageIndex, int pixelsIndex);
			
			imageReader.setSeries(seriesIndex);
			//int imageIndex=imageReader.getSeries();
			
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
			if(usedChannelNames.contains(imsetName)) //In case channel already exist in XML, do not overwrite it
				imsetName="im-"+imageName;
			usedChannelNames.add(imsetName);
			
			Imageset im=(Imageset)d.metaObject.get(imsetName);
			if(im==null)
				d.metaObject.put(imsetName, im=new Imageset());
			for(String s:im.getChannels().keySet())
				{
				//Keep metaobjects below channel?
				im.metaObject.remove(s);
				}
			
			
			String creationDate = retrieve.getImageCreationDate(seriesIndex);
			if(creationDate!=null)
				im.dateCreate=parseBFDate(creationDate);
				
			
			int numx=imageReader.getSizeX();
			int numy=imageReader.getSizeY();
			int numz=imageReader.getSizeZ();
			int numt=imageReader.getSizeT();
			int numc=imageReader.getSizeC();

			//Read meta data from original imageset
			System.out.println("BF # XYZ "+numx+" "+numy+" "+numz+ " T "+numt+" C "+numc+
					" pixel count "+retrieve.getPixelsCount(seriesIndex));

			
			//It *must* be 0,0
			Double fdx=retrieve.getDimensionsPhysicalSizeX(0, 0); //um/px
			Double fdy=retrieve.getDimensionsPhysicalSizeY(0, 0); //um/px
			Double fdz=retrieve.getDimensionsPhysicalSizeZ(0, 0); //um/px
			//imageindex, pixelindex. is this the right place?
			System.out.println("res "+fdx+" "+fdy+" "+fdz);

//		System.out.println("f" + frame+" z "+zpos+" resf  "+fdx+" "+fdy+" "+fdz);
//		System.out.println("resEV "+im.resX+" "+im.resY+" "+im.resZ+" "+frame);

			//Enlist images
			for(int channelnum=0;channelnum<numc;channelnum++)
				{
				String channelName="ch"+channelnum;
				EvChannel mc=im.getCreateChannel(channelName);
				mc.chBinning=1;

				//Fill up with image loaders
				EvChannel c=new EvChannel();
				im.metaObject.put(channelName,c);
				for(int framenum=0;framenum<numt;framenum++)
					{
					//Get all stack information from the first plane
					int firstPixel = imageReader.isRGB() ?  
							imageReader.getIndex(0, 0, framenum) : imageReader.getIndex(0, channelnum, framenum);

					EvDecimal frame=null;
					Double deltaT=retrieve.getPlaneTimingDeltaT(seriesIndex,0,firstPixel);
					if(deltaT!=null)
						frame=new EvDecimal(deltaT);
					if(frame!=null)
						{
						Double fdt=retrieve.getDimensionsTimeIncrement(0, 0);
						if(fdt!=null)
							frame=new EvDecimal(framenum*fdt);
						}
					if(frame==null)
						frame=new EvDecimal(framenum);

					if(fdx==null || fdx==0) fdx=1.0;
					if(fdy==null || fdy==0) fdy=1.0;
					if(fdz==null || fdz==0) fdz=1.0;

					Map<String,String> metaFrame=c.getMetaFrame(frame);

					EvStack stack=c.getCreateFrame(frame);
					stack.dispX=0;
					stack.dispY=0;
					stack.resX=fdx;
					stack.resY=fdy;
					//stack.resZ=new EvDecimal(fdz); 
					
					//For every slice
					for(int slicenum=0;slicenum<numz;slicenum++)
						{
						int curPixel;
						Integer bandID=null;
						if(imageReader.isRGB())
							{
							curPixel=imageReader.getIndex(slicenum, 0, framenum);
							bandID=channelnum;
							}
						else
							curPixel=imageReader.getIndex(slicenum, channelnum, framenum);

						Double expTime=retrieve.getPlaneTimingExposureTime(seriesIndex, 0, curPixel);
						EvDecimal zpos=new EvDecimal(fdz).multiply(slicenum);
	
						EvImage evim=new EvImage();
						evim.io=new BioformatsSliceIO(imageReader, curPixel, bandID, "", false);
						stack.put(zpos, evim);
						metaFrame.put("exposure",""+expTime);
						}
					}
				}
			
			
			
			}


		//https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/meta/MetadataRetrieve.java
		
		//https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/loci/formats/meta/MetadataRetrieve.java?rev=4058
		//https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/loci-plugins/src/loci/plugins/LociFunctions.java
//		imageReader.get
//		retrieve.getDimensionsPhysicalSizeX(seriesIndex, arg1)
		
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
				EvData d=new EvData();
				d.io=new EvIODataBioformats(d, new File(file));
				return d;
				}
			public Integer saveSupports(String file){return null;}
			public List<Tuple<String,String[]>> getSaveFormats(){return new LinkedList<Tuple<String,String[]>>();};
			public EvIOData getSaver(EvData d, String file) throws IOException{return null;}
		});
		}

	
	}
