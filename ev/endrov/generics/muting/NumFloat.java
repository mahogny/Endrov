package endrov.generics.muting;

public class NumFloat implements Num
	{
	double x;
	
	public NumFloat(double x)
		{
		this.x = x;
		}

	public Num add(Num a, Num b)
		{
		return new NumFloat(((NumFloat)a).x+((NumFloat)b).x);
		}
	}
