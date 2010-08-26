/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.undo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import endrov.basicWindow.BasicWindow;
import endrov.data.DataMenuExtension;
import endrov.data.EvData;
import endrov.data.EvDataMenu;




/**
 * Undo operations.
 * 
 * These are all very GUI-related and hence should not be mixed up with data. Because
 * operations will be performed on unlinked objects undo can in particular not be stored
 * in EvData.
 * 
 * Some operations will be commutative but it would be very advanced to figure this out.
 * The compromise is to allow "unsafe undo" where any of the last N operations can be undone
 * but the user has to evaluate if it is safe or not.
 * 
 * @author Johan Henriksson
 *
 */
public class EvUndo
	{
	static final long serialVersionUID=0;
	
	public static LinkedList<UndoOp> undoQueue=new LinkedList<UndoOp>(); //Last is last operation done
	public static LinkedList<UndoOp> redoQueue=new LinkedList<UndoOp>(); //First operation is to be redone first

	public UndoOp getLastUndo()
		{
		return undoQueue.getLast();
		}
	
	/**
	 * Add undo operation and execute it
	 */
	public static void executeAndAdd(UndoOp op)
		{
		undoQueue.add(op);
		while(undoQueue.size()>5)
			undoQueue.removeFirst();
		op.redo();
		}
	
	
	/**
	 * Add undo operation and replace the last one on the stack
	 */
	public static void executeAndReplaceLast(UndoOp op)
		{
		undoQueue.removeLast();
		undoQueue.add(op);
		op.redo(); //Takes care of updating the windows at the moment!
		}
	
	
	/*
	 * Undo has tricky semantics vs save as. 
	 * boolean, is undo allowed? 
	 * redo is always possible.
	 * 
	 * movement, simple replacement of last undo.
	 * 
	 */
	



	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		
		EvDataMenu.extensions.add(new DataMenuExtension()
			{
			public void buildData(JMenu menu)
				{
				final JMenu miUndo=new JMenu("Undo");
				addMetamenu(menu,miUndo);
				
				//System.out.println("----- building undo menu -----");
				ArrayList<UndoOp> undoOpsReverse=new ArrayList<UndoOp>(undoQueue);
				Collections.reverse(undoOpsReverse);
				for(final UndoOp op:undoOpsReverse)
					{
					JMenuItem mi=new JMenuItem(op.getOpName()); 
					miUndo.add(mi);
					
					mi.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							if(op.canUndo())
								{
								//int state=JOptionPane.showConfirmDialog(null, "Experimental feature. really undo?", "Undo?", JOptionPane.YES_NO_OPTION);
								//if(state==JOptionPane.YES_OPTION)
									//{
									op.undo();
									redoQueue.addFirst(op);
									BasicWindow.updateWindows();
									//}
								}
							else
								BasicWindow.showInformativeDialog("This operation does not support undo");
							}
						});
					
					
					}
				
				System.out.println("redos");
				for(UndoOp op:redoQueue)
					System.out.println("redo: "+op.getOpName());
				/*
				System.out.println("----- building redo menu -----");
				for(UndoOp op:redoQueue)
					{
					JMenuItem mi=new JMenuItem(op.getUndoName()); 
					miUndo.add(mi);
					}
					*/
				
				}
			public void buildOpen(JMenu menu)
				{

				}
			public void buildSave(JMenu menu, final EvData meta)
				{
				}
			});
		
		}

	}
