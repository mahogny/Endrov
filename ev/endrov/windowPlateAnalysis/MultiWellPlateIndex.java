package endrov.windowPlateAnalysis;


/**
 * Index of a well when using structured multi-well formats 
 */
public class MultiWellPlateIndex
	{
	public int indexNumber;
	public int indexLetter;
	
	public MultiWellPlateIndex(int indexNumber, int indexLetter)
		{
		this.indexNumber = indexNumber;
		this.indexLetter = indexLetter;
		}
	
	

	/**
	 * Parse a well name. Returns null if it fails
	 */
	public static MultiWellPlateIndex parse(String n)
		{
		n=n.toUpperCase();
		int ac=0;
		while(ac<n.length() && Character.isLetter(n.charAt(ac)))
			ac++;
		String letterpart=n.substring(0,ac);
		String numberpart=n.substring(ac);
		while(ac<n.length() && Character.isDigit(n.charAt(ac)))
			ac++;
		if(ac!=n.length() || letterpart.isEmpty() || numberpart.isEmpty() || letterpart.length()!=1)
			return null;
		
		int num=Integer.parseInt(numberpart);
		int letter=letterpart.charAt(0)-'A'+1;
		return new MultiWellPlateIndex(num, letter);
		}
	}
