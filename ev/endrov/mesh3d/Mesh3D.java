package endrov.mesh3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.modelWindow.gl.GLMaterial;
import endrov.nuc.NucLineage;

/**
 * 3D meshes
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class Mesh3D extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="mesh3d";



	public static Mesh3D generateTestModel()	
		{
		Mesh3D m=new Mesh3D();
		Face f=new Face();
		f.vertex=new int[3];
		f.vertex[0]=0;
		f.vertex[1]=1;
		f.vertex[2]=2;
		m.faces.add(f);
		
		m.vertex.add(new Vector3d(0,0,0));
		m.vertex.add(new Vector3d(0,1,0));
		m.vertex.add(new Vector3d(1,0,0));
		return m;
		}


	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public static class Face
		{
		//Always 3 points! vertex is not-null, the other two can be null
		public int vertex[];
		public int texcoord[];
		public int normal[];
		
		public Integer smooth;
//		public String group; //Note. Same string, must be possible to compare with ==
		}
	
	public List<Face> faces=new LinkedList<Face>();
	public List<Vector3d> vertex=new ArrayList<Vector3d>();
	public List<Vector3d> texcoord=new ArrayList<Vector3d>();
	public List<Vector3d> normal=new ArrayList<Vector3d>();
	
	public GLMaterial material;
	
	public void calcNormals()
		{
		normal.clear();
		
		//Calculate normal for each face
		Map<Face,Vector3d> fnormal=new HashMap<Face, Vector3d>(); 
		for(Face f:faces)
			{
			//System.out.println(vertex);
			//System.out.println(f.vertex[0]+"  "+f.vertex[1]+"  "+f.vertex[2]);
			
			Vector3d v01=new Vector3d(vertex.get(f.vertex[1]));
			Vector3d v02=new Vector3d(vertex.get(f.vertex[2]));
			Vector3d v0=vertex.get(f.vertex[0]);
			v01.sub(v0);
			v02.sub(v0);
			
			Vector3d cross=new Vector3d();
			cross.cross(v01, v02);  //TODO which one?
			cross.normalize();
			
			fnormal.put(f,cross);
			
			
			//For now, ignore smoothing groups. Just make a new normal
			int nid=normal.size();
			normal.add(cross);
			f.normal=new int[3];
			for(int i=0;i<3;i++)
				f.normal[i]=nid;
			}
		
		}
	
	
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
	
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,NucLineage.class);
		
		}

	}
