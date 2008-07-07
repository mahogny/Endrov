/* detri/detri.c  ---  3D Delaunay triangulation (closest/furthest-point) */

/*---------------------------------------------------------------------------*/

const char detri__version[] = "@(#) Detri 2.6";
const char detri__purpose[] = "Robust 3D Delaunay Triangulation";
const char detri__algorit[] = "Randomized Incremental Flip Algorithm";
const char detri__authors[] = "Ernst Mucke";
const char detri__disclai[] = "\
 Code provided AS IS and WITHOUT ANY expressed or implied WARRANTIES\
";
#ifdef __DEBUG__
const char detri_compile[] = "@(#) \t w/ -D__DEBUG__";
#endif

/*--------------------------------------------------------------------------*/

static char usage[] = "\n\
USAGE: \n\
       %s [OPTIONS] <DATA> \n\
with: \n\
       <DATA> ....... path name of input data \n\
OPTIONS: \n\
       -r <SEED> .... seed; 0: sorted, 1: input order, >1: random (default) \n\
       -s ........... silent \n\
       -t ........... test option \n\
       -k ........... keep SoS artefacts \n\
       -X <OPTION> .. undocumented options \n\
ABC mode:             (All But Computation) batch mode: \n\
       -A ............. read commands from standard input, \n\
       -B <FILE> ...... from given <FILE>, or \n\
       -C <COMMAND> ... from command-line. \n\
";

/* NOTE:  The option   -f ........... compute furthest-point DT as well \n\
   is currently disabled, because there are some problems with the code of
   furthest_point().  Need to check into this. */   

/*--------------------------------------------------------------------------*/

#include "detri.h"

/* from prep.c */
void prep_vertices (int n0, int *n1, int vertex[],
                    int randomized, int proto_flag, FILE *info_file);

/*--------------------------------------------------------------------------*/

/* program flags/parameters and their default setting */

static char *cmd_name, *data_name, *abc_file_name = NULL;

static int furthest_flag = FALSE;  /* dont't compute DT_f by default */
static int peel_flag = TRUE;       /* peel off degen. tetrahedra on hull */
static int test_flag = FALSE;
static int silent_flag = FALSE;

static int randomized = -1;  /* randomized incremental-flip by default;
                                cf: -r <SEED> option */

static int maxtri_code = 1;
/* note: By default maxtri_code==1, and maxtri (n) returns some (empirical)
   number suitable for the iniltial Trist malloc.  If maxtri_code==0, the
   correct upper bound according to the Upper Bound Theorem is used.  This,
   however, is no longer necessary, because Trist is now semi-dynamic.
   Cf: maxtri(), -X ubt option. */

/*--------------------------------------------------------------------------*/

/* local procedures (w/ common variables) */

static int maxtri (int n);
static void detri (void);

static void closest_point  (int n, int max_n,
                           int vertex[], Trist_num *q, int hull_vertex[]);
static void furthest_point (int nh, int max_n,
                            const int hull_vertex[], Trist_num *q);
static Trist_num aftermath (int *hull_ef, Trist_num num1);

static void parse_abc_option (char *current, char option, const char string[]);
static void abc_mode (void);

static FILE *info_file;
static int info_trist_bpt;
static unsigned long info_trist_bytes;

static void  open_info_file (const char data_title[]);
static void close_info_file (double total_time, double terminal_time,
                             Trist_num qc, Trist_num qf);

static void print_info_file_dt (const char what[], const Trist *s,
                                Trist_num q1, Trist_num q2, double time);
static void print_info_file_ch (Trist_num q1, Trist_num q2);

static int abort_flag = FALSE;  /* see detri() and detri_error() */
static void detri_error (const char message[]);

#ifdef __DEBUG__
 static void dummy_flip_hook (char code,
                              int x, int y, int z, int o, int p, 
                              int ref);
#endif

/*--------------------------------------------------------------------------*/

void main (int argc, char *argv[])
{
  int c, is_wrong = FALSE;
  char abc_option = ' ';
  Assert_always (basic_types_okay ());
  print ("%s\n %s, %s\n%s\n", basic_strip (detri__version),
         detri__purpose, detri__algorit, detri__disclai);
  basic_error_hook (detri_error);
  basic_malloc_debug (1);
  ;
  dt_test_hooks (sos_positive3, sos_in_sphere);
  delaunay_trace_mode = 1;
  ;
#ifdef __DEBUG__
  delaunay_flip_hook (dummy_flip_hook);
  ;
  /* medium */
  delaunay_test_flag = TRUE;
  delaunay_flip_test_flag = TRUE;    
  trist_test_flag = TRUE;   
  trist_io_check_flag = TRUE;
  ;
  /* extreme */
  delaunay_proto_flag = FALSE;
  delaunay_flip_proto_flag = FALSE;
  dt_test_proto_flag = FALSE;
  sos_test_flag = FALSE;
#endif
  ;
  /* get command-line arguments */
  cmd_name = argv[0];
  (void) basic_getarg_init (argc - 1, argv + 1);
  while ((c = basic_getarg ("stfkr:X:AB:C:")) != 0)
    switch (c)
      {
       case -1:    /* <data_name> */
        data_name = basic_getarg_optarg;
        break;
       case 'r':  /* -r <SEED> .... seed */
        if (sscanf (basic_getarg_optarg, "%d", &randomized) != 1)
          basic_error ("unrecognized option: -r %s", basic_getarg_optarg);
        break;
       case 'f':
        /* furthest_flag = TRUE; ... currently dissabled! */
        break;
       case 'k':
        peel_flag = FALSE;
        break;
       case 's':  /* -s ... silent_flag and w/o tests */
        silent_flag = TRUE;
        delaunay_trace_mode = 0;
        break;
       case 't':  /* -t ... call dt_test_pedantic{) for dt/dtf_structure */
        test_flag = TRUE;
        break;
       case 'X':
        { /* -X <OPTION> ... undocumented options */
          if (strcmp (basic_getarg_optarg, "upt") == 0)
            { /* -X upt ... use UPT for initial Trist malloc */
              maxtri_code = 0;
              (void) maxtri (1); /* just for testing */
            }
          else if (strncmp (basic_getarg_optarg, "kf=", 3) == 0)
            {
              /* -X kf=<VALUE> ... set delaunay_search_kf to given <VALUE> */
              if (sscanf (basic_getarg_optarg, "kf=%f",
                          &delaunay_search_kf) != 1)
                basic_error ("unrecognized option: -X %s",
                             basic_getarg_optarg);
            }
          else
            is_wrong = TRUE;
          break;
        }
       case 'A':
       case 'B':
       case 'C':
        /* -A  or
           -B <COMMAND_FILE>  or
           -C <COMMAND_STRING> ... read binaries and execute commands */
        parse_abc_option (&abc_option, c, basic_getarg_optarg);
        break;
       default:
        is_wrong = TRUE;
      }
  if (is_wrong or (not data_name))
    {
      fprint (stderr, usage, cmd_name);
      exit (1);
    }
  if (randomized == -1)
    randomized = basic_seed ();
  if (not (basic_access (data_name)))
    basic_error ("Data file \"%s\" doesn't exist.", data_name);
  ;
  /* from now on we core-dump on errors */
  abort_flag = TRUE;
  ;
#ifdef __SOS_TRACE__
  /* just in case we want to trace the SoS routines... */
  sos_trace_file = basic_fopen (basic_cb_frmt ("%s.sos", data_name), "w");
#endif
  ;
  /* main part */
  if (abc_option == ' ')
    detri ();
  else
    abc_mode ();
  ;
  /* shutdown */
  if (not silent_flag) 
    basic_malloc_info_print (stdout);
  if (abc_option == 'C')
    basic_rm (abc_file_name);
}

/*--------------------------------------------------------------------------*/

static int maxtri (int n)
     /* ... returns the (expected) maximum number of triangles in the DT
        for n vertices.  The global maxtri_code selects between true or
        "empirical" upper bounds. */
{
  int u = 0;
  switch (maxtri_code)
    {
     case 0:  /* true upper bound */
      u = trist_upper_bound (n);
      break;
     case 1: /* linear "empirical" bound; that's the default! */
      u = 15 * n;
      break;
     default:
      basic_error ("unknown maxtri_code: %d", maxtri_code);
    }
  return (u);
}

/*--------------------------------------------------------------------------*/

static void detri (void)
{
  double time_total = basic_utime ();
  double time_terminal = basic_seconds ();
  Trist_num qc, qf;
  int *vertex, *hull_vertex;
  int n;
  Dt_input_scan *data;
  ;
  data = dt_input_scan (data_name);
  sos_matrix (data->n, 4, data->scale,
              Lia_DIGITS (5 * data->decimals + 3),   /* for sos_in_sphere() */
              Lia_DIGITS (2 * data->decimals + 1));  /* for sum of squares */
  dt_input_load (data_name, data);
  if (data->has_weights)
    basic_error ("Data file contains weights (eg: %d. data line).",
                 data->has_weights);
  ;
  vertex = MALLOC (int, data->n + 1);
  hull_vertex = MALLOC (int, data->n + 1);
  ;
  open_info_file (data->title);
  prep_vertices (data->n, &n, vertex, randomized, FALSE, info_file);
  /* NOTE: At this point, all duplicates are dumped and n <= data->n.
     The indices in vertex[1..n] are between 1 and data->n, though. */
  ;
  closest_point (n, data->n, vertex, &qc, hull_vertex);
  if (furthest_flag)
    {
      furthest_point (qc.vh, data->n, hull_vertex, &qf);
      Assert_always (trist_num_eq (qc, qf, 'h') and (qf.v  == qf.vh));
    }
  ;
  FREE (hull_vertex);
  FREE (vertex);
  ;
  sos_shutdown ();
  time_total = basic_utime () - time_total;
  time_terminal = basic_seconds () - time_terminal;
  close_info_file (time_total, time_terminal, qc, qf);
}

/*--------------------------------------------------------------------------*/

static void closest_point (int n, int max_n,
                           int vertex[], Trist_num *q, int hull_vertex[])
     /* Output: vertex[1..n], *q, hull_vertex[]. */
{
  char *title = "(Closest-point) Delaunay triangulation";
  char *path_name = STRDUP (dt_PATH (data_name));
  double utime;
  int hull_ef;
  Trist_num num1, num2;
  Trist *s;
  if (not silent_flag)
    print ("\n%s (v=%d, f=%d projected) ...\n", title, n, maxtri (n));
  trist_set (s = trist_alloc (max_n, maxtri (n)));
  ;
  utime = basic_utime ();
  delaunay (vertex, n, &hull_ef, randomized);
  utime = basic_utime () - utime;
  ;
  num1 = trist_num ();
  if (test_flag)
    (void) dt_test_pedantic (vertex, n, TRUE);
  num2 = aftermath (&hull_ef, num1);
  dt_save (path_name, If (num1.t == num2.t, 1, -1) * DT_CLOSEST, s, &num2);
  print_info_file_dt (title, s, num1, num2, utime);
  print_info_file_ch (num1, num2);
  trist_hull_copy (vertex, hull_vertex);
  *q = num1;
  trist_kill (s);
}

/*--------------------------------------------------------------------------*/

static Trist_num aftermath (int *hull_ef, Trist_num num1)
     /* Input/Output: *hull_ef. */
     /* Refinement of closest/furthest_point(). */
{
  print ("Done (f=%d, t=%d, %d or %0.2f%% degenerated).\n",
         num1.f, num1.t, num1.t_flat, 100.0 * num1.t_flat / num1.t);
  if (peel_flag and (num1.t_flat > 0) and (num1.t_proper > 0))
    {
      *hull_ef = trist_peel (*hull_ef, num1.t_flat, num1.fh);
      if (test_flag)
        (void) dt_test ();
      return (trist_num ());
    }
  return (num1);
}

/*--------------------------------------------------------------------------*/

static void furthest_point (int nh, int max_n,
                            const int hull_vertex[], Trist_num *q)
     /* Output: *q. */
{
  char *title = "Furthest-point Delaunay triangulation";
  char *path_name = STRDUP (dt_f_PATH (data_name));
  double utime;
  int hull_ef;
  Trist_num num1, num2;
  Trist *s;
  if (not silent_flag)
    print ("\n%s (v=%d, f=%d projected) ...\n", title, nh, maxtri (nh));
  trist_set (s = trist_alloc (max_n, maxtri (nh)));
  ;
  utime = basic_utime ();
  delaunay_test_set_fp (TRUE);
  delaunay (hull_vertex, nh, &hull_ef, randomized);
  utime = basic_utime () - utime;
  ;
  num1 = trist_num ();
  if (test_flag)
    (void) dt_test_pedantic (hull_vertex, nh, FALSE);
  num2 = aftermath (&hull_ef, num1);
  dt_save (path_name, If (num1.t == num2.t, 1, -1) * DT_FURTHEST, s, &num2);
  print_info_file_dt (title, s, num1, num2, utime);
  *q = num1;
  trist_kill (s);
}

/*--------------------------------------------------------------------------*/

static void open_info_file (const char data_title[])
{
  char *path = STRDUP (info_PATH (data_name));
  info_file = basic_fopen (path, "w");
  fprint (info_file, "#\n# %s (%s), on %s, %s\n#\n  Data: %s\n  Title: %s\n",
          basic_strip (detri__version), cmd_name, basic_hostname (),
          basic_date (), data_name, data_title);
}

/*--------------------------------------------------------------------------*/

static void print_info_file_dt (const char what[], const Trist *s,
                                Trist_num q1, Trist_num q2, double time)
{
  Trist_info *ti = trist_info ();
  Delaunay_info *di = delaunay_info ();
  Assert (q1.v == q2.v);
  fprint (info_file, "* %s\n", what);
  fprint (info_file, "%12d . vertices\n", q2.v);
  dt_print_info_sec (info_file, time, "CPU secs");
  if (randomized > 0)
    {
      fprint (info_file, "%12d . search tests (sos_lambda4) w/ kf=%0.2f\n",
              di->search_tests, delaunay_search_kf);
      fprint (info_file, "%12.2f . average search length\n",
              (double) di->search_tests / q2.v);
      fprint (info_file, "%12d . outside insertions (%0.2f%%)\n",
              di->search_outsiders,
              100.0 * di->search_outsiders / (q2.v - 4.0));
    }
  fprint (info_file, "%12d . flips, total\n", di->flips_f + di->flips_e);
  fprint (info_file, "%12d .   triangle-to-edge flips\n", di->flips_f);
  fprint (info_file, "%12d .   edge-to-triangle flips\n", di->flips_e);
  fprint (info_file, "%12d . unsuccessful flips (skips)\n", di->skips);
  fprint (info_file, "%12d . mounts (%d + %d + %d + %d + %d)\n",
          di->mount_1s+di->mount_2s+di->mount_3as+di->mount_3bs+di->mount_4s,
          di->mount_1s,di->mount_2s,di->mount_3as,di->mount_3bs,di->mount_4s);

  if (peel_flag)
    {
      fprint (info_file, "%12d . tetrahedra (was: %d, flat: %d)\n",
              q2.t, q1.t, q1.t_flat);
      fprint (info_file, "%12d . triangles (was: %d, last: %d, max: %d)\n",
              q2.f, q1.f, s->last_triangle, s->max_triangle);
      fprint (info_file, "%12d . edges (was: %d)\n", q2.e, q1.e);
    }
  else
    {
      fprint (info_file, "%12d . tetrahedra (flat: %d)\n",
              q2.t, q1.t_flat);
      fprint (info_file, "%12d . triangles (last: %d, max: %d)\n",
              q2.f, s->last_triangle, s->max_triangle);
      fprint (info_file, "%12d . edges \n", q2.e);
    }
  fprint (info_file, "%12s . Orgs\n",     basic_counter (ti->orgs));
  fprint (info_file, "%12s . Syms\n",     basic_counter (ti->syms));
  fprint (info_file, "%12s . Enexts\n",   basic_counter (ti->enexts));
  fprint (info_file, "%12s . Fnexts\n",   basic_counter (ti->fnexts));
  fprint (info_file, "%12s . Fsplices\n", basic_counter (ti->fsplices));
  info_trist_bpt = ti->bpt;
  info_trist_bytes += ti->bytes;
}

/*--------------------------------------------------------------------------*/

static void print_info_file_ch (Trist_num q1, Trist_num q2)
{
  fprint (info_file, "* Convex hull\n");
  if (peel_flag)
    {
      fprint (info_file, "%12d . vertices (was: %d)\n", q2.vh, q1.vh);
      fprint (info_file, "%12d . edges (was: %d)\n", q2.eh, q1.eh);
      fprint (info_file, "%12d . triangles (was: %d)\n", q2.fh, q1.fh);
    }
  else
    {
      fprint (info_file, "%12d . vertices\n", q2.vh);
      fprint (info_file, "%12d . edges\n", q2.eh);
      fprint (info_file, "%12d . triangles\n", q2.fh);
    }
}

/*--------------------------------------------------------------------------*/

static void close_info_file (double total_time, double terminal_time,
                             Trist_num qc, Trist_num qf)
{
  double  util_c, util0_c, util_f = 0, util0_f = 0;
  Lia_info *li = lia_info ();
  Basic_malloc_info *mi = basic_malloc_info ();
  util_c = (100.0 * qc.f) / (double) maxtri (qc.v);
  util0_c = (100.0 * qc.f) / (double) trist_upper_bound (qc.v);
  if (furthest_flag)
    {
      util_f = (100.0 * qf.f) / (double) maxtri (qf.v);
      util0_f = (100.0 * qf.f) / (double) trist_upper_bound (qf.v);
    }
  fprint (info_file, "* Lia counters\n");
  fprint (info_file, "%12s . Lia mul calls\n", basic_counter (li->mul_calls));
  fprint (info_file, "%12s . Lia mul elops\n", basic_counter (li->mul_elops));
  fprint (info_file, "%12s . Lia add calls\n", basic_counter (li->padd_calls));
  fprint (info_file, "%12s . Lia add elops\n", basic_counter (li->padd_elops));
  fprint (info_file, "%12s . Lia sub calls\n", basic_counter (li->psub_calls));
  fprint (info_file, "%12s . Lia sub elops\n", basic_counter (li->psub_elops));
  fprint (info_file, "%12d . Lia maximum digit\n", li->maximum_digit);
  sos_depth_counters_output (info_file);
  fprint (info_file, "* Miscellaneous\n");
  fprint (info_file, "%12.3f . %% DT   utilization (%.3f%% of UBT)\n",
          util_c, util0_c);
  if (furthest_flag)
    fprint (info_file, "%12.3f . %% DT_f utilization (%.3f%% of UBT)\n",
            util_f, util0_f);
  fprint (info_file, "%12.3f . Mb SoS\n", basic_mbytes (sos_bytes ()));
  fprint (info_file, "%12.3f . Mb Trist (%d bytes per triangle)\n",
          basic_mbytes (info_trist_bytes), info_trist_bpt);
  fprint (info_file,
          "%12.3f . Mb malloc, total (arena: %.3f Mb)\n",
          basic_mbytes (mi->total), basic_mbytes (mi->arena));
  dt_print_info_sec (info_file, total_time,    " CPU secs, detri total");
  dt_print_info_sec (info_file, terminal_time, "real secs, detri total");
  if (not silent_flag)
    {
      print ("Trist: %0.3f%% (%0.3f%%) ", util_c, util0_c);
      if (furthest_flag)
        print ("and %0.3f%% (%0.3f%%) ", util_f, util0_f);
      print ("utilization.\n");
    }
  basic_fclose (info_file);
}

/*---------------------------------------------------------------------------*/

#ifdef __DEBUG__

static void dummy_flip_hook (char code,
                             int x, int y, int z, int o, int p, 
                             int ref)
{
  switch (code)
    {
     case DT_FLIP_TRIANGLE:
      (void) dt_test_triangle (ref, p, o, x);
      break;
     case DT_FLIP_EDGE:
      (void) dt_test_triangle (ref, x, y, z);
      break;
     default:
      Assert (FALSE);
    }
}

#endif

/*--------------------------------------------------------------------------*/

static void parse_abc_option (char *current, char option, const char string[])
     /* Input/Output: *current. */
     /* Refinement of main(). */
{
  FILE *f;
  if ((*current != ' ') and (*current != option))
    basic_error ("Can't use -A, -B, and -C options together: -%c %s",
                 option, string);
  if ((*current == ' ') and (option == 'C'))
    { /* start new internal command file */
      STRSET (abc_file_name, basic_cb_frmt (".detri.%d.abc", basic_seed ()));
      f = basic_fopen (abc_file_name, "w");
      fprint (f, "%s\n", string);
      basic_fclose (f);
    }
  else if (option == 'C')
    { /* continue internal command file */
      f = basic_fopen (abc_file_name, "a");
      fprint (f, "%s\n", string);
      basic_fclose (f);
    }
  else if (option == 'B')
    { /* take given command file */
      STRSET (abc_file_name, string);
    }
  else
    { /* read from standard input */
      STRSET (abc_file_name, ".");
    }
  *current = option;
}

/*--------------------------------------------------------------------------*/

static void abc_mode (void)
     /* Detri [ABC] batch mode. (All but computation. :) */
{
  int i;
  Dt *dt;
  char *buf, *token[99];
  char *dt_path = If (furthest_flag,
                      STRDUP (dt_f_PATH (data_name)),
                      STRDUP (dt_PATH (data_name)));
  Dt_input_scan *data;
  FILE *f = If ((strcmp (abc_file_name, ".") == 0),
                stdin, basic_fopen (abc_file_name, "r"));
  data = dt_input_scan (data_name);
  sos_matrix (data->n, 4, data->scale,
              Lia_DIGITS (5 * data->decimals + 3),   /* sos_in_sphere() */
              Lia_DIGITS (2 * data->decimals + 1));  /* sum of squares */
  dt_input_load (data_name, data);
  dt = dt_load (dt_path);
  print ("Entering [ABC] batch mode...\n");
  while ((buf = basic_cb_getline (f)))
    {
      /* delete '\n' from basic_cb_getline() */
      buf[(int) strlen (buf) - 1] = '\0';
      ;
      i = basic_tokenize (buf, token, 99);
      if (basic_arg_find ("print", i, token))
        dt_print_cmd (data_name, dt, i, token);
      else if (basic_arg_find ("help", i, token))
        dt_print_cmd (data_name, dt, i, token);
    }
  if (f and (f != stdin))
    basic_fclose (f);
  dt_kill (dt);
  sos_shutdown ();
}

/*--------------------------------------------------------------------------*/

static void detri_error (const char message[])
{
  fprint (stderr, "\n%s: %s\n", cmd_name, message);
  if (info_file)
    {
      fprint (info_file, "\nERROR.\n%s\n", message);
      basic_fclose (info_file);
    }
  if (abort_flag)
    abort ();
  else
    exit (1);
}
