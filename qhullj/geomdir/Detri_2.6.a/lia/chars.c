/* lia/chars.c  --- Lia output and buffering. */

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "lia.h"
#include "base.h"

/*--------------------------------------------------------------------------*/

/* Lia buffer space used here (and, in the old days, in lia/mint.c) */
/* NOTE: Right now, the buffer has fixed size... and this is ugly! */

#define CBUFLEN 2000

static char lia_buffer[CBUFLEN];
static int  lia_buffer_pointer = 0;

/*--------------------------------------------------------------------------*/

void lia_clear (void)
        /* Resets the lia_buffer[]. */
{
  lia_buffer_pointer = 0;
}

/*--------------------------------------------------------------------------*/

static void lia_cput (char c)
     /* Adds c to lia_buffer[] and checks for overflow.
        Internal use only! */
{
  lia_buffer[lia_buffer_pointer] = c;
  lia_buffer_pointer ++;
  if (lia_buffer_pointer > CBUFLEN)
    basic_error ("lia: buffer overflow; use lia_clear()");
}

/*--------------------------------------------------------------------------*/

static void lia_dput (Lia_obj longi, int decimal_flag)
     /* Input/Output: longi. */
     /* Puts decimal (or hexadecimal) representation of longi[] into cbbuf[].
        Code is sort of weird (recursive!) to avoid additional buffer space.
        NOTE: - longi[] will destroyed.
              - longi[] > 0 is assumed.
        Internal use only! */
{
  unsigned long part;
  char string[5];
  lia_sdiv (longi, &part, longi, If (decimal_flag, 1000L, 0x1000L));
  sprint (string, If (decimal_flag, "%03lu", "%03lx"), part);
  if ((longi[0] > 2) or (longi[1] > 0))
    lia_dput (longi, decimal_flag);
  lia_cput (string[0]);
  lia_cput (string[1]);
  lia_cput (string[2]);
}

/*--------------------------------------------------------------------------*/

char* lia_chars (const Lia_obj longi, int decimal_flag)
     /* Returns a pointer to a char string containing the decimal or
        hexadecimal representation of longi.  Usually this should work within
        a print statement even when using more than one lia_chars() calls.
        However, the bufferspace lia_buffer[] is limitied by CBUFLEN and the
        caller is highly advised to call lia_clear() once in a while, or even
        after each such print statement.  (:-)
        NOTE: - If you want to save the string, you will have to copy it
                into one of your own string variables since some other Lia
                procedures might destroy the buffer.
              - Please USE THE MACROS lia_deci() and lia_hex(), as defined in
                lia.h, instead of calling lia_chars() directly! */
{
  int p;
  static Lia *buffer = NULL;
  if (not buffer)
    {
      buffer = MALLOC (Lia, lia_common.max);
      MARK (buffer, -lia_magic);
    }
  lia_assign (buffer, longi);
  p = lia_buffer_pointer; 
  if (lia_sign (longi) == 0)
    {
      lia_cput ('+');
      lia_cput ('0');
      lia_cput ('\0');
      return (& (lia_buffer[p]));
    }
  if (lia_sign (buffer) == -1)
    {
      lia_cput ('-');
      lia_chs (buffer);
    }
  else
    lia_cput ('+');
  lia_dput (buffer, decimal_flag); /* print to lia_buffer[] & destroy buffer */
  lia_cput ('\0');
  while (lia_buffer[++p] == '0')
    lia_buffer[p] = lia_buffer[p-1];   /* get rid of +00xxxxx etc */
  return (& (lia_buffer[p-1]));
}
