package endrov.line;

import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.undo.UndoOpBasic;

/**
 * Adding or replacing an object in a container
 * 
 * @author Johan Henriksson
 *
 */
public class UndoOpPutObject extends UndoOpBasic
	{
	private EvContainer container;
	/**
	 * How safe is it to point to the original object? Quite safe, if other undo operations
	 * modify it back to the original state
	 */
	private EvObject newOb;
	private String id;
	private EvObject oldOb;
	public UndoOpPutObject(String opName, EvObject newOb, EvContainer container, String id)
		{
		super(opName);
		this.newOb=newOb;
		this.container=container;
		this.id=id;
		}

	public void redo()
		{
		oldOb=container.metaObject.get(id);
		container.metaObject.put(id, newOb);
		//id=container.addMetaObject(newOb);
		}

	public void undo()
		{
		container.metaObject.remove(id);
		if(oldOb!=null)
			container.metaObject.put(id, oldOb);
		}
	
	}