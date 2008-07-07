/* lia/Makefile.cpp --- C pre-processor Makefile for the Lia Library. */

/* USAGE: cpp -P ${CPPMK} > ${TMPMK}; make -k TMPMK=${TMPMK} -f ${TMPMK} ... */

/* Targets.  (CHANGE THIS ACCORDING TO YOUR NEEDS!) */
LIB=     ../lib
INCLUDE= ../include
CHMOD=   chmod u+rw,go+r

/* Paramters: DEPEND, SHELL, CC, CPP, CPPMK, TMPMK, COPT. */
/* System specifics: CCFLAGS, AR, RANLIB, MALLOC. */
#include "Makefile.sys"

/* Other libraries. */
INCLS = -I${INCLUDE}

/* Source and object files. */
MODULES= lia.c aux.c chars.c stack.c pool.c \
         det.c 
H_FILES= lia.h
H_EXPORT= lia.h
OBJECTS= ${MODULES:.c=.o}

/* Interface. */
normal: a mv;  @echo "Lia Library ready."
debug:  ;      ${MAKE} -k -f ${TMPMK} a COPT="-g -D__DEBUG__" mvd mv
remove: ;      rm -f ${OBJECTS}
all:    ;      ${MAKE} debug remove normal

/* Just testing. (SGI only!) */
testing_debug:; ${CC} ${CCFLAGS} -g -D__DEBUG__ -I${INCLUDE} -L${LIB} \
                      test.c -l_lia-g -l_basic-g -lm ${MALLOC}
testing:;       ${CC} ${CCFLAGS}                -I${INCLUDE} -L${LIB} \
                      test.c -l_lia-g -l_basic-g -lm ${MALLOC}

/* Internal rules. */
a:      ${OBJECTS};     ${AR} ${OBJECTS}; ${RANLIB}
mvd:    ;               mv lib_OUT.a lib_OUT-g.a
mv:     ${H_FILES};     @ /bin/csh -cf ' \
   echo "Moving targets ..."; \
   if (-e lib_OUT.a)      mv lib_OUT.a     ${LIB}/lib_lia.a; \
   if (-e lib_OUT-g.a)    mv lib_OUT-g.a   ${LIB}/lib_lia-g.a; \
   pushd ${LIB}; $(CHMOD) *lia.{a,ln}; popd; \
   cp ${H_EXPORT} ${INCLUDE}; \
   pushd ${INCLUDE}; $(CHMOD) ${H_EXPORT}; popd \
   '

/* Generic rule how to compile source files. */
.c.o: ${H_FILES}; ${CC} ${CCFLAGS} ${COPT} ${INCLS} -c $*.c -o $*.o

/* To compute dependencies with "make depend" ... */
depend:; ${DEPEND} -f${TMPMK} -- ${INCLS} ${MODULES}
