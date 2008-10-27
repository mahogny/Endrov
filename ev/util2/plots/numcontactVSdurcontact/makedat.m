%# contact vs duration of contact







%set to 1 once we use OST4
%may need to rescale times piecewise linear when building the model
timestep=10; %[s]
initialframe=0;

dat=importdata('/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/data/henriksson/volstats.txt');

curframe=(dat(:,1)-initialframe)/timestep;
numcell=dat(:,2);

N=curframe;
X=numcell;

%write dat-file
%out=[N,X];
fp=fopen('series.dat','wt');
for i=1:length(N)
    fprintf(fp,'%f\t%f\n',N(i),X(i));
end
fclose(fp);