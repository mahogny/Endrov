package endrov.undo;



/**
 * Undo operation: there is no undo. Implement redo only.
 * 
 * @author Johan Henriksson
 */
public abstract class UndoOpNone implements UndoOp
	{
	String name;
	public UndoOpNone(String s)
		{
		name=s;
		}
	public boolean canUndo()
		{
		return false;
		}
	public void undo()
		{
		}
	public String undoName()
		{
		return name;
		}
	}
