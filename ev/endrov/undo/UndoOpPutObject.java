package endrov.undo;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvContainer;
import endrov.data.EvObject;

/**
 * Adding, replacing or deleting an object in a container
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
		if(newOb==null)
			container.metaObject.remove(id);
		else
			container.metaObject.put(id, newOb);
		BasicWindow.updateWindows();
		}

	public void undo()
		{
		container.metaObject.remove(id);
		if(oldOb!=null)
			container.metaObject.put(id, oldOb);
		}
	
	}