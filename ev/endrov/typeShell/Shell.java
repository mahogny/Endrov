/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeShell;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.DataConversionException;
import org.jdom.Element;

import endrov.data.*;
import endrov.keybinding.KeyBinding;
import endrov.util.math.ImVector2d;
import endrov.util.math.ImVector3d;
import endrov.windowViewer2D.*;
import endrov.windowViewer3D.*;

/**
 * Shell metadata
 * @author Johan Henriksson
 */
public class Shell extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static final String metaType="shell";
	
	public static final int KEY_TRANSLATE=KeyBinding.register(new KeyBinding("Shell","Translate",'z'));
	public static final int KEY_SETZ=KeyBinding.register(new KeyBinding("Shell","Set Z",'x'));
	public static final int KEY_ROTATE=KeyBinding.register(new KeyBinding("Shell","Rotate",'c'));

	
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public double major, minor;
	public double midx, midy, midz;
	public double angle;        //This is the rotation of the shell around z axis
	public double angleinside; //This is the rotation of the embryo inside shell around major axis
	
	
	/**
	 * Save to XML
	 */
	public String saveMetadata(Element e)
		{
		e.setAttribute("x", ""+midx);
		e.setAttribute("y", ""+midy);
		e.setAttribute("z", ""+midz);

		e.setAttribute("major", ""+major);
		e.setAttribute("minor", ""+minor);

		e.setAttribute("angle", ""+angle);
		e.setAttribute("angleinside", ""+angleinside);
		
		return metaType;
		}

	
	public void loadMetadata(Element e)
		{
		try
			{
			midx=e.getAttribute("x").getDoubleValue();
			midy=e.getAttribute("y").getDoubleValue();
			midz=e.getAttribute("z").getDoubleValue();
			major=e.getAttribute("major").getDoubleValue();
			minor=e.getAttribute("minor").getDoubleValue();
			angle=e.getAttribute("angle").getDoubleValue();
			angleinside=e.getAttribute("angleinside").getDoubleValue();
			}
		catch (DataConversionException e1)
			{
			e1.printStackTrace();
			}
		}

	
	/**
	 * Projected down: check if inside
	 */
	public boolean isPointInside(ImVector2d pos)
		{
		ImVector2d dirvec=ImVector2d.polar(major, angle);
		dirvec=dirvec.normalize().mul(-1);

		//Check if this is within ellipse boundary
		ImVector2d elip=pos.sub(new ImVector2d(midx, midy)).rotate(angle); //todo: angle?
		return 1 >= elip.y*elip.y/(minor*minor) + elip.x*elip.x/(major*major) ;
		}
	
	/**
	 * Projected down: check if inside
	 */
	public boolean isPointInside(ImVector3d pos)
		{
		//Check if this is within ellipse boundary
		ImVector2d foo=new ImVector2d(pos.x,pos.y).sub(new ImVector2d(midx, midy)).rotate(angle); //todo: angle?
		ImVector3d elip=new ImVector3d(foo.x,foo.y,pos.z-midz);
		return 1 >= elip.y*elip.y/(minor*minor) + elip.x*elip.x/(major*major) + elip.z*elip.z/(minor*minor);
		}
	
	/**
	 * Desciption of data
	 */
	public String getMetaTypeDesc()
		{
		return "Shell";
		}
	
	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}
	
	
	public Vector3d getMajorAxis()
		{
		ImVector2d dirvec=ImVector2d.polar(major, angle);
		return new Vector3d(dirvec.x,dirvec.y,0);
		}

	public Vector3d getMinorAxis()
		{
		ImVector2d dirvec=ImVector2d.polar(minor, angle+Math.PI/2.0);
		return new Vector3d(dirvec.x,dirvec.y,0);
		}

	
	public Shell clone()
		{
		Shell s=new Shell();
		s.major=major;
		s.minor=minor;
		s.midx=midx;
		s.midy=midy;
		s.midz=midz;
		s.angle=angle;
		s.angleinside=angleinside;
		return s;
		}
	

	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin(){}
	static
		{
		Viewer2DWindow.addImageWindowExtension(new Viewer2DExtension()
			{
			public void newImageWindow(Viewer2DWindow w)
				{
				w.addImageWindowTool(new ShellImageTool(w));
				}
			});

		Viewer2DWindow.addImageWindowRendererExtension(new Viewer2DRendererExtension()
			{
			public void newImageWindow(Viewer2DInterface w)
				{
				ShellImageRenderer r=new ShellImageRenderer(w);
				w.addImageWindowRenderer(r);
				}
			});

		
		Viewer3DWindow.modelWindowExtensions.add(new ShellModelExtension());
		
		EvData.supportedMetadataFormats.put(metaType,Shell.class);
		
		}
	}
