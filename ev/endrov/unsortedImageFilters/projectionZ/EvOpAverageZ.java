package endrov.unsortedImageFilters.projectionZ;

import java.util.Map;

import endrov.flow.EvOpStack1;
import endrov.flow.std.math.EvOpImageAddImage;
import endrov.flow.std.math.EvOpImageDivScalar;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;

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


		EvPixels ptot=new EvPixels(EvPixels.TYPE_INT,proto.getPixels().getWidth(),proto.getPixels().getHeight());
		int numZ=in.getDepth();
		for(Map.Entry<EvDecimal, EvImage> plane:in.entrySet())
			ptot=new EvOpImageAddImage().exec1(ptot,plane.getValue().getPixels());
			//ImageMath.plus(ptot, plane.getValue().getPixels());

		ptot=new EvOpImageDivScalar(numZ).exec1(ptot);
		//ptot=ImageMath.div(ptot,numZ);
		
		EvImage imout=new EvImage();
		out.getMetaFrom(in);
		imout.setPixelsReference(ptot);
		
		//Lazy stack op will use all planes!
		
		for(Map.Entry<EvDecimal, EvImage> plane:in.entrySet())
			out.put(plane.getKey(), imout.makeShadowCopy());
			
		return out;
		}

	}
