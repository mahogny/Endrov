/* sos/in_sphere.c */

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "sos.h"
#include "internal.h"

/*--------------------------------------------------------------------------*/

int sos_in_sphere (int o, int i, int j, int k, int p)
     /* SoS predicate in_sphere, aka in_ball3.
        Assumes indices in proper range and pairwise different.
        See README file. */
{
  int result, s, d1, d2;
#ifdef __DEBUG__
  if (sos_proto_flag)
    print ("sos_in_sphere (%d,%d,%d,%d,%d)\n", o, i, j, k, p);
#endif
  (void) basic_isort4 (&o, &i, &j, &k);
  d1 = sos_lambda4 (o, i, j, k) -> signum;
  s = basic_isort5p (&o, &i, &j, &k, &p);
  d2 = sos_lambda5 (o, i, j, k, p) -> signum;
  result = If (Odd (s), (d1 != d2), (d1 == d2));
  /* Note: This is wrong in \cite{edels:sos}. */
#ifdef __DEBUG__
  if (sos_test_flag)
    if (not ((0 < o) and (o < i) and (i < j) and (j < k) and (k < p)
             and (p <= sos_common.n)))
      basic_error ("sos_in_sphere: arguments were wrong");
  if (sos_proto_flag)
    print ("sos_in_sphere (%d,%d,%d,%d,%d) result: %d (%d %d %d)\n",
           o, i, j, k, p, result, s, d1, d2);
#endif
  return (result);
}

/*--------------------------------------------------------------------------*/

int sos_in_sphere_p (int o, int i, int j, int k, int p)
     /* SoS predicate in_sphere_p, aka in_ball3_p.
        Assumes indices in proper range and pairwise different.
        This is identical to sos_in_sphere(), but with the assumption
        that sos_positive3 (o, i, j, k) == TRUE. */
{
  int result, s = 0, d2;
#ifdef __DEBUG__
  if (sos_proto_flag)
    print ("sos_in_sphere_p (%d,%d,%d,%d,%d) ", o, i, j, k, p);
  if (sos_test_flag)
    {
      int d1;
      s = basic_isort4 (&o, &i, &j, &k);
      d1 = sos_lambda4 (o, i, j, k) -> signum;
      if (Odd (s))
        d1 = -d1;
      if (d1 != 1)
        basic_error ("sos_in_sphere_p: orientation not positve");
    }
#endif
  s += basic_isort4 (&o, &i, &j, &k);
  s += basic_isort5p (&o, &i, &j, &k, &p);
  d2 = sos_lambda5 (o, i, j, k, p) -> signum;
  result = If (Odd (s), (1 == d2), (1 != d2));
#ifdef __DEBUG__
  if (sos_test_flag)
    if (not ((0 < o) and (o < i) and (i < j) and (j < k) and (k < p) and
             (p <= sos_common.n)))
    basic_error ("sos_in_sphere_p: arguments were wrong");
  if (sos_proto_flag)
    print ("sos_in_sphere_p (%d,%d,%d,%d,%d) result: %d (%d %d)\n",
           o, i, j, k, p, result, s, d2);
#endif
  return (result);
}
