/* basic/istaque.c --- Unbounded int stack/queue with dynamic re-allocation. */

/*--------------------------------------------------------------------------*/

#include "basic.h"

/*--------------------------------------------------------------------------*/

#define DO_TYPECHECKING  /* Uncomment this for no run-time type checking. */
#define PRINT_INTERNALS  /* See basic_istaque_print(). */

#define Magic  130862002

#define default_factor  ((double) 2.0)
#define default_shrink  FALSE
 
/* NOTE: It shrinks ONLY WHEN basic_istaque_pop is used.  The used shrinking
         scheme didn't work well for queue (get/push) operations.  In fact,
         automatic shrinking might be always bad, therefore the default. */

#if defined (__DEBUG__) && (! defined (DO_TYPECHECKING))
# define DO_TYPECHECKING
#endif

/*--------------------------------------------------------------------------*/

typedef struct Internal_istaque_type
{
  int magic;
  int *stack, top, stacksize;
  int *bottom, head, tail, botsize;
  double factor;
  int minsize;
  Basic_byte shrink;
} Internal_istaque;

#ifdef DO_TYPECHECKING
  static Internal_istaque *minp = (Internal_istaque *) ~HIBITL;
  static Internal_istaque *maxp = (Internal_istaque *) 0L;
#endif

static int top (const Internal_istaque *q);
static int bot (const Internal_istaque *q);

#define non_empty_stack(Q)  ((Q)->top >= 0)
#define non_empty_bottom(Q)  ((Q)->head < (Q)->tail)
#define clear(Q) do { Q->top = Q->head = Q->tail = -1; } while(0)

/*---------------------------------------------------------------------------*/

#ifdef DO_TYPECHECKING

static Internal_istaque* cast (const Basic_istaque_adt s)
     /* This casts the "abstract data type" Basic_istaque_adt, ie (char *),
        to (Internal_istaque *).  Type checking is done at runtime by
        checking a magic number and minp & maxp. */
{
  Internal_istaque *q = (Internal_istaque *) s;
  Assert_always (q and (minp <= q) and (q <= maxp));
  Assert_always ((q->magic == Magic));
  return (q);
}

#else

#define cast(S)  ((Internal_istaque *) (S))

#endif

/*--------------------------------------------------------------------------*/

Basic_istaque_adt basic_istaque_new (int minsize)
     /* Allocates memory for a new object of Basic_istaque_adt.
        Use basic_istque_dispose() to free the memory. */
{
  Internal_istaque *q;
  if (minsize < 1)
    basic_error ("basic_istaque_new: invalid minsize");
  q = MALLOC (Internal_istaque, 1);
  MARK (q, -Magic);
  q->magic = Magic;
  q->stack = MALLOC (int, minsize);
  MARK (q->stack, -Magic);
  q->bottom = MALLOC (int, 1);  /* just to initialize; why not = NULL ? */
  MARK (q->bottom, -Magic);
  q->stacksize = q->minsize = minsize;
  q->botsize = 0;
  clear (q);
  q->factor = default_factor;
  q->shrink = default_shrink;
#ifdef DO_TYPECHECKING
  minp = (Internal_istaque *) Min (minp, q);
  maxp = (Internal_istaque *) Max (maxp, q);
#endif
  return ((Basic_istaque_adt ) q);
}

/*--------------------------------------------------------------------------*/

void basic_istaque_dispose (Basic_istaque_adt s)
     /* Input/Output: s. */
     /* De-allocates the memory of s.
        NOTE that this does not zero the value of s; ergo what you 
        really want to do is: "basic_istaque_dispose (s); s = NULL". */
{
  Internal_istaque *q = cast (s);
  FREE (q->stack);
  FREE (q->bottom);
  FREE (q);
}

/*--------------------------------------------------------------------------*/

void basic_istaque_clear (Basic_istaque_adt s)
     /* Input/Output: s. */
     /* Clears the contents of s. */
{
  Internal_istaque *q = cast (s);
  clear (q);
}

/*--------------------------------------------------------------------------*/

int basic_istaque_empty (const Basic_istaque_adt s)
     /* Tests if s is empty. */
{
  Internal_istaque *q = cast (s);
  return (not (non_empty_stack (q) or non_empty_bottom (q)));
}

/*--------------------------------------------------------------------------*/

int basic_istaque_top (const Basic_istaque_adt s)
     /* Returns the top element (stack operation!) but does not pop it. */
{
  Internal_istaque *q = cast (s);
  return (top (q));
}

static int top (const Internal_istaque *q)
{
  if (non_empty_stack (q))
    return (q->stack[q->top]);
  else if (non_empty_bottom (q))
    return (q->bottom[q->tail-1]);
  else
    return (0 - MAXINT);
}

/*--------------------------------------------------------------------------*/

int basic_istaque_pop (Basic_istaque_adt s)
     /* Input/Output: s. */
     /* Returns the top element (stack operation!) and pops it. */
{
  Internal_istaque *q = cast (s);
  int result = top (q);
  if (non_empty_stack (q))
    { 
      q->top --;
      if ((q->shrink) and (q->factor > 1) and (q->stacksize > q->minsize)
          and (q->top < q->stacksize / q->factor / q->factor))
        { /* shrink stack */
          int newsize = (int) (q->stacksize / q->factor);
          if (newsize < q->minsize) newsize = q->minsize;
          REALLOC (q->stack, int, newsize);
          MARK (q->stack, -Magic);
          q->stacksize = newsize;
        }
    }
  else if (non_empty_bottom (q))
    q->tail --;    /* we never shrink the bottom */
  else
    basic_error ("basic_istaque_pop: underflow");
  return (result);
}

/*--------------------------------------------------------------------------*/

void basic_istaque_push (Basic_istaque_adt s, int value)
     /* Input/Output: s. */                      
     /* Adds a new top element (stack and queue operation!). */
{
  Internal_istaque *q = cast (s);
  q->top ++;
  if (q->top == q->stacksize)
    if (q->factor <= 1)
      basic_error ("basic_istaque_push: overflow");
    else
      { /* make stack larger */
        int newsize = (int) (0.5 + q->factor * q->stacksize);
        REALLOC (q->stack, int, newsize);
        MARK (q->stack, -Magic);
        q->stacksize = newsize;
      }
  q->stack[q->top] = value;
}

/*--------------------------------------------------------------------------*/

int basic_istaque_bot (const Basic_istaque_adt s)
     /* Returns the bottom element (queue operation!)
        but does not remove it. */
{
  Internal_istaque *q = cast (s);
  return (bot (q));
}

static int bot (const Internal_istaque *q)
{
  if (non_empty_bottom (q))
    return (q->bottom[q->head]);
  else if (non_empty_stack (q))
    return (q->stack[0]);
  else
    return (0 - MAXINT);
}

/*--------------------------------------------------------------------------*/

int basic_istaque_get (Basic_istaque_adt s)
     /* Input/Output: s. */
     /* Returns the bottom element (queue operation!) and removes it. */
{
  Internal_istaque *q = cast (s);
  int result = bot (q);
  if (non_empty_bottom (q))
    q->head ++;
  else if (non_empty_stack (q))
    { /* move stack to bottom */
      int *aux = q->bottom, auxsize = q->botsize;
      q->bottom = q->stack;
      q->head = 1;
      q->tail = q->top + 1;
      q->botsize = q->stacksize;
      q->stack = aux;
      if (q->stacksize <= auxsize)
        q->stacksize = auxsize;
      else
        {
          REALLOC (q->stack, int, q->stacksize);
          MARK (q->stack, -Magic);
        }
      q->top = -1;
    }
  else
    basic_error ("basic_istaque_get: underflow");
  return (result);
}

/*--------------------------------------------------------------------------*/

int basic_istaque_length (const Basic_istaque_adt s)
     /* Returns the number of elements in s. */
{
  Internal_istaque *q = cast (s);
  int len = 0;
  if (non_empty_stack (q))
    len += q->top + 1;
  if (non_empty_bottom (q))
    len += q->tail - q->head;
  return (len);
}

/*--------------------------------------------------------------------------*/

void basic_istaque_print (const Basic_istaque_adt s, FILE *file)
     /* Prints elements of s to given file. */
{
  Internal_istaque *q = cast (s);
  int i;
#ifdef PRINT_INTERNALS
  fprint (file, "Basic_istaque_adt 0x%lx-> (%d;0x%lx,%d,%d;0x%lx,%d,%d,%d) ",
          Addr (q), q->minsize,
          Addr (q->stack), q->top, q->stacksize,
          Addr (q->bottom), q->head, q->tail, q->botsize);
#endif
  if (basic_istaque_empty (s))
    fprint (file, "empty.");
  if (non_empty_bottom (q))
    {
      fprint (file, "%d", q->bottom[q->head]);
      upfor (i, q->head + 1, q->tail - 1) fprint (file, ",%d", q->bottom[i]);
      if (non_empty_stack (q)) fprint (file, ";");
    }
  if (non_empty_stack (q))
    {
      fprint (file, "%d", q->stack[0]);
      upfor (i, 1, q->top) fprint (file, ",%d", q->stack[i]);
    }
}

/*--------------------------------------------------------------------------*/

double basic_istaque_resize (Basic_istaque_adt s, double resize_code)
     /* Input/Output: s. */
     /* Sets the new resize_code and returns the value of the old one.
        The resize factor is abs (resize_code).  If the resize_code is
        negative, shrinking is allowed as well.  The default resize_code
        is +2; meaning that the size of the list is doubled whenever it
        gets filled up.  In this case, the overhead of this semi-dynamic
        solution is O(1) in average. */ 
{
  Internal_istaque *q = cast (s);
  double old = If (q->shrink, -q->factor, q->factor);
  q->factor = resize_code;
  q->shrink = FALSE;
  if (resize_code < 0)
    {
      q->factor = -q->factor;
      q->shrink = TRUE;
    }
  return (old);
}
