/* lia/aux.c  ---  Auxiliary routines for Lia. */

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "lia.h"
#include "base.h"

/*--------------------------------------------------------------------------*/

#define FFPLOAD_MAXLEN 20

Lia_info lia_common; /* NOTE: This is global because it's used internally
                        within the Lia Library; however, it isn't supposed
                        to be used from outside. (So, don't tell anybody. :) */

static int initialized = FALSE;

/*--------------------------------------------------------------------------*/

void lia_maximum (int length)
     /* Library initialization.
        Sets ABSOLUTE maximum number of Lia digits for lia_*() results.
        Can be called ONLY ONCE per execution!
        NOTE: A Lia object is Lia x[length] = x[0], x[1], ..., x[length-1]. */
{
  if (initialized)
    basic_error ("lia_maximum: can only be called once");
  lia_common.max = length;
  lia_common.length = length;
  lia_common.last = length - 1;
  basic_counter_reset (&lia_common.mul_calls);
  basic_counter_reset (&lia_common.mul_elops);
  basic_counter_reset (&lia_common.padd_calls);
  basic_counter_reset (&lia_common.padd_elops);
  basic_counter_reset (&lia_common.psub_calls);
  basic_counter_reset (&lia_common.psub_elops);
  lia_common.maximum_digit = 0;
  initialized = TRUE;
}

/*--------------------------------------------------------------------------*/

void lia_length (int length)
     /* Change the maximum length, and if want: library initialization.
        Can be called more then once, but cannot increase the ABSOLUTE maximum,
        either set by lia_maximum() or by FIRST call of lia_length(). */
{
  if (not initialized)
    { /* (better call lia_maximum() first :) */
      lia_maximum (length);
    }
  lia_common.length = length;
  lia_common.last = length - 1;
  if (lia_common.last >= lia_common.max)
    basic_error ("lia_length: length (%d) > maximum (%d)",
                length, lia_common.max);
}

/*--------------------------------------------------------------------------*/

int lia_get_maximum (void)
     /* Returns the absolut maximum length; cf, lia_maximum(). */
{
  return (lia_common.max);
}

/*--------------------------------------------------------------------------*/

int lia_get_length (void)
     /* Returns current maximum length; cf, lia_maximum() and lia_length(). */
{
  return (lia_common.length);
}

/*--------------------------------------------------------------------------*/

int lia_get_beta (void)
     /* Returns the BETA value of the implementation.
        Notice, that BASE == 2^BETA and DBASE == BASE^2. */
{
  return (BETA);
}

/*--------------------------------------------------------------------------*/

Lia_info* lia_info (void)
     /* Returns some Lia info. */
     /* NOTE: The returned address, which points to the info structure,
              is a constant.  DO NOT FREE() IT and consider the fields
              of the structure as read-only. */
{
  static Lia_info li;
  li = lia_common;
  return (&li);
}

/*--------------------------------------------------------------------------*/

void lia_fput (FILE *file, const Lia_obj longi)
     /* Prints internal representation of longi to file. */
{
  int i, ind = last_of (longi);
  fprint (file, "%c:", If ((lia_sign (longi) == -1), '-', '+'));
  downfor (i, ind, 1)
    fprint (file, "%lu:", longi[i]);
  fprint (file, "[%d]", ind);
}

/*--------------------------------------------------------------------------*/

double lia_real (const Lia_obj longi)
     /* Converts longi to floating-point. */
{
  /* NOTES:
     (1) This is not too efficient.
     (2) Is it accurate?  It seems to be, but we could use sprint()/sscan() ??
     (3) A 'double' corresponds to roughly 16 or 17 decimals accuracy. I.e.,
     in 32-bit mode the conversion  from 'double' to 'Lia' is not problematic;
     DBASE==1073741824.  However, in 64-bit mode, DBASE==4611686018427387904,
     19 decimals, and this could be problematic.  So, let's be bit paranoid,
     and slow the whole thing down by a factor of 2...
     */
#if 1 || defined (is_64_bit_ARCH)
  /* New code: Splits Lia digits in half like in lia_mul(); cf. note (3).
   */
  int i, ind = last_of (longi);
  double base = (double) BASE, b = 1.0, r = 0.0;
  Lia a1, a2;
  upfor (i, 1, ind)
    {
      a1 = longi[i] div_BASE;
      a2 = longi[i] mod_BASE;
      ;
      r += ((double) a2) * b;
      b *= base;
      ;
      r += ((double) a1) * b;
      b *= base;
    }
#else
  /* Old code: Works fine for 32-bits; probably also works for 64-bits.
     Empirically, both codes give 15 to 17 decimals accuracy; however,
     the new code above seems to be 1 decimal better "on average," both
     in 32- and 64-bit mode...
     */
  int i, ind = last_of (longi);
  double dbase = (double) DBASE, b = 1.0, r = 0.0;
  upfor (i, 1, ind)
    {
      r += ((double) longi[i]) * b;
      b *= dbase;
    }
#endif
  return (r * (double) lia_sign (longi));
}

/*--------------------------------------------------------------------------*/

int lia_high (const Lia_obj longi)
     /* Returns number of highest lia_digit used in longi:
        longi == [0... lia_high (long)]. */
{
  return (last_of (longi));
}

/*--------------------------------------------------------------------------*/

Lia lia_digit (const Lia_obj longi, int d)
     /* Returns longi[d] without any further checking. */
{
 return (longi[d]);
}

/*--------------------------------------------------------------------------*/

void lia_load (Lia_obj longi, int shorti)
     /* Output: longi. */
     /* Initializes a long integer Lia object: longi := shorti.
        Assumes at least longi[0..2]. */
{
  Lia abs_value = Abs (shorti);
  if (lia_common.last < 2)
    basic_error ("lia_load: length < 3");
  longi[1] = abs_value mod_DBASE;
  longi[2] = abs_value div_DBASE;
  longi[0] = If ((longi[2] == 0), 1 times_2, 2 times_2);
  if (shorti < 0)
    longi[0] ++;
}

/*--------------------------------------------------------------------------*/

void lia_strload (Lia_obj longi, const char string[], const char frmt[])
     /* Output: longi. */
     /* Converts a string, in given format frmt, to a Lia object;
        longi := "%d" or "%x" */
{
  Lia basis[3], ziffer[3];
  char digit[2];
  int d, i = 0;
  int n_flag = FALSE;
  static Lia *aux = NULL;
  if (not aux)
    {
      aux = MALLOC (Lia, lia_common.max);
      MARK (aux, -lia_magic);
    }
  if ((frmt[0] != '%') or (frmt[2] != 0))
    basic_error ("lia_strload: totally wrong frmt: \"%s\"", frmt);
  switch (frmt[1])
    {
     case 'd':
      lia_load (basis, 10);
      break;
     case 'x':
      lia_load (basis, 16);
      break;
     default:
      basic_error ("lia_strload: wrong format: \"%s\"", frmt);
    }
  lia_load (longi, 0);
  digit[1] = 0;
  while (string[i] == ' ')
    i ++;
  if (string[i] == '-')
    {
      n_flag = TRUE;
      i ++;
    }
  else if (string[i] == '+')
    i ++;
  while (isdigit (digit[0] = string[i++]))
    {
      sscan (digit, frmt, &d);
      lia_load (ziffer, d);
      lia_mul (aux, longi, basis);
      lia_add (longi, aux, ziffer);   /* longi := basis * longi + ziffer */
    }
  if (n_flag)
    lia_chs (longi);
}

/*--------------------------------------------------------------------------*/

void lia_ffpload (Lia_obj longi, int w, int a, double value)
     /* Output: longi. */
     /* Floating-point to Lia conversion: longi := int (value * 10^a).
        That is, longi is given value in "fix-point" format "%<w>.<a>f"
        ... MULTIPLIED by 10^a.
        Assuming: a < w <= 16, since 64-bit fp has approx 16 decimal digits. */
{
  char buf[FFPLOAD_MAXLEN];
  sprint (buf, "%0.1f", floor (value * (double) basic_ipower (10, a)));
  if (((int) strlen (buf) > w + 1 + 2) or (w + 1 + 2 + 1 > FFPLOAD_MAXLEN))
    basic_error ("lia_ffpload: overflow: %d.%d, %f, \"%s\"", a, w, value, buf);
  lia_strload (longi, buf, "%d");
}

/*--------------------------------------------------------------------------*/

Lia_ptr lia_const (int i)
     /* Returns pointer to TEMPORARY Lia object representing int i.
        It's the users responsibility to save it via lia_copy() or
        lia_assign(), where needed.  The resulting Lia object will
        need at most 3 Lia digits!  Be careful: Don't ever use the
        resulting pointer other than read-only! */
{
  static Lia aux[3];  /* 3 digits are enough for an int */
  lia_load (aux, i);
  return (aux);
}

/*--------------------------------------------------------------------------*/

void lia_sdiv (Lia_obj result, unsigned long *remainder,
               const Lia_obj longi, long shorti)
     /* Output: result, *remainder. */
     /* Does simple divison  longi / shorti  with  0 < shorti < BASE.
        NOTE: The result and longi arguments may denote the same Lia object! */
{
  Lia i, r, s, t, e, f, u, v, ind = last_of (longi);
  int z_flag = TRUE;  /* for the time being, assume zero */
  int n_flag = is_negative (longi);
  s = DBASE / shorti;
  t = DBASE mod shorti;
  r = 0;
  downfor (i, ind, 1)
    {
      e = longi[i] / shorti;
      f = longi[i] mod shorti;
      u = r * s + e;
      v = r * t + f;   /* NOTE: overflow in v is possible when shorti > BASE */
      result[i] = u + v / shorti;
      r = v mod shorti;
    }
  *remainder = If (n_flag, -r, r);
  downfor (i, ind, 1)
    if (result[i] > 0)
      {
        result[0] = i times_2;
        z_flag = FALSE;
        break;
      }
  if (n_flag)
    result[0] ++;
  if (z_flag)
    result[0] = 2;
}
