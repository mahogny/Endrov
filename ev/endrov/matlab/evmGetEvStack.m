%evmGETEVSTACK	Get image planes at a frame as an array of EvImage
%	Example:
%	[pl,zs]=getEvStack(ost.channelImages.get('DIC'),1010);
function [pl,zs]=evmGetEvStack(channel,frame)
pl=[];
slices=channel.getStack(frame);
zs=endrov.matlab.EvMatlab.keySetEvDecimal(slices);
pl = javaArray('endrov.imageset.EvImage', length(zs));
for zi=1:length(zs)
    oneSlice=slices.get(zs(zi));
    pl(zi)=oneSlice;
end
