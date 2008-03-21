package evplugin.consoleWindow;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

/**
 * A JTextField extension with the keys up/down to recall past input
 * @author Johan Henriksson
 */
public class JTextFieldHistorized extends JTextField implements KeyListener
	{
	static final long serialVersionUID=0;
	
	/** List of past commands */
	public Vector<String> history=new Vector<String>();
	
	/** Current position in history or -1 */
	private int ch=-1;
	
	/**
	 * Constructor that makes sure keypresses are captured by this widget
	 */
	public JTextFieldHistorized()
		{
		addKeyListener(this);
		}

	/**
	 * Handle keypresses; assumes the implementation intercepts these
	 * before any other handler processes them.
	 */
	public void keyPressed(KeyEvent e)
		{
		if(e.getKeyCode()==KeyEvent.VK_UP)
			{
			if(ch==-1)
				ch=history.size();
			ch--;
			if(ch<0)
				ch=0;
			if(ch!=history.size())
				setText(history.get(ch));
			}
		else if(e.getKeyCode()==KeyEvent.VK_DOWN)
			{
			if(ch!=-1)
				{
				ch++;
				if(ch>=history.size())
					{
					ch=-1;
					setText("");
					}
				else
					setText(history.get(ch));
				}
			}
		if(e.getKeyCode()==KeyEvent.VK_ENTER)
			{
			history.add(getText());
			ch=-1;
			}
		}
	
	public void keyReleased(KeyEvent e)	{}
	public void keyTyped(KeyEvent e) {}	
	}
