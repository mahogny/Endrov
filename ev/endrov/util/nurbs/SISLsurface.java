package endrov.util.nurbs;

public class SISLsurface
	{
	int ik1;                      /* Order of surface in first parameter direction.       */
int ik2;                      /* Order of surface in second parameter direction.      */
int in1;                      /* Number of vertices in first parameter direction.     */
int in2;                      /* Number of vertices in second parameter direction.    */
double et1[];                  /* Pointer to knotvector in first parameter direction.  */
double et2[];                  /* Pointer to knotvector in second parameter direction. */
double ecoef[];                /* Pointer to array of vertices of surface. */
double rcoef[];                /* Pointer to the array of scaled vertices if surface is rational. */
int ikind;                    /* Kind of surface
  = 1 : Polynomial B-spline tensor-product
        surface.
  = 2 : Rational B-spline tensor-product
        surface.
  = 3 : Polynomial Bezier tensor-product
        surface.
  = 4 : Rational Bezier tensor-product
        surface.                           */
int idim;                     /* Dimension of the space in which the surface lies.    */
//icopy	/* Indicates whether the arrays of the surface are copied or referenced by creation of the surface.*/
SISLdir pdir;                /* Pointer to a structur to store surface
  direction.    */
SISLbox pbox;                /* Pointer to a structur to store the  surrounded boxes. */
int use_count;                /* use count so that several tracks can share
  surfaces, no internal use */
int cuopen_1;                  /* Open/closed flag, 1. par direction */
int cuopen_2;                  /* Open/closed flag. 2. par direction  */

	}
