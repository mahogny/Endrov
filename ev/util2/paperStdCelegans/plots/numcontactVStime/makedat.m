%set to 1 once we use OST4
%may need to rescale times piecewise linear when building the model
timestep=1; %[s]
initialframe=1380;
%this is cell ABa, 34 minutes

dat=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/numneigh.txt');

%curframe=(dat(:,1)-initialframe)*timestep + 17*60;

%temp, replace later. or? cell has been restricted more
%curframe does not match up between the two!!
%dat=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/volstats.txt');
%numcell=dat(:,2);
%g=length(numcell);


dat2=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/volstats.txt');
dat2=dat2(9:end,:); %hack. no idea why needed, FIX LATER


lastframe=dat2(end,1)
dat=dat(dat(:,2)<lastframe,:);

%better? matches vs duration
%OLD
%dat2=importdata('/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/data/henriksson/volstats.txt');
%lastframe=dat2(end,1);
%OLD

curframe=dat(:,1);
numcont=dat(:,2);
numcell=dat(:,3);


curframe=(curframe-initialframe)*timestep + 34*60;

%curframe=curframe(1:g);
%numcont=numcont(1:g);

N=curframe./60; %[min]
X=numcont./numcell;

N(end)

%TODO frame to time
%TODO shift in time

%write dat-file
%out=[N,X];
fp=fopen('series.dat','wt');
for i=1:length(N)
    fprintf(fp,'%f\t%f\n',N(i),X(i));
end
fclose(fp);