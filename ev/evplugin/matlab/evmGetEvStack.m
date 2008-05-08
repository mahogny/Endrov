%evmGETEVSTACK	Get image planes at a frame as an array of EvImage
%	Example:
%	[pl,zs]=getEvStack(ost.channelImages.get('DIC'),1010);
function [pl,zs]=evmGetEvStack(channel,frame)
pl=[];
zs=evplugin.matlab.EvMatlab.keySetInt(channel.imageLoader.get(int32(frame)));
slices=channel.imageLoader.get(int32(frame));
for zi=1:length(zs)
	z=zs(zi);
    pl=[pl;slices.get(int32(z))];
end