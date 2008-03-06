package mmc;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Test
	{

	

	
	public static void main(String[] s)
		{
		MacroRemoteClient client=new MacroRemoteClient("localhost");
		BufferedImage image=client.getScreenshot();
		try
			{
			ImageIO.write(image,"png", new File("imagetest.jpg"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		client.quit();
		}
	
	public static void main2(String[] arg)
		{
		Macro macro=new MacroLocal();
		//macro.mouseClick(300, 200, Macro.MOUSE_RIGHT);
		
		macro.keyType("foobarTest");
		macro.keyUp();macro.keyUp();macro.keyUp();
		
		
		
		
		}
	
	}
