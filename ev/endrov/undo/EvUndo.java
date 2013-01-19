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

import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;




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

	public static UndoOp getLastUndo()
		{
		if(undoQueue.isEmpty())
			return null;
		else
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
		redoQueue.clear();
		op.redo();
		//TODO update menu 
		}
	
	
	/**
	 * Add undo operation and replace the last one on the stack
	 */
	public static void executeAndReplaceLast(UndoOp op)
		{
		undoQueue.removeLast();
		undoQueue.add(op);
		redoQueue.clear();
		op.redo();
		//The menu need not be updated in this case
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
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
			{
			public void newBasicWindow(EvBasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(), new EvBasicWindowHook()
						{
						public void createMenus(EvBasicWindow w)
							{
							

							boolean supportDifferentOrder=false;
							
							
							//////////////////// Undo //////////////////////
								
							final JMenu miUndo=new JMenu("Undo");
							w.addMenuOperation(miUndo,"0_0undo");
							
							ArrayList<UndoOp> undoOpsReverse=new ArrayList<UndoOp>(undoQueue);
							Collections.reverse(undoOpsReverse);
							int undoEntryCount=0;
							for(final UndoOp op:undoOpsReverse)
								{
								JMenuItem mi;
								if(undoEntryCount==0)
									mi=new JMenuItem(op.getOpName());
								else
									mi=new JMenuItem("! "+op.getOpName());
								miUndo.add(mi);
								final int fcount=undoEntryCount;
								if(undoEntryCount==0)
									mi.addActionListener(new ActionListener()
										{
										public void actionPerformed(ActionEvent e)
											{
											if(op.canUndo())
												{
												op.undo();
												undoQueue.removeLast();
												redoQueue.addFirst(op);
												EvBasicWindow.updateWindows();
												}
											else
												EvBasicWindow.showInformativeDialog("This operation does not support undo");
											}
										});
								else
									mi.addActionListener(new ActionListener()
										{
										public void actionPerformed(ActionEvent e)
											{
											if(op.canUndo())
												{
												if(EvBasicWindow.showConfirmYesNoDialog(
														"This is not the last operation. Undoing it is can be incredibly unsafe unless you know what you are doing. Sure?"))
													{
													op.undo();
													for(int i=0;i<fcount+1;i++)
														undoQueue.removeLast();
													redoQueue.clear();
													EvBasicWindow.updateWindows();
													}
												}
											else
												EvBasicWindow.showInformativeDialog("This operation does not support undo");
											}
										});
									
								if(!supportDifferentOrder)
									break;
								undoEntryCount++;
								}
							
							//////////////////// Redo //////////////////////

							
							final JMenu miRedo=new JMenu("Redo");
							w.addMenuOperation(miRedo,"0_1redo");
							//addMetamenu(menu,miRedo);
							
							int redoEntryCount=0;
							for(final UndoOp op:redoQueue)
								{
								JMenuItem mi;
								if(redoEntryCount==0)
									mi=new JMenuItem(op.getOpName());
								else
									mi=new JMenuItem("! "+op.getOpName());
								miRedo.add(mi);
								if(redoEntryCount==0)
									mi.addActionListener(new ActionListener()
										{
										public void actionPerformed(ActionEvent e)
											{
											undoQueue.add(op);
											redoQueue.removeFirst();
											op.redo();
											EvBasicWindow.updateWindows(); //Needed?
											}
										});
								else
									mi.addActionListener(new ActionListener()
										{
										public void actionPerformed(ActionEvent e)
											{
											int state=JOptionPane.showConfirmDialog(null, 
													"This is not the first operation. Redoing it can be incredibly unsafe unless you know what you are doing. Sure?", "Redo?", JOptionPane.YES_NO_OPTION);
											if(state==JOptionPane.YES_OPTION)
												{
												undoQueue.addLast(op);
												redoQueue.clear();
												op.redo();
												EvBasicWindow.updateWindows(); //Needed?
												}

											}
										});
									
								if(!supportDifferentOrder)
									break;
								redoEntryCount++;
								}
							
							}
						public void buildMenu(EvBasicWindow w){}
						});
				}
			});
		
		
		
		}

	}
