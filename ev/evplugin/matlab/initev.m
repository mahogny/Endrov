%INITEV	Inititalize Endrov
%	Must be called first, once, to set up EV. Example:
%
%	initev('/Users/tbudev3/javaproj/ev/')
%
%	It is important that the path ends with /.

function initev(evpath)

import evplugin.matlab.*;

javaaddpath(evpath)
jars=EvMatlab.getJars(evpath)
for i=1:size(jars,1)
	javaaddpath(char(jars(i)));
end

disp('Longest java name you can access directly: ');
disp(namelengthmax);
