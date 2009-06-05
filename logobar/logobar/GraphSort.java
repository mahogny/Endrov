package logobar;



/** @brief This class provides an interface
 * to the variuos graph sorting styles of LogoBar<br>
 * One of the main purposes of LogoBar is the
 * ability to sort the Graph in various ways:
 * <ul><li> Gaps on top DEFAULT</li>
 * <li> Gaps in the bottom </li>
 * <li> Sort by group </li>
 * </ul>More sorting styles can be added by sub classing
 * this super class
 */
public interface GraphSort {

    public static final int GAPS_ON_TOP    = 1;
    public static final int GAPS_IN_BOTTOM = 2;
    public static final int SORT_DEFAULT   = 1;
    public static final int SORT_BY_GROUP  = 2;
    
    

    /** Sorts a the Graph
     * @return The sorted graph
     */
    public GraphTable sort(int seqLength, double[][] aaHeightTable, double[][] freqTable, int gapsPlacement);
    
    public int  getMaxAAEntries();
   
}
    

    
