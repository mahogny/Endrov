package endrov.mesh3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.DataConversionException;
import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.modelWindow.BoundingBox;
import endrov.modelWindow.gl.EvGLMaterial;
import endrov.modelWindow.gl.EvGLMaterialSolid;

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
		
		public EvGLMaterial material;
		}
	
	public List<Face> faces=new ArrayList<Face>();
	public List<Vector3d> vertex=new ArrayList<Vector3d>();
	public List<Vector3d> texcoord=new ArrayList<Vector3d>();
	public List<Vector3d> normal=new ArrayList<Vector3d>();
	
	
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
		
		try
			{
			HashMap<Integer,EvGLMaterial> materials=new HashMap<Integer, EvGLMaterial>();
			
			
			for(Object o:e.getChildren())
				{
				Element ne=(Element)o;
				
				if(ne.getName().equals("m"))
					{
					int id=ne.getAttribute("id").getIntValue();
					
					Element me=(Element)ne.getChildren().iterator().next();
					EvGLMaterialSolid m=EvGLMaterialSolid.fromXML(me);
					
					materials.put(id, m);
					}
				else if(ne.getName().equals("f"))
					{
					Face f=new Face();
					faces.add(f);
					
					if(ne.getAttribute("v")!=null)
						f.vertex=indexListToArr(ne.getAttributeValue("v"));
					/*
						{
						int[] arr=f.vertex=new int[3];
						for(int i=0;i<3;i++)
							arr[i]=ne.getAttribute("v"+i).getIntValue();
						}
						*/
					
					if(ne.getAttribute("t")!=null)
						f.texcoord=indexListToArr(ne.getAttributeValue("t"));
					/*
						{
						int[] arr=f.texcoord=new int[3];
						for(int i=0;i<3;i++)
							arr[i]=ne.getAttribute("t"+i).getIntValue();
						}*/

					if(ne.getAttribute("n")!=null)
						f.normal=indexListToArr(ne.getAttributeValue("n"));
						/*
						{
						int[] arr=f.normal=new int[3];
						for(int i=0;i<3;i++)
							arr[i]=ne.getAttribute("n"+i).getIntValue();
						}*/
					
					if(ne.getAttribute("smoothg")!=null)
						f.smoothGroup=ne.getAttribute("smoothg").getIntValue();
					
					if(ne.getAttribute("mat")!=null)
						{
						int id=ne.getAttribute("mat").getIntValue();
						f.material=materials.get(id);
						}
					}
				else if(ne.getName().equals("v"))
					vertex=string2vector(ne.getText());
					//vertex.add(xmlToVertex(ne));
				else if(ne.getName().equals("t"))
					texcoord=string2vector(ne.getText());
//					texcoord.add(xmlToVertex(ne));
				else if(ne.getName().equals("n"))
					normal=string2vector(ne.getText());
	//				normal.add(xmlToVertex(ne));
				
				}
			}
		catch (DataConversionException e1)
			{
			e1.printStackTrace();
			}
		}
	
	
	@Override
	public String saveMetadata(Element e)
		{
		//Store materials
		Set<EvGLMaterial> materials=new HashSet<EvGLMaterial>();
		for(Face f:faces)
			if(f.material!=null)
				materials.add(f.material);
		int matCount=0;
		HashMap<EvGLMaterial,Integer> materialToID=new HashMap<EvGLMaterial, Integer>();
		for(EvGLMaterial m:materials)
			{
			if(m instanceof EvGLMaterialSolid)
				{
				int id=matCount;
				matCount++;
				
				materialToID.put(m, id);
				
				Element ne=new Element("m");
				ne.setAttribute("id",""+materialToID.get(m));

				//TODO abstract interface?
				EvGLMaterialSolid sm=(EvGLMaterialSolid)m;
				Element me=sm.toXML();
				ne.addContent(me);
				
				e.addContent(ne);
				}
			
			}
		
		//Store faces
		for(Face f:faces)
			{
			Element ne=new Element("f");
			ne.setAttribute("v",arrayToIndexList(f.vertex));//
//			for(int i=0;i<3;i++)
	//			ne.setAttribute("v"+i,Integer.toString(f.vertex[i]));
			if(f.texcoord!=null)
				ne.setAttribute("t",arrayToIndexList(f.texcoord));//
//				for(int i=0;i<3;i++)
	//				ne.setAttribute("t"+i,Integer.toString(f.texcoord[i]));
			if(f.normal!=null)
				ne.setAttribute("n",arrayToIndexList(f.normal));//

//				for(int i=0;i<3;i++)
	//				ne.setAttribute("n"+i,Integer.toString(f.normal[i]));

			if(f.smoothGroup!=null)
				ne.setAttribute("smoothg",Integer.toString(f.smoothGroup));
			
			if(f.material!=null)
				{
				Integer id=materialToID.get(f.material);
				if(id!=null)
					ne.setAttribute("mat",Integer.toString(id));
				}
			
			e.addContent(ne);
			}
		
		Element neV=new Element("v");
		neV.setText(vectors2string(vertex));
		e.addContent(neV);

		Element neT=new Element("t");
		neT.setText(vectors2string(texcoord));
		e.addContent(neT);
		
		Element neN=new Element("n");
		neN.setText(vectors2string(normal));
		e.addContent(neN);
		
		/*
		for(Vector3d v:vertex)
			{
			vertexToXML(v, neV);
			e.addContent(neV);
			}
		//System.out.println("t");
		for(Vector3d v:texcoord)
			{
			Element ne=new Element("t");
			vertexToXML(v, ne);
			e.addContent(ne);
			}
		//System.out.println("n");
		//TODO store the ability recalc normals later
		for(Vector3d v:normal)
			{
			Element ne=new Element("n");
			vertexToXML(v, ne);
			e.addContent(ne);
			}
		*/
		return metaType;
		}
	
	
	private static String vectors2string(List<Vector3d> vs)
		{
		//TODO could cut down on decimals!
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<vs.size();i++)
			{
			Vector3d v=vs.get(i);
			sb.append(v.x);
			sb.append(",");
			sb.append(v.y);
			sb.append(",");
			sb.append(v.z);
			if(i!=vs.size()-1)
				sb.append(",");
			}
		return sb.toString();
		}
	
	private static List<Vector3d> string2vector(String s)
		{
		List<Vector3d> list=new ArrayList<Vector3d>();
		StringTokenizer st=new StringTokenizer(s,",");
		while(st.hasMoreTokens())
			{
			Vector3d v=new Vector3d(
					Double.parseDouble(st.nextToken()),
					Double.parseDouble(st.nextToken()),
					Double.parseDouble(st.nextToken()));
			list.add(v);
			}
		return list;
		}
		
	
	private static String arrayToIndexList(int[] arr)
		{
		return arr[0]+","+arr[1]+","+arr[2];
		}

	private static int[] indexListToArr(String s)
		{
		int[] arr=new int[3];
		StringTokenizer stok=new StringTokenizer(s,",");
		for(int i=0;i<3;i++)
			arr[i]=Integer.parseInt(stok.nextToken());
		return arr;
		}

	/*
	private static void vertexToXML(Vector3d v, Element e)
		{
		e.setAttribute("x", Double.toString(v.x));
		e.setAttribute("y", Double.toString(v.y));
		e.setAttribute("z", Double.toString(v.z));
		}
	private static Vector3d xmlToVertex(Element e) throws DataConversionException
		{
		return new Vector3d(
				e.getAttribute("x").getDoubleValue(),
				e.getAttribute("y").getDoubleValue(),
				e.getAttribute("z").getDoubleValue());
		}
	*/

	/**
	 * Remove unused vertices
	 */
	public void pruneUnusedVertices()
		{
		Set<Integer> used=new HashSet<Integer>();
		
		for(Face f:faces)
			for(int i:f.vertex)
				used.add(i);

		if(used.size()!=vertex.size())
			{
			//New IDs & compactify
			Map<Integer, Integer> lastToNewId=new HashMap<Integer, Integer>();
			List<Vector3d> newlist=new ArrayList<Vector3d>();
			int count=0;
			for(int oldId:used)
				{
				lastToNewId.put(oldId, count++);
				newlist.add(vertex.get(oldId));
				}
			vertex=newlist;
			
			//Remap 
			for(Face f:faces)
				for(int i=0;i<3;i++)
					f.vertex[i]=lastToNewId.get(f.vertex[i]);
			}
		}

	
	public void pruneUnusedTexcoord()
		{
		// TODO Auto-generated method stub
		
		}




	public void pruneUnusedNormals()
		{
		// TODO Auto-generated method stub
		
		}
	

	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}

	
	/**
	 * Get average position of all vertices
	 */
	public Vector3d getVertexAverage()
		{
		if(vertex.isEmpty())
			return null;
		else
			{
			double meanx=0, meany=0, meanz=0;
			int num=0;
			for(Vector3d pos:vertex)
				{
				meanx+=pos.x;
				meany+=pos.y;
				meanz+=pos.z;
				}
			num+=vertex.size();

			meanx/=num;
			meany/=num;
			meanz/=num;
			return new Vector3d(meanx,meany,meanz);
			}
		}
	
	public BoundingBox getBoundingBox()
		{
		BoundingBox bb=new BoundingBox();
		for(Vector3d v:vertex)
			bb.addPoint(v.x, v.y, v.z);
		return bb;
		}


	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,Mesh3D.class);
		
		}
	

	}
