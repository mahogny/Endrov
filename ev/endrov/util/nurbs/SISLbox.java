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
