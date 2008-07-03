% Barber, C.B., Dobkin, D.P., and Huhdanpaa, H.T., "The Quickhull 
%   algorithm for convex hulls," ACM Trans. on Mathematical Software,
%   22(4):469-483, Dec 1996, http://www.qhull.org.

evmInit

import endrov.nuc.*;
import util2.compareDist.*;
import java.util.*;

%reference names
filepathref='/Volumes/TBU_main02/ostxml/model/stdcelegansNew.ostxml';
ostref=endrov.data.EvData.loadFile(java.io.File(filepathref));
linsref=evmGetIdObjects(ostref,NucLineage);
linref=linsref.value;
% Get All names
disp('name indexing');
names2=linref.nuc.keySet;
names=TreeSet;
names.addAll(names2);
names=names.toArray;
namesi=HashMap;
for i=1:length(names)
    namesi.put(names(i),i);
end


dt=2;

ncount1=findneighone('/Volumes/TBU_main02/ost4dgood/N2_071116',namesi,names,1750,dt*6);



%plot(sort(sum(ncount1>1)))

%break;

%%

ncount2=findneighone('/Volumes/TBU_main03/ost4dgood/TB2167_0804016',namesi,names,1750,dt*6);

%%


ncount3=findneighone('/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords',namesi,names,250, dt);
%ncount3=ncount2;

%%
ncount4=findneighone('/Volumes/TBU_main02/ost4dgood/N2_071114/',namesi,names,1750,dt*6);
ncount4=findneighone('/Volumes/TBU_main02/ost4dgood/N2greenLED080206/',namesi,names,1750,dt*6);
ncount4=findneighone('/Volumes/TBU_main02/ost4dgood/stdcelegansNew/',namesi,names,1750,dt*6);
ncount4=findneighone('/Volumes/TBU_main02/ost4dgood/TB2164_080118/',namesi,names,1750,dt*6);
ncount4=findneighone('/Volumes/TBU_main02/ost4dgood/TB2142_071129',namesi,names,1750,dt*6);

%%
ncount1b=ncount1;
ncount2b=ncount2;
ncount3b=ncount3;



%%

colormap('gray');

% %?
numc=size(ncount1,1);
% ncount1=ncount1*numc/sum(diag(ncount1));
% ncount2=ncount2*numc/sum(diag(ncount2));
% ncount3=ncount3*numc/sum(diag(ncount3));
% 
% %?
% ncount1=ncount1*255/max(max(ncount1));
% ncount2=ncount2*255/max(max(ncount2));
% ncount3=ncount3*255/max(max(ncount3));

for i=1:numc
    ncount1(i,i)=0;
    ncount2(i,i)=0;
    ncount3(i,i)=0;
end

for i=1:numc
    s=sum(ncount1(i,:));
    if s==0
        s=1;
    end
    ncount1(i,:)=ncount1(i,:)/s;

    s=sum(ncount2(i,:));
    if s==0
        s=1;
    end
    ncount2(i,:)=ncount2(i,:)/s;

    s=sum(ncount3(i,:));
    if s==0
        s=1;
    end
    ncount3(i,:)=ncount3(i,:)/s;

end




%?
%row-wise?

c=250;
c=300;
subplot(2,3,1);
image(abs(ncount1)*c)
subplot(2,3,2);
image(abs(ncount2)*c)
subplot(2,3,3);
image(abs(ncount3)*c)

subplot(2,3,4);
image(abs(ncount1-ncount2)*c)
%image(sqrt(ncount1*ncount2)*c)
subplot(2,3,5);
image(abs(ncount1-ncount3)*c)
subplot(2,3,6);
image(abs(ncount2-ncount3)*c)


%%average differences in cell contacts
disp('avdif')
sum(mean(abs(ncount1-ncount2)))
sum(mean(abs(ncount1-ncount3)))
sum(mean(abs(ncount2-ncount3)))


%plot(sort(sum(ncount1>1)))

