/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverFrivolous;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

	private Document document;
	private FrivolousComplexArray immobile_sum_array, psf_fft, output_array;
	private FrivolousComplexArray[] immobile_arrays, mobile_arrays;
	private FrivolousDiffusion[] diffusers;
	private FrivolousSettingsNew settings;
	private int w, h;
	private FrivolousFourier fft;
	private FrivolousPSF psf;

	public FrivolousCell(File path)
		{
		w = 512;
		h = 512;
		try
			{
			document = EvXmlUtil.readXML(new File(path, "settings.xml"));
			}
		catch (IOException e)
			{
			System.out.println("Filen kunde inte läsas");
			}
		catch (org.jdom.JDOMException e)
			{
			System.out.println("Filen innehåller fel");
			}

		parseSettings(document.getRootElement().getChild("settings"));

		List<Element> staticlayers = getLayersFromDocument(true);
		List<Element> mobilelayers = getLayersFromDocument(false);

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
					layer_image, FrivolousUtility.COLOR_RED), null, w, h);
			i++;
			}
		immobile_sum_array = FrivolousComplexLib.getRealSum(immobile_arrays);

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
					layer_image, FrivolousUtility.COLOR_RED), null, w, h);

			diffusers[i] = new FrivolousDiffusion(FrivolousComplexLib.getFilledArray(
					mobile_arrays[i], 1f), FrivolousUtility.getIntColorArray(mask_image,
					FrivolousUtility.COLOR_RED), speed);

			i++;
			}

		fft = new FrivolousFourier(w, h);
		psf = new FrivolousPSFDiffraction();
		updatePSF();
		for (i = 0; i<diffusers.length; i++)
			{
			new Thread(diffusers[i]).start();
			}
		}

	private void parseSettings(Element settings)
		{
		this.settings = new FrivolousSettingsNew();
		}

	@SuppressWarnings("unchecked")
	private List<Element> getLayersFromDocument(boolean staticlayer)
		{
		Element root = document.getRootElement();
		Element slice1 = root.getChild("slice");
		Element fluorophore = slice1.getChild("fluorophore");
		if(staticlayer)
			return fluorophore.getChildren("staticlayer");
		else
			return fluorophore.getChildren("mobilelayer");
		}

	public void updatePSF()
		{
		FrivolousComplexArray psf_array = new FrivolousComplexArray(psf.createPSF(settings), null, w,h);
		psf_fft = fft.forward(psf_array, true);
		}

	public BufferedImage getImage()
		{

		FrivolousTimer timer = new FrivolousTimer("Cell, getImage");

		FrivolousComplexArray[] diffused_arrays = new FrivolousComplexArray[diffusers.length];
		for (int i = 0; i<diffusers.length; i++)
			diffused_arrays[i] = FrivolousComplexLib.getRealMultiplication(diffusers[i]
					.getDiffusedArray(), mobile_arrays[i]);

		FrivolousComplexArray diffused_sum_array = FrivolousComplexLib.getRealSum(diffused_arrays);

		timer.show("Mobile arrays calculated");

		FrivolousComplexArray total_array = FrivolousComplexLib.getRealAddition(diffused_sum_array,
				immobile_sum_array);
		FrivolousComplexArray input_fft = fft.forward(total_array, true);

		timer.show("FFT on image");
		FrivolousComplexArray output_fft = FrivolousComplexLib.getComplexMultiplication(input_fft,
				psf_fft);

		timer.show("Complex mult");
		output_array = fft.backward(output_fft);

		timer.show("iFFT");
		FrivolousComplexArray output_noise = FrivolousUtility.addRealNoise(output_array,
				settings);

		timer.show("Poisson noise");
		return FrivolousUtility.getImageFromComplex(output_noise, false);

		}

	public FrivolousSettingsNew getSettings()
		{
		return settings;
		}
	}
