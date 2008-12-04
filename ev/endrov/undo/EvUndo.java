package endrov.undo;

import java.util.LinkedList;

import javax.swing.JMenu;

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

	public static void initPlugin() {}
	static
		{
		
		EvDataMenu.extensions.add(new DataMenuExtension()
			{
			public void buildOpen(JMenu menu)
				{
				final JMenu miUndo=new JMenu("Undo");
				addMetamenu(menu,miUndo);

				}
			public void buildSave(JMenu menu, final EvData meta)
				{
				}
			});
		
		
		
		}

	
	public static LinkedList<UndoOp> undoQueue=new LinkedList<UndoOp>();
	public static LinkedList<UndoOp> redoQueue=new LinkedList<UndoOp>();

	public UndoOp getLastUndo()
		{
		return undoQueue.getLast();
		}
	
	/**
	 * Add undo operation. An operation will not be added if it already is at the end of the list
	 */
	public static void addUndo(UndoOp op)
		{
		if(undoQueue.getLast()!=op)
			undoQueue.add(op);
		}
	
	
	/*
	 * Undo has tricky semantics vs save as. 
	 * boolean, is undo allowed? 
	 * redo is always possible.
	 * 
	 * movement, simple replacement of last undo.
	 * 
	 */
	

	
	}
