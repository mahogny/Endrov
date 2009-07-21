package endrov.ev;

import java.io.*;
import java.awt.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;

/**
 * Simple way of supporting printing
 * @author Johan Henriksson
 *
 */
public abstract class EvPrint2D implements Printable
	{
	public EvPrint2D(File filename) throws IOException, PrintException
		{
		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();
		StreamPrintServiceFactory[] factories = StreamPrintServiceFactory
				.lookupStreamPrintServiceFactories(flavor, psMimeType);

		if (factories.length==0)
			throw new IOException("Output not supported!");
		else
			{
			FileOutputStream fos = new FileOutputStream(filename);

			//System.out.println("Using: "+factories[0].getClass().getName());
			StreamPrintService sps = factories[0].getPrintService(fos);

			DocPrintJob pj = sps.createPrintJob();
			PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();

			// /PageSize ps command
			aset.add(javax.print.attribute.standard.MediaSizeName.ISO_A4);

			Doc doc = new SimpleDoc(this, flavor, null);

			pj.print(doc, aset);
			fos.close();
			}
		}

	/*
	public abstract int getWidth();
	public abstract int getHeight();
	*/
	/**
	 * Fill in the content. The size of the drawing area is (0,0) to (width,height)
	 */
	public abstract void paintComponent(Graphics2D g, double width, double height);

	public int print(Graphics g, PageFormat pf, int pageIndex)
		{
		if (pageIndex<1)
			{
			double imw = pf.getImageableWidth();
			double imh = pf.getImageableHeight();

			Graphics2D g2d = (Graphics2D) g;
			g2d.translate(pf.getImageableX(), pf.getImageableY());

			paintComponent(g2d, imw, imh);

			return Printable.PAGE_EXISTS;
			}
		else
			{
			return Printable.NO_SUCH_PAGE;
			}
		}

	/**
	 * Hopefully won't have to use this
	 */
	/*
	public void drawLine(Graphics g, int x1, int y1, int x2, int y2, int lineWidth)
		{
		if (lineWidth==1)
			{
			g.drawLine(x1, y1, x2, y2);
			}
		else
			{
			double angle;
			double halfWidth = ((double) lineWidth)/2.0;
			double deltaX = (double) (x2-x1);
			double deltaY = (double) (y2-y1);
			if (x1==x2)
				{
				angle = Math.PI;
				}
			else
				{
				angle = Math.atan(deltaY/deltaX)+Math.PI/2;
				}
			int xOffset = (int) (halfWidth*Math.cos(angle));
			int yOffset = (int) (halfWidth*Math.sin(angle));
			int[] xCorners =
				{ x1-xOffset, x2-xOffset+1, x2+xOffset+1, x1+xOffset };
			int[] yCorners =
				{ y1-yOffset, y2-yOffset, y2+yOffset+1, y1+yOffset+1 };
			g.fillPolygon(xCorners, yCorners, 4);
			}
		}*/

	}

// /NumCopies
// aset.add(new javax.print.attribute.standard.Copies(25));
