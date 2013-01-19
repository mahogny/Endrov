/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.generics.muting;

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
