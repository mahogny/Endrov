package endrov.recording;

import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.roi.LineIterator;
import endrov.roi.ROI;
import endrov.roi.LineIterator.LineRange;
import endrov.util.EvDecimal;


/**
 * PMTs and scanning mirror, whatever needed to run a scanning confocal that is not covered elsewhere.
 * 
 * Can emulate a camera if the additional features are not needed
 * 
 * @author Johan Henriksson
 */
public class HWImageScannerUtil 
	{
	
	public int[] makeROI(HWImageScanner scanner, ROI roi, double stageX, double stageY)
		{
		int w=scanner.getWidth();
		int h=scanner.getHeight();
		
		int[] ret=new int[w*h];
		
		EvPixels p=new EvPixels(EvPixelsType.UBYTE, w, h); //TODO avoid this allocation 
		EvImage image=new EvImage(p);
		EvStack stack=new EvStack();

		
		//TODO invert resmag?
		stack.resX=1.0/scanner.getResMagX();  //um/px
		stack.resY=1.0/scanner.getResMagY();
		stack.resZ=EvDecimal.ONE;
		
		String channel="foo";
		EvDecimal frame=EvDecimal.ZERO;
		EvDecimal z=EvDecimal.ZERO;
		
		//TODO how to offset?
		
		//Fill bitmap ROI
		LineIterator it=roi.getLineIterator(stack, image, channel, frame, z);
		while(it.next())
			{
			int y=it.y;
			for(LineRange r:it.ranges)
				for(int x=r.start;x<r.end;x++)  //< or <=?
					ret[y*w+x]=1;
			}
		
		return ret;
		}
	
	//public static void foo(){}
	}
