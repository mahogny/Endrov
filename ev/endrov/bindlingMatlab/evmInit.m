%INITEV	Inititalize Endrov
%	Must be called first, once, to set up EV.
%   Example: initev()
%
%   Warnings about already included jars may appear, nothing to worry
%   about. To reinit Endrov, use "clear" first (or clear hasInitEv).

import endrov.matlab.*;
import java.util.*;

if ~exist('hasInitEv')
    
	evpath=which('evmInit');
	if length(evpath)==0
		disp('====ERROR==== Cannot find Endrov. Check your path');
	else
		evpath=evpath(1:(length(evpath)-length('endrov/bindingMatlab/evmInit.m')));
		javaaddpath(evpath);
        
		jarbefore=javaclasspath('-all');
		jarbeforelist=java.util.LinkedList; %Can use to eliminate warnings
        
		jars=EvMatlab.getJars(evpath,matlabroot,computer('arch'));
		cellpath={};
		for i=1:size(jars,1)
			cellpath{i}=char(jars(i));
		end
        
		javaaddpath(cellpath);

		logger=endrov.core.log.EvLogStdout;
		endrov.core.log.EvLog.addListener(logger);
	    
		endrov.core.EndrovCore.loadPlugins();
	    
        %evpath
	    
		clear logger
		clear evpath
		clear jarbefore
		clear jarbeforelist
	    
		disp('Longest java name you can access directly: ');
		disp(namelengthmax);
	end
    
    
	hasInitEv=1;
else
	disp('EV already initialized');
end

import endrov.util.*;

