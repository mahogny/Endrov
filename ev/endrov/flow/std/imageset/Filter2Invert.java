package endrov.flow.std.imageset;

import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.roi.ROI;
import endrov.util.EvDecimal;
import endrov.util.Maybe;

/**
 * Filter: invert
 * @author Johan Henriksson
 *
 */
public class Filter2Invert extends FlowUnitFilter2
	{
	private static final String metaType="filterInvert";

	public String getBasicShowName(){return "Invert";}
	public ImageIcon getIcon(){return null;}
	
	//Most of this code should be moved out
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		//possibility of concatenation here, if the right type is sent. a wrapper.
		//this can also be done on the level of individual EvImage but less abstraction possible
		
		
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Maybe<?> mroi=flow.getInputValueMaybe(this, exec, nameInROI);
		ROI roi=null;
		if(mroi.hasValue())
			roi=(ROI)mroi.get();
		
		Imageset imin=(Imageset)flow.getInputValue(this, exec, nameInImageset);
		
		Imageset imout=new Imageset();
		
		if(roi!=null)
			{
			//Apply to subset, send the rest unchanged
			
			
			//TODO do for ROI
			
			
			
			}
		else
			{
			
			//Can provide a ForAll ROI to reuse code
			
			for(Map.Entry<String, EvChannel> che:imin.getChannels().entrySet())
				{
//				public TreeMap<EvDecimal, TreeMap<EvDecimal, EvImage>> imageLoader=new TreeMap<EvDecimal, TreeMap<EvDecimal, EvImage>>();

				EvChannel chout=imout.getCreateChannel(che.getKey());
				
				for(Map.Entry<EvDecimal, EvStack> fe:che.getValue().imageLoader.entrySet())
					{
					
					EvStack fout=chout.getCreateFrame(fe.getKey());
					
					for(Map.Entry<EvDecimal, EvImage> se:fe.getValue().entrySet())
						{
						//EvImage evimin=se.getValue();
						
						
						
						EvImage evimout=new EvImage();

						
						
						fout.put(se.getKey(), evimout);
						
						
						}
					
					
					}
			
				
				}
			
			
			}
		
		//having frames/stacks would allow lazier generation of indices. but it is not good as a trigger
		//for computation since this might be separate from just checking availability
		
		
		
		//What to do with metadata?
		//more data should be assigned to the images to have it trivially copied
		//should images 
		
		//where to flag that data has been generated? need to change this back if modified
		
		
		lastOutput.put(nameOutImageset, imout);
		
		}

	public void fromXML(Element e)
		{
		}
	public String toXML(Element e)
		{
		return metaType;
		}

	
	
	
	}
