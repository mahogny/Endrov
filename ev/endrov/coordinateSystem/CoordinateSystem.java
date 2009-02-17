package endrov.coordinateSystem;


import javax.swing.JMenu;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import endrov.data.*;

import org.jdom.*;

/**
 * Coordinate system
 * @author Johan Henriksson
 *
 */
public class CoordinateSystem extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="coordinatesystem";
	
	public static void initPlugin() {}
	static
		{
		EvData.extensions.put(metaType,CoordinateSystem.class);
		}

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public Vector3d midpoint=new Vector3d(0,0,0);
	public final Vector3d[] base=new Vector3d[]{new Vector3d(1,0,0), new Vector3d(0,1,0), new Vector3d(0,0,1)};
	
	public String getMetaTypeDesc()
		{
		return metaType;
		}
	
	/**
	 * Save down data
	 */
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
//		e.
		}

	public void loadMetadata(Element e)
		{
		}


	public void buildMetamenu(JMenu menu)
		{
		}

	/**
	 * Get transformation matrix from current basis to basis of this system
	 * 
	 * e_i' is this basis, e_i is current basis:
	 * e_i' = sum_j basis_i_j e_j 
	 * rewrite as matrix e'=M e 
	 * give vector v, v' = sum_i v_i' e_i' = sum_i v_i' sum_j basis_i_j e_j)  
	 * 
	 * 
	 * @return
	 */
	public Matrix4d getTransformToSystem()
		{
		Matrix4d m=getTransformFromSystem();
		m.invert();
		return m;
		}
	
	public Matrix4d getTransformFromSystem()
		{
		Matrix4d m=new Matrix4d();
		m.m00=base[0].x;
		m.m10=base[0].y;
		m.m20=base[0].z;
		m.m01=base[1].x;
		m.m11=base[1].y;
		m.m21=base[1].z;
		m.m02=base[2].x;
		m.m12=base[2].y;
		m.m22=base[2].z;
		m.m33=1;
		Matrix4d mt=new Matrix4d();
		mt.setTranslation(midpoint);
		
		//TODO mul
		
		return m;
		}
	
	}
