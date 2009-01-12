clear
%# contact vs duration of contact

%set to 1 once we use OST4
%may need to rescale times piecewise linear when building the model
timestep=10; %[s]

%data generated when CCM is generated
dat=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/contactdur.txt');

%assumed correct way
dat2=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/volstats.txt');
lastframe=dat2(end,1)

disp('tot num contacts, even after last frame');
size(dat,1)


%dat2=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/numneigh.txt');
%lastframe=dat2(end,1);
%length(dat)

dur=dat(dat(:,2)<lastframe,1);
dur2=dat(dat(:,3)<lastframe,1);

dur=dur.*timestep./60;
dur2=dur2.*timestep./60;

disp('tot num contacts1');
length(dur)
disp('num contact1 >2.5min');
length(dur(dur>2.5))

X=1:3:70;
N=hist(dur,X);
N2=hist(dur2,X);
N2=N-N2;

%write dat-file
fp=fopen('series.dat','wt');
for i=1:length(N)
	if mod(i,2)==1
	    fprintf(fp,'%f\t%f\t%d\n',N(i),N2(i),X(i));
    else
	    fprintf(fp,'%f\t%f\t\n',N(i),N2(i));    	
    end
end
fclose(fp);