package endrov.nuc;

import endrov.ev.Tuple;

//Maybe a bad class. just use the normal pair? otherwise get rid of pair, and left and right?

public class NucPair extends Tuple<NucLineage,String>
	{
	static final long serialVersionUID=0;
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
		return ""+fst()+":"+snd();
		}
	
	public int hashCode()
		{
		//needed?
		return super.hashCode();
		}
	}
