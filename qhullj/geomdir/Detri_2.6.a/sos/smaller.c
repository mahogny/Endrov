/* sos/smaller.c */

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "sos.h"
#include "internal.h"

/*--------------------------------------------------------------------------*/

int sos_smaller (int i, int j, int k, int l)
     /* SoS predicate smaller.
        Assumes indices in proper range and (i,j) != (k,l) */
{
  int result;
#ifdef __DEBUG__
  if (sos_proto_flag)
    print ("sos_smaller (%d,%d,%d,%d, %f (fp), %f (fp))  ",
           i, j, k, l, lia_real (sos_lia (i,j)), lia_real (sos_lia (k,l)));
  if (sos_test_flag)
    if (not (((i != k) or (j != l))
             and (0 < i) and (i <= sos_common.n)
             and (0 < k) and (k <= sos_common.n)
             and (0 < j) and (j <= sos_common.d)
             and (0 < l) and (l <= sos_common.d)))
    basic_error ("sos_smaller: wrong arguments");
#endif
  if (not lia_eq (sos_lia (i,j), sos_lia (k,l)))
    result = (lia_le (sos_lia (i,j), sos_lia (k,l)));
  else if (i != k)
    result = (i > k);
  else
    result = (j < l);
#ifdef __DEBUG__
  if (sos_proto_flag)
    print  ("sos_smaller result: %d\n", result);
#endif
  return (result);
}

/*--------------------------------------------------------------------------*/

int sos_smaller_dist_abcd (int i, int k,
                           const Lia_ptr a, const Lia_ptr b, const Lia_ptr c)
{
  int result, s;
#ifdef __DEBUG__
  if (sos_proto_flag)
    print ("sos_smaller_abcd (%d,%d,...)\n", i, k); 
#endif
  if (i < k)
    s = 0;
  else
    { /* swap i <--> k */
      s = i;
      i = k;
      k = s;
      s = 1;
    }
  do
    {
      lia_push (a);
      lia_push (sos_lia (i, 1));
      lia_push (sos_lia (k, 1));
      lia_minus ();
      lia_times ();
      lia_push (b);
      lia_push (sos_lia (i, 2));
      lia_push (sos_lia (k, 2));
      lia_minus ();
      lia_times ();
      lia_push (c);
      lia_push (sos_lia (i, 2));
      lia_push (sos_lia (k, 2));
      lia_minus ();
      lia_times ();
      lia_plus ();
      lia_plus ();
      result = lia_sign (lia_popf ());
      if (result)
        break;
      result = lia_sign (c);
      if (result)
        break;
      result = lia_sign (b);
      if (result)
        break;
      result = lia_sign (a);
      if (result)
        break;
      Assert_always (FALSE);
    } once;
  if (Odd (s))
    result = -result;
#ifdef __DEBUG__
  if (sos_proto_flag)
    print  ("sos_smaller_dist_abcd result: %d\n", result);
#endif
  return (result);
}
