% evmGetIdObjects	Equivalent to EvData.getIdObjects but returns in a format suitable for Matlab.
function out=evmGetIdObjects(ost,class)

obid=ost.getIdObjects(getClass(class));
entries=obid.entrySet;
ei=entries.iterator;
i=1;
while ei.hasNext
    ke=ei.next;
    out(i).keystr=num2str(ke.getKey);
    out(i).keynum=ke.getKey;
%    out(i).keynum=str2num(ke.getKey);
    out(i).value=ke.getValue;
    i=i+1;
end