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
			//System.out.println("Component Count: "+components.length);
			for(int j=0;j<components.length;j++)
				{
	//			System.out.println("Component "+j+": "+components[j].getName());
				System.out.println("    Identifier: "+components[j].getIdentifier().getName());
		//		System.out.print("    ComponentType: ");
				/*if (components[j].isRelative())
					System.out.print("Relative");
				else 
					System.out.print("Absolute");
				if (components[j].isAnalog())
					System.out.print(" Analog");
				else 
					System.out.print(" Digital");*/
	//			System.out.println();
				}
	//		System.out.println("---------------------------------");
			}
	
	
	//"Trigger","Thumb","Thumb 2","Top","Top 2","Pinkie","Base","Base 2","Base 3","Base 4","Base 5","Base 6","pov"
		
	
		}
		
	
	//in steps of 0.125
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
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		NewBinding.EvBindStatus status=new NewBinding.EvBindStatus();
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
						
						//status.values.put(component.getName(),value);
						}

					
					//Need to update about every axis every time
					//Can optimize: when no axis at all flipped, don't do this
					for(Component component:cai.getComponents())
						{
						float v=component.getPollData();
						//might be thresholding too early
						if(component.isAnalog())
							{
							//v*=1.0f/0.003f;
							float dz=component.getDeadZone();
							dz=0.08f;
							if(Math.abs(v)<dz)
								v=0;
							}
						
						status.values.put(component.getName(),v);
						}
					for(NewBinding.EvBindListener listener:NewBinding.bindListeners.keySet())
						listener.bindAxisPerformed(status);
					
					
					break; //For now
					}
				}
			

			}

		}

	}
