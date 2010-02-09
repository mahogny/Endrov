/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nuc;

import endrov.basicWindow.EvColor;
import endrov.data.EvSelection.EvSelectable;
import endrov.util.Tuple;

/**
 * A selected nucleus
 * @author Johan Henriksson
 */
public class NucSel extends Tuple<NucLineage,String> implements EvSelectable
	{
	static final long serialVersionUID=0;
 	public NucSel(NucLineage lin, String nuc)
		{
		super(lin,nuc);
		}
	
	public NucSel()
		{
		super(null,"");
		}
	
	public String toString()
		{
		return ""+fst()+":"+snd();
		}
	
	public int hashCode()
		{
		//needed?
		return super.hashCode();
		}
	
	public NucLineage.Nuc getNuc()
		{
		return fst().nuc.get(snd());
		}

	public void setColor(EvColor c)
		{
		getNuc().colorNuc=c.getAWTColor();
		}
	}
