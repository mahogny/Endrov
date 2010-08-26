/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.undo;



/**
 * Undo operation: there is no undo. Implement redo only.
 * 
 * @author Johan Henriksson
 */
public abstract class UndoOpBasic implements UndoOp
	{
	private String name;
	public UndoOpBasic(String opName)
		{
		name=opName;
		}
	public boolean canUndo()
		{
		return true;
		}
	public String getOpName()
		{
		return name;
		}
	
	public void execute()
		{
		EvUndo.executeAndAdd(this);
		}
	}
