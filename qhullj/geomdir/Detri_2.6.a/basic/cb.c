/* basic/cb.c --- A (stack of) dynamically sized char buffer(s). */

/*---------------------------------------------------------------------------*/

#include "basic.h"

/*--------------------------------------------------------------------------*/

/*        This module uses code from cb_doprnt.c (and cb_doscan.c)
               which was originally written by Steve Summit.                */

/*---------------------------------------------------------------------------*/

#define Magic  130862001

#define STACK_SIZE    10
#define INITIAL_SIZE  128
#define REALLOC_FACT  2
#define UPPER_LIMIT   MAXINT

typedef struct cbuf_struct
{
  char *str;
  int size, last, first;
} Cbuf;

/* The static (stack of) char buffer(s). */
#define EMPTY  {NULL, 0, -1, -1}
static Cbuf buffer_stack[STACK_SIZE] = { EMPTY };
static int top = 0;
#define Top_Buffer buffer_stack[top]

/*---------------------------------------------------------------------------*/

void basic_cb_putc (char c)
     /* Appends c to the buffer.
        Will re-allocate and enlarge the buffer if necessary! */
{
  if (Top_Buffer.last == Top_Buffer.size - 1)
    { /* buffer is full */
      if (not Top_Buffer.str)
        { /* hey, it's not even initialized, yet */
          Top_Buffer.size = INITIAL_SIZE;
          Top_Buffer.str = MALLOC (char, Top_Buffer.size);
          MARK (Top_Buffer.str, -Magic);
          Top_Buffer.last = 0;
          Top_Buffer.first = 0;
          Top_Buffer.str[Top_Buffer.last] = '\0';
        }
      else if (Top_Buffer.size < UPPER_LIMIT)
        { /* enlarge size and re-allocate */
          Top_Buffer.size *= REALLOC_FACT;
          if ((Top_Buffer.size < 1) or (Top_Buffer.size > UPPER_LIMIT))
            Top_Buffer.size = UPPER_LIMIT;
          REALLOC (Top_Buffer.str, char, Top_Buffer.size);
          MARK (Top_Buffer.str, -Magic);
        }
      else
        basic_error ("basic_cb: overflow (reached upper limit %d)\n",
                     UPPER_LIMIT);
    }
  Assert ((Top_Buffer.str[Top_Buffer.last] == '\0')
          and (Top_Buffer.first <= Top_Buffer.last));
  Top_Buffer.str[Top_Buffer.last] = c;
  Top_Buffer.last ++;
  Top_Buffer.str[Top_Buffer.last] = '\0';
}

/*---------------------------------------------------------------------------*/

char* basic_cb_str (void)
     /* Returns a pointer to the string of the buffer.
        (NOTE that the buffer might be reallocated and/or changed, so
        you might consider copying the string to a "save" place. :-) */
{
  return (&(Top_Buffer.str[Top_Buffer.first]));
}

/*---------------------------------------------------------------------------*/

void basic_cb_clear (void)
     /* Clears the buffer. */
{
  basic_cb_putc ('\0');  /* just to initialize, if necessary */
  Top_Buffer.last = 0;
  Top_Buffer.first = 0;
  Top_Buffer.str[Top_Buffer.last] = '\0';
}

/*---------------------------------------------------------------------------*/

int basic_cb_len (void)
     /* Well... guess what, it returns the strlen() of the buffer string. */
{
  return (Top_Buffer.last - Top_Buffer.first);
}

/*---------------------------------------------------------------------------*/

int basic_cb_size (void)
     /* Returns the current size of the buffer */
{
  return (Top_Buffer.size);
}

/*---------------------------------------------------------------------------*/

int basic_cb_addalinef (FILE *file)
     /* Reads chars from given file until '\n' or EOF is reached and
        APPENDS them to the buffer (including the '\n').
        NOTE:
        - If EOF is reached while reading, a '\n' is appended.  ????
        - It returns the number of chars added to the buffer string,
          which is 0 if the file is at EOF. */
{
  int c, i = 0;
  loop
    {
      c = getc (file);
      if (c == EOF) break;
      basic_cb_putc (c);
      i ++;
      if (c == '\n') break;
    }
  return (i);
}

/*--------------------------------------------------------------------------*/

char* basic_cb_getline (FILE *file)
     /* Reads a line from the given file and returns a pointer to
        the corresponding string.  Use STRDUP() if necesssary!
        NULL is returned when EOF is reached.  Cf, basic_cb_addalinef(). */
{
  char *result;
  int io;
  basic_cb_push_buffer ();
  io = basic_cb_addalinef (file);
  result = If ((io > 0), basic_cb_str (), (char *) NULL);
  basic_cb_pop_buffer ();
  return (result);
}

/*--------------------------------------------------------------------------*/

int basic_cb_printf (const char *frmt, ...)
       /* This acts like a printf() function on the buffer!
          It returns number of chars added to the buffer.
          NOTE: THIS MIGHT BE BUGGY!
          Variable argument lists don't necessarily work recursively!
          (I'm probably doing something wrong with <stdarg.h> ... but what?)
          Anyways, basic_cb_doprnt works fine, so I defined the macro
          basic_cb_vprintf as basic_cb_doprnt in basic.h. */
{
  va_list argp;
  int r;
  va_start (argp, frmt);
  r = basic_cb_doprnt (frmt, argp);
  va_end (argp);
  return (r);
}

/*---------------------------------------------------------------------------*/

char* basic_cb_frmt (const char *frmt, ...)
       /* Same as basic_cb_printf(), but returns pointer to resulting string.
          NOTE: Use STRDUP() if you need this string later.
          ALSO: "print ("%s %s\n", basic_cb_frmt (...), basic_cb_frmt (...))
          does not work. :( */
{
  va_list argp;
  char *result;
  basic_cb_push_buffer ();
  va_start (argp, frmt);
  (void) basic_cb_doprnt (frmt, argp);
  va_end (argp);
  result = basic_cb_str ();
  basic_cb_pop_buffer ();
  return (result);
}

/*--------------------------------------------------------------------------*/

void basic_cb_push_buffer (void)
     /* Pushes a new buffer on the stack and clears it. */
{
  top ++;
  if (top == STACK_SIZE)
    basic_error ("basic_cb_push_buffer: overflow");
  if ((Top_Buffer.str == NULL) and (Top_Buffer.size == 0)
      and (Top_Buffer.last == 0) and (Top_Buffer.first == 0))
    { /* still need to initialize this element as EMPTY */
      static Cbuf empty = EMPTY;
      Top_Buffer = empty;
    }
  basic_cb_clear ();
}

/*--------------------------------------------------------------------------*/

void basic_cb_pop_buffer (void)
     /* Pops the current buffer, but leaves it as it is...
        until the next basic_cb_push_buffer (). */
{
  top --;
  if (top < 0)
    basic_error ("basic_cb_pop_buffer: underflow");
}
