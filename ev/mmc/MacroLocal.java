package mmc;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class MacroLocal extends Macro
	{
	public Robot robot;
	
	public MacroLocal()
		{
		try
			{
			robot=new Robot();
			}
		catch (AWTException e)
			{
			// TODO Auto-generated catch block
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
		return "";
		}
	
	
	}
