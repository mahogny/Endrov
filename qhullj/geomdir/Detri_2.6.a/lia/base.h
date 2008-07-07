/* lia/base.h  ---  Internal macros and constants for Lia package. */

#ifndef __LIA_BASE_H__  /* Include this file only once! */
#define __LIA_BASE_H__ 

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "lia.h"

/*---------------------------------------------------------------------------*/

/* Define "length" of Lia digits.
   NOTE: "typedef unsigned long Lia" usually makes Lia 32 bits long, which
   gives BETA == 15 since lia_mul() needs 2 bits to catch overflows. */

#define  BETA ((bitsof (Lia) - 2) / 2)
#define  BASE ((Lia) powerof2 (     BETA))
#define DBASE ((Lia) powerof2 (2L * BETA))

extern const int lia_magic;

/*--------------------------------------------------------------------------*/

/* fast mod, /, and * operations */

#define mod_BASE    & (BASE - 1L) /* a mod_BASE = a (bitwise AND) (BASE - 1) */
#define div_BASE   >> BETA       /* a div_BASE = a (shift right) log2 BASE  */
#define times_BASE << BETA
#define mod_DBASE   & (DBASE - 1L)
#define div_DBASE  >> (2 * BETA)

/*--------------------------------------------------------------------------*/

#define div_2    >> 1L
#define mod_2     & 1L
#define times_2  << 1L

#define  is_nonzero(LONG)  ((int) ((LONG[1] != 0L) or ((LONG[0] div_2) > 1L)))
#define is_negative(LONG)  ((int) (LONG[0] mod_2))
#define     last_of(LONG)  ((int) (LONG[0] div_2))

#define chs(LONG) \
do { \
     if (is_negative (LONG)) \
       LONG[0] --; \
     else \
       LONG[0] ++; \
   } once
   /* NOTE: This macro changes sign bit *even* if LONGI is zero! */

/*--------------------------------------------------------------------------*/

extern Lia_info lia_common;  /* from lia/aux.c */

/*--------------------------------------------------------------------------*/

#endif  /* #ifdef __LIA_BASE_H__ */
