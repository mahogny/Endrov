/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.coordinateSystem;


import javax.swing.JMenu;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import endrov.data.*;

import org.jdom.*;

/**
 * Coordinate system
 * 
 * For T*R*p, global displacement will always be on the right side in the 4d matrix 
 * 
 * @author Johan Henriksson
 *
 */
public class CoordinateSystem extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="coordinatesystem";
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	private Matrix4d toSystem=new Matrix4d();
	private Matrix4d toWorld=new Matrix4d();
	
	public CoordinateSystem()
		{
		toSystem.setIdentity();
		toWorld.setIdentity();
		}
	
	/**
	 * Set matrices. Only copies the reference, not the content!
	 */
	public void setFromMatrices(Matrix4d toSystem, Matrix4d toWorld)
		{
		this.toSystem=toSystem;
		this.toWorld=toWorld;
		}
	
	
	/**
	 * Set transformation matrix from current basis to basis of this system
	 * 
	 * e_i' is this basis, e_i is current basis:
	 * e_i' = sum_j basis_i_j e_j 
	 * rewrite as matrix e'=M e 
	 * give vector v, v' = sum_i v_i' e_i' = sum_i v_i' sum_j basis_i_j e_j)  
	 * 
	 * The one changing the bases is responsible for calling this function
	 */
	public void setMidBases(Vector3d midpoint, Vector3d[] base)
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

		m.m03=midpoint.x;
		m.m13=midpoint.y;
		m.m23=midpoint.z;

		m.m33=1;
		
		toWorld=m;
//		toSystem=m;
		
		//System.out.println(cachedFromSystem);
		try
			{
			toSystem.invert(toWorld);
			//toWorld.invert(toSystem);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			toWorld.setIdentity(); //Safe choice, will at least stop things from crashing
			}
		}
	
	/*
	Matrix4d mt=new Matrix4d();
	mt.setIdentity();
	mt.setTranslation(midpoint);
	mt.mul(m);
	cachedToSystem=mt;
	*/
	
	public Vector3d getOrigo()
		{
		return new Vector3d(toWorld.m03, toWorld.m13, toWorld.m23);
		}
	
	public Vector3d[] getBases()
		{
		return new Vector3d[]{
				new Vector3d(toWorld.m00, toWorld.m10, toWorld.m20),
				new Vector3d(toWorld.m01, toWorld.m11, toWorld.m21),
				new Vector3d(toWorld.m02, toWorld.m12, toWorld.m22)
				};
		}
	
	
	
	
	public String getMetaTypeDesc()
		{
		return metaType;
		}
	
	/**
	 * Save down data
	 */
	public String saveMetadata(Element e)
		{
		Vector3d midpoint=getOrigo();
		Vector3d[] base=getBases();
		
		Element mide=new Element("midpoint");
		mide.setAttribute("x",""+midpoint.x);
		mide.setAttribute("y",""+midpoint.y);
		mide.setAttribute("z",""+midpoint.z);
		e.addContent(mide);
		for(int i=0;i<3;i++)
			{
			Element ce=new Element("basis");
			ce.setAttribute("x",""+base[i].x);
			ce.setAttribute("y",""+base[i].y);
			ce.setAttribute("z",""+base[i].z);
			e.addContent(ce);
			}
		return metaType;
		}

	public void loadMetadata(Element e)
		{
		try
			{
			Vector3d midpoint=new Vector3d();
			Vector3d[] base=new Vector3d[3];
			for(Object o:e.getChildren())
				{
				Element c=(Element)o;
				if(c.getName().equals("basis"))
					{
					Vector3d v=new Vector3d(
							c.getAttribute("x").getDoubleValue(),
							c.getAttribute("y").getDoubleValue(),
							c.getAttribute("z").getDoubleValue());
					base[c.getAttribute("num").getIntValue()]=v;
					}
				else if(c.getName().equals("midpoint"))
					{
					Vector3d v=new Vector3d(
							c.getAttribute("x").getDoubleValue(),
							c.getAttribute("y").getDoubleValue(),
							c.getAttribute("z").getDoubleValue());
					midpoint=v;
					}
				}
			setMidBases(midpoint, base);
//			updateCachedMatrices();
			}
		catch (DataConversionException e1)
			{
			e1.printStackTrace();
			}
		//updateCachedMatrices();
		}


	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}

	/**
	 * Get read-only matrix going FROM this system i.e. inversion of matrix bases as columns
	 */
	public Matrix4d getTransformToWorld()
		{
		return toWorld;
		}
	
	/**
	 * Get read-only matrix going TO this system i.e. matrix with bases as columns
	 */
	public Matrix4d getTransformToSystem()
		{
		return toSystem;
		}
	
	/**
	 * Equivalent to a multiplication with a matrix where the base vectors are the columns. Equivalent to projecting down on base vectors.
	 * @param v
	 * @return
	 */
	public Vector3d transformToSystem(Vector3d v)
		{
		Vector4d w=new Vector4d(v.x,v.y,v.z,1);
		toSystem.transform(w);
		return new Vector3d(w.x,w.y,w.z);
		}

	/**
	 * Equivalent to a multiplication with the inverse of a matrix where the base vectors are the columns
	 * @param v
	 * @return
	 */
	public Vector3d transformToWorld(Vector3d v)
		{
		Vector4d w=new Vector4d(v.x,v.y,v.z,1);
		toWorld.transform(w);
		return new Vector3d(w.x,w.y,w.z);
		}

	public static void main(String[] args)
		{
		
		CoordinateSystem cs=new CoordinateSystem();
		
		cs.setMidBases(new Vector3d(1,2,3), new Vector3d[]{
				new Vector3d(1,0,0),
				new Vector3d(0,1,0),
				new Vector3d(0,0,1)});
		
		//Vector3d v=;
		
		Vector3d w=cs.transformToWorld(new Vector3d(0,0,0));
//		Vector3d w=cs.transformToWorld(new Vector3d(1,2,3));
		System.out.println(w);
		
		}
	
	
	/**
	 * v3=v1 x v2. v2 is replaced by one orthogonal to v1 and v3. Finally the base vectors are made to have given lengths.
	 */
	public void setFromTwoVectors(Vector3d v1, Vector3d v2, double len1, double len2, double len3, Vector3d mid)
		{
		Vector3d v1prim=new Vector3d(v1);
		v1prim.normalize();
		v1prim.scale(len1);
		
		Vector3d v3=new Vector3d();
		v3.cross(v1, v2);
		v3.normalize();
		v3.scale(len3);
		
		Vector3d v2prim=new Vector3d();
		v2prim.cross(v3, v1);
		v2prim.normalize();
		v2prim.scale(len2);
		
		setMidBases(mid, new Vector3d[]{
			v1prim,
			v2prim,
			v3
		});
		
/*		base[0]=v1prim;
		base[1]=v2prim;
		base[2]=v3;
		
		midpoint.set(mid);
		
		updateCachedMatrices();*/
		}

	public CoordinateSystem clone()
		{
		CoordinateSystem cs=new CoordinateSystem();
		cs.toSystem.set(toSystem);
		cs.toWorld.set(toWorld);
		return cs;
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
		EvData.supportedMetadataFormats.put(metaType,CoordinateSystem.class);
		}

	}
