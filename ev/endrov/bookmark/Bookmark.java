package endrov.bookmark;


import javax.swing.JMenu;

import endrov.data.*;
import endrov.modelWindow.Camera;
import endrov.modelWindow.ModelWindow;
import endrov.modelWindow.ModelWindowExtension;
import endrov.util.*;

import org.jdom.*;

/**
 * Bookmark of frame, z or whatever comes to mind. For recalling window state.
 * 
 * @author Johan Henriksson
 *
 */
public class Bookmark extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="bookmark";
	
	//private static final String metaElement="bookmark";
	
	public static void initPlugin() {}
	static
		{
		EvData.extensions.put(metaType,Bookmark.class);
		ModelWindow.modelWindowExtensions.add(new ModelWindowExtension(){
			public void newModelWindow(ModelWindow w)
				{
				w.modelWindowHooks.add(new BookmarkModelWindowHook(w));
				}
		});
		}

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	public EvDecimal frame, z;
	public Camera modelCamera;
	

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
		if(frame!=null)
			e.setAttribute("frame",frame.toString());
		if(z!=null)
			e.setAttribute("z",z.toString());
		if(modelCamera!=null)
			{
			Element sub=new Element("modelcamera");
			modelCamera.toElement(sub);
			e.addContent(sub);
			}
		
		
		}


	
	
	public void loadMetadata(Element e)
		{
		String sFrame=e.getAttributeValue("frame");
		String sZ=e.getAttributeValue("z");
		if(sFrame!=null)
			frame=new EvDecimal(sFrame);
		if(sZ!=null)
			z=new EvDecimal(sZ);
		Element eModelCamera=e.getChild("modelcamera");
		if(eModelCamera!=null)
			{
			modelCamera=new Camera();
			modelCamera.fromElement(eModelCamera);
			}
		
		
		}


	public void buildMetamenu(JMenu menu)
		{
		/*
		JMenuItem miGoto=new JMenuItem("Go to");
		menu.add(miGoto);

		miGoto.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				if()
				
				}
		});*/
		//GOTO
		
		}

	
	}
