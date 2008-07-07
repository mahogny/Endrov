/* lia/stack.c  ---  A global pool for Lia objects. */

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "lia.h"
#include "base.h"

/*---------------------------------------------------------------------------*/

#define DO_TYPECHECKING  /* Uncomment this for no run-time type checking. */

#define Magic_number  130862004

#define MIN_BLOCK_SIZE  10

#if defined (__DEBUG__) && (! defined (DO_TYPECHECKING))
# define DO_TYPECHECKING
#endif

/*--------------------------------------------------------------------------*/

typedef struct block_record
{
  Lia *storage;
  int first;
  int last;
  struct block_record *next;
} Block;

typedef struct pool_record
{
  int magic;
  int block_digits;
  Block *block_list;
  int longs;
  int digits;
} Pool;

static Lia a_zero[3];
static int is_virgin = TRUE;

static Block* new_block (int digits);
static void kill_block  (Block *b);

#ifdef DO_TYPECHECKING
 static Pool *minp = (Pool *) ~HIBITL;
 static Pool *maxp = (Pool *) 0L;
#endif

/*---------------------------------------------------------------------------*/

#ifdef DO_TYPECHECKING

static Pool* cast (const Lia_pool_adt pid)
{
  Pool *p = (Pool *) pid;
  Assert_always (p and (minp <= p) and (p <= maxp));
  Assert ((p->magic == Magic_number)
          and (p->block_digits >= MIN_BLOCK_SIZE * lia_common.max));
  return (p);
}

#else

#define cast(PID)  ((Pool *) (PID))

#endif

/*---------------------------------------------------------------------------*/

Lia_pool_adt lia_pool_open (int block_size)
     /* Opens a Lia pool and returns a pointer (ADT) to it.
        A "Lia pool" is essentially a list of blocks of n == block_size
        Lia digits.  This list provides memory space for lia_pool_store().
        Initially, there is only one block allocated.  If this block gets
        filled up, a new one is allocated. */
{
  Pool *p;
  p = MALLOC (Pool, 1);
  MARK (p, lia_magic);
  p->magic = Magic_number;
  p->block_digits = Max (block_size, MIN_BLOCK_SIZE) * lia_common.max;
  p->block_list = new_block (p->block_digits);
  p->longs = 0;
  p->digits = 0;
  if (is_virgin)
    {
      lia_load (a_zero, 0);
      is_virgin = FALSE;
    }
#ifdef DO_TYPECHECKING
  minp = (Pool *) Min (minp, p);
  maxp = (Pool *) Max (maxp, p);
#endif
  return ((Lia_pool_adt) p);
}

/*---------------------------------------------------------------------------*/

Lia_ptr lia_pool_store (Lia_pool_adt pid, const Lia_obj longi, int minlen)
     /* Input/Output: pid. */
     /* This is the core function of this module.  It allocates an appropriate
        number of Lia digits from the given Lia pool, copies the contents of
        longi into them, and returns a pointer to the resulting Lia object.
        It allocates either minlen digits, or the same number of digits as
        in longi, whichever is larger.  Eg, set minlen = 0, if you want to
        use the resulting object as a constant; otherwise, it is best to use
        minlen = lia_get_maximum (). */
{
  if (not longi)
    return (lia_pool_store (pid, a_zero, minlen));
  else
    {
      Pool *p = cast (pid);
      Block *b = p->block_list;
      int high = Max (minlen + 1, lia_high (longi));
      Lia_ptr r;
      Assert (longi and b and (b->last == p->block_digits - 1));
      if (b->first + high > b->last)
        { /* add new block */
          b = new_block (p->block_digits);
          b->next = p->block_list;
          p->block_list = b;
        }
      r = &(b->storage[b->first]);
      b->first += high + 1;
      lia_assign (r, longi);
      p->longs ++;
      p->digits += high;
      return (r);
    }
}

/*---------------------------------------------------------------------------*/

void lia_pool_close (Lia_pool_adt pid)
     /* Input/Output: pid. */
     /* Returns all the unused memory of the current block to the system. */
{
  Pool *p = cast (pid);
  Block *b = p->block_list;
  b->last = b->first - 1;
  REALLOC (b->storage, Lia, b->last + 1);
  MARK (b->storage, lia_magic);
}

/*---------------------------------------------------------------------------*/

void lia_pool_kill (Lia_pool_adt pid)
     /* Input/Output: pid. */
     /* Destroys the given Lia pool. */
{
  Pool *p = cast (pid);
  kill_block (p->block_list);
  p->magic = 0;  /* to be on the "save" side */
  FREE (p);  
}

/*---------------------------------------------------------------------------*/

Lia_pool_info* lia_pool_info (const Lia_pool_adt pid)
     /* Returns some info about the Lia pool. */
     /* NOTE: The returned address, which points to the info structure,
              is a constant.  DO NOT FREE() IT and consider the fields
              of the structure as read-only. */
{
  static Lia_pool_info inf;
  Pool *p = cast (pid);
  Block *b = p->block_list;
  inf.longs = p->longs;
  inf.digits = p->digits;
  inf.blocks = 0;
  inf.bytes = 0L;
  while (b)
    {
      inf.blocks ++;
      inf.bytes += (b->last + 1) * sizeof (Lia);
      b = b->next;
    }
  return (&inf);
}

/*---------------------------------------------------------------------------*/

static Block* new_block (int digits)
     /* Allocate a new block. */
{
  Block *b;
  b = MALLOC (Block, 1);

  MARK (b, lia_magic);
  b->storage =  MALLOC (Lia, digits);
  /* >>> BZERO (b->storage,    Lia, digits);  * not needed? */
  MARK (b->storage, lia_magic);
  b->first = 0;
  b->last = digits - 1;
  b->next = NULL;
  return (b);
}

/*---------------------------------------------------------------------------*/

static void kill_block (Block *b)
     /* Input/Output: *b. */
     /* Free b, pointing to a (list of) block(s). */
{
  if (b)
    {
      kill_block (b->next);
      FREE (b->storage);
      FREE (b);
    }
}
