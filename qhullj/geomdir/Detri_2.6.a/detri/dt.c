/* detri/dt.c --- Data handlers/testers for Detri. */

/*--------------------------------------------------------------------------*/

#include "dt.h"

/*--------------------------------------------------------------------------*/

/* configuring dt.c */

#define MAGIC trist__internal_magic_number
extern int trist__internal_magic_number;
/* Trist magic number.  (Silently imported from trist.c.) */

#define MAX_DECIMALS  16
/* Maximum number of significant decimal digits of "#fix" input coordinates.
   NOTE: 64-bit IEEE 745 floating-point roughly corresponds to 16 significant
   decimal digits. */

#define GLOBAL_TEST(N)  ((N) <= 30)
/* When do you want to do an EXTREMELY expensive global test? */

/*--------------------------------------------------------------------------*/

/* Function hooks to Sos primitives, s.t. the dt_test*() code can be
   used for DT as well as for weighted DT (regular T).  Detri, eg, sets
   them to sos_positive3() and sos_in_sphere(), respectively. */

static int (* positive3_hook) (int, int, int, int) = NULL;
static int (* in_sphere_hook) (int, int, int, int, int) = NULL;

/*--------------------------------------------------------------------------*/

/* local procedures and variables */

static void test_hull (int h);
static void test_hull_recursive (Basic_byte hb[], int h);
static void test_complex (void);
static void test_triangulation (void);
static void test_tetra_global (int a, int b, int c, int d, const int vertex[],
                               int m, int closest_flag, int delaunay_flag);

static int is_command  (const Dt_input_scan *is,
                       const char *str, const char *command, int nchars);

static void int_param_push2 (int i, int j, int x);
static void ffp_param_push2 (int i, int j, double x, int w, int a);

#ifdef __DEBUG__
  int dt_test_proto_flag = FALSE;
# define proto(PRINT_COMMAND)  if (dt_test_proto_flag) PRINT_COMMAND
#else
# define proto(PRINT_COMMAND)  /* do nothing */
#endif

static int dummy_word = 0;  /* as used in dt_save() and dt_load() */
#define Write1f(FIELD)  binfwrite (&(FIELD), 1, f)
#define  Read1f(FIELD)  binfread  (&(FIELD), 1, f)

/*--------------------------------------------------------------------------*/

/* variables coordinating allocation of sos matrix for more than one data
   set (triangulations), loaded sequentially  (added by Mike Facello) */

static int offset = 0;
static int dt_first_load = TRUE;

/*--------------------------------------------------------------------------*/

void dt_save (const char path_name[], int type, Trist *s, const Trist_num *q)
{
  Dt dt;
  int h1, h2;
  int saved_max = s->max_triangle;
  Assert_always (s->magic == MAGIC);
  ;
  { /* using binary format */
    FILE *f = basic_fopen (path_name, "w");
    print ("Saving binary file \"%s\" ...\n", path_name);
    ;
    h2 = trist_pack ();
    h1 = EdFacet (1, 0);  /* trist_pack*() moves hull facets to: [1..h2] */
    if (not trist_hull_facet (h1))
      h1 = Sym (h1);
#ifdef __DEBUG__
    trist_io_check (s, *q);
#endif
    ;
    dt.bpt = (short) sizeof (Trist_record);
    dt.type = (short) type;
    dt.hull_ef = h1;
    dt.last_hull = h2;
    dt.num = *q;
    dt.n = s->max_org;
    dt.redundant  = s->max_org - dt.num.v;
    dt.trist = NULL;
    s->max_triangle = s->last_triangle;
    Assert_always (dt.n == dt.num.v + dt.redundant);
    ;
    /* binfwrite (&dt, 1, f); */
    Write1f (dt.type);
    Write1f (dt.bpt);
    Write1f (dt.n);
    Write1f (dt.redundant);
    Write1f (dt.hull_ef);
    Write1f (dt.last_hull);
    Write1f (dt.num);
    Write1f (dummy_word);
    ;
    /* binfwrite (s, 1, f); */
    Write1f (s->magic);
    Write1f (dummy_word);
    Write1f (s->last_triangle);
    Write1f (s->max_triangle);
    Write1f (s->max_org);
    Write1f (dummy_word);
    Write1f (s->used_triangles);
    Write1f (s->next_reusable_triangle);
    ;
    binfwrite (s->triangle, s->max_triangle + 1, f);
    ;
    basic_fclose (f);
  }
  s->max_triangle = saved_max;  /* such that s leaves unchanged! */
}

/*--------------------------------------------------------------------------*/

Dt* dt_load (const char path_name[])
{
  Dt *dt = MALLOC (Dt, 1);
  Trist *s = MALLOC (Trist, 1);
  ;
  { /* using binary format */
    FILE *f = basic_fopen (path_name, "r");
    print ("Reading Delaunay triangulation from binary file \"%s\" ...\n",
           If (basic_fopen_zpath, basic_fopen_zpath, path_name));
    ;
    Read1f (dt->type);
    Read1f (dt->bpt);
    Read1f (dt->n);
    Read1f (dt->redundant);
    Read1f (dt->hull_ef);
    Read1f (dt->last_hull);
    Read1f (dt->num);
    Read1f (dummy_word);
    ;
    Read1f (s->magic);
    Read1f (dummy_word);
    Read1f (s->last_triangle);
    Read1f (s->max_triangle);
    Read1f (s->max_org);
    Read1f (dummy_word);
    Read1f (s->used_triangles);
    Read1f (s->next_reusable_triangle);
    s->data_size = 0;
    s->data = NULL;
    ;
    dt->trist = s;
    ;
    /* just checking... */
    if (s->magic != MAGIC)
      basic_error ("dt_load: wrong magic number (%d != %d)\n",
                   s->magic, MAGIC);
    if (dt->bpt != (short) sizeof (Trist_record))
      basic_error ("dt_load: wrong Trist record size (%d != %d)\n",
                   dt->bpt, (short) sizeof (Trist_record));
    if (not (    (dt->n == dt->trist->max_org)
             and (dt->n == dt->num.v + dt->redundant)))
      basic_error ("dt_load: corrupted header\n");
    if (dt->redundant)
      {
        print ("%15d redundant vertices dumped.\n", dt->redundant);
        print ("%15d actual vertices remain.\n", dt->num.v);
      }
    ;
    s->triangle = MALLOC (Trist_record, s->max_triangle + 1);
    binfread (s->triangle, s->max_triangle + 1, f);
    ;
    if (offset > 0)
      {
        dt->sos_offset = offset;
        s->max_org += offset;
        trist_set(s);
        trist_modify_vertices (offset);
        offset = 0;
      }
    else
      dt->sos_offset = 0;
#ifdef __DEBUG__
    trist_io_check (s, dt->num);
#endif
    basic_fclose (f);
  }
  return (dt);
}

/*--------------------------------------------------------------------------*/

void dt_kill (Dt *dt)
{
  if (dt)
    {
      trist_kill (dt->trist);
      FREE (dt);
      dt_first_load = TRUE;
    }
}

/*--------------------------------------------------------------------------*/

Dt_input_scan *dt_input_scan (const char data_path[])
     /* ... scans the ASCII input file data_path for the fields in the return
        structure (see dt.h file).  See also dt_input_load() which actually
        loads the data and sets the has_weights field if necessary. */
     /* NOTE: The returned address, which points to the return structure,
              is a constant.  DO NOT FREE() IT and consider the fields
              of the structure as read-only. */
{
  static Dt_input_scan data;
  FILE *inp = basic_fopen (data_path, "r");
  char *r;
  ;
  /* default values */
  data.name = STRDUP (data_path);
  data.title = NULL;
  data.lines = 0;
  data.n = 0;
  data.scale = 1.0;     /* ie, no scaling */
  data.fix_w = 0;    
  data.fix_a = 0;
  data.decimals = 10;  /* ie, int coordinates */
  data.has_weights = 0;  /* ie, unknown; see dt_input_load() */
  ;
  /* scan the file */
  print ("Scanning ASCII file \"%s\" ... ",
         If (basic_fopen_zpath, basic_fopen_zpath, data_path));
  flush ();
  while ((r = basic_cb_getline (inp)))
    {
      data.lines ++;
      if (basic_strip (r) and (r[0] != '#'))
        { /* a data line: <x> <y> <z> */
          data.n ++;
        }
      else if (is_command (&data, r, "title", 5))
        { /* # title: %s */
          r = basic_strip (index (r, 'e') + 1);
          if (r)
            {
              int i = -1;
              data.title = STRDUP (r);
              do { i ++; } until (data.title[i] == '\n');
              data.title[i] = 0;
            }
        }
      else if (is_command (&data, r, "scale", 5))
        { /* # scale: %f> */
          r = basic_strip (index (r, 'e') + 1);
          if (r)
            {
              int io = sscanf (r, "%lf", &(data.scale));
              if (io != 1)
                basic_error ("\nFile: \"%s\", line: %d, wrong #scale=%1.1e.\n",
                             data.name, data.lines, data.scale);
            }
        }
      else if (is_command (&data, r, "fix", 3))
        { /* # fix: %d.%d */
          r = basic_strip (index (r, 'x') + 1);
          if (r)
            {
              int io = sscanf (r, "%d.%d", &(data.fix_w), &(data.fix_a));
              if (   (io != 2)
                  or (not (    (MAX_DECIMALS >= data.fix_w)
                           and (data.fix_w > data.fix_a)
                           and (data.fix_a >= 0))))
                basic_error ("\nFile: \"%s\", line: %d, invalid #fix=%d.%d\n",
                             data.name, data.lines, data.fix_w, data.fix_a);
            }
        }
    }
  basic_fclose (inp);
  Assert_always (data.n > 0);
  if (data.fix_w != 0)
    {
      print ("(#fix=%d.%d) ", data.fix_w, data.fix_a);
      data.decimals = data.fix_w;
      data.scale = data.scale / exp10 ((double) data.fix_a);
    }
  if (data.scale != 1.0)
    print ("(#scale=%1.1e) ", data.scale);
  print ("\n");
  return (&data);
}  

static int is_command (const Dt_input_scan *is,
                       const char *str, const char *command, int nchars)
     /* Returns TRUE iff *str starts with "#" + command[nchars].
        However, a command is only accepted if it comes BEFORE any
        data lines! */
{
  if ((is->n == 0) and (str[0] == '#'))
    {
      while ((*str == '#') or (*str == ' '))
        str ++;  /* skip #'s and blanks */
      if (strncmp (str, command, nchars) == 0)
        return (TRUE);
      else
        return (FALSE);
    }
  else
    return (FALSE);
}

/*--------------------------------------------------------------------------*/

void dt_input_load (const char data_path[], Dt_input_scan *data)
     /* Input/Output: *data. */
     /* Reads coordinates from file data_path, loading them into SoS
        parameter matrix: sos_lia (1..data->n, 1..4).
        Handles both int and ffp coordinates.
        Handles both XYZ (unweighted) and XYZW (weighted) coordinates.
        Sets data->has_weights to non-zero: i > 0 iff at least one coordinate
        (namely the i-th) has a weight.
        Assumption: proper sos_matrix() for data->n * 4 parameters; ie,
        three normal, and one which is sum of squares (+ weight, if any)! */
     /* Changed by Mike Facello for Ping Fu.
        Can now load more than one trianglulation, sequentially (sos_offset) */
{
  FILE *inp = basic_fopen (data_path, "r");
  int io, i, lines = 0;
  int x, y, z, w;
  double xx, yy, zz, ww;
  char *r;
  Assert_always ((strcmp (data_path, data->name) == 0) and (data->n > 0));
  print ("Reading %d vertices from ASCII file \"%s\" ...\n",
         data->n, If (basic_fopen_zpath, basic_fopen_zpath, data_path));
  ;
  if (dt_first_load)
    i = offset = 0;
  else
    i = offset = sos_max();
  ;
  if ((offset > 0))
    {  
      /* Grow the SOS matrix by the number of vertices in this vertex set. */
      sos_grow(data->n);
    }
  else
    {
      dt_first_load = FALSE;
    }
  ;
  while ((r = basic_cb_getline (inp)))
    {
      lines ++;
      if (basic_strip (r) and (r[0] != '#'))
        {
          i ++;
          if (data->fix_w)
            io = sscanf (r, "%lf %lf %lf %lf", &xx, &yy, &zz, &ww);
          else
            io = sscanf (r, "%d %d %d %d", &x, &y, &z, &w);
          switch (io)
            {
             case 3:
              { 
                ww = w = 0;
                break;
              }
             case 4:
              { 
                if (data->has_weights <= 0)
                  data->has_weights = i;
                break;
              }
             default:
              basic_error ("dt_input_load: %s %d (out of %d) at line %d.",
                           "EOF or format error while reading coordinates",
                           i, data->n, lines);
            }
          if (data->fix_w)
            {
              ffp_param_push2 (i, 1, xx, data->fix_w, data->fix_a);
              ffp_param_push2 (i, 2, yy, data->fix_w, data->fix_a);
              ffp_param_push2 (i, 3, zz, data->fix_w, data->fix_a);
              ffp_param_push2 (i, 4, ww, data->fix_w, data->fix_a);  /* (##) */
              if (ww < 0)
                lia_negtop ();
            }
          else
            {
              int_param_push2 (i, 1, x);
              int_param_push2 (i, 2, y);
              int_param_push2 (i, 3, z);
              int_param_push2 (i, 4, w);  /* (##) */
              if (w < 0)
                lia_negtop ();
            }
          lia_minus ();
          lia_plus ();
          lia_plus ();
          sos_param (i, 4, lia_popf ());
          /* Note: 4-th coordinate =  x^2 + y^2 + z^2 - sign (w) * w^2 */
          /* (##): This overwrites the first, otherwise useless,
             sos_param (i, 4, ...) in ffp/int_param_push2 (i, 4, ...). */
        }
    }
  basic_fclose (inp);
  Assert_always ((i - offset) == data->n);
}

static void int_param_push2 (int i, int j, int x)
     /* dt_input_load() refinement */
{
  Lia lx[3];  /* 32-bit int, 10 decimal digits, ceiling(10/8) + 1 == 3 */
  lia_load (lx, x);
  sos_param (i, j, lx);
  lia_push (lx);
  lia_ipower (2);
}

static void  ffp_param_push2 (int i, int j, double x, int w, int a)
     /* dt_input_load() refinement */
{
  Lia lx[Lia_DIGITS(MAX_DECIMALS)];
  lia_ffpload (lx, w, a, x);
  sos_param (i, j, lx);
  lia_push (lx);
  lia_ipower (2);
}

/*--------------------------------------------------------------------------*/

void dt_test_hooks (int (* positive3) (int, int, int, int),
                    int (* in_sphere) (int, int, int, int, int))
     /* Initializes the dt_test*() submodule below.
        Assumes that positive3() and in_sphere() are SoS implementations
        of the corresponding primitives. */
{
  positive3_hook = positive3;
  in_sphere_hook = in_sphere;
}

/*--------------------------------------------------------------------------*/

int dt_test (void)
     /* Tests the current Trist for Delaunay. */
{
  print ("dt_test()...\n");
  if (not (positive3_hook and in_sphere_hook))
    basic_error ("dt_test: Undefined primitive hooks.");
  test_complex ();
  test_triangulation ();
  { /* test (convex) hull */
    int e, t;
    trist_for (t)
      if (trist_hull_triangle (t))
        {
          if (trist_hull_facet (e = EdFacet (t, 0)))
            test_hull (e);
          else if (trist_hull_facet (e = EdFacet (t, 1)))
            test_hull (e);
          else
            Assert_always (FALSE);  /* should never be reached */
        }
  }
  return (TRUE);
}

/*--------------------------------------------------------------------------*/

static void test_hull (int h)
     /* Tests hull-facet h globally. */
{
  Basic_byte *hullbit;
  int is_wrong = FALSE;
  int t = TrIndex (h);
  hullbit = MALLOC (Basic_byte, trist_last () + 1);
  BZERO (hullbit,   Basic_byte, trist_last () + 1);
  hullbit[t] = TRUE;
  Assert_always (trist_hull_facet (h));
  test_hull_recursive (hullbit, Sym (Fnext (h)));
  test_hull_recursive (hullbit, Sym (Fnext (Enext (h))));
  test_hull_recursive (hullbit, Sym (Fnext (Enext2 (h))));
  trist_for (t)
    if (   (trist_hull_triangle (t) and (not hullbit[t]))
        or (hullbit[t] and (not trist_hull_triangle (t))))
      {
        print ("test_hull: Ochsenscheisse: t%d\n", t);
        is_wrong = TRUE;
      }
  FREE (hullbit);
  Assert_always (not is_wrong);
}

/*--------------------------------------------------------------------------*/

static void test_hull_recursive (Basic_byte hb[], int h)
     /* Input/Output: hb[]. */
{
  int t = TrIndex (h);
  if (not hb[t])
    {
      hb[t] = TRUE;
      Assert_always (trist_hull_facet (h));
      test_hull_recursive (hb, Sym (Fnext (h)));
      test_hull_recursive (hb, Sym (Fnext (Enext (h))));
      test_hull_recursive (hb, Sym (Fnext (Enext2 (h))));
    }
}

/*--------------------------------------------------------------------------*/

static void test_complex (void)
     /* Tests Fnext rings. */
{
  int t, i, a, b, length, origin, max = EdFacet (trist_max (), 5);
  print ("  [topology]\n");
  trist_for (t)
    upfor (i, EdFacet (t, 0), EdFacet (t, 5))
      {
        origin = Org (i);
        Assert_always ((trist_max_org () >= origin) and (origin != Dest (i)));
        a = Fnext (Sym (Fnext (Sym (i))));
        b = Sym (Fnext (Sym (Fnext (i))));
        Assert_always ((a == b) and (a == i));
        length = 0;
        a = i;
        b = Sym (i);
        while ((length < max + 1) and (i != (a = Fnext (a))))
          {
            length ++;
            b = Fnext (b);
            Assert_always ((i != b) and (origin == Org (a)));
          }
        Assert_always (length <= max);
      }
}

/*--------------------------------------------------------------------------*/

static void test_triangulation (void)
     /* Tests data structure for being a triangulation; locally.
        Assumes hull bits are set */
     /* QUESTION: DO WE TEST EVERYTHING OR TOO MUCH? */
{
  int i, a, s, t, x, y, z, o, x1, y1, p, x2, y2;
  print ("  [geometry]\n");
  trist_for (t)
    {
      if (trist_hull_facet (i = EdFacet (t, 0)))
        i = Sym (i);
      trist_triangle (i, &x, &y, &z);
      a = Fnext (i);
      s = Sym (i);
      trist_triangle (a, &x1, &y1, &o);
      trist_triangle (Fnext (s), &y2, &x2, &p);
      Assert_always ((x1 == x) and (y1 == y) and (x2 == x) and (y2 == y));
      (void) dt_test_open_tetra (a, x, y, z, o);
      (void) dt_test_triangle (Turn (s), x, o, z);
      (void) dt_test_triangle (Turn (a), y, z, o);
      Assert_always (positive3_hook (o, x, y, z));
      if (not (trist_hull_facet (s)))
        Assert_always (positive3_hook (p, y, x, z));
    }
}

/*--------------------------------------------------------------------------*/
       
int dt_test_triangle (int ef, int x, int y, int z)
     /* Test edge-ring ef to be the triangle x, y, z. */
{
  int vx, vy, vz;
  proto (print ("dt_test_triangle: [%d] = %d,%d,%d\n", ef, x, y, z));
  trist_triangle (ef, &vx, &vy, &vz);
  Assert_always ((x == vx) and (y == vy) and(z == vz));
  return (TRUE);
}

/*--------------------------------------------------------------------------*/

int dt_test_open_tetra (int a, int x, int y, int z, int o)
     /* Tests edge-rings of a tetrahedron (locally). */
{
  int b, c;
  proto (print ("dt_test_open_tetra: [%d] = %d,%d,%d;%d\n", a, x, y, z, o));
  b = Turn (a);
  c = Turn (b);
  (void) dt_test_triangle (a, x, y, o);
  (void) dt_test_triangle (b, y, z, o);
  (void) dt_test_triangle (c, z, x, o);
  Assert_always (a == Turn (c));  /* a == a.Turn.Turn.Turn */
  return (TRUE);
}

/*--------------------------------------------------------------------------*/

static void test_tetra_global (int a, int b, int c, int d, const int vertex[],
                               int m, int closest_flag, int delaunay_flag)
     /* Global geometric test of triangulation:  tests whether any other
        point is inside given tetrahedron; if delaunay_flag==TRUE, also
        tests for global Delaunayhood.  EXTREMELY SLOW!  O(n^4) */
{
  int i, ell;
  int pos_a, pos_b, pos_c, pos_d;
  fprint (stderr, "T");
  pos_a = positive3_hook (a, b, c, d);
  pos_b = positive3_hook (b, a, c, d);
  pos_c = positive3_hook (c, a, b, d);
  pos_d = positive3_hook (d, a, b, c);
  upfor (i, 1, m)
    {
      ell = vertex[i];
      if ((ell != a) and (ell != b) and (ell != c) and (ell != d))
        {
          /* no point inside */
          Assert_always (not (    (pos_a == positive3_hook (ell, b, c, d))
                              and (pos_b == positive3_hook (ell, a, c, d))
                              and (pos_c == positive3_hook (ell, a, b, d))
                              and (pos_d == positive3_hook (ell, a, b, c))));
          if (delaunay_flag)
            /* Delaunay hood */
            Assert_always (closest_flag != in_sphere_hook (a, b, c, d, ell));
        }
    }
}

/*--------------------------------------------------------------------------*/

int dt_test_pedantic (const int vertex[], int m, int closest_flag)
     /* Pedantic but slow test.  NOTE: vertex can be NULL! */
{
  int t, i;
  int x, y, z, o, x1, y1;
  int global_flag = GLOBAL_TEST (m);
  (void) dt_test ();
  if (global_flag)
    fprint (stderr, "GLOBAL dt_test_triangle: ");
  trist_for (t)
    {
      if (trist_hull_triangle (t))
        ; /* pre 2.0 had padantic_ch test here */
      else
        {
          if (global_flag)
            {
              trist_triangle (i = EdFacet (t, 0), &x, &y, &z);
              trist_triangle (Fnext (i), &x1, &y1, &o);
              Assert_always ((x1 == x) and (y1 == y));
              if (vertex)
                test_tetra_global (x, y, z, o, vertex, m, closest_flag, TRUE);
            }
        }
    }
  if (global_flag)
    fprint (stderr, ".\n");
  return (TRUE);
}

/*--------------------------------------------------------------------------*/

void dt_print_info_sec (FILE *info, double secs, const char title[])
{
  int h = (int)  (secs / 3600.0);
  int m = (int) ((secs - 3600 * h) / 60.0);
  if (h > 0)
    fprint (info, "%12.2f . %s (%d h %d min)\n", secs, title, h, m);
  else if (m > 0)
    fprint (info, "%12.2f . %s (%d min)\n", secs, title, m);
  else
    fprint (info, "%12.2f . %s\n", secs, title);
}
