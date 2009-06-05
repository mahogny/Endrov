package logobar;
/** Paint style class tree...
 *  Code mostly written by Johan Koch, my programing teacher,
 *  with some help from Liyi Meng.
 *  I only contributed in a minor way in writing this class.
 *  Asa Perez-Bercoff
 *  $Log: not supported by cvs2svn $
 *  Revision 1.2  2005/08/03 08:38:02  johan
 *
 *
 *   Changed: 	LogoBar.java PaintStyle.java SortDlg.java Stat.java
 *
 *  Revision 1.1.1.1  2005/04/12 12:58:15  johan
 *  Started LogoBar project
 *
 *  Revision 1.3  2005/01/26 15:46:45  pbasa
 *
 *   Changed: Default color for GAPS is now set to white
 *   ---------------------------------------------------------------------
 *
 *  Revision 1.2  2004/12/09 16:41:22  pbasa
 *
 *  Modified Files:
 *   	ColorHandler.java Filehandler.java LogoBar.java
 *   	NameFilter.java PaintBlockStyle.java PaintContStyle.java
 *   	PaintStyle.java Stat.java
 *  Added Files:
 *   	JComponentVista.java Vista.java
 *  ----------------------------------------------------------------------
 *
 *  Revision 1.1  2004/12/03 14:01:13  pbasa
 *
 *   Added File
 *   ----------------------------------------------------------------------
 * 
 *
 */

import java.awt.*;


public interface PaintStyle {
    public void paint(GraphPanel gPanel, Graphics2D g, Stat stat, char[] consensusArray);
    public void update();
    
}


