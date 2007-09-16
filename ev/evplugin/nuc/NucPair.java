package evplugin.nuc;

import evplugin.ev.Pair;

//Maybe a bad class. just use the normal pair? otherwise get rid of pair, and left and right?

public class NucPair extends Pair<NucLineage,String>
	{
	public NucPair(NucLineage lin, String nuc)
		{
		super(lin,nuc);
		}
	
	public NucPair()
		{
		super(null,"");
		}
	
	public String toString()
		{
		return ""+getLeft()+":"+getRight();
		}
	
	public int hashCode()
		{
		//needed?
		return super.hashCode();
		}
	}
