package logobar;
/**
 *  Commenced: Wednesday, April 13, 2004
 *  Author: Johan Koch
 * 
 */

import java.util.*;

/** @brief A utility class for LogoBar
 * All utility functions for various needs must
 * be placed inside this class
 */
public class LogoUtil {

    public static void vecSort(Vector vec){
	int vec_len = vec.size();
	int i;
	for(int n = 0; n < vec_len; n++){
	    Comparable tmp = (Comparable)vec.elementAt(n);
	    //The compareTo method is implemented in the class
	    //of examiniation
	    for(i = n; i > 0 && tmp.compareTo(vec.elementAt(i-1)) < 0; i--){
		vec.set(i, vec.elementAt(i-1));
	    }
	    vec.set(i, tmp);
	}
    }
}

