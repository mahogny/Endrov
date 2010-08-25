/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.undo;

import org.jdom.Element;

import endrov.data.EvData;

/**
 * Undo operation: quick implementation by serializing the entire object.
 * It is a good tradeoff of speed vs implementation efficiency if the object is small.
 * Serializing operations never commutate.
 * Only need to implement redo
 * 
 * Only one problem: cannot deal with special listeners, and will break all pointers! 
 * 
 * @author Johan Henriksson
 */
public abstract class UndoOpSerializeObject implements UndoOp
	{
	String name;
	public UndoOpSerializeObject(EvData data, String objName, String undoName)
		{
		name=undoName;
		Element xml=new Element("root");
		data.getMetaObject(objName).saveMetadata(xml);
		}
	public boolean canUndo()
		{
		return true;
		}
	public void undo()
		{
		}
	public String getOpName()
		{
		return name;
		}
	}
