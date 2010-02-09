package endrov.frivolous.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.jdom.Document;
import org.jdom.Element;

import endrov.util.EvXmlUtil;

class Cell {

	private Document document;
	//private BufferedImage input_image;
	private ComplexArray immobile_sum_array, psf_fft, output_array;
	private ComplexArray[] immobile_arrays, mobile_arrays;
	private Diffusion[] diffusers;
	private Settings_new settings;
	private int w, h;
	Fourier fft;
	PSF psf;
	
	public Cell(String path){
		w = 512;
		h = 512;
		try {
			document = EvXmlUtil.readXML(new File(path+"settings.xml"));
		} catch (IOException e) {
			//FIXME: Filen kunde inte läsas
			System.out.println("Filen kunde inte läsas");
		} catch (org.jdom.JDOMException e){
			//FIXME: Filen innehåller fel
			System.out.println("Filen innehåller fel");
		}
		
		parseSettings(document.getRootElement().getChild("settings"));
		
		List<Element> staticlayers = getLayersFromDocument(true);
		List<Element> mobilelayers = getLayersFromDocument(false);
		
		immobile_arrays = new ComplexArray[staticlayers.size()];
		mobile_arrays = new ComplexArray[mobilelayers.size()];
		diffusers = new Diffusion[mobilelayers.size()];

		int i = 0;
		for(Element e:staticlayers)
		{
			String layer_filename = path + e.getChild("image").getAttributeValue("src");
			
			BufferedImage layer_image = null;
			try {
				layer_image = ImageIO.read(new File(layer_filename));
			} catch (IOException ex) {
				//FIXME: Bilden som xml-filen hänvisar till kan inte läsas
				System.out.println("Bilden som xml-filen hänvisar till kan inte läsas.");
			}
			
			immobile_arrays[i] = new ComplexArray(
					ImageHandeling.getColorArray(
							layer_image,
							ImageHandeling.COLOR_RED),
					null, w, h);
			i++;
		}
		immobile_sum_array = ComplexLib.getRealSum(immobile_arrays);
		
		i = 0;
		for(Element e:mobilelayers)
		{
			String layer_filename = path + e.getChild("image").getAttributeValue("src");
			float speed = Float.parseFloat(e.getChild("speed").getTextTrim());
			String mask_filename = path + e.getChild("stencil").getAttributeValue("src");

			BufferedImage layer_image = null;
			BufferedImage mask_image = null;
			try {
				layer_image = ImageIO.read(new File(layer_filename));
				mask_image = ImageIO.read(new File(mask_filename));
			} catch (IOException ex) {
				//FIXME: Bilderna som xml-filen hänvisar till kan inte läsas
				System.out.println("Bilderna som xml-filen hänvisar till kan inte läsas:\n"+layer_filename+"\n"+mask_filename);
			}
			
			mobile_arrays[i] = new ComplexArray(
					ImageHandeling.getColorArray(
							layer_image,
							ImageHandeling.COLOR_RED),
					null, w, h);
			
			diffusers[i] = new Diffusion(
					ComplexLib.getFilledArray(mobile_arrays[i], 1f),
					ImageHandeling.getIntColorArray(mask_image, ImageHandeling.COLOR_RED),
					speed);
			
			i++;
		}

		fft = new Fourier(w, h);
		psf = new DiffractionPSF();
		updatePSF();
		for(i=0; i<diffusers.length;i++){
			(new Thread(diffusers[i])).start();
			//diffusers[i].bleach();
		}
	}
	
	private void parseSettings(Element settings){
		this.settings = new Settings_new();
	}

	@SuppressWarnings("unchecked")
	private List<Element> getLayersFromDocument(boolean staticlayer) {
		Element root = document.getRootElement();
		Element slice1 = root.getChild("slice");
		Element fluorophore = slice1.getChild("fluorophore");
		return staticlayer ? fluorophore.getChildren("staticlayer") : fluorophore.getChildren("mobilelayer");
	}
	
	public void updatePSF(){
		ComplexArray psf_array = new ComplexArray(psf.createPSF(settings), null, w, h);
		psf_fft = fft.forward(psf_array, true);
	}
	
	public BufferedImage getImage() {

		Timer timer = new Timer("Cell, getImage");
		
		ComplexArray[] diffused_arrays = new ComplexArray[diffusers.length];
		for(int i=0; i<diffusers.length; i++)
			diffused_arrays[i] = ComplexLib.getRealMultiplication(diffusers[i].getDiffusedArray(), mobile_arrays[i]);
		
		ComplexArray diffused_sum_array = ComplexLib.getRealSum(diffused_arrays);
		
		timer.show("Mobile arrays calculated");
		
		ComplexArray total_array = ComplexLib.getRealAddition(diffused_sum_array, immobile_sum_array);

		ComplexArray input_fft = fft.forward(total_array, true);

		timer.show("FFT on image");

		ComplexArray output_fft = ComplexLib.getComplexMultiplication(input_fft, psf_fft);

		timer.show("Complex mult");

		output_array = fft.backward(output_fft);

		timer.show("iFFT");
		ComplexArray output_noise = PoissonNoise.addRealNoise(output_array, settings);

		timer.show("Poisson noise");
		return ImageHandeling.getImageFromComplex(output_noise, false);

	}
	
	public Settings_new getSettings(){
		return settings;
	}
}
