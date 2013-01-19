function c=evmNewClassLoader()
import endrov.matlab.*;
evpath=which('evmInit');
evpath=evpath(1:(length(evpath)-length('endrov/bindingMatlab/evmInit.m')));
c=EvMatlab.getClassLoader(['file://',evpath]);
