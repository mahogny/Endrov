package endrov.mesh3d;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.EvObject;

public class Mesh3D extends EvObject
	{

	public static class Face
		{
		//Always 3 points!
		public int vertex[];
		public int texcoord[];
		public int normal[];
		
		public Integer smooth;
		public String group; //Note. Same string, must be possible to compare with ==
		}
	
	public List<Face> faces=new LinkedList<Face>();
	public List<Vector3d> vertex=new ArrayList<Vector3d>();
	public List<Vector3d> texcoord=new ArrayList<Vector3d>();
	public List<Vector3d> normal=new ArrayList<Vector3d>();
	
	
	@Override
	public void buildMetamenu(JMenu menu)
		{
		}
	@Override
	public String getMetaTypeDesc()
		{
		return "Mesh";
		}
	@Override
	public void loadMetadata(Element e)
		{
		//TODO
		}
	@Override
	public String saveMetadata(Element e)
		{
		//TODO
		return null;
		}
	
	
	}
