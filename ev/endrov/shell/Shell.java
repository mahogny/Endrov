package endrov.shell;

import javax.swing.JMenu;

import org.jdom.DataConversionException;
import org.jdom.Element;

import endrov.data.*;
import endrov.imageWindow.*;
import endrov.keyBinding.KeyBinding;
import endrov.modelWindow.*;
import endrov.util.ImVector2;
import endrov.util.ImVector3d;

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

	
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				ShellImageRenderer r=new ShellImageRenderer(w);
				w.imageWindowTools.add(new ShellImageTool(w,r));
				w.imageWindowRenderers.add(r);
				}
			});
		
		ModelWindow.modelWindowExtensions.add(new ShellModelExtension());
		
		EvData.supportedMetadataFormats.put(metaType,Shell.class);
		
		}
	

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
		//TODO NO! do not store straight in e
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
	public boolean isInside(ImVector2 pos)
		{
		ImVector2 dirvec=ImVector2.polar(major, angle);
		//ImVector2 startpos=dirvec.add(new ImVector2(midx,midy));
		dirvec=dirvec.normalize().mul(-1);

		//Check if this is within ellipse boundary
		ImVector2 elip=pos.sub(new ImVector2(midx, midy)).rotate(angle); //todo: angle?
		return 1 >= elip.y*elip.y/(minor*minor) + elip.x*elip.x/(major*major) ;
		}
	
	/**
	 * Projected down: check if inside
	 */
	public boolean isInside(ImVector3d pos)
		{
		//ImVector2 dirvec=ImVector2.polar(major, angle);
		//ImVector2 startpos=dirvec.add(new ImVector2(midx,midy));
		//dirvec=dirvec.normalize().mul(-1);

		//ImVector2 foo=
		
		//Check if this is within ellipse boundary
		ImVector2 foo=new ImVector2(pos.x,pos.y).sub(new ImVector2(midx, midy)).rotate(angle); //todo: angle?
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
	public void buildMetamenu(JMenu menu)
		{
		}

	
	}
