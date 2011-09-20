package endrov.particle;

import endrov.basicWindow.BasicWindow;
import endrov.undo.UndoOpBasic;
import endrov.util.EvDecimal;

/**
 * Restore lineage by copying all old coordinates. Inefficient but always works
 * @author Johan Henriksson
 * 
 * TODO metadatamodified. change it! create a redo2?
 */
public abstract class UndoOpLineageReplaceAllParticle extends UndoOpBasic
	{
	private Lineage linCopy;
	private Lineage lin;
	private boolean metaWasModified;
	private EvDecimal dateLastModify;	
	private EvDecimal newDateLastModify=new EvDecimal(System.currentTimeMillis());
	
	public void modifyObjects()
		{
		lin.setMetadataModified();
		lin.dateLastModify=newDateLastModify;
		}
	
	
	public UndoOpLineageReplaceAllParticle(String opname, Lineage lin)
		{
		super(opname);
		linCopy=new Lineage();
		this.lin=lin;
		for(String nucName:lin.particle.keySet())
			linCopy.particle.put(nucName, lin.particle.get(nucName).clone());
		metaWasModified=lin.coreMetadataModified;
		dateLastModify=lin.dateLastModify;
		}
	
	public void undo()
		{
		lin.particle.clear();
		for(String name:linCopy.particle.keySet())
			lin.particle.put(name, linCopy.particle.get(name).clone());
		lin.coreMetadataModified=metaWasModified;
		lin.dateLastModify=dateLastModify;
		BasicWindow.updateWindows();
		}
	}