/* basic/Makefile.cpp --- C pre-processor Makefile for the Basic C Library. */

/* USAGE: cpp -P ${CPPMK} > ${TMPMK}; make -k TMPMK=${TMPMK} -f ${TMPMK} ... */

/* Targets.  (CHANGE THIS ACCORDING TO YOUR NEEDS!) */
LIB=     ../lib
INCLUDE= ../include
CHMOD=   chmod u+rw,go+r

/* Paramters: DEPEND, SHELL, CC, CPP, CPPMK, TMPMK, COPT. */
/* System specifics: CCFLAGS, AR, RANLIB, MALLOC. */
#include "Makefile.sys"

/* Source, header, and object files. */
MODULES= basic.c files.c malloc.c getarg.c \
         cb.c cb_doprnt.c \
         isort.c prime.c uhash.c istaque.c time.c counter.c arg.c \
         qsort.c tokenize.c math2.c \
         iit.c \
         heapsort.c uf.c
H_FILES= basic.h
H_EXPORT= basic.h
OBJECTS= ${MODULES:.c=.o}

/* Interface. */
normal: a mv;  @echo "Basic C Library ready."
debug:  ;      ${MAKE} -k -f ${TMPMK} a COPT="-g -D__DEBUG__" mvd mv
remove: ;      rm -f ${OBJECTS}
all:    ;      ${MAKE} debug remove normal

/* Internal rules. */
a:      zap ${OBJECTS}; ${AR} ${OBJECTS}; ${RANLIB}
mvd:    ;               mv lib_OUT.a lib_OUT-g.a
mv:     ${H_FILES};     @ /bin/csh -cf ' \
   echo "Moving targets ..."; \
   if (-e lib_OUT.a)      mv lib_OUT.a     ${LIB}/lib_basic.a; \
   if (-e lib_OUT-g.a)    mv lib_OUT-g.a   ${LIB}/lib_basic-g.a; \
   pushd ${LIB}; $(CHMOD) *basic.{a,ln}; popd; \
   cp ${H_EXPORT} ${INCLUDE}; \
   pushd ${INCLUDE}; $(CHMOD) ${H_EXPORT}; popd \
   '

/* Check for nonexisting modules. */
zap:; @/bin/csh -cf 'Makefile.zap ${MODULES}'

/* The rule how to compile source files. */
.c.o: ${H_FILES}; ${CC} ${CCFLAGS} ${COPT} -c $*.c -o $*.o

/* To compute dependencies with "make depend" ... */
depend: zap; ${DEPEND} -f${TMPMK} -- ${H_FILES} ${MODULES}
