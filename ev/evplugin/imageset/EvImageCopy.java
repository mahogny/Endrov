package evplugin.imageset;

import java.awt.image.*;

public class EvImageCopy extends EvImage
	{
	private int binning;
	private double dispX,dispY;
	private double resX,resY;
	private BufferedImage im;
	
	public EvImageCopy(EvImage src)
		{
		binning=src.getBinning();
		dispX=src.getDispX();
		dispY=src.getDispY();
		resX=src.getResX();
		resY=src.getResY();
		
		//Deep-copy image
		im=src.getJavaImage();
		WritableRaster raster = im.copyData( null );
		im = new BufferedImage( im.getColorModel(), raster, im.isAlphaPremultiplied(), null );
		}

	public int getBinning()
		{
		return binning;
		}

	public double getDispX()
		{
		return dispX;
		}

	public double getDispY()
		{
		return dispY;
		}

	public double getResX()
		{
		return resX;
		}

	public double getResY()
		{
		return resY;
		}

	protected BufferedImage loadJavaImage()
		{
		return im;
		}

	}
