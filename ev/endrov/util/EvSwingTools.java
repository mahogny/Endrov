package endrov.util;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.*;

/**
 * Utility functions for Swing
 * @author Johan Henriksson
 */
public class EvSwingTools
	{

	/**
	 * Add a label to the left of a swing component 
	 */
	public static JPanel withLabel(String s, JComponent c)
		{
		JPanel p=new JPanel(new BorderLayout());
		p.add(new JLabel(s),BorderLayout.WEST);
		p.add(c,BorderLayout.CENTER);
		return p;
		}

	/**
	 * Add a label above a swing component 
	 */
	public static JPanel withLabelAbove(String s, JComponent c)
		{
		JPanel p=new JPanel(new BorderLayout());
		p.add(new JLabel(s),BorderLayout.NORTH);
		p.add(c,BorderLayout.CENTER);
		return p;
		}

	/**
	 * Add a component with two components to the left and right
	 */
	public static JComponent borderLR(JComponent left, JComponent center, JComponent right)
		{
		JPanel p=new JPanel(new BorderLayout());
		if(left!=null)   p.add(left,BorderLayout.WEST);
		if(center!=null) p.add(center,BorderLayout.CENTER);
		if(right!=null)  p.add(right,BorderLayout.EAST);
		return p;
		}

	/**
	 * Get string from clipboard, never null
	 */
	public static String getClipBoardString()
		{
		try
			{
			return (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
			}
		catch(Exception e2)
			{
			System.out.println("Failed to get text from clipboard");
			}
		return "";
		}

	public static void setClipBoardString(String s)
	{
	Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), 
			new ClipboardOwner(){
			public void lostOwnership(Clipboard aClipboard, Transferable aContents){}
			});
	}

	}
