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
		imageReader.setId(basedir.getAbsolutePath());
		imageReader=new ChannelSeparator(imageReader);
		
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

		//Load metadata from added OSTXML-file. This has to be done first or all image loaders
		//are screwed
		File metaFile=getMetaFile();
		if(metaFile.exists())
			d.loadXmlMetadata(metaFile);

		
		
		
		//Bioformats has ImageIndex and imagePlaneIndex
		//MetadataRetrieve retrieve = (MetadataRetrieve)imageReader.getMetadataStore();

		System.out.println("numser "+imageReader.getSeriesCount());
		
		/*for(Object o:(Set)imageReader.getMetadata().entrySet())
			{
			Map.Entry e=(Map.Entry)o;
			System.out.println("> \""+e.getKey()+"\" \""+e.getValue()+"\"");
			}*/

		
		for(int seriesIndex=0;seriesIndex<imageReader.getSeriesCount();seriesIndex++)
			{
			//String getImageDescription(int imageIndex);
			//String getPixelsDimensionOrder(int imageIndex, int pixelsIndex);
			
			imageReader.setSeries(seriesIndex);
			//int imageIndex=imageReader.getSeries();
			
			
			System.out.println("pixel count "+retrieve.getPixelsCount(seriesIndex));
			
			String imageName=retrieve.getImageName(seriesIndex);
			
			
			//Some names might be awful. Might be a bad idea
			String imsetName=imageName==null || imageName.equals("") ? "im"+seriesIndex : "im-"+imageName;
			
			if(d.metaObject.containsKey(imsetName))
				imsetName="im-"+imageName;
			
			Imageset im=(Imageset)d.metaObject.get(imsetName);
			if(im==null)
				d.metaObject.put(imsetName, im=new Imageset());
			for(String s:im.getChannels().keySet())
				im.metaObject.remove(s);
			//im.channelImages.clear();
			
			
			String creationDate = retrieve.getImageCreationDate(seriesIndex);
			if(creationDate!=null)
				im.dateCreate=parseBFDate(creationDate);
				
			//System.out.println("create "+creationDate);
			

			
			/*
			
			//int planeCount = retrieve.getPixelsCount(imageIndex);
			int planeCount = retrieve.getPlaneCount(imageIndex,0);
			System.out.println("planecount "+planeCount);
			for (int curPlane=0; curPlane<planeCount; curPlane++) 
				{
				Integer theC = retrieve.getPlaneTheC(imageIndex, 0, curPlane);
				Integer theT = retrieve.getPlaneTheT(imageIndex, 0, curPlane);
				Integer theZ = retrieve.getPlaneTheZ(imageIndex, 0, curPlane);
				
				String channelName="ch"+theC;
				EvChannel mc=im.getCreateChannel(channelName);

				Float expTime=retrieve.getPlaneTimingExposureTime(imageIndex, 0, curPlane);
				
				Float fdx=retrieve.getDimensionsPhysicalSizeX(imageIndex, curPlane); //um/px
				Float fdy=retrieve.getDimensionsPhysicalSizeY(imageIndex, curPlane); //um/px
				Float fdz=retrieve.getDimensionsPhysicalSizeZ(imageIndex, curPlane); //um/px
				if(fdx==null) fdx=1.0f;
				if(fdy==null) fdy=1.0f;
				if(fdz==null) fdz=1.0f;
				
				EvDecimal frame=new EvDecimal(theT*fdz);
				double resX=1.0/fdx; //[px/um]
				double resY=1.0/fdy; //[px/um]
				double resZ=fdz;
				EvDecimal zpos=new EvDecimal(fdz).multiply(theZ);
				
				int numChannel=retrieve.getChannelComponentCount(imageIndex, curPlane);
				System.out.println("plane "+curPlane+" "+numChannel);
				for(int curChannel=0;curChannel<numChannel;curChannel++)
					{
					
					TreeMap<EvDecimal, EvImage> loaderset=mc.getCreateFrame(frame);
					
					EvImage evim=new EvImage();
					loaderset.put(zpos, evim); //used to be mul, with non-inv resz
					evim.binning=1;
					evim.dispX=0;
					evim.dispY=0;
					evim.resX=resX;
					evim.resY=resY;
					
					im.resX=resX;
					im.resY=resY;
					im.resZ=resZ;
					
					if(imageReader.isRGB())
						evim.io=new SliceIO(imageReader, curPlane, curChannel, "");
					else
						evim.io=new SliceIO(imageReader, curPlane, null, ""); 
							
					}
				
				}
			*/
			
			
			
			int numx=imageReader.getSizeX();
			int numy=imageReader.getSizeY();
			int numz=imageReader.getSizeZ();
			int numt=imageReader.getSizeT();
			int numc=imageReader.getSizeC();

			//Read meta data from original imageset
			System.out.println("BF # XYZ "+numx+" "+numy+" "+numz+ " T "+numt+" C "+numc);

			
			//It *must* be 0,0
			Float fdx=retrieve.getDimensionsPhysicalSizeX(0, 0); //um/px
			Float fdy=retrieve.getDimensionsPhysicalSizeY(0, 0); //um/px
			Float fdz=retrieve.getDimensionsPhysicalSizeZ(0, 0); //um/px
			System.out.println("res "+fdx+" "+fdy+" "+fdz);
			
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

						
						Float expTime=retrieve.getPlaneTimingExposureTime(seriesIndex, 0, curPixel);
						

						EvDecimal frame=null;
						Float deltaT=retrieve.getPlaneTimingDeltaT(seriesIndex,0,curPixel);
						if(deltaT!=null)
							frame=new EvDecimal(deltaT);

						if(frame!=null)
							{
							Float fdt=retrieve.getDimensionsTimeIncrement(0, 0);
							if(fdt!=null)
								frame=new EvDecimal(framenum*fdt);
							}
						
						
						/*
						if(fdx==null && imageReader.getMetadataValue("VoxelSizeX")!=null)
							fdx=(float)(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeX"))*1e6);
						if(fdy==null && imageReader.getMetadataValue("VoxelSizeY")!=null)
							fdy=(float)(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeY"))*1e6);
						if(fdz==null && imageReader.getMetadataValue("VoxelSizeZ")!=null)
							fdz=(float)(1.0/( Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeZ"))/1000000 ));
						
						if(frame==null && imageReader.getMetadataValue("TimeInterval")!=null)
							frame=new EvDecimal(""+imageReader.getMetadataValue("TimeInterval")).multiply(framenum);
							*/

						//System.out.println("orig dz" +fdz);
						
						if(fdx==null || fdx==0) fdx=1.0f;
						if(fdy==null || fdy==0) fdy=1.0f;
						if(fdz==null || fdz==0) fdz=1.0f;

						if(frame==null)
							frame=new EvDecimal(framenum);
						
						double resX=1.0/fdx; //[px/um]
						double resY=1.0/fdy; //[px/um]
						double resZ=fdz;
						EvDecimal zpos=new EvDecimal(fdz).multiply(slicenum);
						
						im.resX=resX;
						im.resY=resY;
						im.resZ=resZ; //Maybe invert?
						
//						System.out.println("resf  "+fdx+" "+fdy+" "+fdz);
	//					System.out.println("resEV "+im.resX+" "+im.resY+" "+im.resZ+" "+frame);
						
						EvImage evim=new EvImage();
						EvStack stack=c.getCreateFrame(frame);
						stack.put(zpos, evim); //used to be mul, with non-inv resz
						//stack.binning=1;
						stack.dispX=0;
						stack.dispY=0;
						stack.resX=resX;
						stack.resY=resY;
//						stack.resZ=new EvDecimal(fdz);
						
						evim.io=new BioformatsSliceIO(imageReader, curPixel, bandID, "");
						}
					}
				}
			
			
			
			}


		//https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/meta/MetadataRetrieve.java
		
		//https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/loci/formats/meta/MetadataRetrieve.java?rev=4058
		//https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/loci-plugins/src/loci/plugins/LociFunctions.java
//		imageReader.get
//		retrieve.getDimensionsPhysicalSizeX(seriesIndex, arg1)
		
		//For a particular Dimensions, gets the size of an individual pixel's Z axis in microns.


		
		/*
		 * 		
		int numx=imageReader.getSizeX();
		int numy=imageReader.getSizeY();
		int numz=imageReader.getSizeZ();
		int numt=imageReader.getSizeT();
		int numc=imageReader.getSizeC();

		//Read meta data from original imageset
		System.out.println("BF # XYZ "+numx+" "+numy+" "+numz+ " T "+numt+" C "+numc);
		for(Object o:(Set)imageReader.getMetadata().entrySet())
			{
			Map.Entry e=(Map.Entry)o;
			System.out.println("> \""+e.getKey()+"\" \""+e.getValue()+"\"");
			}
		double resX=1; //[px/um]
		double resY=1; //[px/um]
		EvDecimal invResZ=EvDecimal.ONE;
		EvDecimal resT=EvDecimal.ONE;
//		double resZ=1;
		if(imageReader.getMetadataValue("VoxelSizeX")!=null)
			resX=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeX"))*1e6);
		if(imageReader.getMetadataValue("VoxelSizeY")!=null)
			resY=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeY"))*1e6);
		if(imageReader.getMetadataValue("VoxelSizeZ")!=null)
			invResZ=new EvDecimal(""+imageReader.getMetadataValue("VoxelSizeZ")).divide(new EvDecimal("1000000"));
		
		if(imageReader.getMetadataValue("X element length (in um)")!=null) 
			resX=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("X element length (in um)")));
		if(imageReader.getMetadataValue("Y element length (in um)")!=null) 
			resY=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("Y element length (in um)")));
		if(imageReader.getMetadataValue("Z element length (in um)")!=null) 
			invResZ=new EvDecimal(""+imageReader.getMetadataValue("Z element length (in um)"));
		
		if(imageReader.getMetadataValue("TimeInterval")!=null)
			resT=new EvDecimal(""+imageReader.getMetadataValue("TimeInterval"));
		
//			resZ=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeZ"))*1e6);

		System.out.println("BF Resolution X Y 1/Z T: "+resX+"\t"+resY+"\t"+invResZ+"\t"+resT);
		
		//Load metadata from added OSTXML-file
		File metaFile=getMetaFile();
		if(metaFile.exists())
			d.loadXmlMetadata(metaFile);
		
		Collection<Imageset> ims=d.getObjects(Imageset.class);
		Imageset im;
		if(ims.isEmpty())
			{
			im=new Imageset();
			d.metaObject.put("im", im);
			}
		else
			im=ims.iterator().next();
		
		//Enlist images
		for(int channelnum=0;channelnum<numc;channelnum++)
			{
			String channelName="ch"+channelnum;
			EvChannel mc=im.getCreateChannel(channelName);
			mc.chBinning=1;

			//Fill up with image loaders
			EvChannel c=new EvChannel();
			im.channelImages.put(channelName,c);
			for(int framenum=0;framenum<numt;framenum++)
				{
				TreeMap<EvDecimal,EvImage> loaderset=new TreeMap<EvDecimal,EvImage>();
				for(int slicenum=0;slicenum<numz;slicenum++)
					{
					EvImage evim=new EvImage();
					loaderset.put(new EvDecimal(slicenum).divide(invResZ), evim); //used to be mul, with non-inv resz
					evim.binning=1;
					evim.dispX=0;
					evim.dispY=0;
					evim.resX=resX;
					evim.resY=resY;
					
					if(imageReader.isRGB())
						evim.io=new SliceIO(imageReader, imageReader.getIndex(slicenum, 0, framenum), channelnum, "");
					else
						evim.io=new SliceIO(imageReader, imageReader.getIndex(slicenum, channelnum, framenum), null, "");
							
					}
				c.imageLoader.put(resT.multiply(framenum), loaderset);
				}
			}
		*/
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
	
	}
