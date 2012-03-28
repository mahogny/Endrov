%evmGETVOXSTACK	Get voxel stack at a frame
%	Example:
%	[vox,zs]=evmGetVoxStack(dat.getChild.get('DIC'),1010);
function [vox,zs]=evmGetVoxStack(channel,frame)
[pl,zs]=evmGetEvStack(channel,frame);
for i=1:length(zs)
	vox(:,:,i)=pl(i).getPixes().getArrayImage;
end
