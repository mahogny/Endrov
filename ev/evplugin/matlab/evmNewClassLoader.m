function c=evmNewClassLoader()
import evplugin.matlab.*;
evpath=which('initev');
evpath=evpath(1:(length(evpath)-length('evplugin/matlab/initev.m')));
c=EvMatlab.getClassLoader(['file://',evpath]);
