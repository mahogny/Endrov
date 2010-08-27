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
 *
 */
public abstract class UndoOpReplaceSomeNuclei extends UndoOpBasic
	{
	private HashMap<Tuple<NucLineage,String>, NucLineage.Nuc> oldnuc=new HashMap<Tuple<NucLineage,String>, NucLineage.Nuc>();
	
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
		BasicWindow.updateWindows();
		}
	}