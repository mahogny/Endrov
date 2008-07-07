/* lia/test.c --- Minimal Lia test code. */

/*--------------------------------------------------------------------------*/

static char usage[] = "\n\
USAGE: \n\
       %s <OPTION> \n\
OPTIONS: \n\
       -version ........ print version/bitsof() information \n\
       -determinants ... do some 4-by-4 determinant tests \n\
";

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "lia.h"
#include "base.h"

static int okay = 0;
static int pflag = 0;

static Lia_obj new (void);
#define kill(X) lia_pop (LIA_NULL)

static void test1 (Lia_obj a, Lia_obj b, Lia_obj c, Lia_obj d,
                   Lia_obj e, Lia_obj f, Lia_obj g, Lia_obj h,
                   Lia_obj o, Lia_obj p, Lia_obj q, Lia_obj r,
                   Lia_obj w, Lia_obj x, Lia_obj y, Lia_obj z);

/*--------------------------------------------------------------------------*/

void main (int argc, char *argv[])
{
  (void) srandom (basic_seed ());
  if (argc <= 1)
    {
      fprint (stderr, usage, argv[0]);
    }
  else if (strncmp (argv[1], "-version", 2) == 0)
    {
      extern const char lia__version[];
      print ("%s, %s\n", basic_strip (lia__version),
#if defined (is_64_bit_ARCH)
             "64-bit architecture"
#else
             "32-bit architecture"
#endif
             );
      print ("%3d bitsof (int)\n",    bitsof (int));
      print ("%3d bitsof (long)\n",   bitsof (long));
      print ("%3d bitsof (void *)\n", bitsof (void *));
      print ("%3d bitsof (Lia)\n",  bitsof (Lia));
      print ("%3ld BETA, %ld DBASE (%d MAXINT)\n", BETA, DBASE, MAXINT);
    }
  else if (strncmp (argv[1], "-determinants", 2) == 0)
    {
      /* 4-by-4 det(#) ==> 24####, # = irandom^3 = 28 digits ==> 114 digits*/
      int digs = Lia_DIGITS (114);
      print ("Running on %d Lia digits\n", digs);
      lia_maximum (digs);
      lia_det ();
      ;
      { /* run tests on random matrix */
        Lia_obj a = new (), b = new (), c = new (), d = new ();
        Lia_obj e = new (), f = new (), g = new (), h = new ();
        Lia_obj o = new (), p = new (), q = new (), r = new ();
        Lia_obj w = new (), x = new (), y = new (), z = new ();
        int i;
        upfor (i, 1, 2000)
          {
            if (i mod 666 == 0)
              pflag = 1;
            test1 (a, b, c, d,
                   e, f, g, h,
                   o, p, q, r,
                   w, x, y, z);
            pflag = 0;
          }
      }
      print ("Okay: %d.\n", okay);
    }
}

/*--------------------------------------------------------------------------*/

static int irandom (void)
     /* gets random number, but truncates it to int (32-bit) */
{
  return ((int) random ());
}

/*--------------------------------------------------------------------------*/

static void mrandom (Lia_obj a)
     /* Loads irandom()^3 into a. */
{
  Lia_obj one =   lia_pushf (LIA_NULL);
  Lia_obj two =   lia_pushf (LIA_NULL);
  Lia_obj three = lia_pushf (LIA_NULL);
  lia_load (one  , irandom ());
  lia_load (two  , irandom ());
  lia_load (three, irandom ());
  lia_times ();
  lia_times ();
  lia_pop (a);
}

/*--------------------------------------------------------------------------*/

static Lia_obj new (void)
     /* Returns pointer to a new Lia_obj, with random int value,
        allocated on the Lia_stack. */
{
  Lia_obj x = lia_pushf (LIA_NULL);
  mrandom (x);
  return (x);
}

/*--------------------------------------------------------------------------*/

static void randomize (Lia_obj a, Lia_obj b, Lia_obj c, Lia_obj d,
                       Lia_obj e, Lia_obj f, Lia_obj g, Lia_obj h,
                       Lia_obj o, Lia_obj p, Lia_obj q, Lia_obj r,
                       Lia_obj w, Lia_obj x, Lia_obj y, Lia_obj z)
     /* Load new random integers into given matrix. */
{
  mrandom (a);  mrandom (b);  mrandom (c);  mrandom (d);
  mrandom (e);  mrandom (f);  mrandom (g);  mrandom (h);
  mrandom (o);  mrandom (p);  mrandom (q);  mrandom (r);
  mrandom (w);  mrandom (x);  mrandom (y);  mrandom (z);
}

/*--------------------------------------------------------------------------*/

static void test1 (Lia_obj a, Lia_obj b, Lia_obj c, Lia_obj d,
                   Lia_obj e, Lia_obj f, Lia_obj g, Lia_obj h,
                   Lia_obj o, Lia_obj p, Lia_obj q, Lia_obj r,
                   Lia_obj w, Lia_obj x, Lia_obj y, Lia_obj z)
{
  Lia_obj r1 = new ();
  Lia_obj r2 = new ();
  ;
  lia_det4 (a, b, c, d,
            e, f, g, h,
            o, p, q, r,
            w, x, y, z, r1);  /* D1 */
  ;
  lia_det4 (a, b, d, c,
            o, p, r, q,
            e, f, h, g,
            w, x, z, y, r2);  /* D2: one row & one column swapped */
  ;
  Assert_always (lia_eq (r1, r2));
  okay ++;
  if (pflag)
    {
      double fp = lia_real (r1);
      lia_clear ();
      print ("\nDET:  _.23456789o_23456\nFP:  %s%.18e\nLIA:  %s [%d chars]\n",
             If ((fp < 0), "", "+"), fp,
             lia_deci (r1), strlen (lia_deci (r1)));
    }
  ;
  kill (r2);
  kill (r1);
  randomize (a, b, c, d,
             e, f, g, h,
             o, p, q, r,
             w, x, y, z);
}
