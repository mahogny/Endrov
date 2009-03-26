package endrov.imagesetImserv;

//note: renaming channel will require all EvImageOST to be renamed as well

import java.awt.image.*;
import java.io.*;
import java.util.*;

import org.jdom.Document;

import bioserv.SendFile;
import bioserv.imserv.DataIF;
import bioserv.imserv.ImservConnection;

import endrov.data.EvData;
import endrov.data.EvIOData;
import endrov.data.EvPath;
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
			ifdata.setMetadata(trans);
			
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
	public boolean loadDatabaseCache(Imageset imageset, InputStream inp)
		{
		String blobid=imageset.ostBlobID;
		try
			{
			BufferedReader in = new BufferedReader(new InputStreamReader(inp));
		 
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
					EvChannel c=imageset.getCreateChannel(channelName);
					
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
							//TODO properly fill in metadata
							evim.resX=imageset.resX;
							evim.resY=imageset.resY;
							evim.dispX=c.dispX;
							evim.dispY=c.dispY;
							evim.binning=c.chBinning;
							
							
							//TODO fill up with metadata here
							
							
							SliceIO io=new SliceIO(blobid, slice,frame,channelName);
							evim.io=io;
//							System.out.println("Got image "+evim+" ch "+channelName);
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
			System.out.println("Loading metadata");
			data.loadXmlMetadata(ifdata.getMetadata().getInputStream());

			//This is a big bottle neck. It would be nice if it could be postponed using new get/set mechanisms
			System.out.println("building imageset");
			for(Map.Entry<EvPath, Imageset> ime:data.getIdObjectsRecursive(Imageset.class).entrySet())
				{
				System.out.println("found imageset "+ime.getKey());
				DataIF.CompressibleDataTransfer ilist=ifdata.getImageCache(ime.getValue().ostBlobID);
				if(ilist!=null)
					loadDatabaseCache(ime.getValue(), ilist.getInputStream());
				else
					System.out.println("Did not get database cache");
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	
	
	/**
	 * Slice I/O - get one image from server
	 */
	private class SliceIO implements EvIOImage
		{
		private EvDecimal z,t;
		private String c;
		private String blobid;
		public SliceIO(String blobid, EvDecimal z, EvDecimal t, String c)
			{
			this.blobid=blobid;
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
				DataIF.ImageTransfer transfer=ifdata.getImage(blobid, c, t, z);
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

