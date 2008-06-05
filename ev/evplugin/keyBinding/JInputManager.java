package evplugin.keyBinding;

import java.util.HashMap;
import java.util.LinkedList;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;


public class JInputManager implements Runnable
	{
	private static HashMap<net.java.games.input.Controller, EvController> evController=new HashMap<net.java.games.input.Controller, EvController>();
	
	
	static
	{
	ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();

	Controller[] ca = ce.getControllers();
	for(Controller cai:ca)
		{
		//System.out.println(cai.getName());
//		System.out.println("Type: "+cai.getType().toString());
		Component[] components = cai.getComponents();
		System.out.println("Component Count: "+components.length);
		for(int j=0;j<components.length;j++)
			{
//			System.out.println("Component "+j+": "+components[j].getName());
			System.out.println("    Identifier: "+components[j].getIdentifier().getName());
	//		System.out.print("    ComponentType: ");
			if (components[j].isRelative())
				System.out.print("Relative");
			else 
				System.out.print("Absolute");
			if (components[j].isAnalog())
				System.out.print(" Analog");
			else 
				System.out.print(" Digital");
//			System.out.println();
			}
//		System.out.println("---------------------------------");
		
		net.java.games.input.Event event=new net.java.games.input.Event();
		net.java.games.input.EventQueue queue=cai.getEventQueue();
		for(;;)
			{
			//poll?
			try { Thread.sleep(20);} catch (InterruptedException e) { }
			while(queue.getNextEvent(event))
				{
				}
			//buffer.append(components[i].getPollData());
			}
		
		}



	

	}
	
	
	
	
	
	public JInputManager()
		{
		new Thread(this).start();
		
		
		}
	
	
	
	
	public void run()
		{
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		Controller firstMouse=null;
		for(int i=0;i<controllers.length && firstMouse==null;i++) 
			{
			if(controllers[i].getType()==Controller.Type.GAMEPAD || 
					controllers[i].getType()==Controller.Type.STICK)
				firstMouse = controllers[i];
			}
		if(firstMouse==null) 
			{
			// Couldn't find a mouse
			System.out.println("Found no gamepad");
			System.exit(0);
			}

		System.out.println("First gamepad is: " + firstMouse.getName());
		while(true)
			{
			firstMouse.poll();
			try
				{
				Thread.sleep(100);
				}
			catch (Exception e)
				{
				}
			Component[] components = firstMouse.getComponents();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<components.length;i++)
				{
				if(i>0) 
					buffer.append(", ");

				buffer.append(components[i].getName());
				buffer.append(": ");
				
				//if(components[i].isAnalog())
				buffer.append(components[i].getPollData());
				
				}
			System.out.println(buffer);
			}

		}
	
	
	private class EvController
		{
		HashMap<net.java.games.input.Component, EvControllerComponent> component=new HashMap<Component, EvControllerComponent>();
		
		/** Axis', meaning absolute analog */ 
		LinkedList<net.java.games.input.Component> analogAxisComponent=new LinkedList<Component>();
		
		//stupid cross encodes into 
		
		
		}
	
	private class EvControllerComponent
		{
		public double midValue, deadRegion;
		public double curValue; //0 if disabled, otherwise 1
		
		
		//in keybind, store name of component and controller name.
		//cache controller and component to avoid expensive string lookup?
		
		//special keyAxisBind, 
		
		}
	
	
	
	
	
	public interface JInputListener
		{
		public void jinputDigital(EvController cont, boolean enabled);
		
		/** Called every 50ms if axis not in dead region */
		public void jinputAxis(EvController cont);
		
		
		}
	
	
	
	
	
	}
