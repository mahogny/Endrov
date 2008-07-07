/* sos/primitive.c --- Some code used in SoS primitives. */

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "sos.h"
#include "internal.h"
#include "primitive.h"

/*--------------------------------------------------------------------------*/

typedef struct clist_type
{ /* for the list storing pointers to depth counters */
  const char *string;
  int n;
  int *c;
  struct clist_type *next;
} Clist;

static Clist *ell = 0;  /* the list of depth counters */

/*--------------------------------------------------------------------------*/

void sos_new_depth_counters (int *array, int length, const char *text)
     /* Adds array to the list of depth counters.
        See the Initilize() macro defined in primitive.h and used in
        lambda4.c code etc.  Internal use only! */
{
  Clist *new;

  new = MALLOC (Clist, 1);
  MARK (new, -SOS_MAGIC);
  new->string = text;
  new->n = length;
  new->c = array;
  new->next = ell;
  ell = new;
}

/*--------------------------------------------------------------------------*/

void sos_depth_counters_output (FILE *file)
     /* Prints all non-zero depth counters in list ell: Lambda4 etc. */
{
  Clist *cell = ell;
  int i, sum0 = 0, sum = 0, max = -1;
  while (cell)
    {
      fprint (file, "* SoS %s, non-zero depth counters\n", cell->string);
      i = 0;
      sum0 += cell->c[0];
      while ((i < cell->n) and (cell->c[i] > 0))
        {
          sum += cell->c[i];
          if (i > max)
            max = i;
          fprint (file, "%12d\n", cell->c[i++]);
        }
      cell = cell->next;
    }
  fprint (file, "* SoS minor evaluations\n");
  downfor (i, sos_common.high_minor, 0)
    if (sos_common.minor[i])
      fprint (file, "%12d . %d-by-%d\n", sos_common.minor[i], i, i);
  fprint (file, "* SoS summary\n");
  fprint (file, "%12d . max depth\n", max);
  fprint (file, "%12f . mean depth\n",(double) sum / (double) sum0 - 1.0);
}

/*--------------------------------------------------------------------------*/

void sos_depth_counters_summary (int *max, double *mean)
     /* Like sos_depth_counters_output(), but returns only summary values. */
{
  Clist *cell = ell;
  int i, sum0 = 0, sum = 0;
  *max = -999;
  *mean = -999.999;
  while (cell)
    {
      i = 0;
      sum0 += cell->c[0];
      while ((i < cell->n) and (cell->c[i] > 0))
        {
          sum += cell->c[i];
          if (i > *max)
            *max = i;
        }
      cell = cell->next;
    }
  *mean = (double) sum / (double) sum0 - 1.0;
}

/*--------------------------------------------------------------------------*/

void sos_minor_calls (int **minor, int *high)
     /* Output: minor[0..high], *hight */
     /* Returns pointer to array with minor counters. */
{
  *minor = sos_common.minor;
  *high = sos_common.high_minor;
}

/*--------------------------------------------------------------------------*/

int sos_epsilon_compare (const SoS_primitive_result *a,
                         const SoS_primitive_result *b)
     /* Returns -1, 0, 1 for
        a->epsilon[0..a->two_k-1] <, ==, > b->epsilon[0..b->two_k-1],
        as returned by SoS primtives. */
{
  int p = 0;
  loop
    {
      if ((p == a->two_k) and (p == b->two_k))
        return (0);
      if (p == a->two_k)
        return (1);
      if (p == b->two_k)
        return (-1);
      if (a->epsilon[p] != b->epsilon[p])
        return (If ((a->epsilon[p] < b->epsilon[p]), 1, -1));
      p ++;
      if (a->epsilon[p] != b->epsilon[p])
        return (If ((a->epsilon[p] > b->epsilon[p]), 1, -1));
      p ++;
    }
}
