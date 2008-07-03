package OSTmaker;
import endrov.starter.*;

/**
 * Start OST maker
 * @author Johan Henriksson
 */
public class StartOSTmaker
	{
	public static void main(String[] args)
		{
		String[] args2=new String[args.length+1];
		args2[0]="OSTmaker.Main";
		for(int i=0;i<args.length;i++)
			args2[i+1]=args[i];
		new StartGUI().run(args2);
		}
	}
