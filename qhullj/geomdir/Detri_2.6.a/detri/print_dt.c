/* detri/print_dt.c --- ASCII print of Dt structure (file: *.dt or *.dt_f). */

/*--------------------------------------------------------------------------*/

#include "dt.h"

/*--------------------------------------------------------------------------*/

static char usage[]  = "\
|\n\
|> print [tetrahedra] [triangles] [edges] [vertices] [faces] [file <PATH>] \n\
|\n\
|  ... prints face list of Delaunay triangulation to given file <PATH>.  By \n\
|  default, only tetrahedra are printed.  Default file path is <DATA>.DT.fl.\n\
|  [NOTE: If command is invoked from detri -f, then face list of furthest- \n\
|  point Delaunay triangulation is printed (by default, to <DATA>.DT_f.fl).]\n\
|\n\
|> print [convex] hull [triangles] [edges] [vertices] [faces] [file <PATH>] \n\
|\n\
|  ... prints the face list of the convex hull to given file <PATH>.  By \n\
|  default, only triangles are printed.  Default file path is <DATA>.CH.fl.\n\
|\n";

static void print_all (FILE *out, const Dt *dt, int h_flag, int t_flag,
                       int f_flag, int e_flag, int v_flag);

/*--------------------------------------------------------------------------*/

void dt_print_cmd (const char data_name[], const Dt *dt,
                   int argc, /*const*/ char *argv[])
     /* ASCII print of DT (*.dt or *.dt_f file!) and CH. */
     /* NOTE:  \*const*\ char *argv[]  indicates that they are read-only.
        Cf, note in basic/arg.c, basic_arg_find(). */   
{
  FILE *out;
  char outfile[MAXPATHLEN], *dt_type = NULL, *dt_string = NULL;
  int h = FALSE, t = FALSE, f = FALSE, e = FALSE, v= FALSE, i;
  Assert_always (dt);
  trist_set (dt->trist);
#if __DEBUG__
  { /* Paranoia ==> Double check! */
    Trist_num aux;
    aux = trist_num ();
    Assert_always (    trist_num_eq (dt->num, aux, '*')
                   and (dt->num.v == trist_vertices ()));
  }
#endif
  switch (abs (dt->type))
    {
     case DT:          dt_type = "DT";   dt_string = ""; break;
     case DT_FURTHEST: dt_type = "DT_f"; dt_string = "furthest-point "; break;
     case DT_WEIGHTED: dt_type = "DT_w"; dt_string = "weighted "; break;
     default:  Assert (FALSE);
    }
  print ("| dt_print_cmd \"");
  upfor (i, 0, argc - 1)
    {
      print ("%s", argv[i]);
      if (i != argc - 1)
        print (" ");
    }
  print ("\"\n");
  basic_arg_open (argc, argv);
  outfile[0] = '\0';
  i = 0;
  while (i < argc)
    {
      if (basic_arg_match (i, "print"))
        ; /* ignore */
      else if (basic_arg_match (i, "help"))
        {
          print ("%s", usage);
          return;
        }
      else if (   basic_arg_match (i, "convex")
               or basic_arg_match (i, "hull"))
        h = TRUE;
      else if (basic_arg_match (i, "tetrahedra"))
        t = TRUE;
      else if (basic_arg_match (i, "triangles"))
        f = TRUE;
      else if (basic_arg_match (i, "edges"))
        e = TRUE;
      else if (basic_arg_match (i, "vertices"))
        v = TRUE;
      else if (basic_arg_match (i, "faces"))
        t = f = e = v = TRUE;
      else if (basic_arg_match2string (i, "file", outfile, MAXPATHLEN))
        ;
      else
        {
          print ("| ** error! **\n");
          return;
        }
      i += basic_arg_increment ();
    }
  basic_arg_close ();
  if (h and t)
    t = FALSE;
  else if (h and (not (f or e or v)))
    f = TRUE;
  else if ((not h) and (not (t or f or e or v)))
    t = TRUE;
  if (outfile[0] == '\0')
    /* use default file name */
    sprint (outfile, "%s", fl_default_PATH (data_name, If (h, "CH", dt_type)));
  out = basic_fopen (outfile, "w");
  fprint (out, "# ASCII face list of %s%s.\n",
          If (h, "convex hull", dt_string),
          If (h, "", "Delaunay triangulation"));
  fprint (out, "# Data: \"%s\"\n", data_name);
  if (not h)
    fprint (out, "# %10d tetrahedra %s\n", dt->num.t,
            If (t, "(T)", ""));
  fprint (out, "# %10d triangles  %s\n", If (h, dt->num.fh, dt->num.f),
          If (f, "(f)", ""));
  fprint (out, "# %10d edges      %s\n", If (h, dt->num.eh, dt->num.e),
          If (e, "(e)", ""));
  fprint (out, "# %10d vertices   %s\n", If (h, dt->num.vh, dt->num.v),
          If (v, "(v)", ""));
  print_all (out, dt, h, t, f, e, v);
  basic_fclose (out);
  print ("|   written to \"%s\"\n", outfile);
}

/*--------------------------------------------------------------------------*/

static void print_all (FILE *out, const Dt *dt, int h_flag, int t_flag,
                       int f_flag, int e_flag, int v_flag)
     /* Refinement.  Not very efficient! */
{ /* Uses trist_nvx() (normal vector) to check if triangle is degenerated! */
  Lia_ptr nx, ny, nz, s;
  int i, e, t, a, b, c, d;
  char *v_printed = NULL;
  trist_nvx_push ();
  if (v_flag)
    {
      v_printed = MALLOC (char, dt->trist->max_org + 1);
      BZERO (v_printed,   char, dt->trist->max_org + 1);
    }
  trist_for (t)
    if ((not h_flag) or (trist_hull_triangle (t)))
      {
        if (t_flag)
          {
            e = EdFacet (t, 0);
            upfor (i, 1, 2)
              { /* 2 incident tetrahedra */
                if (    (not trist_hull_facet (e))
                    and (t == TrIndex (trist_tetra_min_ef (e))))
                  {
                    int sign;
                    trist_triangle (e, &a, &b, &c);
                    d = Dest (Enext (Fnext (e)));
                    sign = lia_sign (sos_minor4 (d, a, b, c, 1, 2, 3, 0));
                    fprint (out, "T %d %d %d %d%s\n", d, a, b, c,
                            If ((sign == 0),
                                " # coplanar", If ((sign < 0),
                                                   " # negative!", "")));
                    /* Note: should never be negative! */
                  }
                e = Sym (e);
              }
          }
        if (f_flag)
          {
            e = EdFacet (t, 0);
            if (trist_hull_facet (Sym (e)))
              e = Sym (e);
            trist_triangle (e, &a, &b, &c);
            trist_nvx (a, b, c, &nx, &ny, &nz, &s);
            fprint (out, "f %d %d %d%s\n", a, b, c,
                    If ((lia_sign (s) == 0), " # collinear", ""));
          }
        if (e_flag)
          {
            e = EdFacet (t, 0);
            upfor (i, 1, 3)
              { /* 3 incident edges */
                if (t == TrIndex (trist_edge_min_ef (e)))
                  {
                    fprint (out, "e %d %d\n", Org (e), Dest (e));
                  }
                e = Enext (e);
              }
          }
        if (v_flag)
          {
            e = EdFacet (t, 0);
            upfor (i, 1, 3)
              { /* 3 vertices */
                a = Org (e);
                if (not v_printed[a])
                  {
                    v_printed[a] = TRUE;
                    fprint (out, "v %d\n", a);
                  }
                e = Enext (e);
              }
          }
      }
  trist_nvx_pop ();
  if (v_flag)
    FREE (v_printed);
}
