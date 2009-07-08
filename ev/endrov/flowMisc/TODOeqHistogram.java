package endrov.flowMisc;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class TODOeqHistogram
	{

	public byte[] makeLookup(BufferedImage in)
		{
		WritableRaster rin=in.getRaster();
		
		//Count each color
		int[] colorcount=new int[256];
		int totalCount=rin.getWidth()*rin.getHeight();
		int width=rin.getWidth();
		int[] pix=new int[width];
		for(int ah=0;ah<rin.getHeight();ah++)
			{
			rin.getSamples(0, ah, width, 1, 0, pix);
			for(int aw=0;aw<width;aw++)
				colorcount[pix[aw]]++;
			}
		
		int lowerLimit=0;
//		int lowerLimit=130;
		
		//Eliminate lower part
		int lowerCount=0;
		for(int i=0;i<lowerLimit;i++)
			{
			lowerCount+=colorcount[i];
			colorcount[i]=0;
			}
		totalCount-=lowerCount;
		
		//Cumulative sum. Do separate from renormalization for precision
		for(int i=1;i<256;i++)
			colorcount[i]+=colorcount[i-1];

		//Normalize
		byte[] b=new byte[256];
		if(totalCount!=0)
			for(int i=0;i<256;i++)
				b[i]=(byte)Math.round(255.0*colorcount[i]/(double)totalCount);
		/*
		for(int i=0;i<256;i++)
			System.out.print(" "+b[i]);
		System.out.println("");
		*/
		return b;
		}
	
	
	/**
	 * 		ByteLookupTable table=new ByteLookupTable(0,makeLookup(in));
		LookupOp bcfilter=new LookupOp(table,null);
		bcfilter.filter(in,out);

	 */
	
	}
