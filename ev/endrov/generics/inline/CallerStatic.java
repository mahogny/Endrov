package endrov.generics.inline;

public class CallerStatic
	{

	//   -XX:+PrintAssembly

	//  /home/mahogny/debug/jdk1.6.0_18/fastdebug/bin/java -XX:+PrintAssembly endrov.generics.inline.Caller
	
	
	/**
	 * This code shows no performance difference, local add vs the add in Statics
	 * @param args
	 */
	public static void main(String[] args)
		{
		for(int i=0;i<1000000000;i++)
			Statics.add(3,5,0,0,0,0,0,0);
		long start=System.currentTimeMillis();
		double foo=0;
		for(long i=0;i<10000000000l;i++)
			foo=Statics.add(3,5,0,0,0,0,0,0);
		if(foo==20)
			System.out.println(foo);
		long end=System.currentTimeMillis();
		System.out.println("--------------time "+(end-start));
		}
	}
