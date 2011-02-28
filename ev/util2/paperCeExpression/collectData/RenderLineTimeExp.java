package util2.paperCeExpression.collectData;

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import gnu.jpdf.PDFJob;

/**
 * Render expression start times along a line
 */
public class RenderLineTimeExp
	{
	public static void main(String[] args)
		{
		try
			{
			FileOutputStream fo=new FileOutputStream(new File("/home/tbudev3/temp.pdf"));
			PDFJob job = new PDFJob(fo);
			Graphics2D g = (Graphics2D)job.getGraphics();
			int scaleY=5;
			g.drawLine(30, 20, 30, 20+100*scaleY);
			
			//For all positions
			for(int pos=0;pos<=100;pos+=10)
				{
				String geneName="ceh-"+pos;
				
				int y=20+pos*scaleY;
				g.drawLine(30, y, 35, y);
				
				//Rotation is not possible. hmmm. might be possible to keep text unrotated, rotate the line
				g.drawString(geneName, 40, y+5);
				}
			
			g.dispose();
			job.end();
			}
		catch (FileNotFoundException e)
			{
			e.printStackTrace();
			}

		}
	}
