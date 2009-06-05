package logobar;
/** From Marty Hall
 *  $Log: not supported by cvs2svn $
 *  Revision 1.1.1.1  2005/04/12 12:58:15  johan
 *  Started LogoBar project
 *
 *  Revision 1.3  2005/01/26 15:46:45  pbasa
 *
 *   Changed: Default color for GAPS is now set to white
 *   ---------------------------------------------------------------------
 *
 */

//package logoBar; // All files in the program must be declared to belong to the same package. Otherwise there's a problem when creating the jar file.
import java.awt.*;
import javax.swing.*;
import java.awt.print.*;
//import org.jibble.epsgraphics.*; // Java EPS Graphics2D package obtained at http://www.jibble.org/epsgraphics/
//import java.awt.print.Pageable;



public class PrintUtilities implements Printable{
    private Component componentToBePrinted;
    
    public static void printComponent(Component c){
	new PrintUtilities(c).print();
    }
    
    public PrintUtilities(Component componentToBePrinted){
	this.componentToBePrinted = componentToBePrinted;
    }

    public void print(){
	PrinterJob printerJob = PrinterJob.getPrinterJob();
	
	//printerJob.setPageable(this);
	printerJob.setPrintable(this);
	if (printerJob.printDialog())
	    try{
		printerJob.print();
	    }
	    catch (PrinterException pe){
		System.out.println("Error printing: " + pe);
	    }
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex){
	if (pageIndex > 0){
	    return(NO_SUCH_PAGE);
	}
	else{
	    Graphics2D g2d = (Graphics2D)g;
	    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	    disableDoubleBuffering(componentToBePrinted);
	    componentToBePrinted.paint(g2d);
	    enableDoubleBuffering(componentToBePrinted);
	    return(PAGE_EXISTS);
	}
    }

    public static void disableDoubleBuffering(Component c){
	RepaintManager currentManager = RepaintManager.currentManager(c);
	currentManager.setDoubleBufferingEnabled(false);
    }

    public static void enableDoubleBuffering(Component c){
	RepaintManager currentManager = RepaintManager.currentManager(c);
	currentManager.setDoubleBufferingEnabled(true);
    }
}
	    