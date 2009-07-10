package endrov.imageannot;

import java.awt.*;
import java.util.*;
import javax.vecmath.*;

import endrov.imageWindow.*;

public class ImageAnnotImageRenderer implements ImageWindowRenderer
	{
	public ImageWindow w;
	
	
	public ImageAnnotImageRenderer(ImageWindow w)
		{
		this.w=w;
		}

	
	public Collection<ImageAnnot> getVisible()
		{
		return w.getRootObject().getIdObjects(ImageAnnot.class).values();
		}
	

	/**
	 * Render nuclei
	 */
	public void draw(Graphics g)
		{
		for(ImageAnnot ann:getVisible())
			{
			//Coordinate transformation
			Vector2d so=w.transformW2S(new Vector2d(ann.pos.x,ann.pos.y));

			//Draw the nucleus
			g.setColor(Color.RED);

			g.drawOval((int)(so.x-5),(int)(so.y-5),(int)(2*5),(int)(2*5));


			g.drawString(ann.text, (int)so.x-g.getFontMetrics().stringWidth(ann.text)/2, (int)so.y-2);
			/*	int crossSize=5;
				g.drawLine((int)so.x-crossSize, (int)so.y, (int)so.x+crossSize, (int)so.y);
				g.drawLine((int)so.x, (int)so.y, (int)so.x, (int)so.y+crossSize);
			 */


			
			}
		}
	
	
	public void dataChangedEvent()
		{
		}

	
	}
