function c=evmNewClassLoader()
import endrov.matlab.*;
evpath=which('evmInit');
evpath=evpath(1:(length(evpath)-length('endrov/matlab/evmInit.m')));
c=EvMatlab.getClassLoader(['file://',evpath]);
