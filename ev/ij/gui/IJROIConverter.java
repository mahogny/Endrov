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

import endrov.roi.ROI;
import endrov.roi.primitive.BoxROI;
import endrov.roi.primitive.EllipseROI;
import endrov.roi.primitive.PolygonROI;
import endrov.typeImageset.EvStack;
import endrov.util.math.EvDecimal;

/**
 * Load ImageJ ROIs and convert them to Endrov equivalent
 *  
 * @author Johan Henriksson
 *
 */
public abstract class IJROIConverter
	{
	
	public abstract void gotROI(ROI roi);
	
	public IJROIConverter(File f, EvStack stack) throws IOException
		{
		ZipFile zf=new ZipFile(f);
		
		
		
		Enumeration<? extends ZipEntry> e=zf.entries();
		while(e.hasMoreElements())
			{
			System.out.println("element");
			ZipEntry ze=e.nextElement();
			
			InputStream is=zf.getInputStream(ze);
			byte[] b=IOUtils.toByteArray(is);
			
			
			ij.io.RoiDecoder dec=new RoiDecoder(b, f.toString());
			ij.gui.Roi roi=dec.getRoi();
			
			ROI endrovRoi=null;
			
			if(roi.type==ij.gui.Roi.RECTANGLE) //Note. have to check this before polygonroi
				{
				ij.gui.Roi r=roi;

				Vector2d pUpperLeft=stack.transformImageWorld(new Vector2d(r.x, r.y));
				Vector2d pLowerRight=stack.transformImageWorld(new Vector2d(r.x+r.width, r.y+r.height));

				BoxROI er=new BoxROI();
				endrovRoi=er;
				er.regionX.start=new EvDecimal(pUpperLeft.x);
				er.regionY.start=new EvDecimal(pUpperLeft.y);
				er.regionX.end=new EvDecimal(pLowerRight.x);
				er.regionY.end=new EvDecimal(pLowerRight.y);
				}
			else if(roi instanceof ij.gui.EllipseRoi) //Note. have to check this before polygonroi
				{
				ij.gui.EllipseRoi r=(ij.gui.EllipseRoi)roi;

				Vector2d pUpperLeft=stack.transformImageWorld(new Vector2d(r.x, r.y));
				Vector2d pLowerRight=stack.transformImageWorld(new Vector2d(r.x+r.width, r.y+r.height));

				EllipseROI er=new EllipseROI();
				endrovRoi=er;
				er.regionX.start=new EvDecimal(pUpperLeft.x);
				er.regionY.start=new EvDecimal(pUpperLeft.y);
				er.regionX.end=new EvDecimal(pLowerRight.x);
				er.regionY.end=new EvDecimal(pLowerRight.y);
				}
			else if(roi instanceof ij.gui.FreehandRoi)
				{
				
				}
			else if(roi instanceof ij.gui.Arrow)
				{
				
				}
			else if(roi instanceof ij.gui.ImageRoi)
				{
				//ij.gui.ImageRoi r=(ij.gui.ImageRoi)roi;

				
				}
			else if(roi instanceof ij.gui.Line)
				{
				
				}
			else if(roi instanceof ij.gui.OvalRoi)
				{
				//an oval is actually a strange construct!!! //http://en.wikipedia.org/wiki/Oval
				
				//best method to convert is probably to make a bitmap roi?
				
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
						Vector2d v=stack.transformImageWorld(new Vector2d(r.xp[i]+r.x, r.yp[i]+r.y));
						er.contour.add(new Vector2d(v));
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
				
				//could use awt and make a bitmap roi out of it
				
				
				}
			else
				System.out.println("Does not know about roi type "+roi.getClass()+" , "+roi.type);
			
			if(endrovRoi!=null)
				gotROI(endrovRoi);
			else
				System.out.println("Cannot convert roi of type "+roi.getClass());
			///http://rsbweb.nih.gov/ij/developer/source/index.html
			
			//ROI roi=new ROIDecoder(b, ze.getName()).getROI(stack);
			
			}
		
		
		}


	}
