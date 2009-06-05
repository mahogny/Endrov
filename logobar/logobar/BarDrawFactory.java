package logobar;


/** A BarDrawer factory.
 * This class creates a new BarDrawer class
 * from the current Preferences settings
 */
public class BarDrawFactory {
    
    static BarDrawer getBarDrawer(){
	boolean weblogo           = LogoBar.iPref.iShowWebLogo;
	boolean showOnlyConserved = LogoBar.iPref.iShowOnlyMostConserved;
	if(weblogo){
	    if(showOnlyConserved)
		return new BarDrawerLettersOnlyConserved();
	    else
		return new BarDrawerLettersAll();
	}
	else{
	    if(showOnlyConserved)
		return new BarDrawerOnlyConserved();
	    else
		return new BarDrawerAll();
	}
	
    }
}
