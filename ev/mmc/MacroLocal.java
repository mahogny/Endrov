package mmc;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;


/**
 * Macro on the local computer
 * @author Johan Henriksson
 *
 */
public class MacroLocal extends Macro
	{
	public Robot robot;
	private final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
	private final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
	private final Rectangle screenRect = new Rectangle(0, 0, WIDTH, HEIGHT);
	
	
	public MacroLocal()
		{
		try
			{
			robot=new Robot();
			}
		catch (AWTException e)
			{
			e.printStackTrace();
			}
		}
	
	
	/** Type a key */
	public void keyType(int keycode, boolean shift)
		{
		if(shift)
			robot.keyPress(KeyEvent.VK_SHIFT);
		robot.keyPress(keycode);
		robot.keyRelease(keycode);
		if(shift)
			robot.keyRelease(KeyEvent.VK_SHIFT);
		}
	
	

	
	
	/** Click with given buttons at location */
	public void mouseClick(int x, int y, int buttons)
		{
		robot.mouseMove(x, y);
		robot.mousePress(buttons);
		robot.mouseRelease(buttons);
		}
	
	
	
	public String getClipboard()
		{
		try
			{
			return (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
			}
		catch(Exception e2)
			{
			System.out.println("Failed to get text from clipboard");
			return "<fail>";
			}
		}
	
	
	public void setClipboard(String s)
		{
		StringSelection sel=new StringSelection(s);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
		}
	
	
	public BufferedImage getScreenshot()
		{
		BufferedImage i = robot.createScreenCapture(screenRect);
//		Image image = i.getScaledInstance(WIDTH/Constants.SCALE_AMOUNT, HEIGHT/Constants.SCALE_AMOUNT, Image.SCALE_SMOOTH);
		return i;
		}
	
	
	}
