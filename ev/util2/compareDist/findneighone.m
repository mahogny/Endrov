function ncount=findneighone(filepath,namesi,names,newmax,dt)

import endrov.nuc.*;
import util2.compareDist.*;
import java.util.*;


ost=endrov.data.EvData.loadFile(java.io.File(filepath));
lins=evmGetIdObjects(ost,NucLineage);
lin=lins.value;



disp('loading done');


%% Make sure no cells stick around
disp('ending');
td=TravelDist;
td.endAllCellsMatlab(lin);


%% Find first and last keyframe
disp('finding first and last frame');
nucs=lin.nuc.values.toArray;
maxframe=-10000;
minframe=10000;
for i=1:length(nucs)
    nv=nucs(i);
    if ~nv.pos.isEmpty
        if nv.pos.firstKey<minframe
            minframe=nv.pos.firstKey;
        end
        if nv.pos.lastKey>maxframe
            maxframe=nv.pos.lastKey;
        end
    end 
end
[minframe,maxframe]
length(nucs)
%maxframe=1500;
%maxframe=newmax;





%%
disp('count neigh');
ncount=zeros(namesi.size);
notneigh=0;

for curframe=[minframe:dt:maxframe]
    curframe
    
    %Collect points
    inter=lin.getInterpNuc(curframe);
    interkey=inter.keySet.toArray;
    
    clear nucpos
    clear thesename
    thesename={};
    j=1;
    for i=1:length(interkey)
        tempname=interkey(i).snd;
        % 080703: take this out 
        if curframe<td.getStr(lin.nuc,tempname).pos.firstKey
            continue;
        end
        if curframe>td.getStr(lin.nuc,tempname).pos.lastKey
            continue;
        end
        
        
        thesename{j}=interkey(i).snd;
        ia=inter.get(interkey(i));
        nucpos(j,:)=[ia.pos.x,ia.pos.y,ia.pos.z];
        j=j+1;
    end
    numpoint=length(thesename);
    
    try
        %Voronoi
        [v,c]=voronoin(nucpos);

        %Find neighbours
        for i=1:numpoint
            if strcmp(cell2mat(thesename(i)),'ABarp')
                curframe
            end
            for j=1:numpoint
%                 mm=max([c{i},c{j}]);
%                 list1=zeros(mm,1);
%                 list2=zeros(mm,1);
%                 list1(c{i})=1;
%                 list2(c{j})=1;
%                 ijneigh=any(list1 & list2);

                setinter=intersect(c{i},c{j});
                setinter=setinter(setinter~=1);
                if length(setinter)>0
                    ncount(namesi.get(cell2mat(thesename(i))),namesi.get(cell2mat(thesename(j))))=ncount(namesi.get(cell2mat(thesename(i))),namesi.get(cell2mat(thesename(j))))+1;
                    if strcmp(cell2mat(thesename(i)),'ABarp')
                        disp([cell2mat(thesename(i)),' ', cell2mat(thesename(j))])
                    end
%                    setinter
                else
%                    disp(['not ', cell2mat(thesename(i)),' ', cell2mat(thesename(j))])
                    notneigh=notneigh+1;
                end
            end
        end
    catch
        666
    end
    
end
disp('done count neigh');
notneigh


save([filepath '/data/newneigh_080703.mat'],'ncount','names');

