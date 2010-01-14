/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.nurbs;

public class SISLdir
	{
  int igtpi;                    /* 0 - The direction of the surface or curve
  is not greater than pi in any
  parameter direction.
1 - The direction of the surface or curve
  is greater than pi in the first
  parameter direction.
2 - The direction of the surface is greater
  than pi in the second parameter
  direction.                            */
double ecoef[];                /* The coordinates to the center of the cone.*/
double aang;                  /* The angle from the center whice describe the
cone.                                     */
double esmooth[];              /* Coordinates of object after smoothing.    */

	}
