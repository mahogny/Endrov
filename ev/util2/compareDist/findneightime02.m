%function findneightime();

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

%findneighonetime('/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords',namesi,names,1750,1);
findneighonetime('/Volumes/TBU_main02/ost4dgood/N2_071116',namesi,names,1750,6);
%findneighonetime('/Volumes/TBU_main03/ost4dgood/TB2167_0804016',namesi,names,1750,6);
%findneighonetime('/Volumes/TBU_main02/ost4dgood/N2_071114/',namesi,names,1750,6);
%findneighonetime('/Volumes/TBU_main02/ost4dgood/N2greenLED080206',namesi,names,1750,6);
%findneighonetime('/Volumes/TBU_main02/ost4dgood/stdcelegansNew',namesi,names,1750,6);
%findneighonetime('/Volumes/TBU_main02/ost4dgood/TB2164_080118',namesi,names,1750,6);
%findneighonetime('/Volumes/TBU_main02/ost4dgood/TB2142_071129',namesi,names,1750,6);

%findneighonetime(ostpath,namesi,names,1750,dt*tcorr);
