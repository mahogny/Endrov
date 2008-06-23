package evplugin.imagesetImserv;

//note: renaming channel will require all EvImageOST to be renamed as well

import java.awt.image.*;
import java.io.*;
import java.util.TreeMap;

import evplugin.data.RecentReference;
import evplugin.imageset.*;
import evplugin.imagesetImserv.service.DataIF;
import evplugin.imagesetImserv.service.SendFile;


/**
 * Support for ImServ
 * @author Johan Henriksson
 */
public class ImservImageset extends Imageset
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/

	private DataIF omeimage;
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	/**
	 * Create a new recording
	 */
	public ImservImageset(DataIF omeimage)
		{
		this.omeimage=omeimage;
		try
			{
			imageset=omeimage.getName();
			}
		catch (Exception e)
			{
			imageset="<name lookup failure>";
			}
		buildDatabase();
		}
	
	/**
	 * Get name description of this metadata
	 */
	public String toString()
		{
		return getMetadataName();
		}

	

	/**
	 * Get directory for this imageset where any datafiles can be stored
	 */
	public File datadir()
		{
		return null;
		}

	/**
	 * Save meta for all channels into RMD-file
	 */
	public void saveMeta()
		{
		}
	
	public RecentReference getRecentEntry()
		{
		return null;
		}
	
	
	/**
	 * Scan recording for channels and build a file database
	 */
	public void buildDatabase()
		{
		//Set metadata TODO
		
		System.out.println("building imageset");
		
		
		int numc=3;
		for(int c=0;c<numc;c++)
			{
			String channelName="ch"+c;
			Channel ch=new Channel(meta.getCreateChannelMeta(channelName));
			ch.scanFiles(channelName);
			channelImages.put(channelName,ch);
			}
		
		}
	
	


	protected ChannelImages internalMakeChannel(ImagesetMeta.Channel ch)
		{
		return new Channel(ch);
		}
		
	
	

	
	
	
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////
	///// this custom channel is messing up more than helping //////////

	
	/**
	 * OST channel - contains methods for building frame database
	 */
	public class Channel extends Imageset.ChannelImages
		{
		public Channel(ImagesetMeta.Channel channelName)
			{
			super(channelName);
			}
		
	
		
		/**
		 * Scan all files for this channel and build a database
		 */
		public void scanFiles(String chnum)
			{
			imageLoader.clear();

			int numframe=1;
			int numz=50;
			for(int frame=0;frame<numframe;frame++)
				{
				TreeMap<Integer,EvImage> loaderset=new TreeMap<Integer,EvImage>();
				for(int z=0;z<numz;z++)
					loaderset.put(z, newEvImage(z, frame, chnum));
				imageLoader.put(frame, loaderset);
				}
			}

		protected EvImage internalMakeLoader(int frame, int z)
			{
			return null;
//			return newEvImage(buildImagePath(getMeta().name, frame, z, ".png").getAbsolutePath()); //png?
			}
		
		
		public EvImage newEvImage(int z, int t, String c)
			{
			return new EvImageImserv(z, t, c);
			}
		
		
		private class EvImageImserv extends EvImage
			{
			int z,t;
			String c;
			public EvImageImserv(int z, int t, String c)
				{
				this.z=z;
				this.t=t;
				this.c=c;
				}

			public int getBinning(){return getMeta().chBinning;}
			public double getDispX(){return getMeta().dispX;}
			public double getDispY(){return getMeta().dispY;}
			public double getResX(){return meta.resX;}
			public double getResY(){return meta.resY;}
			protected BufferedImage loadJavaImage()
				{
				//it is the server side responsibility to make byte_gray?
	//			BufferedImage im=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
				try
					{
					DataIF.ImageTransfer transfer=omeimage.getImage(c, t, z);
					if(transfer!=null)
						return SendFile.getImageFromBytes(transfer.data);
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				return null;
//				return im;
				}
		
			}
		}
	
	
	public void finalize()
		{
		System.out.println("finalize ome");
		}
	
	}

