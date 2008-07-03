package endrov.imagesetOME;

//note: renaming channel will require all EvImageOST to be renamed as well

import java.awt.image.*;
import java.io.*;
import java.util.List;
import java.util.TreeMap;

import endrov.data.RecentReference;
import endrov.imageset.*;


/**
 * Support for the native OST file format
 * @author Johan Henriksson
 */
public class OMEImageset extends Imageset
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/

	private EVOME omesession;
	private ome.model.core.Image omeimage;
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	/**
	 * Create a new recording. Basedir points to imageset- ie without the channel name
	 * @param basedir
	 */
	public OMEImageset(EVOME omesession, ome.model.core.Image omeimage)
		{
		this.omesession=omesession;
		this.omeimage=omeimage;
		imageset=omeimage.getName();
		if(imageset.indexOf('/')>=0)
			imageset=imageset.substring(imageset.lastIndexOf('/')+1);
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
		
		List<ome.model.core.Pixels> pixlist=omesession.getPixels(omeimage);
		
		for(ome.model.core.Pixels pix:omesession.getPixels(omeimage))
			{
			System.out.println("asdasd "+pix.getId()+" "+pix.getImage());
			}
		
		ome.model.core.Pixels pixel=pixlist.iterator().next();
		
		
		System.out.println("building imageset");
		/*
		for(Object oc:omesession.getChannelsData(pixel.getId()))
			{
			ome.model.core.Channel omechannel=(ome.model.core.Channel)oc;
			long chid=omechannel.getId();
			System.out.println("chid: "+chid);
			System.out.println("lid: "+omechannel.getLogicalChannel().getId());
			String channelName=""+chid;
			Channel c=new Channel(meta.getCreateChannelMeta(channelName));
			c.scanFiles(pixel, (int)chid);
			channelImages.put(channelName,c);
			}
		*/
		
		int numc=pixel.getSizeC();
		for(int c=0;c<numc;c++)
			{
			String channelName=""+c;
			Channel ch=new Channel(meta.getCreateChannelMeta(channelName));
			ch.scanFiles(pixel, c);
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
		public void scanFiles(ome.model.core.Pixels pixel, int chnum)
			{
			imageLoader.clear();
			int numframe=pixel.getSizeT();
			int numz=pixel.getSizeZ();
			for(int frame=0;frame<numframe;frame++)
				{
				TreeMap<Integer,EvImage> loaderset=new TreeMap<Integer,EvImage>();
				for(int z=0;z<numz;z++)
					loaderset.put(z, newEvImage(pixel, z, frame, chnum));
				imageLoader.put(frame, loaderset);
				}
			}

		protected EvImage internalMakeLoader(int frame, int z)
			{
			return null;
//			return newEvImage(buildImagePath(getMeta().name, frame, z, ".png").getAbsolutePath()); //png?
			}
		
		
		public EvImage newEvImage(ome.model.core.Pixels pixel, int z, int t, int c)
			{
			return new EvImageOME(pixel, z, t, c);
			}
		
		
		private class EvImageOME extends EvImage
			{
			int z,t,c;
			int w, h;
			long pixelid;
			public EvImageOME(ome.model.core.Pixels pixel, int z, int t, int c)
				{
				this.z=z;
				this.t=t;
				this.c=c;
				w=pixel.getSizeX();
				h=pixel.getSizeY();
				pixelid=pixel.getId();
				}

			public int getBinning(){return getMeta().chBinning;}
			public double getDispX(){return getMeta().dispX;}
			public double getDispY(){return getMeta().dispY;}
			public double getResX(){return meta.resX;}
			public double getResY(){return meta.resY;}
			protected BufferedImage loadJavaImage()
				{
				BufferedImage im=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
				WritableRaster r=im.getRaster();
				byte[] b=omesession.getPlane(pixelid, z, t, c);
				int[] strip=new int[w];
				for(int y=0;y<h;y++)
					{
					for(int x=0;x<w;x++)
						strip[x]=b[y*w+x];
					r.setPixels(0, y, w, 1, strip);
					}
				return im;
				}
		
			}
		}
	
	
	public void finalize()
		{
		System.out.println("finalize ome");
		}
	
	}

