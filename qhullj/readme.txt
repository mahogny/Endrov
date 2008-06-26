http://www.qhull.org/src/qh-qhull.htm


global.c:
void qh_init_A (FILE *infile, FILE *outfile, FILE *errfile, int argc, char *argv[]) {
  qh_meminit (errfile);
  qh_initqhull_start (infile, outfile, errfile);
  qh_init_qhull_command (argc, argv);
} /* init_A */

http://www.qhull.org/src/global.c#init_B
void qh_init_B (coordT *points, int numpoints, int dim, boolT ismalloc) 





qh_init_A (stdin, stdout, stderr, argc, argv);  /* sets qh qhull_command */
  exitcode= setjmp (qh errexit); /* simple statement for CRAY J916 */
  if (!exitcode) {
    qh_option ("voronoi  _bbound-last  _coplanar-keep", NULL, NULL);
    qh DELAUNAY= True;     /* 'v'   */
    qh VORONOI= True; 
    qh SCALElast= True;    /* 'Qbb' */
    qh_checkflags (qh qhull_command, hidden_options);
    qh_initflags (qh qhull_command);
    points= qh_readpoints (&numpoints, &dim, &ismalloc);
    if (dim >= 5) {
      qh_option ("_merge-exact", NULL, NULL);
      qh MERGEexact= True; /* 'Qx' always */
    }
    qh_init_B (points, numpoints, dim, ismalloc);
    qh_qhull();
    qh_check_output();
    qh_produce_output();
    if (qh VERIFYoutput && !qh FORCEoutput && !qh STOPpoint && !qh STOPcone)
      qh_check_points();
    exitcode= qh_ERRnone;
  }
  qh NOerrexit= True;  /* no more setjmp */
