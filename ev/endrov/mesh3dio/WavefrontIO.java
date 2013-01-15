package endrov.mesh3dio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.vecmath.Vector3d;

import endrov.data.EvData;
import endrov.data.EvDataSupport;
import endrov.data.EvIOData;
import endrov.data.RecentReference;
import endrov.data.EvData.FileIOStatusCallback;
import endrov.ev.EvLog;
import endrov.mesh3d.Mesh3D;
import endrov.modelWindow.gl.EvGLMaterial;
import endrov.modelWindow.gl.EvGLMaterialSolid;
import endrov.util.Tuple;

/**
 * Support for the Wavefront OBJ fileformat
 * 
 * @author Johan Henriksson
 *
 */
public class WavefrontIO implements EvIOData
	{
	
	/**
	 * Wavefront material definition
	 *
	 */
	public static class Material
		{
		float[] ambient;
		float[] diffuse;
		float[] specular;
		double specularWeight;
		double transparency=1;
		
		//There are reflection modes as well, see http://en.wikipedia.org/wiki/Wavefront_.obj_file
		
		public EvGLMaterial toGLmaterial()
			{
			//What about transparency?
			EvGLMaterial m=new EvGLMaterialSolid(diffuse, specular, ambient, (float)specularWeight);
			return m;
			}
		}
	
	//Map<String, Material> materials=new HashMap<String, Material>();
	
	
	public static void readMaterials(File f, Map<String, EvGLMaterial> glmaterials) throws IOException
		{
		Material current=null;
		BufferedReader br=new BufferedReader(new FileReader(f));
		
		Map<String, Material> materials=new HashMap<String, Material>();
		
		String line;
		while((line=br.readLine())!=null)
			{
			if(line.startsWith("#"))
				; //Comment
			else if(line.startsWith("newmtl "))
				{
				String mname=line.substring("newmtl ".length());
				Material m=current=new Material();
				materials.put(mname, m);
				}
			else if(line.startsWith("Ka "))
				{
				StringTokenizer st=new StringTokenizer(line," ");
				st.nextElement();
				float[] arr=current.ambient=new float[4];
				for(int i=0;i<3;i++)
					arr[i]=(float)Double.parseDouble(st.nextToken());
				arr[3]=1f;
				}
			else if(line.startsWith("Kd "))
				{
				StringTokenizer st=new StringTokenizer(line," ");
				st.nextElement();
				float[] arr=current.diffuse=new float[4];
				for(int i=0;i<3;i++)
					arr[i]=(float)Double.parseDouble(st.nextToken());
				arr[3]=1f;
				}			
			else if(line.startsWith("Ks "))
				{
				StringTokenizer st=new StringTokenizer(line," ");
				st.nextElement();
				float[] arr=current.specular=new float[4];
				for(int i=0;i<3;i++)
					arr[i]=(float)Double.parseDouble(st.nextToken());
				arr[3]=1f;
				}
			else if(line.startsWith("Ns "))
				{
				StringTokenizer st=new StringTokenizer(line," ");
				st.nextElement();
				current.specularWeight=Double.parseDouble(st.nextToken())*128/1000; //With OpenGL-rescaling
				}
			else if(line.startsWith("d ") || line.startsWith("Tr "))
				{
				StringTokenizer st=new StringTokenizer(line," ");
				st.nextElement();
				current.transparency=Double.parseDouble(st.nextToken());
				}
			else if(line.startsWith("illum "))
				{
				//TODO illumination mode
				}			
			}
		
		for(Map.Entry<String, Material> e:materials.entrySet())
			glmaterials.put(e.getKey(), e.getValue().toGLmaterial());
		br.close();
		}
	
	
	public static Map<String,Mesh3D> readFile(File f) throws IOException
		{
		//All indices in this file format start from 1. Therefore subtract by 1
		//Mesh3D mesh=new Mesh3D();
		BufferedReader br=new BufferedReader(new FileReader(f));
		
		List<Vector3d> vertex=new LinkedList<Vector3d>();
		List<Vector3d> normal=new LinkedList<Vector3d>();
		List<Vector3d> texcoord=new LinkedList<Vector3d>();
		
		String currentGroup="model";
		Integer currentSmooth=null;
		
		//Set<Mesh3D.Face> groupFaces=new HashSet<Mesh3D.Face>(); 
		
		Map<String,Mesh3D> meshes=new HashMap<String, Mesh3D>(); 
		Mesh3D currentMesh=null;
		
		Map<String, EvGLMaterial> glmaterials=new HashMap<String, EvGLMaterial>();
		
		EvGLMaterial currentMaterial=new EvGLMaterialSolid();
		
		String line;
		while((line=br.readLine())!=null)
			{
			if(line.startsWith("#"))
				; //Comment
			else if(line.startsWith("mtllib "))
				{
				File mtlfile=new File(f.getParentFile(),line.substring("mtllib ".length()));
				if(mtlfile.exists())
					readMaterials(mtlfile, glmaterials);
				else
					EvLog.printError("Materials file not found, ignoring: "+mtlfile, null);
				}
			else if(line.startsWith("usemtl "))
				{
				String mname=line.substring("usemtl ".length());
				if(glmaterials.containsKey(mname))
					currentMaterial=glmaterials.get(mname);
				else
					EvLog.printError("Material not found, ignoring: "+mname, null);
				}
			else if(line.startsWith("f "))
				{
				//Face
				StringTokenizer st=new StringTokenizer(line," ");
				st.nextElement();

				//TODO support quads!
				
				int[] fvertex=new int[4];
				int[] faceTC=new int[4];
				int[] faceN=new int[4]; //TODO: reduce to 3 elements later?
				
				boolean hasTC=false;
				boolean hasN=false;
				int vertexcount=0;
				for(int i=0;i<4 && st.hasMoreTokens();i++)
					{
					//Formats: v    v/vt/vn    v//vn
					
					StringTokenizer tok2=new StringTokenizer(st.nextToken(),"/");
					fvertex[i]=Integer.parseInt(tok2.nextToken())-1;
					if(tok2.hasMoreElements())
						{
						String svt=tok2.nextToken();
						if(!svt.equals(""))
							{
							faceTC[i]=Integer.parseInt(svt)-1;
							hasTC=true;
							}
						if(tok2.hasMoreElements())
							{
							faceN[i]=Integer.parseInt(tok2.nextToken())-1;
							hasN=true;
							}
						}
					vertexcount++;
					}
				
				
				for(int i=3;i<=vertexcount;i++)
					{
					int current=i-3;

					Mesh3D.Face face=new Mesh3D.Face();
					face.material=currentMaterial;
					face.vertex=take3a(fvertex,current);

					if(hasTC)
						face.texcoord=take3a(faceTC,current);
					if(hasN)
						face.normal=take3a(faceN,current);
					
					face.smoothGroup=currentSmooth;
					
					//Get or create a mesh
					if(currentMesh==null)
						{
						currentMesh=new Mesh3D();
						meshes.put(currentGroup, currentMesh);
						}
					currentMesh.faces.add(face);

					}
				
				
				}
			else if(line.startsWith("v "))
				{
				//Vertex
				StringTokenizer st=new StringTokenizer(line," ");
				st.nextElement();
				double x=Double.parseDouble(st.nextToken());
				double y=Double.parseDouble(st.nextToken());
				double z=Double.parseDouble(st.nextToken());
				vertex.add(new Vector3d(x,y,z));
				}
			else if(line.startsWith("g "))
				{
				//Group
				currentGroup=line.substring(2);
				currentMesh=null;
				}
			else if(line.startsWith("vt "))
				{
				//Texture coordinate
				StringTokenizer st=new StringTokenizer(line," ");
				st.nextElement();
				double x=Double.parseDouble(st.nextToken());
				double y=Double.parseDouble(st.nextToken());
				if(st.hasMoreTokens())
					{
					double z=Double.parseDouble(st.nextToken());
					texcoord.add(new Vector3d(x,y,z));
					}
				else
					texcoord.add(new Vector3d(x,y,0));
				}
			else if(line.startsWith("vn "))
				{
				//Normal
				StringTokenizer st=new StringTokenizer(line," ");
				st.nextElement();
				double x=Double.parseDouble(st.nextToken());
				double y=Double.parseDouble(st.nextToken());
				double z=Double.parseDouble(st.nextToken());
				normal.add(new Vector3d(x,y,z));
				}
			else if(line.startsWith("s "))
				{
				//Smooth group
				String s=line.substring(2);
				if(s.equals("off"))
					currentSmooth=null;
				else
					currentSmooth=Integer.parseInt(s)-1;
				}
				
				
			}
		br.close();
		
		//Linked lists to array lists, faster allocation
		for(Mesh3D m:meshes.values())
			{
			m.vertex.addAll(vertex);
			m.normal.addAll(normal);
			m.texcoord.addAll(texcoord);

			
			//Doubtful if it should be done
			m.makeAllFacesSmooth();
			
			//Ignore the previous normals! Just recalculate. Or should this be done optionally somehow?
			m.calcNormals();
			
			
			m.pruneUnusedVertices();
			m.pruneUnusedTexcoord();
			m.pruneUnusedNormals();
			
			//TODO prune vertices not used for the group
			
			
			
			/*
			//TODO temp?
			EvColor color=EvColor.colorList[(int)Math.floor(Math.random()*EvColor.colorList.length)];
      float diff=0.7f;
      float ambient=0.3f;
      float spec=0.8f;
			m.material=new GLMaterial(
					new float[]{(float)color.getRedDouble()*diff, (float)color.getGreenDouble()*diff, (float)color.getBlueDouble()*diff},
					new float[]{(float)color.getRedDouble()*spec, (float)color.getGreenDouble()*spec, (float)color.getBlueDouble()*spec},
					new float[]{(float)color.getRedDouble()*ambient, (float)color.getGreenDouble()*ambient, (float)color.getBlueDouble()*ambient},
					80);
					*/

			}
				
		return meshes;
		}

	
	private static int[] take3a(int[] arr, int current)
		{
		if(current==0)
			return new int[]{arr[0],arr[1],arr[2]};
		else if(current==1)
			return new int[]{arr[0],arr[2],arr[3]};
		else
			throw new RuntimeException("bad current index");
		}
	
	
	private File file;
	
	public void buildDatabase(EvData d)
		{		
		}

	public File datadir()
		{
		return null;
		}

	public String getMetadataName()
		{
		return file.getName();
		}

	public RecentReference getRecentEntry()
		{
		return new RecentReference(file.getName(), file.getAbsolutePath());
		}

	public void saveData(EvData d, FileIOStatusCallback cb)
		{
		//Not implemented
		}
	
	
	

	/**
	 * Open a new recording
	 */
	public WavefrontIO(EvData d, File basedir) throws Exception
		{
		this.file=basedir;
		if(!basedir.exists())
			throw new Exception("File does not exist");
		
		
		Map<String,Mesh3D> m=readFile(file);

		for(Map.Entry<String, Mesh3D> e:m.entrySet())
			d.metaObject.put(e.getKey(), e.getValue());

		System.out.println("Loaded "+m.size()+" meshs from wavefront file");

		
		//		d.metaObject.put("model", Mesh3D.generateTestModel());
		}
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedFileFormats.add(new EvDataSupport(){
			public Integer loadSupports(String fileS)
				{
				File file=new File(fileS);
				if(file.getName().endsWith(".obj"))
					{
					//There is no header unfortunately
					return 50;
					}
				return null;
				}
			public List<Tuple<String,String[]>> getLoadFormats()
				{
				LinkedList<Tuple<String,String[]>> formats=new LinkedList<Tuple<String,String[]>>();
				formats.add(Tuple.make("Wavefront", new String[]{"obj"}));
				return formats;
				}
			public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception
				{
				EvData d=new EvData();
				d.io=new WavefrontIO(d, new File(file));
				return d;
				}
			public Integer saveSupports(String file){return null;}
			public List<Tuple<String,String[]>> getSaveFormats(){return new LinkedList<Tuple<String,String[]>>();};
			public EvIOData getSaver(EvData d, String file) throws IOException{return null;}
		});
		}
	
	
	}
