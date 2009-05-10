package endrov.unsortedImageFilters.unfinished;

import java.util.HashMap;
import java.util.Map;

public class Reified<E,F>
	{
	E e;
	F f;
	
	public Reified(E e, F f)
		{
		this.e=e;
		this.f=f;
		}
	
	public static void main(String[] args)
		{
		
		Reified<Integer, Map<Integer,String>> map=new Reified<Integer, Map<Integer, String>>(0, new HashMap<Integer, String>());
		
		//Not happy with this
		
		
		}
	
	}
