package endrov.flowImageStats;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.math.EvOpImagePowScalar;
import endrov.flowGenerateImage.GenerateSpecialImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Moving variance
 * 
 * @author Johan Henriksson
 */
public class EvOpVarianceCircle extends EvOpSlice1
	{
	private final int pw;

	public EvOpVarianceCircle(Number pw)
		{
		this.pw = pw.intValue();
		}

	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0], pw);
		}
	
	public static EvPixels apply(EvPixels in, int iradius)
		{
		EvPixels out;
		try
			{
			in=in.getReadOnly(EvPixelsType.DOUBLE);

			int w=in.getWidth();
			int h=in.getHeight();
			out = new EvPixels(EvPixelsType.DOUBLE,w,h);
			double[] outPixels=out.getArrayDouble();

			EvPixels sum0=EvOpSumCircle.apply(GenerateSpecialImage.genConstant(w, h, 1), iradius);
			EvPixels sum=EvOpSumCircle.apply(in, iradius);
			EvPixels sum2=EvOpSumCircle.apply(EvOpImagePowScalar.apply(in,2),iradius);

			double[] arrSum0=sum0.getArrayDouble();
			double[] arrSum=sum.getArrayDouble();
			double[] arrSum2=sum2.getArrayDouble();

			for(int i=0;i<arrSum.length;i++)
				{
				//Var(x)=E(x^2)-(E(x))^2
				double v1=arrSum2[i];
				double v2=arrSum[i];
				double area=arrSum0[i];
				outPixels[i]=(v1 - v2*v2/(double)area)/area;
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			throw new RuntimeException("aaaa");
			}


		return out;
		}

	}