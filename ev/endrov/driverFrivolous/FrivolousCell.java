/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverFrivolous;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jdom.Document;
import org.jdom.Element;

import endrov.util.EvXmlUtil;

/**
 * @author David Johansson, Arvid Johansson, Johan Henriksson
 */
public class FrivolousCell
	{

	
	private FrivolousComplexArray /*immobile_sum_array,*/ psf_fft, output_array;
	private FrivolousComplexArray[] immobile_arrays, mobile_arrays;
	public FrivolousDiffusion[] diffusers;
	private FrivolousSettingsNew settings;
	private int w, h;
	private FrivolousFourier fft;
	private FrivolousPSF psf;
	private boolean needNewPSF=true; 

	public FrivolousCell(File path)
		{
		//w = 512;
		//h = 512;
		File fSettings=new File(path, "settings.xml");
		Document document;
		List<Element> staticlayers=new LinkedList<Element>();
		List<Element> mobilelayers=new LinkedList<Element>();
		try
			{
			document = EvXmlUtil.readXML(fSettings);
			parseSettings(document.getRootElement().getChild("settings"));
			staticlayers = getLayersFromDocument(document, true);
			mobilelayers = getLayersFromDocument(document, false);
			}
		catch (IOException e)
			{
			System.out.println(fSettings+ " could not be read");
			}
		catch (org.jdom.JDOMException e)
			{
			System.out.println(fSettings+" contains errors");
			}



		immobile_arrays = new FrivolousComplexArray[staticlayers.size()];
		mobile_arrays = new FrivolousComplexArray[mobilelayers.size()];
		diffusers = new FrivolousDiffusion[mobilelayers.size()];

		int i = 0;
		for (Element e : staticlayers)
			{
			File layer_filename = new File(path, e.getChild("image").getAttributeValue("src"));

			BufferedImage layer_image = null;
			try
				{
				layer_image = ImageIO.read(layer_filename);
				}
			catch (IOException ex)
				{
				System.out.println("Cannot read "+layer_filename);
				}

			immobile_arrays[i] = new FrivolousComplexArray(FrivolousUtility.getColorArray(
					layer_image, FrivolousUtility.COLOR_RED), null, layer_image.getWidth(), layer_image.getHeight());
			i++;
			w=layer_image.getWidth();
			}
//		immobile_sum_array = FrivolousComplexLib.getRealSum(immobile_arrays);

		i = 0;
		for (Element e : mobilelayers)
			{
			File layer_filename = new File(path, e.getChild("image")
					.getAttributeValue("src"));
			float speed = Float.parseFloat(e.getChild("speed").getTextTrim());
			File mask_filename = new File(path, e.getChild("stencil")
					.getAttributeValue("src"));

			BufferedImage layer_image = null;
			BufferedImage mask_image = null;
			try
				{
				layer_image = ImageIO.read(layer_filename);
				mask_image = ImageIO.read(mask_filename);
				}
			catch (IOException ex)
				{
				System.out.println("Cannot find:\n"+layer_filename+"\n"+mask_filename);
				}

			mobile_arrays[i] = new FrivolousComplexArray(FrivolousUtility.getColorArray(
					layer_image, FrivolousUtility.COLOR_RED), null, layer_image.getWidth(), layer_image.getHeight());

			diffusers[i] = new FrivolousDiffusion(FrivolousComplexLib.getFilledArray(
					mobile_arrays[i], 1f), FrivolousUtility.getIntColorArray(mask_image,
					FrivolousUtility.COLOR_RED), speed);

			i++;
			}

		fft = new FrivolousFourier(1024, 1024);
		psf = new FrivolousPSFDiffraction();
		updatePSF();
		for (i = 0; i<diffusers.length; i++)
			{
			new Thread(diffusers[i]).start();
			}
		}

	//TODO: Parse the xml-file
	private void parseSettings(Element settings)
		{
		this.settings = new FrivolousSettingsNew();
		}

	@SuppressWarnings("unchecked")
	private List<Element> getLayersFromDocument(Document document, boolean staticlayer)
		{
		Element root = document.getRootElement();
		Element slice1 = root.getChild("slice");
		Element fluorophore = slice1.getChild("fluorophore");
		if(staticlayer)
			return fluorophore.getChildren("staticlayer");
		else
			return fluorophore.getChildren("mobilelayer");
		}

	public synchronized void updatePSF()
		{
		needNewPSF=true;
		}
	
	private void calcPSF()
		{
		if(needNewPSF)
			{
			FrivolousComplexArray psf_array = new FrivolousComplexArray(psf.createPSF(settings), null, 1024,1024);
			psf_fft = fft.forward(psf_array, true);
			needNewPSF=false;
			}
		}

	
	public synchronized int[] getImage(int offsetX, int offsetY, int imageWidth, int imageHeight, boolean simulatePSF, boolean simulateNoise)
		{
		calcPSF();

		FrivolousTimer timer = new FrivolousTimer("Cell, getImage");

		FrivolousComplexArray[] diffused_arrays = new FrivolousComplexArray[diffusers.length];
		for (int i = 0; i<diffusers.length; i++)
			diffused_arrays[i] = FrivolousComplexLib.getRealMultiplication(diffusers[i]
					.getDiffusedArray(), mobile_arrays[i]);

		FrivolousComplexArray diffused_sum_array = FrivolousComplexLib.getRealSum(diffused_arrays);

		timer.show("Mobile arrays calculated");

		FrivolousComplexArray immobile_sum_array = FrivolousComplexLib.getRealSum(immobile_arrays);
		FrivolousComplexArray total_array = FrivolousComplexLib.getRealAddition(diffused_sum_array,	immobile_sum_array);
		
		FrivolousComplexArray crop_array = FrivolousComplexLib.getCrop(total_array,1024,1024,512-((int)(offsetX+0.5)),512-((int)(offsetY+0.5)));
		
		//Can disable PSF simulation for performance
		if(simulatePSF)
			{
			FrivolousComplexArray input_fft = fft.forward(crop_array, true);

			timer.show("FFT on image");
			FrivolousComplexArray output_fft = FrivolousComplexLib.getComplexMultiplication(input_fft, psf_fft);

			timer.show("Complex mult");
			output_array = FrivolousComplexLib.getCrop(fft.backward(output_fft),imageWidth,imageHeight,256,256);
			
			timer.show("iFFT");
			}
		else
			{
			output_array = FrivolousComplexLib.getCrop(crop_array,imageWidth,imageHeight,256,256);
			}

		//FrivolousComplexArray output_noise = FrivolousUtility.addRealNoise(FrivolousComplexLib.getCrop(output_array,imageWidth,imageHeight,256,256), settings);
		if(simulateNoise)
			output_array = FrivolousUtility.addRealNoise(output_array, settings);

		timer.show("Poisson noise");
		
		int[] retarr=new int[output_array.real.length];
		for(int i=0;i<output_array.real.length;i++)
			retarr[i]=(int)output_array.real[i];

		return retarr;
		//return FrivolousUtility.getImageFromComplex(output_noise, false);
		}

	/**
	 * Bleach an arbitrary ROI
	 */
	public synchronized void bleachImmobile(int[] roi, int roiWidth, int roiHeight, int offsetX, int offsetY, float bleachFactor)
		{
		offsetX+=768;//temp
		offsetY+=768; 
//		System.out.println("bleach factor roi immobile 1 "+bleachFactor+"   "+roiWidth+" "+roiHeight+" "+offsetX+"  "+offsetY+" "+w);
		for(int y=0;y<roiHeight;y++)
			for(int x=0;x<roiWidth;x++)
				if(roi[x+y*roiWidth]!=0) 
					for(FrivolousComplexArray c:immobile_arrays)
						c.real[(x+offsetX) + (y+offsetY)*w] *= bleachFactor;
		
		}

	/**
	 * Bleach a square ROI - typically the area the camera focuses on
	 */
	public synchronized void bleachImmobile(int roiWidth, int roiHeight, int offsetX, int offsetY, float bleachFactor)
		{
		offsetX+=768;//temp
		offsetY+=768; 
//		System.out.println("bleach factor immobile 2 "+bleachFactor+"   "+roiWidth+" "+roiHeight+" "+offsetX+"  "+offsetY+" "+w);
		for(int y=0;y<roiHeight;y++)
			for(int x=0;x<roiWidth;x++)
				for(FrivolousComplexArray c:immobile_arrays)
					c.real[(x+offsetX) + (y+offsetY)*w] *= bleachFactor;
		}

	public void stop()
		{
		for (int i = 0; i<diffusers.length; i++)
			{
			diffusers[i].stop();
			}
		}
	
	public FrivolousSettingsNew getSettings()
		{
		return settings;
		}
	}
