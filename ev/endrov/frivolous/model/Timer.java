package endrov.frivolous.model;

public class Timer {

	String name;
	long time_reset;
	long time_last;
	public boolean on = false;
	
	public Timer(String timer_name) {
		name = timer_name;
		if(on)System.out.println("Timer "+name+": Started, 0 ms");
		time_reset = System.currentTimeMillis();
		time_last = time_reset;
	}

	public void show(String completed_action) {
		long time_current = System.currentTimeMillis();
		if(on)System.out.println("Timer "+name+": "+completed_action+", "+(time_current-time_reset)+" ms ("+(time_current-time_last)+" ms)");
		time_last = time_current;
	}

	public void reset(){
		if(on)System.out.print("Timer "+name+": Reset called, 0 ms");
		time_reset = System.currentTimeMillis();
	}
}
