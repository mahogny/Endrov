package evplugin.imagesetOME;

//note: renaming channel will require all EvImageOST to be renamed as well


//BandCombineOp for merging channels

import java.awt.RenderingHints;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.RasterOp;
import java.io.*;
import java.util.List;
import java.util.TreeMap;

import evplugin.ev.*;
import evplugin.imageset.*;


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
		
	//	List/*<ome.model.core.Channel>*/ ;
		
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
		
	
	

	
	
	
	/**
	 * Invalidate database cache (=deletes cache file)
	 */
	public void invalidateDatabaseCache()
		{
		}
	
	
	

	
	
	/**
	 * Save database as a cache file
	 */
	public void saveDatabaseCache()
		{
		
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
//			int numc=pixel.getSizeC();
			int numframe=pixel.getSizeT();
			int numz=pixel.getSizeZ();
			
			for(int frame=0;frame<numframe;frame++)
				{
				TreeMap<Integer,EvImage> loaderset=new TreeMap<Integer,EvImage>();
				
				for(int z=0;z<numz;z++)
					{
					
					
					loaderset.put(z, newEvImage(pixel, z, frame, chnum));
					}
				imageLoader.put(frame, loaderset);
				}
			
			
			/*
			
			
			File chandir=buildChannelPath(getMeta().name);
			File[] framedirs=chandir.listFiles();
			for(File framedir:framedirs)
				if(framedir.isDirectory() && !framedir.getName().startsWith("."))
					{
					int framenum=Integer.parseInt(framedir.getName());
					
					TreeMap<Integer,EvImage> loaderset=new TreeMap<Integer,EvImage>();
					File[] slicefiles=framedir.listFiles();
					for(File f:slicefiles)
						{
						String partname=f.getName();
						if(!partname.startsWith("."))
							{
							partname=partname.substring(0,partname.lastIndexOf('.'));
							try
								{
								int slicenum=Integer.parseInt(partname);
								loaderset.put(slicenum, newEvImage(f.getAbsolutePath()));
								}
							catch (NumberFormatException e)
								{
								Log.printError("partname: "+partname+" filename "+f.getName()+" framenum "+framenum,e);
								System.exit(1);
								}
							}
						}
					imageLoader.put(framenum, loaderset);
					}
			*/
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
				this.z=z;this.c=c;this.t=t;
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
				float matrix[][]={{0,0,0}};
				matrix[0][c]=1;
				RasterOp op=new BandCombineOp(matrix,new RenderingHints(null));
				
				byte[] b=omesession.getPlane(pixelid, z, t, c);
				
				
				//TODO
				
				op.filter(i.getRaster(), im.getRaster());
				
				return im;
				
				
				return null;
				}
		
			}
		}
	
	
	public void finalize()
		{
		System.out.println("finalize ost");
		}
	
	}

