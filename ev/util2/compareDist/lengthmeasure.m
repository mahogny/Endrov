function [linelength,lineindex]=measureline(filepath)

import evplugin.line.*;


%file=java.io.File(filepath);
%ost=evplugin.data.EvData.loadFile(file);
ost=evplugin.data.EvData.loadFile(java.io.File(filepath));

foo=EvLine;
%apa=ost.getObjects(foo.getClass)
%apa.toArray
%whos apa
lin=ost.getObjects(foo.getClass).toArray;
%global linid;

linelength=[];
lineindex={''};
try
    linid=evmGetIdObjects(ost,EvLine);


    for i=1:length(lin)
        linelength(i)=lin(i).getTotalDistance;
        lineindex(i)={linid(i).keystr};
    end

    if length(lin)==0
        system(['osascript /Volumes/TBU_main02/userdata/AppleScripts.xeon/setColor.applescript ',filepath,' 2']);
    else
        system(['osascript /Volumes/TBU_main02/userdata/AppleScripts.xeon/setColor.applescript ',filepath,' 0']);

    end
end
end
