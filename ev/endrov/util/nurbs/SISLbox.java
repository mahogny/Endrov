/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.nurbs;

public class SISLbox
	{
  double emax[];                 /* The minimum values to the boxes.          */
  double emin[];                 /* The maximum values to the boxes.          */
  int imin;                     /* The index of the min coeff (one-dim case) */
  int imax;                     /* The index of the max coeff (one-dim case) */

  //double *e2max[3];             /* The minimum values dependant on tolerance.*/
  //double *e2min[3];             /* The maximum values dependant on tolerance.*/
  double etol[]; /*[3]*/              /* Tolerances of the boxes.                  */

	}
