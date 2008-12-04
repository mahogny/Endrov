package endrov.undo;

import org.jdom.Element;

import endrov.data.EvData;

/**
 * Undo operation: quick implementation by serializing the entire object.
 * It is a good tradeoff of speed vs implementation efficiency if the object is small.
 * Serializing ops never commutate.
 * Implement redo only.
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
	public String undoName()
		{
		return name;
		}
	}
