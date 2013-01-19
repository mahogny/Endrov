package endrov.typeWorms;

import javax.vecmath.Vector2d;

import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvStack;
import endrov.util.math.EvDecimal;

/**
 * to be moved later...
 * 
 * @author mahogny
 *
 */
public class WormAlgo
	{
	
	
	public static void run(WormFit wfit, EvDecimal frame, EvChannel ch)
		{
		
		WormFit.WormFrame nf=wfit.frames.get(frame);
		if(nf==null)
			wfit.frames.put(frame,nf=new WormFit.WormFrame());

		EvStack stack=ch.getStack(frame);
		EvImagePlane evim=stack.getPlane(0); //This can be questioned
		EvPixels p=evim.getPixels(null);
		
		
		nf.centerPoints.add(new Vector2d(0,0));
		nf.centerPoints.add(new Vector2d(100,100));
		
		nf.thickness.add(10.0);
		nf.thickness.add(10.0);
		
		
		//TODO
		
		}
	}
