package endrov.mesh3d;

import endrov.basicWindow.EvColor;
import endrov.data.EvSelection.EvSelectable;

public class SelMesh3D implements EvSelectable
	{
	static final long serialVersionUID=0;
	
	private Mesh3D mesh;
	
	public SelMesh3D(Mesh3D mesh)
		{
		this.mesh=mesh;
		}
	
	/*
	public int hashCode()
		{
		//needed? don't think so
		return super.hashCode();
		}*/
	
	
	public void setColor(EvColor c)
		{
		//TODO
		
		
		//getParticle().color=c.getAWTColor();
		}
	
	protected SelMesh3D clone()
		{
		return new SelMesh3D(mesh);
		}
	}
