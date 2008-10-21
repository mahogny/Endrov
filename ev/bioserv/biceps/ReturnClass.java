package bioserv.biceps;

import java.util.concurrent.Semaphore;

public class ReturnClass
	{

	public Object returnval;
	public Semaphore sem=new Semaphore(0);
	
	/*
	@SuppressWarnings("unused")
	public void run(Integer o)
		{
//		System.out.println("cb "+o);
		returnval=o;
		sem.release();
		}
	
	*/
	}
