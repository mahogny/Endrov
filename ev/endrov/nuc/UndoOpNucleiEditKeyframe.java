package endrov.nuc;

import endrov.basicWindow.BasicWindow;
import endrov.undo.UndoOpBasic;
import endrov.util.EvDecimal;

/**
 * Undo operation for only changing one keyframe
 * @author Johan Henriksson
 */

public class UndoOpNucleiEditKeyframe extends UndoOpBasic
	{
	private NucLineage lin;
	private String name;
	private EvDecimal frame;
	private NucLineage.NucPos pos;
	private NucLineage.NucPos newPos;
	public UndoOpNucleiEditKeyframe(String opname, NucLineage lin, String name, EvDecimal frame, NucLineage.NucPos newPos)
		{
		super(opname);
		this.frame=frame;
		this.lin=lin;
		this.name=name;
		this.newPos=newPos;
		NucLineage.Nuc nuc=lin.nuc.get(name);
		if(nuc.pos.containsKey(frame))
			pos=nuc.pos.get(frame).clone();
		}

	public void redo()
		{
		NucLineage.Nuc nuc=lin.nuc.get(name);
		nuc.pos.put(frame, newPos);
		BasicWindow.updateWindows();  //this is a problem! w.updateImagePanel works well
		}
	
	public void undo()
		{
		if(pos==null)
			lin.nuc.get(name).pos.remove(frame);
		else
			lin.nuc.get(name).pos.put(frame,pos.clone());
		BasicWindow.updateWindows();
		}
	
	}