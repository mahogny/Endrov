%INITEV	Inititalize Endrov
%	Must be called first, once, to set up EV.
%   Example: initev()
%
%   Warnings about already included jars may appear, nothing to worry
%   about. To reinit EV, use "clear" first (or clear hasInitEv).

import evplugin.matlab.*;

if ~exist('hasInitEv')
    hasInitEv=1;
   	
    evpath=which('initev');
    evpath=evpath(1:(length(evpath)-length('evplugin/matlab/initev.m')))
    javaaddpath(evpath)
    jars=EvMatlab.getJars(evpath)
    for i=1:size(jars,1)
    	javaaddpath(char(jars(i)));
    end
   
    logger=evplugin.ev.StdoutLog
	evplugin.ev.Log.listeners.add(logger)
    
    evplugin.ev.EV.loadPlugins(java.io.File(evpath));
    
	clear logger
    clear evpath
    
    disp('Longest java name you can access directly: ');
    disp(namelengthmax);
else
    disp('EV already initialized');
end



