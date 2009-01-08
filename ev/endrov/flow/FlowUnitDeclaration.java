package endrov.flow;

import javax.swing.ImageIcon;

/**
 * One flow unit type
 * @author Johan Henriksson
 *
 */
public class FlowUnitDeclaration
	{
	public final String name, category, metadata;
	public final ImageIcon icon;
	private final Class<? extends FlowUnit> c;
	public FlowUnitDeclaration(String category, String name, String metadata,Class<? extends FlowUnit> c, ImageIcon icon)
		{
		this.name=name;
		this.category=category;
		this.metadata=metadata;
		this.c=c;
		this.icon=icon;
		}
	

	
	public FlowUnit createInstance()
		{
		try
			{
			return c.newInstance();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("this should not happen");
		return null; //wtf
		}
	
	public String toString()
		{
		return name;
		}
	}
