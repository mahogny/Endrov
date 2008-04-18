%GETVOXSTACK	Get voxel stack at a frame
%	Example:
%	[vox,zs]=getVoxStack(ost.channelImages.get('DIC'),1010);
function [vox,zs]=getVoxStack(channel,frame)
[pl,zs]=getEvStack(channel,frame);
for i=1:length(zs)
	vox(:,:,i)=pl(i).getArrayImage;
end
