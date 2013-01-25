/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeText;

import java.awt.*;
import java.util.*;
import javax.vecmath.*;

import endrov.util.collection.Tuple;
import endrov.windowViewer2D.*;

public class TextAnnotImageRenderer implements Viewer2DRenderer
	{
	public Viewer2DInterface w;
	
	Tuple<String,TextAnnot> activeAnnot=null;
	TextAnnot activeAnnotNew=null;

	
	public TextAnnotImageRenderer(Viewer2DInterface w)
		{
		this.w=w;
		}

	
	public Map<String, TextAnnot> getVisible()
		{
		return w.getRootObject().getIdObjects(TextAnnot.class);
		}
	

	/**
	 * Render annotation
	 */
	public void draw(Graphics g)
		{
		for(TextAnnot ann:getVisible().values())
			{
			if(activeAnnot!=null && ann==activeAnnot.snd())
				ann=activeAnnotNew;
				
			//Coordinate transformation
			Vector2d so=w.transformPointW2S(new Vector2d(ann.pos.x,ann.pos.y));

			//Draw the text
			g.setColor(Color.RED);
			g.drawOval((int)(so.x-5),(int)(so.y-5),(int)(2*5),(int)(2*5));
			g.drawString(ann.text, (int)so.x-g.getFontMetrics().stringWidth(ann.text)/2, (int)so.y-2);
			}
		}
	
	
	public void dataChangedEvent()
		{
		}

	
	}
