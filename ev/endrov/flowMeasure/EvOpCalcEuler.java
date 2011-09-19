package endrov.flowMeasure;
/**
 * Original plugin, Copyright 2009 Michael Doube
 * GPL3 or later
 * 
 * additions by Johan Henriksson. GPL3 or later or Endrov license
 * 
 */

import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;


/**
 * 
 * http://www.scipress.org/journals/forma/abstract/1703/17030183.html
 * http://doube.org/plugins.html#euler
 * http://doube.org/files/Connectivity_.java
 * 
 * @author Michael Doube, Johan Henriksson
 * 
 * 
 * 
 * @see <p>
 *      Toriwaki J, Yonekura T (2002) Euler Number and Connectivity Indexes of a
 *      Three Dimensional Digital Picture. Forma 17: 183-209. <a
 *      href="http://www.scipress.org/journals/forma/abstract/1703/17030183.html"
 *      >http://www.scipress.org/journals/forma/abstract/1703/17030183.html</a>
 *      </p>
 *      <p>
 *      Odgaard A, Gundersen HJG (1993) Quantification of connectivity in
 *      cancellous bone, with special emphasis on 3-D reconstructions. Bone 14:
 *      173-182. <a
 *      href="http://dx.doi.org/10.1016/8756-3282(93)90245-6">doi:10.1016
 *      /8756-3282(93)90245-6</a>
 *      </p>
 *      <p>
 *      Lee TC, Kashyap RL, Chu CN (1994) Building Skeleton Models via 3-D
 *      Medial Surface Axis Thinning Algorithms. CVGIP: Graphical Models and
 *      Image Processing 56: 462-478. <a
 *      href="http://dx.doi.org/10.1006/cgip.1994.1042"
 *      >doi:10.1006/cgip.1994.1042</a>
 *      </p>
 *      <p>
 *      Several of the methods are based on Ignacio Arganda-Carreras's
 *      Skeletonize3D_ plugin: <a href=
 *      "http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:skeletonize3d:start"
 *      >Skeletonize3D homepage</a>
 *      </p>
 */

public class EvOpCalcEuler
	{
	private static final int eulerLUT[] = new int[256];
	static
		{
		fillEulerLUT26(eulerLUT);
		}
	
	
	/**
	 * Calculate euler characteristic for the only object.
	 * TODO Modify code to do this for a given object, or do it for all objects?
	 */
	public static void apply(ProgressHandle progh, EvStack in)
		{
		//Make sure values are in the right format directly, less overhead
		double[][] stack=in.getReadOnlyArraysDouble(progh);
		int width=in.getWidth();
		int height=in.getHeight();
		int depth=in.getDepth();
		
		double sumEuler = 0;
		for (int z = 0; z<=depth; z++)  //The <= is from the original code, not a mistake
			for (int y = 0; y<=height; y++)
				for (int x = 0; x<=width; x++)
					{
					// return an array containing the 8 voxels around the top left upper
					// (0,0,0) vertex of the voxel at (x,y,z)
					byte[] octant = getOctant(stack, width, height, depth, x, y, z);
					// check if octant is not empty
					if (octant[0]>0)
						sumEuler += getDeltaEuler(octant, eulerLUT);
					}
		sumEuler /= 8; // deltaEuler values in LUT are 8*true value for clarity
		//JH comment: double sounds insane for precision. should use int?
		
		double deltaChi = sumEuler-correctForEdges(stack, width, height, depth);
		double connectivity = 1-deltaChi;

		/*
		ResultsTable rt = ResultsTable.getResultsTable();
		rt.incrementCounter();
		rt.addLabel("Label", this.imRef.getShortTitle());
		rt.addValue("Euler no.", sumEuler);
		rt.addValue("Î”(Ï‡)", deltaChi);
		rt.addValue("Connectivity", connectivity);
		rt.addValue("Conn. Density ("+cal.getUnit()+"^-3)", connDensity);
		rt.show("Results");
		*/
		
		
		}

	/**
	 * Get octant of a vertex at (0,0,0) of a voxel (upper top left) in a 3D image
	 * (0 border conditions)
	 * 
	 * @return corresponding 8-pixel octant (0 if out of image)
	 */
	private static byte[] getOctant(double[][] stack, int w, int h, int d, int x, int y, int z)
		{
		byte[] octant = new byte[9]; 
		// index 0 is counter to determine octant
		// emptiness, index 8 is at (x,y,z)

		octant[1] = getPixel(stack, w,h,d, x-1, y-1, z-1);
		octant[2] = getPixel(stack, w,h,d, x-1, y, z-1);
		octant[3] = getPixel(stack, w,h,d, x, y-1, z-1);
		octant[4] = getPixel(stack, w,h,d, x, y, z-1);
		octant[5] = getPixel(stack, w,h,d, x-1, y-1, z);
		octant[6] = getPixel(stack, w,h,d, x-1, y, z);
		octant[7] = getPixel(stack, w,h,d, x, y-1, z);
		octant[8] = getPixel(stack, w,h,d, x, y, z);

		for (int n = 1; n<9; n++)
			octant[0] -= octant[n]; 
		// foreground is -1, so octant[0] contains nVoxels in octant
		
		return octant;
		}
	
	/**
	 * Get pixel in 3D image stack (0 border conditions)
	 * 
	 * @return corresponding pixel (0 if out of image). -1 is foreground
	 */
	private static byte getPixel(double[][] stack, int w, int h, int d, int x, int y, int z)
		{
		if (x>=0 && x<w && y>=0 && y<h && z>=0 && z<d)
			return stack[z][w*y+x] != 0 ? (byte) -1 : (byte) 0;
		else
			return 0;
		}

	/**
	 * Get delta euler value for an octant (~= vertex) from look up table Only use
	 * this method when there is at least one foreground voxel in octant. In
	 * binary images, foreground is -1, background = 0
	 * 
	 * @param octant
	 *          9 element array containing nVoxels in zeroth element and 8 voxel
	 *          values
	 * @param LUT
	 *          Euler LUT
	 * @return or false if the point is Euler invariant or not
	 */
	private static int getDeltaEuler(byte[] octant, int[] LUT)
		{
		// check to make sure there is a foreground voxel in this octant
		if (octant[0]==0)
			return 0;
		char n = 1; //optimal really?
		// have to rotate octant voxels around vertex so that
		// octant[8] is foreground as eulerLUT assumes that voxel in position
		// 8 is always foreground. Only have to check each voxel once.
		if (octant[8]==-1)
			{
			n = 1;
			if (octant[1]==-1)
				n |= 128;
			if (octant[2]==-1)
				n |= 64;
			if (octant[3]==-1)
				n |= 32;
			if (octant[4]==-1)
				n |= 16;
			if (octant[5]==-1)
				n |= 8;
			if (octant[6]==-1)
				n |= 4;
			if (octant[7]==-1)
				n |= 2;
			}
		else if (octant[7]==-1)
			{
			n = 1;
			if (octant[2]==-1)
				n |= 128;
			if (octant[4]==-1)
				n |= 64;
			if (octant[1]==-1)
				n |= 32;
			if (octant[3]==-1)
				n |= 16;
			if (octant[6]==-1)
				n |= 8;
			if (octant[5]==-1)
				n |= 2;
			}
		else if (octant[6]==-1)
			{
			n = 1;
			if (octant[3]==-1)
				n |= 128;
			if (octant[1]==-1)
				n |= 64;
			if (octant[4]==-1)
				n |= 32;
			if (octant[2]==-1)
				n |= 16;
			if (octant[5]==-1)
				n |= 4;
			}
		else if (octant[5]==-1)
			{
			n = 1;
			if (octant[4]==-1)
				n |= 128;
			if (octant[3]==-1)
				n |= 64;
			if (octant[2]==-1)
				n |= 32;
			if (octant[1]==-1)
				n |= 16;
			}
		else if (octant[4]==-1)
			{
			n = 1;
			if (octant[1]==-1)
				n |= 8;
			if (octant[3]==-1)
				n |= 4;
			if (octant[2]==-1)
				n |= 2;
			}
		else if (octant[3]==-1)
			{
			n = 1;
			if (octant[2]==-1)
				n |= 8;
			if (octant[1]==-1)
				n |= 4;
			}
		else if (octant[2]==-1)
			{
			n = 1;
			if (octant[1]==-1)
				n |= 2;
			}
		else
			{
			// if we have got here, all the other voxels are background
			n = 1;
			}
		return LUT[n];
		}

	/**
	 * Check all vertices of stack and count if foreground (-1) this is
	 * &#967;<sub>0</sub> from Odgaard and Gundersen (1993) and <i>f</i> in my
	 * working
	 * 
	 * @return number of voxel vertices intersecting with stack vertices
	 */
	private static int getStackVertices(double[][] stack, int w, int h, int d)
		{
		int nStackVertices = 0;
		for (int z = 0; z<d; z += d-1)
			for (int y = 0; y<h; y += h-1)
				for (int x = 0; x<w; x += w-1)
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nStackVertices++;
		return nStackVertices;
		}

	/**
	 * Count the number of foreground voxels on edges of stack, this is part of
	 * &#967;<sub>1</sub> (<i>e</i> in my working)
	 * 
	 * @return number of voxel edges intersecting with stack edges
	 */
	private static int getStackEdges(double[][] stack, int w, int h, int d)
		{
		int nStackEdges = 0;

		// vertex voxels contribute 3 edges
		// this could be taken out into a variable to avoid recalculating it
		// nStackEdges += getStackVertices(stack) * 3; = f * 3;

		// left to right stack edges
		for (int z = 0; z<d; z += d-1)
			for (int y = 0; y<h; y += h-1)
				for (int x = 1; x<w-1; x++)
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nStackEdges++;

		// back to front stack edges
		for (int z = 0; z<d; z += d-1)
			for (int x = 0; x<w; x += w-1)
				for (int y = 1; y<h-1; y++)
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nStackEdges++;

		// top to bottom stack edges
		for (int y = 0; y<h; y += h-1)
			for (int x = 0; x<w; x += w-1)
				for (int z = 1; z<d-1; z++)
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nStackEdges++;
		
		return nStackEdges;
		}

	/**
	 * Count the number of foreground voxel faces intersecting with stack faces
	 * This is part of &#967;<sub>2</sub> and is <i>c</i> in my working
	 * 
	 * @return number of voxel faces intersecting with stack faces
	 */
	private static int getStackFaces(double[][] stack, int w, int h, int d)
		{
		int nStackFaces = 0;

		// vertex voxels contribute 3 faces
		// this could be taken out into a variable to avoid recalculating it
		// nStackFaces += getStackVertices(stack) * 3;

		// edge voxels contribute 2 faces
		// this could be taken out into a variable to avoid recalculating it
		// nStackFaces += getStackEdges(stack) * 2;

		// top and bottom faces
		for (int z = 0; z<d; z += d-1)
			for (int y = 1; y<h-1; y++)
				for (int x = 1; x<w-1; x++)
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nStackFaces++;

		// back and front faces
		for (int y = 0; y<h; y += h-1)
			for (int z = 1; z<d-1; z++)
				for (int x = 1; x<w-1; x++)
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nStackFaces++;

		// left and right faces
		for (int x = 0; x<w; x += w-1)
			for (int y = 1; y<h-1; y++)
				for (int z = 1; z<d-1; z++)
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nStackFaces++;
		
		return nStackFaces;
		}

	/**
	 * Count the number of voxel vertices intersecting stack faces. This
	 * contributes to &#967;<sub>2</sub> (<i>a</i> in my working)
	 * 
	 * @return Number of voxel vertices intersecting stack faces
	 */
	private static int getFaceVertices(double[][] stack, int w, int h, int d)
		{
		int nFaceVertices = 0;

		// top and bottom faces (all 4 edges)
		for (int z = 0; z<d; z += d-1)
			for (int y = 0; y<=h; y++)
				for (int x = 0; x<=w; x++)
					{
					// if the voxel or any of its neighbours are foreground, the vertex is
					// counted
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nFaceVertices++;
					else if (getPixel(stack, w,h,d, x, y-1, z)==-1)
						nFaceVertices++;
					else if (getPixel(stack, w,h,d, x-1, y-1, z)==-1)
						nFaceVertices++;
					else if (getPixel(stack, w,h,d, x-1, y, z)==-1)
						nFaceVertices++;
					}

		// left and right faces (2 vertical edges)
		for (int x = 0; x<w; x += w-1)
			for (int y = 0; y<=h; y++)
				for (int z = 1; z<d; z++)
					{
					// if the voxel or any of its neighbours are foreground, the vertex is
					// counted
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nFaceVertices++;
					else if (getPixel(stack, w,h,d, x, y-1, z)==-1)
						nFaceVertices++;
					else if (getPixel(stack, w,h,d, x, y-1, z-1)==-1)
						nFaceVertices++;
					else if (getPixel(stack, w,h,d, x, y, z-1)==-1)
						nFaceVertices++;
					}

		// back and front faces (0 vertical edges)
		for (int y = 0; y<h; y += h-1)
			for (int x = 1; x<w; x++)
				for (int z = 1; z<d; z++)
					{
					// if the voxel or any of its neighbours are foreground, the vertex is
					// counted
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nFaceVertices++;
					else if (getPixel(stack, w,h,d, x, y, z-1)==-1)
						nFaceVertices++;
					else if (getPixel(stack, w,h,d, x-1, y, z-1)==-1)
						nFaceVertices++;
					else if (getPixel(stack, w,h,d, x-1, y, z)==-1)
						nFaceVertices++;
					}
		
		return nFaceVertices;
		}
	
	/**
	 * Count the number of intersections between voxel edges and stack faces. This
	 * is part of &#967;<sub>2</sub>, in my working it's called <i>b</i>
	 * 
	 * @return number of intersections between voxel edges and stack faces
	 */
	private static int getFaceEdges(double[][] stack, int w, int h, int d)
		{
		int nFaceEdges = 0;

		// top and bottom faces (all 4 edges)
		// check 2 edges per voxel
		for (int z = 0; z<d; z += d-1)
			for (int y = 0; y<=h; y++)
				for (int x = 0; x<=w; x++)
					{
					// if the voxel or any of its neighbours are foreground, the vertex is
					// counted
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						{
						nFaceEdges += 2;
						}
					else
						{
						if (getPixel(stack, w,h,d, x, y-1, z)==-1)
							nFaceEdges++;
						if (getPixel(stack, w,h,d, x-1, y, z)==-1)
							nFaceEdges++;
						}
					}

		// back and front faces, horizontal edges
		for (int y = 0; y<h; y += h-1)
			for (int z = 1; z<d; z++)
				for (int x = 0; x<w; x++)
					{
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nFaceEdges++;
					else if (getPixel(stack, w,h,d, x, y, z-1)==-1)
						nFaceEdges++;
					}

		// back and front faces, vertical edges
		for (int y = 0; y<h; y += h-1)
			for (int z = 0; z<d; z++)
				for (int x = 0; x<=w; x++)
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nFaceEdges++;
					else if (getPixel(stack, w,h,d, x-1, y, z)==-1)
						nFaceEdges++;

		// left and right stack faces, horizontal edges
		for (int x = 0; x<w; x += w-1)
			for (int z = 1; z<d; z++)
				for (int y = 0; y<h; y++)
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nFaceEdges++;
					else if (getPixel(stack, w,h,d, x, y, z-1)==-1)
						nFaceEdges++;

		// left and right stack faces, vertical voxel edges
		for (int x = 0; x<w; x += w-1)
			for (int z = 0; z<d; z++)
				for (int y = 1; y<h; y++)
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nFaceEdges++;
					else if (getPixel(stack, w,h,d, x, y-1, z)==-1)
						nFaceEdges++;
		
		return nFaceEdges;
		}

	/**
	 * Count number of voxel vertices intersecting stack edges. It contributes to
	 * &#967;<sub>1</sub>, and I call it <i>d</i> in my working
	 * 
	 * @param stack
	 * @return number of voxel vertices intersecting stack edges
	 */
	private static int getEdgeVertices(double[][] stack, int w, int h, int d)
		{
		int nEdgeVertices = 0;

		// vertex voxels contribute 1 edge vertex each
		// this could be taken out into a variable to avoid recalculating it
		// nEdgeVertices += getStackVertices(stack);

		// left->right edges
		for (int z = 0; z<d; z += d-1)
			for (int y = 0; y<h; y += h-1)
				for (int x = 1; x<w; x++)
					{
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nEdgeVertices++;
					else if (getPixel(stack, w,h,d, x-1, y, z)==-1)
						nEdgeVertices++;
					}

		// back->front edges
		for (int z = 0; z<d; z += d-1)
			for (int x = 0; x<w; x += w-1)
				for (int y = 1; y<h; y++)
					{
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nEdgeVertices++;
					else if (getPixel(stack, w,h,d, x, y-1, z)==-1)
						nEdgeVertices++;
					}

		// top->bottom edges
		for (int x = 0; x<w; x += w-1)
			for (int y = 0; y<h; y += h-1)
				for (int z = 1; z<d; z++)
					{
					if (getPixel(stack, w,h,d, x, y, z)==-1)
						nEdgeVertices++;
					else if (getPixel(stack, w,h,d, x, y, z-1)==-1)
						nEdgeVertices++;
					}
		
		return nEdgeVertices;
		}
	
	/**
	 * <p>
	 * Calculate a correction value to convert the Euler number of a stack to the
	 * stack's contribution to the Euler number of whatever it is cut from.
	 * <dl>
	 * <dt>a</dt>
	 * <dd>Number of voxel vertices on stack faces</dd>
	 * <dt>b</dt>
	 * <dd>Number of voxel edges on stack faces</dd>
	 * <dt>c</dt>
	 * <dd>Number of voxel faces on stack faces</dd>
	 * <dt>d</dt>
	 * <dd>Number of voxel vertices on stack edges</dd>
	 * <dt>e</dt>
	 * <dd>Number of voxel edges on stack edges</dd>
	 * <dt>f</dt>
	 * <dd>Number of voxel vertices on stack vertices</dd>
	 * </dl>
	 * </p>
	 * <p>
	 * Subtract the returned value from the Euler number prior to calculation of
	 * connectivity
	 * </p>
	 * 
	 * @param stack
	 * @return edgeCorrection for subtraction from the stack's Euler number
	 */
	private static double correctForEdges(double[][] stack, int width, int height, int depth)
		{
		int f = getStackVertices(stack, width, height, depth);
		int e = getStackEdges(stack, width, height, depth)+3*f;
		int c = getStackFaces(stack, width, height, depth)+2*e-3*f; // there are already 6 * f in 2 * e, so remove 3 * f
		int d = getEdgeVertices(stack, width, height, depth)+f;
		int a = getFaceVertices(stack, width, height, depth);
		int b = getFaceEdges(stack, width, height, depth);

		double chiZero = (double) f;
		double chiOne = (double) d-(double) e;
		double chiTwo = (double) a-(double) b+(double) c;

		double edgeCorrection = chiTwo/2+chiOne/4+chiZero/8;

		return edgeCorrection;
		}

	/**
	 * Fill Euler LUT Only odd indices are needed because we only check object
	 * voxels' neighbours, so there is always a 1 in each index. This is derived
	 * from Toriwaki & Yonekura (2002) Table 2 for 26-connected images.
	 */
	private static void fillEulerLUT26(int[] LUT)
		{
		LUT[1] = 1;
		LUT[3] = 0;
		LUT[5] = 0;
		LUT[7] = -1;
		LUT[9] = -2;
		LUT[11] = -1;
		LUT[13] = -1;
		LUT[15] = 0;
		LUT[17] = 0;
		LUT[19] = -1;
		LUT[21] = -1;
		LUT[23] = -2;
		LUT[25] = -3;
		LUT[27] = -2;
		LUT[29] = -2;
		LUT[31] = -1;
		LUT[33] = -2;
		LUT[35] = -1;
		LUT[37] = -3;
		LUT[39] = -2;
		LUT[41] = -1;
		LUT[43] = -2;
		LUT[45] = 0;
		LUT[47] = -1;
		LUT[49] = -1;

		LUT[51] = 0;
		LUT[53] = -2;
		LUT[55] = -1;
		LUT[57] = 0;
		LUT[59] = -1;
		LUT[61] = 1;
		LUT[63] = 0;
		LUT[65] = -2;
		LUT[67] = -3;
		LUT[69] = -1;
		LUT[71] = -2;
		LUT[73] = -1;
		LUT[75] = 0;
		LUT[77] = -2;
		LUT[79] = -1;
		LUT[81] = -1;
		LUT[83] = -2;
		LUT[85] = 0;
		LUT[87] = -1;
		LUT[89] = 0;
		LUT[91] = 1;
		LUT[93] = -1;
		LUT[95] = 0;
		LUT[97] = -1;
		LUT[99] = 0;

		LUT[101] = 0;
		LUT[103] = 1;
		LUT[105] = 4;
		LUT[107] = 3;
		LUT[109] = 3;
		LUT[111] = 2;
		LUT[113] = -2;
		LUT[115] = -1;
		LUT[117] = -1;
		LUT[119] = 0;
		LUT[121] = 3;
		LUT[123] = 2;
		LUT[125] = 2;
		LUT[127] = 1;
		LUT[129] = -6;
		LUT[131] = -3;
		LUT[133] = -3;
		LUT[135] = 0;
		LUT[137] = -3;
		LUT[139] = -2;
		LUT[141] = -2;
		LUT[143] = -1;
		LUT[145] = -3;
		LUT[147] = 0;
		LUT[149] = 0;

		LUT[151] = 3;
		LUT[153] = 0;
		LUT[155] = 1;
		LUT[157] = 1;
		LUT[159] = 2;
		LUT[161] = -3;
		LUT[163] = -2;
		LUT[165] = 0;
		LUT[167] = 1;
		LUT[169] = 0;
		LUT[171] = -1;
		LUT[173] = 1;
		LUT[175] = 0;
		LUT[177] = -2;
		LUT[179] = -1;
		LUT[181] = 1;
		LUT[183] = 2;
		LUT[185] = 1;
		LUT[187] = 0;
		LUT[189] = 2;
		LUT[191] = 1;
		LUT[193] = -3;
		LUT[195] = 0;
		LUT[197] = -2;
		LUT[199] = 1;

		LUT[201] = 0;
		LUT[203] = 1;
		LUT[205] = -1;
		LUT[207] = 0;
		LUT[209] = -2;
		LUT[211] = 1;
		LUT[213] = -1;
		LUT[215] = 2;
		LUT[217] = 1;
		LUT[219] = 2;
		LUT[221] = 0;
		LUT[223] = 1;
		LUT[225] = 0;
		LUT[227] = 1;
		LUT[229] = 1;
		LUT[231] = 2;
		LUT[233] = 3;
		LUT[235] = 2;
		LUT[237] = 2;
		LUT[239] = 1;
		LUT[241] = -1;
		LUT[243] = 0;
		LUT[245] = 0;
		LUT[247] = 1;
		LUT[249] = 2;
		LUT[251] = 1;
		LUT[253] = 1;
		LUT[255] = 0;
		}

	}
