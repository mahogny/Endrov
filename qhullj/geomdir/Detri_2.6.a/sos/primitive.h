/* sos/primitive.h --- Macro definitions for code of SoS primitives. */

/* NOTE: Maybe you also want to use #include "primitive.i.c" to reduce
         code size, see, eg, lambda3.c. */

#ifndef __SOS_PRIMITIVE__ /* Include this file only once! */
#define __SOS_PRIMITIVE__ 

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "sos.h"
#include "internal.h"

/*--------------------------------------------------------------------------*/

#define Static_Declarations(NAME,DIM,MAX_t,MAX_k) \
  static SoS_primitive_result result; \
  static char *name = NAME; \
  static int max = (MAX_t) + 1; \
  static int counter [(MAX_t) + 1] = {0}; \
  static int epsilon_product [2 * (MAX_k) + 1]; \
  static int virgin_flag = TRUE; \
  static int dimension = DIM

/*--------------------------------------------------------------------------*/

#define Pi(LAMBDA,KAPPA)  lia_deci (sos_lia (LAMBDA, KAPPA))

/*--------------------------------------------------------------------------*/

#define Initialize() \
  if (virgin_flag) \
    { virgin_flag = FALSE; sos_new_depth_counters (counter, max, name); } \
  result.epsilon = epsilon_product  

#define Initialize_Star() \
  sos_set_last_star (dimension); \
  Initialize ()

/*--------------------------------------------------------------------------*/

#define Epsilon_Term(T) \
  result.depth = (T); \
  counter[T] ++; \
  result.two_k = 0

#define Epsilon(LAMBDA,KAPPA) \
  result.epsilon[result.two_k++] = (LAMBDA); \
  result.epsilon[result.two_k++] = (KAPPA)

/* NOTE: These macros can be redefined as static functions with
         #include "primitive.i.c" */

/*--------------------------------------------------------------------------*/

#define Coefficient(LIA_POINTER) \
  result.lia_pointer = LIA_POINTER; \
  Aftermath ()

#define Positive_Coefficient(LIA_POINTER) \
  Coefficient (LIA_POINTER)

#define Negative_Coefficient(LIA_POINTER) \
  result.lia_pointer = LIA_POINTER; \
  lia_chs (result.lia_pointer); \
  Aftermath ()

#define Aftermath() \
  result.signum = lia_sign (result.lia_pointer); \
  if (result.signum != 0) return (&result)

/* NOTE: These macros can NOT be redifined as functions because of the
         "return" in Aftermath(). */

/*--------------------------------------------------------------------------*/

#define Minor1  sos_minor1
#define Minor2  sos_minor2
#define Minor3  sos_minor3
#define Minor4  sos_minor4
#define Minor5  sos_minor5

#define Integer lia_const

/*--------------------------------------------------------------------------*/

#define Push(LPTR)     lia_push (LPTR)
#define Times()        lia_times ()
#define Plus()         lia_plus ()
#define Minus()        lia_minus ()
#define Power()        lia_power ()
#define Pop()          lia_popf ()
#define Chs()          lia_negtop ()
#define Negative(LPTR) lia_neg (LPTR)

/*--------------------------------------------------------------------------*/

#define Finish() \
  basic_error ("beyond Finish() in file %s", __FILE__, dimension); \
  return (NULL);
  /* Note: "dimension" is just added above to quiet 'gcc -Wall -pedantic' */

/*--------------------------------------------------------------------------*/

#endif /* #ifndef __SOS_PRIMITIVE__ */
