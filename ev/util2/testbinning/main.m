evminit

ost2=endrov.data.EvData.loadFile('/Volumes/TBU_main02/nonost/henrikssonBinningTest/newBinning2.tiff')
channel2=ost2.getChild('im0').channelImages.get('ch0');
%first frame is crap
frames2=channel2.imageLoader.keySet


ost4=endrov.data.EvData.loadFile('/Volumes/TBU_main02/nonost/henrikssonBinningTest/newerBinning4.tiff')
channel4=ost4.getChild('im0').channelImages.get('ch0');
frames4=channel4.imageLoader.keySet

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

