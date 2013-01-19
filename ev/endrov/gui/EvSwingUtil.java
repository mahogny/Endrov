/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import endrov.core.log.EvLog;
import endrov.util.FuncAB;
import endrov.util.mutable.Mutable;

/**
 * Utility functions for Swing
 * @author Johan Henriksson
 */
public class EvSwingUtil
	{

	/**
	 * Add a label to the left of a swing component 
	 */
	public static JPanel withLabel(String s, JComponent c)
		{
		JPanel p=new JPanel(new BorderLayout());
		p.add(new JLabel(s),BorderLayout.WEST);
		p.add(c,BorderLayout.CENTER);
		p.setOpaque(false);
		return p;
		}

	/**
	 * Place content within a titled border
	 */
	public static JPanel withTitledBorder(String title, JComponent c)
		{
		JPanel p=new JPanel(new GridLayout(1,1));
		p.setBorder(BorderFactory.createTitledBorder(title));
		p.add(c);
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
	public static JComponent layoutLCR(JComponent left, JComponent center, JComponent right)
		{
		JPanel p=new JPanel(new BorderLayout());
		if(left!=null)   p.add(left,BorderLayout.WEST);
		if(center!=null) p.add(center,BorderLayout.CENTER);
		if(right!=null)  p.add(right,BorderLayout.EAST);
		p.setOpaque(false);
		return p;
		}
	
	/**
	 * Table with two columnets: one compact, one wide (filling). Give list of components
	 * as alternating left, right
	 */
	public static JComponent layoutTableCompactWide(JComponent... list)
		{
		int numrow=list.length/2;
		JPanel top=new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;
		for(int i=0;i<numrow;i++)
			{
			c.gridy=i; 
			top.add(list[i*2],c);
			}
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridx=1;
		c.weightx=1;
		for(int i=0;i<numrow;i++)
			{
			c.gridy=i; 
			top.add(list[i*2+1],c);
			}
		return top;
		}

	/**
	 * Compact horizontal layout. No restriction on equal size
	 */
	/*
	public static JComponent compactHorizontal(JComponent left, JComponent right)
		{
		JPanel p=new JPanel(new BorderLayout());
		p.add(left,BorderLayout.WEST);
		p.add(right,BorderLayout.EAST);
		return p;
		}
*/	
	public static JComponent layoutCompactHorizontal(JComponent... list)
		{
		JComponent last=list[list.length-1];
		for(int i=list.length-2;i>=0;i--)
			{
			JPanel p=new JPanel(new BorderLayout());
			p.add(list[i],BorderLayout.WEST);
			p.add(last,BorderLayout.EAST);
			last=p;
			}
		return last;
/*		JPanel p=new JPanel(new BorderLayout());
		p.add(left,BorderLayout.WEST);
		p.add(right,BorderLayout.EAST);
		return p;*/
		}
	
	/**
	 * Add component in a container such that the component is placed in the vertical middle
	 */
	public static JComponent layoutMidVertical(JComponent comp)
		{
		
		JPanel p=new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.BOTH;
		c.weighty=1;
		c.weightx=1;
		c.gridx=0;
		c.gridy=0;
		p.add(new JPanel(),c);
		
		c.gridy=1;
		c.weighty=0;
		p.add(comp,c);
		
		c.weighty=1;
		c.gridy=2;
		p.add(new JPanel(),c);
		
		return p;
		}
	
	public static JComponent layoutCompactVertical(JComponent... list)
		{
		JComponent last=list[list.length-1];
		for(int i=list.length-2;i>=0;i--)
			{
			JPanel p=new JPanel(new BorderLayout());
			p.add(list[i],BorderLayout.NORTH);
			p.add(last,BorderLayout.SOUTH);
			last=p;
			}
		return last;
/*		JPanel p=new JPanel(new BorderLayout());
		p.add(left,BorderLayout.WEST);
		p.add(right,BorderLayout.EAST);
		return p;*/
		}
	
	/**
	 * Layout components in evenly split horizontal boxes
	 */
	public static JComponent layoutEvenHorizontal(JComponent... list)
		{
		JPanel p=new JPanel(new GridLayout(1,list.length));
		for(JComponent c:list)
			p.add(c);
		return p;
		}

	/**
	 * Layout components in evenly split vertical boxes
	 */
	public static JComponent layoutEvenVertical(JComponent... list)
		{
		JPanel p=new JPanel(new GridLayout(list.length,1));
		for(JComponent c:list)
			p.add(c);
		return p;
		}

	
	/**
	 * Layout components using a flow layout
	 */
	public static JComponent layoutFlow(JComponent... list)
		{
		JPanel p=new JPanel(new FlowLayout());
		for(JComponent c:list)
			p.add(c);
		return p;
		}

	/**
	 * Add a component with two components to the above and below
	 */
	public static JComponent layoutACB(JComponent above, JComponent center, JComponent below)
		{
		JPanel p=new JPanel(new BorderLayout());
		if(above!=null)  p.add(above,BorderLayout.NORTH);
		if(center!=null) p.add(center,BorderLayout.CENTER);
		if(below!=null)  p.add(below,BorderLayout.SOUTH);
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

	/**
	 * Totally rip a menu apart, recursively. Action listeners are removed in a
	 * safe way which guarantees GC can proceed
	 */
	public static void tearDownMenu(JMenu menu)
		{
		Vector<JMenuItem> componentsToRemove = new Vector<JMenuItem>();
		for (int i = 0; i<menu.getItemCount(); i++)
			componentsToRemove.add(menu.getItem(i));
		for (JMenuItem c : componentsToRemove)
			if (c==null)
				;// Separator
			else if (c instanceof JMenu)
				tearDownMenu((JMenu) c);
			else
				for (ActionListener l : c.getActionListeners())
					c.removeActionListener(l);
		menu.removeAll();
		}

	/**
	 * Reroute all document changes to a change listener
	 */
	public static void textAreaChangeListener(JTextArea a, final ChangeListener list)
		{
		a.getDocument().addDocumentListener(new DocumentListener(){
			public void change(){list.stateChanged(null);}
			public void changedUpdate(DocumentEvent e) {change();}
			public void removeUpdate(DocumentEvent e) {change();}
			public void insertUpdate(DocumentEvent e) {change();}
		});
		}
	
	/**
	 * Reroute all document changes to a change listener
	 */
	public static void textAreaChangeListener(JTextField a, final ChangeListener list)
		{
		a.getDocument().addDocumentListener(new DocumentListener(){
			public void change(){list.stateChanged(null);}
			public void changedUpdate(DocumentEvent e) {change();}
			public void removeUpdate(DocumentEvent e) {change();}
			public void insertUpdate(DocumentEvent e) {change();}
		});
		}

	
	/**
	 * Run operation in the swing thread. If the code is already executing in the swing thread, then it will not try
	 * to switch. This would otherwise cause an exception
	 */
	public static void invokeAndWaitIfNeeded(Runnable runnable)
		{
		if(SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			{
			try
				{
				SwingUtilities.invokeAndWait(runnable);
				}
			catch (Exception e)
				{
				EvLog.printError(e.getMessage(), e);
				throw new RuntimeException("Error in invoke and wait");
				}
			}
		}

	/**
	 * Run the given code in the swing thread
	 */
	public static <A,B> B runInSwingThread(final FuncAB<A, B> funcAB, final A arg)
		{
		if(SwingUtilities.isEventDispatchThread())
			return funcAB.func(arg);
		else
			{
			final Mutable<B> mutable=new Mutable<B>();
			final Mutable<Exception> hadException=new Mutable<Exception>();
			try
				{
				SwingUtilities.invokeAndWait(new Runnable()
					{
					public void run()
						{
						try
							{
							mutable.setValue(funcAB.func(arg));
							}
						catch (Exception e)
							{
							hadException.setValue(e);
							}
						}
					});
				}
			catch (Exception e)
				{
				throw new RuntimeException("Unexpected swing thread error");
				}
			if(hadException.get()!=null)
				throw new RuntimeException(hadException.get().getMessage());
			return mutable.get();
			}
		}
	}
