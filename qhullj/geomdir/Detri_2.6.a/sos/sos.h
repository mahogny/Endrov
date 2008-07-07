/* sos/sos.h  ---  SoS Library headerfile. */

#ifndef __SOS_H__  /* Include only once! */
#define __SOS_H__  1

/* NOTE: 'minor' seems to be macro definition in <*.h> files on SUNs. */
#if defined (sun)
# ifdef minor
#  undef minor
# endif
#endif

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "lia.h"

/*--------------------------------------------------------------------------*/

/* sos.c */

void          sos_matrix   (int n0, int d, double scale, int len, int lenp);
int           sos_max      (void);
unsigned long sos_bytes    (void);
void          sos_shutdown (void);
int           sos_is_down  (void);
void          sos_param    (int i, int j, const Lia_obj longi);
void          sos_enlarge  (double factor);
void          sos_grow     (int amount);
Lia_ptr       sos_lia      (int i, int j);
double        sos_fp       (int i, int j);
double        sos_fp_0_d_plus_1 (void);
double        sos_scale    (void);

#ifdef __DEBUG__
 /* Globally accessible flags! */
 extern int sos_test_flag;
 extern int sos_proto_flag;
 extern int sos_proto_e_flag;
#endif

/* Uncomment the following line if you want __SOS_TRACE__ */
/* #define __SOS_TRACE__ */
#ifdef __SOS_TRACE__
 /* Globally accessible trace file!  For debugging only! */
 extern FILE *sos_trace_file;
#endif

/*--------------------------------------------------------------------------*/

/* predicates */

int  sos_smaller           (int i, int j, int k, int l);
int  sos_smaller_dist_abcd (int i, int k,
                           const Lia_ptr a, const Lia_ptr b, const Lia_ptr c);

int  sos_in_sphere         (int o, int i, int j, int k, int p);
int  sos_in_sphere_p       (int o, int i, int j, int k, int p);

int  sos_positive3         (int h, int i, int j, int k);

int  sos_above4            (int i, int j, int k, int l, int m);
int  sos_above3            (int i, int j, int k, int l);

int  sos_above3_star       (int i, int j, int k, int l);
void sos_above3_star_set   (int a, int b, int c, int d);

/*--------------------------------------------------------------------------*/

/* non-boolean primitive operations */

void sos_rho3 (int i, int j, int k, int l, Lia_obj a, Lia_obj b);
void sos_rho2 (int i, int j, int k,        Lia_obj a, Lia_obj b);
void sos_rho1 (int i, int j,               Lia_obj a, Lia_obj b);

/*--------------------------------------------------------------------------*/

/* primitives */

typedef struct sos_primitive_result_type
{
  int signum;
  Lia_ptr lia_pointer;
  int depth;
  int two_k;
  int *epsilon;
} SoS_primitive_result;

SoS_primitive_result* sos_lambda5 (int i, int j, int k, int l, int m);
SoS_primitive_result* sos_lambda4 (int i, int j, int k, int l);
SoS_primitive_result* sos_lambda3 (int i, int j, int k);

SoS_primitive_result* sos_lambda4_star (int j, int k, int l);
SoS_primitive_result* sos_lambda3_star (int j, int k);

SoS_primitive_result* sos_rho3_num (int i, int j, int k, int l);
SoS_primitive_result* sos_rho3_den (int i, int j, int k, int l);
SoS_primitive_result* sos_rho2_num (int i, int j, int k);
SoS_primitive_result* sos_rho2_den (int i, int j, int k);
SoS_primitive_result* sos_rho1_num (int i, int j);

void sos_depth_counters_output  (FILE *file);
void sos_depth_counters_summary (int *max, double *mean);
void sos_minor_calls            (int **minor, int *high);

/*--------------------------------------------------------------------------*/

/* minor.c */

void    sos_minor  (void);

Lia_ptr sos_minor1 (int i,
                    int a);

Lia_ptr sos_minor2 (int i, int j,
                    int a, int b);

Lia_ptr sos_minor3 (int i, int j, int k,
                    int a, int b, int c);

Lia_ptr sos_minor4 (int i, int j, int k, int l,
                    int a, int b, int c, int d);

Lia_ptr sos_minor5  (int i, int j, int k, int l, int m,
                    int a, int b, int c, int d, int e);

/*--------------------------------------------------------------------------*/

/*                .........................................................
                  "If we do not succeed, then we face the risk of failure."
                  -- Dan Quayle, Vice-President of the United States [1990]
                  ......................................................... */

#endif  /* #ifndef __SOS_H__ */
