package endrov.roi.primitive;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.vecmath.Vector2d;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.roi.LineIterator;
import endrov.roi.ROI;
import endrov.roi.util.FlipCodeTessellate;
import endrov.roi.util.TriangulationException;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;

/**
 * A ROI defined by a polygon
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class PolygonROI extends ROI2D
	{
	private static final String metaType="ROI_Polygon";
	private static ImageIcon icon=null;//new ImageIcon(DiffROI.class.getResource("iconBox.png"));

	
	/**
	 * Points. Should be in counter-clock-wise order
	 */
	public ArrayList<Vector2d> contour=new ArrayList<Vector2d>(); 
	
	public static class Tessellation
		{
		int[][] tris;
		//Array of triples	
		}
	
	private Tessellation userTessellation=null;
	private Tessellation cachedTessellation=null;
	
	public boolean hintConvex=false;
	
	
	/**
	 * Tesselate a convex polygon, O(n)
	 * 
	 * TODO test
	 * 
	 */
	private Tessellation tessellateConvex()
		{
		int numPoly=contour.size()-2;
		
		Tessellation tes=new Tessellation();
		tes.tris=new int[numPoly][3];
		
		for(int i=0;i<numPoly;i++)
			{
			tes.tris[i][0]=0;
			tes.tris[i][1]=i+1;
			tes.tris[i][2]=i+2;
			}

		return tes;
		}

	/**
	 * Get tesselation, calculate if needed
	 * @throws TriangulationException 
	 */
	public Tessellation getTessellation() throws TriangulationException
		{
		if(userTessellation!=null)
			return userTessellation;
		else
			{
			if(cachedTessellation==null)
				{
				if(hintConvex)
					cachedTessellation=tessellateConvex();
				else
					{
					List<int[]> ret=FlipCodeTessellate.process(contour);
					Tessellation t=new Tessellation();
					t.tris=ret.toArray(new int[][]{});
					cachedTessellation=t; 
					}
				
				
				
				}
			
			
			
			
			return cachedTessellation;
			}
		}

	/**
	 * Set user-defined tesselation
	 */
	public void setUserTessellation(Tessellation tes)
		{
		userTessellation=tes;
		cachedTessellation=null;
		}

	
	
	
	@Override
	public String getROIDesc()
		{
		return "Polygon";
		}

	@Override
	public JComponent getROIWidget()
		{
		return null;
		}


	@Override
	public Handle[] getHandles()
		{
		return new Handle[0];
		}

	@Override
	public Handle getPlacementHandle1()
		{
		return null;
		}

	@Override
	public Handle getPlacementHandle2()
		{
		return null;
		}

	@Override
	public void initPlacement(String chan, EvDecimal frame, EvDecimal z)
		{
		}

	@Override
	public boolean imageInRange(String channel, EvDecimal frame, double z)
		{
		return imageInRange2d(channel, frame, z);
		}

	@Override
	public LineIterator getLineIterator(ProgressHandle progh, EvStack stack,
			EvImage im, String channel, EvDecimal frame, double z)
		{
		
		//////////
		//TODO !!!!!!!!!!!!!!
		
		
		
		return null;
		}

	@Override
	public boolean pointInRange(String channel, EvDecimal frame, double x,
			double y, double z)
		{
		if(imageInRange2d(channel, frame, z))
			{

			//TODO
			
			return false;
			}
		else
			return false;
		}

	@Override
	public String saveMetadata(Element e)
		{
		saveMetadata2(e);
		
		// TODO Auto-generated method stub
		return metaType;
		}

	@Override
	public void loadMetadata(Element e)
		{
		loadMetadata2(e);
		// TODO Auto-generated method stub
		
		}

	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,BoxROI.class);
		
		ROI.addType(new ROIType(icon, PolygonROI.class, true,false,"Polygon"));
		}
	
	}
