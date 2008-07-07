/* sos/Makefile.cpp --- C pre-processor Makefile for the SoS Library. */ 

/* USAGE: cpp -P ${CPPMK} > ${TMPMK}; make -k TMPMK=${TMPMK} -f ${TMPMK} ... */

/* Targets.  (CHANGE THIS ACCORDING TO YOUR NEEDS!) */
LIB=     ../lib
INCLUDE= ../include
CHMOD=   chmod u+rw,go+r

/* Paramters: DEPEND, SHELL, CC, CPP, CPPMK, TMPMK, COPT. */
/* System specifics: CCFLAGS, AR, RANLIB, MALLOC. */
#include "Makefile.sys"

/* Other libraries. */
INCLS =  -I${INCLUDE}

/* Source and object files. */
MODULES= sos.c minor.c primitive.c \
         positive3.c \
         in_sphere.c \
         lambda3.c lambda4.c lambda5.c \
         smaller.c
/* Feel free to move unused primitive modules from MODULES
   to SKIPPED_MODULES in order to speed up compilation. */
SKIPPED_MODULES= above3.c above4.c \
                 lambda3_star.c lambda4_star.c \
                 above3_star.c \
                 rho1.c rho1_num.c \
                 rho2.c rho2_num.c rho2_den.c \
                 rho3.c rho3_num.c rho3_den.c 
H_FILES= sos.h primitive.h internal.h
H_EXPORT= sos.h
OBJECTS= ${MODULES:.c=.o}

/* Interface. */
normal: a mv;  @echo "SoS Library ready."
debug:  ;      ${MAKE} -k -f ${TMPMK} a COPT="-g -D__DEBUG__" mvd mv
remove: ;      rm -f ${OBJECTS}
all:    ;      ${MAKE} debug remove normal;

/* Internal rules. */
a:      ${OBJECTS};     ${AR} ${OBJECTS}; ${RANLIB}
mvd:    ;               mv lib_OUT.a lib_OUT-g.a
mv:     ${H_FILES};     @ /bin/csh -cf ' \
   echo "Moving tragets ..."; \
   if (-e lib_OUT.a)     mv lib_OUT.a     ${LIB}/lib_sos.a; \
   if (-e lib_OUT-g.a)   mv lib_OUT-g.a   ${LIB}/lib_sos-g.a; \
   pushd ${LIB}; $(CHMOD) *lia.{a,ln}; popd; \
   cp ${H_EXPORT} ${INCLUDE}; \
   pushd ${INCLUDE}; $(CHMOD) ${H_EXPORT}; popd \
   '

/* Generic rule how to compile source files. */
.c.o: ${H_FILES}; ${CC} ${CCFLAGS} ${COPT} ${INCLS} -c $*.c -o $*.o

/* To compute dependencies with "make depend" ... */
depend:; ${DEPEND} -f${TMPMK} -- ${INCLS} ${MODULES}
