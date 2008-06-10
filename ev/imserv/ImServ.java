package imserv;

import javax.swing.JFrame;

import evplugin.ev.EV;
import evplugin.ev.Log;

/**
 * Ev Image server
 * @author Johan Henriksson
 */
public class ImServ
	{

	public static class GUI extends JFrame
		{
		static final long serialVersionUID=0;
		Daemon daemon=new Daemon();
		
		
		public GUI()
			{
			
			
			setVisible(true);
			}
		
		
		}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new evplugin.ev.StdoutLog());
		EV.loadPlugins();

		
		//Should run in both console and windowed mode
		
		
		new GUI();
		
		
		
		}
	
	}
