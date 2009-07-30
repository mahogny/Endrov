package endrov.flowImageStats;

import endrov.flow.EvOpStack1;
import endrov.flowBasic.math.EvOpImageComplexMulImage;
import endrov.flowFourier.EvOpFourierComplexInverse3D;
import endrov.flowFourier.EvOpFourierRealForwardFull3D;
import endrov.flowFourier.EvOpRotateImage3D;
import endrov.flowGenerateImage.GenerateSpecialImage;
import endrov.imageset.EvStack;
import endrov.util.Tuple;

/**
 * Convolve by gaussian
 * 
 * Complexity, same as FourierTransform
 */
public class EvOpConvGaussian3D extends EvOpStack1
	{
	private Number sigmaX, sigmaY, sigmaZ;

	public EvOpConvGaussian3D(Number sigmaX, Number sigmaY, Number sigmaZ)
		{
		this.sigmaX = sigmaX;
		this.sigmaY = sigmaY;
		this.sigmaZ = sigmaZ;
		}

	public EvStack exec1(EvStack... p)
		{
		return apply(p[0],sigmaX.doubleValue(),sigmaY.doubleValue(),sigmaZ.doubleValue());
		}
	
	public static EvStack apply(EvStack in, double sigmaX, double sigmaY, double sigmaZ)
		{
		int w=in.getWidth();
		int h=in.getHeight();
		int d=in.getDepth();
		
		/*
		 * 
		System.out.println("incoming stack "+in);
		
		System.out.println("Generating kernel");
		EvStack kernel=GenerateSpecialImage.genGaussian3D(sigmaX, sigmaY, sigmaZ, w, h, d);
		System.out.println(kernel);
		kernel.forceEvaluation();
		System.gc();
		
		System.out.println("Rotating");
		kernel=EvOpRotateImage3D.apply(kernel, null,null, null); //old kernel is collected here
		System.out.println(kernel);
		kernel.forceEvaluation();
		System.gc();
		
		System.out.println("fft kernel");
		EvStack[] ckernel=new EvOpFourierRealForwardFull3D().exec(kernel);
		ckernel[0].forceEvaluation();
		ckernel[1].forceEvaluation();
		System.out.println(ckernel[0]);
		System.out.println(ckernel[1]);
		System.gc();
		
		System.out.println("fft im");
		EvStack[] cin=new EvOpFourierRealForwardFull3D().exec(in);
		cin[0].forceEvaluation();
		cin[1].forceEvaluation();
		System.out.println(cin[0]);
		System.out.println(cin[1]);
		System.gc();
		
		System.out.println("mul");
		cin=new EvOpImageComplexMulImage().exec(cin[0],cin[1],ckernel[0],ckernel[1]);
//		EvStack[] mul=new EvOpImageComplexMulImage().exec(cin[0],cin[1],ckernel[0],ckernel[1]);
		cin[0].forceEvaluation();
		cin[1].forceEvaluation();
		System.out.println(cin[0]);
		System.out.println(cin[1]);
		System.gc();
		
		return EvOpFourierComplexInverse3D.transform(cin[0], cin[1],true).fst();
		
*/		
		
		System.out.println("Generating kernel");
		EvStack kernel=GenerateSpecialImage.genGaussian3D(sigmaX, sigmaY, sigmaZ, w, h, d);
		System.gc();
		
		System.out.println("Rotating");
		kernel=EvOpRotateImage3D.apply(kernel, null,null, null); //old kernel is collected here
		System.gc();
		
		System.out.println("fft kernel");
		Tuple<EvStack,EvStack> ckernel=EvOpFourierRealForwardFull3D.transform(kernel);
		System.gc();
		
		System.out.println("fft im");
		Tuple<EvStack,EvStack> cin=EvOpFourierRealForwardFull3D.transform(in);
		System.gc();
	
		/*
		System.out.println("mul");
		EvStack[] mul=new EvOpImageComplexMulImage().exec(cin.fst(),cin.snd(),ckernel.fst(),ckernel.snd());
		System.gc();
		return EvOpFourierComplexInverse3D.transform(mul[0], mul[1],true).fst();
*/
		System.out.println("mul");
		EvOpImageComplexMulImage.timesInPlaceDouble(cin.fst(),cin.snd(),ckernel.fst(),ckernel.snd());
		System.gc();
		return EvOpFourierComplexInverse3D.transform(cin.fst(),cin.snd(),true).fst();
		}
	}