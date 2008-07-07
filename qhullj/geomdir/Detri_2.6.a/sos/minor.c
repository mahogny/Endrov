/* sos/minor.c */

/* The functions sos_minor*() calculate given minors using the Lia Library.
   
   Normally, all given indices are assumed to be > 0.  HOWEVER...

     o if the FIRST row index is 0, the unperturbed, this indicates
       the *-row sos_lia_0(), and
     o if the LAST column index is 0, this really means the
       unperturbed 1-column {1,1,...,1}.

   The returned address points to a Lia object representing the
   value of the corresponding determinant.  This result is stored
   only TEMPORARILY and will be overwritten by upcoming calls
   of sos_minor*().  It's the callers responsibility to save it
   via lia_copy() or lia_assign() where needed. */

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "sos.h"
#include "internal.h"

/*--------------------------------------------------------------------------*/

/* Local workspace. */
#define MAX_AUX   17
#define AUX(IND)  &(aux_buffer [li * (IND)])
static Lia_ptr aux_0_0;
static Lia aux_one[3];
static Lia *aux_buffer = NULL;
static int li;

#ifdef __SOS_TRACE__
 static void put_c (int i);
 static void put_i (int i);
 static void put_f (double d);
#endif

/*--------------------------------------------------------------------------*/

void sos_minor (void)
     /* Initializes the module.
        Must be called before any of the below routines is invoked. */
{
  if (not aux_buffer)
    {
      int i;
      aux_0_0 = sos_lia_0_0 ();
      lia_assign (aux_one, lia_const (1));  
      li = lia_get_maximum ();
      aux_buffer = MALLOC (Lia, MAX_AUX * li);
      MARK (aux_buffer, -SOS_MAGIC);
      sos_common.high_minor = 5;
      sos_common.minor = MALLOC (int, sos_common.high_minor + 1);
      MARK (sos_common.minor, -SOS_MAGIC);
      upfor (i, 0, sos_common.high_minor)
        sos_common.minor[i] = 0;
    }
}

/*--------------------------------------------------------------------------*/

Lia_ptr sos_minor1 (int i,
                    int a)
     /* Added for consistency only! */
{
  sos_common.minor[1] ++;
  if (i)
    if (a)
      return (sos_lia (i, a));
    else
      return (aux_one);
  else if (a)
    return (sos_lia_0 (a));
  else
    return (aux_0_0);
}

/*--------------------------------------------------------------------------*/

Lia_ptr sos_minor2 (int i, int j,
                    int a, int b)
{
  sos_common.minor[2] ++;
  if (i)
    {
      if (b)
        { /* | Pi[i,a] Pi[i,b] |
             | Pi[j,a] Pi[j,b] |
             */
          lia_det2 (sos_lia (i, a), sos_lia (i, b),
                    sos_lia (j, a), sos_lia (j, b), AUX (0));
        }
      else
        { /* | Pi[i,a] 1 |
             | Pi[j,a] 1 |
             */
          lia_sub (AUX (0), sos_lia (i, a), sos_lia (j, a));
        }
    }
  else if (b)
    { /* |      **      ** |
         | Pi[j,a] Pi[j,b] |
         */
      basic_error ("sos_minor2(0,..) not yet implemented");
    }
  else
    { /* |      ** ** |
         | Pi[j,a]  1 |
         */
      lia_det2 (sos_lia_0  (a), aux_0_0,
                sos_lia (j, a), aux_one, AUX (0));
    }
#ifdef __SOS_TRACE__
  if (sos_trace_file)
    {
      put_c (2);
      put_i (i);  put_i (j);
      put_c (a);  put_c (b);
      put_f (lia_real (AUX (0)));
    }
#endif
  return (AUX (0));
}

/*--------------------------------------------------------------------------*/

Lia_ptr sos_minor3 (int i, int j, int k,
                    int a, int b, int c)
{
  sos_common.minor[3] ++;
  if (i)
    {
      if (c)
        { /* | Pi[i,a] Pi[i,b] Pi[i,c] |
             | Pi[j,a] Pi[j,b] Pi[j,c] |
             | Pi[k,a] Pi[k,b] Pi[k,c] |
             */
          lia_det3 (sos_lia (i, a), sos_lia (i, b), sos_lia (i, c),
                    sos_lia (j, a), sos_lia (j, b), sos_lia (j, c),
                    sos_lia (k, a), sos_lia (k, b), sos_lia (k, c), AUX (0));
        }
      else
        { /* | Pi[i,a] Pi[i,b] 1 |
             | Pi[j,a] Pi[j,b] 1 |
             | Pi[k,a] Pi[k,b] 1 |
             */
          Lia_ptr x, y;
          /* 2nd row - 1st */
          lia_sub (AUX (1), sos_lia (j, a), (x = sos_lia (i, a)));
          lia_sub (AUX (2), sos_lia (j, b), (y = sos_lia (i, b)));
          /* 3rd row - 1st */
          lia_sub (AUX (3), sos_lia (k, a), x);
          lia_sub (AUX (4), sos_lia (k, b), y);
          /* 2-by-2 det */
          lia_det2 (AUX (1), AUX (2),
                    AUX (3), AUX (4), AUX (0));
        }
    }
  else if (c)
    { /* |      **      **      ** |
         | Pi[j,a] Pi[j,b] Pi[j,c] |
         | Pi[k,a] Pi[k,b] Pi[k,c] |
         */
      basic_error ("sos_minor3(0,...) not yet implemented");
    }
  else
    { /* |      **      ** ** |
         | Pi[j,a] Pi[j,b]  1 |
         | Pi[k,a] Pi[k,b]  1 |
         */
      lia_det3 (sos_lia_0  (a), sos_lia_0  (b), aux_0_0,
                sos_lia (j, a), sos_lia (j, b), aux_one, 
                sos_lia (k, a), sos_lia (k, b), aux_one, AUX (0));
    }    
#ifdef __SOS_TRACE__
  if (sos_trace_file)
    {
      put_c (3);
      put_i (i);  put_i (j);  put_i (k);
      put_c (a);  put_c (b);  put_c (c);
      put_f (lia_real (AUX (0)));
    }
#endif
  return (AUX (0));
}

/*--------------------------------------------------------------------------*/

Lia_ptr sos_minor4 (int i, int j, int k, int l,
                    int a, int b, int c, int d)
{
  sos_common.minor[4] ++;
  if (i)
    {
      if (d)
        { /* | Pi[i,a] Pi[i,b] Pi[i,c] Pi[i,d] |
             | Pi[j,a] Pi[j,b] Pi[j,c] Pi[j,d] |
             | Pi[k,a] Pi[k,b] Pi[k,c] Pi[k,d] |
             | Pi[l,a] Pi[l,b] Pi[l,c] Pi[l,d] |
             */
          lia_det4
            (sos_lia (i, a), sos_lia (i, b), sos_lia (i, c), sos_lia (i, d),
             sos_lia (j, a), sos_lia (j, b), sos_lia (j, c), sos_lia (j, d),
             sos_lia (k, a), sos_lia (k, b), sos_lia (k, c), sos_lia (k, d),
             sos_lia (l, a), sos_lia (l, b), sos_lia (l, c), sos_lia (l, d),
             AUX (0));
        }
      else
        { /* | Pi[i,a] Pi[i,b] Pi[i,c] 1 |
             | Pi[j,a] Pi[j,b] Pi[j,c] 1 |
             | Pi[k,a] Pi[k,b] Pi[k,c] 1 |
             | Pi[l,a] Pi[l,b] Pi[l,c] 1 |
             */
          Lia_ptr x, y, z;
          /* 2nd row - 1st */
          lia_sub (AUX (1), sos_lia (j, a), (x = sos_lia (i, a)));  
          lia_sub (AUX (2), sos_lia (j, b), (y = sos_lia (i, b)));
          lia_sub (AUX (3), sos_lia (j, c), (z = sos_lia (i, c)));
          /* 3rd row - 1st */
          lia_sub (AUX (4), sos_lia (k, a), x);  
          lia_sub (AUX (5), sos_lia (k, b), y);
          lia_sub (AUX (6), sos_lia (k, c), z);
          /* 4th row - 1st */
          lia_sub (AUX (7), sos_lia (l, a), x);  
          lia_sub (AUX (8), sos_lia (l, b), y);
          lia_sub (AUX (9), sos_lia (l, c), z);
          /* (-1) * 3-by-3 det */
          lia_det3 (AUX (1), AUX (2), AUX (3),
                    AUX (4), AUX (5), AUX (6),
                    AUX (7), AUX (8), AUX (9), AUX (0));
          lia_chs (AUX (0));
        }
    }
  else if  (d)
    { /* |      **      **      **      ** |
         | Pi[j,a] Pi[j,b] Pi[j,c] Pi[j,d] |
         | Pi[k,a] Pi[k,b] Pi[k,c] Pi[k,d] |
         | Pi[l,a] Pi[l,b] Pi[l,c] Pi[l,d] |
         */
      basic_error ("sos_minor4(0,...) not yet implemented");
    }
  else
    { /* |      **      **      ** ** |
         | Pi[j,a] Pi[j,b] Pi[j,c]  1 |
         | Pi[k,a] Pi[k,b] Pi[k,c]  1 |
         | Pi[l,a] Pi[l,b] Pi[l,c]  1 |
         */
      lia_det4 (sos_lia_0  (a), sos_lia_0  (b), sos_lia_0  (c), aux_0_0,
                sos_lia (j, a), sos_lia (j, b), sos_lia (j, c), aux_one, 
                sos_lia (k, a), sos_lia (k, b), sos_lia (k, c), aux_one, 
                sos_lia (l, a), sos_lia (l, b), sos_lia (l, c), aux_one, 
                AUX (0));
    }
#ifdef __SOS_TRACE__
  if (sos_trace_file)
    {
      put_c (4);
      put_i (i);  put_i (j);  put_i (k);  put_i (l);
      put_c (a);  put_c (b);  put_c (c);  put_c (d);
      put_f (lia_real (AUX (0)));
    }
#endif
  return (AUX (0));
}

/*--------------------------------------------------------------------------*/

Lia_ptr sos_minor5 (int i, int j, int k, int l, int m,
                    int a, int b, int c, int d, int e)
{
  sos_common.minor[5] ++;
  if (i)
    {
      if (e)
        { /* | Pi[i,a] Pi[i,b] Pi[i,c] Pi[i,d] Pi[i,e] |
             | Pi[j,a] Pi[j,b] Pi[j,c] Pi[j,d] Pi[j,e] |
             | Pi[k,a] Pi[k,b] Pi[k,c] Pi[k,d] Pi[k,e] |
             | Pi[l,a] Pi[l,b] Pi[l,c] Pi[l,d] Pi[l,e] |
             | Pi[m,a] Pi[m,b] Pi[m,c] Pi[m,d] Pi[m,e] |
             */
          basic_error ("sos_minor5: full 5-by-5 determinant not implemented");
        }
      else
        { /* | Pi[i,a] Pi[i,b] Pi[i,c] Pi[i,d] 1 |
             | Pi[j,a] Pi[j,b] Pi[j,c] Pi[j,d] 1 |
             | Pi[k,a] Pi[k,b] Pi[k,c] Pi[k,d] 1 |
             | Pi[l,a] Pi[l,b] Pi[l,c] Pi[l,d] 1 |
             | Pi[m,a] Pi[m,b] Pi[m,c] Pi[m,d] 1 |
             */
          Lia_ptr x, y, z, u;
          /* 2nd row - 1st */
          lia_sub (AUX (1), sos_lia (j, a), (x = sos_lia (i, a)));
          lia_sub (AUX (2), sos_lia (j, b), (y = sos_lia (i, b)));
          lia_sub (AUX (3), sos_lia (j, c), (z = sos_lia (i, c)));
          lia_sub (AUX (4), sos_lia (j, d), (u = sos_lia (i, d)));
          /* 3rd row - 1st */
          lia_sub (AUX (5), sos_lia (k, a), x); 
          lia_sub (AUX (6), sos_lia (k, b), y);
          lia_sub (AUX (7), sos_lia (k, c), z);
          lia_sub (AUX (8), sos_lia (k, d), u);
          /* 4th row - 1st */
          lia_sub (AUX (9),  sos_lia (l, a), x); 
          lia_sub (AUX (10), sos_lia (l, b), y);
          lia_sub (AUX (11), sos_lia (l, c), z);
          lia_sub (AUX (12), sos_lia (l, d), u);
          /* 5th row - 1st */
          lia_sub (AUX (13), sos_lia (m, a), x); 
          lia_sub (AUX (14), sos_lia (m, b), y);
          lia_sub (AUX (15), sos_lia (m, c), z);
          lia_sub (AUX (16), sos_lia  (m, d), u);
          /* 4-by-4 det */
          lia_det4 (AUX (1),  AUX (2),  AUX (3),  AUX (4),
                    AUX (5),  AUX (6),  AUX (7),  AUX (8),
                    AUX (9),  AUX (10), AUX (11), AUX (12),
                    AUX (13), AUX (14), AUX (15), AUX (16), AUX (0));
        }
    }
  else if (e)
    { /* |      **      **      **      **      ** |
         | Pi[j,a] Pi[j,b] Pi[j,c] Pi[j,d] Pi[j,e] |
         | Pi[k,a] Pi[k,b] Pi[k,c] Pi[k,d] Pi[k,e] |
         | Pi[l,a] Pi[l,b] Pi[l,c] Pi[l,d] Pi[l,e] |
         | Pi[m,a] Pi[m,b] Pi[m,c] Pi[m,d] Pi[m,e] |
         */
      basic_error ("sos_minor5(0,...) not yet implemented");
    }
  else
    { /* |      **      **      **      ** ** |
         | Pi[j,a] Pi[j,b] Pi[j,c] Pi[j,d]  1 |
         | Pi[k,a] Pi[k,b] Pi[k,c] Pi[k,d]  1 |
         | Pi[l,a] Pi[l,b] Pi[l,c] Pi[l,d]  1 |
         | Pi[m,a] Pi[m,b] Pi[m,c] Pi[m,d]  1 |
         */
      basic_error ("sos_minor5(0,...,0) not yet implemented");
    }
#ifdef __SOS_TRACE__
  if (sos_trace_file)
    {
      put_c (5);
      put_i (i);  put_i (j);  put_i (k);  put_i (l);  put_i (m);
      put_c (a);  put_c (b);  put_c (c);  put_c (d);  put_c (e);
      put_f (lia_real (AUX (0)));
    }
#endif
  return (AUX (0));
}

/*--------------------------------------------------------------------------*/

#ifdef __SOS_TRACE__

static void put_c (int i)
{
  char c = i;
  binfwrite (&c, 1, sos_trace_file);
}

static void put_i ((int i)
{
  int j = i;
  binfwrite (&j, 1, sos_trace_file);
}

static void put_f (double d)
{
  float f = d;
  binfwrite (&f, 1, sos_trace_file);
}

#endif
