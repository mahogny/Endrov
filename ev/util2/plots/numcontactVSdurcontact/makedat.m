%# contact vs duration of contact

%set to 1 once we use OST4
%may need to rescale times piecewise linear when building the model
timestep=10; %[s]

%data generated when CCM is generated
dat=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/numneigh.txt');

dat=dat./timestep;

%bins=linspace(0,0.4,15);
%[N,X]=hist(dstraight);
[N,X]=hist(lifedev); %hist(lifedev,bins);

%write dat-file
%out=[N,X];
fp=fopen('series.dat','wt');
for i=1:length(N)
    fprintf(fp,'%f\t%f\n',N(i),X(i));
end
fclose(fp);