package endrov.worms.javier.fit.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;

import javax.vecmath.Vector3d;

import endrov.worms.javier.WormPixelMatcher;
import endrov.worms.javier.WormShape;
import endrov.worms.javier.skeleton.WormClusterSkeleton;

public class WormFitStoringUtils
	{

	public static void printDicToFile(
			Hashtable<Integer, ArrayList<Vector3d>> fittingDic,
			WormClusterSkeleton wc, String filename, int acumShapeIndex)
		{
		Iterator<Integer> bit = wc.getBasePoints().iterator();
		Iterator<Vector3d> lit;
		ArrayList<Vector3d> mlist;
		Vector3d rec;
		int base;

		File f = new File(filename);
		FileOutputStream fis = null;
		try
			{
			fis = new FileOutputStream(f, true);
			}
		catch (FileNotFoundException e1)
			{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			}
		// BufferedOutputStream bis = new BufferedOutputStream(fis);
		// DataInputStream dis = new DataInputStream(bis);

		PrintWriter pw = null;
		pw = new PrintWriter(fis);
		// The next starts iteration of base points
		while (bit.hasNext())
			{
			base = bit.next();
			mlist = fittingDic.get(base);
			if (mlist==null)
				continue;

			pw.println(base);
			lit = mlist.iterator();
			while (lit.hasNext())
				{
				rec = lit.next();
				pw.print("-1.1 ");
				pw.print(rec.x+" ");
				// pw.println(-4);
				pw.print(rec.y+" ");
				// pw.println(-4);
				pw.print((acumShapeIndex+rec.z)+" ");
				pw.println();
				}
			pw.println("-2.0");
			// Stop iteration for this base
			}
		// is done
		pw.close(); // Without this, the output file may be empty

		}

	public static Hashtable<Integer, ArrayList<Vector3d>> readDicFromFile(
			String filename)
		{
		Hashtable<Integer, ArrayList<Vector3d>> fitDic = new Hashtable<Integer, ArrayList<Vector3d>>();
		Scanner sc = null;
		try
			{
			sc = new Scanner(new File(filename));
			}
		catch (FileNotFoundException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		if (sc==null)
			return null;
		double next = -3.0;

		int base;
		Vector3d v3 = new Vector3d();
		while (sc.hasNext())
			{
			base = sc.nextInt();
			next = sc.nextDouble();
			ArrayList<Vector3d> matches = new ArrayList<Vector3d>();
			while (next!=-2.0)
				{
				v3 = new Vector3d();
				v3.x = (sc.nextDouble());
				v3.y = (sc.nextDouble());
				v3.z = sc.nextDouble();
				next = sc.nextDouble();
				matches.add(v3);
				}
			fitDic.put(base, matches);
			}
		return fitDic;
		}

	public static void printShapesToFile(ArrayList<WormShape> matchedShapes,
			String filename)
		{
		Iterator<WormShape> wit = matchedShapes.iterator();
		Iterator<Integer> lit;
		ArrayList<Vector3d> mlist;
		Vector3d rec;
		int base;

		File f = new File(filename);
		FileOutputStream fis = null;
		try
			{
			fis = new FileOutputStream(f, true);
			}
		catch (FileNotFoundException e1)
			{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			}
		// BufferedOutputStream bis = new BufferedOutputStream(fis);
		// DataInputStream dis = new DataInputStream(bis);

		PrintWriter pw = null;
		pw = new PrintWriter(fis);
		// The next starts iteration of base points
		WormShape wsh = null;
		while (wit.hasNext())
			{
			wsh = wit.next();
			pw.print(-1+" ");
			lit = wsh.getWormArea().iterator();
			int pixel;
			while (lit.hasNext())
				{
				pixel = lit.next();
				pw.print(pixel+" ");
				}
			pw.println(-1);
			// Stop iteration for this base
			}
		// is done
		pw.close(); // Without this, the output file may be empty

		}

	public static void printIsoWormsToFile(ArrayList<WormShape> isolatedWorms,
			String filename)
		{
		Iterator<WormShape> wit = isolatedWorms.iterator();
		Iterator<Integer> lit;
		ArrayList<Vector3d> mlist;
		Vector3d rec;
		int base;

		File f = new File(filename);
		FileOutputStream fis = null;
		try
			{
			fis = new FileOutputStream(f, true);
			}
		catch (FileNotFoundException e1)
			{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			}
		// BufferedOutputStream bis = new BufferedOutputStream(fis);
		// DataInputStream dis = new DataInputStream(bis);

		PrintWriter pw = null;
		pw = new PrintWriter(fis);
		// The next starts iteration of base points
		WormShape wsh = null;
		while (wit.hasNext())
			{
			wsh = wit.next();
			pw.print(-1+" ");
			lit = wsh.getWormContour().iterator();
			int pixel;
			while (lit.hasNext())
				{
				pixel = lit.next();
				pw.print(pixel+" ");
				}
			pw.println(-1);
			pw.print(-1+" ");
			lit = wsh.getWormArea().iterator();
			while (lit.hasNext())
				{
				pixel = lit.next();
				pw.print(pixel+" ");
				}
			pw.println(-1);
			// Stop iteration for this base
			}
		// is done
		pw.close(); // Without this, the output file may be empty
		}

	public static ArrayList<WormShape> readIsoltedFromFile(String filename,
			WormPixelMatcher wpm, int[] dtArray)
		{
		ArrayList<ArrayList<Integer>> shapes = new ArrayList<ArrayList<Integer>>();
		Scanner sc = null;
		System.out.println("ISOLATED FILE NAME: "+filename);
		try
			{
			sc = new Scanner(new File(filename));
			}
		catch (FileNotFoundException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		if (sc==null)
			return null;

		int next;
		while (sc.hasNext())
			{
			next = sc.nextInt();
			if (next==-1)
				{
				next = sc.nextInt();
				ArrayList<Integer> shape = new ArrayList<Integer>();
				while (next!=-1)
					{
					shape.add(next);
					next = sc.nextInt();
					}
				shapes.add(shape);
				}
			}
		ArrayList<WormShape> wshapes = new ArrayList<WormShape>();
		Iterator<ArrayList<Integer>> it = shapes.iterator();
		while (it.hasNext())
			{
			WormShape worm = new WormShape(it.next(), it.next(), wpm, dtArray);
			wshapes.add(worm);
			}
		return wshapes;
		}

	public static ArrayList<WormShape> readShapesFromFile(String filename,
			WormPixelMatcher wpm, int[] dtArray)
		{
		ArrayList<ArrayList<Integer>> shapes = new ArrayList<ArrayList<Integer>>();
		Scanner sc = null;
		try
			{
			sc = new Scanner(new File(filename));
			}
		catch (FileNotFoundException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		if (sc==null)
			return null;

		int next;
		while (sc.hasNext())
			{
			next = sc.nextInt();
			if (next==-1)
				{
				next = sc.nextInt();
				ArrayList<Integer> shape = new ArrayList<Integer>();
				while (next!=-1)
					{
					shape.add(next);
					next = sc.nextInt();
					}
				shapes.add(shape);
				}
			}
		ArrayList<WormShape> wshapes = new ArrayList<WormShape>();
		Iterator<ArrayList<Integer>> it = shapes.iterator();
		while (it.hasNext())
			{
			WormShape worm = new WormShape(it.next(), wpm, false, dtArray);
			wshapes.add(worm);
			}
		return wshapes;
		}

	}
