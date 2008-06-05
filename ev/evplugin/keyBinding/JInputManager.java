package evplugin.keyBinding;


import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;


public class JInputManager implements Runnable
	{
	
	
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
			
			/*
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
			*/
			}
	
	
	
		
	
		}
		
	
	public static final String[] povList={"Neutral","NW","N","NE","E","SE","S","SW","W"};
	
	
	public double povXaxis(double in)
		{
		if(in==0.125 || in==0.875 || in==1) return -1;
		else if(in==0.375 || in==0.25 || in==0.625) return 1;
		return 0;
		}
	public double povYaxis(double in)
		{
		if(in==0.125 || in==0.25 || in==0.375) return -1;
		else if(in==0.625 || in==0.75 || in==0.825) return 1;
		return 0;
		}
	
	
	public JInputManager()
		{
		new Thread(this).start();
		
		
		}
	
	


	public void run()
		{
		/*
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
		 */
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		while(true)
			{
			try{Thread.sleep(20);}catch(Exception e){}
			Controller[] ca = ce.getControllers();
			net.java.games.input.Event event=new net.java.games.input.Event();
			for(Controller cai:ca)
				{
				if(cai.getType()==Controller.Type.GAMEPAD || cai.getType()==Controller.Type.STICK)
					{
					cai.poll();
					net.java.games.input.EventQueue queue=cai.getEventQueue();

					//Button or axis position changed. Will really include analog axis even if it makes no sense
					//right now
					while(queue.getNextEvent(event))
						{
						net.java.games.input.Component component=event.getComponent();
						float dz=component.getDeadZone();
						//May need override
						
						float value=event.getValue();
						
						String name=component.getName();
						
						System.out.println(name+" value "+value+" dz "+dz);
						}

					
					//Need to update about every axis every time
					//Can optimize: when no axis at all flipped, don't do this
					NewBinding.EvBindStatus status=new NewBinding.EvBindStatus();
					for(Component component:cai.getComponents())
						{
						float v=component.getPollData();
						//might be thresholding too early
						if(component.isAnalog())
							{
							float dz=component.getDeadZone();
							dz=0.08f;
							if(Math.abs(v)<dz)
								v=0;
							}
						
						status.values.put(component.getName(),v);
						}
					for(NewBinding.EvBindListener listener:NewBinding.bindListeners)
						{
						
						
						listener.bindAxisPerformed(status);
						}
					
					
					break; //For now
					}
				}
			/*
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
			 */

			}

		}

	}
