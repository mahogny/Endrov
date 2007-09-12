package evplugin.shell;

import org.jdom.DataConversionException;
import org.jdom.Element;

import evplugin.imageWindow.*;
import evplugin.metadata.*;
import evplugin.modelWindow.*;

/**
 * Shell metadata
 * @author Johan Henriksson
 */
public class Shell extends MetaObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static final String metaType="shell";
	
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				ShellImageRenderer r=new ShellImageRenderer(w);
				w.imageWindowTools.add(new ShellImageToolMakeShell(w,r));
				w.imageWindowRenderers.add(r);
				}
			});
		
		ModelWindow.modelWindowExtensions.add(new ModelWindowExtension()
			{
			public void newModelWindow(ModelWindow w)
				{
				w.modelWindowHooks.add(new ShellModelHook(w));
				}
			});
		
		Metadata.extensions.put(metaType,new MetaObjectExtension()
			{
			public MetaObject extractObjects(Element e)
				{
				Shell shell=new Shell();
				try
					{
					shell.midx=e.getAttribute("x").getDoubleValue();
					shell.midy=e.getAttribute("y").getDoubleValue();
					shell.midz=e.getAttribute("z").getDoubleValue();
					shell.major=e.getAttribute("major").getDoubleValue();
					shell.minor=e.getAttribute("minor").getDoubleValue();
					shell.angle=e.getAttribute("angle").getDoubleValue();
					shell.angleinside=e.getAttribute("angleinside").getDoubleValue();
					}
				catch (DataConversionException e1)
					{
					e1.printStackTrace();
					}
				return shell;
				}
			});
		
		}
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public double major, minor;
	public double midx, midy, midz;
	public double angle;        //This is the rotation of the shell around z axis
	public double angleinside; //This is the rotation of the embryo inside shell around major axis
	
	/**
	 * Check if shell exists. this function will disappear
	 * @return
	 */
	public boolean exists()
		{
		return major!=0 || minor!=0;
		}
	

	/**
	 * Save to XML
	 */
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		
		e.setAttribute("x", ""+midx);
		e.setAttribute("y", ""+midy);
		e.setAttribute("z", ""+midz);

		e.setAttribute("major", ""+major);
		e.setAttribute("minor", ""+minor);

		e.setAttribute("angle", ""+angle);
		e.setAttribute("angleinside", ""+angleinside);
		}
	
	
	/**
	 * Desciption of data
	 */
	public String getMetaTypeDesc()
		{
		return "Shell";
		}
	
	}
