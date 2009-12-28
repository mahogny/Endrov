package endrov.generics.muting;

public class ArrDouble implements MyArray
	{
	double[] arr=new double[200];
	
	public Num get(int i)
		{
		return new NumFloat(arr[i]);
		}

	public void get(int i, Mut m)
		{
		((MutFloat)m).x=arr[i];
		}

	public void set(int i, Num a)
		{
		arr[i]=((NumFloat)a).x;
		}

	public void set(int i, Mut a)
		{
		arr[i]=((MutFloat)a).x;
		}

	}
