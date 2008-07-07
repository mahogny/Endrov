/* detri/delaunay.c --- 3D Delaunay triangulations using flip. */

/*---------------------------------------------------------------------------*/

const char delaunay__version[] = "@(#) Delaunay/Flip Module 1.6";
const char delaunay__authors[] = "Ernst Mucke";
#ifdef __DEBUG__
const char delaunay__compile[] = "@(#) \t w/ -D__DEBUG__";
#endif

/*--------------------------------------------------------------------------*/

#include "detri.h"

/*--------------------------------------------------------------------------*/

/* Two useful Trist extensions. */

#define next_CH(E)   Sym (Enext2 (Fnext (E)))
/* Gives next CH EdFacet with same Org as E (which is assumed to be on CH). */

#define Fmerge(D,E)  Fsplice (D, Sym (Fnext (Sym (E))))
/* Merges 2 (nonoverlapping) facet rings st Fnext(d)==e; assumes Fnext(d)!=e.*/

/*--------------------------------------------------------------------------*/

/* Sample size for dt_search(). */

float delaunay_search_kf = 15.0;

#define SEARCH_K(N)  (int) (delaunay_search_kf * log10 ((double) N))

/*--------------------------------------------------------------------------*/

/* Parameters, flags, and tracing stuff  --- for debugging only! */

#define TRACE_BLOCK 20
#define PEDANTIC_TEST 0

#ifdef __DEBUG__
  int delaunay_test_flag = FALSE;
  int delaunay_proto_flag = FALSE;
  int delaunay_trace_mode = 2;
# define PROTO(PRINT_COMMAND)  if (delaunay_proto_flag) PRINT_COMMAND
#else
  int delaunay_trace_mode = 1;
# define PROTO(PRINT_COMMAND)  /* do nothing */
#endif

/* delaunay_trace_mode: 0 ... silent
                        1 ... minimal tracing
                        2 ... normal tracing
                        (assumed when any flag set!) */

/*--------------------------------------------------------------------------*/

/* Local stuff. */

static Delaunay_info counter;

static void initial (int a, int b, int c, int o, int *h);
static void insert (int v, Basic_istaque_adt the_list, int *h, int hull_flag);
static void restore_hull_bits (int v, int hf);
static int is_hull_facet (int h);

static int first_visible (int index, int v, int last_v);
static void collect_visibles (int v, int hx, Basic_istaque_adt v_list);
static void collect_visibles_dfs (int v, Basic_istaque_adt v_list, int h);
static int get_hull (void);

#define INITIAL 'i'
#define NORMAL  'n'

static void mount_t (char code, int p, int a);
static void mount_1 (void);
static void mount_2 (int jki, int kij, int ivj, int vji);
static void mount_3a (int ijv, int vkj, int vji);
static void mount_3b (int ijv, int ivj, int vki);
static void mount_4 (int vkj, int vki, int ivj, int vji);

static void trace_start (int a, int b, int c, int vau);
static void trace_before (int index, int v);
static void trace_after (int index, int flips);
static void trace_finish (int n);
static void minimal_trace (int n, int f);
static void print_flips (int n, const int flips[]);

/*--------------------------------------------------------------------------*/

void delaunay (const int vertex[], int n, int *h, int randomized)
     /* Output: *h. */
     /* | randomized == 0 ... vertex[1..n] is sorted along fixed direction;
        |             > 0 ... vertex[1..n] is in unspecified order */
     /* ... builds Delaunay triangulation into current Trist by incrementally
        adding vertex[index] to triangulation T[index-1], for index = 5..n.
        Obviously, this has a tremendous side effect on the current Trist.
        Furthermore, it outputs h, an edfact on the CH. */
{
  int index, flips, ef, aux;
  Basic_istaque_adt vis_list = basic_istaque_new (2 * n);
  (void) basic_istaque_resize (vis_list, 0.0);  /* bounded by 2n-4 (Euler) */
  delaunay_flip_open (n);
  BZERO (&counter, Delaunay_info, 1);
  counter.search_tests = dt_search_get_tests ();
  trace_start (vertex[1], vertex[2], vertex[3], vertex[4]);
  initial     (vertex[1], vertex[2], vertex[3], vertex[4], h);
  upfor (index, 5, n)
    {
      trace_before (index, vertex[index]);
      Assert (basic_istaque_empty (vis_list));
      Assert_if (PEDANTIC_TEST, dt_test ());
      if (randomized)
        {
          ef = dt_search (vertex[index], SEARCH_K (index));
          if (trist_hull_facet (ef))
            {
              counter.search_outsiders ++;
              collect_visibles (vertex[index], ef, vis_list);
              insert (vertex[index], vis_list, h, TRUE);
            }
          else
            {
              basic_istaque_push (vis_list, ef);
              basic_istaque_push (vis_list, Sym (aux = Fnext (ef)));
              basic_istaque_push (vis_list, Sym (aux = Turn (aux)));
              basic_istaque_push (vis_list, Sym (aux = Turn (aux)));
              Assert (Turn (aux) == Fnext (ef));
              insert (vertex[index], vis_list, h, FALSE);
            }
        }
      else
        { 
          ef = first_visible (index, vertex[index], vertex[index-1]);
          collect_visibles (vertex[index], ef, vis_list);
          insert (vertex[index], vis_list, h, TRUE);
        }
      flips = delaunay_flip (vis_list, vertex[index]);
      trace_after (index, flips);
    }
  trace_finish (n);
  delaunay_flip_close ();
  basic_istaque_dispose (vis_list);
}

/*--------------------------------------------------------------------------*/

Delaunay_info* delaunay_info (void)
     /* ... returns pointer to info structure of delaunay.c. */
     /* NOTE: The returned address, which points to the info structure,
        is a constant.  DO NOT FREE() IT and consider the fields
        of the structure as read-only. */
{
  static Delaunay_info di;
  di = counter;
  delaunay_flip_get_info (&(di.skips), &(di.flips_e), &(di.flips_f));
  di.search_tests = dt_search_get_tests () - counter.search_tests;
  return (&di);
}

/*--------------------------------------------------------------------------*/

static void initial (int a, int b, int c, int o, int *h)
     /* Output: *h. */
     /* ... creates initial triangulation by building tetrahedron (a, b, c, o);
        outputs *h, a face on CH */
{
  int aux = trist_make (a, b, c);
  if (not sos_positive3 (o, a, b, c))
    aux = Sym (aux);
  trist_hull_facet_set (Sym (aux), TRUE);
  mount_t (INITIAL, o, aux);
  restore_hull_bits (o, *h = Enext2 (Fnext (aux)));
}

/*--------------------------------------------------------------------------*/

static void insert (int v, Basic_istaque_adt the_list, int *h, int hull_flag)
     /* Output: *h. */
     /* ... inserts v into the triangulation by mounting v on each triangle
        in the_list; if the hull_flag is set, it is assumed that v was
        outside the (old) CH, and a new CH face, *h, is set. */
     /* > NOTE: Here, it is also assumed that the_list is in DFS order
        >       (see collect_visibles ()).  Since we use get() and push()
        >       to scan through it, this gives us the same order below! */
{
  int aux, m = basic_istaque_length (the_list);
#if __DEBUG__
  int m1 = counter.mount_1s,  m2 = counter.mount_2s, m3 = counter.mount_3as;
#endif
  while (m)
    { 
      m --;
      aux = basic_istaque_get (the_list);
      basic_istaque_push (the_list, aux);
      mount_t (NORMAL, v, aux);
    }
  if (hull_flag)
    restore_hull_bits (v, *h = get_hull ());
#if __DEBUG__
  else
    /* inserting inside tetrahedron <==> mount_1(), mount_2(), mount_3a() */
    Assert_always (    (m1 + 1 == counter.mount_1s)
                   and (m2 + 1 == counter.mount_2s)
                   and (m3 + 1 == counter.mount_3as));
#endif
}

/*--------------------------------------------------------------------------*/

static void restore_hull_bits (int v, int hf)
     /* ... restores hull bits in Trist after mounting of v,
        assuming: hf is on CH *AND* Org (hf) == v. */
{ 
  int counter = 0, h = hf;
  Assert_always (Org (hf) == v);
  Assert (is_hull_facet (hf));
  do
    {
      trist_hull_facet_set (h, TRUE);
      h = next_CH (h);
      counter ++;
      Assert_always (counter <= trist_last ());  /* Emergency break! */
    } until (h == hf);
}

/*--------------------------------------------------------------------------*/

static int is_hull_facet (int h)
     /* ... checks locally (!) whether edfacet (!) h lies on the CH of
        the triangulation built so far; uses geometric primitives for its
        decision! */
     /* NOTE: I think we could also do this by checking the existence of
        incident tetrahedra; note, however, that this isn't called very
        often. */
{
  int x, y, z, u, v, w;
  trist_triangle (h, &x, &y, &z);
  u = Dest (Enext (Fnext (h)));
  v = Dest (Enext (Fnext (h = Enext (h))));
  w = Dest (Enext (Fnext (Enext (h))));
  if (sos_positive3 (u, x, y, z) or
      sos_positive3 (v, x, y, z) or
      sos_positive3 (w, x, y, z))
    return (FALSE);
  else
    return (TRUE);
}

/*--------------------------------------------------------------------------*/

static int rx = 0;  /* ... globally accessed by *visible*(), get_hull(). */

static int first_visible (int index, int v, int last_v)
     /* GLOBAL: rx. */
     /* ... returns some facet visible from v == vertex[index].  The grand
        assumption here is that last_v == vertex[index-1] and visible from v;
        ie, delaunay() inserts vertices sorted along some fixed direction. */
{
  int a, b, c, aux;
  int hx = 0, found = FALSE;
  if (index > 5)
    { /* reference facet rx is valid and we can get a regular hx */
      hx = Sym (Enext (Fnext (rx)));
      Assert (is_hull_facet (hx) and (Org (hx) == last_v));
      /* NOTE: If the the v's are coming sorted along one direction, it's true
         that Org (hx) is now visible from v.  Some adjacent face hx', with
         Org (hx') == Org (hx), must be totally visible from v. */
      aux = hx;
      do
        {
          trist_triangle (aux, &a, &b, &c);
          found = sos_positive3 (v, a, b, c);
          if (not found)
            aux = next_CH (aux);
        } until (found or (aux == hx));
      hx = aux;
      Assert_always (found and (Org (hx) == last_v));
    }
  else
    { /* (index == 5) and rx not valid ==> called for the FIRST time.
         Just go through all facets in Trist (it's only a tetrahedron)
         and figure out some hx. */
      trist_for (aux)
        {
          if (is_hull_facet (EdFacet (aux, 0)))
            hx = EdFacet (aux, 0);
          else
            hx = EdFacet (aux, 1);
          Assert (is_hull_facet (hx));
          trist_triangle (hx, &a, &b, &c);
          if (sos_positive3 (v, a, b, c))
            {
              found = TRUE;
              break;
            }
        }
      Assert_always (found);
    }
  return (hx);
}

static void collect_visibles (int v, int hx, Basic_istaque_adt v_list)
     /* Output: v_list. */
     /* GLOBAL: rx. */
     /* ... collects all CH facets visible from v and stores then in v_list.
        Assumption: hx is on CH and visible from v. */
{
#ifdef __DEBUG__
  int a, b, c;
  trist_triangle (hx, &a, &b, &c);
  Assert (is_hull_facet (hx) and sos_positive3 (v, a, b, c));
#endif
  Assert_always (basic_istaque_empty (v_list));
  trist_hull_facet_set (hx, FALSE);
  basic_istaque_push (v_list, hx);
  collect_visibles_dfs (v, v_list, Sym (Fnext (hx)));
  collect_visibles_dfs (v, v_list, Sym (Fnext (hx = Enext (hx))));
  collect_visibles_dfs (v, v_list, Sym (Fnext (hx = Enext (hx))));
}

static void collect_visibles_dfs (int v, Basic_istaque_adt v_list, int h)
     /* Output: v_list. */
     /* GLOBAL: rx. */
     /* Depth-first serach. */
{
  if (trist_hull_facet (h))
    {
      int i, j, k;
      trist_triangle (h, &i, &j, &k);
      if (sos_positive3 (v, i, j, k))
        {
          trist_hull_facet_set (h, FALSE);
          basic_istaque_push (v_list, h);
          collect_visibles_dfs (v, v_list, Sym (Fnext (h = Enext (h))));
          collect_visibles_dfs (v, v_list, Sym (Fnext (h = Enext (h))));
        }
      else
        rx = h;  /* set the reference facet! */
    }  
}

static int get_hull (void)
     /* GLOBAL: hx. */
     /* ... returns CH facet h with Org (h) == v, the point just inserted.
        This works ONLY after collect_visibles() and mount_t(). */
{
  Assert (rx);
  return (Sym (Enext (Fnext (rx))));
}

/*--------------------------------------------------------------------------*/

static int v, ijk, i, j, k; /* ... globally accessed by mount_*() routines! */

/*--------------------------------------------------------------------------*/

static void mount_t (char code, int p, int a)
     /* Mounts tetrahedron connecting new point v = p with ijk = a, some
        visible face on CH.  Distinguishes a couple of cases; cf mount_* (). */
     /* GLOBAL: hull, v, i, j, k. */
{
  int jki, kij, ijv, ivj, vji, vkj, vki, jkv, kiv;  
  int b1, b2, vau, cov;
  /* NOTE: a was on hull but hull bit might already have been deleted */
  ijk = a;
  v = p;
  trist_triangle (ijk, &i, &j, &k);
  Assert (sos_positive3 (v, i, j, k));
  if (code == INITIAL)
    {
      mount_1 ();
      return;
    }
  jki = Enext (ijk);
  kij = Enext (jki);
  ijv = Fnext (ijk);
  jkv = Fnext (jki);
  kiv = Fnext (kij);
  vji = Sym (Enext (ijv));
  vau = (Org (vji) == v);
  cov = ((Turn (ijv) == jkv) and (Turn (jkv) == kiv) and (Turn (kiv) == ijv));
  if (not (vau and cov))
    {  /* act iff not already mounted w/ tetrahedron w/ top = v */
      vkj = Sym (Enext (jkv));
      vki = Enext2 (kiv);
      b1 = (Org (vkj) == v);
      b2 = (Org (vki) == v);
      ivj = Enext (Sym (ijv));
      PROTO (print ("mount_t [%d]=t%d = %d,%d,%d visible from %d (%d,%d;%d)\n",
                    ijk, TrIndex (ijk), i, j, k, v,
                    Org (vkj), Org (vki), vau));
      Assert_if (delaunay_test_flag and vau,
                 (    dt_test_triangle (ijk, i, j, k)
                  and dt_test_triangle (vji, v, j, i)
                  and dt_test_triangle (ijv, i, j, v)
                  and dt_test_triangle (ivj, i, v, j)));
      Assert (vau or (not (b1 or b2)));
      /* > NOTE: This assumption is essential because of the "if" below.
         >       It is true since facets are processed in DFS order! */
      if (not vau)
        mount_1 ();
      else if (b1)
        {
          if (b2)
            mount_4 (vkj, vki, ivj, vji);
          else 
            mount_3a (ijv, vkj, vji);
        }
      else if (b2)
        mount_3b (ijv, ivj, vki);
      else
        mount_2 (jki, kij, ivj, vji);
      Assert (    (Org (Enext (Fnext (jki))) == k)
              and (Org (Enext (Sym (Fnext (kij)))) == k)
              and (Org (Sym (Enext (Fnext (jki)))) == v)
              and (Org (Enext2 (Fnext (kij))) == v));
    }
}

/*--------------------------------------------------------------------------*/

static void mount_1 (void)
     /* GLOBAL: hull, v, ijk, i, j, k. */
     /* Mounts v on "top" of ijk; creates 3 * 3 new edfacets;
        assumes: edge-ring of ijk is seen making a CCW turn from vertex v. */
{ 
  int ijv, jkv, kiv, jki = Enext (ijk), kij = Enext (jki); 
  counter.mount_1s ++;
  ijv = trist_make (i, j, v);
  jkv = trist_make (j, k, v);
  kiv = trist_make (k, i, v);
  Fsplice (ijk, ijv);
  Fsplice (jki, jkv);
  Fsplice (kij, kiv);
  Fsplice (Enext (ijv), Enext (Sym (jkv)));
  Fsplice (Enext (jkv), Enext (Sym (kiv)));
  Fsplice (Enext (kiv), Enext (Sym (ijv)));
  PROTO (print ("mount_1 done: ijk=[%d] tetra=(%d,%d,%d,%d)\n",
                ijk, i, j, k, v));
  Assert_if (delaunay_test_flag,
             (    sos_positive3 (v, i, j, k)
              and dt_test_open_tetra (ijv, i, j, k, v)
              and dt_test_open_tetra (Enext (Sym (ijk)), i, k, v, j)));
}

/*--------------------------------------------------------------------------*/

static void mount_2 (int jki, int kij, int ivj, int vji)
     /* GLOBAL: hull, v, ijk, i, j, k. */
     /* Mounts 2 triangular facets given by edge-rings represented by GLOBAL
        ijk and Sym (Fnext (ijk)); assumes: sos_positive3 (v, i, j, k);
        and: mounted facets are on hull; and: Sym (Fnext (ijk)) == jiv. */
{
  int jkv, kiv, ivk, kvj;
  counter.mount_2s ++;
  jkv = trist_make (j, k, v);
  kiv = trist_make (k, i, v);
  ivk = Enext (kiv);
  kvj = Enext (jkv);
  Fsplice (jki, jkv);
  Fsplice (kij, kiv);
  Fsplice (ivj, ivk);
  Fsplice (kvj, Sym (Enext (ivk)));
  Fsplice (vji, Enext (kvj));
  PROTO (print ("mount_2 done: ijk=[%d] edge=(%d,%d) tetra=(%d,%d,%d,%d)\n",
                ijk, v, k, i, j, k, v));
  Assert_if (delaunay_test_flag,
             dt_test_open_tetra (kiv, k, i, j, v));
}

/*--------------------------------------------------------------------------*/

static void mount_3a (int ijv, int vkj, int vji)
     /* GLOBAL: hull, v, ijk, i, j, k. */
     /* Nearly same prereqisites as mount_2()... */
{
  int vki;
  counter.mount_3as ++;
  vki = trist_make (v, k, i);
  Fsplice (vkj, vki);
  Fsplice (Enext2 (ijk), Enext (vki));
  Fsplice (Enext (Sym (ijv)), Enext2 (vki));
  if (Fnext (vji) != Enext (Sym (vkj)))
    Fmerge (vji, Enext (Sym (vkj)));
  PROTO (print ("mount_3a done: ijk=[%d] vkj=[%d] tetra=(%d,%d,%d,%d)\n",
                ijk, vkj, i, j, k, v));
  Assert_if (delaunay_test_flag,
             dt_test_open_tetra (ijv, i, j, k, v));
}

/*--------------------------------------------------------------------------*/

static void mount_3b (int ijv, int ivj, int vki)
     /* GLOBAL: hull, v, ijk, i, j, k. */
     /* Nearly same prereqisites as mount_2()... */
{
  int vkj;
  counter.mount_3bs ++;
  vkj = trist_make (v, k, j);
  Fsplice (Sym (vki), Sym (vkj));
  Fsplice (Enext (ijk), Sym (Enext (vkj)));
  Fsplice (Sym (Enext (ijv)), Enext (Sym (vkj)));
  if (Fnext (ivj) != Enext2 (vki))
    Fmerge (ivj, Enext2 (vki));
  PROTO (print ("mount_3b done: ijk=[%d] vki=[%d] tetra=(%d,%d,%d,%d)\n",
                ijk, vki, i, j, k, v));
  Assert_if (delaunay_test_flag,
             dt_test_open_tetra (ijv, i, j, k, v));
}

/*--------------------------------------------------------------------------*/

static void mount_4 (int vkj, int vki, int ivj, int vji)
     /* GLOBAL: hull, v, ijk, i, j, k. */
     /* No comment. */
{
  int a = (Fnext (vkj) != vki);
  int b = (Fnext (vji) != Enext (Sym (vkj)));
  int c = (Fnext (ivj) != Enext2 (vki));
  counter.mount_4s ++;
  if (a)
    Fmerge (vkj, vki);
  if (b)
    Fmerge (vji, Enext (Sym (vkj)));
  if (c)
    Fmerge (ivj, Enext2 (vki));
  PROTO (print ("mount_4 done: vkj=[%d] vki=[%d] tetra=(%d,%d,%d,%d)\n",
                vkj, vki, i, j, k, v));
  Assert_if (delaunay_test_flag,
             (    (a or b or c)
              and dt_test_triangle (vki, v, k, i)
              and dt_test_triangle (vkj, v, k, j)
              and dt_test_open_tetra (Sym (ijk), j, i, v, k)));
}

/*--------------------------------------------------------------------------*/

static void trace_start (int a, int b, int c, int vau)
{
  int index;
#ifdef __DEBUG__
  if (    (delaunay_proto_flag or delaunay_flip_proto_flag)
      and (delaunay_trace_mode < 2))
    { 
      delaunay_trace_mode = 2;
      print ("delaunay: delaunay_trace_mode is automatically set to 2\n");
    }
#endif
  if (delaunay_trace_mode > 1)
    print ("PROCESSING VERTEX %d AND %d AND %d PLUS %d.\n", a, b, c, vau);
  if (delaunay_trace_mode == 1)
    upfor (index, 1, 4)
      minimal_trace (index, 0);    
}

static void trace_before (int index, int v)
{
  switch (delaunay_trace_mode)
    {
     case 0:
     case 1:
      break;
     default:
      print ("ADDING VERTEX[%d] %d\n", index, v);
    }
}

static void trace_after (int index, int flips)
{
  if (delaunay_trace_mode == 1)
    minimal_trace (index, flips);
}

static void trace_finish (int n)
{
  if (delaunay_trace_mode == 1)
    minimal_trace (n, -1);  /* flush */
}  

static void  minimal_trace (int n, int f)
{
  static int flips[TRACE_BLOCK];
  int m = n mod TRACE_BLOCK;
  int j;
  if (f < 0)
    { /* flush */
      print_flips (n, flips);
      return;
    }
  if (n == 1)
    upfor (j, 0, TRACE_BLOCK - 1)
      flips[j] = -1;  /* clear */
  flips [If (m, m - 1, TRACE_BLOCK - 1)] = f;
  if (m == 0)
    {
      print_flips (n, flips);
      upfor (j, 0, TRACE_BLOCK - 1)
        flips[j] = -1;  /* clear */
    }
}

static void print_flips (int n, const int flips[])
{
  static int sum = 0, all = 0;
  int j;
  if (flips[0] < 0)
    return; /* there is nothing to print */
  print ("v[%d]", ((n - 1) / TRACE_BLOCK) * TRACE_BLOCK + 1);
  upfor (j, 0, TRACE_BLOCK - 1)
    if (flips[j] >= 0)
      {
        print (" %d", flips[j]);
        sum += flips[j];
        all ++;
      }
  print (" (%.1f)\n", ((double) sum) / ((double) all));
}
