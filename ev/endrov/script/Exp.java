package endrov.script;

public abstract class Exp
	{
	
	public String stringValue() throws Exception
		{
		if(this instanceof ExpVal)
			{
			ExpVal val=(ExpVal)this;
			return (String)val.o;
			}
		else
			{ //Hack(?): cast sym to string. You still need to check if it is the right type
			ExpSym sym=(ExpSym)this;
			return sym.sym;
			}
			
			
			
		/*
		if(this instanceof ExpVal)
			{
			ExpVal val=(ExpVal)
			return ()
			}
		else
			throw new Exception("Not a string");
			*/
		}
	}
