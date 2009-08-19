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
import endrov.ev.EvLog;
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
	
	
	
	/*
	public void foo()
		{
		EvIODataOST.loadDatabaseCacheMap33(EvChannel ch, HashMap<EvDecimal,HashMap<EvDecimal,File>> c, InputStream cachefile, File blobFile)

		}
	*/
	
	//TODO share cache code with OST
	public boolean loadDatabaseCacheV3d2(Imageset imageset, InputStream inp)
		{
		String blobid=imageset.ostBlobID;
		try
			{
			BufferedReader in = new BufferedReader(new InputStreamReader(inp));
		 
			String line=in.readLine();
			if(!line.equals("version1"))
				{
				EvLog.printLog("Image cache wrong version, ignoring");
				return false;
				}
			else
				{
				EvLog.printLog("Loading imagelist cache");

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
						Map<String,String> frameMeta=c.getMetaFrame(frame);
						int numSlice=Integer.parseInt(in.readLine());
						EvStack stack=c.imageLoader.get(frame);
						if(stack==null)
							{
							//A sorted linked list would make set generation linear time
							stack=new EvStack();
							c.imageLoader.put(frame, stack);
							}
						//TODO proper metadata
//						stack.resX=imageset.resX;
	//					stack.resY=imageset.resY;
//						stack.resX=(1.0/imageset.resX);///c.chBinning;
//						stack.resY=(1.0/imageset.resY);///c.chBinning;
						//resZ TODO
						
						stack.resX=c.defaultResX;
						stack.resY=c.defaultResY;
						stack.resZ=c.defaultResZ;
						
						//TODO override
						if(frameMeta.get("resX")!=null)
							stack.resX=Double.parseDouble(frameMeta.get("resX"));
						if(frameMeta.get("resY")!=null)
							stack.resY=Double.parseDouble(frameMeta.get("resY"));
						if(frameMeta.get("resZ")!=null)
							stack.resZ=new EvDecimal(frameMeta.get("resZ"));
						
						stack.dispX=c.defaultDispX;
						stack.dispY=c.defaultDispY;
						stack.dispZ=c.defaultDispZ;
//						stack.binning=c.chBinning;
						
						for(int k=0;k<numSlice;k++)
							{
							String s=in.readLine();
							if(s.startsWith("ext"))
								s=in.readLine(); //We don't have to care about extensions
							EvDecimal slice=new EvDecimal(s);
							
							EvImage evim=new EvImage();
							
							SliceIO io=new SliceIO(blobid, slice,frame,channelName);
							evim.io=io;
//							System.out.println("Got image "+evim+" ch "+channelName);
							stack.put(slice, evim);
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
					loadDatabaseCacheV3d2(ime.getValue(), ilist.getInputStream());
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
		
		public EvPixels loadJavaImage()
			{
			//it is the server side responsibility to make byte_gray?
			//			BufferedImage im=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
			try
				{
				DataIF.ImageTransfer transfer=ifdata.getImage(blobid, c, t, z);
				if(transfer!=null)
					{
					BufferedImage bim=SendFile.getImageFromBytes(transfer.data);
					if(bim!=null)
						return new EvPixels(bim);
					else
						return null;
					}
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

