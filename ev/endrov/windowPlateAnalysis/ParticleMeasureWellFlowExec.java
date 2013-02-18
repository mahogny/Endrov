package endrov.windowPlateAnalysis;

import endrov.core.log.EvLog;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.flow.FlowExec;
import endrov.flow.FlowExecListener;
import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.util.math.EvDecimal;
import endrov.util.mutable.Mutable;

/**
 * Calculating parameters for a well using a flow
 * 
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureWellFlowExec
	{
	
	/**
	 * Put a PM from evaluating a well into a total PM
	 */
	public static void mergeWellPM(final ParticleMeasure totalPM, final ParticleMeasure wellPM, final EvPath pathToWell)
		{
		ParticleMeasure.Well well=wellPM.getWell("");
		if(well==null)
			throw new RuntimeException("NULL WELL");
		
		//Force the evaluation of this data
		for(EvDecimal frame:well.getFrames())
			{
			//TODO only curframe! or closest frame. ...or?
			well.getFrame(frame).size(); //This is sufficient
			}
		
		//TODO: check that this output exists!
		
		for(EvDecimal frame:well.getFrames())
			System.out.println("Got flow frame: "+frame+"   #particles: "+well.getFrame(frame).size());
		
		//Merge data into current pm
		totalPM.setWell(pathToWell.toString(), well);
		for(String s:wellPM.getColumns())
			totalPM.addColumn(s);
		}
	
	
	/**
	 * Execute a flow on this well
	 */
	public static ParticleMeasure execFlowOnWell(final EvPath pathToWell, final EvPath pathToFlow) throws Exception
		{
		final Mutable<ParticleMeasure> gotPM=new Mutable<ParticleMeasure>();
		
		EvData data=(EvData)pathToFlow.getRoot();
		
		System.out.println("start flow");
		FlowExec fexec=new FlowExec(data, pathToFlow);
		fexec.listener=new FlowExecListener()
			{
			public void setOutputObject(String name, Object ob)
				{
				if(name.equals("pm"))
					{
					ParticleMeasure thispm=(ParticleMeasure)ob;

					gotPM.setValue(thispm);
					
					}
				else
					EvLog.printLog("Warning: unused output");
				}
			
			public Object getInputObject(String name)
				{
				if(name.equals("well"))
					{
					System.out.println("sending well "+pathToWell);
					return pathToWell.getObject();
					}
				else
					{
					throw new RuntimeException("Error, flow requested non-existing input "+name);
					}
				}
			};
		
			

			

			
			
			
		try
			{
			System.out.println("pre-eval");
			fexec.evaluateAll();
			System.out.println("eval!");
			
			ParticleMeasure pm=gotPM.get();
			if(pm==null)
				throw new Exception("Flow did not output a ParticleMeasure");

			return pm;
			
			
			}
		catch (Throwable e)
			{
			EvLog.printError(e);
			throw new Exception("Error running flow");
			}
		
		}


	}
