package evplugin.imagesetBioformats;

import java.io.*;
import java.util.*;

import evplugin.imageset.*;
import evplugin.data.*;
import evplugin.script.Script;

import loci.formats.*;

/**
 * Support for proprietary formats through LOCI Bioformats
 * 
 * @author Johan Henriksson (binding to library only)
 */
public class BioformatsImageset extends Imageset
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
	
		Script.addCommand("dbio", new CmdDBIO());
		
		supportFileFormats.add(new EvDataSupport(){
			public Integer supports(File file)
				{
				return file.isFile() ? 100 : null; //Low priority; need to find a way to check extensions
				}
			public EvData load(File file) throws Exception
				{
				return new BioformatsImageset(file.getAbsolutePath());
				}
		});
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	
	/** Path to imageset */
	public String basedir;

	
	public IFormatReader imageReader=null;
	
	/**
	 * Open a new recording
	 */
	public BioformatsImageset(String basedir) throws Exception
		{
		this.basedir=basedir;
		this.imageset=(new File(basedir)).getName();
		if(!(new File(basedir)).exists())
			throw new Exception("File does not exist");

		imageReader=new ImageReader();
		imageReader.setId(basedir);
		
		buildDatabase();
		}
	
	

	public File datadir()
		{
		return new File(basedir).getParentFile();
		}

	/**
	 * This plugin saves metadata into FILENAME.ostxml. This function constructs the name
	 */
	private File getMetaFile()
		{
		File fname=new File(basedir);
		return new File(fname.getParent(),fname.getName()+".ostxml");
		}
	
	public void saveMeta()
		{
		try
			{
			saveMeta(getMetaFile());
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	

	
	/**
	 * Scan recording for channels and build a file database
	 */
	@SuppressWarnings("unchecked") public void buildDatabase()
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
		meta=new ImagesetMeta();
		meta.resX=1;
		meta.resY=1;
		meta.resZ=1;
		if(imageReader.getMetadataValue("VoxelSizeX")!=null)
			meta.resX=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeX"))*1e6);
		if(imageReader.getMetadataValue("VoxelSizeY")!=null)
			meta.resY=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeY"))*1e6);
		if(imageReader.getMetadataValue("VoxelSizeZ")!=null)
			meta.resZ=1.0/(Double.parseDouble(""+imageReader.getMetadataValue("VoxelSizeZ"))*1e6);

		//Load metadata from added OSTXML-file
		File metaFile=getMetaFile();
		if(metaFile.exists())
			{
			//Load metadata
			loadXmlMetadata(metaFile.getPath());
			for(String oi:metaObject.keySet())
				if(metaObject.get(oi) instanceof ImagesetMeta)
					{
					meta=(ImagesetMeta)metaObject.get(oi);
					metaObject.remove(oi);
					break;
					}
			}
		
		
		//Enlist images
		channelImages.clear();
		for(int channelnum=0;channelnum<numc;channelnum++)
			{
			String channelName="ch"+channelnum;
			ImagesetMeta.Channel mc=meta.getCreateChannelMeta(channelName);
			loadMeta(mc);

			//Fill up with image loaders
			Channel c=new Channel(meta.getCreateChannelMeta(channelName));
			channelImages.put(channelName,c);
			for(int framenum=0;framenum<numt;framenum++)
				{
				TreeMap<Integer,EvImage> loaderset=new TreeMap<Integer,EvImage>();
				for(int slicenum=0;slicenum<numz;slicenum++)
					{
					if(imageReader.isRGB())
						loaderset.put(slicenum, c.newImage(imageReader,imageReader.getIndex(slicenum, 0, framenum), channelnum, ""));
//						loaderset.put(slicenum, c.newImage(imageReader,imageReader.getIndex(slicenum, channelnum, framenum), channelnum, ""));
					else
						loaderset.put(slicenum, c.newImage(imageReader,imageReader.getIndex(slicenum, channelnum, framenum), null, ""));
					}
				c.imageLoader.put(framenum, loaderset);
				}
			}
		}

	private void loadMeta(ImagesetMeta.Channel mc)
		{
		mc.chBinning=1;
		
		}

	
	
	
	
	/**
	 * Channel - contains methods for building frame database
	 */
	protected ChannelImages internalMakeChannel(ImagesetMeta.Channel ch)
		{
		return new Channel(ch);
		}
	public class Channel extends Imageset.ChannelImages
		{
		public Channel(ImagesetMeta.Channel channelName)
			{
			super(channelName);
			}
		protected EvImage internalMakeLoader(int frame, int z)
			{
			return new EvImageExt(null,0,0,"");
			}
		
		
		public EvImageExt newImage(IFormatReader imageReader, int id, Integer subid, String sourceName)
			{
			return new EvImageExt(imageReader,id,subid,sourceName);
			}
		
		private class EvImageExt extends EvImageBioformats
			{
			public EvImageExt(IFormatReader imageReader, int id, Integer subid, String sourceName){super(imageReader,id,subid,sourceName);}
	
			public int getBinning(){return getMeta().chBinning;}
			public double getDispX(){return getMeta().dispX;}
			public double getDispY(){return getMeta().dispY;}
			public double getResX(){return meta.resX;}
			public double getResY(){return meta.resY;}
			}
		
		
		}
	
	public RecentReference getRecentEntry()
		{
		return new RecentReference(getMetadataName(), basedir);
		}
	
	}

