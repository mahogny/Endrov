package endrov.nuc;

import endrov.basicWindow.BasicWindow;
import endrov.undo.UndoOpBasic;
import endrov.util.EvDecimal;

/**
 * Restore lineage by copying all old coordinates. Inefficient but always works
 * @author Johan Henriksson
 * 
 * TODO metadatamodified. change it! create a redo2?
 */
public abstract class UndoOpReplaceAllNuclei extends UndoOpBasic
	{
	private NucLineage linCopy;
	private NucLineage lin;
	private boolean metaWasModified;
	private EvDecimal dateLastModify;	
	private EvDecimal newDateLastModify=new EvDecimal(System.currentTimeMillis());
	
	public void modifyObjects()
		{
		lin.setMetadataModified();
		lin.dateLastModify=newDateLastModify;
		}
	
	
	public UndoOpReplaceAllNuclei(String opname, NucLineage lin)
		{
		super(opname);
		linCopy=new NucLineage();
		this.lin=lin;
		for(String nucName:lin.nuc.keySet())
			linCopy.nuc.put(nucName, lin.nuc.get(nucName).clone());
		metaWasModified=lin.coreMetadataModified;
		dateLastModify=lin.dateLastModify;
		}
	
	public void undo()
		{
		lin.nuc.clear();
		for(String name:linCopy.nuc.keySet())
			lin.nuc.put(name, linCopy.nuc.get(name).clone());
		lin.coreMetadataModified=metaWasModified;
		lin.dateLastModify=dateLastModify;
		BasicWindow.updateWindows();
		}
	}