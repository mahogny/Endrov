package endrov.imagesetOME;

import java.awt.image.*;
import java.io.*;
import java.util.List;
import endrov.data.EvData;
import endrov.data.EvIOData;
import endrov.data.RecentReference;
import endrov.imageset.*;
import endrov.util.EvDecimal;


/**
 * Support for the native OST file format
 * @author Johan Henriksson
 */
public class EvIODataOME implements EvIOData
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
	 */
	public EvIODataOME(EvData data, EVOME omesession, ome.model.core.Image omeimage)
		{
		this.omesession=omesession;
		this.omeimage=omeimage;
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
		String imageset=omeimage.getName();
		if(imageset.indexOf('/')>=0)
			imageset=imageset.substring(imageset.lastIndexOf('/')+1);
		return imageset;
		}

	/**
	 * Get directory for this imageset where any datafiles can be stored
	 */
	public File datadir()
		{
		return null;
		}

	/**
	 * Save data
	 */
	public void saveData(EvData data, EvData.FileIOStatusCallback cb)
		{
		}
	
	public RecentReference getRecentEntry()
		{
		return null;
		}
	
	
	/**
	 * Scan recording for channels and build a file database
	 */
	public void buildDatabase(EvData data)
		{
		//TODO Set metadata 
		
		Imageset im;
		if(data.getObjects(Imageset.class).isEmpty())
			data.metaObject.put("im",im=new Imageset());
		else
			im=data.getObjects(Imageset.class).iterator().next();
		
		
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
//			Channel ch=new Channel(meta.getCreateChannelMeta(channelName));
			
			EvChannel ch=im.getCreateChannel(channelName);
			
			ch.imageLoader.clear();
			int numframe=pixel.getSizeT();
			int numz=pixel.getSizeZ();
			for(int frame=0;frame<numframe;frame++)
				{
				EvStack stack=new EvStack();
				stack.binning=1;
				stack.resX=1;
				stack.resY=1;
				stack.dispX=0;
				stack.dispY=0;
				//TODO ome metadata
				//TODO bd real resolution					
				for(int z=0;z<numz;z++)
					{
					EvImage evim=new EvImage();
					evim.io=new EvImageOME(pixel, z, frame, c);
					stack.put(new EvDecimal(z), evim);
					}
				ch.imageLoader.put(new EvDecimal(frame), stack);
				}
			
			}
		
		}
	
	
	
	
	private class EvImageOME implements EvIOImage
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
		public EvPixels loadJavaImage()
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
			return new EvPixels(im);
			}
	
		}
		

	
	
	public void finalize()
		{
		System.out.println("finalize ome");
		}
	
	}

