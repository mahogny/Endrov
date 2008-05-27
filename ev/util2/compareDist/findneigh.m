% Barber, C.B., Dobkin, D.P., and Huhdanpaa, H.T., "The Quickhull 
%   algorithm for convex hulls," ACM Trans. on Mathematical Software,
%   22(4):469-483, Dec 1996, http://www.qhull.org.

evmInit

import evplugin.nuc.*;
import util2.compareDist.*;
import java.util.*;

%reference names
filepathref='/Volumes/TBU_main02/ostxml/model/stdcelegansNew.ostxml';
ostref=evplugin.data.EvData.loadFile(java.io.File(filepathref));
linsref=evmGetIdObjects(ostref,NucLineage);
linref=linsref.value;
% Get All names
disp('name indexing');
names=linref.nuc.keySet.toArray;
namesi=HashMap;
for i=1:length(names)
    namesi.put(names(i),i);
end


ncount1=findneighone('/Volumes/TBU_main02/ost4dgood/N2_071116',namesi);

%plot(sort(sum(ncount1>1)))

%break;

%%

ncount2=findneighone('/Volumes/TBU_main03/ost4dgood/TB2167_0804016',namesi);

%%

ncount3=findneighone('/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords',namesi);

%%

colormap('gray');

numc=size(ncount1,1);
ncount1=ncount1*numc/sum(diag(ncount1));
ncount2=ncount2*numc/sum(diag(ncount2));
ncount3=ncount3*numc/sum(diag(ncount3));

c=40;
subplot(2,3,1);
image(abs(ncount1)*c)
subplot(2,3,2);
image(abs(ncount2)*c)
subplot(2,3,3);
image(abs(ncount3)*c)

subplot(2,3,4);
image(abs(ncount1-ncount2)*c)
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