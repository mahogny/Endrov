/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.generics.testinterface;


public class Caller
	{

	//   -XX:+PrintAssembly

	//  /home/mahogny/debug/jdk1.6.0_18/fastdebug/bin/java -XX:+PrintAssembly endrov.generics.inline.Caller
	
	public static int add(int a, int b, int x1, int x2, int x3, int x4, int x5, int x6)
		{
		return a+b;
		}
	
	/**
	 * This code shows no performance difference, local add vs the add in Statics
	 * @param args
	 */
	public static void main(String[] args)
		{
		Iface f=new Implem();
		for(int i=0;i<100000;i++)
			//add(3,5,0,0,0,0,0,0);
			f.add(3,5,0,0,0,0,0,0);
		long start=System.currentTimeMillis();
		int foo=0;
		for(long i=0;i<1000000000l;i++)
			{
			
			foo+=f.add(3,5,0,0,0,0,0,0);
			foo+=f.add(3,5,0,0,0,0,0,0);
			foo+=f.add(3,5,0,0,0,0,0,0);
			foo+=f.add(3,5,0,0,0,0,0,0);
			foo+=f.add(3,5,0,0,0,0,0,0);
			foo+=f.add(3,5,0,0,0,0,0,0);
			foo+=f.add(3,5,0,0,0,0,0,0);
			foo+=f.add(3,5,0,0,0,0,0,0);
			foo+=f.add(3,5,0,0,0,0,0,0);
			foo+=f.add(3,5,0,0,0,0,0,0);
			
			/*
			foo+=add(3,5,0,0,0,0,0,0);
			foo+=add(3,5,0,0,0,0,0,0);
			foo+=add(3,5,0,0,0,0,0,0);
			foo+=add(3,5,0,0,0,0,0,0);
			foo+=add(3,5,0,0,0,0,0,0);
			foo+=add(3,5,0,0,0,0,0,0);
			foo+=add(3,5,0,0,0,0,0,0);
			foo+=add(3,5,0,0,0,0,0,0);
			foo+=add(3,5,0,0,0,0,0,0);
			foo+=add(3,5,0,0,0,0,0,0);
			*/
			}
		if(foo==20)
			System.out.println(foo);
		long end=System.currentTimeMillis();
		System.out.println("--------------time "+(end-start));
		}
	}
