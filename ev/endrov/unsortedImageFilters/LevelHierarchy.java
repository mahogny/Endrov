package endrov.unsortedImageFilters;

import java.io.File;
import java.util.*;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.util.EvDecimal;
import endrov.util.Vector3i;

/**
 * Alternative representation of an image. The image is a tree. Every pixel belongs to one node.
 * Every subnode contains pixels of higher intensity that are surrounded by pixels in the current node.
 *  
 * 
 * @author Johan Henriksson
 *
 */
public class LevelHierarchy
	{
	
	public static class Node
		{
		public int intensity;
		public HashSet<Vector3i> pixels=new HashSet<Vector3i>();
		public Node parent;
		public HashSet<Node> children=new HashSet<Node>();
		
		public String toString()
			{
			return pixels.size()+" { "+children+" }";
			}
		}
	
	public Node root;
	
/*
	public static class SpecialPartition
		{
		HashSet<Vector3i> members=new HashSet<Vector3i>(); 
		}*/
	
	
	
	
	/**
	 * Form level hierarchy. O(#level log(#level) + w h d )
	 */
	public LevelHierarchy(EvStack stack)
		{
		/* Take all pixels
		 * Sort pixels
		 * From the highest intensities:
		 * 	For one level of pixels
		 * 		For all pixels
		 * 			Add to partitioning
		 * 			Check if neighbour in a partition (all directions have to be checked now!)
		 * 			if so, either put in the same level or level above
		 * 			
		 * 					
		 *
		 * 
		 * 
		 * Speed-ups: Can store a hashmap::intensity -> list. sort levels later. images with few levels will be sorted much faster I suspect.
		 * also easier to iterate over levels
		 * 
		 */
		
		/**
		 * TODO all pixels in a stack should have the same dimensions! this allows w & h to go into stack. 
		 */
		
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();
		Node[][][] pixelNode=new Node[d][h][w]; 
		
		
		
		TreeMap<Integer, LinkedList<Vector3i>> pixels=getSortedPixelList(stack, true);
		
		for(Map.Entry<Integer, LinkedList<Vector3i>> level:pixels.entrySet())
			{
			int thisIntensity=level.getKey();
			System.out.println(thisIntensity);
			
			for(Vector3i v:level.getValue())
				{
				Node thisNode=null;
				
				if(v.x+1<w)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x+1, v.y,   v.z);
				if(v.x>0)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x-1, v.y,   v.z);
				if(v.y+1<h)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x,   v.y+1, v.z);
				if(v.y>0)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x,   v.y-1, v.z);
				if(v.z+1<d)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x,   v.y,   v.z+1);
				if(v.z>0)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x,   v.y,   v.z-1);

				//Alone so far. No neighbours. Create a new Node
				if(thisNode==null)
					{
					thisNode=new Node();
					thisNode.intensity=level.getKey();
					thisNode.pixels.add(v);
					}

				//System.out.println(thisNode);
				
				pixelNode[v.z][v.y][v.x]=thisNode;
				}

			
			}
		
		
		HashSet<Node> nodes=new HashSet<Node>();
		for(int z=0;z<d;z++)
			for(int y=0;y<h;y++)
				for(int x=0;x<w;x++)
					nodes.add(pixelNode[z][y][x]);
		//System.out.println(nodes);
		System.out.println(nodes.size());
		
		root=pixelNode[0][0][0];
		
		
		}
	
	
	private Node testOneNeigh(Node thisNode, Integer thisIntensity, Node[][][] pixelNode, Vector3i v, int x2, int y2, int z2)
		{
		//Test in all directions
		Node neighNode=pixelNode[z2][y2][x2];
		if(neighNode!=null)
			{
			if(neighNode.intensity==thisIntensity)
				{
				//Same level
				if(thisNode==null)
					{
					//Add pixel to node
					neighNode.pixels.add(v);
					thisNode=neighNode;
					}
				else
					{
					//Need to join nodes. Eliminate thisNode.
					neighNode.pixels.addAll(thisNode.pixels);
					neighNode.children.addAll(thisNode.children);
					for(Vector3i u:thisNode.pixels)
						pixelNode[u.z][u.y][u.x]=neighNode;
					thisNode=neighNode;
					}
				}
			else
				{
				//Neighbour must be a higher intensity, hence a child of this pixel.
				
				//Which is the highest node containing pixel?
				while(neighNode.parent!=null)
					neighNode=neighNode.parent;
				
				//Check if area is on the same level
				if(neighNode.intensity==thisIntensity)
					{
					//Add this pixel to node
					neighNode.pixels.add(v);
					thisNode=neighNode;
					}
				else
					{
					//Add area as child. Create a node for this pixel if needed.
					if(thisNode==null)
						{
						thisNode=new Node();
						thisNode.intensity=thisIntensity;
						thisNode.pixels.add(v);
						}
					thisNode.children.add(neighNode);
					}
				}
			}
		return thisNode;
		}
	
	/**
	 * 
	 * 
	 * TODO java 1.6 has .descendingMap(), which could replace reverse. 
	 */
	public static TreeMap<Integer, LinkedList<Vector3i>> getSortedPixelList(EvStack stack, boolean reverse)
		{
		HashMap<Integer, LinkedList<Vector3i>> pixels=new HashMap<Integer, LinkedList<Vector3i>>();
		
		EvPixels[] parr=stack.getPixels();
		
		for(int z=0;z<parr.length;z++)
			{
			EvPixels p=parr[z].convertTo(EvPixels.TYPE_INT, true);
			int[] arr=p.getArrayInt();
			for(int y=0;y<p.getHeight();y++)
				for(int x=0;x<p.getWidth();x++)
					{
					int value=arr[p.getPixelIndex(x, y)];
					LinkedList<Vector3i> list=pixels.get(value);
					if(list==null)
						pixels.put(value,list=new LinkedList<Vector3i>());
					list.add(new Vector3i(x,y,z));
					}
			}
		
		
		
		TreeMap<Integer, LinkedList<Vector3i>> sortedPixels=reverse ?
				new TreeMap<Integer, LinkedList<Vector3i>>(Collections.reverseOrder())
				: new TreeMap<Integer, LinkedList<Vector3i>>();
		sortedPixels.putAll(pixels);
		return sortedPixels;
		}
	
	
	
	
	
	public void _check()
		{
		int count=_check(root,0);
		System.out.println("tot num pixels "+count);
		}
	public int _check(Node node, int count)
		{
		int sum=node.pixels.size();
		System.out.println("size here "+sum);
		for(Node c:node.children)
			{
			if(c.intensity<=node.intensity)
				System.out.println("intensity order error");
			sum+=_check(c,count++);
			}
		if(node.children.isEmpty())
			System.out.println("Leaf depth "+count);
		return sum;
		}
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		
		EvData data=EvData.loadFile(new File("/Volumes/TBU_main03/ost4dgood/TB2167_080416.ost"));
		
		Imageset im=data.getIdObjectsRecursive(Imageset.class).values().iterator().next();
		
		EvStack stack=im.getChannel("RFP").imageLoader.get(new EvDecimal(14050));
		stack.keySet().remove(new EvDecimal("1"));
		stack.keySet().remove(new EvDecimal("2.5"));
		stack.keySet().remove(new EvDecimal("4"));
		stack.keySet().remove(new EvDecimal("5.5"));
		
		stack.keySet().remove(new EvDecimal("31"));		
		stack.keySet().remove(new EvDecimal("32.5"));
		stack.keySet().remove(new EvDecimal("34"));
		System.out.println(stack.keySet());
		
		LevelHierarchy hi=new LevelHierarchy(stack);
		
		hi._check();
		System.out.println(hi.root);
		
		System.exit(0);
		}
	
	
	
	}
