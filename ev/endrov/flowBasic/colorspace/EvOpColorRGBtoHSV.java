package endrov.flowBasic.colorspace;


import endrov.flow.EvOpSlice;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * 
 * Equations from http://en.wikipedia.org/wiki/HSL_and_HSV
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpColorRGBtoHSV extends EvOpSlice
	{
	
	public EvPixels[] exec(ProgressHandle progh, EvPixels... p)
		{
		EvPixels inR=p[0];
		EvPixels inG=p[1];
		EvPixels inB=p[2];
		
		double[] arrInR=inR.convertToDouble(true).getArrayDouble();
		double[] arrInG=inG.convertToDouble(true).getArrayDouble();
		double[] arrInB=inB.convertToDouble(true).getArrayDouble();
		
		EvPixels retH=new EvPixels(EvPixelsType.DOUBLE, inR.getWidth(),inR.getHeight());
		EvPixels retS=new EvPixels(EvPixelsType.DOUBLE, inG.getWidth(),inR.getHeight());
		EvPixels retV=new EvPixels(EvPixelsType.DOUBLE, inB.getWidth(),inR.getHeight());
		
		
		if(inG.getWidth()!=inR.getWidth() || inB.getWidth()!=inR.getWidth() ||
				inG.getHeight()!=inR.getHeight() || inB.getHeight()!=inR.getHeight())
			throw new RuntimeException("Sizes of input channels does not match");
		

		double[] arrOutH=retH.getArrayDouble();
		double[] arrOutS=retS.getArrayDouble();
		double[] arrOutV=retV.getArrayDouble();

		
		for(int i=0;i<arrInR.length;i++)
			calc(i,
					arrInR, arrInG, arrInB,
					arrOutH, arrOutS, arrOutV);
		
		return new EvPixels[]{retH, retS, retV};
		}
	
	private static void calc(
			int i,
			double[] inR, double[] inG, double[] inB, 
			double[] outH, double[] outS, double[] outV)
		{
		double r=inR[i];
		double g=inG[i];
		double b=inB[i];
		
		double M=Math.max(Math.max(r, g),b);
		double m=Math.min(Math.min(r, g),b);
		
		double C=M - m;

		////////////// Calculate H. This could probably be made faster by not comparing to M, but rather have the whole if()-tree to find max
		double H;
		if(C==0)
			H=0; //Undefined - but have to return something
		else if(M==r)
			{
			H=(g-b)/C;  // mod 6
			if(H<0)
				H+=6;
			}
		else if(M==g)
			H=(b-r)/C+2;
		else //if(M==b)
			H=(r-g)/C+4;
		H*=60;  //This multiplication could be removed to speed things up, but it's a really small burden compared to all other operations

		double V=M;
		
		double S;
		if(C==0)
			S=0;
		else
			S=C/V;
		
		outH[i]=H;
		outS[i]=S;
		outV[i]=V;
		}


	@Override
	public int getNumberChannels()
		{
		return 3;
		}
	
	}