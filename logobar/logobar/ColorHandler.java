package logobar;
/**
 *  Commenced: Wednesday, November 17, 2004
 *  Asa Perez-Bercoff
 *  LogoEntry.java holds the graph entry data in the
 *  logo bar
 *  
 *
 */

import java.util.*; // Contains the Map class.
import java.awt.*;
//import org.jibble.epsgraphics.*; // Java EPS Graphics2D package obtained at http://www.jibble.org/epsgraphics/


public class ColorHandler {
    
    private static Hashtable AminoTab = new Hashtable(21);
    
    private static Color ColorTab[]   =  new Color[21];

    public static Color  LBlue        = new Color(80,80,255);
    
    public static Color  Purple      = new Color(180,115,225);

    public static Color Brown1       = new Color(185,138,120);
    
    public static Color LogoBlue     = new Color(100,140,255);
    
    public static Color DarkGreen    = new Color(0,210,100);
    
    static{
	
	ColorTab[0]  = Color.red;
	ColorTab[1]  = ColorHandler.LogoBlue;
	ColorTab[2]  = Color.green;
	ColorTab[3]  = ColorHandler.Purple;
	ColorTab[4]  = Color.yellow;
	ColorTab[5]  = Color.lightGray;
	ColorTab[6]  = ColorHandler.Brown1;
	ColorTab[7]  = Color.white;
	ColorTab[8]  = Color.white;
	ColorTab[9]  = Color.white;
	ColorTab[10] = Color.white;
	ColorTab[11] = Color.white;
	ColorTab[12] = Color.white;
	ColorTab[13] = Color.white;
	ColorTab[14] = Color.white;
	ColorTab[15] = Color.white;
	ColorTab[16] = Color.white;
	ColorTab[17] = Color.white;
	ColorTab[18] = Color.white;
	ColorTab[19] = Color.white;
	ColorTab[20] = Color.white;
	
	
	AminoTab.put("A", new Integer(5));
	AminoTab.put("C", new Integer(4));
	AminoTab.put("D", new Integer(0));
	AminoTab.put("E", new Integer(0));
	AminoTab.put("F", new Integer(3));
	AminoTab.put("G", new Integer(6));
	AminoTab.put("H", new Integer(1));
	AminoTab.put("I", new Integer(5));
	AminoTab.put("K", new Integer(1));
	AminoTab.put("L", new Integer(5));
	AminoTab.put("M", new Integer(5));
	AminoTab.put("N", new Integer(2));
	AminoTab.put("P", new Integer(6));
	AminoTab.put("Q", new Integer(2));
	AminoTab.put("R", new Integer(1));
	AminoTab.put("S", new Integer(2));

	AminoTab.put("T", new Integer(2));
	AminoTab.put("V", new Integer(5));
	AminoTab.put("W", new Integer(3));
	AminoTab.put("Y", new Integer(3));
	//AminoTab.put("-", new Integer(9)); // Gaps are black.
	AminoTab.put("-", new Integer(20)); // Gaps are white with a black frame.
	
    }
	
    public static Color getAAColor(char AA){
	String toStr = String.valueOf(AA);
	int colorIdx = ((Integer)AminoTab.get(toStr)).intValue();  //first get color index 
	//from Amino hash table
	if( colorIdx > 20 || colorIdx < 0)
	    return new Color(2,2,2);
	else{
	    return ColorTab[colorIdx];
	}
    }
    
    /** Sets the color of an Amino Acid
     * @param AA The amino acid represented as an char
     * @param idx The index to the color in the ColorTab
     */
    public static void setAAColorGroup(char AA, int idx){
	String toStr = String.valueOf(AA);
	if(AminoTab.containsKey(toStr))
	    AminoTab.put(toStr,new Integer(idx));
    }
    
    public static void setColorGroup(int idx, Color color){
	if(idx >= 0 && idx < 21)
	    ColorTab[idx] = color;
    }
    
    public static void setColorGroup(int idx, int r, int g, int b){
	if(idx >= 0 && idx < 21)
	    ColorTab[idx] = new Color(r,g,b);
    }

    /** Gets the group an amino acid belongs to
     * @param AA The amino acid
     * @return Index to the group that the amino acid
     * belongs to.
     */
    public static int getAAColorGroup(char AA){
	String toStr = String.valueOf(AA);
	if(AminoTab.containsKey(toStr))
	    return ((Integer)AminoTab.get(toStr)).intValue();
	return 0;
    }
    
    public static Color getColor(int idx){
	if(idx >= 0 && idx < 21)
	    return ColorTab[idx];
	return Color.black;
    }
}

    

	
    
