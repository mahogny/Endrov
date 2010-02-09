package endrov.frivolous.model;

public class GaussianPSF extends PSF {

	@Override
	public float[] createPSF(Settings_new settings) {

		int w = settings.w;
		int h = settings.h;
		int wc = w>>1;
		int hc = h>>1;
		
		double z = settings.offsetZ;
		
		double sigma = 1 / (2*Math.PI*settings.na) *1000;
		double twoS2 = 2*sigma*sigma;
		
		float[] psf = new float[w*h];
		float total = 0;
		
		for(int x=0; x<w;x++)
			for(int y=0; y<h;y++){
				double d2=get_d2(x-wc,y-hc,z, settings.pixelSpacing);
				double d22=get_d2(x-wc,y-hc,0, settings.pixelSpacing);
				psf[x*h+y]=(float)Math.exp(-d2/twoS2);
				total+=(float)Math.exp(-d22/twoS2);
			}
		
		System.out.println(total);
		for(int x=0; x<w;x++)
			for(int y=0; y<h;y++)
				psf[x*h+y]/=total;
		
		shiftQuadrants(w, h, psf);
		
		return psf;
	}

	private double get_d2(int xPixel, int yPixel, double z, double pixelSpacing){
		double x = xPixel * pixelSpacing;
		double y = yPixel * pixelSpacing;
		return (x*x+y*y+z*z);
	}	
}