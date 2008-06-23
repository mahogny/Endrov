package evplugin.imagesetImserv;

//note: renaming channel will require all EvImageOST to be renamed as well

import java.awt.image.*;
import java.io.*;
import java.util.TreeMap;

import evplugin.data.RecentReference;
import evplugin.ev.Log;
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

	private DataIF data;
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	/**
	 * Create a new recording
	 */
	public ImservImageset(DataIF omeimage)
		{
		this.data=omeimage;
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
	
	
	
	public boolean loadDatabaseCache(byte inp[])
		{
		try
			{
			BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(inp)));
		 
			String line=in.readLine();
			if(!line.equals("version1"))
				{
				Log.printLog("Image cache wrong version, ignoring");
				return false;
				}
			else
				{
				Log.printLog("Loading imagelist cache");
				
				channelImages.clear();
				int numChannels=Integer.parseInt(in.readLine());
				for(int i=0;i<numChannels;i++)
					{
					String channelName=in.readLine();
					int numFrame=Integer.parseInt(in.readLine());
					ChannelImages c=getChannel(channelName);
					if(c==null)
						{
						c=new Channel(meta.getCreateChannelMeta(channelName));
						channelImages.put(channelName,c);
						}
					
					for(int j=0;j<numFrame;j++)
						{
						int frame=Integer.parseInt(in.readLine());
						int numSlice=Integer.parseInt(in.readLine());
						TreeMap<Integer,EvImage> loaderset=c.imageLoader.get(frame);
						if(loaderset==null)
							{
							//A sorted linked list would make set generation linear time
							loaderset=new TreeMap<Integer,EvImage>();
							c.imageLoader.put(frame, loaderset);
							}
						
						for(int k=0;k<numSlice;k++)
							{
							String s=in.readLine();
							if(s.startsWith("ext"))
								s=in.readLine(); //We don't have to care about extensions
							int slice=Integer.parseInt(s);

							EvImage evim=((Channel)c).newEvImage(slice,frame,channelName);
							loaderset.put(slice, evim);
							}
						}
					}
				return true;
				}
			}
		catch(FileNotFoundException e)
			{
			return false;
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return false;
			}
		}

	
	/**
	 * Scan recording for channels and build a file database
	 */
	public void buildDatabase()
		{
		try
			{
			System.out.println("building imageset");
			DataIF.CompressibleDataTransfer ilist=data.getImageList();
			loadDatabaseCache(ilist.data);
			
			//Set metadata
			loadImagesetXmlMetadata(new ByteArrayInputStream(data.getRMD().data));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}

		/*
		int numc=3;
		for(int c=0;c<numc;c++)
			{
			String channelName="ch"+c;
			Channel ch=new Channel(meta.getCreateChannelMeta(channelName));
			ch.scanFiles(channelName);
			channelImages.put(channelName,ch);
			}
		*/
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
					DataIF.ImageTransfer transfer=data.getImage(c, t, z);
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

