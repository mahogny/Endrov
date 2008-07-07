/* detri/detri.h */

/*--------------------------------------------------------------------------*/

#include "dt.h"  /* Include Dt (and Trist) header file. */

/*--------------------------------------------------------------------------*/

/* file paths for *.dt files, etc: <data_PATH>.<EXTENSION> */

#define       dt_PATH(data_PATH)  basic_cb_frmt ("%s.dt",     data_PATH)
#define     dt_f_PATH(data_PATH)  basic_cb_frmt ("%s.dt_f",   data_PATH)
#define     info_PATH(data_PATH)  basic_cb_frmt ("%s.info",   data_PATH)
#define   status_PATH(data_PATH)  basic_cb_frmt ("%s.status", data_PATH)

#define   tetra_dt_PATH(data_PATH)  basic_cb_frmt ("%s.tl",   data_PATH)
#define tetra_dt_f_PATH(data_PATH)  basic_cb_frmt ("%s.tl_f", data_PATH)

#define fl_default_PATH(data_PATH,TYPE) \
  basic_cb_frmt ("%s.%s.fl", data_PATH,TYPE)

/*** NOTE that above *_PATH macros return pointers to *temporary* strings! ***/
/***      Most of the time you'll have to use it with STRDUP() !!!         ***/

/*--------------------------------------------------------------------------*/

/* detri/delaunay.c */

typedef struct delaunay_info_type
{
  int mount_1s, mount_2s, mount_3as, mount_3bs, mount_4s;
  int skips, flips_e, flips_f;
  int search_tests, search_outsiders;
} Delaunay_info;

void delaunay (const int vertex[], int n, int *h, int randomized);
Delaunay_info* delaunay_info (void);
extern float   delaunay_search_kf;

extern int delaunay_trace_mode;

#ifdef __DEBUG__
 extern int delaunay_test_flag, delaunay_proto_flag;
#endif

/*--------------------------------------------------------------------------*/

/* detri/flip.c */

void delaunay_flip_open     (int n);
int  delaunay_flip          (Basic_istaque_adt nd_list, int v);
void delaunay_flip_get_info (int *s, int *e, int *f);
void delaunay_flip_close    (void);

void delaunay_flip_hook (void (* routine) (char code,
                                           int x, int y, int z, int o, int p, 
                                           int ref));
                             

#define DT_FLIP_TRIANGLE    'T'
#define DT_FLIP_EDGE        'E'

int  delaunay_test        (int ef);
void delaunay_test_set_fp (int flag);

#ifdef __DEBUG__
 extern int delaunay_flip_test_flag;
 extern int delaunay_flip_proto_flag;
#endif
