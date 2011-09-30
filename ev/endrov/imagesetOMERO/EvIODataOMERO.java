/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imagesetOMERO;

import java.io.File;
import java.sql.Timestamp;
import java.util.Set;

import loci.common.DataTools;
import loci.formats.FormatTools;


import omero.ServerError;
import omero.api.RawPixelsStorePrx;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PixelsData;

import endrov.data.EvData;
import endrov.data.EvIOData;
import endrov.data.EvPath;
import endrov.data.RecentReference;
import endrov.data.EvData.FileIOStatusCallback;
import endrov.imageset.EvChannel;
import endrov.imageset.EvIOImage;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;

/**
 * Support for proprietary formats through LOCI Bioformats
 * 
 * @author Johan Henriksson (binding to library only)
 */
public class EvIODataOMERO implements EvIOData
	{

	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	
	
	/******************************************************************************************************
	 *                               Image I/O class                                                      *
	 *****************************************************************************************************/

	

	public class SliceIO extends EvIOImage
		{
		private final long pixelsId;
		private final int c;
		private final int z;
		private final int t;
		private final int w, h;
		private final int pixelType;
		
		public SliceIO(ImageData image, int c, int z, int t, int w, int h, int pixelType)
			{
			PixelsData pixels = image.getDefaultPixels();
			pixelsId = pixels.getId();
			this.c=c;
			this.z=z;
			this.t=t;
			this.w=w;
			this.h=h;
			this.pixelType=pixelType;
			
			//image.getFormat()  //This is the FILE format
			
			
			
			}
		
		@Override
		protected EvPixels eval(ProgressHandle ph)
			{
			
			try
				{
				RawPixelsStorePrx store = connection.getEntry().createRawPixelsStore();
				store.setPixelsId(pixelsId, false);
				byte[] plane = store.getPlane(z, c, t);
				store.close();

				System.out.println("plane size "+plane.length+"  wh "+w+"   "+h);
				
				boolean isSigned=store.isSigned();
				
				boolean little=false;
				
				if(pixelType==FormatTools.INT32)
					{
					
					int[] arr=new int[w*h];
					for(int i=0;i<arr.length;i++)
						arr[i]=DataTools.bytesToInt(plane, i*4, little);
					
					//int[] arr=DataTools.bytesToInt(plane, little);
					
					
					return EvPixels.createFromInt(w, h, arr);
					}
				else
					{
					return EvPixels.createFromUByte(w, h, plane);
					
					}
				
				//TODO convert to suitable evpixel!!!!
				
				
				//int[] arr=DataTools.bytesToInt(plane, little);
				
				//NOTE: the array might be longer than w*h
				
				}
			catch (ServerError e)
				{
				e.printStackTrace();
				return null;
				}
			}

		}
	
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	

	public OMEROConnection connection;
	
	
	//ImageData image;
	
	
	//Map: (omero image, channel) <-> endrov evchannel path
	
	public static class OMEROChannelMap
		{
		public EvPath path;
		public long imageId;
		public int omeroChannel;
		}
	
	
	public EvIODataOMERO(OMEROConnection connection, EvData d)
		{
		this.connection=connection;
		buildDatabase(d);
		}
	
	public void populateChannel(ImageData imd, EvChannel ch, OMEROChannelMap m)
		{
		int omeroChannel=m.omeroChannel;

		Timestamp acqDate=imd.getAcquisitionDate();

		PixelsData pixels = imd.getDefaultPixels();  
		int sizeZ = pixels.getSizeZ(); 
		int sizeT = pixels.getSizeT();
		int w = pixels.getSizeX(); 
		int h = pixels.getSizeY(); 
		
		System.out.println("Pixel type "+pixels.getPixelType());
		
		int type = FormatTools.pixelTypeFromString(pixels.getPixelType());

		
		for(int t=0;t<sizeT;t++)
			{
			
			EvStack stack=new EvStack();
			for(int z=0;z<sizeZ;z++)
				{
				SliceIO io=new SliceIO(imd, omeroChannel, z, t, w, h, type);


				EvImage evim=new EvImage();
				evim.io=io;
				stack.putInt(z, evim);
				}

			stack.resX=pixels.getPixelSizeX();
			stack.resY=pixels.getPixelSizeY();
			stack.resZ=pixels.getPixelSizeZ();
			
			
			//TODO metadata, frame!
			
			EvDecimal frame=new EvDecimal(t);
			ch.putStack(frame, stack);
			}
			
		
		}

	public void buildDatabase(EvData d)
		{
		
		Imageset imset=new Imageset();
		d.metaObject.put("im", imset);
		
		try
			{
			Set<DatasetData> datasets=connection.getDatasetsForUser(connection.getMyUserId());
			
			for(DatasetData dataset:datasets)
				{
				Set<ImageData> images=connection.getImagesForDataset(dataset.getId());


				for(ImageData imd:images)
					{
					
					
					
					PixelsData pixels=imd.getDefaultPixels();
					int sizeC=pixels.getSizeC();
					
					for(int omeroChannel=0;omeroChannel<sizeC;omeroChannel++)
						{
						EvChannel ch=new EvChannel();
						
						OMEROChannelMap m=new OMEROChannelMap();
						
						m.imageId=imd.getId();
						m.omeroChannel=omeroChannel;
						
						String chanName=imd.getName()+"-"+omeroChannel;
						
						populateChannel(imd, ch, m);
						
						imset.metaObject.put(chanName, ch);
						}
					}
				

				}
			}
		catch (ServerError e)
			{
			e.printStackTrace();
			}
		
		
		
		}
	
	
	

	public File datadir()
		{
		return null;
		}

	public String getMetadataName()
		{
		return null;
		}

	public RecentReference getRecentEntry()
		{
		return null;
		}

	public void saveData(EvData d, FileIOStatusCallback cb)
		{
		}
	
	}
