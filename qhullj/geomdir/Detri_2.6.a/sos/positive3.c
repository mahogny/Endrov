/* sos/positive3.c */

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "sos.h"
#include "internal.h"

/*--------------------------------------------------------------------------*/

int sos_positive3 (int h, int i, int j, int k)
     /* SoS predicate positive3.
        Assumes indices in proper range and pairwise different. */
{
  int result, s, d;
#ifdef __DEBUG__
  if (sos_proto_flag)
    print ("sos_positive3 (%d,%d,%d,%d) ", h, i, j, k); 
#endif
  s = basic_isort4 (&h, &i, &j, &k);
  d = sos_lambda4 (h, i, j, k) -> signum;
  result = If (Odd (s), (d == -1), (d == 1));
#ifdef __DEBUG__
  if (sos_test_flag)
    if (not ((0 < h) and (h < i) and (i < j) and (j < k)
             and (k <= sos_common.n)))
      basic_error ("sos_positive3: arguments were wrong");
  if (sos_proto_flag)
    print  ("sos_positive3 (%d,%d,%d,%d) result: %d (%d %d)\n",
            h, i, j, k, result, s, d);
#endif
  return (result);
}
