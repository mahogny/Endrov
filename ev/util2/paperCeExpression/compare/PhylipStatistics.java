package util2.paperCeExpression.compare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import endrov.util.EvFileUtil;

/**
 * 
 * newlines are inserted randomly after ,
 *
 * file = node ';'
 * node = leaf ':' distance ',' leaf ':' distance
 * leaf = [ '('  ')' ] | NAME
 * 
 * @author Johan Henriksson
 *
 */

public class PhylipStatistics
	{
	public static class Node
		{
		public String seqName; //null if not at the bottom
		public Node a, b;
		public double distA, distB;
		public Node parent;
		
		@Override
		public String toString()
			{
			if(seqName!=null)
				return seqName;
			else
				return "("+a+","+b+")";
			}
		}

	
	private static Node parseNode(LinkedList<Character> s)
		{
		Node n=new Node();
		if(s.getFirst().equals('('))
			{
			s.removeFirst(); //'('
			n.a=parseNode(s);
			n.a.parent=n;
			s.removeFirst(); //':'
			n.distA=parseDistance(s);
			s.removeFirst(); //','
			while(s.getFirst().equals('\n') || s.getFirst().equals('\r'))
				s.removeFirst();
			n.b=parseNode(s);
			n.b.parent=n;
			s.removeFirst(); //':'
			n.distB=parseDistance(s);
			s.removeFirst(); //')'
			}
		else
			{
			StringBuffer sb=new StringBuffer();
			do
				{
				sb.append(s.getFirst());
				s.removeFirst();
				} while(!s.getFirst().equals(':'));
			n.seqName=sb.toString();
			}
		return n;
		}
	
	private static double parseDistance(LinkedList<Character> s)
		{
		StringBuffer sb=new StringBuffer();
		while(".0123456789".indexOf(s.getFirst())!=-1)
			{
			sb.append(s.getFirst());
			s.removeFirst();
			}
		return Double.parseDouble(sb.toString());
		}
	
	public static Node parseFile(File f) throws IOException
		{
		String c=EvFileUtil.readFile(f);
		LinkedList<Character> list=new LinkedList<Character>();
		for(char cc:c.toCharArray())
			list.add(cc);
		return parseNode(list);
		}
	
	
	
	public static void keepOnlyGenename(Node n)
		{
		if(n.seqName!=null)
			n.seqName=n.seqName.substring(0,n.seqName.indexOf(' '));
		if(n.a!=null)
			{
			keepOnlyGenename(n.a);
			keepOnlyGenename(n.b);
			}
		}
	
	
	public static void getLeafs(List<Node> list, Node n)
		{
		if(n.a!=null)
			{
			getLeafs(list, n.a);
			getLeafs(list, n.b);
			}
		else
			list.add(n);
		}
	
	public static int coallesceDistance(Node a, Node b)
		{
		Map<Node,Integer> as=new HashMap<Node, Integer>();
		int cnta=0;
		while(a!=null)
			{
			as.put(a,cnta);
			a=a.parent;
			cnta++;
			}

		int cntb=0;
		for(;;) //No coallescence means crash
			{
			if(as.containsKey(b))
				return as.get(b)+cntb;
			b=b.parent;
			cntb++;
			}
		
		}
	
	public static int longestDistance(Node n)
		{
		if(n.seqName!=null)
			return 0;
		else
			return Math.max(longestDistance(n.a), longestDistance(n.b))+1;
		}
	
	/*
	public static void randomizeLeafs(List<Node> list)
		{
		ArrayList<Node> newList=new ArrayList<Node>();
		newList.addAll(list);
		Collections.shuffle(newList); //Probably not a good RNG
		
		
		}*/
	
	
	public static void evaluateTree(File f) throws IOException
		{
		System.out.println("------------------------------- "+f);
		
		Node root=parseFile(f);
		keepOnlyGenename(root);
		//System.out.println(n);
		
		List<Node> leafs=new ArrayList<Node>();
		getLeafs(leafs, root);
		int numLeafs=leafs.size();
		
		System.out.println("longest distance: "+longestDistance(root));
		
		//Collect genes of the same type
		Map<String,List<Node>> sameGene=new HashMap<String, List<Node>>();
		for(Node leaf:leafs)
			{
			List<Node> sameGenes=sameGene.get(leaf.seqName);
			if(sameGenes==null)
				sameGene.put(leaf.seqName,sameGenes=new ArrayList<Node>());
			sameGenes.add(leaf);
			}
		
		//Average distance between two nodes
		int numSampleAnyGene=100000;
		double averageDistanceAnyGene=0;
		for(int k=0;k<numSampleAnyGene;k++)
			{
			Node a,b;
			do
				{
				int ra=(int)(Math.random()*numLeafs);
				a=leafs.get(ra);
				}
			while(sameGene.get(a.seqName).size()==1);
			do
				{
				int rb=(int)(Math.random()*numLeafs);
				b=leafs.get(rb);
				} while(sameGene.get(b.seqName).size()==1); 
			
			int dist=coallesceDistance(a,b);
			averageDistanceAnyGene+=dist;
			}
		averageDistanceAnyGene/=numSampleAnyGene;
		System.out.println("for any two recordings: "+averageDistanceAnyGene);
		
		
		//Average distance between two recordings of the same gene.
		//Note the weighting to account for genes with higher duplicity;
		//without it genes like ceh-5 will affect the value more
		int numSampleSameGene=100000;
		double totalWeightSameGene=0;
		double averageDistanceSameGene=0;
		for(int k=0;k<numSampleSameGene;k++)
			{
			Node a,b;
			do
				{
				int ra=(int)(Math.random()*numLeafs);
				a=leafs.get(ra);
				}
			while(sameGene.get(a.seqName).size()==1);
			double weight=1.0/sameGene.get(a.seqName).size();
			do
				{
				int rb=(int)(Math.random()*numLeafs);
				b=leafs.get(rb);
				} while(!a.seqName.equals(b.seqName));
			
			int dist=coallesceDistance(a,b);
			averageDistanceSameGene+=dist*weight;
			totalWeightSameGene+=weight;
			}
		averageDistanceSameGene/=totalWeightSameGene;
		System.out.println("same gene: "+averageDistanceSameGene);
		
		System.out.println("Ratio to random: "+((averageDistanceSameGene-2)/(averageDistanceAnyGene-2)));
		
		//Precision is at least within 0.1 or so. Could do CI
		
		
		
		//Optimal distance:
		System.out.println("Optimal distance: 2");
		//I don't think this needs further motivation. In our case it would make sense to subtract 2
		//in the metric to make 0 the best possible value
		
		
		}
	
	public static void main(String[] args)
		{
		
		try
			{
			evaluateTree(new File("/home/tbudev3/expsummary-0-60/phylip/t-l2/outtree2"));
			evaluateTree(new File("/home/tbudev3/expsummary-0-60/phylip/t-pearson/outtree2"));
			
			evaluateTree(new File("/home/tbudev3/expsummary-0-60/phylip/apt-l2/outtree2"));
			evaluateTree(new File("/home/tbudev3/expsummary-0-60/phylip/apt-pearson/outtree2"));

			evaluateTree(new File("/home/tbudev3/expsummary-0-60/phylip/ss-l2/outtree2"));
			evaluateTree(new File("/home/tbudev3/expsummary-0-60/phylip/ss-pearson/outtree2"));

			evaluateTree(new File("/home/tbudev3/expsummary-0-60/phylip/xyz-l2/outtree2"));
			evaluateTree(new File("/home/tbudev3/expsummary-0-60/phylip/xyz-pearson/outtree2"));
			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
		}
	}
