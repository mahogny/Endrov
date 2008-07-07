/* detri/search.c --- Simple but efficient point location in Delaunay Tri. */

/*--------------------------------------------------------------------------*/

#include "dt.h"

/*--------------------------------------------------------------------------*/

static int positive3_tests = 0;

static int search_t (int p, int ef, int i, int j, int k);
static double distance (int ef, int p);
static double square (double x);
static void int_param_push2 (int i, int j, int x);

/*--------------------------------------------------------------------------*/

int dt_search (int p, int k)
     /* Locates point p within Delaunay triangulation of current Trist
        using k initial random samples. Assumes: k > 0 and srandom (seed).
        The result is an edgefacet ef with p in ef^+.  If it's a hull facet,
        then p lies outside the convex hull; otherwise, the point lies inside
        the tetrahedron represented by ef. */
{
  int result, a, b, c, i, ef, ef_prime, last = trist_current ()->last_triangle;
  double dist, dist_prime;
  ;
#define GET_RANDOM_FACET(X)  /* Input/output: int x. */       \
  do                                                           \
    {                                                           \
      X = ((int) random () mod last) + 1; /* random triangle */  \
    } until (not trist_deleted (X));                              \
  X = EdFacet (X, 0)
  ;
  /* Select "good" candidate using k random samples, taking the closest one.
     (The trick here is that we use just normal FP distance() function.) */
  GET_RANDOM_FACET (ef_prime);
  dist_prime = distance (ef_prime, p);
  upfor (i, 2, k)
    {
      GET_RANDOM_FACET (ef);
      dist = distance (ef, p);
      if (dist < dist_prime)
        {
          ef_prime = ef;
          dist_prime = dist;
        }      
    }
  trist_triangle (ef_prime, &a, &b, &c);
  if (not sos_positive3 (p, a, b, c))
    {
      ef_prime = Sym (ef_prime);
      trist_triangle (ef_prime, &a, &b, &c);
    }
  positive3_tests ++;
  ;
  /* Start search at "good" candidate... and return result. */
  result = search_t (p, ef_prime, a, b, c);
#if __DEBUG__
  trist_triangle (result, &a, &b, &c);
  if (trist_hull_facet (result))
    /* p is on CH and visible from v, right? */
    Assert (sos_positive3 (p, a, b, c));
  else
    { /* p is inside tetrahedron ef^+, right? */
      i = Dest (Enext (Fnext (result)));
      (void) dt_test_open_tetra (Fnext (result), a, b, c, i);
      Assert (    sos_positive3 (p, a, b, c)
              and sos_positive3 (p, b, a, i)
              and sos_positive3 (p, c, b, i)
              and sos_positive3 (p, a, c, i));
      }
#endif
  return (result);
}

/*--------------------------------------------------------------------------*/

static int search_t (int p, int ef, int i, int j, int k)
     /* This is the core routine. <Right now implemented with tail recursion!>
        It locates point p within Delaunay triangulation of current Trist,
        assuming ef == (i,j,k) and sos_positive3 (p, i, j, k) == TRUE. 
        The result... (see dt_search() :). */
{
  Assert (dt_test_triangle (ef, i, j, k) and sos_positive3 (p, i, j, k));
  if (trist_hull_facet (ef))
    {
      return (ef);   /* Found!  Outside convex hull. */
    }
  else
    {
      int ef_1 = Fnext (ef);
      int ef_2 = Fnext (ef = Enext (ef));
      int ef_3 = Fnext (ef = Enext (ef));
      int o = Dest (Enext (ef_1));
      if (sos_positive3 (p, i, j, o))
        {
          positive3_tests += 1;
          return (search_t (p, ef_1, i, j, o));
        }
      else if (sos_positive3 (p, j, k, o))
        {
          positive3_tests += 2;
          return (search_t (p, ef_2, j, k, o));
        }
      else if (sos_positive3 (p, k, i, o))
        {
          positive3_tests += 3;
          return (search_t (p, ef_3, k, i, o));
        }
      else
        {
          /* Found! Inside tetrahedron. */
          positive3_tests += 3;
          return (ef);
        }
    }
}

/*--------------------------------------------------------------------------*/

static double distance (int ef, int p)
     /* Returns FP "distance" of edfacet ef to p. */
{
  int a, b, c;
  double d, da, db, dc;
  double p1 = sos_fp (p, 1);
  double p2 = sos_fp (p, 2);
  double p3 = sos_fp (p, 3);
  trist_triangle (ef, &a, &b, &c);
  da = (  square (p1 - sos_fp (a, 1))
        + square (p2 - sos_fp (a, 2))
        + square (p3 - sos_fp (a, 3)));
  db =  (  square (p1 - sos_fp (b, 1))
         + square (p2 - sos_fp (b, 2))
         + square (p3 - sos_fp (b, 3)));
  dc = (  square (p1 - sos_fp (c, 1))
        + square (p2 - sos_fp (c, 2))
        + square (p3 - sos_fp (c, 3)));
  d = Min (da, db);
  return (Min (d, dc));
}

#if 0

/* NOTE: Below "distance" function is slightly cheaper,
         but yields to worse results. */

static double distance (int ef, int p)
     /* Returns FP "distance" of edfacet ef to p. */
{
  double p1 = sos_fp (p, 1);
  double p2 = sos_fp (p, 2);
  double p3 = sos_fp (p, 3);
  double d1, d2, d3;
  int a, b, c;
  trist_triangle (ef, &a, &b, &c);
  d1 = (sos_fp (a, 1) + sos_fp (b, 1) + sos_fp (c, 1)) / 3.0;
  d2 = (sos_fp (a, 2) + sos_fp (b, 2) + sos_fp (c, 2)) / 3.0;
  d3 = (sos_fp (a, 3) + sos_fp (b, 3) + sos_fp (c, 3)) / 3.0;
  return (square (p1 - d1) + square (p2 - d2) + square (p3 - d3));
}

#endif

/*--------------------------------------------------------------------------*/

static double square (double x)
{
  return (x * x);
}    

/*--------------------------------------------------------------------------*/

void dt_search_experiment (int x1, int x2, int y1, int y2, int z1, int z2,
                           int n, int m, int k, int p,
                           const char name[])
     /* Calls "dt_search (p, k)" N times, with N = (n * m), for random point
        within range x1..x2 * y1..y2 * z1..z2, loaded into p-th row of SoS
        parameter matrix (!!!); n different points will be searched for, each
        point m times. ASSUMPTION: srandom() AND sos_param (p, ?) is valid. */
     /* Hey, this is only for debugging! */
{
#define N ((double) (n * m))
  int ef, i, j, i1, j1, k1, o1, i2, j2, k2, o2, ch = 0, x, y, z;
  int tests, tests_sum, tests_sum2, tests_min, tests_max, n_ch;
  double time_total;
  tests_sum = 0;
  tests_sum2 = 0;
  tests_min = MAXINT;
  tests_max = -MAXINT;
  n_ch = 0;
  time_total = basic_utime ();
  upfor (i, 1, n)
    {
      /* load i-th random point */
      x = ((int) random () mod (x2 - x1)) + x1;
      y = ((int) random () mod (y2 - y1)) + y1;
      z = ((int) random () mod (z2 - z1)) + z1;
      int_param_push2 (p, 1, x);
      int_param_push2 (p, 2, y);
      int_param_push2 (p, 3, z);
      lia_plus ();
      lia_plus ();
      sos_param (p, 4, lia_popf ());
      upfor (j, 1, m)
        { /* locate p */
          tests = dt_search_get_tests ();
          ef = dt_search (p, k);
          tests = dt_search_get_tests () - tests;
          ;
          tests_sum += tests;
          tests_sum2 += tests * tests;
          tests_max = Max (tests, tests_max);
          tests_min = Min (tests, tests_min);
          ;
          if (j == 1)
            {
              if (trist_hull_facet (ef))
                {
                  ch = TRUE;
                  n_ch ++;
                }
              else
                {
                  ch = FALSE;
                  trist_triangle (ef, &i1, &j1, &k1);
                  o1 = Dest (Enext (Fnext (ef)));
                  (void) basic_isort4 (&i1, &j1, &k1, &o1);
                }
            }
          else
            {
              Assert_always (ch == trist_hull_facet (ef));
              if (not ch)
                {
                  trist_triangle (ef, &i2, &j2, &k2);
                  o2 = Dest (Enext (Fnext (ef)));
                  (void) basic_isort4 (&i2, &j2, &k2, &o2);
                  Assert_always ((i1 == i2) and (j1 == j2) and (k1 == k2)
                                 and (o1 == o2));
                }
            }
        }
    }
  time_total = basic_utime () - time_total;
  print ("%4d %7.2f %7.2f %8d %3d %8.2f%% %6.2fu %s\n",
         k,
         tests_sum / N,
         sqrt ((N * tests_sum2 - tests_sum) / N / (N - 1.0)),
         tests_max,
         tests_min,
         n_ch / N * 100.0,
         time_total,
         name);
}

static void int_param_push2 (int i, int j, int x)
{
  Lia lx[3];  /* 32-bit int, 10 decimal digits, ceiling(10/8) + 1 == 3 */
  lia_load (lx, x);
  sos_param (i, j, lx);
  lia_push (lx);
  lia_ipower (2);
}

/*--------------------------------------------------------------------------*/

int dt_search_get_tests (void)
     /* Returns (accumulated) number of sos_positive3() tests. */
{
  return (positive3_tests);
}
