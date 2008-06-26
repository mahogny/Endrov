%module Qhullj
%{
#include "../include/qhull.h"
%}
void qh_init_B (coordT *points, int numpoints, int dim, unsigned int ismalloc);

//85.228.71.136