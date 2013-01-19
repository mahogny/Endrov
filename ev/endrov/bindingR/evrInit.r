.jaddClassPath("/Users/tbudev3/javaproj/ev");
#this line can be called multiple times, will not add the same path multiple times

#check evmInit, get classpath from ev core init


#http://www.rforge.net/rJava/index.html
#http://wiki.r-project.org/rwiki/doku.php?id=packages:cran:rjava






#convert code below to R


import endrov.matlab.*;

if ~exist('hasInitEv')
    hasInitEv=1;
   	
    evpath=which('evmInit');
    if length(evpath)==0
    	disp('====ERROR==== Cannot find Endrov. Check your path');
   	else
	    evpath=evpath(1:(length(evpath)-length('endrov/matlab/evmInit.m')))
	    javaaddpath(evpath)
	    jars=EvMatlab.getJars(evpath)
	    for i=1:size(jars,1)
		    char(jars(i))
	    	javaaddpath(char(jars(i)));
	    end
	   
	    logger=endrov.ev.StdoutLog
		endrov.ev.Log.listeners.add(logger)
	    
	    endrov.ev.EV.loadPlugins();
	    
	    evpath
	    
		clear logger
	    clear evpath
	    
	    disp('Longest java name you can access directly: ');
	    disp(namelengthmax);
    end
    
else
    disp('EV already initialized');
end


