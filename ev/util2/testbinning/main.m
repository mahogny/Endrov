evminit

ost2=endrov.data.EvData.loadFile('/Volumes/TBU_main02/nonost/henrikssonBinningTest/newBinning2.tiff')
channel2=ost2.getChild('im0').channelImages.get('ch0');
%first frame is crap
frames2=channel2.imageLoader.keySet

clear stack2;
for i=1:30
    p=channel2.imageLoader.get(EvDecimal(i)).getPixels;
    p=p(1);
    arr=p.getArrayDouble2D;
    stack2(i,:,:)=arr;
end
stack2a=squeeze(mean(stack2));
%image(stack2a)

scalestack2a=(stack2a(1:2:end,1:2:end)+stack2a(1+(1:2:end),1:2:end)+stack2a(1+(1:2:end),1+(1:2:end))+stack2a((1:2:end),1+(1:2:end)));


%colormap('gray');
%axis equal

ost4=endrov.data.EvData.loadFile('/Volumes/TBU_main02/nonost/henrikssonBinningTest/newerBinning4.tiff')
channel4=ost4.getChild('im0').channelImages.get('ch0');
frames4=channel4.imageLoader.keySet

clear stack4;
for i=1:30
    p=channel4.imageLoader.get(EvDecimal(i)).getPixels;
    p=p(1);
    arr=p.getArrayDouble2D;
    stack4(i,:,:)=arr;
end
stack4a=squeeze(mean(stack4));
%image(stack4a/4)

%For binning 2
p=channel2.imageLoader.get(EvDecimal(1)).getPixels;
p=p(1);
arr=p.getArrayDouble2D;
w=p.getWidth;
h=p.getHeight;

arr1=reshape(arr,w*h,1);

hist(arr1,0:255);




%For binning 4
p=channel4.imageLoader.get(EvDecimal(1)).getPixels;
p=p(1);
arr=p.getArrayDouble2D;
w=p.getWidth;
h=p.getHeight;

arr1=reshape(arr,w*h,1);

hist(arr1,0:255);



%
%hist(squeeze(stack4a(:,1,1)),50)
std(squeeze(stack2a(:,1,1)*4))
std(squeeze(scalestack2a(:,1,1)))
std(squeeze(stack4a(:,1,1)))

mean(std(scalestack2a))
mean(std(stack4a))




