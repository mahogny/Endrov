package logobar;
/** From Jan Skansholm's book 'Java Direkt med Swing' 3rd edition, p. 499
 *  $Log: not supported by cvs2svn $
 *  Revision 1.1.1.1  2005/04/12 12:58:15  johan
 *  Started LogoBar project
 *
 *  Revision 1.4  2005/01/26 15:46:45  pbasa
 *
 *   Changed: Default color for GAPS is now set to white
 *   ---------------------------------------------------------------------
 *
 *  Revision 1.3  2004/12/21 09:45:05  pbasa
 *  *** empty log message ***
 *
 */

import java.io.*;
import java.util.*;

public class NameFilter
    extends javax.swing.filechooser.FileFilter{

    // instance variables:
    private String[] iSuf; // array with suffix
    private String iDescription = "";
    
    public NameFilter(String suffix){ // constructor with a String parameter
	StringTokenizer tok = new StringTokenizer(suffix);
	iSuf = new String[tok.countTokens()];
	for(int i = 0; i < iSuf.length; i++){
	    iSuf[i] = tok.nextToken(); // next suffix
	    iDescription += "*" + iSuf[i] + " "; // iDescription is initiated
	}
    }

    public boolean accept(File f){
	if (f.isDirectory()){
	    return true;
	}
	for (int i = 0; i < iSuf.length; i++){
	    if(f.getName().endsWith(iSuf[i]))
		return true;
	}
	return false;
    }

    public String getDescription(){ // Describes which filers are chosen.
	return iDescription;
    }
}
			  
