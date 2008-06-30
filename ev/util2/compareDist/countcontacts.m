%%
load('/Volumes/TBU_main02/ost4dgood/stdcelegansNew/data/newneigh.mat');
%%

nc=ncount;
for (q=1:575)
    for (r=1:q)
        nc(q,r)=0;
    end
end

f=3;
bins=20;

image(ncount*f);

image(nc*f);

[x,y]=hist((nc(nc>0)-1)/2,bins); %/2 (*30s/60s => min), -1: self contacts
hist((nc(nc>0)-1)/2,bins);

x
y

f=fopen('contacthist.dat','w');
for (n=1:length(x))
    fprintf(f,'%f\t%f\n',x(n),y(n));
end
fclose(f);
