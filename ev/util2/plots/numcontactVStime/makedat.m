%set to 1 once we use OST4
%may need to rescale times piecewise linear when building the model
timestep=10; %[s]
initialframe=1020;
%this is cell AB, 17 minutes

dat=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/numneigh.txt');

curframe=(dat(:,1)-initialframe)*timestep + 17*60;
numcont=dat(:,2);
numcell=dat(:,3);

%temp, replace later. or? cell has been restricted more
%curframe does not match up between the two!!
dat=importdata('/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/data/henriksson/volstats.txt');
%numcell=dat(:,2);
g=length(numcell);


%better? matches vs duration
%dat2=importdata('/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/data/henriksson/volstats.txt');
%lastframe=dat2(end,1);


curframe=curframe(1:g);
numcont=numcont(1:g);

N=curframe./60; %[min]
X=numcont./numcell;

%TODO frame to time
%TODO shift in time

%write dat-file
%out=[N,X];
fp=fopen('series.dat','wt');
for i=1:length(N)
    fprintf(fp,'%f\t%f\n',N(i),X(i));
end
fclose(fp);