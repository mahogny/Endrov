%Ensure that you are in the KFtools directory
dirname=pwd;


if ~prod(dirname(length(dirname)-length('KFtools')+1:length(dirname))=='KFtools')
   disp('Please find centroidmex.c')
   [filename, pathname] = uigetfile('centroidmex.c', 'Please find centroidmex.c');
   cd(pathname)
end


disp('Compiling centroidmex...')
mex centroidmex.c
disp('Compiling selectobjectmex...')
mex selectobjectmex.c
disp('Compiling watershedmex...')
mex watershedmex.c
disp('Compiling getboundarymex...')
mex getboundarymex.c
disp('Compiling palettemex...')
mex palettemex.c
