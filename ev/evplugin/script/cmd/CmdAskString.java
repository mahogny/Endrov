package evplugin.script.cmd;
import java.util.*;
import evplugin.script.*;
import javax.swing.*;

public class CmdAskString extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Object e=Command.expVal(arg.get(1));
		String input=JOptionPane.showInputDialog(""+e);
		return new ExpVal(input);
		}
	}
