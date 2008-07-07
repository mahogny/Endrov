/* lia/lia.h  ---  Headerfile to the Lia (Long-integer arithmetic) package. */

#ifndef __LIA_H__  /* Include only once! */
#define __LIA_H__

/*--------------------------------------------------------------------------*/

#include "basic.h"

/*--------------------------------------------------------------------------*/

typedef unsigned long Lia;

typedef Lia* Lia_obj;
typedef Lia_obj Lia_ptr;  /* Synonyms! */
        /* Note: "typedef Lia[] Lia_obj" would be more consistent
           with this implementation but it produced syntax error. */

typedef struct Lia_info_type
{
  Basic_counter mul_calls, mul_elops;
  Basic_counter padd_calls, padd_elops;
  Basic_counter psub_calls, psub_elops;
  int maximum_digit;
  /* Below fields are private. */
  int max, length;
  int last;   /* A lia-object has the form: Lia longi[0..last], last < max. */
} Lia_info;

#if defined (is_64_bit_ARCH)
# define Lia_DIGITS(D)  (((D) / 18) + 1 + 1)
#else
# define Lia_DIGITS(D)  (((D) /  9) + 1 + 1)
#endif
/* This macro computes the number of Lia digits needed to represent upto
   D decimal digits.  NOTE: It's a slightly conservative estimate; eg, for
   32-bit architectures, Lia digits hold upto 2^30 == 1073741824 (see base.h),
   but we count it just as 9 decimal digits. */

/*--------------------------------------------------------------------------*/

/* lia/lia.c */

void lia_assign (Lia_obj long1, const Lia_obj long2);

#define lia_copy(A,B)  lia_assign (B, A)

void    lia_chs  (Lia_obj longi);
Lia_ptr lia_neg  (Lia_obj longi);
int     lia_sign (const Lia_obj longi);

int lia_eq  (const Lia_obj long1, const Lia_obj long2);
int lia_le  (const Lia_obj long1, const Lia_obj long2);
int lia_leq (const Lia_obj long1, const Lia_obj long2);

void lia_add (Lia_obj longi, const Lia_obj long1, const Lia_obj long2);
void lia_sub (Lia_obj longi, const Lia_obj long1, const Lia_obj long2);
void lia_mul (Lia_obj longi, const Lia_obj long1, const Lia_obj long2);

/*--------------------------------------------------------------------------*/

/* lia/aux.c */

void lia_maximum (int length);
void lia_length  (int length);

int  lia_get_maximum (void);
int  lia_get_length  (void);
int  lia_get_beta    (void);

Lia_info* lia_info   (void);

void   lia_fput (FILE *file, const Lia_obj longi);
double lia_real (const Lia_obj longi);

int lia_high  (const Lia_obj longi);
Lia lia_digit (const Lia_obj longi, int d);

void lia_load    (Lia_obj longi, int shorti);
void lia_strload (Lia_obj longi, const char string[], const char frmt[]);
void lia_ffpload (Lia_obj longi, int w, int a, double value);

Lia_ptr lia_const (int i);

void lia_sdiv(Lia_obj result, unsigned long *remainder,
              const Lia_obj longi, long shorti);

/*--------------------------------------------------------------------------*/

/* lia/chars.c */

void  lia_clear (void);
char* lia_chars (const Lia_obj longi, int decimal_flag);

#define lia_deci(L)  lia_chars (L, TRUE)
#define lia_hexa(L)  lia_chars (L, FALSE)

/*--------------------------------------------------------------------------*/

/* lia/stack.c */

#define LIA_NULL  ((Lia_ptr) NULL)

void    lia_stack_limit (int new_limit);
int     lia_stack_size  (void);
int     lia_stack_depth (void);
int     lia_stack_empty (void);
void    lia_push        (const Lia_obj longi);
Lia_ptr lia_pushf       (const Lia_obj longi);
void    lia_pushtop     (void);
void    lia_pop         (Lia_obj longi);
Lia_ptr lia_popf        (void);
Lia_ptr lia_topf        (void);
void    lia_times       (void);
void    lia_plus        (void);
void    lia_minus       (void);
void    lia_power       (void);
void    lia_ipower      (int p);
void    lia_negtop      (void);

/*---------------------------------------------------------------------------*/

/* lia/pool.c */

typedef char* Lia_pool_adt;  /* Abstract data type! */

typedef struct lia_pool_info_record
{
  int longs, digits, blocks;
  unsigned long bytes;
} Lia_pool_info;

Lia_pool_adt   lia_pool_open  (int block_size);
void           lia_pool_close (Lia_pool_adt pid);
void           lia_pool_kill  (Lia_pool_adt pid);

Lia_ptr lia_pool_store (Lia_pool_adt pid, const Lia_obj longi, int minlen);

Lia_pool_info* lia_pool_info (const Lia_pool_adt pid);

/*--------------------------------------------------------------------------*/

/* lia/det.c */

#define INp const Lia_obj
#define OUTp      Lia_obj 

void lia_det  (void);

void lia_det2 (INp a, INp b,
               INp c, INp d, OUTp result);

void lia_det3 (INp x11, INp x12, INp x13,
               INp x21, INp x22, INp x23,
               INp x31, INp x32, INp x33, OUTp result);

void lia_det4 (INp x11, INp x12, INp x13, INp x14,
               INp x21, INp x22, INp x23, INp x24,
               INp x31, INp x32, INp x33, INp x34,
               INp x41, INp x42, INp x43, INp x44, OUTp result);

#undef INp
#undef OUTp

/*--------------------------------------------------------------------------*/

/*           ..............................................................
             [Seventh Commandment for C Programmers]   Thou shalt study thy
             libraries and strive not to re-invent them without cause, that
             thy code may be short and readable and thy days  pleasant  and
             productive.                                    --Henry Spencer
             .............................................................. */

#endif  /* #ifndef __LIA_H__ */
