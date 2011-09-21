package endrov.lineage;

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
public abstract class UndoOpLineageReplaceSomeParticle extends UndoOpBasic
	{
	private HashMap<Tuple<Lineage,String>, Lineage.Particle> oldnuc=new HashMap<Tuple<Lineage,String>, Lineage.Particle>();
	
	private EvDecimal newDateLastModify=new EvDecimal(System.currentTimeMillis());
	private HashMap<Lineage,ModObj> oldob=new HashMap<Lineage, ModObj>();
	private static class ModObj
		{
		private boolean metaWasModified;
		private EvDecimal dateLastModify;
		public ModObj(boolean metaWasModified, EvDecimal dateLastModify)
			{
			this.metaWasModified = metaWasModified;
			this.dateLastModify = dateLastModify;
			}
		public void restore(Lineage lin)
			{
			lin.coreMetadataModified=metaWasModified;
			lin.dateLastModify=dateLastModify;
			}
		}
	
	public void modifyObjects()
		{
		for(Lineage lin:oldob.keySet())
			{
			lin.setMetadataModified();
			lin.dateLastModify=newDateLastModify;
			}
		}
	
	public UndoOpLineageReplaceSomeParticle(String name)
		{
		super(name);
		}

	/**
	 * Keep particle for later. Will only store the particles the first time this function is called
	 */
	public void keep(Lineage lin, String name)
		{
		Tuple<Lineage,String> key=Tuple.make(lin, name);
		if(!oldnuc.containsKey(key))
			{
			Lineage.Particle n=lin.particle.get(name);
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
		for(Tuple<Lineage,String> key:oldnuc.keySet())
			{
			Lineage lin=key.fst();
			Lineage.Particle theoldnuc=oldnuc.get(key);
			if(theoldnuc==null)
				lin.particle.remove(key.snd());
			else
				lin.particle.put(key.snd(), theoldnuc.clone());
			}
		for(Lineage lin:oldob.keySet())
			oldob.get(lin).restore(lin);
		BasicWindow.updateWindows();
		}
	}