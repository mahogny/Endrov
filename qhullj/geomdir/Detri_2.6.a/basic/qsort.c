/* basic/qsort.c --- Quicksort routine: interface to standard qsort(). */

/*--------------------------------------------------------------------------*/

#include "basic.h"

typedef int (*Compfunc) (const void *, const void *);

/*---------------------------------------------------------------------------*/

void basic_qsort (int table[], int  i, int j,
                  int (*compare) (const int *, const int *))
     /* Input/Output: table[i..j]. */
     /* This routine sorts table[i..j] in place: int (*compare)() is the
        comparison function, which is called with two arguments that point
        to the elements of table[i..j] that are compared; it is assumed to
        return -1, 0, or +1 with the usual meaning.  Cf, man qsort. */
{
  qsort (&(table[i]), (size_t) j - i + 1, (size_t) sizeof (int),
         (Compfunc) compare);
}
