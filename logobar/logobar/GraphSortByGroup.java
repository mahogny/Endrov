package logobar;
/**
 *  Commenced: Friday, April 15, 2005
 *  Author: Johan Koch
 *  
 *  GraphSortByGroup is part of LogoBar
 *  
 */

import java.util.*;

/** A graph sorting class. 
 * Sorts the graph with respect of the group an amino
 * acid belongs to.
 */
public class GraphSortByGroup implements GraphSort {

    /** A Group entry class
     * The group vector that is used by the sort function
     * of GraphSortByGroup is a vector of GroupEntry
     * objects. It's really a POD with a contructor
     * a sorting function
     */
    public class GroupEntry implements Comparable<GroupEntry>{
	public int    iTotalHeight;
	public Vector<LogoEntry> iAAVec;
	public int    iGroup;

	public GroupEntry() {
	    iTotalHeight = 0;
	}
	
	public int compareTo(GroupEntry rhs){
	    int rhs_tot_height = ((GroupEntry)rhs).iTotalHeight;
	    if(iTotalHeight < rhs_tot_height)
		return -1;
	    else if(iTotalHeight == rhs_tot_height)
		return 0;
	    else 
		return 1;
	}
	
    }

    private int iMaxAAEntries;
  
    public  int getMaxAAEntries() {return iMaxAAEntries;}
  

    public GraphTable sort(int seqLength, double [][] aaHeightTable, double[][] freqTable, int gapsPlacement){
	iMaxAAEntries = 0;
	switch (gapsPlacement){
	case GAPS_IN_BOTTOM:
	    return sortGapsBottom(seqLength, aaHeightTable, freqTable);

	default:
	    return sortGapsOnTop(seqLength, aaHeightTable, freqTable);

	}
    }
    private GraphTable sortGapsOnTop(int seqLength, double[][] aaHeightTable, double[][] freqTable){
	
	GraphTable graphTable = new GraphTable();
	graphTable.iGraph     = new Vector[seqLength];
	int maxHeight         = 0;
	for(int i = 0; i < seqLength;i++){
	    Vector groupVec = new Vector(5,2);
	    graphTable.iGraph[i] = new Vector(5,2);
	    int tmp_entries = 0;
	    for(int j = 0; j < 26;j++){
		double tmp = aaHeightTable[i][j];
		
		if(tmp > 0){
		    tmp_entries++;
		    char AA    = (char) (j+65);
		    int height = (int) (tmp * 250);
		    double freq = freqTable[i][j];
		    LogoEntry lEntry = new LogoEntry(AA, height, freq);
		  
		  
		    //See if this group is in the map
		    int aaGroup = ColorHandler.getAAColorGroup(AA);
		
		    System.out.println(aaGroup);
		    
		    GroupEntry gEntry = findGroup(aaGroup, groupVec);
		    if(gEntry != null){
			//Group exists
		
			gEntry.iTotalHeight += height;
			gEntry.iAAVec.add(lEntry);
		    }
		    else{
		
			//Create a new group
			GroupEntry newGmap = new GroupEntry();
			newGmap.iTotalHeight = height;
			newGmap.iAAVec    = new Vector(3,1);
			newGmap.iAAVec.add(lEntry);
			newGmap.iGroup = aaGroup;
			groupVec.add(newGmap);
		
		    }
		}
	    }
	   
	    //Sort the group vector
	    sortGroupVec(groupVec);
	    
	    //Now put the values of the sorted group
	    //Vector in the Graph Table
	    Iterator it = groupVec.iterator();
	    while(it.hasNext()){
		GroupEntry tmp_ent = (GroupEntry) it.next();
		Vector vec = tmp_ent.iAAVec;
		Iterator it2 = vec.iterator();
		while(it2.hasNext()){
		    LogoEntry logo = (LogoEntry) it2.next();
		    int height = logo.getHeight();
		    
		    if(height > maxHeight)
			maxHeight = height;
		    graphTable.iGraph[i].add(logo);
				   
		}
	    }
	    //Finally add th gaps
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

	    
	}//end for i to seqLength
	
	
	graphTable.iMaxHeight = maxHeight;
	return graphTable;
	
    }

    private GraphTable sortGapsBottom(int seqLength, double[][] aaHeightTable, double[][] freqTable){
	
	GraphTable graphTable = new GraphTable();
	graphTable.iGraph     = new Vector[seqLength];
	int maxHeight         = 0;
	for(int i = 0; i < seqLength;i++){
	    Vector groupVec = new Vector(5,2);
	    graphTable.iGraph[i] = new Vector(5,2);
	    int tmp_entries = 0;
	    for(int j = 0; j < 26;j++){
		double tmp = aaHeightTable[i][j];
		
		if(tmp > 0){
		    tmp_entries++;
		    char AA    = (char) (j+65);
		    int height = (int) (tmp * 250);
		    double freq = freqTable[i][j];
		    LogoEntry l_ent = new LogoEntry(AA, height, freq);
		  
		    
		    //See if this group is in the map
		    int aaGroup = ColorHandler.getAAColorGroup(AA);
		
		    System.out.println(aaGroup);
		    
		    GroupEntry gEntry = findGroup(aaGroup, groupVec);
		    if(gEntry != null){
			//Group exists
		
			gEntry.iTotalHeight += height;
			gEntry.iAAVec.add(l_ent);
		    }
		    else{
		
			//Create a new group
			GroupEntry newGmap = new GroupEntry();
			newGmap.iTotalHeight = height;
			newGmap.iAAVec    = new Vector(3,1);
			newGmap.iAAVec.add(l_ent);
			newGmap.iGroup = aaGroup;
			groupVec.add(newGmap);
		
		    }
		}
	    }
	   
	    //Sort the group vector
	    sortGroupVec(groupVec);
	    
	    //Now put the values of the sorted group
	    //Vector in the Graph Table
	    Iterator it = groupVec.iterator();
	    while(it.hasNext()){
		GroupEntry tmp_ent = (GroupEntry) it.next();
		Vector vec = tmp_ent.iAAVec;
		Iterator it2 = vec.iterator();
		while(it2.hasNext()){
		    LogoEntry logo = (LogoEntry) it2.next();
		    int height = logo.getHeight();
		    
		    if(height > maxHeight)
			maxHeight = height;
		    graphTable.iGraph[i].add(logo);
				   
		}
	    }
	    //Finally add th gaps
	    double gap = aaHeightTable[i][26];
	    if(gap > 0){
		char AA    = '-';
		int height = (int) (gap*250);
		if(height > maxHeight)
		    maxHeight = height;
		double freq = freqTable[i][26];
		LogoEntry l_ent = new LogoEntry(AA, height, freq);
		
		graphTable.iGraph[i].add(l_ent);
	    }  
	    if(tmp_entries >= iMaxAAEntries) iMaxAAEntries = tmp_entries;
	    
	    graphTable.iGraph[i].trimToSize();
	    
	    
	}//end for i to seqLength
	
	
	graphTable.iMaxHeight = maxHeight;
	return graphTable;
	
      }
    
    private void sortGroupVec(Vector<GroupEntry> groupVec){
	
	//First sort the Groups
	LogoUtil.vecSort(groupVec);          
	
	//Now sort the group vectors internally
	Iterator it = groupVec.iterator();
	while(it.hasNext()){
	   
	    GroupEntry tmp = (GroupEntry) it.next();
	    Vector aa_vec  = tmp.iAAVec;
	    LogoUtil.vecSort(aa_vec);
	}
    }

    private GroupEntry findGroup(int aaGroup, Vector vec){
	Iterator it = vec.iterator();
	while(it.hasNext()){
	    GroupEntry entry = (GroupEntry) it.next();
	    if(entry.iGroup == aaGroup)
		return entry;
	}
	return null;
    }
    
}


