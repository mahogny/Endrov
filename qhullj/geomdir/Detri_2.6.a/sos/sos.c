/* sos/sos.c  ---  The SoS Library kernel. */

/*---------------------------------------------------------------------------*/

const char sos__version[] = "@(#) SoS Library 1.6";
const char sos__authors[] = "Ernst Mucke";
#ifdef __DEBUG__
const char sos__compile[] = "@(#) \t w/ -D__DEBUG__";
#endif

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "sos.h"
#include "internal.h"

/*--------------------------------------------------------------------------*/

/* SoS tuning. */

#define ENLARGE  2.0
/* The SoS parameter matrix is now semi-dynamic, meaning: the number of rows
   of the matrix can be enlarged (by a factor n_enlarge, set by default to
   ENLARGE) whenever sos_param() runs out of range.  A reasonable value for
   ENLARGE would be 2.0; set to 1.0 to disable this feature!
   Cf: sos_matrix(), sos_param(), sos_enlarge(), and n_enlarge. */

#ifdef __DEBUG__
 /* Global flags.  For debugging only! */
 int sos_test_flag = FALSE;
 int sos_proto_flag = FALSE;
 int sos_proto_e_flag = FALSE;
#endif

#ifdef __SOS_TRACE__
 /* Globally accessible trace file.  For debugging only! */
 FILE *sos_trace_file = NULL;
#endif

/*--------------------------------------------------------------------------*/

static Lia *parameter;                    /* SoS parameter matrix. */
static Lia *parameter_0_d_plus_1;
static int  last_loc;

static double *fp_parameter;              /* Floating-point shadow matrix. */
static double  fp_parameter_0_d_plus_1;
static int     fp_last_loc;

static int last_star;
static unsigned long memory = 0L, memory2 = 0L;

static double n_enlarge = ENLARGE;
static int n_max = 0;

static void allocate (int malloc_flag);

SoS_common sos_common;  /* Global SoS common block, for internal use only! */

/*--------------------------------------------------------------------------*/

/* Macros to access the (I,J)-th element of the parameter matrix. */

#define    LOC(I,J)  (sos_common.lenp * (sos_common.d * (I) + (J) - 1))
#define FP_LOC(I,J)                     (sos_common.d * (I) + (J) - 1)

/*--------------------------------------------------------------------------*/

void sos_matrix (int n0, int d, double scale, int len, int lenp)
     /* Initializes the SoS module.
        - Allocates a dynamic array for the global SoS parameter matrix
          Pi[i,j] == sos_lia (i,j).  The parameter range is 1 <= i <= n
          and 1 <= j <= d, where n == n0, initially.  The matrix can then
          be "enlarged" (wrt. number of rows) to fit any following sos_param()
          calls by a certain factor (default: 2.0).
          Cf: sos_param(), sos_enlarge().
        - Allocates space for a floating-point "shadow" matrix, storing
          nothing but the (double) FP representation of the Lia parameters.
          Cf: sos_fp().
        - Parameter scale sets the value for sos_scale().
          This can be used to "camouflage" the fact that SoS works internally
          with long-integer (Lia) parameters.  The user can either ignore this
          feature or use the following convention:
          
              "real" FP parameter [i,j] == sos_scale () "*" sos_lia (i,j)
                                        == sos_scale ()  *  sos_fp  (i,j)
                                        
        - Furthermore, allocates space for the *-row with internal index 0
          (see minor.c and lambda3_star.c). The *-row has d + 1 entries!
          The first d entries are stored in the unused 0-row of parameter[];
          the last entry is stored in parameter_0_d_plus_1.  The *-row is
          accessible via sos_lia_0 (j) with 1 <= j <= d + 1. */
{
  unsigned long m;
  ;
  if (parameter)
    basic_error ("sos_matrix: called twice");
  lia_length (len);
  n_enlarge = If ((n0 < 0), 1.0, ENLARGE);
  n_max = abs (n0);
  sos_common.n = n_max;
  sos_common.d = d;
  sos_common.len = len;
  sos_common.lenp = lenp;
  sos_common.scale = scale;
  last_star = sos_common.d;
  allocate (TRUE);
  /* NOTE: We don't touch the sos_common.*minor fields! */
  ;
  m = basic_malloc_info ()->total;
  lia_det ();
  sos_minor ();
  memory2 = basic_malloc_info ()->total - m;
  ;
#ifdef __DEBUG__
  if (sos_test_flag)
    print ("SoS: Library compiled with -DEBUG and sos_test flag set!\n");
#endif
}

/*--------------------------------------------------------------------------*/

int sos_max (void)
{
  return (n_max);
}

/*--------------------------------------------------------------------------*/

unsigned long sos_bytes (void)
     /* Returns space requirements for SoS matrix in bytes. */     
{
  return (memory + memory2);
}

/*--------------------------------------------------------------------------*/

void sos_shutdown (void)
     /* Shuts down the SoS module.
        It can then be (re)initiaized with sos_matrix(). */
{
  if (parameter)
    {
      FREE (parameter);
      FREE (parameter_0_d_plus_1);
      FREE (fp_parameter);
      last_loc = fp_last_loc = 0;
      n_enlarge = 1.0;
      sos_common.d = 0;
      sos_common.n = n_max = 0;
      sos_common.len = 0;
      sos_common.lenp = 0;
      sos_common.scale = 0;
      /* NOTE: We don't touch the sos_common.*minor fields! */
    }
}

/*--------------------------------------------------------------------------*/

int sos_is_down (void)
     /* Returns TRUE iff SoS is shut down and sos_matrix() can be called. */
{
  return (not parameter);
}

/*--------------------------------------------------------------------------*/

void sos_param (int i, int j, const Lia_obj longi)
     /* Loads a Lia object longi into SoS parameter Pi[i,j]. */
{
  int loc = FP_LOC (i, j);
  ;
  /* Always do a range check! */
  if ((i == 0) or (i <  1) or (j <  1) or (j > sos_common.d))
    basic_error ("sos_param (%d, %d, ...): wrong indices", i, j);
  if (i > sos_common.n)
    { 
      if (n_enlarge <= 1.0)
        basic_error ("sos_param (%d, %d, ...): row index too large", i, j);
      else if (i <= n_max)
        sos_common.n = i;
      else
        { /* enlarge parameter matrix...  */
          n_max *= n_enlarge;
          sos_common.n = i;
          allocate (FALSE);
        }
    }
  ;
  if (lia_high (longi) > sos_common.lenp - 1)
    basic_error ("sos_param (%d, %d, ...): Lia parameter too long: %d",
                 i, j, lia_high (longi));
  ;
  lia_assign (sos_lia (i, j), longi);
  ;
  Assert_always (loc <= fp_last_loc);
  fp_parameter[loc] = lia_real (sos_lia (i, j));
}

/*--------------------------------------------------------------------------*/

void sos_enlarge (double factor)
     /* Changes the n_enlarge factor (default: ENLARGE).
        If factor <= 1.0, the "enlarging" feature is turned off.
        Also, in this case, the matrix is reallocated so that it fits
        only the rows between 1 and the current n. */
{
  n_enlarge = factor = Max (factor, 1.0);
  if ((factor <= 1.0) and (sos_common.n < n_max))
    {
      n_max = sos_common.n;
      allocate (FALSE);
    }
}

/*--------------------------------------------------------------------------*/

void sos_grow (int amount)
     /* Enlarges the sos matrix by "amount" rows */
{
  n_max = n_max + amount;
  allocate(FALSE);
}

/*--------------------------------------------------------------------------*/

static void allocate (int malloc_flag)
     /* Refinement doing the matrix (re)allocation for n_max rows. */
{
  unsigned long m = basic_malloc_info ()->total;
  last_loc = LOC (n_max, sos_common.d);
  fp_last_loc = FP_LOC (n_max, sos_common.d);
  if (malloc_flag)
    { /* Cf: sos_matrix() */
      print ("SoS: ");
      flush ();
      parameter = MALLOC (Lia, last_loc + sos_common.lenp);
      BZERO (parameter,   Lia, last_loc + sos_common.lenp);
      parameter_0_d_plus_1 = MALLOC (Lia, sos_common.lenp);
      BZERO (parameter_0_d_plus_1,   Lia, sos_common.lenp);
      ;
      fp_parameter = MALLOC (double, fp_last_loc + 1);
      BZERO (fp_parameter,   double, fp_last_loc + 1);
      fp_parameter_0_d_plus_1 = 0.0;
      memory = basic_malloc_info ()->total - m;
      print ("matrix[%d,%d] @ %d Lia digits; lia_length (%d); %.3f Mb.\n",
             n_max, sos_common.d,
             sos_common.lenp, sos_common.len, basic_mbytes (memory));
    }
  else
    { /* Cf; sos_param(), sos_enlarge() */
      print ("SoS: Resizing... ");
      flush ();
      REALLOC (parameter, Lia, last_loc + sos_common.lenp);
      REALLOC (fp_parameter, double, fp_last_loc + 1);                 
      memory = basic_malloc_info ()->total - m;
      print ("matrix[%d,%d], %.3f Mb.\n",
             n_max, sos_common.d, basic_mbytes (memory));
    }
}

/*--------------------------------------------------------------------------*/

Lia_ptr sos_lia (int i, int j)
     /* Returns a pointer to the Lia representation of parameter Pi[i,j] */
{
  int loc = LOC (i, j);
#ifdef __DEBUG__
  if (sos_test_flag)
    if ((loc < 0) or (loc > last_loc)
        or (i < 1) or (i > sos_common.n)
        or (j < 1) or (j > sos_common.d))
    basic_error ("sos_lia(%d,%d) %s: n=%d d=%d lenp=%d len=%d loc=%d last=%d",
                i, j, "index error", sos_common.n, sos_common.d,
                sos_common.lenp, sos_common.len, loc, last_loc);
#endif
  return (& (parameter[loc]));
}

/*--------------------------------------------------------------------------*/

double sos_fp (int i, int j)
     /* Returns the floating-point "shadow" copy of sos_lia (i, j). */
{
  int loc = FP_LOC (i, j);
  Assert (fp_parameter[loc] == lia_real (sos_lia (i, j)));
  return (fp_parameter[loc]);
}

double sos_fp_0_d_plus_1 (void)
     /* I agree, that's a bit silly... :) */
{
  return (fp_parameter_0_d_plus_1);
}

/*--------------------------------------------------------------------------*/

double sos_scale (void)
     /* Returns the scale parameter of sos_matrix(). */
{
  return (sos_common.scale);
}

/*--------------------------------------------------------------------------*/

Lia_ptr sos_lia_0 (int j)
     /* Returns pointer to Lia entry of *-row. Internal use only! */
{
  if (j == sos_common.d + 1)
    return (parameter_0_d_plus_1);
  else
    {
#ifdef __DEBUG__
      if (sos_test_flag)
        if ((j < 1) or (j > sos_common.d))
          basic_error ("sos_lia_0: index error");
#endif
      return (& (parameter [LOC (0, j)]));
    }
}

/*--------------------------------------------------------------------------*/

Lia_ptr sos_lia_0_0 (void)
     /* For notational convenience, the [0] entry of the *-row equals
        the [last_star] entry of the *-row (see minor.c/lambda3_star.c).
        Internal use only! */
{
#if __DEBUG__
  if (sos_test_flag)
    if ((last_star < 1) or (last_star > sos_common.d + 1))
      basic_error ("sos_lia_0_0: wrong last_star index");
#endif
  return (sos_lia_0 (last_star));
}

/*--------------------------------------------------------------------------*/

void sos_set_last_star (int j)
     /* Internal use only! */
{
  last_star = j;
}
