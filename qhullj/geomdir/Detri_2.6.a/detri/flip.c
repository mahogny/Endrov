/* detri/flip.c --- Flips 3D triangulation to Delaunay triangulation. */

/*--------------------------------------------------------------------------*/

#include "detri.h"

/*--------------------------------------------------------------------------*/

/* Global variables/flags, macros, and switches --- for debugging only! */

#ifdef __DEBUG__
  int delaunay_flip_test_flag = FALSE;
  int delaunay_flip_proto_flag = FALSE;
# define PROTO(PRINT_COMMAND)  if (delaunay_flip_proto_flag) PRINT_COMMAND
#else
# define PROTO(PRINT_COMMAND)  /* do nothing */
#endif

/*--------------------------------------------------------------------------*/

/* Face colors and flip list. */

#define GREEN  'g'  /* flip_list triangle, possibly non-Delaunay */
#define RED    'r'  /* non-transformable triangle */
#define WHITE  ' '  /* non-CH triangle known to be Delaunay
                       (CH triangles have "color '*') */

/* Face colors could be considered a historical relict. I guess, right now,
   it's for little more than sanity checks... */

static int reds, greens;
static Basic_istaque_adt flip_list;

static int next_nd_facet (void);
static void add_facet (int ef);

#define NONE -666

static int new_v;  /* new point whose insertion made Trist non-Delaunay */

/*--------------------------------------------------------------------------*/

/* More locals. */

static int cp_flag = TRUE;  /* cf, delaunay_test(), delaunay_test_set_fp (), */
     
static void (* hook) (char code,
                      int x, int y, int z, int o, int p, 
                      int ref) = NULL;

static int skips, flips_e, flips_f;
static int max_flips;
static int max_flips_overflow_flag;

static void mark_triangle (int t, char color);
static void delete_triangle (int ef);
static int flip_edge (int pox);
static int flip_triangle (int xyz);

static void diagonalize (const int feld[], int x, int y, int z, int o, int p);
static void cut (const int feld[], int x, int y, int z, int o, int p);
static void load (int xyo, int field[], int x, int y, int z, int o, int p);
#ifdef __DEBUG__
 static int diagonalize_test (int xpo, int x, int y, int z, int p, int o);
#endif

/*--------------------------------------------------------------------------*/

void delaunay_flip_open (int n)
     /* Initializes the Delaunay Flip module for current Trist;
        for upto n vertices (if n <= 0, takes n = trist_max_org()). */
{
  if (n <= 0)
    n = trist_max_org ();
  max_flips = n * (n + 3) + 1;  /* UB on total number of flips;
                                   see Herbert's Triangulation notes, p95 */
  if (max_flips <= 0)
    { /* Overflow! */
      max_flips_overflow_flag = TRUE;
      max_flips = MAXINT;
      print ("\n");
      print ("WARNING in delaunay_flip_open():  Max_flips overflow!\n");
      print ("               Will be set to MAXINT for each vertex!\n");
    }
  else
    max_flips_overflow_flag = FALSE;
  ;
  skips = flips_e = flips_f = reds = greens = 0;
  ;
  if (flip_list)
    basic_istaque_dispose (flip_list);
  flip_list = basic_istaque_new (2 * n);  /* w/ initial size, but unbounded! */
}

/*--------------------------------------------------------------------------*/

int delaunay_flip (Basic_istaque_adt nd_list, int v)
     /* Flips non-Delaunay triangles in given Trist until all triangles
        are locally Delaunay (cf, delaunay_test() and delaunay_test_put()).
        ASSUMPTION: Trist is non-Delaunay after inserting point v,
        AND: all possibly non-Delaunay link-facets after are on nd_list.
        Upon success (which should always happen, by now :) delaunay_flip()
        returns the number of necessary flips. As a side effect, nd_list
        will be cleared, and current Trist updated accordingly. */
{
  int f0 = flips_e + flips_f;
  new_v = v;
  while (not basic_istaque_empty (nd_list))
    add_facet (basic_istaque_pop (nd_list));
  Assert_always (flip_list);
  Assert ((reds == 0) and (greens == basic_istaque_length (flip_list)));
  { 
    int ef;
    while ((ef = next_nd_facet ()) != NONE)
      {
        Assert_always (max_flips >= 0);  /* emergency break! */
        if (not flip_edge (ef))
          if (not flip_edge (ef = Enext (ef)))
            if (not flip_edge (ef = Enext (ef)))
              if (not flip_triangle (ef))
                { /* non-transformable triangle */
                  skips ++;
                  mark_triangle (TrIndex (ef), RED);
                }
      }
  }
  if (reds or greens)
    basic_error ("flip.c: algorithm crashed!");
  if (max_flips_overflow_flag)
    max_flips = MAXINT;
  return (flips_e + flips_f - f0);
}

/*--------------------------------------------------------------------------*/

void delaunay_flip_get_info (int *s, int *e, int *f)
     /* Output: all. */
     /* Returns number of skips, e-f flips, and f-e flips. */
{
  *s = skips;
  *e = flips_e;
  *f = flips_f;
}

/*--------------------------------------------------------------------------*/

void delaunay_flip_close (void)
     /* Close down the module. */
{
  if (flip_list)
    {
      basic_istaque_dispose (flip_list);
      flip_list = NULL;
    }
}

/*--------------------------------------------------------------------------*/

void delaunay_flip_hook (void (* routine) (char code,
                                           int x, int y, int z, int o, int p, 
                                           int ref))
     /* The routine (code, x, y, z, o, p, pox) will be called *after*
        each flip in delaunay_flip(); code = DT_FLIP_{EDGE,TRIANGLE}. */
{
  hook = routine;
}

/*--------------------------------------------------------------------------*/

int delaunay_test (int ef)
     /* Tests triangle of ef for local Delaunayhood. Either closest-point
        (which is default) or furthest-point; see delaunay_test_set_fp(). */
{
  int x, y, z, o, p;
  if (trist_hull_triangle (TrIndex (ef)))
    return (TRUE);
  trist_triangle (ef, &x, &y, &z);
  o = Dest (Enext (Fnext (ef)));
  p = Dest (Enext (Fnext (Sym (ef))));
  Assert_if (delaunay_flip_test_flag,
             (    sos_positive3 (o, x, y, z)
              and (not sos_positive3 (p, x, y, z))
              and dt_test_triangle (Fnext (ef), x, y, o)
              and dt_test_triangle (Fnext (Sym (ef)), y, x, p)));
  return (cp_flag == sos_in_sphere_p (o, x, y, z, p));
}

/*--------------------------------------------------------------------------*/

void delaunay_test_set_fp (int flag)
     /* flag = TRUE  ... delaunay_test() is for  closest-point (Default!);
        flag = FALSE ... delaunay_test() is for furthest-point. */
{
  cp_flag = (not flag);
}

/*--------------------------------------------------------------------------*/

static int next_nd_facet (void)
     /* Takes facets from flip_list and returns first one with
        delaunay_test (ef) == FALSE. Returns NONE if none such is found. */
{
  int t, ef;
  while (not basic_istaque_empty (flip_list))
    {
      ef = basic_istaque_pop (flip_list);      /*** flip_list is a stack! ***/
      t = TrIndex (ef);
      if ((not trist_deleted (t)) and (trist_color (t) == GREEN))
        { /* ef is still on flip_list; check NOW if it's still non-Delaunay */
          if (delaunay_test (ef))
            mark_triangle (t, WHITE);
          else
            return (ef);
        }
    }
  return (NONE);
}

/*--------------------------------------------------------------------------*/

static void add_facet (int ef)
     /* Adds ef to flip_list, but only if it is
        (a) not on flip_list already (ie, not GREEN),
        (b) not on CH, and
        (c) a link facet. */
{
  int a, b, c, t = TrIndex (ef);
  if ((trist_color (t) != GREEN) and (not trist_hull_triangle (t)))
    {
      trist_triangle (ef, &a, &b, &c);
      if ((new_v != a) and (new_v != b) and (new_v != c))
        {
          basic_istaque_push (flip_list, ef);   /*** flip_list is a stack! ***/
          mark_triangle (TrIndex (ef), GREEN);
        }
    }
}

/*--------------------------------------------------------------------------*/

static void mark_triangle (int t, char color)
     /* Marks triangle t with given color. */
{
  switch (color)
    {
     case WHITE:
      {
        switch (trist_color (t))
          {
           case RED:
            reds --;
            trist_color_set (t, WHITE);
            break;
           case GREEN:
            greens --;
            trist_color_set (t, WHITE);
            break;
           default:
            ; /* don't do anything if it wasn't RED or GREEN */
          }
        break;
      }
     case GREEN:
      {
        Assert (trist_color (t) != GREEN);
        if (trist_color (t) == RED)
          reds --;
        trist_color_set (t, GREEN);
        greens ++;
        break;
      }
     case RED:
      {
        if (trist_color (t) == GREEN)
          greens --;
        trist_color_set (t, RED);
        reds ++;
        break;
      }
     default:
      Assert_always (FALSE);
    }
}

/*--------------------------------------------------------------------------*/

static void delete_triangle (int ef)
     /* Refinement: Deletes triangle of ef (after clearing its color). */
{
  mark_triangle (TrIndex (ef), WHITE); 
  trist_delete (ef);
}

/*--------------------------------------------------------------------------*/

static int flip_edge (int pox)
     /* Flips edge pox to triangle xyz, in case it's transformable;
        returns FALSE otherwise. Assumes: delaunay_test (pox) == FALSE. */
     /* NOTE: Transformability of edge can be tested WITHOUT geometric tests.
              We just have to make sure, it's not a CH edge. */
{
  int x, y, z, o, p, poy, poz, xyo, j, feld[6];
  int is_transformable;
  Assert (    (trist_color (TrIndex (pox)) == GREEN)
          and (not delaunay_test (pox)));
  poy = Fnext (pox);
  poz = Fnext (poy);
  if (Fnext (poz) != pox) 
    is_transformable = FALSE;  /* no degree-3 edge */
  else
    is_transformable = (    (not (trist_hull_triangle (TrIndex (poy))))
                        and (not (trist_hull_triangle (TrIndex (poz)))));
  if (is_transformable)
    {
      /* Edge-to-triangle flip:
         assumig sos_positive3 (o, x, y, z) and sos_positive3 (p, y, x, z). */
      flips_e ++;
      max_flips --;
      trist_triangle (pox, &p,  &o,  &x);
      y = Dest (Enext (poy));
      z = Dest (Enext (poz));
      Assert ((y == new_v) or (z == new_v));
      Assert_if (delaunay_flip_test_flag,
                 (    sos_positive3 (o, x, y, z)
                  and sos_positive3 (p, y, x, z)
                  and (not sos_positive3 (p, y, z, o))
                  and (not sos_positive3 (p, z, x, o))
                  and (not sos_positive3 (p, x, y, o))));
      PROTO (print ("FLIP edge to tri [%d|%d|%d] :: %d,%d,%d;%d,%d\n",
                    pox, poy, poz, x, y, z, o, p));
      xyo = Enext (Fnext (Enext (pox)));  /* must save before deleting! */
      delete_triangle (pox);
      delete_triangle (poy);
      delete_triangle (poz);
      load (xyo, feld, x, y, z, o, p);
      cut (feld, x, y, z, o, p);
      upfor (j, 0, 5)
        add_facet (feld[j]);
      if (hook)
        {
          int xyz = Sym (Fnext (Sym (xyo)));
          Assert (dt_test_triangle (xyz, x, y, z));
          hook (DT_FLIP_EDGE, x, y, z, o, p, xyz);                
        }
      return (TRUE);
    }
  else
    return (FALSE);
}

/*--------------------------------------------------------------------------*/

static int flip_triangle (int xyz)
     /* Flips triangle xyz to edge pox, in case it's transformable.
        Assumption: delaunay_test (xyz) == FALSE. */
{
  int x, y, z, o, p, xyo = Fnext (xyz), j, feld[6];
  int is_concave;
  trist_triangle (xyz, &x, &y, &z);
  o = Dest (Enext (xyo));
  p = Dest (Enext (Fnext (Sym (xyz))));
  is_concave = (              sos_positive3 (p, y, z, o)
                or /* else */ sos_positive3 (p, z, x, o)
                or /* else */ sos_positive3 (p, x, y, o));
  Assert  ((o == new_v) or (p == new_v));
  Assert_if (delaunay_flip_test_flag,
             (    sos_positive3 (o, x, y, z)
              and (not sos_positive3 (p, x, y, z))
              and dt_test_triangle (xyo, x, y, o)
              and dt_test_triangle (Fnext (Sym (xyz)), y, x, p)));
  PROTO (print ("%s [%d] t%d = %d,%d,%d;%d,%d\n",
                If (is_concave, "SKIP tetra", "FLIP tri to edge"),
                xyz, TrIndex (xyz), x, y, z, p, o));
  if (is_concave)
    return (FALSE);
  else
    { /* Triangle-to-edge flip:
         assumig sos_positive3 (o, x, y, z) and sos_positive3 (p, y, x, z). */
      flips_f ++;
      max_flips --;
      delete_triangle (xyz);
      load (xyo, feld, x, y, z, o, p);
      diagonalize (feld, x, y, z, o, p);
      upfor (j, 0, 5)
        add_facet (feld[j]);
      if (hook)
        {
          int pox = Sym (Enext (Fnext (Enext (Sym (xyo)))));
          Assert (dt_test_triangle (pox, p, o, x));
          hook (DT_FLIP_TRIANGLE, x, y, z, o, p, pox);            
        }
      return (TRUE);
    }
}

/*--------------------------------------------------------------------------*/

static void diagonalize (const int feld[], int x, int y, int z, int o, int p)
     /* Note: feld[0..5]. */
     /* Refinement: assumes that we have a double tetrahedron, and
        creates 3 new triangles: i.e, 3*3 edfacets */
{
  int xpo, pyo, pzo, oxz, zpx, xpz, zox, yoz, pyz;
  xpo = trist_make (x, p, o);
  pyo = trist_make (p, y, o);
  pzo = trist_make (p, z, o);
  oxz = Sym (Enext (feld[2]));
  zpx = Enext (feld[4]);
  xpz = Sym (Enext (zpx));
  zox = Enext2 (oxz);
  yoz = Enext (Sym (feld[1]));
  pyz = Sym (Enext (feld[5]));
  Fsplice (xpz, xpo);
  Fsplice (oxz, Enext2 (xpo));
  Fsplice (pyz, pyo);
  Fsplice (yoz, Enext (pyo));
  Fsplice (Sym (zpx), pzo);
  Fsplice (zox, Enext (pzo));
  Fsplice (Enext (xpo), Enext (Sym (pyo)));
  Fsplice (Enext (Sym (pyo)), Enext (Sym (pzo)));
  Assert_if (delaunay_flip_test_flag, diagonalize_test (xpo, x, y, z, p, o));
}

#if __DEBUG__

static int diagonalize_test (int xpo, int x, int y, int z, int p, int o)
{
  int one, two, three;
  one   = Enext (xpo);
  two   = Fnext (one);
  three = Fnext (two);
  Assert (    (one == Fnext (three))
          and dt_test_triangle (one,   p, o, x)
          and dt_test_triangle (two,   p, o, y)
          and dt_test_triangle (three, p, o, z)
          and dt_test_open_tetra (one,   p, o, z, x)
          and dt_test_open_tetra (two,   p, o, x, y)
          and dt_test_open_tetra (three, p, o, y, z));
  return (TRUE);
}

#endif

/*--------------------------------------------------------------------------*/

static void cut (const int feld[], int x, int y, int z, int o, int p)
     /* Note: feld[0..5]. */
     /* Refinement: assumes that we have a double tetrahedron, and
         creates 1 new triangle: i.e, 1*3 edfacets + syms. */
{
  int xyz;
  Touch_args (o && p);
  xyz = trist_make (x, y, z);
  Fsplice (Sym (Fnext (Sym (feld[0]))), xyz);
  Fsplice (Sym (Fnext (Sym (feld[1]))), Enext (xyz));
  Fsplice (Sym (Fnext (Sym (feld[2]))), Enext (Enext (xyz)));
  Assert_if (delaunay_flip_test_flag,
             (    dt_test_open_tetra (Fnext (xyz), x, y, z, o)
              and dt_test_open_tetra (Fnext (Sym (xyz)), y, x, z, p)
              and dt_test_triangle (Fnext (Fnext (Sym (feld[0]))), y, x, p)));
}

/*--------------------------------------------------------------------------*/

static void load (int xyo, int field[], int x, int y, int z, int o, int p)
     /* Output: field[0..5] */
     /* Refinement returning: field[0] = xyo, field[1] = yzo, field[2] = zxo
                              field[3] = yxp, field[4] = xzp, field[5] = zyp */
{
  int xyp, h;
  Touch_args (x && y && z && o && p);
  field[0] = xyo;
  field[1] = h = Turn (xyo);
  field[2] = Turn (h);
  field[3] = xyp = Fnext (Sym (xyo));
  field[4] = h = Turn (xyp);
  field[5] = Turn (h);
  Assert_if
    (delaunay_flip_test_flag,
     (    (Turn (field[2]) == xyo)
      and (Turn (field[5]) == xyp)
      and dt_test_open_tetra (xyo, x, y, z, o)
      and dt_test_open_tetra (xyp, y, x, z, p)
      and
      (Enext (xyp)
       ==
       Fnext (Sym (Enext (Turn (Fnext (Sym (Turn (Turn (Enext (xyp))))))))))));
}
