package endrov.particle;

import endrov.basicWindow.BasicWindow;
import endrov.undo.UndoOpBasic;
import endrov.util.EvDecimal;

/**
 * Undo operation for only changing one keyframe
 * @author Johan Henriksson
 */

public class UndoOpLineageEditParticleKeyframe extends UndoOpBasic
	{
	private Lineage lin;
	private String name;
	private EvDecimal frame;
	private Lineage.ParticlePos pos;
	private Lineage.ParticlePos newPos;
	public UndoOpLineageEditParticleKeyframe(String opname, Lineage lin, String name, EvDecimal frame, Lineage.ParticlePos newPos)
		{
		super(opname);
		this.frame=frame;
		this.lin=lin;
		this.name=name;
		this.newPos=newPos;
		Lineage.Particle nuc=lin.particle.get(name);
		if(nuc.pos.containsKey(frame))
			pos=nuc.pos.get(frame).clone();
		}

	public void redo()
		{
		Lineage.Particle nuc=lin.particle.get(name);
		nuc.pos.put(frame, newPos);
		BasicWindow.updateWindows();  //this is a problem! w.updateImagePanel works well
		}
	
	public void undo()
		{
		if(pos==null)
			lin.particle.get(name).pos.remove(frame);
		else
			lin.particle.get(name).pos.put(frame,pos.clone());
		BasicWindow.updateWindows();
		}
	
	}