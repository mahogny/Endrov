README.make --- Remarks on code installation with a generic Makefile.

The generic Makefile runs Makefile.cpp (and indirectly Makefile.sys)
through the C preprocessor to create a Makefile.tmp.  It is this file
that is then implicitly executed with second-level make command to do
the actual compilation.  This approach leads to "somewhat" portable
Makefiles by taking advantage of the "#ifdef <machine>" commands of
the C pre-processor.


HOW TO INSTALL?

Modify Makefile.cpp to determine the desired target directories, eg,
LIB, INCLUDE, and DEST (but, frankly, it is best to use the defaults).
If neccessary, edit Makefile.sys to port the code to your system.
Then run:

        make -k new

or
        make -k clear normal

To compile a dbx'able version run:

        make -k clear debug

To use a compiler different to cc run, for example,

        make -k CC=gcc new


PROTABILITY

The sources should now be pretty protable in the following sense,
because they compile 'gcc -ansi -pedantic -Wall' clean (on SGIs with
properly installed gcc).  The code is known to compile AS IT IS (and
with no warnings!) on the following systems:

1. SGI (Irix 4.x, 5.x)
   #if defined (sgi) || defined (__sgi)
   % make CC="cc  [-ansi]"      ... cc  [-ansi] -fullwarn
   % make CC="gcc [-ansi]"      ... gcc [-ansi] -pedantic -Wall 

1a. SGI (Irix 6.x) / Multiprocessor / 64 bits
    ???

2. SUN Sparc (SunOS)
   #if defined (sun) || defined (sparc)
   % make CC="gcc [-ansi]"      ... gcc [-ansi] -pedantic -Wall 

3. NeXT (Mach 3.2)
   %if defined (NeXT)
   % make                       ... cc -Wall

                (PLEASE: If you port the code to other systems, let me know.
                 My email-address is at the end of this file. Thanks. --Ernst.)


REMARKS

1. The code should compile with GNU's gcc: 'make CC=gcc new'

2. Unfortunately (and for reasons totally beyond my comprehension :)
   it can sometimes happen that 'make CC=gcc new' does not even start
   compiling!  If this is the case, check Makefile.tmp; if it's empty, type
   'make clear; make CC=gcc' ... and chances are that it will work.  Why?

3. If 'gcc -ansi -pedantic -Wall' gives you warnings en masse, eg,
        "warning: implicit declaration of function `fprintf'",
   it seems to me that gcc is not properly installed.
   Try 'gcc -ansi -D__USE_FIXED_PROTOTYPES__ -Wall' ...

4. Older versions of the code tried to be 'lint' clean... Now we're trying
   to be ANSI C and, in particular, 'gcc -ansi -pedantic -Wall' clean.
   This should give us pretty good portability!

5. NOTE: ANSI C casts char-bitfields to int-bitfields (and gives a warning).
   This means that certain code (eg, mkalf) will use more storage in ANSI
   and will have uncompatible binary files.  This should be fixed!

Good luck,
--Ernst.

/*--------------------------------------------------------------------------*/

  Ernst Mucke
  Computer Research & Applications Group, Los Alamos National Lab.
  <mucke@lanl.gov>    ------------ http://www.c3.lanl.gov/~emucke/
