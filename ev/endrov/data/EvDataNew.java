package endrov.data;

import java.util.LinkedList;

public abstract class EvDataNew
	{

	//Undo ops are defined on level of one EvData. cross-evdata is generally wtf?
	
	LinkedList<UndoOp> undoList=new LinkedList<UndoOp>();
	LinkedList<UndoOp> redoList=new LinkedList<UndoOp>();

	public abstract UndoOp getLastUndo();
	
	/**
	 * Add undo operation. If the same operation is added twice then the last value on the queue will not be replaced.
	 */
	public void addUndo(UndoOp op)
		{
		if(undoList.getLast()!=op)
			undoList.add(op);
		}
	
	
	/*
	 * Undo has tricky semantics vs save as. 
	 * boolean, is undo allowed? 
	 * redo is always possible.
	 * 
	 * movement, simple replacement of last undo.
	 * 
	 */
	
	
	public interface UndoOp
		{
		public boolean canUndo();
		public void undo();
		public void redo();
		
		
		
		public String undoName();
		}
	
	/**
	 * This is meant as a stupid fallback. Make a deep copy of an entire object
	 *
	 */
	public class UndoOpStateSave //implements UndoOp
		{
		public UndoOpStateSave(String objName, String name)
			{
			
			}
		}
	
	}
