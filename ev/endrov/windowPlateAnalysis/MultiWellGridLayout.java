package endrov.windowPlateAnalysis;

import java.util.Collection;

/**
 * One grid to show labels for
 */
public class MultiWellGridLayout
	{
	public int numNumber, numLetter;

	public int x,y;
	public int distance;
	

	
	/**
	 * Check if the wells follow a multi-well format - if so, return a suitable grid
	 */
	public static MultiWellGridLayout isMultiwellFormat(Collection<String> wellNames)
		{
		int maxletter=0;
		int maxnum=0;
		
		//Does this follow a multi-well format?  LettersNumbers
		for(String n:wellNames)
			{
			MultiWellPlateIndex pos=MultiWellPlateIndex.parse(n);
			if(pos==null)
				return null;
			
			if(pos.indexNumber>maxnum)
				maxnum=pos.indexNumber;
			if(pos.indexLetter>maxletter)
				maxletter=pos.indexLetter;
			}
		
		MultiWellGridLayout g=new MultiWellGridLayout();
		g.numLetter=maxletter;
		g.numNumber=maxnum;
		g.distance=PlateWindowView.imageSize+PlateWindowView.imageMargin;
		return g;
		}
	
	
	
	}