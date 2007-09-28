package evplugin.lociBioformats;

import java.awt.image.*;




import evplugin.imageset.*;

import loci.formats.*;

/**
 * Loader of images using LOCI Bioformats
 * 
 * @author Johan Henriksson (binding only)
 */
public class ImageLoaderBioformats implements EvImage
	{
	private int id;
	private Integer subid;
	private IFormatReader imageReader;
	private String sourceName;

	
	public ImageLoaderBioformats(IFormatReader imageReader, int id, Integer subid, String sourceName)
		{
		this.imageReader=imageReader;
		this.id=id;
		this.subid=subid;
		this.sourceName=sourceName;
		}

	
	public String sourceName()
		{
		return sourceName;
		}

	
	/**
	 * Load the image
	 */
	public BufferedImage loadImage()
		{
		try
			{
			BufferedImage i=imageReader.openImage(id);
			
			if(subid!=null)
				{
				int w=i.getWidth();
				int h=i.getHeight();
				
				BufferedImage im=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

				WritableRaster rastin=i.getRaster();
				WritableRaster rastout=im.getRaster();
				int[] pixin=new int[3*w*h];
				int[] pixout=new int[w*h];
				rastin.getPixels(0, 0, w, h, pixin);				
				for(int j=0;j<w*h;j++)
					pixout[j]=pixin[j*3+subid];
				rastout.setPixels(0, 0, w, h, pixout);				

				return im;
				}
				
			return i;
			}
		catch(Exception e)
			{
			evplugin.ev.Log.printError("Failed to read image "+sourceName(),e);
			return null;
			}
		}
	}

