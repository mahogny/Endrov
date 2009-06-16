package endrov.lineageWindow.print;

import java.io.*;
import java.awt.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
 
public abstract class Print2DtoPostScript implements Printable{
 
	public Print2DtoPostScript(File filename){
		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();
		StreamPrintServiceFactory[] factories = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, psMimeType);
 
		if(factories.length == 0){
			System.err.println("Output not supported!");
			return;
		}
 
		try{
		FileOutputStream fos = new FileOutputStream(filename);
 
			System.out.println(factories[0].getClass().getName());
			StreamPrintService sps = factories[0].getPrintService(fos);
 
			DocPrintJob pj = sps.createPrintJob();
			PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
			
 
			// /PageSize ps command
			aset.add(javax.print.attribute.standard.MediaSizeName.ISO_A4);
 
			Doc doc = new SimpleDoc(this, flavor, null);
 
			pj.print(doc, aset);
			fos.close();
		}
		catch(PrintException pe){
			System.err.println("PrintException : "+pe);
		}
		catch(IOException ioe){
			System.err.println("IOexception : "+ioe);
		}
	}
 
	
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract void paintComponent(Graphics2D g);
	
	
	public int print(Graphics g, PageFormat pf, int pageIndex){
		if(pageIndex < 1){
			Graphics2D g2d = (Graphics2D)g;
			g2d.translate(pf.getImageableX(), pf.getImageableY());
			
			//double pw=pf.getImageableWidth();
			//double ph=pf.getImageableHeight();
			
			//scale here

			paintComponent(g2d);
			
/*			g2d.setColor(Color.black);
			g2d.drawString("Hello the world  : "+pageIndex, 0, 10);
			g2d.setColor(Color.red);
			g2d.fillRect(0, 0, 72, 72);*/
			return Printable.PAGE_EXISTS;
		}
		else{
			return Printable.NO_SUCH_PAGE;
		}
	}
 
	/*
	public static void main(String args[]){
		Print2DtoPostScript sp = new Print2DtoPostScript();
	}
	*/
}

///NumCopies
//aset.add(new javax.print.attribute.standard.Copies(25));
