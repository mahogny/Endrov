package endrov.undo;

/**
 * One undo operation
 */
public interface UndoOp
	{
	/**
	 * Can undo? Some operations cannot but should still be wrapped to tell the user this is a one-way trip.
	 * Also, it is not good if the last operation was "silent", the wrong operation will be undone.
	 */
	public boolean canUndo();
	
	/**
	 * Take data to the last state 
	 */
	public void undo();
	
	/**
	 * Take data to the next state
	 */
	public void redo();
	
	/**
	 * What is the name of the undo?
	 */
	public String undoName();
	}