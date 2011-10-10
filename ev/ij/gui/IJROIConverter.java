package ij.gui;

import ij.io.RoiDecoder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.vecmath.Vector2d;

import org.apache.commons.io.IOUtils;

import endrov.imageset.EvStack;
import endrov.roi.ROI;
import endrov.roi.primitive.EllipseROI;
import endrov.roi.primitive.PolygonROI;
import endrov.util.EvDecimal;

/**
 * Load ImageJ ROIs and convert them to Endrov equivalent
 *  
 * @author Johan Henriksson
 *
 */
public class IJROIConverter
	{
	public IJROIConverter(File f, EvStack stack) throws IOException
		{
		ZipFile zf=new ZipFile(f);
		
		Enumeration<? extends ZipEntry> e=zf.entries();
		while(e.hasMoreElements())
			{
			ZipEntry ze=e.nextElement();
			
			InputStream is=zf.getInputStream(ze);
			byte[] b=IOUtils.toByteArray(is);
			
			
			ij.io.RoiDecoder dec=new RoiDecoder(b, f.toString());
			ij.gui.Roi roi=dec.getRoi();
			
			ROI endrovRoi=null;
			
			if(roi instanceof ij.gui.EllipseRoi) //Note. have to check this before polygonroi
				{
				ij.gui.EllipseRoi r=(ij.gui.EllipseRoi)roi;
				
				EllipseROI er=new EllipseROI();
				endrovRoi=er;
				er.regionX.start=new EvDecimal(r.x);
				er.regionY.start=new EvDecimal(r.x);
				er.regionX.end=new EvDecimal(r.x+r.width);
				er.regionY.end=new EvDecimal(r.y+r.height);
				}
			else if(roi instanceof ij.gui.FreehandRoi)
				{
				
				}
			else if(roi instanceof ij.gui.Arrow)
				{
				
				}
			else if(roi instanceof ij.gui.ImageRoi)
				{
				
				}
			else if(roi instanceof ij.gui.Line)
				{
				
				}
			else if(roi instanceof ij.gui.OvalRoi)
				{
				//an oval is actually a strange construct!!! //http://en.wikipedia.org/wiki/Oval
				}
			else if(roi instanceof ij.gui.PointRoi)
				{
				
				}
			else if(roi instanceof ij.gui.PolygonRoi)
				{
				ij.gui.PolygonRoi r=(ij.gui.PolygonRoi)roi;
				
				if(r.type==ij.gui.PolygonRoi.POLYLINE || r.type==ij.gui.PolygonRoi.FREELINE)
					{
					PolygonROI er=new PolygonROI();
					endrovRoi=er;
					for(int i=0;i<r.nPoints;i++)
						{
						er.contour.add(new Vector2d(r.xp2[i], r.yp2[i]));
						}
					
					/////r.xSpline  these are for spline... always valid? no. points always valid?
					/////r.ySpline
					
					//what is ANGLE?
					}
				
				}
			else if(roi instanceof ij.gui.TextRoi)
				{
				
				//This is not a ROI at all - turn into an annotation
				
				}
			else if(roi instanceof ij.gui.ShapeRoi)
				{
				//Implemented through AWT Shape, union intersection etc
				//Built up from segments (see makeshapefromarray), awt GeneralPath. messy!!!
				
				
				}
			else
				System.out.println("Skipping unsupported roi, "+roi.getClass());
			
			if(endrovRoi!=null)
				{
				
				//roi.stroke;
				//roi.strokeColor
				//roi.name
				//roi.fillColor
				//roi.handleColor
				//roi.lineWidth
				
				}
			///http://rsbweb.nih.gov/ij/developer/source/index.html
			
			//ROI roi=new ROIDecoder(b, ze.getName()).getROI(stack);
			
			}
		
		
		}


	}
