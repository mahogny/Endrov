package endrov.mesh3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.modelWindow.gl.GLMaterial;
import endrov.particle.Lineage;

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
		
		public Integer smoothGroup;
//		public String group; //Note. Same string, must be possible to compare with ==
		}
	
	public List<Face> faces=new ArrayList<Face>();
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
			Vector3d v01=new Vector3d(vertex.get(f.vertex[1]));
			Vector3d v02=new Vector3d(vertex.get(f.vertex[2]));
			Vector3d v0=vertex.get(f.vertex[0]);
			v01.sub(v0);
			v02.sub(v0);
			
			Vector3d cross=new Vector3d();
			cross.cross(v01, v02);  //TODO which one?
			cross.normalize();
			
			fnormal.put(f,cross);
			}
		
		//Build map: vertex -> [faces]
		Map<Integer, List<Face>> vertFaces=new HashMap<Integer, List<Face>>();
		for(Face f:faces)
			for(int vi=0;vi<3;vi++)
				{
				List<Face> loclist=vertFaces.get(f.vertex[vi]);
				if(loclist==null)
					vertFaces.put(f.vertex[vi], loclist=new LinkedList<Face>());
				loclist.add(f);
				}

		//Build normals for each face
		for(Face f:faces)
			{
			if(f.smoothGroup==null)
				{
				//All faces have the same normals
				int nid=normal.size();
				normal.add(fnormal.get(f));
				f.normal=new int[3];
				for(int i=0;i<3;i++)
					f.normal[i]=nid;
				}
			else
				{
				//Interpolate each normal
				f.normal=new int[3];
				for(int vi=0;vi<3;vi++)
					{
					Vector3d n=new Vector3d();
					for(Face of:vertFaces.get(f.vertex[vi]))
						if(f.smoothGroup.equals(of.smoothGroup)) //handle null? this should also include this face
							n.add(fnormal.get(of));
					n.normalize();
					int nid=normal.size();
					normal.add(n);
					f.normal[vi]=nid;
					}
				}
			
			}
		
		}
	
	
	

	/**
	 * Make all faces smooth together
	 */
	public void makeAllFacesSmooth()
		{
		for(Face f:faces)
			f.smoothGroup=0;
		
		/*  Below gives different groups to unconnected parts. Not really beneficial
		//Which vertex has which faces?
		Map<Integer, Set<Face>> vertToFaces=new HashMap<Integer, Set<Face>>();
		for(Face f:faces)
			{
			for(int i=0;i<3;i++)
				{
				int vid=f.vertex[i];
				Set<Face> vf=vertToFaces.get(vid);
				if(vf==null)
					vertToFaces.put(vid, vf=new HashSet<Face>());
				vf.add(f);
				}
			}

		//Smooth groups make up equivalence sets! Find out which these are
		Partitioning<Face> smoothPartitions=new Partitioning<Face>();
		for(int fi=0;fi<faces.size();fi++)
			for(int fj=fi+1;fj<faces.size();fj++)
				{
				Face fa=faces.get(fi);
				Face fb=faces.get(fj);
				
				//Faces share an edge if exactly 2 vertexes are in common
				Set<Integer> vids=new HashSet<Integer>();
				for(int v:fa.vertex)
					vids.add(v);
				int countVert=0;
				for(int v:fb.vertex)
					if(vids.contains(v))
						countVert++;
				if(countVert==2)
					{
					//These two belong together
					smoothPartitions.createSpecifyEquivalent(fa, fb);
					}
				else
					{
					//These two do not belong together. Create separate
					smoothPartitions.createElement(fa);
					smoothPartitions.createElement(fb);
					}
				}
		
		//Assign smooth groups
		int curSmoothGroup=1;
		for(Set<Face> faces:smoothPartitions.getPartitions())
			{
			for(Face f:faces)
				f.smoothGroup=curSmoothGroup;
			curSmoothGroup++;
			}
			*/
		
		}
	
	@Override
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
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
	

	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}


	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,Lineage.class);
		
		}

	}
