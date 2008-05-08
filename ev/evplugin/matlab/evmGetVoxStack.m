%evmGETVOXSTACK	Get voxel stack at a frame
%	Example:
%	[vox,zs]=evmGetVoxStack(ost.channelImages.get('DIC'),1010);
function [vox,zs]=evmGetVoxStack(channel,frame)
[pl,zs]=getEvStack(channel,frame);
for i=1:length(zs)
	vox(:,:,i)=pl(i).getArrayImage;
end
