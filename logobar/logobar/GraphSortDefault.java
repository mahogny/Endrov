package logobar;
/**
 *  Commenced: Wednesday, April 13, 2005
 *  Author: Johan Koch
 *  
 *  GraphSortGapsOnTop is part of LogoBar
 *  
 */

import java.util.*;

/**@brief The default concrete sorting style
 * class. Puts the gaps always on top of the Graph
 */
public class GraphSortDefault implements GraphSort {
    
    private int iMaxAAEntries;
  
    public  int getMaxAAEntries() {return iMaxAAEntries;}
  
    private GraphTable sortGapsOnTop(int seqLength, double [][] aaHeightTable, double[][] freqTable){
	iMaxAAEntries = 0;
	GraphTable graphTable = new GraphTable();
	graphTable.iGraph = new Vector[seqLength];
	int maxHeight = 0;
	System.out.print("Seq length:");
	System.out.println(seqLength);
	for(int i = 0; i< seqLength;i++){
	    graphTable.iGraph[i] = new Vector(5,2);
	    int tmp_entries = 0;
	    for(int j = 0; j < 26;j++){
		double tmp = aaHeightTable[i][j];
		if(tmp > 0){
		    tmp_entries++;
		    char AA    = (char) (j+65);
		    int height = (int) (tmp * 250F);
		    if(height > maxHeight)
			maxHeight = height;
		    double freq = freqTable[i][j];
		    LogoEntry l_ent = new LogoEntry(AA, height, freq);
		    graphTable.iGraph[i].add(l_ent);
		}
		
	    }
	    LogoUtil.vecSort(graphTable.iGraph[i]);
	    double gap = aaHeightTable[i][26];
	    if(gap > 0){
		char AA    = '-';
		int height = (int) (gap*250);
		if(height > maxHeight)
		    maxHeight = height;
		double freq = freqTable[i][26];
		LogoEntry l_ent = new LogoEntry(AA, height,freq);
		graphTable.iGraph[i].add(0, l_ent);         // Put the gaps at the beginning of the vector.
		                                            // Hence on top of the graph since the graph 
		                                            // is drawn in reversed order.
	    }
	    graphTable.iGraph[i].trimToSize();
	    if(tmp_entries >= iMaxAAEntries) iMaxAAEntries = tmp_entries;
	}
	
	graphTable.iMaxHeight = maxHeight;
	return graphTable;
    }	
    
     private GraphTable sortGapsBottom(int seqLength, double[][] aaHeightTable, double[][] freqTable){
	iMaxAAEntries = 0;
	GraphTable graphTable = new GraphTable();
	graphTable.iGraph = new Vector[seqLength];
	int maxHeight = 0;
	for(int i = 0; i< seqLength;i++){
	    graphTable.iGraph[i] = new Vector(5,2);
	    int tmp_entries = 0;
	    for(int j = 0; j < 26;j++){
		double tmp = aaHeightTable[i][j];

		if(tmp > 0){
		    char AA    = (char) (j+65);
		    int height = (int) (tmp * 250);
		    double freq = freqTable[i][j];
		    LogoEntry l_ent = new LogoEntry(AA, height,freq);

		    if(height > maxHeight)
			maxHeight = height;
		  
		    graphTable.iGraph[i].add(l_ent);
		}
		
		
	    }
	    LogoUtil.vecSort(graphTable.iGraph[i]);
	    double gap = aaHeightTable[i][26];
	    if(gap > 0){
		char AA    = '-';
		int height = (int) (gap*250);
		if(height > maxHeight)
		    maxHeight = height;
		double freq = freqTable[i][26];
		LogoEntry l_ent = new LogoEntry(AA, height,freq);
		graphTable.iGraph[i].add(l_ent);
	    }
	    graphTable.iGraph[i].trimToSize();
	    if(tmp_entries >= iMaxAAEntries) iMaxAAEntries = tmp_entries;
	}
	
	graphTable.iMaxHeight = maxHeight;
	return graphTable;
    }	

    public GraphTable sort(int seqLength, double [][] aaHeightTable, double[][] freqTable, int gapsPlacement){
	switch (gapsPlacement){
	case GAPS_IN_BOTTOM:
	    return sortGapsBottom(seqLength, aaHeightTable, freqTable);

	default:
	    return sortGapsOnTop(seqLength, aaHeightTable, freqTable);

	}
    }
    
}

    

