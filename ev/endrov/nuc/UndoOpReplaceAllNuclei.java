package endrov.nuc;

import endrov.basicWindow.BasicWindow;
import endrov.undo.UndoOpBasic;
import endrov.util.EvDecimal;

/**
 * Restore lineage by copying all old coordinates. Inefficient but always works
 * @author Johan Henriksson
 * 
 * TODO metadatamodified
 */
abstract class UndoOpReplaceAllNuclei extends UndoOpBasic
	{
	private NucLineage savedlin;
	private NucLineage lin;
	private boolean metaWasModified;
	private EvDecimal dateLastModify;
	
	public UndoOpReplaceAllNuclei(String opname, NucLineage lin)
		{
		super(opname);
		savedlin=new NucLineage();
		for(String nucName:lin.nuc.keySet())
			savedlin.nuc.put(nucName, lin.nuc.get(nucName).clone());
		metaWasModified=lin.coreMetadataModified;
		dateLastModify=lin.dateLastModify;
		}
	
	public void undo()
		{
		lin.nuc.clear();
		for(String name:lin.nuc.keySet())
			lin.nuc.put(name, savedlin.nuc.get(name).clone());
		lin.coreMetadataModified=metaWasModified;
		lin.dateLastModify=dateLastModify;
		BasicWindow.updateWindows();
		}
	}