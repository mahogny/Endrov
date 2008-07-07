/* basic/uhash.c  ---  Unversal hash functions. */

/*--------------------------------------------------------------------------*/

#include "basic.h"

/*--------------------------------------------------------------------------*/

#define max_a(R)  (MAXINT / (int) powerof2 (bitsof (Basic_byte)) / (R))
        /* To avoid overflow w/i uhash_function(). */

/*--------------------------------------------------------------------------*/

void basic_uhash_new (int s, int r, int *m, int a[])
     /* Ouput: *m, a[0..r-1]. */
     /* Given s, the desired size of the hash table, and r, the number of
        bytes per key, this procedure returns m, a prime number larger
        than s, and a vector a[0..r-1] defining the universal hash function
        It's the user responsibility to set up a hash table of size m, with
        r-bit keys.
        NOTE: Use a = MALLOC (int, r) and FREE (a) to allocate and free the
        memory of a[].  Also, call srandom (seed) to set up the seed for the
        random() function which will be called within this procedure.
        Reference, eg: Thomas H Cormen, Charles E Leiserson, and Ronald L
        Rivest. "Introduction to Algorithms."  MIT Press, 1990, p229ff. */
{
  int i, ma;
  *m = s = basic_prime_successor (s);
  ma = If ((s < max_a (r)), s - 1, max_a (r) - 1);
  upfor (i, 0, r - 1)
    a[i] = 1 + ((int) random () mod ma);
}

/*--------------------------------------------------------------------------*/

int basic_uhash (const int a[], int r, int m, const Basic_byte x[])
     /* Returns value of unversal hash function given by a[0..r-1] 
        for the (bytes of the) key x[0..r-1].  See basic_uhash_new(). */
{
  int i, sum = 0, m_reg = m;
  upfor (i, 0, r - 1)
    sum += (a[i] * x[i]) mod m_reg;
  return (sum mod m_reg);
}
