/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi.window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowTool;
import endrov.roi.ImageRendererROI;
import endrov.roi.ROI;

/**
 * Image window tool: initial placement of a ROI
 * @author Johan Henriksson
 */
public class ToolDragCreateROI extends GeneralToolDragCreateROI implements ImageWindowTool 
	{
	private final ImageWindow w;
	private final ROI roi;
	private boolean active=false;
	private ImageRendererROI renderer;
	
	
	public ToolDragCreateROI(ImageWindow w, ROI roi, ImageRendererROI renderer)
		{
		super(w,roi,renderer);
		this.w=w;
		this.roi=roi;
		this.renderer=renderer;
		}
	
	
	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Placing ROI");
		mi.setSelected(w.getTool()==this);
		//final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(ToolDragCreateROI.this);}
		});
		return mi;
		}

	public void deselected()
		{
		setRendererROI(null);
		}

	
		
	}

