

%thez=EvDecimal(13)


evim=channel.getImageLoader(theframe,thez);
pixels=evim.getPixels();


%tic
percpixels=algPercentile.run(pixels, 20, 20, 0.9);
%toc
percpixels=percpixels(1);
c2=algMath.minus(pixels,percpixels);
spotpixels=algCompare.greater(c2,0);
binmask=[0,1,0;1,1,1;0,1,0];
binmaskpixels=EvPixels(EvPixels.TYPE_INT,3,3);
binmaskpixels.setArrayDouble2D(binmask);


e=algMorph.close(spotpixels,binmaskpixels,1,1);
f=algMorph.open(e,binmaskpixels,1,1);

%image(e.getArrayDouble2D)

%algSpotcluster.exec2d(e,0)

