package endrov.ev;

//TODO: add ev observers

/**
 * Mutable Boolean - replacement for Boolean whenever one wants to be able to change the value 
 * 
 * @author Johan Henriksson
 */
public class EvMutableBoolean 
	{
	static final long serialVersionUID=0;
	private boolean b;

	
	public EvMutableBoolean()
		{
		b=false;
		}
	
	public EvMutableBoolean(boolean b)
		{
		this.b=b;
		}

	
	public void setValue(boolean b)
		{
		this.b=b;
		}
	public boolean getValue()
		{
		return b;
		}
	
	
	}
