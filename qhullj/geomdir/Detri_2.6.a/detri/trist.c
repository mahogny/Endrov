/* detri/trist.c --- Trist module: 3d TRIangle-edge data STructure. */

/*---------------------------------------------------------------------------*/

const char trist__version[] = "@(#) Trist Module 2.6";
const char trist__authors[] = "Ernst Mucke";
#ifdef __DEBUG__
const char trist__compile[] = "@(#) \t w/ -D__DEBUG__";
#endif

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include "trist.h"

/*--------------------------------------------------------------------------*/

/* configuring trist.c */

#define MAGIC 130862015
int trist__internal_magic_number = MAGIC;
/* Trist magic number ("silently" exported to: dt.c, don't tell anybody :). */

#define ENLARGE  2.0
/* Factor to enlarge current Trist_record array when maximum index reached.
   Set to 1.0 to disable this feature! */

#define REUSE  TRUE
/* Set REUSE to FALSE, if you don't want to (re)use deleted trangle records.
   This might be helpful for debugging!  Normally, it should be set to TRUE;
   otherwise overflow is very likely: "No more space for triangles!" */

/*--------------------------------------------------------------------------*/

/* local procedures and variables */

static Trist *st = NULL;         /* pointer to current Trist */
static Trist_record *tr = NULL;  /* shortcut for st->triangle */

static int vo[6] = { 0, 1, 1, 2, 2, 0 };  /* origins wrt triangle version */
static int ve[6] = { 2, 5, 4, 1, 0, 3 };  /* enexts  ... */

#define UNDEFINED  -9999

static Basic_counter orgs, syms, enexts, fnexts, fsplices;
static int min_ef_fnexts;

static void equality (int number, int left, int right);
static int hull_compare (const int *t1, const int *t2);

#ifdef __DEBUG__
 static void count_edges (int *edges, int *edges_hull);
 static int edge_index  (int e, const int dope[]);
 static int *new_edge_dope (int *max_index);
 static void kill_edge_dope (int *dope);
#endif
 
/* Called at end of trist_permute () */
static int (* permute_hook) (const int []) = NULL;
     
/*--------------------------------------------------------------------------*/

/* origin indices and obits */

#define MAX_ORG   ((Trist_org) powerof2 (bitsof (Trist_org) - 1) - 1)

#define org_Mask  ((Trist_org)  MAX_ORG)  /* bin: 011...1, hex: 7FF...F */
#define bit_Mask  ((Trist_org) ~MAX_ORG)  /* bin: 100...0, hex: 800...0 */

#define obit(T,B)      (tr[T].origin[B] != (tr[T].origin[B] & org_Mask))
#define set_obit(T,B)   tr[T].origin[B] =  (tr[T].origin[B] | bit_Mask)
#define del_obit(T,B)   tr[T].origin[B] =  (tr[T].origin[B] & org_Mask)

/* NOTE: Above three macros behave like functions! */

/*--------------------------------------------------------------------------*/

/* handy macros */

#define div8 trist_Div8
#define mul8 trist_Mul8
#define mod8 trist_Mod8

#define wrong_org(I,ST)  ((0 > I) or (I > ST->max_org))

#define sym_macro(E)  If (Odd (E), (E - 1), (E + 1))

#ifdef __DEBUG__

#define test(FUNC,T,V) \
  do { \
    if (/* trace(T): */ FALSE) \
      print ("traced t%d at %s\n", T, FUNC); \
    if (trist_test_flag) \
      if ((T < 1) or (T > st->last_triangle) or (V > 5) \
          or (trist_deleted (T))) \
       basic_error ("%s: accessing unused triangle [%d] t=%d v=%d h=%d d=%d", \
               FUNC, EdFacet (T, V), T, V, st->last_triangle, \
               trist_deleted (T)); \
  } once

#else

#define test(FUNC,T,V) /* empty */

#endif

/*--------------------------------------------------------------------------*/

/* ============================ Core routines. ============================ */

/*--------------------------------------------------------------------------*/

int trist_upper_bound (int n)
     /* Returns upper bound for number of triangles in 3D triangulation with
        n vertices.  This uses the Upper Bound Theorem [see H Edelsbrunner,
        Algoritms in Combinatorial Geometry, Springer-Verlag, 1987, page 117]
        for k==1 in dual space.  HOWEVER: If n is so large to make n * n
        overflow, MAXINT is returned instead... as a "hard-wired" upper bound,
        so to speak.  This happens when n > 46340.  A warning will be issued
        in this case (but at most one per execution!). */
{
  if (n * n < n)  /* looks like int overflow!?! */
    {
      static int first_flag = TRUE;
      if (first_flag)
        {
          print ("WARNING: trist_upper_bound (n = %d) int overflow!  %s\n",
                 n, "Returning MAXINT.");
          first_flag = FALSE;
        }
      return (MAXINT);
    }
  else
    return (n * (n - 3) + 1);
}

/*--------------------------------------------------------------------------*/

Trist* trist_alloc (int max_n, int m)
     /* Allocates Trist for (initial) vertex index range 1..max_n and
        (initial!) triangle index range 1..m. The triangle index range
        can internally be changed by trist_make() calls; the vertex index
        range can be changed by trist_max_org_set(). */
{
  Trist_record *tr;
  Trist *s = MALLOC (Trist, 1);
  if (max_n > MAX_ORG)
    basic_error ("trist_alloc: vertex range to big, %d > %d", max_n, MAX_ORG);
  s->magic = MAGIC;
  s->max_org = max_n;
  s->max_triangle = m;
  tr = MALLOC (Trist_record, m + 1);
  s->triangle = tr;
  s->data_size = 0;
  s->data = NULL;
  trist_clear (s);
  return (s);
}

/*--------------------------------------------------------------------------*/

void trist_kill (Trist *s)
     /* Deallocates Trist s. */
{
  Assert_always (s->magic == MAGIC);
  if (s == st)
    {
      st = NULL;
      tr = NULL;
    }
  FREE (s->triangle);
  FREE (s->data);
  FREE (s);
}

/*--------------------------------------------------------------------------*/

void trist_set (Trist *s)
     /* Sets s to be the current Trist. */
{
  st = s;
  tr = If (s, s->triangle, NULL);
}

/*--------------------------------------------------------------------------*/

void trist_copy (const Trist *a, Trist *b)
     /* Copies Trist a to Trist b, assuming b empty and large enough. */
{
  if ((not a) or (not b)
      or (b->max_org != a->max_org)
      or (b->last_triangle > a->max_triangle)
      or (b->used_triangles != 0))
    basic_error ("trist_copy: can't copy");
  b->used_triangles = a->used_triangles;
  b->last_triangle = a->last_triangle;
  b->next_reusable_triangle = a->next_reusable_triangle;
  bcopy (a->triangle, b->triangle, 
         (a->last_triangle + 1) * (int) sizeof (Trist_record));
  bcopy (a->data, b->data, 
         (a->last_triangle + 1) * a->data_size);
}

/*--------------------------------------------------------------------------*/

void trist_clear (Trist *s)
     /* Clears s. */
{
  s->last_triangle = 0;
  s->next_reusable_triangle = UNDEFINED;
  s->used_triangles = 0;
  basic_counter_reset (&orgs);
  basic_counter_reset (&syms);
  basic_counter_reset (&enexts);
  basic_counter_reset (&fnexts);
  basic_counter_reset (&fsplices);;
  min_ef_fnexts = 0;
}

/*--------------------------------------------------------------------------*/

Trist* trist_current (void)
     /* Returns a pointer to the current Trist. */
{
  return (st);
}

/*--------------------------------------------------------------------------*/

int trist_max (void)
     /* Returns the high-end of the current triangle index range.
        The might be changed internally, whenever the Trist gets enlarged
        (vis trsit_make() calls). */
{
  return (st->max_triangle);
}

/*--------------------------------------------------------------------------*/

int trist_last (void)
     /* Returns the largest index of any undeleted triangle. */
{
  return (st->last_triangle);
}

/*--------------------------------------------------------------------------*/

int trist_max_org (void)
     /* Returns the high-end of the current vertex index range index range.
        Can be changed by the user with trist_max_org_set() below. */
{
  return (st->max_org);
}

/*--------------------------------------------------------------------------*/

void trist_max_org_set (int n_max)
     /* Changes the vertex index range to new 1..n_max.
        Remark: Why do we even need this vertex index range?  Well, it's
        mainly for debugging and control checks while execution; nevertheless
        it always needs to be specified.  Changing it costs O(1) only time. */
{
  Assert_always (n_max > st->max_org);
  st->max_org = n_max;
}

/*--------------------------------------------------------------------------*/

int trist_make (int i, int j, int k)
     /* Creates a new (isolated) triangle and returns edfacet ijk.
        If necessary, will enlarge the triangle index range by a factor of
        ENLARGE; needs O(trist_last()) time for reallocation, but this turns
        out to be very fast in practice, esp, with ENLARGE == 2.0. */
        
{
  int v, t;
#ifdef __DEBUG__
  if (trist_test_flag)
    if (wrong_org (i, st) or wrong_org (j, st) or wrong_org (k, st))
      basic_error ("trist_make: Wrong origins.  %d %d %d", i, j, k);
#endif
  if (st->next_reusable_triangle == UNDEFINED)
    { /* take a new Trist_record */
      st->last_triangle ++;
      t = st->last_triangle;
    }
  else
    { /* Reuse a deleted Trist_record */
      t = st->next_reusable_triangle;
      st->next_reusable_triangle = tr[t].fnext[0];  /* pop! */
    }
  if (t > st->max_triangle)
    { /* overflow */
      int ub = trist_upper_bound (st->max_org + 1); 
      int n = Min (ub, (int) (ENLARGE * st->max_triangle + 0.5));
      Assert (t == st->last_triangle);
      if (ENLARGE <= 1.0)
        basic_error ("trist_make: overflow!");
      else if (t > ub)
        basic_error ("trist_make: reached upper bound!");
      else
        { /* try to enlarge the tr[..] (and data[..]) array(s). */
          print ("\n[trist_make: resizing for %d triangles ...", n);
          REALLOC (tr, Trist_record, n + 1);  /* O(trist_last()) time! */
          st->triangle = tr;
          st->max_triangle = n;
          if (st->data)
            REALLOC (st->data, char, st->data_size * (n + 1));
          print (" done] \n");
        }
    }
  st->used_triangles ++;
  tr[t].origin[0] = (Trist_org) i;
  tr[t].origin[1] = (Trist_org) j;
  tr[t].origin[2] = (Trist_org) k;
  upfor (v, 0, 5)
    tr[t].fnext[v] = EdFacet (t, v);
  return (EdFacet (t, 0));
}

/*--------------------------------------------------------------------------*/

void trist_delete (int e)
     /* Deletes triangle of edfacet e (and pushes record into free list).
        NOTE: it's not allowed to delete a triangle when its hull bit
              is set.  (Strange rule, but still true. :) */
{
  int t = TrIndex (e), d = Enext (e), f = Enext2 (e);
#ifdef __DEBUG__
  if (trist_test_flag) if (trist_hull_triangle (t))
    basic_error ("trist_delete: Das darf man nicht.");
#endif
  ;
  /* isolate triangle within Trist */
  Fsplice (Sym (Fnext (Sym (e))), e);
  Fsplice (Sym (Fnext (Sym (d))), d);
  Fsplice (Sym (Fnext (Sym (f))), f);
  ;
  /* "Deleting" a triangle means to set the first two origins equal.
     However, in order to make trist_hull_triangle(), etc, work w/o calling
     trist_deleted() first, they (ie, their hull bits) are set to 0.
     (Is this still really necessary?) */ 
  tr[t].origin[0] = tr[t].origin[1] = 0;
  st->used_triangles --;
  ;
  if (REUSE)
    { /* push onto free list */
      tr[t].fnext[0] = st->next_reusable_triangle; 
      st->next_reusable_triangle = t;
    }
}

/*--------------------------------------------------------------------------*/

int trist_deleted (int t)
     /* Returns TRUE if triangle t is deleted. */
{
  return (tr[t].origin[0] == tr[t].origin[1]);
}

/*--------------------------------------------------------------------------*/

int trist_org (int e)
     /* Basic triangle-edge function; abbrev: Org (e). */
{
  int t = TrIndex (e), v = TrVersion (e);
  basic_counter_plus (&orgs, 1);
  return ((int) (tr[t].origin[vo[v]] & org_Mask));
}

/*--------------------------------------------------------------------------*/

int trist_sym (int e)
     /* Basic triangle-edge function; abbrev: Sym (e). */
{
  basic_counter_plus (&syms, 1);
  return (sym_macro (e));
}

/*--------------------------------------------------------------------------*/

int trist_enext (int e)
     /* Basic triangle-edge function; abbrev: Enext (e). */
{
  int t = TrIndex (e), v = TrVersion (e);
  test ("trist_enext", t, v);
  basic_counter_plus (&enexts, 1);
  return (EdFacet (t, ve[v]));
}

/*--------------------------------------------------------------------------*/

int trist_fnext (int e)
     /* Basic triangle-edge function: abbrev: Fnext (e). */
{
  int t = TrIndex (e), v = TrVersion (e);
  test ("trist_fnext", t, v);
  basic_counter_plus (&fnexts, 1);
  return (tr[t].fnext[v]);
}

/*--------------------------------------------------------------------------*/

int trist_enext2 (int e)
     /* Returns Enext (Enext (e)); abbrev: Enext2 (e). */
{
  int t = TrIndex (e), v = TrVersion (e);
  test ("trist_enext2", t, v);
  basic_counter_plus (&enexts, 2);
  return (EdFacet (t, ve[ve[v]]));
}

/*--------------------------------------------------------------------------*/

int trist_turn (int e)
     /* Returns Enext (Fnext (Sym (Enext (e)))); abbrev: Turn (e). */
{
  int t = TrIndex (e), v = TrVersion (e);
  test ("trist_turn", t, v);
  basic_counter_plus (&enexts, 2);
  basic_counter_plus (&fnexts, 1);
  basic_counter_plus (&syms, 1);
  v = ve[v];
  v = sym_macro (v);
  e = tr[t].fnext[v];
  t = TrIndex (e);
  v = TrVersion (e);
  return (EdFacet (t, ve[v]));
}

/*--------------------------------------------------------------------------*/

int trist_dest (int e)
     /* Returns Org (Sym (e)); abbrev: Dest (e). */
{
  int t = TrIndex (e), v = TrVersion (sym_macro (e));
  basic_counter_plus (&orgs, 1);
  basic_counter_plus (&syms, 1);
  return ((int) (tr[t].origin[vo[v]] & org_Mask));
}

/*--------------------------------------------------------------------------*/

void trist_fsplice (int a, int b)
     /* Triangle-edge function; abbrev: Fsplice (a, b). */
{
#define swapfnext(A,B)  ta = TrIndex (A);  va = TrVersion (A);  \
                        tb = TrIndex (B);  vb = TrVersion (B);  \
                        aux = tr[ta].fnext[va];  \
                        tr[ta].fnext[va] = tr[tb].fnext[vb];  \
                        tr[tb].fnext[vb] = aux
  int alpha = Sym (Fnext (a)), beta = Sym (Fnext (b));
  int ta, tb, va, vb, aux;
  basic_counter_plus (&fsplices, 1);
  swapfnext (a, b);
  swapfnext (alpha, beta);
#undef swapfnext
}

/*--------------------------------------------------------------------------*/

void trist_triangle (int e, int *i, int *j, int *k)
     /* Outputs endpoints of edfacet e == ijk */
{
  int t = TrIndex (e), v = TrVersion (e);
  *i = (int) (tr[t].origin[vo[v]] & org_Mask);   v = ve[v];
  *j = (int) (tr[t].origin[vo[v]] & org_Mask);   v = ve[v];
  *k = (int) (tr[t].origin[vo[v]] & org_Mask);
#ifdef __DEBUG__
  if (trist_test_flag)
    if ((i == j) or (i == k) or (k == j) or (ve[v] != TrVersion (e)))
      basic_error ("Triangle: Given edge-ring is not triangular.");
#endif
}

/*--------------------------------------------------------------------------*/

void trist_tetra (int e, int *i, int *j, int *k, int *o)
     /* Outputs endpoints of tetrehedron e^+ == ijko.
        NOTE: *o == 0 iff trist_hull_facet (e) == TRUE. */
{
  trist_triangle (e, i, j, k);
  *o = If ((trist_hull_facet (e) == FALSE), Dest (Enext (Fnext (e))), 0);
}  

/*--------------------------------------------------------------------------*/

void trist_hull_facet_set (int e, int flag)
     /* Sets triangle's hull bit to TURE (for flag != 0) or FALSE.  This
        implicitly sets the triangle's "color" to HULL, SYMHULL, or BLANK. */
{
  int t = TrIndex (e);
  test ("trist_hull_facet_set", t, TrVersion (e));
#ifdef __DEBUG__
  if (trist_test_flag)
    {
      if ((flag == trist_hull_triangle (t)))
        basic_error ("trist_hull_facet_set: Overcoloring.");
    }
#endif
  if (flag)
    {
      set_obit (t, 0);
      del_obit (t, 1);
      if (Odd (e))
        set_obit (t, 2);
      else
        del_obit (t, 2);
    }
  else
    {
      del_obit (t, 0);
      del_obit (t, 1); /* not really needed! */
      del_obit (t, 2);
    }
}

/*--------------------------------------------------------------------------*/

int trist_hull_triangle (int t)
     /* Returns the hull bit of triangle t. */
{
  test ("trist_hull_triangle", t, 0);
  return (obit (t, 0) and (not obit (t, 1)));
}

/*--------------------------------------------------------------------------*/

int trist_hull_facet (int e)
     /* Returns TRUE iff triangle of e lies on hull and e points outward. */
{
  int t = TrIndex (e);
  test ("trist_hull_facet", t, 0);
  return (obit (t, 0) and (not obit (t, 1)) and (Odd (e) == obit (t, 2)));
}

/*--------------------------------------------------------------------------*/

int trist_tetra_min_ef (int e)
     /* Returns key "min_ef" for tetrahedron on top of edfacet e, ie: e^+.
        NOTE: Since edfacets are internally represented by int numbers,
        it makes sense to talk of minima: min_ef = minimum edfacet bounding
        the tetrahedron. */
{
  int x, a, b, c, d, min_ef;
  a = x = e;
  b = Sym (Fnext (x));
  c = Sym (Fnext (x = Enext (x)));
  d = Sym (Fnext (x = Enext (x)));
  x = Min (Min (Min (a, b), c), d);
  min_ef = EdFacet (TrIndex (x), If (Odd (x), 1, 0));
  Assert (min_ef != 0);
  return (min_ef);
}

/*--------------------------------------------------------------------------*/

int trist_edge_min_ef (int e)
     /* Returns key "min_ef" for edge from Org (e) to Dest (e). */
{
  int min_ef, aux;
  min_ef = e;
  aux = Fnext (e);
  min_ef_fnexts ++;
  while (aux != e)
    {
      if (aux < min_ef)
        min_ef = aux;
      aux = Fnext (aux);
      min_ef_fnexts ++;
    }
  if (Odd (min_ef))
    min_ef = Sym (min_ef);
  Assert (min_ef != 0);
  return (min_ef);
}

/*--------------------------------------------------------------------------*/

int trist_vertices (void)
     /* Returns the number of "active" (or: nonduplicate, nonredundant)
        vertices in current Trist.  O(trist_last()) time. */
{
  int t, v, a, b, c;
  int mv = trist_max_org ();
  Basic_byte *hbit;
  hbit = MALLOC (Basic_byte, mv + 1);
  BZERO (hbit,   Basic_byte, mv + 1);
  trist_for (t)
    {
      trist_triangle (EdFacet (t, 0), &a, &b, &c);
      hbit[a] = hbit[b] = hbit[c] = TRUE;
    }
  v = 0;
  upfor (a, 1, st->max_org)
    if (hbit[a])
      v ++;
  FREE (hbit);
  return (v);
}

/*--------------------------------------------------------------------------*/

Trist_num trist_num (void)
     /* Returns Trist_num quantities of current Trist.
        Shouldn't be called too often, because it's not too efficient.
        O(trist_last()) time. */
{
  Trist_num tq;
  int t, t_flat, f = st->used_triangles, e, f_hull, e_hull, v_hull;
  int i, j, a, b, c, d;
  int v = trist_vertices ();
  int mv = trist_max_org ();
  Basic_byte *hbit;
  ;
  /* count tetrahedra */
  t = t_flat = 0;
  trist_for (j)
    upfor (i, EdFacet (j, 0), EdFacet (j, 1))
      if (    (not trist_hull_facet (i))
          and (j == TrIndex (trist_tetra_min_ef (i))))
        {
          t ++;
          trist_triangle (i, &a, &b, &c);
            d = Dest (Enext (Fnext (i)));
          if (lia_sign (sos_minor4 (a, b, c, d, 1, 2, 3, 0)) == 0)
            t_flat ++;
        }
  ;
  /* count hull vertices and triangles...
     by scanning whole triangulation via trist_for loop */
  f_hull = v_hull = 0;
  hbit = MALLOC (Basic_byte, mv + 1);
  BZERO (hbit,   Basic_byte, mv + 1);
  trist_for (i)
    if (trist_hull_triangle (i))
      {
        f_hull ++;
        trist_triangle (EdFacet (i, 0), &a, &b, &c);
        hbit[a] = hbit[b] = hbit[c] = TRUE;
      }
  upfor (i, 1, mv)
    if (hbit[i])
      v_hull ++;
  FREE (hbit);
  ;
#ifdef __DEBUG__
  /* count edges using time-consuming count_edges() */
  count_edges (&e, &e_hull);
  print ("[ trist_num: v=%d, e=%d, f=%d, t=%d;  vH=%d, eH=%d, fH=%d ]\n",
         v, e, f, t, v_hull, e_hull, f_hull);
#else
  /* get edge numbers from Euler formula (ie equalities (1) and (3)) */
  e = v + f - t - 1;
  e_hull = v_hull + f_hull - 2;
#endif
  ;
  /* check Euler formulas, etc */
  equality (1,  v_hull - e_hull + f_hull,  2);
  equality (2,  3 * f_hull,  2 * e_hull);
  equality (3,  v - e + f - t,  1);
  equality (4,  4 * t,  f_hull + 2 * (f - f_hull));
  equality (5,  f_hull,  2 * v_hull - 4);
  equality (6,  f - f_hull,  v_hull + 2 * ((e - e_hull) - (v - v_hull)) - 4);
  equality (7,  t,  e - (v + v_hull) + 3);
  ;
  /* return Trist_num record */
  tq.v = v;
  tq.e = e;
  tq.f = f;
  tq.t = t;
  tq.vh = v_hull;
  tq.eh = e_hull;
  tq.fh = f_hull;
  tq.t_flat = t_flat;
  tq.t_proper = t - t_flat;
  return (tq);
}

static void equality (int number, int left, int right)
     /* Refinement for the above. */
{
  if (left != right)
    basic_error ("equality (%d) violated", number);
}

int trist_num_eq (Trist_num a, Trist_num b, char code)
     /* Compares two Trist_num records a and b;
        code == 'h' ... hull faces only;
        code == '*' ... all faces. */
{
  int is_correct = TRUE;
  switch (code)
    {
     case '*':
      if ((a.v != b.v) or (a.e != b.e) or (a.f != b.f) or (a.t != b.t))
        is_correct = FALSE;
      /* note: no "break;" here */
     case 'h':
      if ((a.vh != b.vh) or (a.eh != b.eh) or (a.fh != b.fh))
        is_correct = FALSE;
      break;
     default:
      basic_error ("trist_num_eq: wrong code");
    }
  if (not is_correct)
    print ("%s (%d,%d,%d,%d, %d,%d,%d, %d,%d,%d,%d, %d,%d,%d, '%c') %s\n",
           "trist_num_eq",
           a.v, a.e, a.f, a.t,  a.vh, a.eh, a.fh,
           b.v, b.e, b.f, b.t,  b.vh, b.eh, b.fh, code, "returns FALSE");
  return (is_correct);
}   

/*--------------------------------------------------------------------------*/

Trist_info* trist_info (void)
     /* Returns some Trist info. */
     /* NOTE: The returned address, which points to the info structure,
              is a constant.  DO NOT FREE() IT, and consider the fields
              of the structure as read-only. */
{
  static Trist_info ti;
  int f_size = (int) sizeof (Trist_record) + st->data_size;
  if (not st)
    basic_error ("trist_info: undefined Trist.");
  ti.orgs     = orgs;
  ti.syms     = syms;
  ti.enexts   = enexts;
  ti.fnexts   = fnexts;
  ti.fsplices = fsplices;
  ti.min_ef_fnexts = min_ef_fnexts;
  ti.bpt      = (short) sizeof (Trist_record);
  ti.bytes    = (unsigned long) (st->last_triangle + 1) * f_size;
  ti.maxbytes = (unsigned long) (st-> max_triangle + 1) * f_size;
  return (&ti);
}

/*--------------------------------------------------------------------------*/

/* ========================= Auxiliary functions. ========================= */

/*--------------------------------------------------------------------------*/

void trist_print (FILE * file, int t)
     /* ASCII printout of triangle record t to given file. */
{
  int i, j, k, v;
  trist_triangle (EdFacet (t, 0), &i, &j, &k);
  fprint (file, "%d: %d %d %d -->", t, i, j, k);
  upfor (v, 0, 5)
    fprint (file, " %d.%d",
            TrIndex (tr[t].fnext[v]), TrVersion (tr[t].fnext[v]));
  fprint (file, "\n");
}

/*--------------------------------------------------------------------------*/

int trist_pack (void)
     /* "Packs" the current Trist and returns last_hull.  This means:
        - get rid of all "holes" (deleted triangles) in [1..trist_last()];
        - move all hull triangles to the front [1..last_hull<=trist_last()]. */
{
  int *map;
  int last_hull = trist_pack_n_keep (&map);  /* all the work is done below! */
  FREE (map);
  return (last_hull);
}

int trist_pack_n_keep (int **pmap)
     /* Output: *pmap. */
     /* See discription of trist_pack() -- plus: keeps the permutation map
        pmap[1..st->used_triangles], where map[t] == new index of triangle t,
        so that the caller can modify external edge references.  It's the
        caller's responsibility to deallocate pmap[]. (Note 1: This is a
        refinement of trist_pack(); in fact, it's its main body. :)
        (Note 2: You can also use trist_permute_hook() as an alternative.) */
{
  int last_hull, t, u;
  static int *map;
  double t0 = basic_utime ();
  if (st->data)
    basic_error ("Sorry, trist_pack*() w/ trist_data: not yet implemented.\n");
  ;
  /* allocate map[]; caller must deallocate! */
  *pmap = map = MALLOC (int, st->last_triangle + 1);
  print ("  Packing %d triangles (%d used) ... ",
         st->last_triangle, st->used_triangles);
  flush ();
  ;
  /* collect all hull triangles... */
  /* NOTE: collecting hull could be done faster with DFS on hull */
  u = map[0] = 0;
  trist_for (t)
    if (trist_hull_triangle (t))
      {
        u ++;
        map[t] = u;
      }
  last_hull = u;
  ;
  /* ... and then the rest */  
  trist_for (t)
    if (not trist_hull_triangle (t))
      {
        u ++;
        map[t] = u;
      }
  ;
  /* permutation moves hull triangles to front and gets rid of holes */
  trist_permute (map);
  Assert_always (    (u == st->used_triangles)
                 and (st->max_triangle == st->last_triangle)
                 and (st->last_triangle == st->used_triangles));
  print ("done: %.2fs.\n", basic_utime () - t0);
  return (last_hull);
}

/*--------------------------------------------------------------------------*/

int trist_pack_n_sort (void)
     /* This is the old version of trist_pack(), which also sorted the
        hull triangles lexicographically. This is not really needed anymore. */
{
  int *map;   /* map[t] is new index of triangle t; cf: trist_pack_n_keep() */
  int *arr;   /* arr[t] ... t is the (sorted) position of triangle arr[t] */
  int t;
  int last_hull = trist_pack_n_keep (&map);
  double t0 = basic_utime ();
  ;
  /* collect hull triangles for sorting */
  print ("  Sorting %d hull triangles ...", last_hull);
  flush ();
  arr = MALLOC (int, last_hull + 1);
  upfor (t, 1, last_hull)
    arr[t] = t;
  basic_qsort (arr, 1, last_hull, hull_compare);
  upfor (t, 1, last_hull)
    map[arr[t]] = t;
  upfor (t, last_hull + 1, st->used_triangles)
    map[t] = t;
  ;
  /* this permutation sorts hull */
  trist_permute (map);
  Assert_always (    (st->max_triangle == st->last_triangle)
                 and (st->last_triangle == st->used_triangles));
  ;
  FREE (arr);
  FREE (map);  /* as allocated by trist_pack_n_keep() */
  print (" done: %.2fs.\n", basic_utime () - t0);
  return (last_hull);
}

static int hull_compare (const int *t1, const int *t2)
     /* That's how the hull triangles get sorted in trist_pack(). */
{
  int i1, j1, k1, i2, j2, k2;
  trist_triangle (EdFacet (*t1, 0), &i1, &j1, &k1);
  trist_triangle (EdFacet (*t2, 0), &i2, &j2, &k2);
  (void) basic_isort3 (&i1, &j1, &k1);
  (void) basic_isort3 (&i2, &j2, &k2);
  if (i1 < i2)
    return (-1);
  else if (i1 == i2)
    {
      if (j1 < j2)
        return (-1);
      else if (j1 == j2)
        {
          if (k1 < k2)
            return (-1);
          else
            return (1);
        }
      else
        return (1);
    }
  else
    return (1);
}

/*--------------------------------------------------------------------------*/

void trist_permute (const int map[])
     /* Permutes Trist_record's of current Trist such that map[t] is the
        new index of triangle t, provided, t denotes an active triangle.
        Needs O(st->last_triangle) time AND space.  The "active" indices
        in map[1..st->last_triangle] are a permutation of all indices 
        t between 1..st->last_triangle wich denote active triangles!
        Clear? */
     /* NOTE: This is the new "in-place" version of trist_permute()
        written by Mike Facello.  Of course, it's not really "in-place"
        because it uses this bit-vector, but it's still saving a lot of
        memory. */
{
  int j, fn, new_t, t = 0;
  Trist_record temp_tr, swap_tr;
  Basic_byte *moved;
  ;
  /* Allocate a bit vector indicating whether a record has been moved */
  moved = MALLOC (Basic_byte, ((trist_last() + 1) div8) + 1);
  BZERO (moved,   Basic_byte, ((trist_last() + 1) div8) + 1);
  /* A position has already been processed if the record was moved, or
     if there was no record there to be moved. */
#define processed(T)     (basic_charbit_on (T mod8, moved[T div8])\
                          or trist_deleted (T)\
                          or (map[T] <= 0))
#define set_processed(T) (moved[T div8] = \
                          basic_charbit_s1 (T mod8, moved[T div8]))
  ;
#ifdef DEBUG_PERMUTE
  print ("\n\nTHE NEW trist_permute()!\n\n");
  print ("\n\nTHE MAP:\n\n");
  trist_for (t)
    print ("map[%d] = %d\n", t, map[t]);
#endif
  ;
  trist_for (t)
    if (not processed (t))
      {
#ifdef __DEBUG__
        if (trist_test_flag)
          if ((map[t]< 0) or (map[t] > st->used_triangles))
            basic_error ("trist_permute: wrong map: map[%d]=%d", map[t], t);
#endif
        temp_tr = tr[t];
        new_t = map[t];
        set_processed (t);
#ifdef DEBUG_PERMUTE
        print ("     Moving %d to %d\n", t, map[t]);
#endif
        while (not processed(new_t))
          {
            set_processed (new_t);
            swap_tr = tr[new_t];
            tr[new_t] = temp_tr;
            upfor (j, 0, 5)
              {
                fn = tr[new_t].fnext[j];
                tr[new_t].fnext[j] = EdFacet (map [TrIndex (fn)],
                                              TrVersion (fn));
              }
#ifdef DEBUG_PERMUTE
            print ("     Moving %d to %d\n", new_t, map[new_t]);
#endif
            temp_tr = swap_tr;
            new_t = map[new_t];
          }
#ifdef DEBUG_PERMUTE
        print ("\n");
#endif
        /* Handle the final triangle in this cycle */
        tr[new_t] = temp_tr;
        upfor (j, 0, 5)
          {
            fn = tr[new_t].fnext[j];
            tr[new_t].fnext[j] = EdFacet (map [TrIndex (fn)],
                                          TrVersion (fn));
          }
        set_processed(new_t);
      }
  ;
  FREE (moved);
  st->max_triangle = st->last_triangle = st->used_triangles;
  st->next_reusable_triangle = UNDEFINED;
  if (permute_hook)
    permute_hook (map);
}

void trist_permute_hook (int (* hook) (const int []))
     /* This function sets the function hook that will be called at the
        and of each trist_permute() called.  The parturbation map[] will
        be passed to this function.  (Mike Facello) */
{
  permute_hook = hook;
}

/*--------------------------------------------------------------------------*/

void trist_hull_copy (const int vertex[], int vertex_hull[])
     /* Output: vertex_hull[]. */
     /* Scans the (convex) hull of current Trist and copies corresponding
        vertex indices from vertex[1..v] into vertex_hull[1..v_hull].
        Time: O(trist_last()), b/c of simple trist_for loop. */
     /* NOTE: Could be done faster with DFS and a pointer to hull,
        but is it worth it? */
{
  int i, j, t, x, y, z;
  int v = trist_vertices ();
  int mv = trist_max_org ();
  Basic_byte *hbit;
  hbit = MALLOC (Basic_byte, mv + 1);
  BZERO (hbit,   Basic_byte, mv + 1);
  trist_for (t)
    if (trist_hull_triangle (t))
      {
        trist_triangle (EdFacet (t, 0), &x, &y, &z);
        hbit[x] = hbit[y] = hbit[z] = TRUE;
      }
  i = j = 0;
  while (++i <= v)
    if (hbit[vertex[i]])
      vertex_hull[++j] = vertex[i];
  FREE (hbit);
}

/*--------------------------------------------------------------------------*/

void trist_color_set (int t, char rgb)
     /* Sets color of triangle t to rgb, on of 'r', 'g', 'b', or ' '.
        Assumes trist_hull_triangle (t) == FALSE.
        NOTE:
        - The internal "color" of a triangle is implemented by using the sign
          bits of the origin fields.  These are mainly used to mark the hull
          triangles with HULL, SYMHULL.
        - If three colors R, G, B, and the fact that you can't further color
          hull triangles is sufficient for your application, go ahead and use
          the trist_color*() functions.
        - Otherwise, it's better to use the trist_data*() feature. */
{
  test ("trist_color_set", t, 0);
#ifdef __DEBUG__
  if (trist_test_flag) if (trist_hull_triangle (t))
    basic_error ("trist_color_set: Overcoloring hull triangle?");
#endif
  switch (rgb)
    {
     case ' ':  del_obit (t, 0);  del_obit (t, 1);  del_obit (t, 2);  break;
     case 'r':  del_obit (t, 0);  del_obit (t, 1);  set_obit (t, 2);  break;
     case 'g':  del_obit (t, 0);  set_obit (t, 1);  del_obit (t, 2);  break;
     case 'b':  del_obit (t, 0);  set_obit (t, 1);  set_obit (t, 2);  break;
     default:  basic_error ("trist_color_set: Wrong color: '%c'", rgb);
    }
  Assert (rgb == trist_color (t));  /* paranoia? */
}

/*--------------------------------------------------------------------------*/

char trist_color (int t)
     /* Returns internal color of t (one of 'r', 'g', 'b', ' ').
        Returns '*' if trist_hull_triangle (t) == TRUE. */
{
  test ("trist_color", t, 0);
  if (obit (t, 0))
    return (If (obit (t, 1), '?', '*'));  /* hull ('*') or '?' */
  else
    if (obit (t, 1))
      return (If (obit (t, 2), 'b', 'g'));
    else
      return (If (obit (t, 2), 'r', ' '));
}

/*--------------------------------------------------------------------------*/

void trist_data_size (int record_size)
     /* Allows the user to maintain a "data" record (w/ record_size bytes)
        for each triangle t in the current Trist.  This is implemented in form
        of an array "parallel" to Trist's triangle[] array.
        NOTE: the data array gets properly resized whenever trist_make()
              dynamically resizes triangle[].
        USAGE: >
               > typedef struct whatever { ... } WHATEVER;
               > :
               > WHATEVER p;
               > :
               > trist_set (...);
               > trist_data_size ((int) sizeof (WHATEVER));
               >  :
               >  p = (WHATEVER *) trist_data_addr (some triangle index t);
               >  ... read/write acces to data fields via: p->BLAH_BLAH ...
               >  :
               > trist_data_size (0);  ...deallocation! */
{
  if (record_size <= 0)
    FREE (st->data);  /* deallocate */
  else
    { /* allocate */
      st->data_size = record_size;
      st->data = MALLOC (char, (int) record_size * (st->max_triangle + 1));
    }
}

/*--------------------------------------------------------------------------*/

char * trist_data_addr (int t)
     /* See above. */
{
  test ("trist_data_record", t, 0);
  if (st->data)
    return (&(st->data[st->data_size * t]));
  Assert (FALSE);
  return ((char *) NULL);
}

/*--------------------------------------------------------------------------*/

void trist_data_zero (void)
     /* Clears (zeros) the data records. */
{
  if (st->data)
    BZERO (st->data, char, st->data_size * (st->max_triangle + 1));
}

/*--------------------------------------------------------------------------*/

void trist_modify_vertices (int offset)
     /* This procedure will add offset to all the vertex numbers.
        This is used when two different triangulations are loaded.
        --Mike. */
{
  int t;
  trist_forall (t)
    {
      tr[t].origin[0] += offset;
      tr[t].origin[1] += offset;
      tr[t].origin[2] += offset;
    }
}

/*--------------------------------------------------------------------------*/

/* ========= Routines to compute normal vectors (in long-integer). ======== */

/*--------------------------------------------------------------------------*/

static Lia_ptr a1, a2, a3, b1, b2, b3, nx, ny, nz, s;

/*--------------------------------------------------------------------------*/

void trist_nvx (int a, int b, int c,
                Lia_ptr *nx_ptr, Lia_ptr *ny_ptr, Lia_ptr *nz_ptr,
                Lia_ptr *s_ptr)
     /* Output: nx_ptr, ny_ptr, nz_ptr, s_ptr. */
     /* Computes exact normal vector of triangle with vertex indices a, b, c,
        using the Lia long-integer package. 
        USAGE: >
               > trist_nvx_push ();
               > ...;
               > trist_nvx (...);
               > ...
               > trist_nvx_pop ().
       NOTE:
       - Output parameters *_ptr point to TEMPORARY Lia objects.
       - Triangle is degenerated iff (lia_sign (s_ptr) == 0).
       - Length = sqrt (lia_real (s_ptr)).
       . X = lia_real (nx_ptr) / Length
       . Y = lia_real (ny_ptr) / Length
       . Z = lia_real (nz_ptr) / Length. */
{
  lia_sub (a1, sos_lia (b, 1), sos_lia (a, 1));
  lia_sub (a2, sos_lia (b, 2), sos_lia (a, 2));
  lia_sub (a3, sos_lia (b, 3), sos_lia (a, 3));
  lia_sub (b1, sos_lia (c, 1), sos_lia (a, 1));
  lia_sub (b2, sos_lia (c, 2), sos_lia (a, 2));
  lia_sub (b3, sos_lia (c, 3), sos_lia (a, 3));
  lia_det2 (a2, a3,  b2, b3,  nx);
  lia_det2 (a3, a1,  b3, b1,  ny);
  lia_det2 (a1, a2,  b1, b2,  nz);
  lia_push (nx);
  lia_ipower (2);
  lia_push (ny);
  lia_ipower (2);
  lia_push (nz);
  lia_ipower (2);
  lia_plus ();
  lia_plus ();
  lia_assign (s, lia_popf ()); /* square of the length */
  *nx_ptr = nx;
  *ny_ptr = ny;
  *nz_ptr = nz;
  *s_ptr = s;
}

/*--------------------------------------------------------------------------*/

void trist_nvx_push (void)
     /* Allocate buffer space on the Lia stack. */
     /* NOTE: I'm actually not sure anymore if the stack digits are long
        enough.  Check this, sometimes!! */
{
  a1 = lia_pushf (LIA_NULL);
  a2 = lia_pushf (LIA_NULL);
  a3 = lia_pushf (LIA_NULL);
  b1 = lia_pushf (LIA_NULL);
  b2 = lia_pushf (LIA_NULL);
  b3 = lia_pushf (LIA_NULL);
  nx = lia_pushf (LIA_NULL);
  ny = lia_pushf (LIA_NULL);
  nz = lia_pushf (LIA_NULL);
  s  = lia_pushf (LIA_NULL);
}

/*--------------------------------------------------------------------------*/

void trist_nvx_pop (void)
{
  Assert_always (lia_popf () == s);
  while (lia_popf () != a1)
    ;  /* clear the Lia stack */
}

/*--------------------------------------------------------------------------*/

#ifdef __DEBUG__  /* ========= Below code if for debugging only! ========== */

/*--------------------------------------------------------------------------*/

int trist_io_check_flag = FALSE;
int trist_test_flag = FALSE;

/*--------------------------------------------------------------------------*/

void trist_io_check (Trist *s, Trist_num num)
{
  Trist_record *tri = s->triangle;
  int i, j, k, t;
  int is_wrong = FALSE;
  int u = 0;
  ;
  if (not trist_io_check_flag)
    return;
  ;
  print ("  trist_io_check()\n");
  upfor (t, 1, s->last_triangle)
    if (tri[t].origin[0] != tri[t].origin[1])
      {
        u ++;
        i = (int) (tri[t].origin[0] & org_Mask);
        j = (int) (tri[t].origin[1] & org_Mask);
        k = (int) (tri[t].origin[2] & org_Mask);
        if ((i == j) or (i == k) or (k == j)
            or wrong_org (i, s) or wrong_org (j, s) or wrong_org (k, s))
          is_wrong = TRUE;
        upfor (i, 0, 5)
          {
            j = TrIndex (tri[t].fnext[i]);
            k = TrVersion  (tri[t].fnext[i]);
            is_wrong = ((j > s->last_triangle)
                        or (j < 1) or (k > 5) or (k < 0));
          }
      }
    else
      {
        print ("   > triangle[%d] is deleted.\n", t);
      }
  Assert (not is_wrong);
  Assert (u == s->used_triangles);
  {
    Trist *save = trist_current ();
    trist_set (s);
    Assert (trist_num_eq (trist_num (), num, '*'));
    trist_set (save);
  }
}

/*--------------------------------------------------------------------------*/

static void count_edges (int *edges, int *edges_hull)
     /* Output: all. */
     /* trist_num() refinement */
{
  int i, j, ei, i1, i2;
  int e, eh;
  Basic_byte *aux;     /* used as bit vector to count edges! */
  int *dope;           /* we also need some dope, man! */
  int max_index;
  ;
  dope = new_edge_dope (&max_index);
  aux = MALLOC (Basic_byte, (max_index div8) + 1);
  BZERO (aux,   Basic_byte, (max_index div8) + 1);
  ;
  eh = 0;
  trist_for (j)
    if (trist_hull_triangle (j))
      upfor (i, EdFacet (j, 0), EdFacet (j, 5))  /* 50% useless !?!? */
        {
          ei = edge_index (i, dope);
          i1 = ei div8;
          i2 = ei mod8;
          if (not basic_charbit_on (i2, aux[i1]))
            {
              eh ++;
              aux[i1] = basic_charbit_s1 (i2, aux[i1]);
            }
        }
  ;
  e = eh;
  trist_for (j)
    upfor (i, EdFacet (j, 0), EdFacet (j, 5))  /* 50% useless !?!? */
      {
        ei = edge_index (i, dope);
        i1 = ei div8;
        i2 = ei mod8;
        if (not basic_charbit_on (i2, aux[i1]))
          {
            e ++;
            aux[i1] = basic_charbit_s1 (i2, aux[i1]);
          }
      }
  ;
  FREE (aux);
  kill_edge_dope (dope);
  *edges = e;
  *edges_hull = eh;
}

/*--------------------------------------------------------------------------*/

static int edge_index (int e, const int dope[])
     /* returns an unique index for the edge given by edfacet e;
        dope must have been allocated via new_edge_dope();
        NOTE: there might be overlow if max_org too large! */
{
  int a = Org (e), b = Dest (e), aux;
  if (b < a)
    {
      aux = a;
      a = b;
      b = aux;
    }
  if ((b == a) or (b > trist_max_org ()))
    basic_error ("edge_index: Schu bi du.");
  return (dope[a] + b + 1);
}

/*--------------------------------------------------------------------------*/

static int *new_edge_dope (int *max_index)
     /* Output: *max_index). */
{
  int i, m = st->max_org;
  int *dope;
  dope = MALLOC (int, m);
  dope[0] = -1;
  upfor (i, 1, m - 1)
    dope[i] = dope[i-1] + m - i;
  if (max_index)
    *max_index = dope[m-1] + m + 1;
  return (dope);
}

/*--------------------------------------------------------------------------*/

static void kill_edge_dope (int *dope)
     /* Input/Output: dope. */
{
  FREE (dope);
}

/*--------------------------------------------------------------------------*/

#endif  /* #ifdef __DEBUG__ */
