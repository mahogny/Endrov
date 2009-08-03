package endrov.flowProjection;

import endrov.flow.EvOpStack1;
import endrov.flowBasic.math.EvOpImageAddImage;
import endrov.flowBasic.math.EvOpImageDivScalar;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;

/**
 * Projection: Average Z
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpAverageZ extends EvOpStack1
	{
	

	@Override
	public EvStack exec1(EvStack... p)
		{
		return averageZ(p[0]);
		}

	
	
	
	public static EvStack averageZ(EvStack in)
		{
		EvImage proto=in.firstEntry().snd();
		
		EvStack out=new EvStack();


		EvPixels ptot=new EvPixels(EvPixelsType.INT,proto.getPixels().getWidth(),proto.getPixels().getHeight());
		int numZ=in.getDepth();
		for(EvImage plane:in.getImages())
			ptot=new EvOpImageAddImage().exec1(ptot,plane.getPixels());
			//ImageMath.plus(ptot, plane.getValue().getPixels());

		ptot=new EvOpImageDivScalar(numZ).exec1(ptot);
		//ptot=ImageMath.div(ptot,numZ);
		
		EvImage imout=new EvImage();
		out.getMetaFrom(in);
		imout.setPixelsReference(ptot);
		
		//Lazy stack op will use all planes!
		
		for(int cz=0;cz<numZ;cz++)
			out.putInt(cz,imout.makeShadowCopy());
		//for(Map.Entry<EvDecimal, EvImage> plane:in.entrySet())
//			out.put(plane.getKey(), imout.makeShadowCopy());
			
		return out;
		}

	}
