

%thez=EvDecimal(13)


evim=channel.getImageLoader(theframe,thez);
pixels=evim.getPixels();


%percpixels=algPercentile.run(pixels, 30, 30, 0.5);
%percpixels=algPercentile.run(pixels, 20, 20, 0.5);
%percpixels=algPercentile.run(pixels, 20, 20, 0.9);
%percpixels=percpixels(1);
percpixels=algAverage.movingAverage(pixels,30,30);

c2=algMath.minus(pixels,percpixels);
%otsu=algThres.otsuThreshold(c2);


spotpixels=algCompare.greater(c2,2);
%spotpixels=algCompare.greater(c2,otsu); %otsu always says "1"


%spotpixels2=algCompare.greater(algAverage.movingSum(spotpixels,1,1),6); %9 is highest possible
spotpixels2=algCompare.greater(algAverage.movingSum(spotpixels,2,2),15); %25 is highest possible


binmask=[0,1,0;1,1,1;0,1,0];
binmaskpixels=EvPixels(EvPixels.TYPE_INT,3,3);
binmaskpixels.setArrayDouble2D(binmask);


%Removal can be done based on volume later on
%e=algMorph.close(spotpixels2,binmaskpixels,1,1);   %Really needed
e=spotpixels2;

%But an alternative could be a hit-and-miss to be more selective in removal


%f=algMorph.open(e,binmaskpixels,1,1);
f=e;


%image(e.getArrayDouble2D)

%algSpotcluster.exec2d(e,0)

%algAverage.movingSum(spotpixels,3,3)