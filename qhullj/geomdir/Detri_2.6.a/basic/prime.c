/* basic/prime.c  ---  Simple module for prime numbers. */

/*--------------------------------------------------------------------------*/

#include "basic.h"

/*--------------------------------------------------------------------------*/

static int tests = 0;
static int mods = 0;

/*--------------------------------------------------------------------------*/

int basic_prime_successor (int n)
     /* Returns first prime number larger than n.  The prime number theorem
        quarantees that this won't take too many numbers to test, ie, approx
        no more than ln(n).  To test for primality, the trivial algoritm is
        used, which gives an overall time complexity of O (sqrt(n) * ln(n)).
        Reference, eg, Thomas H Cormen, Charles E Leiserson, and Ronald L
        Rivest. "Introduction to Algorithms."  MIT Press, 1990, p837. */
{
  if (n < 2)
    return (2);
  else
    {      
      n ++;  /* at least one larger! */
      if (not Odd (n))
        n ++;
      while (not basic_prime_test (n))
        n += 2;
      return (n);
    }
}

/*--------------------------------------------------------------------------*/

int basic_prime_test (int n)
     /* Trivially checks n for primality using less than sqrt(n) checks.
        However, it takes advantage of knowing 2, 3, and 5 to be primes,
        thus reducing the number of necessary checks by at least 70%.
        NOTE: Works only for numbers > 1; otherwise will return TRUE!
        */
#define Check(K)  mods++; \
                  if (n mod (K) == 0) return (FALSE)
#define BaseCheck(K)  if (n <= (K)) return (TRUE); \
                      Check (K)
{
  int k;
  tests ++;
  BaseCheck (2);
  BaseCheck (3);
  BaseCheck (5);
  k = 7;
  while (k * k <= n)
    {
      Check (k);  k += 4;
      Check (k);  k += 2;
      Check (k);  k += 4;
      Check (k);  k += 2;
      Check (k);  k += 4;
      Check (k);  k += 6;
      Check (k);  k += 2;
      Check (k);  k += 6;
    }
  return (TRUE);
#undef BaseCheck
#undef Check
}

/*--------------------------------------------------------------------------*/

Basic_prime_info* basic_prime_info (void)
     /* Returns statistical info on basic_prime_test(). */
     /* NOTE: The returned address, which points to the info structure,
              is a constant.  DO NOT FREE() IT and consider the fields
              of the structure as read-only. */
{
  static Basic_prime_info info;
  info.tests = tests;
  info.mods = mods;
  return (&(info));  
}

/*--------------------------------------------------------------------------*/

#if 0

int basic_is_prime_trivial (int n)
     /* Trivially checks n for primality. */
{
  tests ++;
  if (not Odd (n))
    return (FALSE);
  else
    { /* check odd n for primality */
      int k = 3;
      while (k * k <= n)
        {
          mods ++;
          if (n mod k == 0)
            return (FALSE);
          else
            k ++;
        }
      return (TRUE);
    }
}

#endif
