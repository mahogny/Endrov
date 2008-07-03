package endrov.keyBinding;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class JStest
	{

	
	public static void main(String[] arg)
		{


		/** Creates a new instance of ControllerScanner */
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		System.out.println("Controller Env = "+ce.toString());


		Controller[] ca = ce.getControllers();
		for(Controller cai:ca)
			{
			System.out.println(cai.getName());
			System.out.println("Type: "+cai.getType().toString());
			Component[] components = cai.getComponents();
			System.out.println("Component Count: "+components.length);
			for(int j=0;j<components.length;j++)
				{
				System.out.println("Component "+j+": "+components[j].getName());
				System.out.println("    Identifier: "+components[j].getIdentifier().getName());
				System.out.print("    ComponentType: ");
				if (components[j].isRelative())
					System.out.print("Relative");
				else 
					System.out.print("Absolute");
				if (components[j].isAnalog())
					System.out.print(" Analog");
				else 
					System.out.print(" Digital");
				System.out.println();
				}
			System.out.println("---------------------------------");
			}


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

	}
