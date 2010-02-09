/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.unsortedImageFilters.unfinished;

import java.io.File;
import java.util.*;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowImageStats.EvOpAverageRect;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.util.EvDecimal;
import endrov.util.Vector3i;

/**
 * Alternative representation of an image. The image is a tree. Every pixel belongs to one node.
 * Every subnode contains pixels of higher intensity that are surrounded by pixels in the current node.
 *  
 * THIS CODE IS DIRT SLOW, likely due to memory locality
 * 
 * @author Johan Henriksson
 *
 */
public class LevelHierarchy
	{
	
	public static class Node
		{
		public int intensity;
//		public PersistentGrowingCollection<Vector3i> pixels=null;//new PersistentGrowingCollection<Vector3i>();
		public LinkedList<HashSet<Vector3i>> morePixels=new LinkedList<HashSet<Vector3i>>(); //For fast joining
		public HashSet<Vector3i> pixels=new HashSet<Vector3i>();
		public Node parent;
		public HashSet<Node> children=new HashSet<Node>();
		
		public String toString()
			{
			return " { "+children+" }";
//			return pixels.size()+" { "+children+" }";
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
		
		int countPixel=0;
		
		for(Map.Entry<Integer, LinkedList<Vector3i>> level:pixels.entrySet())
			{
			int thisIntensity=level.getKey();
			System.out.println("i"+thisIntensity);
			
			for(Vector3i v:level.getValue())
				{
				countPixel++;
				if(countPixel%1000==0)
					System.out.println(countPixel);
				
				Node thisNode=null;

				//Order optimized to add single pixels as often as possible
				if(v.z>0)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x,   v.y,   v.z-1);
				if(v.z+1<d)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x,   v.y,   v.z+1);
				if(v.y>0)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x,   v.y-1, v.z);
				if(v.y+1<h)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x,   v.y+1, v.z);
				if(v.x>0)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x-1, v.y,   v.z);
				if(v.x+1<w)
					thisNode=testOneNeigh(thisNode, thisIntensity, pixelNode, v, v.x+1, v.y,   v.z);

				//Alone so far. No neighbours. Create a new Node
				if(thisNode==null)
					{
					thisNode=new Node();
					thisNode.intensity=level.getKey();
					//thisNode.pixels=new PersistentGrowingCollection<Vector3i>(v);//.add(v);
					thisNode.pixels.add(v);
					}

				//System.out.println(thisNode);
				
				pixelNode[v.z][v.y][v.x]=thisNode;
				}

			
			HashSet<Node> nodes=new HashSet<Node>();
			for(int z=0;z<d;z++)
				for(int y=0;y<h;y++)
					for(int x=0;x<w;x++)
						{
						nodes.add(pixelNode[z][y][x]);
						}
			//System.out.println(nodes);
			System.out.println("# top level nodes: "+nodes.size());
			}
		
		
		
		root=pixelNode[0][0][0];
		
		
		}
	
	int expensive=0;
	
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
					//neighNode.pixels=new PersistentGrowingCollection<Vector3i>(v,neighNode.pixels);
					neighNode.pixels.add(v);
					thisNode=neighNode;
					}
				else
					{
					//Need to join nodes. Eliminate thisNode.
					//neighNode.pixels.addAll(thisNode.pixels);   //O(n), replaced by morePixels
					neighNode.morePixels.add(thisNode.pixels); //O(1) join
					
					if(neighNode.morePixels.size()>5)
						{
						//PROBLEM!!!! might join multiple times, hence slow
						//Solution: join groups as they grow
						for(HashSet<Vector3i> p:thisNode.morePixels) //Additional pixel lists from joining
							neighNode.pixels.addAll(p);
						neighNode.morePixels.clear();
						
						}
					
					
					neighNode.children.addAll(thisNode.children);
					for(Node n:thisNode.children)
						n.parent=neighNode;
					
					for(Vector3i u:thisNode.pixels)
						pixelNode[u.z][u.y][u.x]=neighNode;
					for(HashSet<Vector3i> p:thisNode.morePixels) //Additional pixel lists from joining
						for(Vector3i u:p)
							pixelNode[u.z][u.y][u.x]=neighNode;
					thisNode=neighNode;
					
					expensive++;
					if(expensive%1000==0)
						System.out.println("expensive "+expensive);
					}
				}
			else
				{
				//Neighbour must be a higher intensity, hence a child of this pixel.
				
				//Which is the highest node containing pixel?
				int parentInc=0;
				while(neighNode.parent!=null)
					{
					neighNode=neighNode.parent;
					parentInc++;
					if(parentInc>100)
						System.out.println("parentInc "+parentInc);
					}
				pixelNode[z2][y2][x2]=neighNode; //Avoid parent traversal
				
				
				//Check if area is on the same level
				if(neighNode.intensity==thisIntensity)
					{
					//Add this pixel to node
					//neighNode.pixels=new PersistentGrowingCollection<Vector3i>(v,neighNode.pixels);
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
						//neighNode.pixels=new PersistentGrowingCollection<Vector3i>(v,neighNode.pixels);
						thisNode.pixels.add(v);
						}
					thisNode.children.add(neighNode);
					neighNode.parent=thisNode;
					}
				}
			}
		return thisNode;
		}
	
	/*
	private void recursiveNewParent(Node thisNode, Node neighNode, Node[][][] pixelNode)
		{
		for(Vector3i u:thisNode.pixels)
			pixelNode[u.z][u.y][u.x]=neighNode;
		for(Node cn:thisNode.children)
			recursiveNewParent(cn, neighNode, pixelNode);
		}
	*/
	
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
			EvPixels p=parr[z].getReadOnly(EvPixelsType.INT);
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
//		int sum=PersistentGrowingCollection.size(node.pixels);
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
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		
		
		EvData data=EvData.loadFile(new File("/Volumes/TBU_main03/ost4dgood/TB2167_080416.ost"));
		
		Imageset im=data.getIdObjectsRecursive(Imageset.class).values().iterator().next();
		im.metaObject.put("MA15", new EvOpAverageRect(5,5).exec1(im.getChannel("RFP")));
		
		EvStack stack=im.getChannel("MA15").imageLoader.get(new EvDecimal(14050));
		
		
		stack.keySet().remove(new EvDecimal("1"));
		stack.keySet().remove(new EvDecimal("2.5"));
		stack.keySet().remove(new EvDecimal("4"));
		stack.keySet().remove(new EvDecimal("5.5"));
		stack.keySet().remove(new EvDecimal("7"));
		stack.keySet().remove(new EvDecimal("8.5"));
		stack.keySet().remove(new EvDecimal("10"));
		stack.keySet().remove(new EvDecimal("11.5"));
		stack.keySet().remove(new EvDecimal("13"));

		stack.keySet().remove(new EvDecimal("25"));
		stack.keySet().remove(new EvDecimal("26.5"));
		stack.keySet().remove(new EvDecimal("28"));
		stack.keySet().remove(new EvDecimal("29.5"));
		stack.keySet().remove(new EvDecimal("31"));		
		stack.keySet().remove(new EvDecimal("32.5"));
		stack.keySet().remove(new EvDecimal("34"));
		System.out.println(stack.keySet());
		
		LevelHierarchy hi=new LevelHierarchy(stack);
		
		System.out.println("hello");
		
		hi._check();
		System.out.println(hi.root);
		
		System.exit(0);
		}
	
	
	
	}
