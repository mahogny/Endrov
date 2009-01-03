package endrov.imagesetImserv;

//note: renaming channel will require all EvImageOST to be renamed as well

import java.awt.image.*;
import java.io.*;
import java.util.Collection;
import java.util.TreeMap;

import org.jdom.Document;

import bioserv.SendFile;
import bioserv.imserv.DataIF;
import bioserv.imserv.ImservConnection;

import endrov.data.EvData;
import endrov.data.EvIOData;
import endrov.data.RecentReference;
import endrov.ev.Log;
import endrov.imageset.*;
import endrov.util.EvDecimal;
import endrov.util.EvXmlUtil;


/**
 * Support for ImServ
 * @author Johan Henriksson
 */
public class EvIODataImserv implements EvIOData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private DataIF ifdata;
	private ImservConnection conn;
//	private EvData data;
	private String cachedName="<name not checked>";
	
	/**
	 * Create a new recording
	 */
	public EvIODataImserv(EvData data, DataIF omeimage, ImservConnection conn)
		{
		this.ifdata=omeimage;
		this.conn=conn;
		buildDatabase(data);
		}
	
	/**
	 * Get name description of this metadata
	 */
	public String toString()
		{
		return getMetadataName();
		}
	public String getMetadataName()
		{
		return cachedName;
		}

	


	/**
	 * Get directory for this imageset where any datafiles can be stored
	 */
	public File datadir()
		{
		return null;
		}

	/**
	 * Save meta for all channels
	 */
	public void saveData(EvData data, EvData.FileIOStatusCallback cb)
		{
		try
			{
			ByteArrayOutputStream os=new ByteArrayOutputStream();

			Document document=data.saveXmlMetadata();
			EvXmlUtil.writeXmlData(document, os);
			DataIF.CompressibleDataTransfer trans=new DataIF.CompressibleDataTransfer();
			trans.compression=DataIF.CompressibleDataTransfer.NONE;
			trans.data=os.toByteArray();
			ifdata.setRMD(trans);
			
			//TODO also save image data
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		}
	
	public RecentReference getRecentEntry()
		{
			//TODO @ in strings? or /? will fuck up. need escaping
		String path="imserv://"+conn.user+"@"+conn.host+"/"+getMetadataName();
		System.out.println("made path "+path);
		return new RecentReference(getMetadataName(),path);
		}
	
	
	//TODO share cache code with OST
	public boolean loadDatabaseCache(EvData data, byte inp[])
		{
		//TODO support multiple imset
		Collection<Imageset> imsets=data.getObjects(Imageset.class);
		Imageset imageset;
		if(imsets.isEmpty())
			data.metaObject.put("im", imageset=new Imageset());
		else
			imageset=imsets.iterator().next();
		
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

				//TODO don't clear, check if channels are gone
				//channelImages.clear();
				int numChannels=Integer.parseInt(in.readLine());
				for(int i=0;i<numChannels;i++)
					{
					String channelName=in.readLine();
					int numFrame=Integer.parseInt(in.readLine());
					Imageset.ChannelImages c=imageset.createChannel(channelName);
					
					for(int j=0;j<numFrame;j++)
						{
						EvDecimal frame=new EvDecimal(in.readLine());
						int numSlice=Integer.parseInt(in.readLine());
						TreeMap<EvDecimal,EvImage> loaderset=c.imageLoader.get(frame);
						if(loaderset==null)
							{
							//A sorted linked list would make set generation linear time
							loaderset=new TreeMap<EvDecimal,EvImage>();
							c.imageLoader.put(frame, loaderset);
							}
						
						for(int k=0;k<numSlice;k++)
							{
							String s=in.readLine();
							if(s.startsWith("ext"))
								s=in.readLine(); //We don't have to care about extensions
							EvDecimal slice=new EvDecimal(s);

							
							
							EvImage evim=new EvImage();
							//TODO fill up with metadata here
							
							
							SliceIO io=new SliceIO(slice,frame,channelName);
							evim.io=io;
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
	public void buildDatabase(EvData data)
		{
		try
			{
			cachedName=ifdata.getName();
			}
		catch (Exception e)
			{
			cachedName="<name lookup failure>";
			}
		try
			{
			System.out.println("building imageset");
			DataIF.CompressibleDataTransfer ilist=ifdata.getImageList();
			if(ilist!=null)
				loadDatabaseCache(data, ilist.data);

//			long time=System.currentTimeMillis();
			//Set metadata
			data.loadXmlMetadata(new ByteArrayInputStream(ifdata.getRMD().data));
//			loadImagesetXmlMetadata(new ByteArrayInputStream(ifdata.getRMD().data));
//			System.out.println(""+(System.currentTimeMillis()-time));
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
	
	

/*
	protected ChannelImages internalMakeChannel(Imageset.Channel ch)
		{
		return new Channel(ch);
		}
	*/	
	
	

	
	
	
	
	public class SliceIO implements EvIOImage
		{
		EvDecimal z,t;
		String c;
		public SliceIO(EvDecimal z, EvDecimal t, String c)
			{
			this.z=z;
			this.t=t;
			this.c=c;
			}
		
		public BufferedImage loadJavaImage()
			{
			//it is the server side responsibility to make byte_gray?
			//			BufferedImage im=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
			try
				{
				DataIF.ImageTransfer transfer=ifdata.getImage(c, t, z);
				if(transfer!=null)
					return SendFile.getImageFromBytes(transfer.data);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			return null;			
			}

		}
	
	
	public void finalize()
		{
		System.out.println("finalize imserv imageset");
		}
	
	}

