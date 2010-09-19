package endrov.mesh3dio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.vecmath.Vector3d;

import endrov.data.EvData;
import endrov.data.EvDataSupport;
import endrov.data.EvIOData;
import endrov.data.RecentReference;
import endrov.data.EvData.FileIOStatusCallback;
import endrov.imagesetBD.EvIODataBD;
import endrov.mesh3d.Mesh3D;
import endrov.util.Tuple;

/**
 * Support for the Wavefront OBJ fileformat
 * 
 * @author Johan Henriksson
 *
 */
public class WavefrontIO implements EvIOData
	{
	
	
	public static Mesh3D readFile(File f) throws IOException
		{
		//All indices in this file format start from 1. Therefore subtract by 1
		Mesh3D mesh=new Mesh3D();
		BufferedReader br=new BufferedReader(new FileReader(f));
		
		List<Vector3d> vertex=new LinkedList<Vector3d>();
		List<Vector3d> normal=new LinkedList<Vector3d>();
		List<Vector3d> texcoord=new LinkedList<Vector3d>();
		
		String currentGroup=null;
		Integer currentSmooth=null;
		
		//Set<Mesh3D.Face> groupFaces=new HashSet<Mesh3D.Face>(); 
		
		
		String line;
		while((line=br.readLine())!=null)
			{
			if(line.startsWith("#"))
				; //Comment
			else if(line.startsWith("f "))
				{
				//Face
				StringTokenizer st=new StringTokenizer(line," ");
				st.nextElement();

				Mesh3D.Face face=new Mesh3D.Face();
				face.vertex=new int[3];
				int[] faceTC=new int[3];
				int[] faceN=new int[3];
				boolean hasTC=false;
				boolean hasN=false;
				for(int i=0;i<3;i++)
					{
					//Formats: v    v/vt/vn    v//vn
					
					StringTokenizer tok2=new StringTokenizer(st.nextToken(),"/");
					face.vertex[i]=Integer.parseInt(tok2.nextToken())-1;
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
				if(hasTC)
					face.texcoord=faceTC;
				if(hasN)
					face.normal=faceN;
				
				face.group=currentGroup;
				face.smooth=currentSmooth;
				
				
				mesh.faces.add(face);
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
		
		
		return mesh;
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
		
		Mesh3D m=readFile(file);
		d.metaObject.put("model", m);
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
				d.io=new EvIODataBD(d, new File(file));
				return d;
				}
			public Integer saveSupports(String file){return null;}
			public List<Tuple<String,String[]>> getSaveFormats(){return new LinkedList<Tuple<String,String[]>>();};
			public EvIOData getSaver(EvData d, String file) throws IOException{return null;}
		});
		}
	
	
	}
