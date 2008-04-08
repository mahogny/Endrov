package evplugin.imagesetBioformats;

import java.awt.*;
import java.awt.image.*;
import loci.formats.*;

import evplugin.imageset.*;


/**
 * Loader of images using LOCI Bioformats
 * 
 * @author Johan Henriksson (binding only)
 */
public abstract class EvImageBioformats extends EvImage
	{
	private int id;
	private Integer subid;
	private IFormatReader imageReader;
	private String sourceName;

	
	public EvImageBioformats(IFormatReader imageReader, int id, Integer subid, String sourceName)
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
	public BufferedImage loadJavaImage()
		{
		try
			{
			BufferedImage i=imageReader.openImage(id);
			
			//System.out.println(""+i+" "+i.getWidth());
			
			//This hack fixes Leica
			if(subid==null)
				subid=0;
			else
				System.out.println("subid "+subid);
			
			if(subid!=null)
				{
				int w=i.getWidth();
				int h=i.getHeight();
				
				/*
				BufferedImage im=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
				WritableRaster rastin=i.getRaster();
				WritableRaster rastout=im.getRaster();
				int[] pixels=new int[w*h];
				rastin.getSamples(0, 0, w, h, subid, pixels);				
				rastout.setSamples(0, 0, w, h, 0, pixels);				
				 */
				/*
				int[] pixin=new int[3*w*h];
				int[] pixout=new int[w*h];
				rastin.getPixels(0, 0, w, h, pixin);				
				for(int j=0;j<w*h;j++)
					pixout[j]=pixin[j*3+subid];
				rastout.setPixels(0, 0, w, h, pixout);				
				*/
				

				BufferedImage im=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
				
				float matrix[][]={{0,0,0}};
				if(i.getRaster().getNumBands()==1)
					matrix=new float[][]{{0/*,0*/}};
				else if(i.getRaster().getNumBands()==2)
					matrix=new float[][]{{0,0/*,0*/}};
				else if(i.getRaster().getNumBands()==3)
					matrix=new float[][]{{0,0,0/*,0*/}};
				
				matrix[0][subid]=1;
				RasterOp op=new BandCombineOp(matrix,new RenderingHints(null));
				op.filter(i.getRaster(), im.getRaster());
				
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

