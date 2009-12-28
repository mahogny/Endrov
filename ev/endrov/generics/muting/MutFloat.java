package endrov.generics.muting;

public class MutFloat implements Mut
	{
	double x;
	
	public void add(Num b)
		{
		x+=((MutFloat)b).x;
		}
	}
