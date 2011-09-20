/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.particle;

import endrov.basicWindow.EvColor;
import endrov.data.EvSelection.EvSelectable;
import endrov.util.Tuple;

/**
 * A selected particle
 * @author Johan Henriksson
 */
public class LineageSelParticle extends Tuple<Lineage,String> implements EvSelectable
	{
	static final long serialVersionUID=0;
 	public LineageSelParticle(Lineage lin, String nuc)
		{
		super(lin,nuc);
		}
	
	public LineageSelParticle()
		{
		super(null,"");
		}
	
	public String toString()
		{
		return ""+fst()+":"+snd();
		}
	
	public int hashCode()
		{
		//needed? don't think so
		return super.hashCode();
		}
	
	public Lineage.Particle getNuc()
		{
		if(fst()==null)
			throw new RuntimeException("null particle");
		else if(snd()==null)
			throw new RuntimeException("null particle name");
		else
			return fst().particle.get(snd());
		}

	public void setColor(EvColor c)
		{
		getNuc().color=c.getAWTColor();
		}
	
	protected LineageSelParticle clone()
		{
		return new LineageSelParticle(fst(), snd());
		}
	}
