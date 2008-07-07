/* basic/basic.c --- Basic C Library, core routines and version information. */

/*---------------------------------------------------------------------------*/

const char basic__version[] = "@(#) Basic C Library 1.6";
const char basic__authors[] = "Ernst Mucke, et al";
#ifdef __DEBUG__
const char basic__compile[] = "@(#) \t w/ -D__DEBUG__";
#endif

/*---------------------------------------------------------------------------*/

#include "basic.h"

/*---------------------------------------------------------------------------*/

const Basic_byte basic_charbit_mask__[8] = 
{
  (Basic_byte) 128, (Basic_byte) 64, (Basic_byte) 32, (Basic_byte) 16,
  (Basic_byte)   8, (Basic_byte)  4, (Basic_byte)  2, (Basic_byte)  1
};

const Basic_byte basic_charbit_mask_n[8] =
{
  (Basic_byte)~128, (Basic_byte)~64, (Basic_byte)~32, (Basic_byte)~16,
  (Basic_byte)~  8, (Basic_byte)~ 4, (Basic_byte)~ 2, (Basic_byte)~ 1
};

/* NOTE: The masks must be global.  I don't see any reasonable way around it,
   except, giving up the macros. */

/* The headerfile basic.h defines the following macros:

   * macro basic_charbit_on(I,C) returns TRUE iff bit I in char C is set to 1;
   * macro basic_charbit_s1(I,C) sets bit I in char C to 1;
   * macro basic_charbit_s0(I,C) sets bit I in char C to 0;
     usage eg.:  c = basic_charbit_s1 (2,c);
   */

/*---------------------------------------------------------------------------*/

static void (* hook) (const char []) = NULL;

/* Global constants used in definitions in basic.h  --- don't tell anybody! */
const char basic__assert_frmt[] = "Assertion failed, file \"%s\", line %d.";
void (* basic__null_hook) () = NULL;  /* NOTE: "const void blah blah...
                                         gives a warning.  Why? */

/*--------------------------------------------------------------------------*/

void basic_error (const char *frmt, ...)
     /* Procedure basic_error (frmt [, arg] ...) has a variable number
        of arguments.  It produces an message just like fprint or vfprint.
        Then it either aborts execution or calls a given error hook. */
{ 
  va_list argp;
  char *msg;
  va_start (argp, frmt);
  basic_cb_push_buffer ();
  (void) basic_cb_vprintf (frmt, argp);
  msg = STRDUP (basic_cb_str ());
  basic_cb_pop_buffer ();
  va_end (argp);
  if (hook)
    { /* call hook */
      hook (msg);
    }
  else
    { /* print error message and abort execution (with coredump) */
      fprint (stderr, "%s\n", msg);
#ifdef sgi
      (void) abort ();
#else
      abort ();
#endif      
    }
  FREE (msg);
}  

/*--------------------------------------------------------------------------*/

void basic_error_hook (void (* user_error) (const char msg[]))
     /* To specify an error hook call basic_error_hook (my_error) assuming
        my_error is a pointer to a function:
        .
        .            void my_error (const char error_message[])
        .            { ... }
        .
        With this, basic_error() will generate the error_message and pass it
        to my_error().  This function might then do some cleanup and/or cause
        segmentation fault for dbx.  Use basic_error_hook (NULL_HOOK) to get
        default behaviour back. */
{
  hook = user_error;
}

/*---------------------------------------------------------------------------*/

unsigned long basic_kbytes (unsigned long bytes)
     /* Converts bytes to kilobytes (going to next higher integer). */
{
  return (If ((bytes > 0), bytes / 1024 + 1, bytes));
}

/*--------------------------------------------------------------------------*/

double basic_mbytes (unsigned long bytes)
     /* Converts bytes to megabytes. */
{
  return ((double) bytes / 1024.0 / 1024.0);
}

/*--------------------------------------------------------------------------*/

char* basic_strip  (const char *strng)
     /* Returns a pointer to the first character in strng that is not
        a special  character, or NULL  if no such  character exists.
        Cf, man  strpbrk(). */
{ return ((char*)
     strpbrk
          (strng,
           "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"));
}

/*--------------------------------------------------------------------------*/

void basic_system (const char *frmt, ...)
{ 
  va_list argp;
  char *cmd;
  int result;
  va_start (argp, frmt);
  basic_cb_push_buffer ();
  (void) basic_cb_vprintf (frmt, argp);
  cmd = STRDUP (basic_cb_str ());
  basic_cb_pop_buffer ();
  va_end (argp);
  result = system (cmd);
  if (result != 0)
    print ("WARNING: basic_system(\"%s\") status=%d\n", cmd, result);
  FREE (cmd);
}

/*--------------------------------------------------------------------------*/

int basic_types_okay (void)
     /* Returns TRUE iff the size of base types is satisfactory;
        otherwise, issues a fatal error warning... */
{
  int okay = TRUE;
  ;
  if ((bitsof (char) != 8) or (bitsof (short) != 16) or (bitsof (int) != 32))
    okay = FALSE;
  else if (not ((bitsof (void *) == 32) or (bitsof (void *) == 64)))
    okay = FALSE;
  else if (not ((bitsof (long) == 32) or (bitsof (long) == 64)))
    okay = FALSE;
  else if (bitsof (long) != bitsof (void *))
    okay = FALSE;
#if defined (__LONGLONG)
  else if (not ((bitsof (long long) == 32) or (bitsof (long long) == 64)
                or (bitsof (long long) == 128)))
    okay = FALSE;
#endif
  else if (bitsof (float) != 32)
    okay = FALSE;
  else if (bitsof (double) != 64)
    okay = FALSE;
  ;
  if (not okay)
    {
#define PRT(B_STR,B_SIZE,SHOULD_BE) \
      fprint (stderr, "%4d == %s ... should be %s\n", B_SIZE, B_STR, SHOULD_BE)
      ;
      fprint (stderr, "\nFATAL ERROR: basic_types_okay() returns FALSE!\n");
      fprint (stderr, "\n");
      PRT ("bitsof (char)     ", bitsof (char),   " 8");
      PRT ("bitsof (short)    ", bitsof (short),  "16");
      PRT ("bitsof (int)      ", bitsof (int),    "32");
      fprint (stderr, "\n");
      PRT ("bitsof (long)   | ", bitsof (long)  , "32 or 64");
      PRT ("bitsof (void *) | ", bitsof (void *), "32 or 64");
      fprint (stderr, "\n");
#if defined (__LONGLONG)
      PRT ("bitsof (long long)", bitsof (long long), "32 or 64 or 128");
#endif
      fprint (stderr, "\n");
      PRT ("bitsof (float)    ", bitsof (float),     "32");
      PRT ("bitsof (double)   ", bitsof (double),    "64");
      fprint (stderr, "\n");
#undef PRT
    }
  return (okay);
}
