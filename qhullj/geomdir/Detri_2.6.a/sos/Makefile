# Generic Makefile using Makefile.{cpp,sys,[tmp]} and the C pre-processor.

# GENERIC MAKE PARAMETERS.
#                 (Not all af them are necessarily defined!)
#       normal  : normal compilation (with -O flag)
#       debug   : compile for dbx debugging (with -g flag)
#       clear   : remove objectfiles and reset state of program/library
#       new     : (clear + normal)
#       all     : ([clear + debug] + clear + normal)
#       mv      :
#       a       :

# Parameters, passed down to lower-level make, and their default settings.
CC= cc
CPP= ${CC} -E
COPT= -O
SHELL= /bin/sh
CPPMK= Makefile.cpp
TMPMK= Makefile.tmp

# Other Parameters.
PREREQU= Makefile.sys ${CPPMK}

# Lower-level make.
MK= ${MAKE} -k  CC="${CC}" CPP="${CPP}" COPT="${COPT}" \
    SHELL="${SHELL}" CPPMK="${CPPMK}" TMPMK=${TMPMK} -f ${TMPMK}

# Interface.
normal:   ;             ${MAKE} CPP="${CPP}" ${TMPMK}; ${MK} $@
.DEFAULT: ;             ${MAKE} CPP="${CPP}" ${TMPMK}; ${MK} $@
all:      clear;        ${MAKE} CPP="${CPP}" ${TMPMK}; ${MK} all
new:      clear;        ${MAKE} CPP="${CPP}" ${TMPMK}; ${MK} normal
clear:    stdclear objclear; rm -f Makefile.tmp*
clean:    clear;
${TMPMK}: ${PREREQU};   ${CPP} ${CPPMK} > ${TMPMK}; ${MK} depend
stdclear: ;             rm -f *.tmp *.tmp.bak lib* core
objclear: ;             ${MAKE} CPP="${CPP}" ${TMPMK}; ${MAKE} remove
