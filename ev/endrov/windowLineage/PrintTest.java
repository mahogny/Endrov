/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowLineage;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import javax.print.PrintException;

import endrov.core.EvPrint2D;

public class PrintTest extends EvPrint2D
	{

	public PrintTest(File filename) throws IOException, PrintException
		{
		super(filename);
		}

	public int getHeight()
		{
		return 100;
		}

	public int getWidth()
		{
		return 100;
		}

	public void paintComponent(Graphics2D g, double width, double height)
		{
		//g.drawLine(0, 0, 20, 00);
		//g.drawLine(0, 0, 20, 20);
		
		g.setStroke(new BasicStroke(0.5f));
		g.drawLine(0, 20, (int) width, 20);
		g.drawString("Hello the world  : ", 50, 50);

		
		}

	public static void main(String[] args)
		{
		try
			{
			new PrintTest(new File("/tmp/foo.ps"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		catch (PrintException e)
			{
			e.printStackTrace();
			}
		}	
	}
