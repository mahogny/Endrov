package endrov.imagesetBioformats;

import java.awt.RenderingHints;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.RasterOp;
import java.io.*;
import java.util.*;

import loci.formats.*;
import endrov.data.*;
import endrov.imageset.*;
import endrov.imagesetOST.EvIODataOST;
import endrov.util.EvDecimal;
import endrov.util.Tuple;


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
		EvData.supportFileFormats.add(new EvDataSupport(){
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
	
	/**
	 * Image I/O
	 */
	private class SliceIO implements EvIOImage
		{
		private int id;
		private Integer subid;
		private IFormatReader imageReader;
		private String sourceName;
	
		
		public SliceIO(IFormatReader imageReader, int id, Integer subid, String sourceName)
			{
			this.imageReader=imageReader;
			this.id=id;
			this.subid=subid;
			this.sourceName=sourceName;
			}
	
		
		public String sourceName()
			{
			return sourceName;
			}
	
		
		/**
		 * Load the image
		 */
		public BufferedImage loadJavaImage()
			{
			try
				{
				BufferedImage i=imageReader.openImage(id);
				
				int w=i.getWidth();
				int h=i.getHeight();
	
				if(imageReader.getPixelType()==FormatTools.UINT16)
					{
					//Bug *compensation* 2008-11-07 CB72070min3.liff (openlab)
					//Complain!
					byte[] buf=new byte[imageReader.getSizeX()*imageReader.getSizeY()*2];
					imageReader.openBytes(id, buf);
					i=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
					for(int y=0;y<h;y++)
						for(int x=0;x<w;x++)
							{
							byte a=buf[(w*y+x)*2];
							int b=buf[(w*y+x)*2+1];
							if(b<0)
								b+=256;
							int c=(((int)b)+(a<<8))>>4;
							i.getRaster().setPixel(x, y, new int[]{c});
							}
					}
				
				
				//System.out.println(""+i+" "+i.getWidth());
				
				//This hack fixes Leica
				if(subid==null)
					subid=0;
				/*else
					System.out.println("subid "+subid);*/
				
				if(subid!=null)
					{
					
					/*
					BufferedImage im=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
					WritableRaster rastin=i.getRaster();
					WritableRaster rastout=im.getRaster();
					int[] pixels=new int[w*h];
					rastin.getSamples(0, 0, w, h, subid, pixels);				
					rastout.setSamples(0, 0, w, h, 0, pixels);				
					 */
					/*
					int[] pixin=new int[3*w*h];
					int[] pixout=new int[w*h];
					rastin.getPixels(0, 0, w, h, pixin);				
					for(int j=0;j<w*h;j++)
						pixout[j]=pixin[j*3+subid];
					rastout.setPixels(0, 0, w, h, pixout);				
					*/
					
	
					BufferedImage im=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
					
					float matrix[][]={{0,0,0}};
					if(i.getRaster().getNumBands()==1)
						matrix=new float[][]{{0/*,0*/}};
					else if(i.getRaster().getNumBands()==2)
						matrix=new float[][]{{0,0/*,0*/}};
					else if(i.getRaster().getNumBands()==3)
						matrix=new float[][]{{0,0,0/*,0*/}};
					
					matrix[0][subid]=1;
					RasterOp op=new BandCombineOp(matrix,new RenderingHints(null));
					op.filter(i.getRaster(), im.getRaster());
					
					return im;
					}
					
				return i;
				}
			catch(Exception e)
				{
				endrov.ev.Log.printError("Failed to read image "+sourceName(),e);
				return null;
				}
			}
		}

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	/** Path to imageset */
	public File basedir;

	
	public IFormatReader imageReader=null;
	
	/**
	 * Open a new recording
	 */
	public EvIODataBioformats(EvData d, File basedir) throws Exception
		{
		this.basedir=basedir;
//		this.imageset=(new File(basedir)).getName();
		if(!basedir.exists())
			throw new Exception("File does not exist");

		imageReader=new ImageReader();
		imageReader.setId(basedir.getAbsolutePath());
		
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
	
	public void saveData(EvData d, EvData.FileIOStatusCallback cb)
		{
		try
			{
			EvIODataOST.saveMeta(d, getMetaFile());
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	


	
	/**
	 * Scan recording for channels and build a file database
	 */
	@SuppressWarnings("unchecked") public void buildDatabase(EvData d)
		{
		int numx=imageReader.getSizeX();
		int numy=imageReader.getSizeY();
		int numz=imageReader.getSizeZ();
		int numt=imageReader.getSizeT();
		int numc=imageReader.getSizeC();
		

		
		//Read meta data from original imageset
		System.out.println("# XYZ "+numx+" "+numy+" "+numz+ " T "+numt+" C "+numc);
		for(Object o:(Set)imageReader.getMetadata().entrySet())
			{
			Map.Entry e=(Map.Entry)o;
			System.out.println("> "+e.getKey()+" "+e.getValue());
			}
		double resX=1;
		double resY=1;
		EvDecimal invResZ=EvDecimal.ONE;
		EvDecimal resT=EvDecimal.ONE;
//		double resZ=1;
		if(imageReader.getMetadataValue("VoxelSizeX")!=null)
			resX=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeX"))*1e6);
		if(imageReader.getMetadataValue("VoxelSizeY")!=null)
			resY=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeY"))*1e6);
		if(imageReader.getMetadataValue("VoxelSizeZ")!=null)
			invResZ=new EvDecimal(""+imageReader.getMetadataValue("VoxelSizeZ")).divide(new EvDecimal("1000000"));

		if(imageReader.getMetadataValue("TimeInterval")!=null)
			resT=new EvDecimal(""+imageReader.getMetadataValue("TimeInterval"));
		
//			resZ=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeZ"))*1e6);

		
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
			EvChannel mc=im.createChannel(channelName);
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
