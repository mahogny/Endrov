/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer3Dimset.isosurf;

import java.util.*;
import java.util.Map.Entry;

import javax.vecmath.Vector3f;


/**
 * Generator for isosurfaces
 * 
 * @author Johan Henriksson
 */
public class Isosurface
	{
	
	
	private static int[] m_edgeTable = 
		{
				0x0  , 0x109, 0x203, 0x30a, 0x406, 0x50f, 0x605, 0x70c,
				0x80c, 0x905, 0xa0f, 0xb06, 0xc0a, 0xd03, 0xe09, 0xf00,
				0x190, 0x99 , 0x393, 0x29a, 0x596, 0x49f, 0x795, 0x69c,
				0x99c, 0x895, 0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90,
				0x230, 0x339, 0x33 , 0x13a, 0x636, 0x73f, 0x435, 0x53c,
				0xa3c, 0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30,
				0x3a0, 0x2a9, 0x1a3, 0xaa , 0x7a6, 0x6af, 0x5a5, 0x4ac,
				0xbac, 0xaa5, 0x9af, 0x8a6, 0xfaa, 0xea3, 0xda9, 0xca0,
				0x460, 0x569, 0x663, 0x76a, 0x66 , 0x16f, 0x265, 0x36c,
				0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a, 0x963, 0xa69, 0xb60,
				0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0xff , 0x3f5, 0x2fc,
				0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9, 0xaf0,
				0x650, 0x759, 0x453, 0x55a, 0x256, 0x35f, 0x55 , 0x15c,
				0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950,
				0x7c0, 0x6c9, 0x5c3, 0x4ca, 0x3c6, 0x2cf, 0x1c5, 0xcc ,
				0xfcc, 0xec5, 0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0,
				0x8c0, 0x9c9, 0xac3, 0xbca, 0xcc6, 0xdcf, 0xec5, 0xfcc,
				0xcc , 0x1c5, 0x2cf, 0x3c6, 0x4ca, 0x5c3, 0x6c9, 0x7c0,
				0x950, 0x859, 0xb53, 0xa5a, 0xd56, 0xc5f, 0xf55, 0xe5c,
				0x15c, 0x55 , 0x35f, 0x256, 0x55a, 0x453, 0x759, 0x650,
				0xaf0, 0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5, 0xdfc,
				0x2fc, 0x3f5, 0xff , 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0,
				0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c,
				0x36c, 0x265, 0x16f, 0x66 , 0x76a, 0x663, 0x569, 0x460,
				0xca0, 0xda9, 0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac,
				0x4ac, 0x5a5, 0x6af, 0x7a6, 0xaa , 0x1a3, 0x2a9, 0x3a0,
				0xd30, 0xc39, 0xf33, 0xe3a, 0x936, 0x83f, 0xb35, 0xa3c,
				0x53c, 0x435, 0x73f, 0x636, 0x13a, 0x33 , 0x339, 0x230,
				0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f, 0x895, 0x99c,
				0x69c, 0x795, 0x49f, 0x596, 0x29a, 0x393, 0x99 , 0x190,
				0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c,
				0x70c, 0x605, 0x50f, 0x406, 0x30a, 0x203, 0x109, 0x0
		};
	
	private static int[][] m_triTable = //can remove -1, check length instead
		{
					{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 8, 3, 9, 8, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 8, 3, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{9, 2, 10, 0, 2, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{2, 8, 3, 2, 10, 8, 10, 9, 8, -1, -1, -1, -1, -1, -1, -1},
					{3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 11, 2, 8, 11, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 9, 0, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 11, 2, 1, 9, 11, 9, 8, 11, -1, -1, -1, -1, -1, -1, -1},
					{3, 10, 1, 11, 10, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 10, 1, 0, 8, 10, 8, 11, 10, -1, -1, -1, -1, -1, -1, -1},
					{3, 9, 0, 3, 11, 9, 11, 10, 9, -1, -1, -1, -1, -1, -1, -1},
					{9, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{4, 3, 0, 7, 3, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 1, 9, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{4, 1, 9, 4, 7, 1, 7, 3, 1, -1, -1, -1, -1, -1, -1, -1},
					{1, 2, 10, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{3, 4, 7, 3, 0, 4, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1},
					{9, 2, 10, 9, 0, 2, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
					{2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4, -1, -1, -1, -1},
					{8, 4, 7, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{11, 4, 7, 11, 2, 4, 2, 0, 4, -1, -1, -1, -1, -1, -1, -1},
					{9, 0, 1, 8, 4, 7, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
					{4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1, -1, -1, -1, -1},
					{3, 10, 1, 3, 11, 10, 7, 8, 4, -1, -1, -1, -1, -1, -1, -1},
					{1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4, -1, -1, -1, -1},
					{4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3, -1, -1, -1, -1},
					{4, 7, 11, 4, 11, 9, 9, 11, 10, -1, -1, -1, -1, -1, -1, -1},
					{9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{9, 5, 4, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 5, 4, 1, 5, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{8, 5, 4, 8, 3, 5, 3, 1, 5, -1, -1, -1, -1, -1, -1, -1},
					{1, 2, 10, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{3, 0, 8, 1, 2, 10, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
					{5, 2, 10, 5, 4, 2, 4, 0, 2, -1, -1, -1, -1, -1, -1, -1},
					{2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8, -1, -1, -1, -1},
					{9, 5, 4, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 11, 2, 0, 8, 11, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
					{0, 5, 4, 0, 1, 5, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
					{2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5, -1, -1, -1, -1},
					{10, 3, 11, 10, 1, 3, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1},
					{4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10, -1, -1, -1, -1},
					{5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3, -1, -1, -1, -1},
					{5, 4, 8, 5, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1},
					{9, 7, 8, 5, 7, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{9, 3, 0, 9, 5, 3, 5, 7, 3, -1, -1, -1, -1, -1, -1, -1},
					{0, 7, 8, 0, 1, 7, 1, 5, 7, -1, -1, -1, -1, -1, -1, -1},
					{1, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{9, 7, 8, 9, 5, 7, 10, 1, 2, -1, -1, -1, -1, -1, -1, -1},
					{10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3, -1, -1, -1, -1},
					{8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2, -1, -1, -1, -1},
					{2, 10, 5, 2, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1},
					{7, 9, 5, 7, 8, 9, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1},
					{9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11, -1, -1, -1, -1},
					{2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7, -1, -1, -1, -1},
					{11, 2, 1, 11, 1, 7, 7, 1, 5, -1, -1, -1, -1, -1, -1, -1},
					{9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11, -1, -1, -1, -1},
					{5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0, -1},
					{11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0, -1},
					{11, 10, 5, 7, 11, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 8, 3, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{9, 0, 1, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 8, 3, 1, 9, 8, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
					{1, 6, 5, 2, 6, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 6, 5, 1, 2, 6, 3, 0, 8, -1, -1, -1, -1, -1, -1, -1},
					{9, 6, 5, 9, 0, 6, 0, 2, 6, -1, -1, -1, -1, -1, -1, -1},
					{5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8, -1, -1, -1, -1},
					{2, 3, 11, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{11, 0, 8, 11, 2, 0, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
					{0, 1, 9, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
					{5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11, -1, -1, -1, -1},
					{6, 3, 11, 6, 5, 3, 5, 1, 3, -1, -1, -1, -1, -1, -1, -1},
					{0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6, -1, -1, -1, -1},
					{3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9, -1, -1, -1, -1},
					{6, 5, 9, 6, 9, 11, 11, 9, 8, -1, -1, -1, -1, -1, -1, -1},
					{5, 10, 6, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{4, 3, 0, 4, 7, 3, 6, 5, 10, -1, -1, -1, -1, -1, -1, -1},
					{1, 9, 0, 5, 10, 6, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
					{10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4, -1, -1, -1, -1},
					{6, 1, 2, 6, 5, 1, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1},
					{1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7, -1, -1, -1, -1},
					{8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6, -1, -1, -1, -1},
					{7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9, -1},
					{3, 11, 2, 7, 8, 4, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
					{5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11, -1, -1, -1, -1},
					{0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1},
					{9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6, -1},
					{8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6, -1, -1, -1, -1},
					{5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11, -1},
					{0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7, -1},
					{6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9, -1, -1, -1, -1},
					{10, 4, 9, 6, 4, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{4, 10, 6, 4, 9, 10, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1},
					{10, 0, 1, 10, 6, 0, 6, 4, 0, -1, -1, -1, -1, -1, -1, -1},
					{8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10, -1, -1, -1, -1},
					{1, 4, 9, 1, 2, 4, 2, 6, 4, -1, -1, -1, -1, -1, -1, -1},
					{3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4, -1, -1, -1, -1},
					{0, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{8, 3, 2, 8, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1},
					{10, 4, 9, 10, 6, 4, 11, 2, 3, -1, -1, -1, -1, -1, -1, -1},
					{0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6, -1, -1, -1, -1},
					{3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10, -1, -1, -1, -1},
					{6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1, -1},
					{9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3, -1, -1, -1, -1},
					{8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1, -1},
					{3, 11, 6, 3, 6, 0, 0, 6, 4, -1, -1, -1, -1, -1, -1, -1},
					{6, 4, 8, 11, 6, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{7, 10, 6, 7, 8, 10, 8, 9, 10, -1, -1, -1, -1, -1, -1, -1},
					{0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10, -1, -1, -1, -1},
					{10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0, -1, -1, -1, -1},
					{10, 6, 7, 10, 7, 1, 1, 7, 3, -1, -1, -1, -1, -1, -1, -1},
					{1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7, -1, -1, -1, -1},
					{2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9, -1},
					{7, 8, 0, 7, 0, 6, 6, 0, 2, -1, -1, -1, -1, -1, -1, -1},
					{7, 3, 2, 6, 7, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7, -1, -1, -1, -1},
					{2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7, -1},
					{1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11, -1},
					{11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1, -1, -1, -1, -1},
					{8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6, -1},
					{0, 9, 1, 11, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0, -1, -1, -1, -1},
					{7, 11, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{3, 0, 8, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 1, 9, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{8, 1, 9, 8, 3, 1, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
					{10, 1, 2, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 2, 10, 3, 0, 8, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
					{2, 9, 0, 2, 10, 9, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
					{6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8, -1, -1, -1, -1},
					{7, 2, 3, 6, 2, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{7, 0, 8, 7, 6, 0, 6, 2, 0, -1, -1, -1, -1, -1, -1, -1},
					{2, 7, 6, 2, 3, 7, 0, 1, 9, -1, -1, -1, -1, -1, -1, -1},
					{1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6, -1, -1, -1, -1},
					{10, 7, 6, 10, 1, 7, 1, 3, 7, -1, -1, -1, -1, -1, -1, -1},
					{10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8, -1, -1, -1, -1},
					{0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7, -1, -1, -1, -1},
					{7, 6, 10, 7, 10, 8, 8, 10, 9, -1, -1, -1, -1, -1, -1, -1},
					{6, 8, 4, 11, 8, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{3, 6, 11, 3, 0, 6, 0, 4, 6, -1, -1, -1, -1, -1, -1, -1},
					{8, 6, 11, 8, 4, 6, 9, 0, 1, -1, -1, -1, -1, -1, -1, -1},
					{9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6, -1, -1, -1, -1},
					{6, 8, 4, 6, 11, 8, 2, 10, 1, -1, -1, -1, -1, -1, -1, -1},
					{1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6, -1, -1, -1, -1},
					{4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9, -1, -1, -1, -1},
					{10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3, -1},
					{8, 2, 3, 8, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1},
					{0, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8, -1, -1, -1, -1},
					{1, 9, 4, 1, 4, 2, 2, 4, 6, -1, -1, -1, -1, -1, -1, -1},
					{8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1, -1, -1, -1, -1},
					{10, 1, 0, 10, 0, 6, 6, 0, 4, -1, -1, -1, -1, -1, -1, -1},
					{4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3, -1},
					{10, 9, 4, 6, 10, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{4, 9, 5, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 8, 3, 4, 9, 5, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
					{5, 0, 1, 5, 4, 0, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
					{11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5, -1, -1, -1, -1},
					{9, 5, 4, 10, 1, 2, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
					{6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5, -1, -1, -1, -1},
					{7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2, -1, -1, -1, -1},
					{3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6, -1},
					{7, 2, 3, 7, 6, 2, 5, 4, 9, -1, -1, -1, -1, -1, -1, -1},
					{9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7, -1, -1, -1, -1},
					{3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0, -1, -1, -1, -1},
					{6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8, -1},
					{9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7, -1, -1, -1, -1},
					{1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4, -1},
					{4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10, -1},
					{7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10, -1, -1, -1, -1},
					{6, 9, 5, 6, 11, 9, 11, 8, 9, -1, -1, -1, -1, -1, -1, -1},
					{3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5, -1, -1, -1, -1},
					{0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11, -1, -1, -1, -1},
					{6, 11, 3, 6, 3, 5, 5, 3, 1, -1, -1, -1, -1, -1, -1, -1},
					{1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6, -1, -1, -1, -1},
					{0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10, -1},
					{11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5, -1},
					{6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3, -1, -1, -1, -1},
					{5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2, -1, -1, -1, -1},
					{9, 5, 6, 9, 6, 0, 0, 6, 2, -1, -1, -1, -1, -1, -1, -1},
					{1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8, -1},
					{1, 5, 6, 2, 1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6, -1},
					{10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0, -1, -1, -1, -1},
					{0, 3, 8, 5, 6, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{10, 5, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{11, 5, 10, 7, 5, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{11, 5, 10, 11, 7, 5, 8, 3, 0, -1, -1, -1, -1, -1, -1, -1},
					{5, 11, 7, 5, 10, 11, 1, 9, 0, -1, -1, -1, -1, -1, -1, -1},
					{10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1, -1, -1, -1, -1},
					{11, 1, 2, 11, 7, 1, 7, 5, 1, -1, -1, -1, -1, -1, -1, -1},
					{0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11, -1, -1, -1, -1},
					{9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7, -1, -1, -1, -1},
					{7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2, -1},
					{2, 5, 10, 2, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1},
					{8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5, -1, -1, -1, -1},
					{9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2, -1, -1, -1, -1},
					{9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2, -1},
					{1, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 8, 7, 0, 7, 1, 1, 7, 5, -1, -1, -1, -1, -1, -1, -1},
					{9, 0, 3, 9, 3, 5, 5, 3, 7, -1, -1, -1, -1, -1, -1, -1},
					{9, 8, 7, 5, 9, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{5, 8, 4, 5, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1},
					{5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0, -1, -1, -1, -1},
					{0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5, -1, -1, -1, -1},
					{10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4, -1},
					{2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8, -1, -1, -1, -1},
					{0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11, -1},
					{0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5, -1},
					{9, 4, 5, 2, 11, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4, -1, -1, -1, -1},
					{5, 10, 2, 5, 2, 4, 4, 2, 0, -1, -1, -1, -1, -1, -1, -1},
					{3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9, -1},
					{5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2, -1, -1, -1, -1},
					{8, 4, 5, 8, 5, 3, 3, 5, 1, -1, -1, -1, -1, -1, -1, -1},
					{0, 4, 5, 1, 0, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5, -1, -1, -1, -1},
					{9, 4, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{4, 11, 7, 4, 9, 11, 9, 10, 11, -1, -1, -1, -1, -1, -1, -1},
					{0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11, -1, -1, -1, -1},
					{1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11, -1, -1, -1, -1},
					{3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4, -1},
					{4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2, -1, -1, -1, -1},
					{9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3, -1},
					{11, 7, 4, 11, 4, 2, 2, 4, 0, -1, -1, -1, -1, -1, -1, -1},
					{11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4, -1, -1, -1, -1},
					{2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9, -1, -1, -1, -1},
					{9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7, -1},
					{3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10, -1},
					{1, 10, 2, 8, 7, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{4, 9, 1, 4, 1, 7, 7, 1, 3, -1, -1, -1, -1, -1, -1, -1},
					{4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1, -1, -1, -1, -1},
					{4, 0, 3, 7, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{4, 8, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{9, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{3, 0, 9, 3, 9, 11, 11, 9, 10, -1, -1, -1, -1, -1, -1, -1},
					{0, 1, 10, 0, 10, 8, 8, 10, 11, -1, -1, -1, -1, -1, -1, -1},
					{3, 1, 10, 11, 3, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 2, 11, 1, 11, 9, 9, 11, 8, -1, -1, -1, -1, -1, -1, -1},
					{3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9, -1, -1, -1, -1},
					{0, 2, 11, 8, 0, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{3, 2, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{2, 3, 8, 2, 8, 10, 10, 8, 9, -1, -1, -1, -1, -1, -1, -1},
					{9, 10, 2, 0, 9, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8, -1, -1, -1, -1},
					{1, 10, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{1, 3, 8, 9, 1, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 9, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{0, 3, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
		};
	
	
	private static class TRIANGLE
		{
		int p0, p1, p2;
		}
	
	private static class POINT3DID
		{
		//int newID;
		float x, y, z;
		}
	
	
	public Vector3f[] getVertices()
		{
		return vertices;
		}
	public int[] getIndices()
		{
		return indices;
		}
	public Vector3f[] getNormals()
		{
		return normals;
		}
	
	
	private Vector3f[] vertices;
	private int[] indices; 
	private Vector3f[] normals;
	
	//List of POINT3Ds which form the isosurface.
	private final HashMap<Integer,POINT3DID> ivertices=new HashMap<Integer,POINT3DID>();
	
	//List of TRIANGLES which form the triangulation of the isosurface.
	private final Vector<TRIANGLE> triangles=new Vector<TRIANGLE>();
	
	
	//No. of cells in x, y, and z directions.
	private int m_nCellsX, m_nCellsY, m_nCellsZ;
	
	//Cell length in x, y, and z directions.
	private float m_fCellLengthX, m_fCellLengthY, m_fCellLengthZ;
	
	//The buffer holding the scalar field.
	private float[] ptScalarField;
	
	//The isosurface value.
	private float m_tIsoLevel;
	
	//Indicates whether a valid surface is present.
	private boolean m_bValidSurface=false;
	
	
	
	
	/**
	 * Generates the isosurface from the scalar field contained in the buffer ptScalarField[]. 
	 */
	void generateSurface(float[] ptScalarField, float tIsoLevel, int nCellsX, int nCellsY, int nCellsZ, float fCellLengthX, float fCellLengthY, float fCellLengthZ)
		{
		//Clean prior memory
		m_bValidSurface = false;
		vertices = null;
		indices = null;
		normals = null;

		//Store settings
		m_tIsoLevel = tIsoLevel;
		m_nCellsX = nCellsX;
		m_nCellsY = nCellsY;
		m_nCellsZ = nCellsZ;
		m_fCellLengthX = fCellLengthX;
		m_fCellLengthY = fCellLengthY;
		m_fCellLengthZ = fCellLengthZ;
		this.ptScalarField = ptScalarField;
	
		int nPointsInXDirection = (m_nCellsX + 1);
		int nPointsInSlice = nPointsInXDirection*(m_nCellsY + 1);
	
		//manual index remapping probably disables static bound check?
		//only one index, 2 checks removed?
		
		
		// Generate isosurface.
		short oldf[][]=new short[m_nCellsY][m_nCellsX];
		for (int y = 0; y < m_nCellsY; y++)
			for (int x = 0; x < m_nCellsX; x++) 
				{
				// Calculate table lookup index from those vertices which are below the isolevel.
				int z=0;
				short tableIndexB = 0;
				if (ptScalarField[z*nPointsInSlice + y*nPointsInXDirection + x] < m_tIsoLevel)
					tableIndexB |= 16;
				if (ptScalarField[z*nPointsInSlice + (y+1)*nPointsInXDirection + x] < m_tIsoLevel)
					tableIndexB |= 32;
				if (ptScalarField[z*nPointsInSlice + (y+1)*nPointsInXDirection + (x+1)] < m_tIsoLevel)
					tableIndexB |= 64;
				if (ptScalarField[z*nPointsInSlice + y*nPointsInXDirection + (x+1)] < m_tIsoLevel)
					tableIndexB |= 128;
				oldf[y][x]=tableIndexB;
				}
		for (int z = 0; z < m_nCellsZ; z++)
			{
			for (int y = 0; y < m_nCellsY; y++)
				for (int x = 0; x < m_nCellsX; x++) 
					{
					// Calculate table lookup index from those vertices which are below the isolevel.
					short tableIndexB=0;
					if (ptScalarField[(z+1)*nPointsInSlice + y*nPointsInXDirection + x] < m_tIsoLevel)
						tableIndexB |= 16;
					if (ptScalarField[(z+1)*nPointsInSlice + (y+1)*nPointsInXDirection + x] < m_tIsoLevel)
						tableIndexB |= 32;
					if (ptScalarField[(z+1)*nPointsInSlice + (y+1)*nPointsInXDirection + (x+1)] < m_tIsoLevel)
						tableIndexB |= 64;
					if (ptScalarField[(z+1)*nPointsInSlice + y*nPointsInXDirection + (x+1)] < m_tIsoLevel)
						tableIndexB |= 128;
	
					int tableIndex=oldf[y][x]>>4 | tableIndexB;
					oldf[y][x]=tableIndexB;
					
					
					// Now create a triangulation of the isosurface in this cell
					if (m_edgeTable[tableIndex] != 0) 
						{
						if ((m_edgeTable[tableIndex] & 8) != 0) 
							{
							POINT3DID pt = calculateIntersection(x, y, z, 3);
							int id = getEdgeID(x, y, z, 3);
							ivertices.put(id,pt);
							}
						if ((m_edgeTable[tableIndex] & 1) != 0) 
							{
							POINT3DID pt = calculateIntersection(x, y, z, 0);
							int id = getEdgeID(x, y, z, 0);
							ivertices.put(id,pt);
							}
						if ((m_edgeTable[tableIndex] & 256) != 0) 
							{
							POINT3DID pt = calculateIntersection(x, y, z, 8);
							int id = getEdgeID(x, y, z, 8);
							ivertices.put(id,pt);
							}
	
						if (x == m_nCellsX - 1) 
							{
							if ((m_edgeTable[tableIndex] & 4) != 0)
								{
								POINT3DID pt = calculateIntersection(x, y, z, 2);
								int id = getEdgeID(x, y, z, 2);
								ivertices.put(id,pt);
								}
							if ((m_edgeTable[tableIndex] & 2048) != 0)
								{
								POINT3DID pt = calculateIntersection(x, y, z, 11);
								int id = getEdgeID(x, y, z, 11);
								ivertices.put(id,pt);
								}
							}
						if (y == m_nCellsY - 1) 
							{
							if ((m_edgeTable[tableIndex] & 2) != 0) 
								{
								POINT3DID pt = calculateIntersection(x, y, z, 1);
								int id = getEdgeID(x, y, z, 1);
								ivertices.put(id,pt);
								}
							if ((m_edgeTable[tableIndex] & 512) != 0)
								{
								POINT3DID pt = calculateIntersection(x, y, z, 9);
								int id = getEdgeID(x, y, z, 9);
								ivertices.put(id,pt);
								}
							}
						if (z == m_nCellsZ - 1)
							{
							if ((m_edgeTable[tableIndex] & 16) != 0)
								{
								POINT3DID pt = calculateIntersection(x, y, z, 4);
								int id = getEdgeID(x, y, z, 4);
								ivertices.put(id,pt);
								}
							if ((m_edgeTable[tableIndex] & 128) != 0)
								{
								POINT3DID pt = calculateIntersection(x, y, z, 7);
								int id = getEdgeID(x, y, z, 7);
								ivertices.put(id,pt);
								}
							}
						if ((x==m_nCellsX - 1) && (y==m_nCellsY - 1))
							if ((m_edgeTable[tableIndex] & 1024) != 0)
								{
								POINT3DID pt = calculateIntersection(x, y, z, 10);
								int id = getEdgeID(x, y, z, 10);
								ivertices.put(id,pt);
								}
						if ((x==m_nCellsX - 1) && (z==m_nCellsZ - 1))
							if ((m_edgeTable[tableIndex] & 64) != 0)
								{
								POINT3DID pt = calculateIntersection(x, y, z, 6);
								int id = getEdgeID(x, y, z, 6);
								ivertices.put(id,pt);
								}
						if ((y==m_nCellsY - 1) && (z==m_nCellsZ - 1))
							if ((m_edgeTable[tableIndex] & 32) != 0) 
								{
								POINT3DID pt = calculateIntersection(x, y, z, 5);
								int id = getEdgeID(x, y, z, 5);
								ivertices.put(id,pt);
								}

						for (int i = 0; m_triTable[tableIndex][i] != -1; i += 3) 
							{
							TRIANGLE triangle=new TRIANGLE();
							triangle.p0 = getEdgeID(x, y, z, m_triTable[tableIndex][i]);
							triangle.p1 = getEdgeID(x, y, z, m_triTable[tableIndex][i+1]);
							triangle.p2 = getEdgeID(x, y, z, m_triTable[tableIndex][i+2]);
							triangles.add(triangle);
							}
						}
					}
			}
	
		renameVerticesAndTriangles();
		calculateNormals();
		m_bValidSurface = true;
		ptScalarField = null;
		maxUpdated=false;
		}
	
	
	public boolean isSurfaceValid()
		{
		return m_bValidSurface;
		}
	
	
	
	
	//only valid if isSurfaceValid
	public float getVolumeLengthX(){return m_fCellLengthX*m_nCellsX;}
	public float getVolumeLengthY(){return m_fCellLengthY*m_nCellsY;}
	public float getVolumeLengthZ(){return m_fCellLengthZ*m_nCellsZ;}
	
	
	/**
	 * Returns the edge ID.
	 */
	private int getEdgeID(int nX, int nY, int nZ, int nEdgeNo)
		{
		switch (nEdgeNo) 
			{
			case 0:
			return getVertexID(nX, nY, nZ) + 1;
			case 1:
			return getVertexID(nX, nY + 1, nZ);
			case 2:
			return getVertexID(nX + 1, nY, nZ) + 1;
			case 3:
			return getVertexID(nX, nY, nZ);
			case 4:
			return getVertexID(nX, nY, nZ + 1) + 1;
			case 5:
			return getVertexID(nX, nY + 1, nZ + 1);
			case 6:
			return getVertexID(nX + 1, nY, nZ + 1) + 1;
			case 7:
			return getVertexID(nX, nY, nZ + 1);
			case 8:
			return getVertexID(nX, nY, nZ) + 2;
			case 9:
			return getVertexID(nX, nY + 1, nZ) + 2;
			case 10:
			return getVertexID(nX + 1, nY + 1, nZ) + 2;
			case 11:
			return getVertexID(nX + 1, nY, nZ) + 2;
			default:
			// Invalid edge no.
			return -1;
			}
		}
	
	/**
	 * Returns the vertex ID
	 */
	private int getVertexID(int nX, int nY, int nZ)
		{
		return 3*(nZ*(m_nCellsY + 1)*(m_nCellsX + 1) + nY*(m_nCellsX + 1) + nX);
		}
	
	/**
	 * Calculates the intersection point of the isosurface with an edge.
	 */
	private POINT3DID calculateIntersection(int nX, int nY, int nZ, int nEdgeNo)
		{
		float x1, y1, z1, x2, y2, z2;
		int v1x = nX, v1y = nY, v1z = nZ;
		int v2x = nX, v2y = nY, v2z = nZ;
	
		switch (nEdgeNo)
			{
			case 0:
			v2y += 1;
			break;
			case 1:
			v1y += 1;
			v2x += 1;
			v2y += 1;
			break;
			case 2:
			v1x += 1;
			v1y += 1;
			v2x += 1;
			break;
			case 3:
			v1x += 1;
			break;
			case 4:
			v1z += 1;
			v2y += 1;
			v2z += 1;
			break;
			case 5:
			v1y += 1;
			v1z += 1;
			v2x += 1;
			v2y += 1;
			v2z += 1;
			break;
			case 6:
			v1x += 1;
			v1y += 1;
			v1z += 1;
			v2x += 1;
			v2z += 1;
			break;
			case 7:
			v1x += 1;
			v1z += 1;
			v2z += 1;
			break;
			case 8:
			v2z += 1;
			break;
			case 9:
			v1y += 1;
			v2y += 1;
			v2z += 1;
			break;
			case 10:
			v1x += 1;
			v1y += 1;
			v2x += 1;
			v2y += 1;
			v2z += 1;
			break;
			case 11:
			v1x += 1;
			v2x += 1;
			v2z += 1;
			break;
			}
	
		x1 = v1x*m_fCellLengthX;
		y1 = v1y*m_fCellLengthY;
		z1 = v1z*m_fCellLengthZ;
		x2 = v2x*m_fCellLengthX;
		y2 = v2y*m_fCellLengthY;
		z2 = v2z*m_fCellLengthZ;
	
		int nPointsInXDirection = (m_nCellsX + 1);
		int nPointsInSlice = nPointsInXDirection*(m_nCellsY + 1);
		float val1 = ptScalarField[v1z*nPointsInSlice + v1y*nPointsInXDirection + v1x];
		float val2 = ptScalarField[v2z*nPointsInSlice + v2y*nPointsInXDirection + v2x];
		return interpolate(x1, y1, z1, x2, y2, z2, val1, val2);
		}
	
	
	/**
	 * Interpolates between two grid points to produce the point at which the isosurface intersects an edge.
	 */
	private POINT3DID interpolate(float fX1, float fY1, float fZ1, float fX2, float fY2, float fZ2, float tVal1, float tVal2)
		{
		float mu = (m_tIsoLevel - tVal1)/(tVal2 - tVal1);
		POINT3DID interpolation=new POINT3DID();
		interpolation.x = fX1 + mu*(fX2 - fX1);
		interpolation.y = fY1 + mu*(fY2 - fY1);
		interpolation.z = fZ1 + mu*(fZ2 - fZ1);
		return interpolation;
		}
	
	/**
	 * Renames vertices and triangles so that they can be accessed more efficiently.
	 */
	private void renameVerticesAndTriangles()
		{
		int nextID = 0;

	
		//newID better made a local array. move to final array directly
		HashMap<Integer,Integer> fromto=new HashMap<Integer,Integer>();
		vertices = new Vector3f[ivertices.size()];
		for(Entry<Integer,POINT3DID> p:ivertices.entrySet())
			{
			fromto.put(p.getKey(),nextID);
			POINT3DID pp=p.getValue();
			vertices[nextID]=new Vector3f(pp.x,pp.y,pp.z);
			nextID++;
			}
		
		
		// Copy vertex indices which make triangles.
		Iterator<TRIANGLE> vecIterator=triangles.iterator();
		indices = new int[triangles.size()*3];
		for (int i = 0; i < triangles.size(); i++) 
			{
			TRIANGLE tri=vecIterator.next();
			
			//rename triangle
			tri.p0 = fromto.get(tri.p0);
			tri.p1 = fromto.get(tri.p1);
			tri.p2 = fromto.get(tri.p2);
			
			//copy
			indices[i*3] = tri.p0;
			indices[i*3+1] = tri.p1;
			indices[i*3+2] = tri.p2;
			}

		
		ivertices.clear();
		triangles.clear();
		}
	
	
	/**
	 * Calculates the normals
	 */
	private void calculateNormals()
		{
		normals = new Vector3f[indices.length/3];
	
		// Set all normals to 0
		for(int i=0;i<normals.length;i++)
			normals[i]=new Vector3f();
	
		// Calculate normals.
		for(int i = 0; i < indices.length; i+=3) 
			{
			int id0 = indices[i];
			int id1 = indices[i+1];
			int id2 = indices[i+2];
			Vector3f vec1=new Vector3f(
					vertices[id1].x - vertices[id0].x,
					vertices[id1].y - vertices[id0].y,
					vertices[id1].z - vertices[id0].z);
			Vector3f vec2=new Vector3f(
					vertices[id2].x - vertices[id0].x,
					vertices[id2].y - vertices[id0].y,
					vertices[id2].z - vertices[id0].z);
			Vector3f normal=new Vector3f(
					vec1.z*vec2.y - vec1.y*vec2.z,
					vec1.x*vec2.z - vec1.z*vec2.x,
					vec1.y*vec2.x - vec1.x*vec2.y);
			normals[id0].add(normal);
			normals[id1].add(normal);
			normals[id2].add(normal);
			}
	
		// Normalize
		for(Vector3f n:normals)
			n.normalize();
		}
		
	private boolean maxUpdated=false;
	public float maxX,maxY,maxZ,minX,minY,minZ;
	public boolean updateScale()
		{
		if(isSurfaceValid())
			{
			if(vertices.length==0)
				return false;
			if(!maxUpdated)
				{
				maxUpdated=true;
				maxX=maxY=maxZ=0;
				minX=minY=minZ=Float.MAX_VALUE;
				for(Vector3f pp:vertices)
					{
					if(pp.x>maxX) maxX=pp.x;
					if(pp.y>maxY) maxY=pp.y;
					if(pp.z>maxZ) maxZ=pp.z;
					if(pp.x<minX) minX=pp.x;
					if(pp.y<minY) minY=pp.y;
					if(pp.z<minZ) minZ=pp.z;
					}
				
				}
			return true;
			}
		else
			return false;
		}
	}
