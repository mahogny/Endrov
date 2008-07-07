/* detri/prep.c --- Preprocessing (of vertices) for detri. */

/*--------------------------------------------------------------------------*/

#include "detri.h"

/*--------------------------------------------------------------------------*/

#define SORT_ALONG_SOME_RAY  /* Uncomment this if you want to sort strictly
                                by x-axis like in the original algorithm. */

/* NOTE: Sorting points along some ray other than a major axises helps
   breaking the worst-case behavior of the (nonrandomized) algorithm
   for points that come on planes, slices, or grids.  (Of course,
   all this is no longer necessary with the randomized algorithm... */

static Lia ray_a[3], ray_b[3], ray_c[3];

static int lexi_comparisons, axis_comparisons;
static int lexi_compare (const int *i, const int *j);
static int axis_compare (const int *i, const int *j);
static int index_compare  (const int *i, const int *j);
static void permute (int vertex[], int a, int b);
static void info (FILE *info_file, int n0, int n1, int randomized);

/*--------------------------------------------------------------------------*/

void prep_vertices (int n0, int *n1, int vertex[],
                    int randomized, int proto_flag, FILE *info_file)
     /* Output: *n1, vertex[1..n1..n0] with n1 <= n0 */
     /* ... dumps "duplicates" and "sorts" vertex[1..*n1] as below:
        randomized == 0 ... sort wrt SOME axis, under SoS;
                   == 1 ... take input order;
                    > 1 ... generate random order (srandom) */
{
  int i, j = 1;
  print ("%s %d vertices ...\n",
         If ((randomized == 0), "Sorting", "Scanning"), n0);
  upfor (i, 1, n0)
    vertex[i] = i;
  lexi_comparisons = 0;
  basic_qsort (vertex, 1, n0, lexi_compare);
  upfor (i, 2, n0)
    {
      if (    lia_eq (sos_lia (vertex[i], 1), sos_lia (vertex[j], 1))
          and lia_eq (sos_lia (vertex[i], 2), sos_lia (vertex[j], 2))
          and lia_eq (sos_lia (vertex[i], 3), sos_lia (vertex[j], 3)))
        {
          if (proto_flag)
            print ("     Dumping duplicate vertex %d.\n", vertex[i]);
        }
      else
        {
          j ++;
          if (j < i)
            vertex[j] = vertex[i];
        }
    }
  if (j < n0)
    {
      print ("%15d duplicates dumped as redundant.\n", n0 - j);
      print ("%15d vertices left.\n", j);
    }
  *n1 = j;
  axis_comparisons = 0;
  switch (randomized)
    {
     case 0:
      { 
        lia_assign (ray_a, lia_const (1));
        lia_assign (ray_b, lia_const (1));
        lia_assign (ray_c, lia_const (1));
        basic_qsort (vertex, 1, *n1, axis_compare);  /* Sort a 2nd time! */
        break;
      }
     case 1:
      {
        basic_qsort (vertex, 1, *n1, index_compare);  /* Restore old order. */
        break;
      }
     default:
      {
        srandom (randomized);
        permute (vertex, 1, *n1);
        break;
      }
    }
  if (info_file)
    info (info_file, n0, *n1, randomized);
}

/*--------------------------------------------------------------------------*/

static int axis_compare (const int *i, const int *j)
{
  axis_comparisons ++;
#ifdef SORT_ALONG_SOME_RAY
  return (If (sos_smaller_dist_abcd (*i, *j, ray_a, ray_b, ray_c), -1, 1));
#else
  return (If (sos_smaller (*i, 1, *j, 1), -1, 1));
#endif
}

static int lexi_compare (const int *i, const int *j)
{
  lexi_comparisons ++;
  if (lia_eq (sos_lia (*i, 1), sos_lia (*j, 1)))
    {
      if (lia_eq (sos_lia (*i, 2), sos_lia (*j, 2)))
        {
          if (lia_eq (sos_lia (*i, 3), sos_lia (*j, 3)))
            return (0);
          else if (lia_le (sos_lia (*i, 3), sos_lia (*j, 3)))
            return (-1);
          else
            return (1);
        }
      else if (lia_le (sos_lia (*i, 2), sos_lia (*j, 2)))
        return (-1);
      else
        return (1);
    }
  else if (lia_le (sos_lia (*i, 1), sos_lia (*j, 1)))
    return (-1);
  else
    return (1);
}

static int index_compare (const int *i, const int *j)
{
  return (If ((*i < *j), -1, 1));
}

/*--------------------------------------------------------------------------*/

static void permute (int vertex[], int a, int b)
     /* Input/Output: vertex[a..b]. */
     /* ... permutes vertex[a..b] in a random fashion. */
{
  if (a < b)
    {
      int aux, i = a + ((int) random () mod (b - a + 1));
      Assert ((a <= i) and (i <= b));  /* paranoia? */
      aux = vertex[a];
      vertex[a] = vertex[i];
      vertex[i] = aux;
      permute (vertex, a + 1, b);  /* tail recursion */
    }
}

/*--------------------------------------------------------------------------*/

static void info (FILE *info_file, int n0, int n1, int randomized)
{
#define PrintComps(S,C,N) \
  fprint (info_file, "%12d . %s (%3.2f * n*ld(n), n = %d)\n", \
         C, S, ((double) (C)) / (log2 ((double) (N)) * (N)), N)
  fprint (info_file, "* Input %s\n",
          If ((randomized == 0), "(and sorting)", ""));
  fprint (info_file, "%12d . points\n", n0);
  fprint (info_file, "%12d . duplicates\n", n0 - n1);
  PrintComps ("lexi comparisons", lexi_comparisons, n0);
  if (randomized)
    fprint (info_file, "%12d . seed (1: input order, >1: randomized)\n",
            randomized);
  else
    PrintComps ("axis comparisons", axis_comparisons, n1);
#undef PrintComps
}
