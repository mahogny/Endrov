/* detri/dt.h --- Dt (and Trist) header file. (This is part of the Alf lib!) */

#ifndef __DT_H__  /* Include this file only once! */
#define __DT_H__ 

/*--------------------------------------------------------------------------*/

#include "trist.h"  /* Include the header file for the Trist module. */

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

/* detri/dt.c, typedefs */

typedef struct dt_record
{
  short type; /* triangulation type; negative if SoS artefacts were removed! */
  short bpt;
  int n;
  int sos_offset; /* (added by Mike Facello) */
  int redundant;  /* n == trist->max_org == num.v + redundant */
  int hull_ef;
  int last_hull;
  Trist_num num;
  Trist *trist;
} Dt;

/* triangulation-type enumerators */
#define DT          101
#define DT_CLOSEST  101
#define DT_FURTHEST 102
#define DT_WEIGHTED 103
#define DT_REGULAR  103

typedef struct dt_input_scan_struct
{
  char *name, *title;
  int lines, n;
  int decimals;
  int fix_w, fix_a;
  double scale;
  int has_weights;
} Dt_input_scan;

/*--------------------------------------------------------------------------*/

/* detri/dt.c */

void dt_save (const char path_name[], int type, Trist *s, const Trist_num *q);
Dt*  dt_load (const char path_name[]);
void dt_kill (Dt *dt);

Dt_input_scan* dt_input_scan (const char data_path[]);
void           dt_input_load (const char data_path[], Dt_input_scan *data);

void dt_test_hooks (int (* positive3) (int, int, int, int),
                    int (* in_sphere) (int, int, int, int, int));
int  dt_test (void);
int  dt_test_triangle (int ef, int x, int y, int z);
int  dt_test_open_tetra (int a, int x, int y, int z, int o);
int  dt_test_pedantic (const int vertex[], int m, int closest_flag);

#ifdef __DEBUG__
 extern int dt_test_proto_flag;
#endif

void dt_print_info_sec (FILE *info, double secs, const char title[]);

/*--------------------------------------------------------------------------*/

/* detri/search.c */

int  dt_search (int p, int k);
int  dt_search_get_tests (void);

/*--------------------------------------------------------------------------*/

/* other */

void dt_print_cmd (const char data_name[], const Dt *dt,
                   int argc, /*const*/ char *argv[]);

/*--------------------------------------------------------------------------*/

#endif  /* #ifndef __DT_H__ */
