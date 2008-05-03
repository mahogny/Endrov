This set of MATLAB tools consists of some functions that I have
found useful for basic image processing and blob analysis. The
functions have been tested for MATLAB Version 5.3.0.10183 (R11) on
PCWIN. Many of the files must be run as MEX files (MEX files are
sometimes much faster than M-files). These must be compiled before
use by typing "mex filename.c" at the MATLAB command prompt where
filename.c is the relevant filename. Ensure that you are in the
correct directory (e.g. cd c:\..\KFtools) and that the KFtools
directory is included in the MATLAB path. The last three letters
of all of the functions that are implemented as MEX files are
m-e-x e.g. getboundarymex.

The usage of the functions is described by typing "help function"
at the MATLAB command prompt, where function is the name of the
relative function.

Before you do anything, you must compile the mex files. This can
be done by running the script mexKFtools.m.

The M-file script KFtoolsDemo.m shows an example of the usage of
all of the functions in this toolbox.

When working with binary objects, I have often found it useful to
measure features from the boundary stored as a list of coordinates
as opposed to an image of the object. The boundary of an object in
a binary (black and white) image can be stored as a list of pixel
corner coordinates. The function getboundarymex forms a list of
these corner coordinates from a binary image containing an object.

(c) 2001 Keith Forbes keith@umpire.com
