package endrov.generics.muting;

public class Main
	{
	
	public static void swap(double[] narr1, double[] narr2)
		{
		for(int i=0;i<200;i++)
			{
			double v1=narr2[i];
			double v2=narr1[i];
			narr1[i]=v1;
			narr2[i]=v2;
			}
		}
	
	public static void main(String[] args)
		{
		
		ArrDouble arr1=new ArrDouble(), arr2=new ArrDouble();

		for(int numloop=0;numloop<1000;numloop++)
			for(int i=0;i<200;i++)
				{
				Num v1=arr2.get(i);
				Num v2=arr1.get(i);
				
				arr1.set(i, v1);
				arr2.set(i, v2);
				}

		long start=System.currentTimeMillis();
		for(int numloop=0;numloop<100000;numloop++)
			for(int i=0;i<200;i++)
				{
				Num v1=arr2.get(i);
				Num v2=arr1.get(i);
				
				arr1.set(i, v1);
				arr2.set(i, v2);
				}
		long end=System.currentTimeMillis();

		System.out.println("time "+(end-start));

		
		////////////
		
		Mut a=new MutFloat(), b=new MutFloat();
		for(int numloop=0;numloop<100000;numloop++)
			for(int i=0;i<200;i++)
				{
				arr2.get(i,a);
				arr1.get(i,b);
				arr1.set(i,a);
				arr2.set(i,b);
				}

		long start2=System.currentTimeMillis();
		for(int numloop=0;numloop<100000;numloop++)
			for(int i=0;i<200;i++)
				{
				arr2.get(i,a);
				arr1.get(i,b);
				arr1.set(i,a);
				arr2.set(i,b);
				}
		long end2=System.currentTimeMillis();

		System.out.println("time "+(end2-start2));

		///////////
		double[] narr1=new double[200];
		double[] narr2=new double[200];
		
		for(int numloop=0;numloop<100000;numloop++)
			swap(narr1, narr2);

		long start1=System.currentTimeMillis();
		for(int numloop=0;numloop<100000;numloop++)
			swap(narr1, narr2);
		long end1=System.currentTimeMillis();

		System.out.println("time "+(end1-start1));
		
		
		}
	}
