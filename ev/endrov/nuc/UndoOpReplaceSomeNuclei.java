package endrov.nuc;

import java.util.HashMap;

import endrov.basicWindow.BasicWindow;
import endrov.undo.UndoOpBasic;
import endrov.util.Tuple;

/**
 * Undo operation where the undo is replacing individual cells with previous copies 
 * @author Johan Henriksson
 * 
 * TODO metadatamodified
 * TODO ensure clone really clones everything
 *
 */
abstract class UndoOpReplaceSomeNuclei extends UndoOpBasic
	{
	private HashMap<Tuple<NucLineage,String>, NucLineage.Nuc> oldnuc=new HashMap<Tuple<NucLineage,String>, NucLineage.Nuc>();
	
	public UndoOpReplaceSomeNuclei(String name)
		{
		super(name);
		}

	/**
	 * Keep nucleus for later. Will only store the nuclei the first time this function is called
	 */
	public void keepNuc(NucLineage lin, String name)
		{
		Tuple<NucLineage,String> key=Tuple.make(lin, name);
		if(!oldnuc.containsKey(key))
			oldnuc.put(key, lin.nuc.get(name));
		}
/*
	public boolean isKept(NucLineage lin, String name)
		{
		return oldnuc.containsKey(Tuple.make(lin, name));
		}*/

	public void undo()
		{
		for(Tuple<NucLineage,String> key:oldnuc.keySet())
			{
			System.out.println("Replacing "+key.snd());
			NucLineage lin=key.fst();
			lin.nuc.put(key.snd(), oldnuc.get(key).clone());
			}
		BasicWindow.updateWindows();
		}
	}