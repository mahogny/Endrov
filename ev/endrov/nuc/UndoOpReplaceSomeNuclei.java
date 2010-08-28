package endrov.nuc;

import java.util.HashMap;

import endrov.basicWindow.BasicWindow;
import endrov.undo.UndoOpBasic;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * Undo operation where the undo is replacing individual cells with previous copies 
 * @author Johan Henriksson
 * 
 * TODO metadatamodified. change it! create a redo2?
 *
 */
public abstract class UndoOpReplaceSomeNuclei extends UndoOpBasic
	{
	private HashMap<Tuple<NucLineage,String>, NucLineage.Nuc> oldnuc=new HashMap<Tuple<NucLineage,String>, NucLineage.Nuc>();
	
	private EvDecimal newDateLastModify=new EvDecimal(System.currentTimeMillis());
	private HashMap<NucLineage,ModObj> oldob=new HashMap<NucLineage, ModObj>();
	private static class ModObj
		{
		private boolean metaWasModified;
		private EvDecimal dateLastModify;
		public ModObj(boolean metaWasModified, EvDecimal dateLastModify)
			{
			this.metaWasModified = metaWasModified;
			this.dateLastModify = dateLastModify;
			}
		public void restore(NucLineage lin)
			{
			lin.coreMetadataModified=metaWasModified;
			lin.dateLastModify=dateLastModify;
			}
		}
	
	public void modifyObjects()
		{
		for(NucLineage lin:oldob.keySet())
			{
			lin.setMetadataModified();
			lin.dateLastModify=newDateLastModify;
			}
		}
	
	public UndoOpReplaceSomeNuclei(String name)
		{
		super(name);
		}

	/**
	 * Keep nucleus for later. Will only store the nuclei the first time this function is called
	 */
	public void keep(NucLineage lin, String name)
		{
		Tuple<NucLineage,String> key=Tuple.make(lin, name);
		if(!oldnuc.containsKey(key))
			{
			NucLineage.Nuc n=lin.nuc.get(name);
			if(n!=null)
				oldnuc.put(key, n.clone());
			else
				oldnuc.put(key, null);
			}
		if(oldob.containsKey(lin))
			oldob.put(lin, new ModObj(lin.coreMetadataModified, lin.dateLastModify));
		}

	public void undo()
		{
		for(Tuple<NucLineage,String> key:oldnuc.keySet())
			{
			NucLineage lin=key.fst();
			NucLineage.Nuc theoldnuc=oldnuc.get(key);
			if(theoldnuc==null)
				lin.nuc.remove(key.snd());
			else
				lin.nuc.put(key.snd(), theoldnuc.clone());
			}
		for(NucLineage lin:oldob.keySet())
			oldob.get(lin).restore(lin);
		BasicWindow.updateWindows();
		}
	}