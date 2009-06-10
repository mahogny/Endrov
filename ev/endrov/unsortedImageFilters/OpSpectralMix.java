package endrov.unsortedImageFilters;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import endrov.imageset.EvPixels;

/**
 * Spectral mixing: Transform input colors by matrix.
 * Given channels c_i, apply c'=M c for each pixel. 
 * Number of output channels need not be the same as number of input channels.
 * <br/>
 * O(numPixel numInputColors numOutputColors)
 * 
 * @author Johan Henriksson
 *
 */
public class OpSpectralMix
	{
	private DoubleMatrix2D m;
	public OpSpectralMix(DoubleMatrix2D m)
		{
		this.m = m;
		}
	public EvPixels exec(EvPixels... p)
		{
		return map(p, m)[0];
		//TODO!!!!!! need to support multiple return arguments
		}
	
	
	
	public static EvPixels[] map(EvPixels[] in, DoubleMatrix2D m)
		{
		if(in.length==0)
			return new EvPixels[]{};
		
		int numOutput=m.rows();
		int numInput=m.columns();
		
		double pin[][]=new double[in.length][];
		double pout[][]=new double[in.length][];
		EvPixels out[]=new EvPixels[in.length];
		int w=in[0].getWidth();
		int h=in[0].getHeight();
		int numpix=w*h;
		for(int i=0;i<in.length;i++)
			{
			pin[i]=in[i].convertTo(EvPixels.TYPE_DOUBLE, true).getArrayDouble();
			out[i]=new EvPixels(EvPixels.TYPE_DOUBLE,w,h);
			pout[i]=out[i].getArrayDouble();
			}
		
		if(in.length!=numInput)
			throw new RuntimeException("Matrix dimensions does not correspond to number of input channels");
		
		DenseDoubleMatrix1D cprim=new DenseDoubleMatrix1D(numOutput);
		double[] cArr=new double[numInput];
		for(int i=0;i<numpix;i++)
			{
			//Assemble color input 
			for(int j=0;j<numInput;j++)
				cArr[j]=pin[j][i];
			DenseDoubleMatrix1D c=new DenseDoubleMatrix1D(cArr);

			//Multiply colors
			m.zMult(c, cprim);

			//Store output
			double[] tout=cprim.toArray();
			for(int j=0;j<numOutput;j++)
				pout[j][i]=tout[j];
			}
		
		return out;
		}
	
	}
